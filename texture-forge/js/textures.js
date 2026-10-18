/* Texture Forge — procedural texture styles.
 * Every style is a pure function of (pixel position, seed): noise + geometry + color ramps.
 * No AI model and no network calls are involved. */
(function (root) {
  'use strict';

  const NoiseLib = root.TFNoise || (typeof require !== 'undefined' ? require('./noise.js') : null);
  const hash2 = NoiseLib.hash2;

  // ---------- helpers ----------
  const clamp01 = (x) => (x < 0 ? 0 : x > 1 ? 1 : x);
  const smoothstep = (a, b, x) => { const t = clamp01((x - a) / (b - a)); return t * t * (3 - 2 * t); };
  const fract = (x) => x - Math.floor(x);

  function hexToRgb(hex) {
    const n = parseInt(hex.slice(1), 16);
    return [(n >> 16) & 255, (n >> 8) & 255, n & 255];
  }

  // Color ramp lookup table (1024 steps, evenly spaced stops).
  const LUT_N = 1024;
  function buildLUT(colors) {
    const stops = colors.map(hexToRgb);
    const lut = new Float32Array(LUT_N * 3);
    for (let i = 0; i < LUT_N; i++) {
      const t = (i / (LUT_N - 1)) * (stops.length - 1);
      const k = Math.min(stops.length - 2, Math.floor(t));
      const f = t - k;
      const a = stops[k], b = stops[k + 1];
      lut[i * 3] = a[0] + (b[0] - a[0]) * f;
      lut[i * 3 + 1] = a[1] + (b[1] - a[1]) * f;
      lut[i * 3 + 2] = a[2] + (b[2] - a[2]) * f;
    }
    return lut;
  }
  function lut(L, t, o) {
    const i = ((t < 0 ? 0 : t > 1 ? 1 : t) * (LUT_N - 1)) | 0;
    o.r = L[i * 3]; o.g = L[i * 3 + 1]; o.b = L[i * 3 + 2];
  }
  function mix(o, r, g, b, a) {
    o.r += (r - o.r) * a; o.g += (g - o.g) * a; o.b += (b - o.b) * a;
  }

  // ---------- styles ----------
  // setup(N, pal, S, U, V) -> { fn(u, v, o), relief }
  //   N: seeded noise, pal: palette, S: short side in px, U/V: image extent in short-side units.
  //   fn writes o.r/o.g/o.b (0..255) and o.h (height for relief lighting).
  // `fine(f)` caps a frequency so the finest detail never drops below ~3 px (no aliasing at preview sizes).

  const STYLES = [
    {
      id: 'grunge', name: 'Grunge Wall',
      keywords: ['grunge', 'wall', 'texture', 'background', 'distressed', 'dirty', 'abstract', 'old', 'aged', 'rough', 'weathered', 'vintage', 'surface', 'backdrop', 'design', 'pattern', 'scratched', 'stained', 'worn', 'retro'],
      palettes: [
        { name: 'Forest Moss', colors: ['#0f1a0c', '#2b4220', '#56782f', '#9cba5a', '#d6e39a'] },
        { name: 'Arctic Concrete', colors: ['#141a1f', '#4a5560', '#9aa7b2', '#dfe7ec', '#ffffff'] },
        { name: 'Rust Brown', colors: ['#1a0d07', '#4a2312', '#8a4a24', '#c58a52', '#ead1a6'] },
        { name: 'Midnight Blue', colors: ['#05070f', '#101d3a', '#24427a', '#5a7fbf', '#b6c9ea'] },
        { name: 'Charcoal Black', colors: ['#050505', '#1d1d1d', '#3d3d3d', '#6e6e6e', '#bdbdbd'] },
        { name: 'Blood Red', colors: ['#120203', '#3d0709', '#7a1015', '#b8343a', '#e8a3a0'] },
        { name: 'Sepia Vintage', colors: ['#2a1d10', '#5b4126', '#9c7a50', '#d4bc8c', '#f4e8c8'] },
        { name: 'Royal Purple', colors: ['#0c0614', '#2a1347', '#53308a', '#9474c9', '#d9c9f2'] },
        { name: 'Deep Teal', colors: ['#03100f', '#0d3532', '#1f6b64', '#5fb3a8', '#c4ece4'] },
      ],
      setup(N, pal, S, U, V, fine) {
        const L = buildLUT(pal.colors), r = N.rnd;
        const fr = 2 + r() * 2.5, streak = r() < 0.65 ? 0.25 + r() * 0.35 : 0;
        const ox = r() * 100, oy = r() * 100, scratch = 0.15 + r() * 0.25;
        const f1 = fine(180), f2 = fine(450);
        return {
          relief: 0.35,
          fn(u, v, o) {
            const w = N.fbm(u * 1.5 + ox, v * 1.5 + oy, 3);
            let t = 0.5 + 0.9 * N.fbm(u * fr + w * 1.2 + oy, v * fr + w * 1.2 + ox, 6);
            if (streak) t += N.fbm(u * 30 + ox, v * 1.2 + oy, 4) * streak;
            t -= smoothstep(0.25, 0.45, N.fbm(u * 14 + oy, v * 14 + ox, 4)) * 0.25;
            const sl = 1 - Math.abs(N.perlin(u * 6 + w * 0.3 + ox, v * 6 + oy));
            const line = Math.pow(sl, 60) * smoothstep(0.05, 0.35, w);
            t += line * scratch;
            t += N.perlin(u * f1, v * f1) * 0.06 + N.perlin(u * f2 + 7, v * f2) * 0.04;
            o.h = t;
            lut(L, t, o);
          },
        };
      },
    },

    {
      id: 'concrete', name: 'Concrete',
      keywords: ['concrete', 'cement', 'wall', 'texture', 'background', 'grey', 'gray', 'stone', 'rough', 'surface', 'industrial', 'urban', 'floor', 'material', 'construction', 'architecture', 'backdrop', 'plaster', 'raw', 'loft'],
      palettes: [
        { name: 'Light Grey', colors: ['#5f5f5c', '#8f8e8a', '#b4b3ae', '#d4d3ce', '#e6e5e0'] },
        { name: 'Warm Beige', colors: ['#6b5f50', '#958877', '#b8ab98', '#d3c7b4', '#e6dccb'] },
        { name: 'Anthracite', colors: ['#141517', '#26292c', '#3c4044', '#575c61', '#757a80'] },
        { name: 'Blue Grey', colors: ['#3e4750', '#606b76', '#85909b', '#a9b3bc', '#c9d0d6'] },
        { name: 'Polished White', colors: ['#9d9c98', '#c4c3bf', '#dddcd8', '#ecebe7', '#f6f5f2'] },
      ],
      setup(N, pal, S, U, V, fine) {
        const L = buildLUT(pal.colors), r = N.rnd;
        const ox = r() * 100, oy = r() * 100, stain = 0.4 + r() * 0.8;
        const poreF = fine(40 + r() * 50), poreDensity = 0.05 + r() * 0.15;
        const f1 = fine(120), f2 = fine(350);
        return {
          relief: 0.6,
          fn(u, v, o) {
            let t = 0.5 + N.fbm(u * 2 + ox, v * 2 + oy, 5) * 0.28 + N.fbm(u * 18 + oy, v * 18, 3) * 0.1;
            t += N.perlin(u * f1, v * f1) * 0.05 + N.perlin(u * f2, v * f2 + 3) * 0.035;
            t -= smoothstep(0.1, 0.5, N.fbm(u * 1.2 + oy, v * 1.2 + ox, 4)) * 0.18 * stain;
            const c = N.worley(u * poreF, v * poreF, 1);
            let pit = 0;
            if (c.id < poreDensity) {
              const rad = 0.08 + 0.22 * (c.id / poreDensity);
              pit = 1 - smoothstep(rad * 0.5, rad, c.f1);
            }
            o.h = t - pit * 0.6;
            lut(L, t - pit * 0.35, o);
          },
        };
      },
    },

    {
      id: 'leather', name: 'Leather',
      keywords: ['leather', 'texture', 'background', 'skin', 'pebbled', 'grain', 'luxury', 'natural', 'material', 'surface', 'closeup', 'macro', 'fashion', 'upholstery', 'genuine', 'hide', 'elegant', 'detail', 'organic', 'pattern'],
      palettes: [
        { name: 'Tan', colors: ['#2a160a', '#5e3619', '#8f5a2e', '#b9824a', '#d5a46c'] },
        { name: 'Dark Brown', colors: ['#0d0704', '#24140a', '#3f2514', '#5c3920', '#7a5030'] },
        { name: 'Black', colors: ['#000000', '#0b0b0b', '#1a1a1a', '#2b2b2b', '#424242'] },
        { name: 'Oxblood', colors: ['#120304', '#33090c', '#5a1318', '#7d2128', '#a0373d'] },
        { name: 'Navy', colors: ['#03060d', '#0b1426', '#172747', '#253d68', '#3a5688'] },
        { name: 'Olive', colors: ['#0f1106', '#262a12', '#3f4521', '#5b6333', '#7a844a'] },
        { name: 'Cognac', colors: ['#2a1004', '#5c260b', '#8f4316', '#b9662a', '#d98c47'] },
        { name: 'Stone Grey', colors: ['#1a1a1a', '#383836', '#575754', '#787874', '#9a9a95'] },
      ],
      setup(N, pal, S, U, V, fine) {
        const L = buildLUT(pal.colors), r = N.rnd;
        const F = fine(18 + r() * 18), ox = r() * 100, oy = r() * 100;
        const f1 = fine(60), f2 = fine(420);
        return {
          relief: 1.1,
          fn(u, v, o) {
            const wx = N.fbm(u * 3 + ox, v * 3 + oy, 2) * 0.03;
            const wy = N.fbm(u * 3 + oy, v * 3 + ox, 2) * 0.03;
            const c = N.worley((u + wx) * F, (v + wy) * F, 1);
            const dome = Math.sqrt(smoothstep(0, 0.32, c.f2 - c.f1));
            const micro = N.perlin(u * f2, v * f2) * 0.06;
            o.h = dome * 0.8 + micro + N.fbm(u * f1, v * f1, 2) * 0.1;
            const t = 0.25 + dome * 0.45 + N.fbm(u * 2 + oy, v * 2 + ox, 4) * 0.3 + (c.id - 0.5) * 0.08 + micro;
            lut(L, t, o);
          },
        };
      },
    },

    {
      id: 'wood', name: 'Wood',
      keywords: ['wood', 'wooden', 'texture', 'background', 'grain', 'natural', 'timber', 'plank', 'board', 'table', 'floor', 'rustic', 'brown', 'surface', 'hardwood', 'material', 'organic', 'tree', 'oak', 'nature'],
      palettes: [
        { name: 'Oak', colors: ['#4a2e14', '#7a5129', '#a6773f', '#c99a60', '#dcb781'] },
        { name: 'Walnut', colors: ['#1c0e06', '#3a2111', '#5a361d', '#7a4e2c', '#94653b'] },
        { name: 'Pine', colors: ['#8a5a28', '#b5803f', '#d4a462', '#e7c48c', '#f3dcae'] },
        { name: 'Mahogany', colors: ['#2a0905', '#541a0e', '#7e2d18', '#a2462a', '#bd6440'] },
        { name: 'Ebony', colors: ['#050302', '#120c08', '#21170f', '#33251a', '#493626'] },
        { name: 'Whitewashed', colors: ['#8f897e', '#b1ab9f', '#cdc8bd', '#e2ded5', '#f0ede6'] },
        { name: 'Weathered Grey', colors: ['#38352f', '#5c5852', '#827d76', '#a5a09a', '#c4c0ba'] },
        { name: 'Cherry', colors: ['#3a1408', '#682a14', '#924323', '#b56236', '#cf8452'] },
      ],
      setup(N, pal, S, U, V, fine) {
        const L = buildLUT(pal.colors), r = N.rnd;
        const planks = r() < 0.6 ? 3 + Math.floor(r() * 5) : 0;
        const ringF = 12 + r() * 14, seedOff = Math.floor(r() * 1e6);
        const knots = [];
        const nk = Math.floor(r() * 3);
        for (let i = 0; i < nk; i++) knots.push({ x: r() * U, y: r() * V, a: 1.5 + r() * 2.5, s: 0.03 + r() * 0.05 });
        const fib = fine(260), pore = fine(900);
        const gapW = 1.5 / S;
        return {
          relief: 0.45,
          fn(u, v, o) {
            let po = 0, gap = 0;
            if (planks) {
              const pv = (v / V) * planks;
              const pi = Math.floor(pv);
              po = hash2(pi, 7, seedOff) * 100;
              const d = Math.min(pv - pi, pi + 1 - pv) * (V / planks);
              gap = 1 - smoothstep(gapW, gapW * 3, d);
            }
            const warp = N.fbm(u * 0.4 + po, v * 2.5 + po, 4);
            let rf = v * ringF + warp * 4 + po;
            for (let k = 0; k < knots.length; k++) {
              const kn = knots[k];
              const dx = u - kn.x, dy = (v - kn.y) * 2.5;
              rf += kn.a * Math.exp(-(dx * dx + dy * dy) / (kn.s * kn.s * 4));
            }
            const ring = fract(rf);
            const late = smoothstep(0.55, 0.92, ring) * (1 - smoothstep(0.92, 1, ring));
            const fibers = N.fbm(u * 3 + po, v * fib, 3);
            let t = 0.72 - late * 0.35 + fibers * 0.3 + N.perlin(u * 40 + po, v * pore) * 0.05 + N.fbm(u * 1.5 + po, v * 1.5, 3) * 0.12;
            t *= 1 - gap * 0.75;
            o.h = t * 0.3 - gap;
            lut(L, t, o);
          },
        };
      },
    },

    {
      id: 'marble', name: 'Marble',
      keywords: ['marble', 'texture', 'background', 'stone', 'luxury', 'elegant', 'natural', 'veins', 'surface', 'polished', 'granite', 'tile', 'floor', 'countertop', 'interior', 'pattern', 'abstract', 'rock', 'smooth', 'premium'],
      palettes: [
        { name: 'Carrara White', colors: ['#5c5c5c', '#9a9a9a', '#d8d8d6', '#f2f1ee', '#fbfaf8'] },
        { name: 'Calacatta Gold', colors: ['#7d6a4d', '#b7a68a', '#e9e4dc', '#f7f5f1', '#fdfcfa'] },
        { name: 'Nero Black', colors: ['#f2f2f2', '#9a9a9a', '#2a2a2a', '#161616', '#0b0b0b'] },
        { name: 'Black Gold', colors: ['#f0cf7a', '#a07c34', '#1f1a12', '#111010', '#0a0a0a'] },
        { name: 'Emerald Green', colors: ['#e8f5ec', '#7fb59a', '#1d5a40', '#0e3a28', '#082619'] },
        { name: 'Rose Pink', colors: ['#8a5a5a', '#c49a96', '#ead2cc', '#f6e8e4', '#fbf3f1'] },
        { name: 'Blue Sodalite', colors: ['#f2f5fa', '#9fb3d1', '#2a4a80', '#1a3160', '#0f1f42'] },
      ],
      setup(N, pal, S, U, V) {
        const L = buildLUT(pal.colors), r = N.rnd;
        const a = r() * Math.PI, dx = Math.cos(a), dy = Math.sin(a);
        const a2 = a + 0.8 + r() * 1.2, dx2 = Math.cos(a2), dy2 = Math.sin(a2);
        const freq = 1.5 + r() * 2.5, freq2 = 2 + r() * 3, warpAmp = 3 + r() * 3, sharp = 6 + r() * 10;
        const ox = r() * 100, oy = r() * 100;
        return {
          relief: 0,
          fn(u, v, o) {
            const turb = N.fbm(u * 2 + ox, v * 2 + oy, 6);
            const w2 = N.fbm(u * 5 + turb + oy, v * 5 + ox, 4);
            const s = Math.sin((u * dx + v * dy) * freq * Math.PI + turb * warpAmp);
            const as = 1 - Math.abs(s);
            const vein = Math.pow(as, sharp) + Math.pow(as, 3) * 0.25;
            const s2 = Math.sin((u * dx2 + v * dy2) * freq2 * Math.PI + w2 * 6);
            const vein2 = Math.pow(1 - Math.abs(s2), 45) * smoothstep(-0.1, 0.3, turb);
            const cloud = N.fbm(u * 3 + oy, v * 3 + ox, 5);
            const t = 0.8 + cloud * 0.3 - vein * 0.55 - vein2 * 0.4;
            o.h = 0;
            lut(L, t, o);
          },
        };
      },
    },

    {
      id: 'watercolor', name: 'Organic Watercolor',
      keywords: ['watercolor', 'watercolour', 'texture', 'background', 'paint', 'abstract', 'artistic', 'wash', 'stain', 'paper', 'hand painted', 'art', 'pastel', 'soft', 'organic', 'brush', 'splash', 'creative', 'design', 'aquarelle'],
      palettes: [
        { name: 'Ocean', layers: ['#1f4e79', '#2e86ab', '#7ec8e3', '#0b3c5d'] },
        { name: 'Sunset', layers: ['#e4572e', '#f3a712', '#a8325e', '#f6c28b'] },
        { name: 'Botanical', layers: ['#2d6a4f', '#74c69d', '#b7e4c7', '#40916c'] },
        { name: 'Lavender', layers: ['#7b5ea7', '#c8a2c8', '#f4c2c2', '#9d8bc7'] },
        { name: 'Autumn', layers: ['#9c4a1a', '#d9822b', '#e9c46a', '#6b3a1f'] },
        { name: 'Forest Moss', layers: ['#3b5d23', '#7d9d3c', '#c5d68a', '#5a7a2c'] },
        { name: 'Oxidised Copper', layers: ['#b06a3b', '#3fb8a8', '#2a5c58', '#d29a6a'] },
        { name: 'Blush', layers: ['#e8a1a7', '#f6cfc4', '#d4797f', '#f2b8a2'] },
      ],
      setup(N, pal, S, U, V, fine) {
        const r = N.rnd;
        const paper = [251, 248, 241];
        const n = 3 + Math.floor(r() * 2);
        const layers = [];
        for (let i = 0; i < n; i++) {
          const c = hexToRgb(pal.layers[i % pal.layers.length]);
          layers.push({ c: [c[0] / 255, c[1] / 255, c[2] / 255], f: 1 + r() * 2.2, ox: r() * 100, oy: r() * 100, th: -0.12 + r() * 0.22, op: 0.55 + r() * 0.35 });
        }
        const g1 = fine(90), g2 = fine(300);
        return {
          relief: 0.12,
          fn(u, v, o) {
            const w = N.fbm(u * 1.5 + 50, v * 1.5 + 50, 3) * 1.2;
            let cr = 1, cg = 1, cb = 1;
            const gran = N.perlin(u * g2, v * g2) * 0.06;
            for (let k = 0; k < layers.length; k++) {
              const ly = layers[k];
              const f = N.fbm(u * ly.f + ly.ox + w, v * ly.f + ly.oy + w, 5);
              const d = f - ly.th;
              const m = smoothstep(0, 0.08, d);
              const e = (d + 0.01) / 0.025;
              const edge = Math.exp(-e * e);
              const a = clamp01((m * (0.35 + 0.6 * clamp01(d)) + edge * 0.35 + gran * m) * ly.op);
              cr *= 1 - a + a * ly.c[0];
              cg *= 1 - a + a * ly.c[1];
              cb *= 1 - a + a * ly.c[2];
            }
            const grain = N.fbm(u * g1, v * g1, 3);
            const k2 = 1 + grain * 0.05;
            o.r = paper[0] * cr * k2; o.g = paper[1] * cg * k2; o.b = paper[2] * cb * k2;
            o.h = grain;
          },
        };
      },
    },

    {
      id: 'oxidized', name: 'Oxidized Metal',
      keywords: ['rust', 'rusty', 'metal', 'texture', 'background', 'oxidized', 'corroded', 'patina', 'iron', 'copper', 'grunge', 'old', 'industrial', 'weathered', 'corrosion', 'surface', 'steel', 'decay', 'aged', 'damaged'],
      palettes: [
        { name: 'Rusty Iron', colors: ['#2e3134', '#5b5f63', '#8c9094', '#aeb2b5'], accent: ['#2e1005', '#6b280c', '#a54b19', '#d07d3e', '#e3a46a'] },
        { name: 'Oxidised Copper', colors: ['#3a1a0c', '#7e3f1f', '#b06a3b', '#d6956a'], accent: ['#123d39', '#1f6b62', '#3fa597', '#7fd6c4', '#b5ecdf'] },
        { name: 'Weathered Bronze', colors: ['#2e200d', '#5e4520', '#8f6c35', '#b58e52'], accent: ['#1e3528', '#3a6048', '#5c8a6a', '#8fb89a', '#c0dcc4'] },
        { name: 'Corroded Steel', colors: ['#1e2328', '#3b444d', '#5d6873', '#86919b'], accent: ['#3a1e10', '#7a3d1a', '#b8682e', '#d99a5a', '#efc79a'] },
      ],
      setup(N, pal, S, U, V, fine) {
        const LM = buildLUT(pal.colors), LA = buildLUT(pal.accent), r = N.rnd;
        const cov = -0.15 + r() * 0.3, ox = r() * 100, oy = r() * 100;
        const pf = fine(80), lf = fine(300), gf = fine(40);
        const tmp = { r: 0, g: 0, b: 0 };
        return {
          relief: 0.55,
          fn(u, v, o) {
            const w = N.fbm(u * 2 + ox, v * 2 + oy, 3);
            const m = N.fbm(u * 3 + w + oy, v * 3 + w + ox, 6);
            lut(LM, 0.5 + 0.4 * N.fbm(u * 8 + ox, v * 8, 4) + N.perlin(u * 2, v * lf) * 0.1, o);
            const patch = smoothstep(cov - 0.1, cov + 0.12, m);
            const at = 0.5 + 0.5 * N.fbm(u * 12 + oy, v * 12 + ox, 4) + 0.25 * N.perlin(u * gf, v * gf);
            lut(LA, at, tmp);
            mix(o, tmp.r, tmp.g, tmp.b, patch);
            const c = N.worley(u * pf, v * pf, 1);
            const pit = c.id < 0.25 ? 1 - smoothstep(0.05, 0.2, c.f1) : 0;
            o.r *= 1 - pit * 0.5; o.g *= 1 - pit * 0.5; o.b *= 1 - pit * 0.5;
            o.h = patch * 0.5 + at * 0.25 * patch - pit * 0.3;
          },
        };
      },
    },

    {
      id: 'brushed', name: 'Brushed Metal',
      keywords: ['brushed', 'metal', 'texture', 'background', 'steel', 'aluminum', 'stainless', 'metallic', 'silver', 'industrial', 'surface', 'shiny', 'chrome', 'plate', 'sheet', 'modern', 'technology', 'iron', 'polished', 'material'],
      palettes: [
        { name: 'Steel', colors: ['#4d5156', '#7a7f85', '#a9aeb3', '#d3d7da', '#eef0f2'] },
        { name: 'Gold', colors: ['#5a3f10', '#94701f', '#c9a246', '#ead18b', '#fbedc0'] },
        { name: 'Copper', colors: ['#4a1d0c', '#83391a', '#b8653a', '#dc9a72', '#f2c9ad'] },
        { name: 'Titanium', colors: ['#1f2125', '#383b40', '#585c62', '#7d8188', '#a3a7ad'] },
        { name: 'Rose Gold', colors: ['#5e3434', '#966060', '#c28f88', '#e2bcb2', '#f6e0d8'] },
        { name: 'Black Anodized', colors: ['#050506', '#101113', '#1d1f22', '#2e3135', '#45494e'] },
      ],
      setup(N, pal, S, U, V, fine) {
        const L = buildLUT(pal.colors), r = N.rnd;
        const vertical = r() < 0.35, ox = r() * 100, ph = r() * 6.28;
        const f1 = fine(600), f2 = fine(1600);
        return {
          relief: 0,
          fn(u, v, o) {
            if (vertical) { const t = u; u = v; v = t; }
            const lines = N.fbm(u * 1.5 + ox, v * f1, 3) * 0.5 + N.perlin(u * 6, v * f2 + ox) * 0.35;
            const sheen = 0.5 + 0.5 * Math.sin(u * 1.3 * Math.PI + ph + N.fbm(u * 0.7, v * 0.7 + ox, 2) * 2);
            const t = 0.22 + sheen * 0.55 + lines * 0.2;
            o.h = 0;
            lut(L, t, o);
          },
        };
      },
    },

    {
      id: 'paper', name: 'Paper',
      keywords: ['paper', 'texture', 'background', 'crumpled', 'old', 'vintage', 'parchment', 'sheet', 'blank', 'grunge', 'aged', 'kraft', 'retro', 'surface', 'wrinkled', 'antique', 'page', 'cardboard', 'design', 'empty'],
      palettes: [
        { name: 'White', colors: ['#a9a69e', '#d2cfc7', '#ebe8e1', '#f7f5f0', '#fdfcf9'] },
        { name: 'Kraft', colors: ['#5c3e22', '#8a643e', '#ab8456', '#c49e6e', '#d6b487'] },
        { name: 'Vintage Parchment', colors: ['#5a3d1c', '#9a7442', '#cfae78', '#e6d0a3', '#f3e6c6'], vintage: true },
        { name: 'Black', colors: ['#000000', '#0d0d0d', '#1b1b1b', '#2a2a2a', '#3c3c3c'] },
        { name: 'Recycled Grey', colors: ['#6e6c66', '#97948d', '#b5b2ab', '#cbc8c1', '#dddad3'] },
      ],
      setup(N, pal, S, U, V, fine) {
        const L = buildLUT(pal.colors), r = N.rnd;
        const crumple = r() < 0.65, vintage = pal.vintage || r() < 0.25;
        const cf = 3 + r() * 4, ox = r() * 100, oy = r() * 100;
        const fb1 = fine(200), fb2 = fine(600);
        return {
          relief: crumple ? 0.9 : 0.25,
          fn(u, v, o) {
            let h = 0;
            if (crumple) {
              const w = N.fbm(u * 2 + ox, v * 2 + oy, 2);
              h = N.ridged(u * cf + w * 0.4, v * cf + w * 0.4, 5) * 0.8;
              const c = N.worley(u * cf * 1.7 + ox, v * cf * 1.7 + oy, 1);
              h -= (1 - smoothstep(0, 0.06, c.f2 - c.f1)) * 0.25;
            }
            const fibers = N.fbm(u * fb1, v * fb1, 2) * 0.03 + N.perlin(u * 40 + oy, v * fb2) * 0.015;
            let t = 0.78 + fibers + 0.08 * N.fbm(u * 3 + ox, v * 3, 4);
            if (vintage) {
              t -= smoothstep(0.15, 0.55, N.fbm(u * 2.5 + oy, v * 2.5 + ox, 5)) * 0.28;
              const e = Math.min(u, v, U - u, V - v);
              t -= (1 - smoothstep(0, 0.1 + N.fbm(u * 6, v * 6, 3) * 0.05, e)) * 0.55;
            }
            o.h = h + fibers;
            lut(L, t, o);
          },
        };
      },
    },

    {
      id: 'granite', name: 'Granite',
      keywords: ['granite', 'stone', 'texture', 'background', 'rock', 'speckled', 'countertop', 'natural', 'polished', 'surface', 'mineral', 'quartz', 'floor', 'kitchen', 'tile', 'pattern', 'luxury', 'interior', 'material', 'closeup'],
      palettes: [
        { name: 'Salt and Pepper', colors: ['#141414', '#4a4a4a', '#8f8f8f', '#cfcfcf', '#f0f0f0'] },
        { name: 'Imperial Red', colors: ['#200b09', '#5e241d', '#9c4b38', '#c99a88', '#eadbd2'] },
        { name: 'Black Galaxy', colors: ['#030303', '#0c0c0d', '#18181a', '#2a2622', '#d4b06c'] },
        { name: 'Kashmir White', colors: ['#6a5f58', '#a39890', '#cec6bd', '#e6e0d8', '#f6f2ec'] },
        { name: 'Baltic Brown', colors: ['#120c08', '#3b2a1e', '#6e5139', '#a07e5c', '#c6a985'] },
        { name: 'Blue Pearl', colors: ['#0b0f16', '#1c2633', '#33465c', '#5e7a96', '#a9bfd6'] },
      ],
      setup(N, pal, S, U, V, fine) {
        const L = buildLUT(pal.colors), r = N.rnd;
        const F1 = fine(35 + r() * 30), F2 = fine(F1 * 2.6), F3 = fine(F1 * 5.5);
        const ox = r() * 100, oy = r() * 100;
        return {
          relief: 0,
          fn(u, v, o) {
            const wx = N.fbm(u * F1 * 0.25 + ox, v * F1 * 0.25, 2) * 0.6;
            const wy = N.fbm(u * F1 * 0.25 + oy, v * F1 * 0.25 + 9, 2) * 0.6;
            const t1 = N.worley(u * F1 + wx, v * F1 + wy, 1).id;
            const t2 = N.worley(u * F2 + wy, v * F2 + wx, 1).id;
            const c3 = N.worley(u * F3, v * F3, 1);
            let t = 0.55 * t1 + 0.35 * t2 + 0.1 * (0.5 + N.fbm(u * 4 + ox, v * 4 + oy, 3));
            t = t * t * (3 - 2 * t);
            if (c3.id > 0.94) t += (1 - smoothstep(0.1, 0.35, c3.f1)) * 0.5;
            o.h = 0;
            lut(L, t, o);
          },
        };
      },
    },

    {
      id: 'terrazzo', name: 'Terrazzo',
      keywords: ['terrazzo', 'texture', 'background', 'pattern', 'stone', 'chips', 'floor', 'tile', 'modern', 'interior', 'mosaic', 'speckled', 'seamless', 'design', 'abstract', 'marble', 'decor', 'trendy', 'surface', 'colorful'],
      palettes: [
        { name: 'Classic', colors: ['#e3dfd6', '#efece5', '#f6f4ef'], chips: ['#c98f6f', '#7a8b7f', '#2f2f2f', '#d9b56d', '#b7b2a8', '#a14a3a'] },
        { name: 'Blush Pink', colors: ['#ecd5cc', '#f4e3dc', '#faefea'], chips: ['#c97b6e', '#f2b5a0', '#5b4a44', '#e9d3a4', '#9c5a50', '#ffffff'] },
        { name: 'Forest Green', colors: ['#1d3a2f', '#24473a', '#2c5546'], chips: ['#e8e2d4', '#b6855c', '#7fae94', '#0f1f19', '#d5b36a', '#5b7d6b'] },
        { name: 'Navy', colors: ['#16233a', '#1c2c48', '#223656'], chips: ['#f0e9dc', '#d68a5c', '#8fb3d9', '#0a1220', '#e2c072', '#6d7f99'] },
        { name: 'Monochrome', colors: ['#d8d8d6', '#e6e6e4', '#f1f1ef'], chips: ['#111111', '#555555', '#8a8a8a', '#bdbdbd', '#ffffff', '#333333'] },
      ],
      setup(N, pal, S, U, V, fine) {
        const L = buildLUT(pal.colors), r = N.rnd;
        const chips = pal.chips.map(hexToRgb);
        const Fb = 8 + r() * 10, Fs = Fb * (2.4 + r());
        const densB = 0.4 + r() * 0.35, densS = 0.5 + r() * 0.4;
        const ox = r() * 100, oy = r() * 100, gf = fine(220);
        const aaB = 1.5 * Fb / S, aaS = 1.5 * Fs / S;
        function chip(c, dens, aa, o) {
          if (c.id >= dens) return;
          const thick = 0.12 + 0.3 * (c.id / dens);
          const a = smoothstep(thick, thick + aa, c.f2 - c.f1);
          if (a <= 0) return;
          const col = chips[Math.floor(hash2(Math.floor(c.id * 1e6), 3, 0) * chips.length)];
          const shade = 0.9 + 0.2 * c.id2;
          mix(o, col[0] * shade, col[1] * shade, col[2] * shade, a);
        }
        return {
          relief: 0,
          fn(u, v, o) {
            const wx = N.fbm(u * 2 + ox, v * 2 + oy, 3) * 0.04, wy = N.fbm(u * 2 + oy, v * 2 + ox, 3) * 0.04;
            lut(L, 0.5 + N.fbm(u * 6 + ox, v * 6, 3) * 0.6 + N.perlin(u * gf, v * gf) * 0.25, o);
            chip(N.worley((u + wx) * Fs + 50, (v + wy) * Fs + 50, 1), densS, aaS, o);
            chip(N.worley((u + wy) * Fb, (v + wx) * Fb, 1), densB, aaB, o);
            o.h = 0;
          },
        };
      },
    },

    {
      id: 'fabric', name: 'Linen Fabric',
      keywords: ['fabric', 'linen', 'canvas', 'texture', 'background', 'textile', 'cloth', 'weave', 'woven', 'burlap', 'natural', 'material', 'cotton', 'sackcloth', 'closeup', 'pattern', 'jute', 'surface', 'denim', 'fiber'],
      palettes: [
        { name: 'Natural Linen', colors: ['#5e5140', '#8f7f66', '#b3a389', '#cfc1a8', '#e3d8c4'] },
        { name: 'White Canvas', colors: ['#8d8a83', '#b8b5ad', '#d6d3cb', '#eae8e2', '#f7f6f2'] },
        { name: 'Denim Blue', colors: ['#070e1c', '#14264a', '#26457a', '#46699f', '#8aa6cc'] },
        { name: 'Burlap', colors: ['#2e200f', '#5c4424', '#8a6a3c', '#b08d5a', '#cdb07c'], gap: true },
        { name: 'Charcoal Black', colors: ['#050505', '#141414', '#242424', '#363636', '#4a4a4a'] },
        { name: 'Sage Green', colors: ['#3a4434', '#5e6b56', '#84927a', '#a6b39c', '#c6d0bd'] },
      ],
      setup(N, pal, S, U, V, fine) {
        const L = buildLUT(pal.colors), r = N.rnd;
        const gap = !!pal.gap;
        const T = Math.min(gap ? 45 + r() * 30 : 110 + r() * 110, S / 7);
        const ox = r() * 100, oy = r() * 100, ff = fine(T * 6);
        return {
          relief: 0.7,
          fn(u, v, o) {
            const wu = u * T + N.fbm(u * 2 + ox, v * 2, 3) * 1.2;
            const wv = v * T + N.fbm(u * 2, v * 2 + oy, 3) * 1.2;
            const ix = Math.floor(wu), iy = Math.floor(wv);
            const fx = wu - ix, fy = wv - iy;
            const over = (ix + iy) & 1;
            let prof, dip, yarn;
            if (over) {
              prof = Math.sin(fx * Math.PI); dip = Math.pow(Math.sin(fy * Math.PI), 0.35);
              yarn = hash2(ix, 1, 11) * 0.6 + N.perlin(ix * 0.37, v * 6) * 0.4;
            } else {
              prof = Math.sin(fy * Math.PI); dip = Math.pow(Math.sin(fx * Math.PI), 0.35);
              yarn = hash2(1, iy, 17) * 0.6 + N.perlin(u * 6, iy * 0.37) * 0.4;
            }
            if (gap) prof = smoothstep(0.15, 0.55, prof);
            const p = Math.pow(prof, 0.6) * (0.55 + 0.45 * dip);
            const fiber = N.perlin(u * ff, v * ff) * 0.06;
            o.h = p;
            lut(L, 0.15 + p * 0.6 + (yarn - 0.3) * 0.18 + fiber + N.fbm(u * 2 + oy, v * 2 + ox, 3) * 0.1, o);
          },
        };
      },
    },

    {
      id: 'fluid', name: 'Fluid Gradient',
      keywords: ['abstract', 'gradient', 'background', 'fluid', 'colorful', 'vibrant', 'wallpaper', 'liquid', 'flow', 'modern', 'blur', 'smooth', 'mesh', 'holographic', 'wave', 'design', 'color', 'trendy', 'bright', 'grain'],
      palettes: [
        { name: 'Aurora', colors: ['#0b1d51', '#2c5fbf', '#6fd3c9', '#f6e27f', '#f45b69'] },
        { name: 'Candy', colors: ['#ff9a9e', '#fad0c4', '#a18cd1', '#fbc2eb', '#8fd3f4'] },
        { name: 'Sunset Glow', colors: ['#2b1055', '#7a1f6f', '#d53f6b', '#f78f4b', '#ffd36e'] },
        { name: 'Deep Ocean', colors: ['#020b1a', '#06306b', '#0f6fa8', '#2cb8c8', '#a8f0e6'] },
        { name: 'Neon Night', colors: ['#0d0221', '#3a0ca3', '#7209b7', '#f72585', '#4cc9f0'] },
        { name: 'Peach Cream', colors: ['#f8b195', '#f67280', '#c06c84', '#f9d5bb', '#fff1e6'] },
        { name: 'Emerald Lime', colors: ['#03231c', '#0b5d47', '#1fa774', '#9be15d', '#f0ff9c'] },
      ],
      setup(N, pal, S, U, V) {
        const L = buildLUT(pal.colors), r = N.rnd;
        const f = 0.45 + r() * 0.4, k1 = 1.5 + r() * 1.5, k2 = 1.2 + r() * 1.3;
        const ox = r() * 100, oy = r() * 100;
        return {
          relief: 0,
          fn(u, v, o) {
            const q = N.fbm(u * f + ox, v * f + oy, 3);
            const q2 = N.fbm(u * f + oy, v * f + ox + 5.2, 3);
            const rr = N.fbm(u * f + k1 * q + 1.7, v * f + k1 * q2 + 9.2, 3);
            const t = 0.5 + 0.9 * N.fbm(u * f + k2 * rr + ox, v * f + k2 * rr + oy, 2);
            o.h = 0;
            lut(L, t, o);
          },
        };
      },
    },
  ];

  const BY_ID = {};
  for (const s of STYLES) BY_ID[s.id] = s;

  // Directional light from the top-left, applied to a height field.
  function applyRelief(out, hgt, W, H, S, strength) {
    const k = strength * S * 0.0025;
    let lx = -0.55, ly = -0.6, lz = 0.58;
    const ln = Math.hypot(lx, ly, lz); lx /= ln; ly /= ln; lz /= ln;
    for (let y = 0; y < H; y++) {
      const ym = y > 0 ? y - 1 : y, yp = y < H - 1 ? y + 1 : y;
      for (let x = 0; x < W; x++) {
        const xm = x > 0 ? x - 1 : x, xp = x < W - 1 ? x + 1 : x;
        const gx = (hgt[y * W + xp] - hgt[y * W + xm]) * k;
        const gy = (hgt[yp * W + x] - hgt[ym * W + x]) * k;
        const inv = 1 / Math.sqrt(gx * gx + gy * gy + 1);
        let s = ((-gx * lx - gy * ly + lz) * inv) / lz;
        s = s < 0.35 ? 0.35 : s > 1.6 ? 1.6 : s;
        const i = (y * W + x) * 4;
        out[i] *= s; out[i + 1] *= s; out[i + 2] *= s;
      }
    }
  }

  /** Render one texture into `out` (Uint8ClampedArray, W*H*4). */
  function renderTexture(styleId, palIdx, W, H, seed, out, onProgress) {
    const style = BY_ID[styleId];
    const pal = style.palettes[palIdx % style.palettes.length];
    const N = NoiseLib.makeNoise(seed >>> 0);
    const S = Math.min(W, H);
    const fine = (f) => Math.min(f, S / 3);
    const px = style.setup(N, pal, S, W / S, H / S, fine);
    const hgt = px.relief ? new Float32Array(W * H) : null;
    const o = { r: 0, g: 0, b: 0, h: 0 };
    const inv = 1 / S;
    const step = Math.max(1, Math.floor(H / 20));
    for (let y = 0; y < H; y++) {
      const v = y * inv;
      let i = y * W * 4, j = y * W;
      for (let x = 0; x < W; x++, i += 4, j++) {
        px.fn(x * inv, v, o);
        out[i] = o.r; out[i + 1] = o.g; out[i + 2] = o.b; out[i + 3] = 255;
        if (hgt) hgt[j] = o.h;
      }
      if (onProgress && y % step === 0) onProgress(y / H);
    }
    if (hgt) applyRelief(out, hgt, W, H, S, px.relief);
    // Dither to avoid banding in smooth gradients (stock reviewers reject banding).
    for (let j = 0, i = 0; j < W * H; j++, i += 4) {
      const d = (hash2(j, seed, 99) - 0.5) * 2;
      out[i] += d; out[i + 1] += d; out[i + 2] += d;
    }
    return { style: style.name, palette: pal.name };
  }

  const api = { STYLES, BY_ID, renderTexture };
  if (typeof module !== 'undefined' && module.exports) module.exports = api;
  else root.TFTextures = api;
})(typeof self !== 'undefined' ? self : this);
