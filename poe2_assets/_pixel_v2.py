# -*- coding: utf-8 -*-
"""保形像素化 v2：剪影提取 + 前景加权缩小 + 色调保留量化(加权k-means) + 深色描边。
输出与原版 MC 风格一致（16x16、少色、描边），保留每个装备的原有色彩特征，保持基本形状。"""
from PIL import Image
import numpy as np
import os
import sys


def kmeans_quantize(colors, alpha, ncolors=8, iters=20, seed=0):
    """colors: (N,3) float; alpha: (N,) float 权重。返回每个像素分配到的中心颜色 (N,3)。"""
    N = len(colors)
    if N == 0:
        return colors
    rng = np.random.default_rng(seed)
    # 初始中心：按亮度分位采样，覆盖亮暗两端
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
                # 二元透明度：覆盖率足够即为实体像素（硬边缘，贴近原版 MC 风格）
                out[cy, cx, 3] = 255.0 if cov >= 0.35 else 0.0
                if cov < 0.35:
                    out[cy, cx, :3] = 0
    small = out
    # 前景像素集合（只取实体像素）
    fg = small[:, :, 3] >= 255
    if not fg.any():
        return Image.fromarray(np.zeros((size, size, 4), np.uint8), 'RGBA')
    px = small[fg][:, :3].copy()
    al = np.full(len(px), 255.0)
    # 对比度拉伸：逐通道 2%-98% 分位裁剪，避免整体偏灰
    for ch in range(3):
        lo, hi = np.percentile(px[:, ch], 2), np.percentile(px[:, ch], 98)
        if hi - lo > 4:
            px[:, ch] = (px[:, ch] - lo) * 255.0 / (hi - lo)
            px[:, ch] = np.clip(px[:, ch], 0, 255)
    qpx = kmeans_quantize(px, al, ncolors=ncolors)
    res = np.zeros((size, size, 4), np.uint8)
    res[fg] = np.concatenate([qpx, np.full((len(qpx), 1), 255, np.uint8)], axis=1)
    vis = res[:, :, 3] > 0
    # 深色描边：透明像素邻接可见像素 -> 用最暗的量化色作轮廓（贴近原版黑描边）
    dark = qpx[np.argsort(qpx @ np.array([0.299, 0.587, 0.114]))[:max(1, len(qpx)//8)]].mean(axis=0)
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


def main():
    raw = r'E:\McModding\Modding\CraftOfExile-NeoForge\poe2_assets\item\Helmets'
    outdir = r'E:\McModding\Modding\CraftOfExile-NeoForge\poe2_assets\item_pixel\_prototype'
    os.makedirs(outdir, exist_ok=True)
    names = sys.argv[1:] if len(sys.argv) > 1 else [
        'shabby_hood', 'soldier_helm', 'iron_crown', 'wool_cap',
        'brimmed_helm', 'death_mask', 'gold_circlet', 'feathered_tiara']
    for n in names:
        rp = os.path.join(raw, n + '.webp')
        if not os.path.exists(rp):
            print(n, 'raw missing')
            continue
        out = convert(rp)
        if out is None:
            print(n, 'empty')
            continue
        op = os.path.join(outdir, n + '.png')
        out.save(op)
        print(n, 'colors:', len(out.getcolors(16 * 16) or []))


if __name__ == '__main__':
    main()
