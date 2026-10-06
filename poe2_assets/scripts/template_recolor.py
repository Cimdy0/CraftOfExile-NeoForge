# -*- coding: utf-8 -*-
"""模板重着色 v5：以原版 MC 物品图标为形状模板，用 PoE2 装备图标的色调特征覆盖修饰。
v5 相对 v4 的改进：
 1) 调色板饱和度增强（金更金、铜更铜），让色调特征在 16x16 上可见；
 2) 叠加 PoE2 原图内部的明暗纹理（16x16 亮度场调制），每件装备有自己的纹路细节；
 3) 保留模板深色描边，图标轮廓清晰。
朝向/剪影/结构 100% 来自原版模板（正面、规范朝向）。
槽位映射：Helmets->iron_helmet, Body_Armours->iron_chestplate, Boots->iron_boots, Gloves->iron_leggings。
"""
from PIL import Image
import numpy as np
import colorsys
import os

TEMPLATES = {
    'Helmets': 'iron_helmet.png',
    'Body_Armours': 'iron_chestplate.png',
    'Boots': 'iron_boots.png',
    'Gloves': 'iron_leggings.png',
}


def kmeans_centers(colors, ncolors=6, iters=25, seed=0):
    N = len(colors)
    if N == 0:
        return None
    rng = np.random.default_rng(seed)
    lum = colors @ np.array([0.299, 0.587, 0.114])
    order = np.argsort(lum)
    idx = np.linspace(0, N - 1, ncolors).astype(int)
    centers = colors[order[idx]].copy()
    for _ in range(iters):
        d = ((colors[:, None, :] - centers[None, :, :]) ** 2).sum(axis=2)
        assign = d.argmin(axis=1)
        newc = centers.copy()
        for k in range(ncolors):
            m = assign == k
            if m.sum() > 0:
                newc[k] = colors[m].mean(axis=0)
        if np.abs(newc - centers).max() < 0.5:
            centers = newc
            break
        centers = newc
    return centers


def _family(pal):
    """按物品整体饱和度/色相判定材质家族，返回 (hue, sat) 或 None=None=保留原色调。
    避免 PoE2 统一暖调打光把金属/皮革染成同一暖褐。"""
    l = pal @ np.array([0.299, 0.587, 0.114])
    w = l - l.min() + 8.0
    hs, ss, vs = [], [], []
    for c in pal:
        h, s, v = colorsys.rgb_to_hsv(c[0] / 255.0, c[1] / 255.0, c[2] / 255.0)
        hs.append(h if s > 0.08 else 0.0)
        ss.append(s)
        vs.append(v)
    ah = np.average(hs, weights=w)
    asat = np.average(ss, weights=w)
    aval = np.average(vs, weights=w)
    if asat >= 0.45:      # 彩色织物/染皮 -> 保留原色调
        return None, None
    if 0.02 < ah < 0.24:  # 暖色低饱和 -> 金/铜/皮
        if aval > 0.55:    # 高亮度暖金属 -> 金
            return 0.105, 0.60
        return 0.075, 0.52  # 铜/皮革棕
    return 0.585, 0.26    # 中性/冷 -> 钢蓝灰（对齐 MC 铁甲）


def extract_palette(src_path, ncolors=6):
    """从 PoE2 图标前景提取按亮度排序（暗->亮）的调色板。
    材质家族着色 + 保留物品自身明暗结构 + 最亮色调拉到 ~230 保证 MC 可读性。"""
    im = Image.open(src_path).convert('RGBA')
    arr = np.asarray(im).astype(np.float32)
    m = arr[:, :, 3] >= 96
    px = arr[m][:, :3]
    if len(px) == 0:
        return None
    centers = kmeans_centers(px, ncolors=ncolors)
    order = np.argsort(centers @ np.array([0.299, 0.587, 0.114]))
    pal = centers[order]  # dark -> light
    fam_h, fam_s = _family(pal)
    out = []
    for i, c in enumerate(pal):
        h, s, v = colorsys.rgb_to_hsv(c[0] / 255.0, c[1] / 255.0, c[2] / 255.0)
        if fam_h is not None:
            h = fam_h
            s = fam_s
        else:
            s = min(s * 1.6, 1.0)
        # 高光降饱和：顶部 1-2 个色带收敛为柔和色调白，避免冷白/青白高光
        if i >= ncolors - 2:
            s = min(s, 0.32 if i == ncolors - 1 else 0.45)
        out.append((h, s, v))
    # 明度全范围拉伸到 [~22, ~228]（对齐原版图标的明暗跨度，保证 MC 可读性）
    vs = np.array([o[2] for o in out])
    vmin, vmax = vs.min(), vs.max()
    if vmax - vmin > 1e-3:
        vs = 22.0 / 255.0 + (vs - vmin) / (vmax - vmin) * 206.0 / 255.0
    pal = np.array([colorsys.hsv_to_rgb(h, s, vv) for (h, s, _), vv in zip(out, vs)]) * 255.0
    return np.clip(pal, 0, 255)


def detail_map(src_path, size=16):
    """PoE2 原图内部亮度场（alpha 加权下采样到 16x16，归一化 [0,1]），
    作为模板内部的纹路/明暗细节叠加源。"""
    im = Image.open(src_path).convert('RGBA')
    a = np.asarray(im).astype(np.float32)
    al = a[:, :, 3]
    lum = a[:, :, :3] @ np.array([0.299, 0.587, 0.114])
    m = al >= 96
    if not m.any():
        return None
    lo, hi = lum[m].min(), lum[m].max()
    nrm = np.where(m, (lum - lo) / max(hi - lo, 1e-3), 0.5)
    H, W = nrm.shape
    grid = np.full((size, size), 0.5, np.float32)
    for sy in range(size):
        y0 = sy * H // size
        y1 = max((sy + 1) * H // size, y0 + 1)
        for sx in range(size):
            x0 = sx * W // size
            x1 = max((sx + 1) * W // size, x0 + 1)
            cell = nrm[y0:y1, x0:x1]
            wcell = (al[y0:y1, x0:x1] >= 96).astype(np.float32)
            s = wcell.sum()
            if s > 0:
                grid[sy, sx] = (cell * wcell).sum() / s
    return grid


def recolor_template(template_path, src_path, ncolors=6, detail_strength=0.4):
    """模板亮度排名 -> 调色板同排名颜色（保持模板明暗结构），
    再乘零均值内部亮度场做纹路调制（不整体压暗）；最暗像素保持调色板暗色。"""
    t = np.asarray(Image.open(template_path).convert('RGBA')).astype(np.float32)
    vis = t[:, :, 3] > 0
    lum = t[:, :, 0] * 0.299 + t[:, :, 1] * 0.587 + t[:, :, 2] * 0.114
    pal = extract_palette(src_path, ncolors=ncolors)
    det = detail_map(src_path)
    if pal is None or det is None:
        return None
    lum_vis = lum[vis]
    # 分位数映射：模板亮度排名（0..1）-> 调色板索引
    order = np.argsort(lum_vis)
    ranks = np.empty_like(lum_vis)
    ranks[order] = np.arange(len(lum_vis)) / max(len(lum_vis) - 1, 1)
    idx_vis = np.clip((ranks * (ncolors - 1)).astype(int), 0, ncolors - 1)
    cols = pal[idx_vis]  # 与 lum_vis 顺序对应
    # 零均值纹路：scale = 1 + ds*2*(det-0.5)，明暗对比但平均不压暗
    det_vis = det[vis]
    scale = 1.0 + detail_strength * 2.0 * (det_vis - 0.5)
    rim = ranks < 0.06  # 最暗 6% 像素保持暗色，轮廓清晰
    cols[rim] = pal[0]
    cols[~rim] = np.clip(cols[~rim] * scale[~rim, None], 0, 255)
    out = t.copy()
    out[vis, :3] = cols
    out[vis, 3] = 255
    return Image.fromarray(out.astype(np.uint8), 'RGBA')


def main():
    base = r'E:\McModding\Modding\CraftOfExile-NeoForge\poe2_assets'
    tpl_dir = os.path.join(base, 'item_pixel')
    outdir = os.path.join(base, 'item_pixel', '_template_proto')
    os.makedirs(outdir, exist_ok=True)
    tests = [
        ('Helmets', ['gold_circlet', 'shabby_hood', 'soldier_helm', 'death_mask', 'wool_cap', 'feathered_tiara']),
        ('Boots', ['Bronze_Greaves', 'Golden_Greaves', 'Bound_Boots', 'Bladed_Shoes',
                   'Blacksteel_Sabatons', 'Bulwark_Greaves', 'Bastion_Sabatons', 'Cavalry_Boots']),
        ('Body_Armours', ['Abyssal_Cuirass', 'Barbarian_Plate', 'Arcane_Raiment', 'Assassin_Garb', 'Avian_Robe']),
        ('Gloves', ['Adorned_Wraps', 'Blacksteel_Gauntlets', 'Bandage_Wraps', 'Ancient_Gauntlets', 'Barbed_Bracers']),
    ]
    for cat, names in tests:
        raw_dir = os.path.join(base, 'item', cat)
        tpl = os.path.join(tpl_dir, TEMPLATES[cat])
        for n in names:
            src = os.path.join(raw_dir, n + '.webp')
            if not os.path.exists(src):
                print(cat, n, 'missing')
                continue
            out = recolor_template(tpl, src)
            if out is None:
                print(cat, n, 'empty palette')
                continue
            dst = os.path.join(outdir, cat + '_' + n + '.png')
            out.save(dst)
            pal = extract_palette(src)
            print(cat, n, 'palette:', np.round(pal, 0).tolist())


if __name__ == '__main__':
    main()
