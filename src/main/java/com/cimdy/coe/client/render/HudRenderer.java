package com.cimdy.coe.client.render;

import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.client.InputState;
import com.cimdy.coe.block.entity.TerminalBlockEntity;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.data.codec.item.CharmSave;
import com.cimdy.coe.data.codec.skill.core.Skill;
import com.cimdy.coe.data.codec.skill.core.SkillCaster;
import com.cimdy.coe.data.codec.skill.context.SkillContext;
import com.cimdy.coe.data.codec.skill.gem.SkillData;
import com.cimdy.coe.data.codec.skill.gem.KeyBind;
import com.cimdy.coe.data.codec.skill.gem.SkillAssembly;
import com.cimdy.coe.data.codec.stronghold.task.site.Site;
import com.cimdy.coe.data.codec.stronghold.task.site.SiteTypes;
import com.cimdy.coe.data.codec.stronghold.task.site.helper.ActionKey;
import com.cimdy.coe.data.codec.stronghold.task.site.helper.SiteBindingHandler;
import com.cimdy.coe.data.codec.support.status.StatusHudEntry;
import com.cimdy.coe.helper.LookAtHelper;
import com.cimdy.coe.stronghold.energy.StrongholdHelper;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.cimdy.coe.data.helper.hub.ResourceRegistry;

/**
 * HUD 渲染（原 {@code event.GuiEvent}，铁律 #56 迁出）：纯客户端，仅监听 {@code RenderGuiLayerEvent.Pre}。
 */
@EventBusSubscriber(modid = CraftOfExile.MODID, value = Dist.CLIENT)
public class HudRenderer {
    private static Map<ActionKey, Component> currentHints = new LinkedHashMap<>();

    @SubscribeEvent
    public static void RenderGuiLayerEvent(RenderGuiLayerEvent.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        Window window = Minecraft.getInstance().getWindow();
        GuiGraphicsExtractor guiGraphics = event.getGuiGraphics();

        int guiWidth = window.getGuiScaledWidth();
        int guiHeight = window.getGuiScaledHeight();

        AbstractClientPlayer player = minecraft.player;
        if (player != null) {
            if (!player.isCreative()) {
                renderHealthState(guiGraphics, guiHeight, guiWidth, player, minecraft);
            }
            renderSkillState(guiGraphics, guiHeight, guiWidth, player, minecraft);

            renderStatusEffects(guiGraphics, guiHeight, guiWidth, player, minecraft);

            renderCharmEffects(guiGraphics, guiHeight, guiWidth, player, minecraft);

            renderHoldInfo(guiGraphics, guiHeight, guiWidth, player, minecraft);
        }
    }

    private static void renderHoldInfo(GuiGraphicsExtractor guiGraphics, int guiHeight, int guiWidth, AbstractClientPlayer player, Minecraft minecraft) {
        if (StrongholdHelper.isPlayerInHold(player)) {
            StrongholdHelper.HoldRef ref = StrongholdHelper.getHoldRefForEntity(player);
            if (ref == null || ref.pos() == null || ref.hold() == null) return;
            renderInfo(guiGraphics, guiHeight, guiWidth, minecraft);
            Map<ActionKey, Component> hints = new LinkedHashMap<>();
            if (StrongholdHelper.isLookAtContainer(player)) {
                hints.put(new ActionKey(InputState.INTERACT_0),
                        Component.translatable("hold.coe.stock_set"));
            }
            if (LookAtHelper.getBlockEntityLookAt(player, 6.0) instanceof TerminalBlockEntity) {
                hints.put(new ActionKey(InputState.INTERACT_0),
                        Component.translatable("hold.coe.terminal_set"));
                hints.put(new ActionKey(InputState.INTERACT_1),
                        Component.translatable("hold.coe.terminal_teleport"));
            }

            Level level = player.level();
            Site selectedSite = SiteBindingHandler.getCachedSite();
            BlockPos lookPos = InputState.getCachedPos();
            // 站点方块
            if (lookPos != null && SiteTypes.isSiteBlock(level, lookPos)) {
                boolean isLookSelected = selectedSite != null && lookPos.equals(selectedSite.getSitePos());
                boolean isHasSite = ref.hold().holdState().getSite(lookPos).isPresent();
                if (!isHasSite && !isLookSelected) {
                    hints.put(new ActionKey(InputState.INTERACT_1),
                            Component.translatable("hold.site.coe.bind_site"));
                } else if (!isLookSelected) {
                    hints.put(new ActionKey(InputState.INTERACT_1),
                            Component.translatable("hold.site.coe.select_site"));
                }
                if (isHasSite) {
                    hints.put(new ActionKey(InputState.INTERACT_0),
                            Component.translatable("hold.site.coe.site_set"));
                }
            }
            // 绑定方块
            if (selectedSite != null) {
                if (lookPos != null) {
                    hints.putAll(selectedSite.getInteractionHint(level, lookPos).getHints());
                } else {
                    hints.put(new ActionKey(InputState.INTERACT_3),
                            Component.translatable("hold.site.coe.cancel_selection"));
                }
            }
            currentHints = hints;
            SiteBindingHandler.renderSite(level);
        }
        renderBindSide(guiGraphics, guiHeight, guiWidth, minecraft);
    }

    public static void renderBindSide(
            GuiGraphicsExtractor guiGraphics, int guiHeight, int guiWidth, Minecraft minecraft) {
        if (currentHints.isEmpty()) return;

        int maxKeyWidth = 0;
        int maxDescWidth = 0;
        for (Map.Entry<ActionKey, Component> entry : currentHints.entrySet()) {
            int keyW = minecraft.font.width(entry.getKey().getText());
            int descW = minecraft.font.width(entry.getValue());
            if (keyW > maxKeyWidth) maxKeyWidth = keyW;
            if (descW > maxDescWidth) maxDescWidth = descW;
        }

        final int PADDING = 6;
        int leftAreaWidth = maxKeyWidth + PADDING * 2;
        int rightAreaWidth = maxDescWidth + PADDING * 2;

        int startX = guiWidth / 2 + 30;
        int startY = guiHeight / 2 + 10;
        int boxHeight = 12;
        int rowSpacing = 2;
        int textYOffset = 2; // 固定顶部偏移

        int index = 0;
        for (Map.Entry<ActionKey, Component> entry : currentHints.entrySet()) {
            ActionKey key = entry.getKey();
            Component description = entry.getValue();
            Component keyText = key.getText();

            int boxX = startX;
            int boxY = startY + (boxHeight + rowSpacing) * index;

            // 左侧按键区
            guiGraphics.fill(boxX, boxY, boxX + leftAreaWidth, boxY + boxHeight, 0x80808080);
            if (key.isDown()) {
                guiGraphics.fill(boxX, boxY, boxX + leftAreaWidth, boxY + boxHeight, 0x40ADD8E6);
            }

            // 右侧描述区
            int descBoxX = boxX + leftAreaWidth + 1;
            guiGraphics.fill(descBoxX, boxY, descBoxX + rightAreaWidth, boxY + boxHeight, 0x80808080);

            // 边框（保持不变）
            guiGraphics.verticalLine(boxX + leftAreaWidth, boxY, boxY + boxHeight, 0xFFADD8E6);
            guiGraphics.horizontalLine(boxX, boxX + 2, boxY, 0xFFADD8E6);
            guiGraphics.horizontalLine(boxX, boxX + 2, boxY + boxHeight - 1, 0xFFADD8E6);
            guiGraphics.horizontalLine(descBoxX + rightAreaWidth - 1, descBoxX + rightAreaWidth - 3, boxY, 0xFFADD8E6);
            guiGraphics.horizontalLine(descBoxX + rightAreaWidth - 1, descBoxX + rightAreaWidth - 3, boxY + boxHeight - 1, 0xFFADD8E6);
            guiGraphics.verticalLine(boxX, boxY, boxY + boxHeight, 0xFFFFFFFF);
            guiGraphics.fill(boxX, boxY, boxX + 1, boxY + 1, 0xFFFFFFFF);
            guiGraphics.fill(boxX, boxY + boxHeight - 1, boxX + 1, boxY + boxHeight, 0xFFFFFFFF);
            guiGraphics.fill(descBoxX + rightAreaWidth - 1, boxY, descBoxX + rightAreaWidth, boxY + 1, 0xFFFFFFFF);
            guiGraphics.fill(descBoxX + rightAreaWidth - 1, boxY + boxHeight - 1, descBoxX + rightAreaWidth, boxY + boxHeight, 0xFFFFFFFF);

            // 按键文本（固定顶部偏移）
            guiGraphics.centeredText(minecraft.font, keyText,
                    boxX + leftAreaWidth / 2, boxY + textYOffset, 0xFFFFFFFF);
            // 描述文本
            guiGraphics.centeredText(minecraft.font, description,
                    descBoxX + rightAreaWidth / 2, boxY + textYOffset, 0xFFFFFFFF);

            index++;
        }
    }

    private static void renderInfo(GuiGraphicsExtractor guiGraphics, int guiHeight, int guiWidth, Minecraft minecraft) {
        Component text = Component.translatable("hold.coe.description");
        int boxWidth = minecraft.font.width(text) + 2;
        int boxX = guiWidth - boxWidth - 2;
        int boxY = guiHeight / 2 - 50;
        renderTitle(guiGraphics, boxX, boxY, boxWidth, 0xFF000000,
                text, 0xFFFFFFFF);
    }

    private static void renderTitle(
            GuiGraphicsExtractor guiGraphics, int x, int y, int width, int bgColor, Component text, int textColor) {
        int height = 14;
        // 渲染背景
        guiGraphics.fill(x, y, x + width, y + height, bgColor);

        // 渲染边框（2像素高）
        guiGraphics.fill(x, y, x + width, y + 1, textColor); // 上边框
        guiGraphics.fill(x, y + height - 1, x + width, y + height, textColor); // 下边框

        // 渲染文字（1像素留空）
        int textX = x + 1;
        int textY = y + (height - Minecraft.getInstance().font.lineHeight) / 2;
        guiGraphics.text(Minecraft.getInstance().font, text, textX, textY, textColor);
    }

    private static void renderSkillState(
            GuiGraphicsExtractor guiGraphics, int guiHeight, int guiWidth, Player player, Minecraft minecraft
    ) {
        int scale = 20;
        int space = 3;
        int count = 5;
        // 辅助键按下时显示后 5 个槽（5-9），否则显示前 5 个槽（0-4）
        int base = InputState.isAuxDown() ? 5 : 0;
        int x = 2;
        int totalHeight = scale * count + space * (count - 1);
        int startY = (guiHeight - totalHeight) / 2;

        SkillAssembly assembly = KeyBind.getMergedAssembly(player);

        for (int n = 0; n < count; n++) {
            int key = base + n;
            int slotY = startY + n * (scale + space);

            // 槽位背景框（始终渲染，未装配也显示空槽位）
            guiGraphics.fill(x, slotY, x + scale, slotY + scale, 0x80000000);
            guiGraphics.fill(x, slotY, x + scale, slotY + 1, 0xFFADD8E6);
            guiGraphics.fill(x, slotY + scale - 1, x + scale, slotY + scale, 0xFFADD8E6);
            guiGraphics.fill(x, slotY, x + 1, slotY + scale, 0xFFADD8E6);
            guiGraphics.fill(x + scale - 1, slotY, x + scale, slotY + scale, 0xFFADD8E6);

            KeyBind.EntitySkill keyData = KeyBind.getEntitySkill(player, key);
            SkillData gem = (keyData.assemblySlot().getInt() >= 0 && keyData.assemblySlot().getInt() < SkillAssembly.SIZE)
                    ? assembly.get(keyData.assemblySlot().getInt()) : SkillData.EMPTY;
            if (!gem.isEmpty()) {
                guiGraphics.blit(RenderPipelines.GUI_TEXTURED, gem.getIcon(),
                        x, slotY, 0, 0, scale, scale, scale, scale);

                double cooldown = SkillCaster.of(player).getCooldown(key);
                if (cooldown > 0) {
                    // 黑色蒙版：按剩余 CD 比例递减，模拟 CD 刷新
                    double maxCd = getMaxCooldown(player, gem);
                    double ratio = maxCd > 0 ? Math.min(1.0, cooldown / maxCd) : 1.0;
                    int alpha = (int) (0.78 * 255 * ratio);
                    guiGraphics.fill(x, slotY, x + scale, slotY + scale, (alpha << 24) & 0xFF000000);
                    // 最上层渲染 CD 秒数
                    String cd = String.valueOf((int) Math.ceil(cooldown / 20.0));
                    guiGraphics.text(minecraft.font, Component.literal(cd),
                            x + 5, slotY + 5, 0xFFFFFFFF);
                } else if (!canCastClient(player, key, gem)) {
                    // 条件不满足（魔力/等级/装备等）：红色蒙版
                    guiGraphics.fill(x, slotY, x + scale, slotY + scale, 0x80FF3030);
                }
            }

            // 按键标签（缩小渲染在图标左上角）
            Component keyName = InputState.getSkillKeyLabel(key);
            RenderHelper.renderStringWithScale(guiGraphics, keyName, 0xFFFFFFFF, x, slotY, 0.6F);
        }
    }

    /**
     * 渲染玩家当前生效的状态（异常 / Debuff / Buff / 充能球）。
     * 图标优先取技能图标（由技能施加的状态），否则取 gui/status/&lt;id&gt;（poe2db 爬取）。
     * 数据来自同步附件 {@code status_hud}（服务端每 10 tick 汇总一次）。
     */
    private static void renderStatusEffects(
            GuiGraphicsExtractor guiGraphics, int guiHeight, int guiWidth, AbstractClientPlayer player, Minecraft minecraft) {
        List<StatusHudEntry> effects = player.getData(AttachmentRegistry.STATUS_HUD);
        if (effects == null || effects.isEmpty()) return;

        int size = 18;
        int space = 2;
        int perRow = 8;
        int startX = 2;
        int startY = 2;
        for (int i = 0; i < effects.size(); i++) {
            StatusHudEntry e = effects.get(i);
            int col = i % perRow;
            int row = i / perRow;
            int x = startX + col * (size + space);
            int y = startY + row * (size + space);

            guiGraphics.fill(x, y, x + size, y + size, 0x80000000);
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, e.icon(),
                    x, y, 0, 0, size, size, size, size);

            // 剩余时长（秒）；showTimeBelow=false 的常驻标记不显示
            if (e.showTimeBelow() && e.durationTicks() > 0) {
                String sec = String.valueOf((int) Math.ceil(e.durationTicks() / 20.0));
                guiGraphics.text(minecraft.font, Component.literal(sec),
                        x + size - minecraft.font.width(sec) - 1, y + size - 9, 0xFFFFFFFF);
            }
            // 层数（充能球 / 可叠层状态）
            if (e.stacks() > 1) {
                guiGraphics.text(minecraft.font, Component.literal(String.valueOf(e.stacks())),
                        x + 1, y + 1, 0xFFFFFF00);
            }
            // 自定义文本覆盖（如「复活中」）
            if (!e.text().isEmpty()) {
                guiGraphics.text(minecraft.font, Component.translatable(e.text()),
                        x + 1, y + size - 9, 0xFF9FE8FF);
            }
        }
    }

    /**
     * 渲染玩家当前激活的护符（数据驱动：效果经 {@code CharmEngine} 注入属性层，此处仅以
     * 护符<b>物品图标</b> + 剩余秒数呈现，区别于状态图标流）。
     * 数据来自同步附件 {@code charm_active}（slot → 剩余 tick）与 {@code charm_save}（取槽位物品），
     * 绘制于状态图标下方（与 {@link #renderStatusEffects} 同列、错开行）。
     */
    private static void renderCharmEffects(
            GuiGraphicsExtractor guiGraphics, int guiHeight, int guiWidth, AbstractClientPlayer player, Minecraft minecraft) {
        Map<Integer, Integer> active = player.getData(AttachmentRegistry.CHARM_ACTIVE);
        CharmSave save = player.getData(AttachmentRegistry.CHARM_SAVE);
        if (active == null || active.isEmpty()) return;

        int size = 18;
        int space = 2;
        int perRow = 8;
        // 置于状态图标下方：状态网格行数 = ceil(状态数 / 每行 8)
        List<StatusHudEntry> status = player.getData(AttachmentRegistry.STATUS_HUD);
        int statusRows = (status == null || status.isEmpty()) ? 0 : (int) Math.ceil((double) status.size() / perRow);
        int startY = 2 + statusRows * (size + space) + 4;
        int x = 2;
        for (var e : active.entrySet()) {
            int ticks = e.getValue();
            if (ticks <= 0) continue;
            ItemStack stack = save.get(e.getKey());
            if (stack == null || stack.isEmpty()) continue;
            guiGraphics.fill(x, startY, x + size, startY + size, 0x80000000);
            RenderHelper.renderItemWithScale(guiGraphics, stack, x, startY, size);
            String sec = String.valueOf((int) Math.ceil(ticks / 20.0));
            guiGraphics.text(minecraft.font, Component.literal(sec),
                    x + size - minecraft.font.width(sec) - 1, startY + size - 9, 0xFFFFFFFF);
            x += size + space;
        }
    }

    /** 客户端估算某技能的最大冷却（tick），用于 CD 蒙版比例。 */
    private static double getMaxCooldown(Player player, SkillData gem) {        Skill skill = gem.getSkill();
        if (skill == null) return 0;
        SkillContext context = new SkillContext(player, 0);
        context.skillLevel(gem.getEffectiveSkillLevel());
        context.quality(gem.getEffectiveQuality());
        return skill.cast().getCooldown(context);
    }

    /** 客户端判定技能当前是否满足施法条件。 */
    private static boolean canCastClient(Player player, int key, SkillData gem) {
        if (gem.isEmpty()) return false;
        Skill skill = gem.getSkill();
        if (skill == null) return false;
        SkillContext context = new SkillContext(player, key);
        context.skillLevel(gem.getEffectiveSkillLevel());
        context.quality(gem.getEffectiveQuality());
        return skill.canCast(context);
    }

    private static void renderHealthState(
            GuiGraphicsExtractor guiGraphics, int guiHeight, int guiWidth, Player player, Minecraft minecraft
    ) {
        int scale = 65;
        float textScale = 0.70F;
        int textHeight = (int) (scale + 3 + 12 * textScale);
        //不知道是什么的边框
        int wrapScale = scale + 3;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "textures/gui/box2.png"),
                0, guiHeight - wrapScale, 0, 0, wrapScale, wrapScale, wrapScale, wrapScale);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "textures/gui/box2.png"),
                guiWidth - wrapScale, guiHeight - wrapScale, 0, 0, wrapScale, wrapScale, wrapScale, wrapScale);
        wrapScale = scale + 2;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "textures/gui/box1.png"),
                0, guiHeight - wrapScale, 0, 0, wrapScale, wrapScale, wrapScale, wrapScale);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "textures/gui/box1.png"),
                guiWidth - wrapScale, guiHeight - wrapScale, 0, 0, wrapScale, wrapScale, wrapScale, wrapScale);
        wrapScale = scale + 1;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "textures/gui/box2.png"),
                0, guiHeight - wrapScale, 0, 0, wrapScale, wrapScale, wrapScale, wrapScale);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "textures/gui/box2.png"),
                guiWidth - wrapScale, guiHeight - wrapScale, 0, 0, wrapScale, wrapScale, wrapScale, wrapScale);

        scale = scale + 1;
        //不知道保留了多少的生命保留
//            guiGraphics.enableScissor(0, guiHeight - scale, scale, (int) (guiHeight - scale * ( 1 - 0.50)));
//            guiGraphics.blit(RenderType::guiTextured, Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "textures/gui/unuse.png"),
//                    0,guiHeight - scale,0,0,scale,scale,scale,scale);
//            guiGraphics.disableScissor();

        //生命值
        float f = player.getHealth() / player.getMaxHealth();
        guiGraphics.enableScissor(0, (int) (guiHeight - scale * f), scale, guiHeight);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "textures/gui/health.png"),
                0, guiHeight - scale, 0, 0, scale, scale, scale, scale);
        guiGraphics.disableScissor();

        // 能量护盾
        if (ResourceRegistry.max(player, "energy_shield") > 0) {
            f = (float) (ResourceRegistry.current(player, "energy_shield") / ResourceRegistry.max(player, "energy_shield"));
            guiGraphics.enableScissor(0, (int) (guiHeight - scale * f), scale, guiHeight);
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "textures/gui/energy.png"),
                    0, guiHeight - scale, 0, 0, scale, scale, scale, scale);
            guiGraphics.disableScissor();

            renderLeftText(guiGraphics, minecraft, guiHeight, scale, textHeight,
                    Component.translatable("stat.energy"),
                    Component.literal((int) ResourceRegistry.current(player, "energy_shield") + "/" + (int) ResourceRegistry.max(player, "energy_shield")), textScale);

            textHeight += (int) (12 * textScale);
        }

        renderLeftText(guiGraphics, minecraft, guiHeight, scale, textHeight,
                Component.translatable("stat.health"),
                Component.literal((int) player.getHealth() + "/" + (int) player.getMaxHealth()), textScale);

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "textures/gui/light.png"),
                0, guiHeight - scale, 0, 0, scale, scale, scale, scale);

        //不知道保留了多少的魔力
//            guiGraphics.enableScissor(guiWidth - scale, guiHeight - scale, guiWidth, (int) (guiHeight - scale * ( 1 - 0.50)));
//            guiGraphics.blit(RenderType::guiTextured, Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "textures/gui/unuse.png"),
//                    guiWidth - scale,guiHeight - scale,0,0,scale,scale,scale,scale);
//            guiGraphics.disableScissor();

        //魔力
        f = (float) (ResourceRegistry.current(player, "mana") / ResourceRegistry.max(player, "mana"));
        guiGraphics.enableScissor(guiWidth - scale, (int) (guiHeight - scale * f), guiWidth, guiHeight);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "textures/gui/mana.png"),
                guiWidth - scale, guiHeight - scale, 0, 0, scale, scale, scale, scale);
        guiGraphics.disableScissor();

        textHeight = (int) (scale + 12 * textScale);
        renderRightText(guiGraphics, minecraft, guiWidth, guiHeight, scale, textHeight,
                Component.translatable("stat.mana"),
                Component.literal((int) ResourceRegistry.current(player, "mana") + "/" + (int) ResourceRegistry.max(player, "mana")), textScale);

        //可能变成魔力的能量护盾
//            guiGraphics.enableScissor(guiWidth - scale, (int) (guiHeight - scale * 0.73), guiWidth, guiHeight);
//            guiGraphics.blit(RenderType::guiTextured, Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "textures/gui/energy.png"),
//                    guiWidth - scale,guiHeight - scale,0,0,scale,scale,scale,scale);
//            guiGraphics.disableScissor();

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "textures/gui/light.png"),
                guiWidth - scale, guiHeight - scale, 0, 0, scale, scale, scale, scale);
    }

    private static void renderLeftText(GuiGraphicsExtractor guiGraphics, Minecraft minecraft, int guiHeight, int scale,
                                       int textHeight, Component title, Component value, float textScale) {
        RenderHelper.renderStringWithScale(guiGraphics, title,
                (int) (scale * 0.05), guiHeight - textHeight, textScale);
        RenderHelper.renderStringWithScale(guiGraphics, value,
                (int) (scale * (0.05 + 0.25F)), guiHeight - textHeight, textScale);
    }

    private static void renderRightText(GuiGraphicsExtractor guiGraphics, Minecraft minecraft, int guiWidth, int guiHeight, int scale,
                                        int textHeight, Component title, Component value, float textScale) {
        RenderHelper.renderStringWithScale(guiGraphics, title,
                guiWidth - (int) (scale * 0.05) - minecraft.font.width(title), guiHeight - textHeight, textScale);
        RenderHelper.renderStringWithScale(guiGraphics, value,
                guiWidth - (int) (scale * (0.05 + 0.25F)) - minecraft.font.width(value), guiHeight - textHeight, textScale);
    }
}
