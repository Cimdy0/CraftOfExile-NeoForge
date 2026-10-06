package com.cimdy.coe.helper;

import com.cimdy.coe.data.codec.support.stat.engine.StatEngine;

import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.registry.AttributeRegistry;
import com.cimdy.coe.data.codec.entity.DodgeRollSave;
import com.cimdy.coe.data.codec.skill.core.SkillCaster;
import com.cimdy.coe.data.codec.skill.core.SkillEntry;
import com.cimdy.coe.data.codec.skill.gem.SkillData;
import com.cimdy.coe.event.data.LoadEvent;
import com.cimdy.coe.data.codec.skill.runtime.SkillTriggerHelper;
import com.cimdy.coe.data.codec.skill.support.SupportProfile;
import com.cimdy.coe.data.helper.hub.SpiritManager;
import java.util.Set;
import com.cimdy.coe.network.input.DodgeRollAnimPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 翻滚（Dodge Roll）/ 奔跑（Sprint）机制核心：发起判定、冲刺速度维持、实体推开、受击响应。
 * 服务端权威：按键 → {@link #tryDodge} 校验并写入状态；每 tick 由 {@link #tickRoll} 维持冲刺速度；
 * 受击由 {@link #onHit} 处理翻滚免伤 / 奔跑倒地（调整动画由网络包驱动客户端播放）。
 *
 * <p><b>架构（POE2 对齐，第七轮定稿）</b>：翻滚与奔跑是两个<b>独立机制</b>，仅通过同一按键（X）联动——
 * 点按 = 翻滚；按住翻滚键（翻滚结束后仍按住）= 进入<b>原版 sprint</b>（由 client {@code SprintingMixin} 驱动）。
 * 翻滚键 + 空格 = <b>空翻</b>变体（{@link #DODGE_FLIP}）：空中发起时赋予落地坠落伤害保护（见 {@link #consumeFlipLanding}）。
 * 奔跑不再由本类以属性修正“第二套”实现，而是复用原版 sprint（保留饥饿消耗、撞墙减速、水中停止等原版行为）。
 *
 * <p>POE2 联动细节：
 * <ul>
 *   <li>翻滚前段 {@link #IFRAME_TICKS} 游戏刻为免伤窗口（免疫近战/投射物命中）。</li>
 *   <li>奔跑中被敌人击中 → 倒地（取消 sprint + 击退 + 调整动作动画）；起步 {@link #SPRINT_GRACE_TICKS} 有 grace period 免误击倒。</li>
 *   <li>{@code CANNOT_DODGE_ROLL_OR_SPRINT} ≥ 1.0（如 Unwavering Stance）同时禁用翻滚与奔跑。</li>
 * </ul>
 */
public final class DodgeRollHelper {
    private DodgeRollHelper() {
    }

    /** 翻滚持续游戏刻（≈0.6s @20tps），与动画时长一致。 */
    public static final int ROLL_TICKS = 12;
    /** 翻滚冲刺速度（方块/tick）。 */
    public static final double ROLL_SPEED = 0.55;
    /** 翻滚冷却（游戏刻，≈1.5s）：两次翻滚之间的最小间隔。 */
    public static final int COOLDOWN_TICKS = 30;
    /** 翻滚前段免伤窗口（游戏刻）：POE2 翻滚起始段免疫近战/投射物命中。 */
    public static final int IFRAME_TICKS = 5;
    /** 被击倒地后调整动作时长（游戏刻，≈0.5s）。 */
    public static final int RECOVERY_TICKS = 10;
    /** 奔跑被击中后免误击倒的起步 grace period（游戏刻）。 */
    public static final int SPRINT_GRACE_TICKS = 6;
    /** 翻滚推开实体的半径（方块）。 */
    public static final double PUSH_RADIUS = 0.9;
    /** 翻滚推开实体的水平冲量。 */
    public static final double PUSH_SPEED = 0.4;
    /** 空翻（翻滚+空格）落地保护窗口（游戏刻，≈3s）：空中发起空翻后，落地前的坠落伤害在此窗口内被大幅减免。 */
    public static final int FLIP_LANDING_TICKS = 60;

    /** 原版奔跑（sprint）起始时刻（服务端瞬态，用于“被击中倒地”grace period 判定；非持久化）。 */
    private static final Map<UUID, Long> sprintStartedAt = new HashMap<>();
    /** 空翻落地保护截止时刻（服务端瞬态）：空中发起空翻后，落地前的坠落伤害在此窗口内被大幅减免；非持久化。 */
    private static final Map<UUID, Long> flipLandingUntil = new HashMap<>();

    /** {@link #tryDodge} 返回码：0=发起失败（冷却/调整/禁用），1=普通翻滚，2=空翻。 */
    public static final int DODGE_FAIL = 0, DODGE_ROLL = 1, DODGE_FLIP = 2;
    /** 快行技能（Travel，如 Blink）已接管本次翻滚输入。 */
    public static final int DODGE_TRAVEL = 3;

    /**
     * 发起翻滚（含空翻变体）：校验冷却 / 调整窗口 / 禁用标记后，设定冲刺方向与速度并写入状态。
     *
     * @param inputX 客户端移动输入：左右（A/D），+1=右（= LocalPlayer.xxa / strafe）
     * @param inputZ 客户端移动输入：前后（W/S），+1=前（= LocalPlayer.zza / forward）
     * @param flip   是否空翻变体（翻滚键 + 空格同时按住）。仅当<b>空中</b>发起时空翻落地保护窗口生效。
     * @return {@link #DODGE_FAIL}=发起失败（冷却/调整/禁用），{@link #DODGE_ROLL}=普通翻滚，{@link #DODGE_FLIP}=空翻
     */
    public static int tryDodge(ServerPlayer player, float inputX, float inputZ, boolean flip) {
        long now = player.level().getGameTime();
        // 禁用类标记：CANNOT_DODGE_ROLL_OR_SPRINT 生效（≥1.0，如 Unwavering Stance）时无法翻滚/奔跑
        if (StatEngine.gateOf(player, "cannot_dodge_roll_or_sprint", Set.of())) return DODGE_FAIL;

        // POE2 真值（Blink）：已激活的<b>快行（Travel）</b>精魂宝石**替换**闪避翻滚
        // （"While active, replaces your dodge roll with a short-cooldown Spell ..."）。
        // 位置要点：
        //   · 在「翻滚被禁用」检查<b>之后</b> —— 真值「If dodge roll is disabled via Unwavering Stance,
        //     then Blink gets disabled as well」；
        //   · 在翻滚冷却 / 调整窗口检查<b>之前</b> —— Blink 替换翻滚后，翻滚自身的冷却不再适用，
        //     节奏改由 Blink 自身冷却（4.5s，落在 TRAVEL_BIND 槽）决定。
        SkillData travel = SpiritManager.getActiveTravel(player);
        if (travel != null) return tryTravelSkill(player, travel);

        // 翻滚自身冷却 / 调整窗口：仅在<b>未</b>被快行技能接管时适用（Blink 节奏走自身冷却槽）。
        DodgeRollSave save = player.getData(AttachmentRegistry.DODGE_ROLL);
        if (save.isOnCooldown(now) || save.isRecovering(now)) return DODGE_FAIL;

        Vec3 dir = resolveDirection(player, inputX, inputZ);
        // 立即给出初速度，保证手感即时响应
        double speed = rollSpeed(player);
        player.setDeltaMovement(dir.x * speed, player.getDeltaMovement().y, dir.z * speed);

        long rollingUntil = now + ROLL_TICKS;
        long cooldownUntil = now + COOLDOWN_TICKS;
        player.setData(AttachmentRegistry.DODGE_ROLL,
                new DodgeRollSave(rollingUntil, cooldownUntil, dir.x, dir.z, 0L));

        // 空翻：仅在空中发起时开启落地保护窗口（「坠地时减免伤害」）；地面空翻不赋予坠落无敌。
        if (flip && !player.onGround()) {
            flipLandingUntil.put(player.getUUID(), now + FLIP_LANDING_TICKS);
        }
        // 翻滚动作事件：供「近期翻滚过 → 增闪避」类词缀经 recently(dodge_roll) 读取（与伤害管线 CAUSED_DODGE 正交）。
        com.cimdy.coe.event.Event.record(player, com.cimdy.coe.event.EventName.DODGE_ROLL);
        // 翻滚元事件（事件总线的<b>真实发射点</b>）：Event.record 只喂 recently 条件，
        // 「翻滚时 / 翻滚后触发 X」类常驻监听（Trail of Caltrops「翻滚时在身后撒下铁蒺藜」）
        // 此前根本无从订阅。Blink 等快行技能替换翻滚的分支在其之前 return，故不计为翻滚。
        com.cimdy.coe.data.codec.skill.phase.event.SkillEventBus.publishMeta(
                player, com.cimdy.coe.event.EventName.DODGE_ROLL);
        return flip ? DODGE_FLIP : DODGE_ROLL;
    }

    /**
     * 以翻滚键触发快行（Travel）精魂技能：技能本体携带全部真值（距离 4m / 冷却 4.5s，见
     * {@code blue/blink} 注册），此处只做「按键 → 施放」的转发与冷却门控，<b>不复制任何数值</b>。
     *
     * <p>真值 0.3.0 起「Using Blink no longer counts as dodge rolling」，故此处<b>不</b>记
     * {@code DODGE_ROLL} 事件，也不写入翻滚状态（无需冲刺维持与免伤窗口）。
     *
     * @return {@link #DODGE_TRAVEL}=已施放；{@link #DODGE_FAIL}=技能冷却中或不可施放
     */
    private static int tryTravelSkill(ServerPlayer player, SkillData data) {
        SkillEntry entry = LoadEvent.getSpirit(data.id());
        if (entry == null) return DODGE_FAIL;
        // 用 castGranted 而非普通 cast：翻滚键<b>不产生技能按键输入</b>，pressTime 恒为 0，
        // 而技能须 {@code pressTime >= castTime} 才结算 —— 走普通 cast 路径 Blink 会永不结算（死线）。
        // castGranted 预置 pressTime = castTime，并在施放前校验本槽冷却
        // （冷却中返回 null ⇒ Blink 与翻滚一并禁用，符合真值）。
        var inst = SkillCaster.of(player).castGranted(entry.resolve(), SkillTriggerHelper.TRAVEL_BIND,
                data.level().get(), data.quality().get(), SupportProfile.EMPTY, data.id());
        return inst != null ? DODGE_TRAVEL : DODGE_FAIL;
    }

    /** 每 tick 维持翻滚冲刺速度（方向恒定、竖直速度保留重力/跳跃），并在翻滚中推开附近实体；并观测原版奔跑起始时刻。 */
    public static void tickRoll(ServerPlayer player) {
        DodgeRollSave save = player.getData(AttachmentRegistry.DODGE_ROLL);
        long now = player.level().getGameTime();

        if (save.isRolling(now)) {
            double speed = rollSpeed(player);
            player.setDeltaMovement(
                    save.dirX() * speed,
                    player.getDeltaMovement().y,
                    save.dirZ() * speed);
            pushNearby(player);
            return;
        }

        // 翻滚无关：观测原版 sprint 起始时刻，供“被击中倒地”grace period 使用。
        // 奔跑现由 client SprintingMixin 驱动原版 sprint，本类只读取其状态（setSprinting 由服务端在倒地时取消）。
        UUID id = player.getUUID();
        if (player.isSprinting()) sprintStartedAt.putIfAbsent(id, now);
        else sprintStartedAt.remove(id);
    }

    /**
     * 受击响应（服务端权威）：
     * <ul>
     *   <li>翻滚中：起始 {@link #IFRAME_TICKS} 游戏刻内免伤（POE2 翻滚前段免疫命中）；其余段正常承伤，不中断翻滚。</li>
     *   <li>奔跑中：被<b>敌人</b>击中 → 倒地（取消原版 sprint + 击退 + 调整动作动画）；起步 grace period 内免误击倒。
     *       环境伤害（跌落/火焰等无攻击者）不中断奔跑。</li>
     *   <li>其余状态：不改写伤害数值，不产生任何效果。</li>
     * </ul>
     */
    public static void onHit(ServerPlayer player, LivingIncomingDamageEvent event) {
        DodgeRollSave save = player.getData(AttachmentRegistry.DODGE_ROLL);
        long now = player.level().getGameTime();

        if (save.isRolling(now)) {
            long rollStart = save.rollingUntil() - ROLL_TICKS;
            if (now - rollStart < IFRAME_TICKS
                    && !StatEngine.gateOf(player, "dodge_roll_cannot_avoid_damage", Set.of())) {
                event.setAmount(0.0F); // 翻滚前段免伤窗口（DODGE_ROLL_CANNOT_AVOID_DAMAGE ≥1.0 时失效）
            }
            return; // 翻滚其余段正常承伤，翻滚不被打断
        }

        if (player.isSprinting()) {
            DamageSource src = event.getSource();
            if (src.getEntity() == null) return; // 仅敌人攻击触发倒地，环境伤害不中断奔跑
            Long started = sprintStartedAt.get(player.getUUID());
            if (started != null && now - started < SPRINT_GRACE_TICKS) return; // 起步 grace period 内免误击倒

            // 取消奔跑：服务端置否 → 客户端经实体数据同步停止 sprint
            player.setSprinting(false);
            sprintStartedAt.remove(player.getUUID());

            // 击退（远离攻击者）
            Entity attacker = src.getEntity();
            Vec3 away = player.position().subtract(attacker.position());
            away = new Vec3(away.x, 0, away.z);
            if (away.lengthSqr() < 1e-4) away = new Vec3(player.getLookAngle().x, 0, player.getLookAngle().z);
            away = away.normalize().scale(0.5);
            player.setDeltaMovement(
                    player.getDeltaMovement().x + away.x,
                    player.getDeltaMovement().y,
                    player.getDeltaMovement().z + away.z);

            // 倒地调整窗口 + 广播调整动作动画（客户端据其设置“禁止立即重新奔跑”窗口）
            long downedUntil = now + RECOVERY_TICKS;
            player.setData(AttachmentRegistry.DODGE_ROLL,
                    new DodgeRollSave(0L, save.cooldownUntil(), 0.0, 0.0, downedUntil));
            PacketDistributor.sendToPlayersInDimension(player.level(),
                    new DodgeRollAnimPacket(player.getUUID(), DodgeRollAnimPacket.KIND_RECOVERY, RECOVERY_TICKS));
        }
    }

    /** 翻滚中：将附近较小实体（非玩家、非自身）向远离方向推开，模拟 POE2 翻滚挤开怪物。 */
    private static void pushNearby(ServerPlayer player) {
        var level = player.level();
        for (var entity : level.getEntities(player, player.getBoundingBox().inflate(PUSH_RADIUS))) {
            if (!(entity instanceof LivingEntity other) || other == player) continue;
            if (other instanceof Player) continue;            // 不推其他玩家，避免 PvP 异样
            if (!other.isAlive()) continue;
            Vec3 away = other.position().subtract(player.position());
            away = new Vec3(away.x, 0, away.z);
            if (away.lengthSqr() < 1e-4) away = new Vec3(1, 0, 0); // 完全重合时给个默认方向
            away = away.normalize();
            other.setDeltaMovement(away.x * PUSH_SPEED, other.getDeltaMovement().y, away.z * PUSH_SPEED);
            other.hurtMarked = true;
        }
    }

    /** 是否正在翻滚。 */
    public static boolean isRolling(LivingEntity entity) {
        if (!entity.hasData(AttachmentRegistry.DODGE_ROLL)) return false;
        return entity.getData(AttachmentRegistry.DODGE_ROLL).isRolling(entity.level().getGameTime());
    }

    /**
     * 空翻落地保护：若玩家仍在保护窗口内，消耗该窗口并返回 true（由 {@code LivingFallEvent} 调用以减免坠落伤害）。
     * 仅服务端瞬态，不持久化；窗口过期（落地过晚 / 未在窗口内坠落）则视为未保护。
     */
    public static boolean consumeFlipLanding(ServerPlayer player) {
        UUID id = player.getUUID();
        Long until = flipLandingUntil.get(id);
        if (until != null && player.level().getGameTime() < until) {
            flipLandingUntil.remove(id);
            return true;
        }
        return false;
    }

    /**
     * 翻滚速度（方块/tick）：基础距离 {@code ROLL_SPEED*ROLL_TICKS} 叠加 {@code DODGE_ROLL_DISTANCE} 属性（零基 ADD_VALUE，
     * 「+X metres to Dodge Roll distance」接线于此真读），再除以 {@code ROLL_TICKS} 还原为每 tick 速度。
     * 属性为 0 时速度 = ROLL_SPEED（与原行为完全一致）。
     */
    private static double rollSpeed(LivingEntity e) {
        double baseDist = ROLL_SPEED * ROLL_TICKS;
        double dist = baseDist + e.getAttributeValue(AttributeRegistry.DODGE_ROLL_DISTANCE);
        return dist / ROLL_TICKS;
    }

    /**
     * 由移动输入 + 玩家朝向解析世界水平方向。
     * <ul>
     *   <li>无方向键 → 默认朝<b>水平视线前方</b>（满足「翻滚默认朝向方向」）。</li>
     *   <li>有方向键 → <b>逐字复刻</b>原版 {@code LocalPlayer.isHorizontalCollisionMinor}（L1130‑1131）的世界位移变换
     *       {@code globalX = xxa·cos − zza·sin, globalZ = zza·cos + xxa·sin}，
     *       且使用与客户端上报完全相同的 {@code xxa=inputX / zza=inputZ}（来自 {@code lp.xxa / lp.zza}）。
     *       因输入与变换式均与原版一致，翻滚方向 = 玩家“按此输入实际行走”的方向，
     *       故「按 W 朝前、A 朝左、W+A 左前、S+D 右后 …」一一对应，<b>绝不左右镜像</b>。
     *       该原版式不含任何对角线符号修正项（d3），此前误加的 d3 会镜像对角线方向，已移除。</li>
     * </ul>
     */
    private static Vec3 resolveDirection(Player player, float inputX, float inputZ) {
        if (inputX == 0f && inputZ == 0f) {
            // 无方向键：默认朝水平视线前方
            Vec3 look = player.getLookAngle();
            Vec3 forward = new Vec3(look.x, 0, look.z);
            if (forward.lengthSqr() < 1e-6) return new Vec3(0, 0, 1); // 极端竖直视角兜底
            return forward.normalize();
        }
        // 有方向键：与原版移动变换逐字一致（无 d3 项）
        double rad = Math.toRadians(player.getYRot());
        double sin = Math.sin(rad);                       // = Mth.sin(yRot)
        double cos = Math.cos(rad);                       // = Mth.cos(yRot)
        double wx = inputX * cos - inputZ * sin;
        double wz = inputZ * cos + inputX * sin;
        Vec3 dir = new Vec3(wx, 0, wz);
        if (dir.lengthSqr() < 1e-6) {
            Vec3 look = player.getLookAngle();
            return new Vec3(look.x, 0, look.z).normalize();
        }
        return dir.normalize();
    }
}
