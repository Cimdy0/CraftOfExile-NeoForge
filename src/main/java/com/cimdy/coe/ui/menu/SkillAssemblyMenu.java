package com.cimdy.coe.ui.menu;

import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.registry.DataComponentRegistry;
import com.cimdy.coe.data.codec.skill.core.SkillCaster;
import com.cimdy.coe.data.codec.skill.gem.GemCategory;
import com.cimdy.coe.data.codec.skill.gem.SkillData;
import com.cimdy.coe.data.codec.skill.gem.SupportData;
import com.cimdy.coe.data.codec.skill.gem.KeyBind;
import com.cimdy.coe.data.codec.skill.gem.SkillAssembly;
import com.cimdy.coe.network.skill.SkillAssemblyActionPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

/**
 * 技能装配界面的菜单（容器）。
 *
 * <p>仅包含玩家自身背包的 36 个槽位（主背包 27 + 快捷栏 9），
 * 使技能装配界面中的背包区域完全等价于一个普通容器菜单的物品栏：
 * 可点选拾取、放置、Shift 快速移动、拖拽整理等，所有交互由菜单自身处理。
 *
 * <p>技能装配数据（SkillAssembly）仍存储在玩家附件中，由自定义 widget 负责展示与编辑。
 * 装配相关的物品移动（手持 ↔ 背包）由服务端在 {@link #handleAssemblyAction} 中权威处理，
 * 客户端仅发起动作请求并从同步的附件回读结果，避免物品复制或丢失。
 */
public class SkillAssemblyMenu extends AbstractPanelMenu {

    public SkillAssemblyMenu(int containerId, Inventory playerInventory) {
        // 复用基类背包 36 槽布局，仅坐标按本界面（180×158 背包窗口、左侧 9px 边距）定制
        super(MenuRegistry.SKILL_ASSEMBLY_MENU.get(), containerId, playerInventory, 9, 30, 84);
    }

    @Override
    public boolean stillValid(Player player) {
        // 菜单仅展示玩家自身背包，始终有效（玩家死亡时由游戏逻辑关闭界面）
        return player.isAlive();
    }

    /** 客户端读取技能装配数据（双端同步的权威来源，供 Screen 渲染 / 发包使用）。 */
    public SkillAssembly getAssembly() {
        return KeyBind.getAssembly(this.player);
    }

    /**
     * 服务端权威地处理技能装配的物品操作。
     *
     * @param action 见 {@link SkillAssemblyActionPacket} 中的常量
     * @param slot   装配槽索引
     * @param socket 辅助槽索引（不涉及时 -1；装辅助到整技能时 -1 = 首个空辅助槽）
     */
    public void handleAssemblyAction(int action, int slot, int socket, int fromSlot, int fromSocket) {
        // 越界防御：恶意客户端可发任意 slot，SkillAssembly.set 会无限扩充列表导致附件序列化失败
        if (slot < 0 || slot >= SkillAssembly.SIZE) return;
        SkillAssembly asm = KeyBind.getAssembly(this.player);
        boolean changed = false;

        switch (action) {
            case SkillAssemblyActionPacket.EQUIP_SKILL -> {
                ItemStack carried = this.getCarried();
                Optional<GemCategory> cat = GemCategory.categorize(carried);
                if (cat.isEmpty() || cat.get() == GemCategory.SUPPORT) return;
                SkillData sd = carried.get(DataComponentRegistry.SKILL_DATA);
                if (sd == null || sd.isEmpty()) return;
                SkillData old = asm.get(slot);
                if (!old.isEmpty()) {
                    // 槽位已有宝石：先退回背包，腾出位置（背包满则放弃并提示）
                    if (!this.player.getInventory().add(old.toItemStack())) {
                        this.player.sendSystemMessage(Component.translatable("msg.coe.inv_full"));
                        return;
                    }
                    unbindKeys(slot);
                }
                asm = asm.set(slot, sd).setAuraEnabled(slot, false);
                this.setCarried(ItemStack.EMPTY);
                changed = true;
            }
            case SkillAssemblyActionPacket.UNEQUIP_SKILL -> {
                SkillData sd = asm.get(slot);
                if (sd.isEmpty()) return;
                // 先把镶嵌的辅助宝石逐颗退回背包（背包满则掉落在地）
                for (SupportData sp : sd.supports()) {
                    if (sp != null && !sp.isEmpty()) {
                        ItemStack sup = sp.toItemStack();
                        if (!this.player.getInventory().add(sup)) {
                            this.player.drop(sup, false);
                        }
                    }
                }
                // 技能石本体去掉辅助后退回（手持或背包，背包满则掉落）
                SkillData bare = SkillData.builder(sd.id())
                        .status(sd.status()).level(sd.level()).quality(sd.quality())
                        .sockets(sd.sockets()).spirit(sd.spirit()).build();
                ItemStack item = bare.toItemStack();
                ItemStack carried = this.getCarried();
                if (carried.isEmpty()) {
                    this.setCarried(item);
                } else if (!this.player.getInventory().add(item)) {
                    this.player.drop(item, false);
                }
                asm = asm.set(slot, SkillData.EMPTY);
                unbindKeys(slot);
                changed = true;
            }
            case SkillAssemblyActionPacket.EQUIP_SUPPORT -> {
                ItemStack carried = this.getCarried();
                Optional<GemCategory> cat = GemCategory.categorize(carried);
                if (cat.isEmpty() || cat.get() != GemCategory.SUPPORT) return;
                SupportData spd = carried.get(DataComponentRegistry.SUPPORT_DATA);
                if (spd == null || spd.isEmpty()) return;
                SkillData sd = asm.get(slot);
                if (sd.isEmpty()) return;
                int socketCount = sd.getSockets();
                int target = resolveSupportSocket(sd, socketCount, socket);
                if (target < 0) return;
                List<SupportData> supports = sd.supports();
                SupportData cur = supports.size() > target ? supports.get(target) : SupportData.EMPTY;
                if (!cur.isEmpty() && !this.player.getInventory().add(cur.toItemStack())) return;
                asm = asm.setSupport(slot, target, spd);
                carried.shrink(1);
                this.setCarried(carried);
                changed = true;
            }
            case SkillAssemblyActionPacket.REMOVE_SUPPORT -> {
                SkillData sd = asm.get(slot);
                if (sd.isEmpty() || socket < 0 || socket >= sd.getSockets()) return;
                List<SupportData> supports = sd.supports();
                SupportData cur = supports.size() > socket ? supports.get(socket) : SupportData.EMPTY;
                if (cur.isEmpty()) return;
                if (!this.player.getInventory().add(cur.toItemStack())) return;
                asm = asm.clearSupport(slot, socket);
                changed = true;
            }
            case SkillAssemblyActionPacket.TOGGLE_AURA -> {
                SkillData sd = asm.get(slot);
                if (sd.isEmpty() || !sd.isSpirit()) return;
                asm = asm.setAuraEnabled(slot, !asm.isAuraEnabled(slot));
                changed = true;
            }
            // 主技能石槽位间交换（辅助随技能走；光环开关按槽位保留）。fromSlot 越界防御。
            case SkillAssemblyActionPacket.MOVE_SKILL -> {
                if (fromSlot < 0 || fromSlot >= SkillAssembly.SIZE) return;
                if (slot < 0 || slot >= SkillAssembly.SIZE) return;
                SkillData a = asm.get(fromSlot);
                SkillData b = asm.get(slot);
                asm = asm.set(fromSlot, b).set(slot, a);
                changed = true;
            }
            // 辅助宝石槽位间交换（可跨技能）。兼容性由装配期/运行期统一求解，
            // 此处信任服务端权威写入（与 EQUIP_SUPPORT 一致，不做客户端兼容性回退）。
            case SkillAssemblyActionPacket.MOVE_SUPPORT -> {
                if (fromSlot < 0 || fromSlot >= SkillAssembly.SIZE) return;
                if (slot < 0 || slot >= SkillAssembly.SIZE) return;
                SkillData fromSd = asm.get(fromSlot);
                SkillData toSd = asm.get(slot);
                if (fromSd.isEmpty() || toSd.isEmpty()) return;
                if (fromSocket < 0 || fromSocket >= fromSd.getSockets()) return;
                if (socket < 0 || socket >= toSd.getSockets()) return;
                List<SupportData> fromSupports = fromSd.supports();
                List<SupportData> toSupports = toSd.supports();
                SupportData moving = fromSocket < fromSupports.size() ? fromSupports.get(fromSocket) : SupportData.EMPTY;
                SupportData target = socket < toSupports.size() ? toSupports.get(socket) : SupportData.EMPTY;
                if (moving.isEmpty() && target.isEmpty()) return;
                asm = asm.setSupport(fromSlot, fromSocket, target).setSupport(slot, socket, moving);
                changed = true;
            }
            default -> {
                return;
            }
        }

        if (changed) {
            this.player.setData(AttachmentRegistry.SKILL_ASSEMBLY, asm);
            this.broadcastChanges();
        }
    }

    /** 解析辅助宝石目标插槽：socket<0 时返回首个空插槽，否则返回原值（越界则 -1）。 */
    private int resolveSupportSocket(SkillData sd, int socketCount, int socket) {
        if (socket >= 0) {
            return socket < socketCount ? socket : -1;
        }
        List<SupportData> supports = sd.supports();
        for (int j = 0; j < socketCount; j++) {
            SupportData cur = j < supports.size() ? supports.get(j) : SupportData.EMPTY;
            if (cur.isEmpty()) return j;
        }
        return -1;
    }

    /** 解绑所有指向指定装配槽的按键槽（清空/替换装配槽时调用）。 */
    private void unbindKeys(int asmIdx) {
        KeyBind.SkillSave save = this.player.hasData(AttachmentRegistry.SKILL_SAVE)
                ? this.player.getData(AttachmentRegistry.SKILL_SAVE)
                : KeyBind.SkillSave.DEFAULT;
        boolean changed = false;
        for (int k = 0; k < save.save().size(); k++) {
            if (save.getData(k).assemblySlot().getInt() == asmIdx) {
                save = save.unbind(k);
                SkillCaster.of(this.player).setCooldown(k, 0);
                changed = true;
            }
        }
        if (changed) this.player.setData(AttachmentRegistry.SKILL_SAVE, save);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack original = slot.getItem().copy();
        ItemStack remaining = original.copy();
        // 主背包(0-26) ↔ 快捷栏(27-35) 互移
        int start = index < 27 ? 27 : 0;
        int end = index < 27 ? 36 : 27;
        // 先合并到同类已有堆叠
        for (int i = start; i < end; i++) {
            Slot target = this.slots.get(i);
            ItemStack targetStack = target.getItem();
            if (!targetStack.isEmpty() && ItemStack.isSameItemSameComponents(targetStack, remaining)
                    && targetStack.getCount() < targetStack.getMaxStackSize()) {
                int canAdd = targetStack.getMaxStackSize() - targetStack.getCount();
                int move = Math.min(canAdd, remaining.getCount());
                targetStack.grow(move);
                remaining.shrink(move);
                target.setByPlayer(targetStack);
                target.setChanged();
                if (remaining.isEmpty()) break;
            }
        }
        // 再移入空槽
        if (!remaining.isEmpty()) {
            for (int i = start; i < end; i++) {
                Slot target = this.slots.get(i);
                if (target.getItem().isEmpty() && target.mayPlace(remaining)) {
                    int move = Math.min(remaining.getCount(), remaining.getMaxStackSize());
                    target.setByPlayer(remaining.split(move));
                    target.setChanged();
                    if (remaining.isEmpty()) break;
                }
            }
        }
        slot.setByPlayer(remaining.isEmpty() ? ItemStack.EMPTY : remaining);
        slot.setChanged();
        return original;
    }
}
