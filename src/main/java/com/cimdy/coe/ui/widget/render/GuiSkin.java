package com.cimdy.coe.ui.widget.render;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 贴图存在性探测器 —— 「渐进式贴图化」的基础设施。
 *
 * <p>组件只负责声明它<b>想要</b>哪张贴图，由本类回答这张贴图<b>是否已经做好</b>：
 * 做好了就走贴图分支，没做好就退回纯色/硬渲染。美术把 png 丢进对应目录即可生效，
 * 程序端一行都不用改。
 *
 * <h3>两种贴图模式</h3>
 * <ul>
 *   <li><b>sprite（推荐）</b>：文件位于 {@code assets/<ns>/textures/gui/sprites/<path>.png}，
 *       用 {@code guiGraphics.blitSprite} 绘制。旁边可放同名 {@code .png.mcmeta} 声明
 *       {@code nine_slice}/{@code stretch}/{@code tile}，由原版负责九宫格拉伸。
 *       注意：传给 {@code blitSprite} 的 id <b>不含</b>目录前缀与 {@code .png} 后缀。</li>
 *   <li><b>texture</b>：文件位于 {@code assets/<ns>/textures/<path>}，用 {@code blit} 绘制，
 *       id <b>需要</b>完整路径（含 {@code textures/} 前缀与扩展名）。</li>
 * </ul>
 *
 * <h3>设计要点</h3>
 * <ul>
 *   <li>不再硬编码任何 MODID —— 命名空间由 {@link #of(String)} 注入，本类属于纯 UI lib，
 *       不认识任何具体模组；</li>
 *   <li>实例按命名空间缓存，多命名空间（模组自身 + 资源包扩展）互不干扰；</li>
 *   <li>缓存可 {@link #invalidate()}，配合资源重载监听即可热更新，无需重启。</li>
 * </ul>
 *
 * <h3>关于缓存</h3>
 * 探测要走 ResourceManager 的 IO 查找，绝不能每帧执行，因此结果被永久缓存。
 * 资源系统尚未就绪时（极早期初始化）返回 {@code false} 且<b>不写缓存</b>，留待下次重探。
 */
public final class GuiSkin {

    /** sprite 在资源包中的实际目录前缀（仅用于存在性探测，不用于 id 构造） */
    private static final String SPRITE_DIR = "textures/gui/sprites/";
    /** 普通贴图的目录前缀 */
    private static final String TEXTURE_DIR = "textures/";

    /** 按命名空间缓存实例，避免重复建缓存表 */
    private static final Map<String, GuiSkin> INSTANCES = new ConcurrentHashMap<>();

    /**
     * 取得指定命名空间的探测器。
     *
     * @param namespace 资源命名空间，例如模组 id
     */
    public static GuiSkin of(String namespace) {
        return INSTANCES.computeIfAbsent(namespace, GuiSkin::new);
    }

    /** 清空所有命名空间的探测缓存（资源包切换 / F3+T 后调用） */
    public static void invalidateAll() {
        INSTANCES.values().forEach(GuiSkin::invalidate);
    }

    private final String namespace;
    /** key = 资源包内完整路径，value = 是否存在 */
    private final Map<String, Boolean> existCache = new ConcurrentHashMap<>();

    private GuiSkin(String namespace) {
        this.namespace = namespace;
    }

    public String namespace() {
        return namespace;
    }

    // ------------------------------------------------------------------
    // Identifier 构造
    // ------------------------------------------------------------------

    /**
     * 构造 sprite id，用于 {@code blitSprite}。
     *
     * @param path 如 {@code "widget/panel"}，对应 {@code textures/gui/sprites/widget/panel.png}
     */
    public Identifier sprite(String path) {
        return Identifier.fromNamespaceAndPath(namespace, path);
    }

    /**
     * 构造普通贴图 id，用于 {@code blit}。
     *
     * @param path 相对 {@code textures/} 的路径，如 {@code "gui/box1.png"}（含扩展名）
     */
    public Identifier texture(String path) {
        return Identifier.fromNamespaceAndPath(namespace, TEXTURE_DIR + path);
    }

    // ------------------------------------------------------------------
    // 存在性探测
    // ------------------------------------------------------------------

    /**
     * sprite 是否已存在。
     *
     * @param path 如 {@code "widget/panel"}（不含目录前缀与 {@code .png}）
     */
    public boolean spriteExists(String path) {
        return exists(SPRITE_DIR + path + ".png");
    }

    /**
     * 普通贴图是否已存在。
     *
     * @param path 相对 {@code textures/} 的路径，如 {@code "gui/box1.png"}（含扩展名）
     */
    public boolean textureExists(String path) {
        return exists(TEXTURE_DIR + path);
    }

    /**
     * 底层探测：给定资源包内完整相对路径判断资源是否存在，结果缓存。
     *
     * @param fullPath 如 {@code "textures/gui/sprites/widget/panel.png"}
     */
    public boolean exists(String fullPath) {
        Boolean cached = existCache.get(fullPath);
        if (cached != null) return cached;

        Minecraft mc = Minecraft.getInstance();
        // 资源系统未就绪时不写缓存，留待下次再探，避免把 false 永久钉死
        if (mc == null || mc.getResourceManager() == null) return false;

        boolean found;
        try {
            found = mc.getResourceManager()
                    .getResource(Identifier.fromNamespaceAndPath(namespace, fullPath))
                    .isPresent();
        } catch (Exception e) {
            // 任何异常都视为「贴图没做好」，安全退回硬渲染，绝不让 UI 崩掉
            found = false;
        }
        existCache.put(fullPath, found);
        return found;
    }

    // ------------------------------------------------------------------
    // 缓存管理
    // ------------------------------------------------------------------

    /** 清空本命名空间的探测缓存 */
    public void invalidate() {
        existCache.clear();
    }

}
