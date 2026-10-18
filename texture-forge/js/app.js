/* Texture Forge — UI, worker pool, downloads and microstock metadata. */
(function () {
  'use strict';

  const { STYLES } = window.TFTextures;
  const { mulberry32 } = window.TFNoise;

  const RATIOS = [
    { id: '3:2', w: 4500, h: 3000 },
    { id: '16:9', w: 3840, h: 2160 },
    { id: '4:3', w: 4000, h: 3000 },
    { id: '1:1', w: 3000, h: 3000 },
    { id: '4:5', w: 2400, h: 3000 },
    { id: '2:3', w: 3000, h: 4500 },
    { id: '9:16', w: 2160, h: 3840 },
  ];
  const SCALES = [
    { id: 1, label: 'Full', note: 'stock-ready' },
    { id: 0.5, label: '50%', note: 'faster' },
    { id: 0.25, label: 'Preview', note: 'quick look' },
  ];
  const COUNTS = [5, 10, 15, 20, 30];

  const state = {
    ratio: RATIOS[0],
    scale: 1,
    styles: new Set(STYLES.map((s) => s.id)),
    count: 15,
    results: [],
    running: false,
  };

  const $ = (id) => document.getElementById(id);
  const pad = (n) => String(n).padStart(2, '0');
  const slug = (s) => s.toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/^-|-$/g, '');

  // ---------- settings UI ----------
  function chip(label, small, pressed, onClick) {
    const b = document.createElement('button');
    b.type = 'button';
    b.className = 'chip';
    b.setAttribute('aria-pressed', String(pressed));
    b.textContent = label;
    if (small) { const s = document.createElement('small'); s.textContent = small; b.appendChild(s); }
    b.addEventListener('click', onClick);
    return b;
  }

  function renderSingle(container, items, isOn, pick) {
    container.replaceChildren(...items.map((it) => chip(it.label, it.small, isOn(it), () => {
      pick(it);
      for (const c of container.children) c.setAttribute('aria-pressed', 'false');
      container.children[items.indexOf(it)].setAttribute('aria-pressed', 'true');
    })));
  }

  renderSingle($('ratios'), RATIOS.map((r) => ({ ...r, label: r.id, small: `${r.w} × ${r.h}` })), (r) => r.id === state.ratio.id, (r) => { state.ratio = RATIOS.find((x) => x.id === r.id); });
  renderSingle($('scales'), SCALES.map((s) => ({ ...s, small: s.note })), (s) => s.id === state.scale, (s) => { state.scale = s.id; });
  renderSingle($('counts'), COUNTS.map((n) => ({ n, label: String(n) })), (c) => c.n === state.count, (c) => { state.count = c.n; });

  const stylesBox = $('styles');
  function renderStyles() {
    stylesBox.replaceChildren(...STYLES.map((s) => chip(s.name, null, state.styles.has(s.id), (e) => {
      if (state.styles.has(s.id)) { if (state.styles.size > 1) state.styles.delete(s.id); } else state.styles.add(s.id);
      e.currentTarget.setAttribute('aria-pressed', String(state.styles.has(s.id)));
      syncToggle();
    })));
    syncToggle();
  }
  function syncToggle() {
    $('toggleAll').textContent = state.styles.size === STYLES.length ? 'Select one' : 'Select all';
  }
  $('toggleAll').addEventListener('click', () => {
    state.styles = state.styles.size === STYLES.length ? new Set([STYLES[0].id]) : new Set(STYLES.map((s) => s.id));
    renderStyles();
  });
  renderStyles();

  const seedInput = $('seed');
  const randomSeed = () => { seedInput.value = Math.floor(Math.random() * 1e9); };
  randomSeed();
  $('dice').addEventListener('click', randomSeed);

  const quality = $('quality');
  const showQ = () => { $('qv').textContent = quality.value + '%'; };
  quality.addEventListener('input', showQ);
  showQ();

  // ---------- worker pool ----------
  let workers = [];
  function poolSize() {
    const cores = navigator.hardwareConcurrency || 4;
    const mem = navigator.deviceMemory || 8;
    return Math.max(1, Math.min(cores - 1, mem <= 4 ? 2 : 4));
  }
  function stopWorkers() {
    for (const w of workers) w.terminate();
    workers = [];
  }

  // Main-thread JPEG encoding, used only when the browser lacks OffscreenCanvas in workers.
  function encodeOnMain(buffer, W, H, q) {
    const c = document.createElement('canvas');
    c.width = W; c.height = H;
    c.getContext('2d').putImageData(new ImageData(new Uint8ClampedArray(buffer), W, H), 0, 0);
    const toBlob = (cv, qq) => new Promise((res) => cv.toBlob(res, 'image/jpeg', qq));
    const tw = Math.min(520, W), th = Math.round((H * tw) / W);
    const t = document.createElement('canvas');
    t.width = tw; t.height = th;
    t.getContext('2d').drawImage(c, 0, 0, tw, th);
    return Promise.all([toBlob(c, q), toBlob(t, 0.85)]).then(([blob, thumb]) => ({ blob, thumb }));
  }

  // ---------- jobs ----------
  function planJobs() {
    const base = Math.max(0, parseInt(seedInput.value, 10) || 0);
    const rnd = mulberry32(base);
    const ids = [...state.styles];
    const W = Math.round(state.ratio.w * state.scale), H = Math.round(state.ratio.h * state.scale);
    // Shuffle so a batch spreads across all selected styles before repeating one.
    let bag = [];
    const jobs = [];
    for (let i = 0; i < state.count; i++) {
      if (!bag.length) {
        bag = ids.slice();
        for (let k = bag.length - 1; k > 0; k--) { const j = Math.floor(rnd() * (k + 1)); [bag[k], bag[j]] = [bag[j], bag[k]]; }
      }
      const styleId = bag.pop();
      const style = window.TFTextures.BY_ID[styleId];
      const palIdx = Math.floor(rnd() * style.palettes.length);
      const seed = (base + (i + 1) * 7919) >>> 0;
      const pal = style.palettes[palIdx];
      jobs.push({
        id: i, styleId, palIdx, W, H, seed,
        quality: quality.value / 100,
        file: `${slug(pal.name)}-${styleId}-${seed}.jpg`,
        styleName: style.name, palName: pal.name,
      });
    }
    return jobs;
  }

  function makeCard(job) {
    const card = document.createElement('article');
    card.className = 'card';
    card.innerHTML = `
      <a class="thumb" style="--ar:${job.W} / ${job.H}" target="_blank" rel="noopener">
        <div class="pending">QUEUED</div><div class="mini"><i></i></div>
      </a>
      <div class="meta">
        <div><div class="name">#${pad(job.id + 1)} · ${job.file}</div><div class="sub">${job.palName} · ${job.styleName} · ${job.W}×${job.H}</div></div>
        <a class="dl" aria-disabled="true" download="${job.file}">⬇ JPG</a>
      </div>`;
    return card;
  }

  async function generate() {
    if (state.running) return;
    clearResults();
    const jobs = planJobs();
    state.running = true;
    setBusy(true);

    const grid = $('grid');
    const cards = jobs.map(makeCard);
    grid.replaceChildren(...cards);

    const progress = new Array(jobs.length).fill(0);
    let done = 0, next = 0;
    const update = () => {
      const p = progress.reduce((a, b) => a + b, 0) / jobs.length;
      $('bar').style.width = (p * 100).toFixed(1) + '%';
      $('status').textContent = done < jobs.length ? `Generating ${Math.min(done + 1, jobs.length)} / ${jobs.length} …` : `Done — ${jobs.length} backgrounds`;
    };
    update();

    const finishJob = (job, blob, thumb) => {
      const card = cards[job.id];
      const url = URL.createObjectURL(blob);
      const turl = URL.createObjectURL(thumb);
      const a = card.querySelector('.thumb');
      a.href = url;
      a.replaceChildren(Object.assign(document.createElement('img'), { src: turl, alt: `${job.palName} ${job.styleName} texture`, loading: 'lazy' }));
      const dl = card.querySelector('.dl');
      dl.href = url;
      dl.removeAttribute('aria-disabled');
      state.results.push({ ...job, blob, url, turl });
      progress[job.id] = 1;
      done++;
      update();
    };

    await new Promise((resolve) => {
      const n = Math.min(poolSize(), jobs.length);
      let active = 0;
      const feed = (w) => {
        if (!state.running || next >= jobs.length) {
          if (active === 0) resolve();
          return;
        }
        const job = jobs[next++];
        active++;
        cards[job.id].querySelector('.pending').textContent = 'RENDERING';
        w.onmessage = async (e) => {
          const m = e.data;
          if (m.type === 'progress') {
            progress[job.id] = m.p * 0.9;
            cards[job.id].querySelector('.mini i').style.width = (m.p * 100).toFixed(0) + '%';
            update();
            return;
          }
          if (m.type === 'done') finishJob(job, m.blob, m.thumb);
          else if (m.type === 'pixels') {
            const r = await encodeOnMain(m.buffer, m.W, m.H, job.quality);
            finishJob(job, r.blob, r.thumb);
          }
          active--;
          feed(w);
          if (active === 0 && (next >= jobs.length || !state.running)) resolve();
        };
        w.onerror = (err) => {
          console.error(err);
          cards[job.id].querySelector('.pending').textContent = 'FAILED';
          progress[job.id] = 1;
          active--;
          done++;
          feed(w);
          if (active === 0 && (next >= jobs.length || !state.running)) resolve();
        };
        w.postMessage(job);
      };
      for (let i = 0; i < n; i++) {
        const w = new Worker('js/worker.js');
        workers.push(w);
        feed(w);
      }
      state.resolveRun = resolve;
    });

    stopWorkers();
    state.running = false;
    setBusy(false);
    if (done < jobs.length) $('status').textContent = `Stopped — ${state.results.length} of ${jobs.length} finished`;
  }

  function cancel() {
    if (!state.running) return;
    state.running = false;
    stopWorkers();
    if (state.resolveRun) state.resolveRun();
  }

  function clearResults() {
    for (const r of state.results) { URL.revokeObjectURL(r.url); URL.revokeObjectURL(r.turl); }
    state.results = [];
  }

  function setBusy(b) {
    const btn = $('generate');
    btn.disabled = b;
    btn.classList.toggle('busy', b);
    btn.querySelector('.label').textContent = b ? 'GENERATING BG' : 'BG GENERATE';
    $('progressWrap').hidden = false;
    $('actions').hidden = false;
    $('cancel').hidden = !b;
    for (const id of ['zip', 'csvAdobe', 'csvShutter']) $(id).disabled = b || !state.results.length;
  }

  // ---------- downloads ----------
  function save(blob, name) {
    const a = document.createElement('a');
    a.href = URL.createObjectURL(blob);
    a.download = name;
    document.body.appendChild(a);
    a.click();
    a.remove();
    setTimeout(() => URL.revokeObjectURL(a.href), 4000);
  }

  function sorted() { return state.results.slice().sort((a, b) => a.id - b.id); }

  async function downloadZip() {
    const btn = $('zip');
    btn.disabled = true;
    const old = btn.textContent;
    btn.textContent = 'Packing…';
    try {
      const zip = await window.TFZip.makeZip(sorted().map((r) => ({ name: r.file, blob: r.blob })));
      save(zip, `texture-forge-${seedInput.value}.zip`);
    } finally {
      btn.textContent = old;
      btn.disabled = false;
    }
  }

  // Titles and keywords are a starting point — stock sites reward accurate, specific metadata.
  function metadata(r) {
    const style = window.TFTextures.BY_ID[r.styleId];
    const title = `${r.palName} ${style.name.toLowerCase()} texture background, abstract high resolution surface`;
    const words = [...r.palName.toLowerCase().split(/\s+/), ...style.keywords, 'high resolution', 'copy space', 'wallpaper'];
    const keywords = [...new Set(words)].slice(0, 49);
    return { title: title.charAt(0).toUpperCase() + title.slice(1), keywords };
  }

  const csvCell = (s) => `"${String(s).replace(/"/g, '""')}"`;

  function downloadCsv(kind) {
    const rows = kind === 'adobe'
      ? [['Filename', 'Title', 'Keywords', 'Category', 'Releases']]
      : [['Filename', 'Description', 'Keywords', 'Categories', 'Editorial', 'Mature content', 'illustration']];
    for (const r of sorted()) {
      const m = metadata(r);
      rows.push(kind === 'adobe'
        ? [r.file, m.title, m.keywords.join(', '), '8', '']
        : [r.file, m.title, m.keywords.join(','), 'Backgrounds/Textures,Abstract', 'no', 'no', 'yes']);
    }
    const csv = rows.map((row) => row.map(csvCell).join(',')).join('\r\n');
    save(new Blob([csv], { type: 'text/csv' }), `${kind === 'adobe' ? 'adobe-stock' : 'shutterstock'}-${seedInput.value}.csv`);
  }

  $('generate').addEventListener('click', generate);
  $('cancel').addEventListener('click', cancel);
  $('zip').addEventListener('click', downloadZip);
  $('csvAdobe').addEventListener('click', () => downloadCsv('adobe'));
  $('csvShutter').addEventListener('click', () => downloadCsv('shutter'));
})();
