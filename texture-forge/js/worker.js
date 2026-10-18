/* Texture Forge — render worker. Generates one texture per message and encodes it to JPEG. */
importScripts('noise.js', 'textures.js');

const THUMB_W = 520;

self.onmessage = async (e) => {
  const { id, styleId, palIdx, W, H, seed, quality } = e.data;
  const out = new Uint8ClampedArray(W * H * 4);
  let last = 0;
  const meta = self.TFTextures.renderTexture(styleId, palIdx, W, H, seed, out, (p) => {
    if (p - last > 0.04) { last = p; self.postMessage({ id, type: 'progress', p }); }
  });

  if (typeof OffscreenCanvas !== 'undefined') {
    try {
      const canvas = new OffscreenCanvas(W, H);
      canvas.getContext('2d').putImageData(new ImageData(out, W, H), 0, 0);
      const blob = await canvas.convertToBlob({ type: 'image/jpeg', quality });
      const tw = Math.min(THUMB_W, W), th = Math.round((H * tw) / W);
      const thumbCanvas = new OffscreenCanvas(tw, th);
      const tctx = thumbCanvas.getContext('2d');
      tctx.imageSmoothingQuality = 'high';
      tctx.drawImage(canvas, 0, 0, tw, th);
      const thumb = await thumbCanvas.convertToBlob({ type: 'image/jpeg', quality: 0.85 });
      self.postMessage({ id, type: 'done', blob, thumb, meta });
      return;
    } catch (err) {
      // fall through: let the main thread encode
    }
  }
  self.postMessage({ id, type: 'pixels', buffer: out.buffer, W, H, meta }, [out.buffer]);
};
