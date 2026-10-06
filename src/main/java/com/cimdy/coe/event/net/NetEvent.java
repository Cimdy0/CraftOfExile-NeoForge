package com.cimdy.coe.event.net;

import com.cimdy.coe.data.codec.stronghold.task.helper.dispatch.TaskDispatcher;
import com.cimdy.coe.data.codec.talent.data.TalentWeaponSet;
import com.cimdy.coe.network.sync.ProgressionSyncSender;
import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.block.entity.ResearchBenchBlockEntity;
import com.cimdy.coe.block.entity.TerminalBlockEntity;
import com.cimdy.coe.client.animation.DodgeRollAnimation;
import com.cimdy.coe.client.render.GroundFieldRenderer;
import com.cimdy.coe.client.animation.SkillCastAnimation;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.data.codec.progression.PlayerProgress;
import com.cimdy.coe.data.codec.progression.ProgressionData;
import com.cimdy.coe.data.codec.progression.ProgressionService;
import com.cimdy.coe.data.codec.skill.core.Skill;
import com.cimdy.coe.data.codec.skill.core.SkillCaster;
import com.cimdy.coe.data.codec.skill.gem.KeyBind;
import com.cimdy.coe.data.codec.stronghold.Stronghold;
import com.cimdy.coe.data.codec.stronghold.task.work.WorkTypeRegistry;
import com.cimdy.coe.data.codec.support.addition.AdditionData;
import com.cimdy.coe.data.codec.support.addition.AdditionKeys;
import com.cimdy.coe.data.codec.support.addition.GoldState;
import com.cimdy.coe.data.codec.talent.data.*;
import com.cimdy.coe.data.codec.talent.effect.*;
import com.cimdy.coe.data.codec.talent.node.*;
import com.cimdy.coe.data.codec.talent.respec.TalentRespecCost;
import com.cimdy.coe.data.codec.talent.tree.*;
import com.cimdy.coe.data.helper.skill.PlayerCastHelper;
import com.cimdy.coe.helper.DodgeRollHelper;
import com.cimdy.coe.helper.PlayerReach;
import com.cimdy.coe.stronghold.energy.StrongholdHelper;
import com.cimdy.coe.stronghold.area.StrongholdRepository;
import com.cimdy.coe.item.charm.CharmEngine;
import com.cimdy.coe.item.flask.FlaskHelper;
import com.cimdy.coe.network.entity.*;
import com.cimdy.coe.network.input.*;
import com.cimdy.coe.network.menu.*;
import com.cimdy.coe.network.research.*;
import com.cimdy.coe.network.skill.*;
import com.cimdy.coe.inventory.BackpackCarried;
import com.cimdy.coe.network.inventory.TakePutAckPacket;
import com.cimdy.coe.network.inventory.TakePutPacket;
import com.cimdy.coe.ui.widget.compose.inventory.InventoryFloatWindow;
import com.cimdy.coe.ui.screen.gem.CutScreen;
import com.cimdy.coe.ui.screen.talent.AbstractTalentTreeScreen;
import com.cimdy.coe.network.stronghold.*;
import com.cimdy.coe.network.sync.*;
import com.cimdy.coe.ui.menu.SkillAssemblyMenu;
import com.cimdy.coe.ui.menu.TalentMenu;
import com.cimdy.coe.ui.menu.TestPanelMenu;
import com.cimdy.coe.ui.menu.ExtraSlotsMenu;
import com.cimdy.coe.event.data.LoadEvent;
import com.cimdy.coe.ui.screen.progression.ProgressionScreens;
import com.cimdy.coe.ui.screen.talent.TalentAllocator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.HandlerThread;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = CraftOfExile.MODID)
public class NetEvent {
    @SubscribeEvent
    public static void registerServer(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(CraftOfExile.MODID + "1.0");
        registrar.executesOn(HandlerThread.MAIN).playBidirectional(
                MobEffectPacket.TYPE, MobEffectPacket.STREAM_CODEC, (mobEffect, context) -> {
                    Player player = context.player();
                    if (mobEffect.uuid().isPresent() &&
                            player.level().getEntity(mobEffect.uuid().get()) instanceof LivingEntity living) {
                        PacketDistributor.sendToPlayer((ServerPlayer) player,
                                new MobEffectPacket(Optional.of(living.getUUID()),
                                        Optional.of(living.getActiveEffects().stream().toList())));
                    }
                });

        registrar.executesOn(HandlerThread.MAIN).playBidirectional(
                KeyPressPacket.TYPE, ByteBufCodecs.fromCodec(KeyPressPacket.CODEC), (press, context) -> {
                    Player player = context.player();
                    PlayerCastHelper.playerCast(player, press);

                    if (press.released()) {
                        Skill.stopUseSkill(player);
                        press = press.setReleased(false);
                    }

                    player.setData(AttachmentRegistry.KEY_PRESS, press);
                });

        // Flask 使用（客户端 → 服务端）：按键触发。slotIndex 0/1 = 指定槽，-1 = 全部使用。
        registrar.executesOn(HandlerThread.MAIN).playBidirectional(
                FlaskUsePacket.TYPE, FlaskUsePacket.STREAM_CODEC, (use, context) -> {
                    Player player = context.player();
                    if (use.slotIndex() < 0) FlaskHelper.useAll(player);
                    else FlaskHelper.useSlot(player, use.slotIndex());
                });

        // 施放动画通知（服务端 → 客户端）：施放成功播动画 / 打断停止。仅客户端方向，处理器只客户端执行。
        registrar.executesOn(HandlerThread.MAIN).playToClient(
                CastAnimationPacket.TYPE, CastAnimationPacket.STREAM_CODEC, (cast, context) ->
                        SkillCastAnimation.onCast(cast.skillId(), cast.animation(), cast.castTimeTicks(), cast.startTime()));

        // 翻滚发起（客户端 → 服务端）：校验冷却 / 禁用标记后发起翻滚，并广播翻滚动画给同维度玩家。
        registrar.executesOn(HandlerThread.MAIN).playBidirectional(
                DodgeRollPacket.TYPE, DodgeRollPacket.STREAM_CODEC, (packet, context) -> {
                    Player player = context.player();
                    if (!(player instanceof ServerPlayer sp)) return;
                    int result = DodgeRollHelper.tryDodge(sp, packet.moveX(), packet.moveZ(), packet.flip());
                    if (result != DodgeRollHelper.DODGE_FAIL) {
                        int kind = result == DodgeRollHelper.DODGE_FLIP
                                ? DodgeRollAnimPacket.KIND_FLIP : DodgeRollAnimPacket.KIND_ROLL;
                        PacketDistributor.sendToPlayersInDimension(sp.level(),
                                new DodgeRollAnimPacket(sp.getUUID(), kind, DodgeRollHelper.ROLL_TICKS));
                    }
                });

        // 翻滚动画通知（服务端 → 客户端）：在指定 UUID 玩家实体上播放翻滚 / 调整动作动画（本人/他者通用）。
        registrar.executesOn(HandlerThread.MAIN).playToClient(
                DodgeRollAnimPacket.TYPE, DodgeRollAnimPacket.STREAM_CODEC, (packet, context) ->
                        DodgeRollAnimation.onRoll(packet.playerUuid(), packet.kind(), packet.rollTicks()));

        // 地面场全量同步（服务端 → 客户端）：周期性把各维度活动地面场推给该维度玩家渲染。
        registrar.executesOn(HandlerThread.MAIN).playToClient(
                GroundFieldSyncPacket.TYPE, GroundFieldSyncPacket.STREAM_CODEC, (sync, context) ->
                        GroundFieldRenderer.onSync(sync.fields()));

        registrar.executesOn(HandlerThread.MAIN).playBidirectional(
                PosPacket.TYPE, PosPacket.STREAM_CODEC, (pos, context) -> {
                    Player player = context.player();
                    pos.set(player.level());
                });

        registrar.executesOn(HandlerThread.MAIN).playBidirectional(
                SpawnEntityPacket.TYPE, SpawnEntityPacket.STREAM_CODEC, (spawnEntity, context) -> {
                    Player player = context.player();
                    ServerLevel serverLevel = (ServerLevel) player.level();
                    // 盟友（随行伙伴）放出后同样进据点，但 TaskDispatcher#canWork 会把它排除在派工之外
                    // （已派任务自动回退 PENDING），标记本身由 spawnPlayerEntity 搬到实体附件上，回收时再搬回存档。
                    StrongholdHelper.spawnPlayerEntity(spawnEntity.data(), serverLevel, player, spawnEntity.pos());
                });

        // 盟友标记切换（客户端 → 服务端）：据点 UI 右键格子触发，服务端改完由附件同步回客户端刷新。
        registrar.executesOn(HandlerThread.MAIN).playBidirectional(
                ToggleAllyPacket.TYPE, ToggleAllyPacket.STREAM_CODEC, (toggleAlly, context) -> {
                    Player player = context.player();
                    if (!(player instanceof ServerPlayer serverPlayer)) return;
                    // 防作弊：必须在同维度、够得着的终端上操作
                    if (!PlayerReach.canReach(serverPlayer, toggleAlly.pos())) return;
                    // 归属校验下沉在 toggleAlly 内部（已部署分支才校验据点所有权，已存分支只动自身存档）
                    if (!StrongholdRepository.toggleAlly((ServerLevel) player.level(), player,
                            toggleAlly.pos(), toggleAlly.uuid())) {
                        serverPlayer.sendOverlayMessage(Component.translatable("hold.coe.site.not_owner")
                                .withStyle(net.minecraft.ChatFormatting.RED));
                    }
                });

        // 战斗开关切换（客户端 → 服务端）：据点 UI 中键格子触发，权限模型与 toggleAlly 完全一致。
        registrar.executesOn(HandlerThread.MAIN).playBidirectional(
                ToggleCombatantPacket.TYPE, ToggleCombatantPacket.STREAM_CODEC, (toggleCombatant, context) -> {
                    Player player = context.player();
                    if (!(player instanceof ServerPlayer serverPlayer)) return;
                    if (!PlayerReach.canReach(serverPlayer, toggleCombatant.pos())) return;
                    if (!StrongholdRepository.toggleCombatant((ServerLevel) player.level(), player,
                            toggleCombatant.pos(), toggleCombatant.uuid())) {
                        serverPlayer.sendOverlayMessage(Component.translatable("hold.coe.site.not_owner")
                                .withStyle(net.minecraft.ChatFormatting.RED));
                    }
                });

        registrar.executesOn(HandlerThread.MAIN).playBidirectional(
                RecycleEntityPacket.TYPE, RecycleEntityPacket.STREAM_CODEC, (recycleEntity, context) -> {
                    Player player = context.player();
                    ServerLevel serverLevel = (ServerLevel) player.level();
                    if (serverLevel.getEntity(recycleEntity.uuid()) instanceof LivingEntity living) {
                        StrongholdHelper.transferEntity(player, recycleEntity.pos(), living);
                    }
                });

        registrar.executesOn(HandlerThread.MAIN).playBidirectional(
                StockSettingPacket.TYPE, StockSettingPacket.STREAM_CODEC, (stockSetting, context) -> {
                    Player player = context.player();
                    ServerLevel serverLevel = (ServerLevel) player.level();
                    if (serverLevel.getBlockEntity(stockSetting.pos()) instanceof BlockEntity be) {
                        be.setData(AttachmentRegistry.STOCK_SET, stockSetting.stockSet());
                    }
                });

        registrar.executesOn(HandlerThread.MAIN).playBidirectional(
                BindSite.TYPE, BindSite.STREAM_CODEC, (bindSite, context) -> {
                    Player player = context.player();
                    if (!(player instanceof ServerPlayer serverPlayer)) return;
                    MinecraftServer server = serverPlayer.level().getServer();
                    GlobalPos tPos = bindSite.terminalPos();
                    ServerLevel serverLevel = server.getLevel(tPos.dimension());
                    if (serverLevel == null) return;
                    // 防作弊：必须在同维度、够得着的终端上操作，禁止隔空/跨维改动据点
                    if (!tPos.dimension().equals(serverPlayer.level().dimension())) return;
                    if (!PlayerReach.canReach(serverPlayer, tPos.pos())) return;
                    if (!(serverLevel.getBlockEntity(tPos.pos()) instanceof TerminalBlockEntity terminal)) return;
                    // 归属校验：只有据点所有者能改动站点
                    if (!StrongholdRepository.getHoldData(serverLevel).isOwnerAtPos(serverPlayer, tPos)) {
                        serverPlayer.sendOverlayMessage(Component.translatable("hold.coe.site.not_owner")
                                .withStyle(net.minecraft.ChatFormatting.RED));
                        return;
                    }
                    Stronghold hold = terminal.getData(AttachmentRegistry.STRONGHOLD);
                    Stronghold newHold;
                    if (bindSite.remove()) {
                        newHold = hold.withoutSite(bindSite.site().getSitePos());
                    } else {
                        // P4 进度门控：工种未授权 / 站点数超上限则拒绝绑定（服务端权威，
                        // 客户端的灰显只是提示，不能作为唯一防线）
                        if (!WorkTypeRegistry.isUnlocked(serverPlayer, bindSite.site().getType())) {
                            serverPlayer.sendOverlayMessage(Component.translatable("hold.coe.site.locked",
                                    Component.translatable("work_type.coe."
                                            + WorkTypeRegistry.workTypeName(bindSite.site().getType())))
                                    .withStyle(net.minecraft.ChatFormatting.RED));
                            return;
                        }
                        // 站点上限按据点所有者进度计算（共享据点不能被低级玩家卡死）
                        int cap = ProgressionSyncSender.ownerSiteCap(serverLevel, tPos);
                        if (hold.holdState().sites().size() >= cap
                                && hold.getSite(bindSite.site().getSitePos()).isEmpty()) {
                            serverPlayer.sendOverlayMessage(Component.translatable("hold.coe.site.cap",
                                    hold.holdState().sites().size(), cap)
                                    .withStyle(net.minecraft.ChatFormatting.RED));
                            return;
                        }
                        newHold = hold.withSite(bindSite.site());
                    }
                    if (newHold != hold) {
                        terminal.setData(AttachmentRegistry.STRONGHOLD, newHold);
                    }
                }
        );

        registrar.playBidirectional(
                SkillSetupPacket.TYPE, SkillSetupPacket.STREAM_CODEC, (setup, context) -> {
                    Player player = context.player();
                    // 按键绑定：仅合并装配槽映射；冷却统一存于 SKILL_COOLDOWN（见 SkillCaster）
                    KeyBind.SkillSave current = player.hasData(AttachmentRegistry.SKILL_SAVE)
                            ? player.getData(AttachmentRegistry.SKILL_SAVE)
                            : KeyBind.SkillSave.DEFAULT;
                    KeyBind.SkillSave merged = current.mergeSetup(setup.skillSave());
                    player.setData(AttachmentRegistry.SKILL_SAVE, merged);
                    // 装配槽发生变化的按键槽，其冷却应清零（冷却已不在 SkillSave 内）
                    for (int i = 0; i < merged.save().size(); i++) {
                        int oldSlot = current.getData(i).assemblySlot().getInt();
                        int newSlot = merged.getData(i).assemblySlot().getInt();
                        if (oldSlot != newSlot) {
                            SkillCaster.of(player).setCooldown(i, 0);
                        }
                    }
                });

        registrar.executesOn(HandlerThread.MAIN).playBidirectional(
                SkillAssemblyActionPacket.TYPE, SkillAssemblyActionPacket.STREAM_CODEC,
                (action, context) -> {
                    Player player = context.player();
                    // 物品移动必须在服务端权威执行：读取玩家当前打开的装配菜单，操作手持/背包
                    if (player.containerMenu instanceof SkillAssemblyMenu menu) {
                        menu.handleAssemblyAction(action.action(), action.slot(), action.socket(),
                                action.fromSlot(), action.fromSocket());
                    }
                });

        registrar.playBidirectional(
                OpenSkillMenuPacket.TYPE, OpenSkillMenuPacket.STREAM_CODEC, (packet, context) -> {
                    Player player = context.player();
                    if (player instanceof ServerPlayer serverPlayer) {
                        serverPlayer.openMenu(new MenuProvider() {
                            @Override
                            public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player p) {
                                return new SkillAssemblyMenu(containerId, inventory);
                            }

                            @Override
                            public Component getDisplayName() {
                                return Component.translatable("ui.coe.skill_assembly");
                            }
                        });
                    }
                });

        // 测试面板：服务端 openMenu（基类链路验证——Menu 操控物品 + 内嵌背包）
        registrar.playBidirectional(
                OpenTestPanelPacket.TYPE, OpenTestPanelPacket.STREAM_CODEC, (packet, context) -> {
                    Player player = context.player();
                    if (player instanceof ServerPlayer serverPlayer) {
                        serverPlayer.openMenu(new MenuProvider() {
                            @Override
                            public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player p) {
                                return new TestPanelMenu(containerId, inventory);
                            }

                            @Override
                            public Component getDisplayName() {
                                return Component.translatable("ui.coe.panel_test");
                            }
                        });
                    }
                });

        // 药剂 / 护符 / 饰品 统一面板：服务端 openMenu（单按钮点开的窗口，集中渲染三类槽位）
        registrar.playBidirectional(
                OpenExtraSlotsMenuPacket.TYPE, OpenExtraSlotsMenuPacket.STREAM_CODEC, (packet, context) -> {
                    Player player = context.player();
                    if (player instanceof ServerPlayer serverPlayer) {
                        // 面板依赖槽位定义：打开前先推一次，确保客户端（专用服务器）拿到最新表
                        SlotSyncSender.syncSlots(serverPlayer);
                        serverPlayer.openMenu(new MenuProvider() {
                            @Override
                            public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player p) {
                                return new ExtraSlotsMenu(containerId, inventory);
                            }

                            @Override
                            public Component getDisplayName() {
                                return Component.translatable("ui.coe.flask_charm_trinket");
                            }
                        });
                    }
                });

        registrar.playBidirectional(
                TeleportRequestPacket.TYPE, TeleportRequestPacket.STREAM_CODEC, (teleportRequest, context) -> {
                    Player player = context.player();
                    ServerLevel serverLevel = (ServerLevel) player.level();
                    GlobalPos pos = teleportRequest.targetPos();
                    // 检查能量并扣除（距离相关计费：源终端 → 目标点）
                    double amount = StrongholdHelper.getEnergyCost(
                            new GlobalPos(serverLevel.dimension(), teleportRequest.terminalPos()),
                            teleportRequest.targetPos());
                    if (StrongholdHelper.reduceEnderEnergy(serverLevel, teleportRequest.terminalPos(), amount)) {
                        // 传送
                        ServerLevel targetLevel = serverLevel.getServer().getLevel(pos.dimension());
                        if (targetLevel == null) return;
                        Vec3 targetVec = new Vec3(
                                pos.pos().getX() + 0.5,
                                pos.pos().getY() + 1.0,
                                pos.pos().getZ() + 0.5);
                        TeleportTransition transition = new TeleportTransition(targetLevel, targetVec,
                                Vec3.ZERO, player.getYRot(), player.getXRot(), TeleportTransition.DO_NOTHING);
                        player.teleport(transition);
                    }
                }
        );

        // 背包浮窗取放（客户端 → 服务端）：服务端权威执行拾放，回包镜像本地背包与手持。
        // 非容器屏不走 Menu：玩家打开任意 WidgetScreen 时 B 键浮窗内的点击均由此处理，
        // 服务端改 PlayerInventory + 回包（客户端按服务端权威值写本地镜像，后续 Menu 同步不冲突）。
        registrar.executesOn(HandlerThread.MAIN).playToServer(
                TakePutPacket.TYPE, TakePutPacket.STREAM_CODEC, (take, context) -> {
                    Player player = context.player();
                    if (!(player instanceof ServerPlayer sp)) return;
                    ItemStack carried = BackpackCarried.handleTakePut(sp, take.slotIndex(), take.button());
                    ItemStack inSlot = sp.getInventory().getItem(take.slotIndex());
                    PacketDistributor.sendToPlayer(sp, new TakePutAckPacket(take.slotIndex(), inSlot, carried));
                });

        registrar.playBidirectional(
                CutGemPacket.TYPE, CutGemPacket.STREAM_CODEC, (cut, context) -> {
                    Player player = context.player();
                    if (!(player instanceof ServerPlayer sp)) return;
                    // 切割界面由玩家背包容器中的未切割宝石右键打开：仅当当前容器即玩家背包时才合法，
                    // 否则 slotIndex 指向其它容器槽位，误当背包索引会覆盖无关物品。
                    if (!(player.containerMenu instanceof net.minecraft.world.inventory.InventoryMenu)) return;
                    net.minecraft.world.entity.player.Inventory inv = player.getInventory();
                    int idx = cut.slotIndex();
                    if (idx < 0 || idx >= inv.getContainerSize()) return;
                    ItemStack current = inv.getItem(idx);
                    // 仅当该槽位当前为未切割宝石（UncutGem）时才执行切割，避免覆盖无关物品
                    if (!(current.getItem() instanceof com.cimdy.coe.item.gem.UncutGem uncut)) return;
                    // 防作弊：目标宝石由服务端按「原槽未切割宝石的类别 + 等阶」从可切割列表按 id 反查重建，
                    // 不信任客户端传来的任何物品数据（客户端只提供目标宝石 id）。产物等级 = 等阶（等级灌输已内置）。
                    int tier = com.cimdy.coe.item.gem.UncutGem.getTier(current);
                    ItemStack rebuilt = com.cimdy.coe.data.codec.skill.gem.GemCategory
                            .byKey(uncut.getCutType()).buildCutList(tier).stream()
                            .filter(s -> com.cimdy.coe.data.codec.skill.gem.GemCategory.extractGemId(s) != null
                                    && com.cimdy.coe.data.codec.skill.gem.GemCategory.extractGemId(s).equals(cut.gemId()))
                            .findFirst().orElse(null);
                    if (rebuilt == null) return;
                    // 切割 100% 确定（用户"别失败"；与 POE2 参考一致）。产物 = 服务端重建的带等级宝石
                    inv.setItem(idx, rebuilt.copy());
                    sp.containerMenu.broadcastChanges();
                    PacketDistributor.sendToPlayer(sp,
                            new CutResultPacket(true, idx, rebuilt.copy(), ""));
                }
        );

        registrar.playBidirectional(
                SiteSetting.TYPE, SiteSetting.STREAM_CODEC, (siteSetting, context) -> {
                    Player player = context.player();
                    ServerLevel serverLevel = (ServerLevel) player.level();
                    if (serverLevel.getBlockEntity(siteSetting.terminalPos()) instanceof TerminalBlockEntity terminal) {
                        Stronghold hold = terminal.getData(AttachmentRegistry.STRONGHOLD);
                        Stronghold newHold = hold.updateSite(siteSetting.sitePos(), site -> {
                            site.setPriority(siteSetting.priority());
                            // 仅当站点允许设置自动派遣时才应用，避免越权修改（如合成站）
                            if (site.canSetAssign()) site.setAutoAssign(siteSetting.autoAssign());
                            site.setMaxWorkers(siteSetting.maxWorkers());
                            return site;
                        });
                        terminal.setData(AttachmentRegistry.STRONGHOLD, newHold);
                    }
                }
        );

        registrar.playBidirectional(
                TalentActionPacket.TYPE, TalentActionPacket.STREAM_CODEC, (action, context) -> {
                    Player player = context.player();
                    var model = TalentTreeModelLoader.get(action.treeId());
                    TalentData td = player.hasData(AttachmentRegistry.TALENT_TREE)
                            ? player.getData(AttachmentRegistry.TALENT_TREE) : TalentData.EMPTY;
                    TalentState cur = td.get(action.treeId()).state();
                    TalentAllocator alloc = TalentAllocator.create(model, action.treeId(), cur, player);
                    switch (action.action()) {
                        case TalentActionPacket.ALLOCATE -> {
                            // 本次分配绑定到客户端传入的武器组（当前激活组），确保「在查看某组时加点即归属该组」
                            alloc.setActiveSet(action.weaponSet());
                            if (alloc.canAllocate(action.nodeId())) {
                                alloc.preview(action.nodeId(), true);
                                if (action.choice() >= 0) alloc.setChoice(action.nodeId(), action.choice());
                            }
                        }
                        case TalentActionPacket.DEALLOCATE -> {
                            if (alloc.isAllocated(action.nodeId())
                                    && !model.startNodeIds().contains(action.nodeId())) {
                                alloc.preview(action.nodeId(), false);
                            }
                        }
                        // 整树清空（洗点）不再走本包：POE2 语义下重置必须按等级单价付金币，
                        // 统一由 TalentCommitPacket 的 RESET 模式提交（见下方注册）。
                        default -> { }
                    }
                    TalentState next = new TalentState(alloc.allocations(), model.version());
                    player.setData(AttachmentRegistry.TALENT_TREE,
                            td.with(action.treeId(), new TalentTreeData(next)));
                    TalentEffectApplier.apply(player);
                });

        // 天赋提交保存（客户端 → 服务端）：「加点集合提交」或「金币重置」。
        // 服务端权威：节点存在性 / 连通性 / 点数上限 / 等级单价 / 金币扣减全部在此重算，
        // 客户端提交的一切数值都只作期望值校验，失败时存档与金币一律不动。
        registrar.executesOn(HandlerThread.MAIN).playToServer(
                TalentCommitPacket.TYPE, TalentCommitPacket.STREAM_CODEC, (commit, context) -> {
                    Player player = context.player();
                    if (!(player instanceof ServerPlayer serverPlayer)) return;
                    var model = TalentTreeModelLoader.get(commit.treeId());
                    TalentData td = player.hasData(AttachmentRegistry.TALENT_TREE)
                            ? player.getData(AttachmentRegistry.TALENT_TREE) : TalentData.EMPTY;
                    TalentState cur = td.get(commit.treeId()).state();
                    switch (commit.mode()) {
                        case TalentCommitPacket.ALLOCATE -> {
                            // 1) 去重 + 节点存在性 + 总点数上限
                            Set<String> wanted = new LinkedHashSet<>();
                            Map<String, TalentCommitPacket.Entry> byId = new LinkedHashMap<>();
                            for (TalentCommitPacket.Entry e : commit.entries()) {
                                if (e == null || e.nodeId() == null || e.nodeId().isEmpty()) continue;
                                if (model.node(e.nodeId()) == null) {
                                    PacketDistributor.sendToPlayer(serverPlayer, TalentCommitResultPacket.fail(
                                            commit.treeId(), commit.mode(),
                                            TalentCommitResultPacket.REASON_UNKNOWN_NODE, 0L, 0));
                                    return;
                                }
                                if (byId.putIfAbsent(e.nodeId(), e) == null) wanted.add(e.nodeId());
                            }
                            if (wanted.size() > model.totalPoints()) {
                                PacketDistributor.sendToPlayer(serverPlayer, TalentCommitResultPacket.fail(
                                        commit.treeId(), commit.mode(),
                                        TalentCommitResultPacket.REASON_TOO_MANY_POINTS, 0L, wanted.size()));
                                return;
                            }
                            // 2) 以服务端存档为基准重放：先退掉本次未提交的节点（插槽珠宝退回背包，避免丢物）
                            TalentAllocator alloc = TalentAllocator.create(model, commit.treeId(), cur, player);
                            Inventory inv = player.getInventory();
                            boolean itemsMoved = false;
                            for (TalentAllocation a : cur.allocated()) {
                                if (wanted.contains(a.id())) continue;
                                ItemStack jewel = alloc.socketOf(a.id());
                                alloc.preview(a.id(), false);
                                if (!jewel.isEmpty()) {
                                    if (!inv.add(jewel.copy())) player.drop(jewel.copy(), false);
                                    itemsMoved = true;
                                }
                            }
                            // 3) 多轮扫描补齐新增 / 换武器组 / 改选：不依赖客户端的提交顺序
                            List<TalentCommitPacket.Entry> pending = new ArrayList<>(byId.values());
                            boolean progressed = true;
                            while (progressed && !pending.isEmpty()) {
                                progressed = false;
                                Iterator<TalentCommitPacket.Entry> it = pending.iterator();
                                while (it.hasNext()) {
                                    TalentCommitPacket.Entry e = it.next();
                                    alloc.setActiveSet(Math.max(0, Math.min(TalentWeaponSet.MAX_BINDING, e.weaponSet())));
                                    if (!alloc.isAllocated(e.nodeId()) && !alloc.canAllocate(e.nodeId())) continue;
                                    alloc.preview(e.nodeId(), true);
                                    TalentNodeDef def = model.node(e.nodeId());
                                    if (def != null && def.hasChoice()
                                            && e.choice() >= 0 && e.choice() < def.choices().size()) {
                                        alloc.setChoice(e.nodeId(), e.choice());
                                    }
                                    it.remove();
                                    progressed = true;
                                }
                            }
                            if (!pending.isEmpty()) {
                                // 存在连不通的节点：整包拒绝，存档与金币一律不动
                                PacketDistributor.sendToPlayer(serverPlayer, TalentCommitResultPacket.fail(
                                        commit.treeId(), commit.mode(),
                                        TalentCommitResultPacket.REASON_UNREACHABLE_NODE, 0L, wanted.size()));
                                return;
                            }
                            TalentState next = new TalentState(alloc.allocations(), model.version());
                            player.setData(AttachmentRegistry.TALENT_TREE,
                                    td.with(commit.treeId(), new TalentTreeData(next)));
                            TalentEffectApplier.apply(player);
                            if (itemsMoved) serverPlayer.containerMenu.broadcastChanges();
                            PacketDistributor.sendToPlayer(serverPlayer, TalentCommitResultPacket.ok(
                                    commit.treeId(), commit.mode(), 0L, alloc.allocatedCount()));
                        }
                        case TalentCommitPacket.RESET -> {
                            int points = cur.allocated().size();
                            long cost = TalentRespecCost.total(PlayerProgress.get(player).level(), points);
                            // 金币不足：不扣费、不清空，直接回执失败（客户端本地预览保持不变）
                            if (!GoldState.pay(player, cost)) {
                                PacketDistributor.sendToPlayer(serverPlayer, TalentCommitResultPacket.fail(
                                        commit.treeId(), commit.mode(),
                                        TalentCommitResultPacket.REASON_NOT_ENOUGH_GOLD, cost, points));
                                return;
                            }
                            Inventory inv = player.getInventory();
                            for (TalentAllocation a : cur.allocated()) {
                                if (!a.hasSocket()) continue;
                                ItemStack jewel = a.socket().copy();
                                if (!inv.add(jewel)) player.drop(jewel, false);
                            }
                            player.setData(AttachmentRegistry.TALENT_TREE,
                                    td.with(commit.treeId(), new TalentTreeData(new TalentState(List.of(), model.version()))));
                            TalentEffectApplier.apply(player);
                            serverPlayer.containerMenu.broadcastChanges();
                            PacketDistributor.sendToPlayer(serverPlayer, TalentCommitResultPacket.ok(
                                    commit.treeId(), commit.mode(), cost, points));
                        }
                        default -> PacketDistributor.sendToPlayer(serverPlayer, TalentCommitResultPacket.fail(
                                commit.treeId(), commit.mode(), TalentCommitResultPacket.REASON_BAD_MODE, 0L, 0));
                    }
                });

        // 提交结果回执（服务端 → 客户端）：仅登记类型，客户端处理由天赋树 UI 在 registerClient 接入。
        registrar.executesOn(HandlerThread.MAIN).playToClient(
                TalentCommitResultPacket.TYPE, TalentCommitResultPacket.STREAM_CODEC);

        registrar.playBidirectional(
                WeaponSetPacket.TYPE, WeaponSetPacket.STREAM_CODEC, (packet, context) -> {
                    // 强制切换激活武器组（UI 调试 / 手动覆盖用）：写入已同步附件并重算天赋效果
                    // （仅当前激活栏位 + 全局节点点亮）。常规切换由玩家选中快捷栏栏位自动触发（见 onPlayerTick）。
                    Player player = context.player();
                    int set = Math.max(0, Math.min(TalentWeaponSet.MAX_BINDING, packet.set()));
                    // 原 AttachmentRegistry.ACTIVE_WEAPON_SET 已迁入 AdditionData（key=active_weapon_set，默认 1）
                    AdditionData.addOrReplaceData(player, AdditionKeys.ACTIVE_WEAPON_SET, set);
                    TalentEffectApplier.apply(player);
                });

        registrar.executesOn(HandlerThread.MAIN).playBidirectional(
                TalentSocketPacket.TYPE, TalentSocketPacket.STREAM_CODEC, (packet, context) -> {
                    // 珠宝插槽的插入 / 取出必须在服务端权威执行（涉及背包物品移动）。
                    Player player = context.player();
                    var model = TalentTreeModelLoader.get(packet.treeId());
                    TalentNodeDef def = model.node(packet.nodeId());
                    // 目标必须是「已点亮的珠宝插槽节点」
                    if (def == null || !def.type().socket()) return;
                    TalentData td = player.hasData(AttachmentRegistry.TALENT_TREE)
                            ? player.getData(AttachmentRegistry.TALENT_TREE) : TalentData.EMPTY;
                    TalentState cur = td.get(packet.treeId()).state();
                    TalentAllocator alloc = TalentAllocator.create(model, packet.treeId(), cur, player);
                    if (!alloc.isAllocated(packet.nodeId())) return;
                    Inventory inv = player.getInventory();
                    switch (packet.action()) {
                        case TalentSocketPacket.IN -> {
                            if (!alloc.socketOf(packet.nodeId()).isEmpty()) return; // 已有珠宝
                            int idx = packet.invSlot();
                            if (idx < 0 || idx >= inv.getContainerSize()) return;
                            ItemStack stack = inv.getItem(idx);
                            if (!(stack.getItem() instanceof com.cimdy.coe.item.gem.Jewel)) return;
                            ItemStack jewel = stack.copyWithCount(1);
                            stack.shrink(1);
                            inv.setItem(idx, stack);
                            alloc.setSocket(packet.nodeId(), jewel);
                        }
                        case TalentSocketPacket.OUT -> {
                            ItemStack jewel = alloc.socketOf(packet.nodeId());
                            if (jewel.isEmpty()) return;
                            if (!inv.add(jewel.copy())) player.drop(jewel.copy(), false);
                            alloc.setSocket(packet.nodeId(), ItemStack.EMPTY);
                        }
                        default -> { }
                    }
                    TalentState next = new TalentState(alloc.allocations(), model.version());
                    player.setData(AttachmentRegistry.TALENT_TREE,
                            td.with(packet.treeId(), new TalentTreeData(next)));
                    TalentEffectApplier.apply(player);
                    player.containerMenu.broadcastChanges();
                });

        registrar.playBidirectional(
                OpenTalentMenuPacket.TYPE, OpenTalentMenuPacket.STREAM_CODEC, (packet, context) -> {
                    Player player = context.player();
                    if (player instanceof ServerPlayer serverPlayer) {
                        Identifier treeId = packet.treeId();
                        serverPlayer.openMenu(new MenuProvider() {
                            @Override
                            public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player p) {
                                return new TalentMenu(containerId, inventory);
                            }

                            @Override
                            public Component getDisplayName() {
                                // 菜单标题仅用于把树 id 承载给客户端界面（界面自身标题另经 treeDisplayName 渲染），
                                // 故写字面 id 字符串，客户端再由 Identifier.parse 还原。
                                return Component.literal(treeId.toString());
                            }
                        });
                    }
                });

        // ==================== 进度系统 ====================

        // 定义表同步（S2C）：服务端不消费此包，此处仅为满足双向注册
        registrar.playBidirectional(
                ProgressionSyncPacket.TYPE, ProgressionSyncPacket.STREAM_CODEC, (packet, context) -> {
                });

        // 槽位定义同步（S2C）：服务端不消费此包，此处仅为满足发送注册；
        // 客户端收到后经 LoadEvent.applySlotSync 整表覆盖本地快照。
        registrar.executesOn(HandlerThread.MAIN).playToClient(
                SlotSyncPacket.TYPE, SlotSyncPacket.STREAM_CODEC);

        registrar.executesOn(HandlerThread.MAIN).playBidirectional(
                UnlockTechPacket.TYPE, UnlockTechPacket.STREAM_CODEC, (packet, context) -> {
                    // 服务端权威校验（等级 + 科技点 + 未解锁），客户端的灰显只是提示
                    Player player = context.player();
                    if (!(player instanceof ServerPlayer sp)) return;
                    ProgressionService.unlockTech(sp, packet.techId());
                });

        registrar.executesOn(HandlerThread.MAIN).playBidirectional(
                StartResearchPacket.TYPE, StartResearchPacket.STREAM_CODEC, (packet, context) -> {
                    Player player = context.player();
                    BlockPos pos = packet.benchPos();
                    // 防作弊：研究必须在玩家够得着的研究台上发起，禁止隔空启动
                    if (!PlayerReach.canReach(player, pos)) return;
                    if (player.level().getBlockEntity(pos) instanceof ResearchBenchBlockEntity bench) {
                        bench.start(player, packet.researchId());
                    }
                });

        // 打开研究树界面（S2C）：服务端不消费
        registrar.playBidirectional(
                OpenResearchPacket.TYPE, OpenResearchPacket.STREAM_CODEC, (packet, context) -> {
                });
    }

    @SubscribeEvent
    public static void registerClient(RegisterClientPayloadHandlersEvent event) {
        event.register(
                MobEffectPacket.TYPE, HandlerThread.NETWORK, (mobEffect, context) -> {
                    Player player = context.player();
                    if (mobEffect.uuid().isPresent() &&
                            player.level().getEntity(mobEffect.uuid().get()) instanceof LivingEntity living) {
                        if (mobEffect.effects().isPresent()) {
                            ArrayList<MobEffectInstance> currentEffects
                                    = new ArrayList<>(living.getActiveEffects());
                            currentEffects.stream()
                                    .filter(effect -> !mobEffect.effects().get().contains(effect))
                                    .forEach(effect -> living.removeEffect(effect.getEffect()));
                            mobEffect.effects().get().stream()
                                    .filter(effect -> !currentEffects.contains(effect))
                                    .forEach(living::addEffect);
                        }
                    }
                });

        event.register(KeyPressPacket.TYPE, HandlerThread.NETWORK, (packet, context) -> {});
        event.register(PosPacket.TYPE, HandlerThread.NETWORK, (packet, context) -> {});
        event.register(SpawnEntityPacket.TYPE, HandlerThread.NETWORK, (packet, context) -> {});
        event.register(RecycleEntityPacket.TYPE, HandlerThread.NETWORK, (packet, context) -> {});
        event.register(ToggleAllyPacket.TYPE, HandlerThread.NETWORK, (packet, context) -> {});
        event.register(ToggleCombatantPacket.TYPE, HandlerThread.NETWORK, (packet, context) -> {});
        event.register(StockSettingPacket.TYPE, HandlerThread.NETWORK, (packet, context) -> {});
        event.register(BindSite.TYPE, HandlerThread.NETWORK, (packet, context) -> {});
        event.register(TeleportRequestPacket.TYPE, HandlerThread.NETWORK, (packet, context) -> {});
        // 背包取放回执：服务端权威值镜像到本地背包 + 背包浮窗手持栈
        event.register(TakePutAckPacket.TYPE, HandlerThread.MAIN, (packet, context) -> {
            Player player = context.player();
            if (player == null) return;
            player.getInventory().setItem(packet.slotIndex(), packet.stack());
            InventoryFloatWindow.setCarried(packet.carried());
        });
        event.register(SkillSetupPacket.TYPE, HandlerThread.NETWORK, (packet, context) -> {});
        event.register(SkillAssemblyActionPacket.TYPE, HandlerThread.NETWORK, (packet, context) -> {});
        event.register(OpenSkillMenuPacket.TYPE, HandlerThread.NETWORK, (packet, context) -> {});
        event.register(OpenTestPanelPacket.TYPE, HandlerThread.NETWORK, (packet, context) -> {});
        event.register(OpenExtraSlotsMenuPacket.TYPE, HandlerThread.NETWORK, (packet, context) -> {});
        event.register(CutGemPacket.TYPE, HandlerThread.NETWORK, (packet, context) -> {});
        // 切割结果回执：驱动 CutScreen 成功 / 失败双反馈（屏幕已关闭时静默——产物/消耗
        // 已由服务端写入背包，玩家下次打开可见）
        event.register(CutResultPacket.TYPE, HandlerThread.MAIN, (packet, context) -> {
            if (CutScreen.current() != null) {
                CutScreen.current().handleCutResult(packet);
            }
        });
        event.register(SiteSetting.TYPE, HandlerThread.NETWORK, (packet, context) -> {});
        event.register(TalentActionPacket.TYPE, HandlerThread.NETWORK, (packet, context) -> {});
        event.register(OpenTalentMenuPacket.TYPE, HandlerThread.NETWORK, (packet, context) -> {});
        event.register(TalentSocketPacket.TYPE, HandlerThread.NETWORK, (packet, context) -> {});
        event.register(WeaponSetPacket.TYPE, HandlerThread.NETWORK, (packet, context) -> {});
        // 天赋提交回执：转发给当前打开的天赋树屏幕（回执只发提交者本人；26.2 无 Minecraft.screen
        // 访问器，天赋屏单实例注册于 AbstractTalentTreeScreen.current）。
        // 成功 → 屏幕确认本地预览为权威；失败 → 屏幕提示原因并等待服务端回同步兜底。
        event.register(TalentCommitResultPacket.TYPE, HandlerThread.MAIN, (packet, context) -> {
            if (AbstractTalentTreeScreen.current() != null) {
                AbstractTalentTreeScreen.current().handleCommitResult(packet);
            }
        });

        // 进度定义表：整表替换客户端静态表，供科技树 / 研究树 / 任务 HUD / 工种授权读取
        event.register(ProgressionSyncPacket.TYPE, HandlerThread.MAIN, (packet, context) ->
                ProgressionData.applyNetworkSync(packet.quests(), packet.tech(), packet.research(), packet.jobCharters()));
        // 打开研究树界面必须在主线程（涉及 Screen 创建）
        event.register(OpenResearchPacket.TYPE, HandlerThread.MAIN, (packet, context) ->
                ProgressionScreens.openResearch(packet.benchPos()));
        // 槽位定义表：整表覆盖客户端 volatile 快照，供 AccessorySlotRegistry / FlaskSlotRegistry 读取
        event.register(SlotSyncPacket.TYPE, HandlerThread.MAIN, (packet, context) ->
                LoadEvent.applySlotSync(packet.accessorySlots(), packet.flaskSlots()));
        event.register(UnlockTechPacket.TYPE, HandlerThread.NETWORK, (packet, context) -> {});
        event.register(StartResearchPacket.TYPE, HandlerThread.NETWORK, (packet, context) -> {});
        event.register(FlaskUsePacket.TYPE, HandlerThread.NETWORK, (packet, context) -> {});
        event.register(DodgeRollPacket.TYPE, HandlerThread.NETWORK, (packet, context) -> {});
    }

    /** 玩家重生 / 登录后，依据已同步的天赋附件重新应用属性修正与能力。 */
    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            TalentEffectApplier.apply(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            TalentEffectApplier.apply(serverPlayer);
            // 进度定义表在数据包里，客户端读不到，登录时先整表推一次
            ProgressionSyncSender.syncProgression(serverPlayer);
            // 槽位定义来自服务端 config/coe/slots.json，客户端读不到，登录时一并推
            SlotSyncSender.syncSlots(serverPlayer);
        }
    }

    /**
     * 武器专精自动切换：玩家每 tick 检查当前选中的快捷栏栏位，若与已同步的
     * {@code ACTIVE_WEAPON_SET} 不符，则更新该附件并重算天赋效果——
     * 选中的栏位即成为激活武器组，其绑定的天赋点亮，其余栏位节点休眠。
     * 仅在栏位真正变化时触发，避免每 tick 重算。
     */
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Pre event) {
        Player player = event.getEntity();
        if (player.level().isClientSide() || !(player instanceof ServerPlayer)) return;
        int selected = player.getInventory().getSelectedSlot(); // 0–8
        int value = TalentWeaponSet.forSlot(selected);  // 1–9
        // 原 AttachmentRegistry.ACTIVE_WEAPON_SET 已迁入 AdditionData（key=active_weapon_set）
        // 缺省 0 而非附件默认值 1：确保首次 tick 与当前栏位「必然不相等」，从而必定写入并触发一次重算。
        int current = (int) AdditionData.getOrDefault(player, AdditionKeys.ACTIVE_WEAPON_SET, 0.0);
        if (current != value) {
            AdditionData.addOrReplaceData(player, AdditionKeys.ACTIVE_WEAPON_SET, value);
            TalentEffectApplier.apply(player);
        }
        // 翻滚冲刺速度维持（服务端权威，每 tick 重写水平速度直到翻滚窗口结束）
        DodgeRollHelper.tickRoll((ServerPlayer) player);
    }

    /**
     * Flask / Charm 机制每 tick 驱动（服务端）：活跃效果持续恢复（满资源移除）+ 双方充能被动恢复。
     * 两者同点驱动：POE2「gain X charges per Second」对药剂与护符是同一套被动充能语义。
     */
    @SubscribeEvent
    public static void onPlayerFlaskTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide() || !(player instanceof ServerPlayer)) return;
        FlaskHelper.tick(player);
        CharmEngine.tick(player);
    }

    /**
     * 翻滚 / 奔跑中被击中 → 受击响应（POE2 联动）：翻滚前段免伤窗口；奔跑中被敌人击中倒地（击退 + 调整动作）。
     * 仅服务端、仅玩家；非翻滚/非奔跑状态时不产生任何效果（不改写伤害数值）。
     */
    @SubscribeEvent
    public static void onDodgeHit(LivingIncomingDamageEvent event) {
        LivingEntity living = event.getEntity();
        if (living instanceof ServerPlayer sp) {
            DodgeRollHelper.onHit(sp, event);
        }
    }
}
