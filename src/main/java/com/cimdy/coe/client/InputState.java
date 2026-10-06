package com.cimdy.coe.client;
import com.cimdy.coe.data.codec.support.status.def.StatusDefs;

import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.block.entity.TerminalBlockEntity;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.data.codec.stronghold.task.site.helper.SiteBindingHandler;
import com.cimdy.coe.data.codec.support.status.StatusHudEntry;
import com.cimdy.coe.helper.EntityHelper;
import com.cimdy.coe.helper.LookAtHelper;
import com.cimdy.coe.stronghold.energy.StrongholdHelper;
import com.cimdy.coe.network.entity.*;
import com.cimdy.coe.network.input.*;
import com.cimdy.coe.network.menu.*;
import com.cimdy.coe.network.research.*;
import com.cimdy.coe.network.skill.*;
import com.cimdy.coe.network.stronghold.*;
import com.cimdy.coe.network.sync.*;
import com.cimdy.coe.ui.screen.StunScreen;
import com.cimdy.coe.ui.screen.gem.SkillScreen;
import com.cimdy.coe.ui.screen.stronghold.SiteScreen;
import com.cimdy.coe.ui.screen.stronghold.StockScreen;
import com.cimdy.coe.ui.screen.stronghold.TeleportScreen;
import com.cimdy.coe.ui.screen.stronghold.TerminalScreen;
import com.cimdy.coe.ui.screen.radial.RadialMenuScreen;
import com.cimdy.coe.ui.screen.radial.RadialMenus;
import com.cimdy.coe.ui.screen.CodexScreen;
import com.cimdy.coe.ui.screen.KeywordShowcaseScreen;
import com.cimdy.coe.ui.screen.GalleryScreen;
import com.cimdy.coe.ui.screen.StylePickerScreen;
import com.cimdy.coe.ui.widget.render.Theme;
import com.cimdy.coe.ui.widget.render.StylePresets;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import java.util.List;

@EventBusSubscriber(modid = CraftOfExile.MODID, value = Dist.CLIENT)
public class InputState {

    // 技能槽布局：前 5 个槽（0-4）直接绑定数字键 1-5；
    // 后 5 个槽（5-9）在“辅助键”按下时，由数字键 1-5 触发（辅助键 + 1~5）。
    // 辅助键本身不单独释放技能，仅作为后 5 个槽的修饰键。
    public static KeyMapping Skill_0;
    public static KeyMapping Skill_1;
    public static KeyMapping Skill_2;
    public static KeyMapping Skill_3;
    public static KeyMapping Skill_4;

    /** 辅助键：按住时数字键 1-5 映射到后 5 个技能槽（5-9）。 */
    public static KeyMapping Skill_AUX;

    /** 仅前 5 个数字键（对应槽位 0-4；辅助键按下时映射到 5-9）。 */
    public static List<KeyMapping> SKILL_KEYS;

    public static KeyMapping OPEN_SKILL;

    public static KeyMapping OPEN_TALENT;

    /** 径向轮盘菜单：单个按键囊括技能装配 / 天赋等界面（取代分散的多个打开键）。 */
    public static KeyMapping OPEN_MENU;

    /** 测试面板（基类链路验证）：H 键打开内嵌背包面板界面。 */
    public static KeyMapping TEST_PANEL;

    /** 物品图鉴 / 查询界面（只读浏览物品、词缀、属性）：L 键打开。纯客户端界面，无需 Menu 同步。 */
    public static KeyMapping OPEN_CODEX;

    /** 关键字参考展示屏（悬停 / ALT 钻取 / 左键钉住演示）：K 键打开。纯客户端界面，无需 Menu 同步。 */
    public static KeyMapping OPEN_KEYWORD;

    /** UI 组件库浏览屏（组件目录 + 实况演示）：G 键打开。纯客户端界面，无需 Menu 同步。 */
    public static KeyMapping OPEN_GALLERY;

    /** 风格预设：循环切换到下一个预设（任意 Screen 生效）。默认 ] 键。 */
    public static KeyMapping CYCLE_STYLE;

    /** 风格预设：打开预设选择界面（任意 Screen 生效）。默认 [ 键。 */
    public static KeyMapping OPEN_STYLE_PICKER;

    /** 风格预设：运行时重新加载 config/coe/styles.json（任意 Screen 生效）。默认 ' 键。 */
    public static KeyMapping RELOAD_STYLE;

    /** 翻滚（Dodge Roll）键：默认 X；按下向服务端发起翻滚，方向取当前移动输入 / 视线前方。
     *  该键亦作为原版 sprint 的额外触发源（见 client SprintingMixin）：按住 = 翻滚结束后接奔跑。 */
    public static KeyMapping DODGE_ROLL;

    public static KeyMapping INTERACT_0;
    public static KeyMapping INTERACT_1;
    public static KeyMapping INTERACT_2;
    public static KeyMapping INTERACT_3;

    public static List<KeyMapping> INTERACTS;

    /**
     * Flask 使用键（POE2 原版为数字 1/2；本 mod 数字 1–5 已被技能槽占用，
     * 故顺延为 6/7，并新增「全部使用」键 `（GRAVE_ACCENT，不与原版任何键冲突）。
     * 避开 Q（丢弃）/E（背包）等原版硬冲突键。
     */
    public static KeyMapping FLASK_1;
    public static KeyMapping FLASK_2;
    public static KeyMapping FLASK_ALL;

    @SubscribeEvent
    public static void registerBindings(RegisterKeyMappingsEvent event) {
        KeyMapping.Category skill = new KeyMapping.Category(
                Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "skill.skill"));
        event.registerCategory(skill);
        // 前 5 个槽（0-4）直接绑定数字键 1-5
        Skill_0 = new KeyMapping("keymapping.coe.skill_0", GLFW.GLFW_KEY_1, skill);
        Skill_1 = new KeyMapping("keymapping.coe.skill_1", GLFW.GLFW_KEY_2, skill);
        Skill_2 = new KeyMapping("keymapping.coe.skill_2", GLFW.GLFW_KEY_3, skill);
        Skill_3 = new KeyMapping("keymapping.coe.skill_3", GLFW.GLFW_KEY_4, skill);
        Skill_4 = new KeyMapping("keymapping.coe.skill_4", GLFW.GLFW_KEY_5, skill);
        // 辅助键：按住时数字键 1-5 映射到后 5 个槽（5-9）
        Skill_AUX = new KeyMapping("keymapping.coe.skill_aux", GLFW.GLFW_KEY_R, skill);

        INTERACT_0 = new KeyMapping("keymapping.coe.interact_0", GLFW.GLFW_KEY_F, skill);
        INTERACT_1 = new KeyMapping("keymapping.coe.interact_1", GLFW.GLFW_KEY_C, skill);
        INTERACT_2 = new KeyMapping("keymapping.coe.interact_2", GLFW.GLFW_KEY_V, skill);
        INTERACT_3 = new KeyMapping("keymapping.coe.interact_3", GLFW.GLFW_KEY_Z, skill);

        // 原「打开技能装配」「打开天赋」按键改为默认空（UNKNOWN），仍可在设置中由玩家自行绑定；
        // 统一由径向轮盘菜单（OPEN_MENU）取代，将多个必备打开键缩减为 1 个。
        OPEN_SKILL = new KeyMapping("keymapping.coe.open_skill", GLFW.GLFW_KEY_UNKNOWN, skill);
        event.register(OPEN_SKILL);

        OPEN_TALENT = new KeyMapping("keymapping.coe.open_talent", GLFW.GLFW_KEY_UNKNOWN, skill);
        event.register(OPEN_TALENT);

        OPEN_MENU = new KeyMapping("keymapping.coe.open_menu", GLFW.GLFW_KEY_G, skill);
        event.register(OPEN_MENU);

        // Flask 使用键（数字 6/7 承接 POE2 的 1/2；` 键 = 全部使用）
        FLASK_1 = new KeyMapping("keymapping.coe.flask_1", GLFW.GLFW_KEY_6, skill);
        FLASK_2 = new KeyMapping("keymapping.coe.flask_2", GLFW.GLFW_KEY_7, skill);
        FLASK_ALL = new KeyMapping("keymapping.coe.flask_all", GLFW.GLFW_KEY_GRAVE_ACCENT, skill);
        event.register(FLASK_1);
        event.register(FLASK_2);
        event.register(FLASK_ALL);

        // 翻滚键（默认 X；与原版及既有按键均无冲突，可在设置中重绑定）
        DODGE_ROLL = new KeyMapping("keymapping.coe.dodge_roll", GLFW.GLFW_KEY_X, skill);
        event.register(DODGE_ROLL);

        TEST_PANEL = new KeyMapping("keymapping.coe.test_panel", GLFW.GLFW_KEY_H, skill);
        event.register(TEST_PANEL);

        OPEN_CODEX = new KeyMapping("keymapping.coe.open_codex", GLFW.GLFW_KEY_L, skill);
        event.register(OPEN_CODEX);

        OPEN_KEYWORD = new KeyMapping("keymapping.coe.open_keyword", GLFW.GLFW_KEY_K, skill);
        event.register(OPEN_KEYWORD);

        // C.7 勘误：OPEN_GALLERY 原默认 G，与 OPEN_MENU（同 G）冲突。KeyMapping 静态 Map 后注册
        // 覆盖先注册，OPEN_GALLERY 晚于 OPEN_MENU 注册，G 实际只路由到画廊 → OPEN_MENU.consumeClick()
        // 永不命中（主世界开不出圆盘，径向成死键）。现默认空绑，把 G 唯一让给 OPEN_MENU；
        // 画廊改由玩家在「选项>按键控制」自绑（径向菜单内也有重绑入口）。
        OPEN_GALLERY = new KeyMapping("keymapping.coe.open_gallery", GLFW.GLFW_KEY_UNKNOWN, skill);
        event.register(OPEN_GALLERY);

        // 风格预设（B2）：循环切换 / 打开选择界面，均任意 Screen 生效
        CYCLE_STYLE = new KeyMapping("keymapping.coe.cycle_style", GLFW.GLFW_KEY_RIGHT_BRACKET, skill);
        event.register(CYCLE_STYLE);
        OPEN_STYLE_PICKER = new KeyMapping("keymapping.coe.open_style_picker", GLFW.GLFW_KEY_LEFT_BRACKET, skill);
        event.register(OPEN_STYLE_PICKER);

        // 风格预设（B2）：运行时重新加载 styles.json —— 任意 Screen 生效；' 键未被其它绑定占用
        RELOAD_STYLE = new KeyMapping("keymapping.coe.reload_style", GLFW.GLFW_KEY_APOSTROPHE, skill);
        event.register(RELOAD_STYLE);

        event.register(Skill_0);
        event.register(Skill_1);
        event.register(Skill_2);
        event.register(Skill_3);
        event.register(Skill_4);
        event.register(Skill_AUX);
        SKILL_KEYS = List.of(Skill_0, Skill_1, Skill_2, Skill_3, Skill_4);

        event.register(INTERACT_0);
        event.register(INTERACT_1);
        event.register(INTERACT_2);
        event.register(INTERACT_3);

        INTERACTS = List.of(INTERACT_0, INTERACT_1, INTERACT_2, INTERACT_3);
    }

    public static boolean isHoldingCtrl() {
        return InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), GLFW.GLFW_KEY_LEFT_CONTROL);
    }

    public static boolean isHoldingAlt() {
        return InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), GLFW.GLFW_KEY_LEFT_ALT);
    }

    public static boolean isHoldingShift() {
        return InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT);
    }

    /** 技能槽总数（前 5 + 后 5）。 */
    public static final int SKILL_SLOT_COUNT = 10;

    /** 辅助键当前是否被按住。 */
    public static boolean isAuxDown() {
        return Skill_AUX.isDown();
    }

    /**
     * 返回当前被按下的技能槽索引。数字键 1-5 对应槽位 0-4；
     * 当辅助键（{@link #Skill_AUX}）被按住时，数字键 1-5 映射到后 5 个槽位 5-9。
     */
    public static int getLastPressedSkillKey() {
        Player player = Minecraft.getInstance().player;
        if (player == null || !player.hasData(AttachmentRegistry.SKILL_SAVE)) return -1;
        boolean aux = isAuxDown();
        for (int n = 0; n < SKILL_KEYS.size(); n++) {
            if (SKILL_KEYS.get(n).isDown()) {
                return aux ? 5 + n : n;
            }
        }
        return -1;
    }

    /** 返回某槽位在 HUD / 装配界面中显示的按键标签（如 “1” 或 “A+1”）；槽位 ≥ 按键槽总数时返回空（无绑定键）。 */
    public static Component getSkillKeyLabel(int slot) {
        if (slot < 0 || slot >= SKILL_SLOT_COUNT) {
            return Component.empty();
        }
        int n = slot % 5;
        if (slot >= 5) {
            return Component.literal("A+").append(Component.literal(String.valueOf(n + 1)));
        }
        return Component.literal(String.valueOf(n + 1));
    }

    private static boolean interact0WasDown = false;
    private static boolean interact1WasDown = false;
    private static boolean interact2WasDown = false;
    private static boolean interact3WasDown = false;
    @Nullable private static BlockPos cachedPos;

    /**
     * 玩家眩晕硬控：检测同步快照 STATUS_HUD 中的轻晕（STUN）/重晕（HEAVY_STUN），
     * 命中则打开无渲染 {@link StunScreen}（拦截输入/移动，简单控制），消失自动关闭。
     * 数据用同步附件而非 STATUS_STORE（后者不传客户端）。
     */
    @SubscribeEvent
    public static void onStunControlTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        AbstractClientPlayer player = mc.player;
        if (player == null) return;
        List<StatusHudEntry> effects = player.getData(AttachmentRegistry.STATUS_HUD);
        boolean stunned = effects != null && effects.stream().anyMatch(e ->
                e.id().equals(StatusDefs.STUN.id) || e.icon().equals(StatusDefs.HEAVY_STUN_ICON));
        if (stunned) {
            if (mc.gui.screen() == null) {
                mc.setScreenAndShow(new StunScreen(player, Component.translatable("stat.stun")));
            }
        } else if (mc.gui.screen() instanceof StunScreen) {
            mc.setScreenAndShow(null);
        }
    }

    @SubscribeEvent
    public static void onKeyTick(ClientTickEvent.Pre event) {
        AbstractClientPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        // 0. 打开径向轮盘菜单：单个按键囊括技能装配 / 天赋等，按住显示、松开关闭
        // C.7：此判断置于所有 Screen 提前返回之前 → 主世界可开圆盘；且任何本模组 Screen 打开时
        // 只要该键未被当前 Screen 拦截消费，consumeClick() 仍会命中并弹出圆盘（不拦截时响应）。
        // 配合上方 OPEN_GALLERY 让出 G，主世界按 G 现可正常开圆盘。
        if (OPEN_MENU.consumeClick()) {
            Minecraft.getInstance().setScreenAndShow(new RadialMenuScreen(RadialMenus.buildMainMenu()));
        }

        // 风格预设（B2）：循环切换 / 打开选择界面 —— 任意 Screen 生效（置于轮盘菜单提前返回之前）
        if (CYCLE_STYLE.consumeClick()) {
            Theme.cyclePreset();
        }
        // C.11 皮肤入口：OPEN_STYLE_PICKER（[ 键）打开 StylePickerScreen。此处同样置于所有
        // Screen 提前返回之前 → 主世界可开、任何本模组 Screen 打开时按键可用（不拦截时响应）。
        // 已为全局注册（ClientTickEvent.Pre 每轮轮询），无需额外补注册。
        if (OPEN_STYLE_PICKER.consumeClick()) {
            Minecraft.getInstance().setScreenAndShow(new StylePickerScreen());
        }
        // 风格预设（B2）：运行时重新加载 styles.json，编辑配置后立即生效（置于轮盘菜单提前返回之前）
        if (RELOAD_STYLE.consumeClick()) {
            StylePresets.reload();
        }
        // 轮盘菜单打开时跳过其余按键处理，避免误触
        if (Minecraft.getInstance().gui.screen() instanceof RadialMenuScreen) return;

        // 0. 打开技能装配界面（仍可经重绑定后的原按键触发）
        if (OPEN_SKILL.consumeClick()) {
            ClientPacketDistributor.sendToServer(new OpenSkillMenuPacket());
        }

        // 0. 测试面板（基类链路验证）：Menu 操控物品 + 内嵌可交互背包
        if (TEST_PANEL.consumeClick()) {
            ClientPacketDistributor.sendToServer(new OpenTestPanelPacket());
        }

        // 0. 物品图鉴 / 查询界面：纯客户端只读浏览，直接打开（不走 Menu / 网络）
        if (OPEN_CODEX.consumeClick()) {
            Minecraft.getInstance().setScreenAndShow(new CodexScreen());
        }

        // 0. 关键字参考展示屏：纯客户端只读浏览，直接打开（不走 Menu / 网络）
        if (OPEN_KEYWORD.consumeClick()) {
            Minecraft.getInstance().setScreenAndShow(new KeywordShowcaseScreen());
        }

        // 0. UI 组件库浏览屏：纯客户端只读浏览，直接打开（不走 Menu / 网络）
        if (OPEN_GALLERY.consumeClick()) {
            Minecraft.getInstance().setScreenAndShow(new GalleryScreen());
        }

        // 0.1 打开天赋树界面（仍可经重绑定后的原按键触发）
        if (OPEN_TALENT.consumeClick()) {
            ClientPacketDistributor.sendToServer(new OpenTalentMenuPacket(
                    Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "player_talent")));
        }

        // 技能装配界面打开时不处理技能施放，避免绑定按键时误触
        if (Minecraft.getInstance().gui.screen() instanceof SkillScreen) return;

        // 1. 技能按键处理（基于固定槽位 KeyMapping）
        int skillIndex = getLastPressedSkillKey();
        if (skillIndex >= 0) {
//            player.setDeltaMovement(player.getLookAngle()
//                    .multiply(1.0, 0.0, 1.0)
//                    .normalize().scale(1.5)
//                    .add(0, player.getDeltaMovement().y, 0)); // 这是冲刺欸
            ClientPacketDistributor.sendToServer(new KeyPressPacket(skillIndex, false));
        } else if (EntityHelper.isJustReleasedSkillKey(player)) {
            ClientPacketDistributor.sendToServer(new KeyPressPacket(-1, true));
        }

        // 1.1 Flask 使用按键（6/7 = 两槽药剂，` = 全部使用）。
        // 各键独立判定：同一帧可同时触发多个 Flask（POE2 允许并行饮用不同药剂）。
        if (FLASK_1.consumeClick()) {
            ClientPacketDistributor.sendToServer(new FlaskUsePacket(0));
        }
        if (FLASK_2.consumeClick()) {
            ClientPacketDistributor.sendToServer(new FlaskUsePacket(1));
        }
        if (FLASK_ALL.consumeClick()) {
            ClientPacketDistributor.sendToServer(new FlaskUsePacket(-1));
        }

        // 3. 翻滚（Dodge Roll）/ 奔跑（Sprint，原版 mixin 驱动）
        // 点按发起翻滚：服务端判定方向 / 冷却 / 禁用并广播动画。
        // 按住翻滚键接奔跑由 client SprintingMixin 直接读取本键状态驱动原版 sprint，无需额外发包。
        if (DODGE_ROLL.consumeClick() && Minecraft.getInstance().gui.screen() == null) {
            LocalPlayer lp = Minecraft.getInstance().player;
            if (lp != null) {
                // flip = 翻滚键 + 空格同时按住（空翻变体）；方向取当前移动输入（xxa/zza）。
                boolean flip = Minecraft.getInstance().options.keyJump.isDown();
                ClientPacketDistributor.sendToServer(new DodgeRollPacket(lp.xxa, lp.zza, flip));
            }
        }

        // 2. 领地内交互处理
        if (skillIndex == -1 && StrongholdHelper.isPlayerInHold(player)) {

            StrongholdHelper.HoldRef ref = StrongholdHelper.getHoldRefForEntity(player);
            if (ref == null || ref.pos() == null || ref.hold() == null) return;

            BlockPos lookingPos = LookAtHelper.getBlockPosLookingAt(player, 6.0);
            cachedPos = lookingPos;
            if (lookingPos == null) {
                if (INTERACT_3.isDown()) SiteBindingHandler.clearSelection();
                return;
            }
            Level level = player.level();
            BlockEntity lookingBE = level.getBlockEntity(lookingPos);
            lookingBE = StrongholdHelper.getCurrentBlockEntity(level, lookingBE); // 自定义处理

            if (!(level.getBlockEntity(ref.pos().pos()) instanceof TerminalBlockEntity terminal)) return;

            // 获取当前按键状态
            boolean interact0Down = INTERACT_0.isDown();
            boolean interact1Down = INTERACT_1.isDown();
            boolean interact2Down = INTERACT_2.isDown();
            boolean interact3Down = INTERACT_3.isDown();
            // ----- 处理各个交互按键（仅在按下瞬间触发）-----
            if (interact0Down && !interact0WasDown) {
                // 交互键0：打开终端或存量设定界面
                if (lookingBE instanceof TerminalBlockEntity t) {
                    Minecraft.getInstance().setScreenAndShow(new TerminalScreen(t, player));
                } else if (ref.hold().getSite(lookingPos).isPresent()) {
                    Minecraft.getInstance().setScreenAndShow(new SiteScreen(player, ref.pos(), lookingPos));
                } else if (lookingBE != null && lookingBE.hasData(AttachmentRegistry.STOCK_SET)) {
                    Minecraft.getInstance().setScreenAndShow(new StockScreen(player, lookingBE, terminal));
                }
            } else if (interact1Down && !interact1WasDown) { // 交互键1
                // 终端传送
                if (lookingBE instanceof TerminalBlockEntity t) {
                    Minecraft.getInstance().setScreenAndShow(new TeleportScreen(t, player));
                }else {
                    if (!SiteBindingHandler.handleSite(level, lookingPos, ref)) {
                        SiteBindingHandler.handleInteraction(player, lookingPos, ref, INTERACT_1);
                    }
                }
            } else if (interact2Down && !interact2WasDown) {
                SiteBindingHandler.handleInteraction(player, lookingPos, ref, INTERACT_2);
            } else if (interact3Down && !interact3WasDown) {
                SiteBindingHandler.handleInteraction(player, lookingPos, ref, INTERACT_3);
            }

            // 更新上一帧按键状态
            interact0WasDown = interact0Down;
            interact1WasDown = interact1Down;
            interact2WasDown = interact2Down;
            interact3WasDown = interact3Down;
        } else {
            // 不在领地内，重置所有按键状态，并清除任何可能残留的选择
            interact0WasDown = false;
            interact1WasDown = false;
            interact2WasDown = false;
            interact3WasDown = false;
            SiteBindingHandler.clearSelection();
        }
    }

    @Nullable
    public static BlockPos getCachedPos() {
        return cachedPos;
    }
}
