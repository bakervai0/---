# Texture Forge

A procedural background and texture generator for microstock (Adobe Stock, Shutterstock, Freepik…).
Every image is computed from math — Perlin noise, fBm, ridged noise, Worley cells, domain warping and
color ramps. **No AI model, no API key, no server**: everything runs in the visitor's browser.

## Styles

Grunge Wall · Concrete · Leather · Wood · Marble · Organic Watercolor · Oxidized Metal (rust / copper patina) ·
Brushed Metal · Paper · Granite · Terrazzo · Linen Fabric · Fluid Gradient — each with 4–9 color palettes and
random variation per seed.

## Features

- 7 ratios at stock-ready sizes (4500×3000, 3840×2160, 4000×3000, 3000×3000, 2400×3000, 3000×4500, 2160×3840)
- Full / 50% / preview output sizes
- Batches of 5–30 images, rendered in parallel Web Workers
- JPEG quality control, per-image download, download all as ZIP
- Adobe Stock and Shutterstock metadata CSV (title + keywords) as a starting point
- Seeded: the same seed and settings always give the same images

## Run locally

Workers do not load from `file://`, so serve the folder:

```bash
cd texture-forge
npx serve .
```

## Deploy on Vercel

1. Go to <https://vercel.com/new> and import this GitHub repository.
2. Set **Root Directory** to `texture-forge`.
3. Framework preset: **Other**. Leave build command and output directory empty.
4. Deploy. Every push to the selected branch redeploys automatically.

## Add a new style

Add an entry to `STYLES` in `js/textures.js`: an `id`, `name`, `keywords`, `palettes` and a `setup()` that returns
`fn(u, v, o)` — a function that writes the color (`o.r`, `o.g`, `o.b`) and an optional height (`o.h`, used for relief
lighting when `relief > 0`) for one pixel. It appears in the UI automatically.
