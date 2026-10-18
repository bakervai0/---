/* Texture Forge — seeded procedural noise primitives.
 * Pure math, no AI: Perlin gradient noise, fBm, ridged noise, Worley (cellular) noise.
 * Works in the browser main thread, in Web Workers and in Node (for tests). */
(function (root) {
  'use strict';

  function mulberry32(a) {
    return function () {
      a |= 0; a = (a + 0x6D2B79F5) | 0;
      let t = Math.imul(a ^ (a >>> 15), 1 | a);
      t = (t + Math.imul(t ^ (t >>> 7), 61 | t)) ^ t;
      return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
    };
  }

  // Integer hash -> [0,1). Used for Worley feature points, per-cell colors and dithering.
  function hash2(x, y, s) {
    let h = Math.imul(x | 0, 0x27d4eb2d) ^ Math.imul(y | 0, 0x165667b1) ^ Math.imul(s | 0, 0x9e3779b1);
    h = Math.imul(h ^ (h >>> 15), 0x85ebca6b);
    h = Math.imul(h ^ (h >>> 13), 0xc2b2ae35);
    return ((h ^ (h >>> 16)) >>> 0) / 4294967296;
  }

  function makeNoise(seed) {
    const rnd = mulberry32(seed);
    const p = new Uint8Array(256);
    for (let i = 0; i < 256; i++) p[i] = i;
    for (let i = 255; i > 0; i--) {
      const j = Math.floor(rnd() * (i + 1));
      const t = p[i]; p[i] = p[j]; p[j] = t;
    }
    const perm = new Uint16Array(512);
    for (let i = 0; i < 512; i++) perm[i] = p[i & 255];
    const gx = new Float32Array(256), gy = new Float32Array(256);
    for (let i = 0; i < 256; i++) {
      const a = rnd() * Math.PI * 2;
      gx[i] = Math.cos(a); gy[i] = Math.sin(a);
    }

    // 2D Perlin gradient noise, roughly in [-1, 1].
    function perlin(x, y) {
      const xi = Math.floor(x), yi = Math.floor(y);
      const xf = x - xi, yf = y - yi;
      const X = xi & 255, Y = yi & 255;
      const a = perm[X] + Y, b = perm[X + 1] + Y;
      const aa = perm[a], ab = perm[a + 1], ba = perm[b], bb = perm[b + 1];
      const u = xf * xf * xf * (xf * (xf * 6 - 15) + 10);
      const v = yf * yf * yf * (yf * (yf * 6 - 15) + 10);
      const n00 = gx[aa] * xf + gy[aa] * yf;
      const n10 = gx[ba] * (xf - 1) + gy[ba] * yf;
      const n01 = gx[ab] * xf + gy[ab] * (yf - 1);
      const n11 = gx[bb] * (xf - 1) + gy[bb] * (yf - 1);
      const x1 = n00 + u * (n10 - n00);
      const x2 = n01 + u * (n11 - n01);
      return (x1 + v * (x2 - x1)) * 1.41421356;
    }

    // Fractal Brownian motion. Each octave is rotated to hide lattice artifacts.
    function fbm(x, y, oct, gain) {
      const g = gain === undefined ? 0.5 : gain;
      let sum = 0, amp = 1, norm = 0;
      for (let i = 0; i < oct; i++) {
        sum += amp * perlin(x, y);
        norm += amp;
        amp *= g;
        const nx = x * 1.6 - y * 1.2 + 17.3;
        y = x * 1.2 + y * 1.6 + 9.1;
        x = nx;
      }
      return sum / norm;
    }

    // Ridged multifractal: sharp creases / veins, in [0, 1].
    function ridged(x, y, oct) {
      let sum = 0, amp = 0.5, norm = 0, prev = 1;
      for (let i = 0; i < oct; i++) {
        let n = 1 - Math.abs(perlin(x, y));
        n *= n;
        sum += n * amp * prev;
        norm += amp;
        prev = n;
        amp *= 0.5;
        const nx = x * 1.6 - y * 1.2 + 31.7;
        y = x * 1.2 + y * 1.6 + 5.3;
        x = nx;
      }
      return Math.min(1, sum / norm);
    }

    // Worley / cellular noise. Results land in `cell`: f1, f2 (distances) and id (cell hash in [0,1)).
    const cell = { f1: 0, f2: 0, id: 0, id2: 0 };
    function worley(x, y, jitter) {
      const j = jitter === undefined ? 0.9 : jitter;
      const xi = Math.floor(x), yi = Math.floor(y);
      let f1 = 9, f2 = 9, id = 0, id2 = 0;
      for (let dy = -1; dy <= 1; dy++) {
        for (let dx = -1; dx <= 1; dx++) {
          const cx = xi + dx, cy = yi + dy;
          const px = cx + 0.5 + (hash2(cx, cy, seed) - 0.5) * j;
          const py = cy + 0.5 + (hash2(cx, cy, seed + 101) - 0.5) * j;
          const ddx = px - x, ddy = py - y;
          const d = ddx * ddx + ddy * ddy;
          if (d < f1) { f2 = f1; id2 = id; f1 = d; id = hash2(cx, cy, seed + 202); }
          else if (d < f2) { f2 = d; id2 = hash2(cx, cy, seed + 202); }
        }
      }
      cell.f1 = Math.sqrt(f1); cell.f2 = Math.sqrt(f2); cell.id = id; cell.id2 = id2;
      return cell;
    }

    return { perlin, fbm, ridged, worley, cell, rnd, seed };
  }

  const api = { mulberry32, hash2, makeNoise };
  if (typeof module !== 'undefined' && module.exports) module.exports = api;
  else root.TFNoise = api;
})(typeof self !== 'undefined' ? self : this);
