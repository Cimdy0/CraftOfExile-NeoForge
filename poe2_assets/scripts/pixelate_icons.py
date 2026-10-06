# -*- coding: utf-8 -*-
"""保形像素化 v3（最终版）：将 poe2db 爬取的 212x212 webp 基底贴图转为
Minecraft 原版风格 16x16 平面物品图标。

算法要点：
1. 剪影提取（alpha>=96），裁边、居中、补成方形；
2. 前景加权区域缩小到 16x16（颜色只取前景均值，避免背景污染）；
3. 二元透明度（覆盖率>=0.35 为实体像素），硬边缘贴近原版；
4. 对比度拉伸（逐通道 2%-98% 分位）；
5. 加权 k-means 量化到 7 色（保留原装备色调特征）；
6. 深色描边（最暗量化色 x0.45，上限 90），保证暗背景下可辨识。

用法：
  python pixelate_icons.py <raw_dir> <out_dir>
  将 <raw_dir> 下全部 *.webp 转为 16x16 PNG 到 <out_dir>（同名 snake 保持原文件名）。
"""
from PIL import Image
import numpy as np
import os
import sys


def kmeans_quantize(colors, alpha, ncolors=7, iters=20, seed=0):
    N = len(colors)
    if N == 0:
        return colors
    rng = np.random.default_rng(seed)
    lum = colors @ np.array([0.299, 0.587, 0.114])
    order = np.argsort(lum)
    idx = np.linspace(0, N - 1, ncolors).astype(int)
    centers = colors[order[idx]].copy()
    w = np.clip(alpha / 255.0, 0.05, 1.0)
    for _ in range(iters):
        d = ((colors[:, None, :] - centers[None, :, :]) ** 2).sum(axis=2)
        assign = d.argmin(axis=1)
        newc = centers.copy()
        for k in range(ncolors):
            m = assign == k
            if m.sum() > 0:
                newc[k] = (colors[m] * w[m, None]).sum(axis=0) / w[m].sum()
        if np.abs(newc - centers).max() < 0.5:
            centers = newc
            break
        centers = newc
    d = ((colors[:, None, :] - centers[None, :, :]) ** 2).sum(axis=2)
    assign = d.argmin(axis=1)
    return centers[assign]


def convert(src, size=16, ncolors=7):
    im = Image.open(src).convert('RGBA')
    a = np.asarray(im).astype(np.float32)
    mask = a[:, :, 3] >= 96
    ys, xs = np.where(mask)
    if len(ys) == 0:
        return None
    y0, y1, x0, x1 = ys.min(), ys.max() + 1, xs.min(), xs.max() + 1
    crop = im.crop((int(x0), int(y0), int(x1), int(y1)))
    w, h = crop.size
    s = max(w, h)
    sq = Image.new('RGBA', (s, s), (0, 0, 0, 0))
    sq.paste(crop, ((s - w) // 2, (s - h) // 2))
    arr = np.asarray(sq).astype(np.float32)
    out = np.zeros((size, size, 4), np.float32)
    scale = s / size
    for cy in range(size):
        y0i = int(cy * scale); y1i = int(min((cy + 1) * scale, s))
        for cx in range(size):
            x0i = int(cx * scale); x1i = int(min((cx + 1) * scale, s))
            cell = arr[y0i:y1i, x0i:x1i]
            cm = cell[:, :, 3] >= 96
            n = cm.sum()
            if n == 0:
                out[cy, cx, 3] = 0
            else:
                cov = n / cm.size
                wgt = np.where(cm, cell[:, :, 3], 0)
                wsum = wgt.sum()
                for ch in range(3):
                    out[cy, cx, ch] = (cell[:, :, ch] * wgt).sum() / wsum
                out[cy, cx, 3] = 255.0 if cov >= 0.35 else 0.0
                if cov < 0.35:
                    out[cy, cx, :3] = 0
    small = out
    fg = small[:, :, 3] >= 255
    if not fg.any():
        return None
    px = small[fg][:, :3].copy()
    al = np.full(len(px), 255.0)
    for ch in range(3):
        lo, hi = np.percentile(px[:, ch], 2), np.percentile(px[:, ch], 98)
        if hi - lo > 4:
            px[:, ch] = (px[:, ch] - lo) * 255.0 / (hi - lo)
            px[:, ch] = np.clip(px[:, ch], 0, 255)
    qpx = kmeans_quantize(px, al, ncolors=ncolors)
    res = np.zeros((size, size, 4), np.uint8)
    res[fg] = np.concatenate([qpx, np.full((len(qpx), 1), 255, np.uint8)], axis=1)
    vis = res[:, :, 3] > 0
    dark = qpx[np.argsort(qpx @ np.array([0.299, 0.587, 0.114]))[:max(1, len(qpx) // 8)]].mean(axis=0)
    outline = np.clip(dark * 0.45, 0, 90).astype(np.uint8)
    for y in range(size):
        for x in range(size):
            if not vis[y, x]:
                nb = False
                for dy in (-1, 0, 1):
                    for dx in (-1, 0, 1):
                        ny, nx = y + dy, x + dx
                        if 0 <= ny < size and 0 <= nx < size and vis[ny, nx]:
                            nb = True
                            break
                    if nb:
                        break
                if nb:
                    res[y, x, :3] = outline
                    res[y, x, 3] = 255
    return Image.fromarray(res, 'RGBA')


def convert_dir(raw_dir, out_dir):
    os.makedirs(out_dir, exist_ok=True)
    files = [f for f in os.listdir(raw_dir) if f.lower().endswith('.webp')]
    ok, fail = 0, []
    for f in sorted(files):
        src = os.path.join(raw_dir, f)
        name = os.path.splitext(f)[0].lower() + '.png'
        dst = os.path.join(out_dir, name)
        try:
            img = convert(src)
            if img is None:
                fail.append(f)
                continue
            img.save(dst)
            ok += 1
        except Exception as e:
            fail.append(f + ' :: ' + str(e))
    print('ok=%d fail=%d' % (ok, len(fail)))
    for f in fail:
        print('  FAIL', f)
    return ok, fail


def main():
    if len(sys.argv) < 3:
        print('usage: pixelate_icons.py <raw_dir> <out_dir>')
        return
    raw_dir, out_dir = sys.argv[1], sys.argv[2]
    ok, fail = convert_dir(raw_dir, out_dir)
    print('done: %s -> %s (ok=%d, fail=%d)' % (raw_dir, out_dir, ok, len(fail)))


if __name__ == '__main__':
    main()
