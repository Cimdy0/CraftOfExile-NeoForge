package com.cimdy.coe.registry.world;

import com.cimdy.coe.data.codec.entity.DodgeRollSave;
import com.cimdy.coe.data.helper.impl.CursePending;
import com.cimdy.coe.data.helper.impl.value.EvasionEntropy;
import com.cimdy.coe.data.helper.impl.value.LeechValue;
import com.cimdy.coe.data.helper.impl.value.RecoupValue;
import com.cimdy.coe.data.helper.stat.AttributeLimiterHelper;
import com.cimdy.coe.data.helper.hub.ResourceRegistry;

import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.data.codec.entity.LivingMod;
import com.cimdy.coe.data.codec.entity.LivingModifier;
import com.cimdy.coe.data.codec.item.AccessorySave;
import com.cimdy.coe.data.codec.item.CharmSave;
import com.cimdy.coe.data.codec.item.flask.FlaskEffect;
import com.cimdy.coe.data.codec.item.flask.FlaskSave;
import com.cimdy.coe.data.codec.ChunkSpawners;
import com.cimdy.coe.data.codec.progression.PlayerProgress;
import com.cimdy.coe.data.codec.progression.research.ResearchBench;
import com.cimdy.coe.data.codec.skill.core.SkillCaster;
import com.cimdy.coe.data.codec.skill.gem.SkillData;
import com.cimdy.coe.data.codec.skill.gem.KeyBind;
import com.cimdy.coe.data.codec.skill.gem.SkillAssembly;
import com.cimdy.coe.data.codec.skill.helper.state.SealState;
import com.cimdy.coe.data.codec.stronghold.Stronghold;
import com.cimdy.coe.data.codec.stronghold.SaveData;
import com.cimdy.coe.data.codec.stronghold.entity.StrongholdEntity;
import com.cimdy.coe.data.codec.stronghold.entity.PlayerEntity;
import com.cimdy.coe.data.codec.stronghold.storage.Detailed;
import com.cimdy.coe.data.codec.stronghold.storage.stock.StockSet;
import com.cimdy.coe.data.codec.support.addition.AdditionData;
import com.cimdy.coe.data.codec.support.meta.Camp;
import com.cimdy.coe.data.codec.support.meta.DamageConvert;
import com.cimdy.coe.data.codec.support.rarity.Rarity;
import com.cimdy.coe.data.codec.support.value.Value;
import com.cimdy.coe.data.codec.support.attribute.AttributeLimiters;
import com.cimdy.coe.data.codec.support.attribute.EntityAttributeLimiter;
import com.cimdy.coe.data.codec.support.stat.engine.StatStore;
import com.cimdy.coe.data.codec.support.status.StatusHudEntry;
import com.cimdy.coe.data.codec.support.status.runtime.StatusStore;
import com.cimdy.coe.data.codec.support.status.runtime.TransferStore;
import com.cimdy.coe.data.codec.support.status.registry.ground.GroundFieldSaveData;
import com.cimdy.coe.data.helper.combat.ReviveEntry;
import com.cimdy.coe.data.codec.talent.data.TalentData;
import com.cimdy.coe.data.helper.impl.*;
import com.cimdy.coe.network.input.KeyPressPacket;
import com.mojang.serialization.Codec;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public class AttachmentRegistry {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, CraftOfExile.MODID);

    public static final Supplier<AttachmentType<ChunkSpawners>> CHUNK_SPAWNERS = registerNotSync(
            "chunk_spawners", ChunkSpawners.CODEC, ChunkSpawners.EMPTY);

    /**
     * 统一状态容器（取代原 StatusContents）：实体的全部状态实例（异常/减益/增益/
     * 技能型/神龛）均存于此附件，由 {@link com.cimdy.coe.data.codec.support.status.runtime.StatusEngine} 驱动。
     * 非同步（状态逻辑服务端权威，仅 HUD 所需部分另行同步）。
     */
    public static final Supplier<AttachmentType<StatusStore>> STATUS_STORE = registerNotSync(
            "status_store", StatusStore.CODEC, StatusStore.EMPTY);

    /**
     * 异常转存存储（P2-3 store/transfer 通道）：承载被消费异常的「记账」数值
     * （intensity/remaining/stacks），由
     * {@link com.cimdy.coe.data.codec.support.status.runtime.AilmentTransfer} 读写。
     * 复用 {@code StatusStore} 同款非同步附件风格（服务端权威、不随死亡复制、不随客户端同步）；
     * 仅运行时瞬时记账，重启/重生后清空。
     */
    public static final Supplier<AttachmentType<TransferStore>> TRANSFER_STORE = registerNotSync(
            "transfer_store", TransferStore.CODEC, TransferStore.EMPTY);

    /** 客户端 HUD 渲染用的状态快照（仅携带图标/时长/层数，由服务端周期性同步）。 */
    public static final Supplier<AttachmentType<List<StatusHudEntry>>> STATUS_HUD = registerSync(
            "status_hud", StatusHudEntry.CODEC.listOf(), List.of());
    public static final Supplier<AttachmentType<KeyPressPacket>> KEY_PRESS = registerSync(
            "key_press", KeyPressPacket.CODEC, KeyPressPacket.EMPTY);
    /**
     * 翻滚（Dodge Roll）运行时状态：{@code rollingUntil / cooldownUntil / 方向向量}。
     * 服务端权威、不随客户端同步（动画由独立包驱动）、不随死亡复制（重生后翻滚状态清空）。
     */
    public static final Supplier<AttachmentType<DodgeRollSave>> DODGE_ROLL = registerNotSync(
            "dodge_roll", DodgeRollSave.CODEC, DodgeRollSave.EMPTY);
    /**
     * Flask 槽位（POE2：生命药剂槽 + 魔力药剂槽，共 2 槽）：存于玩家附件，背包 UI 的 FlaskSlot 读写。
     * 已同步（客户端 UI 需渲染槽内物品与充能条）、<b>死亡保留</b>（POE2 死亡不掉落装备/药剂）。
     */
    public static final Supplier<AttachmentType<FlaskSave>> FLASK_SAVE = register(
            "flask_save", FlaskSave.CODEC, FlaskSave.STREAM_CODEC, FlaskSave.EMPTY, true, true);
    /**
     * 活跃 Flask 效果（slotIndex → perTick/剩余 tick；手持饮用用键 -1）：服务端权威，
     * 由 FlaskHelper.tick 每 tick 驱动。<b>死亡清空</b>（重生后不继承未走完的回复）。
     */
    public static final Supplier<AttachmentType<Map<Integer, FlaskEffect>>> FLASK_ACTIVE = registerSync(
            "flask_active", Codec.unboundedMap(Codec.INT, FlaskEffect.CODEC), Map.of());
    /**
     * Charm 槽位（POE2：最多 3 个 Charm 槽）：存于玩家附件，背包 UI 的 CharmSlot 读写。
     * 已同步（客户端 UI 需渲染槽内物品与充能条）、<b>死亡保留</b>（与 Flask 同策略：保留装备/护符）。
     */
    public static final Supplier<AttachmentType<CharmSave>> CHARM_SAVE = register(
            "charm_save", CharmSave.CODEC, CharmSave.STREAM_CODEC, CharmSave.EMPTY, true, true);
    /**
     * 护符 HUD 激活标记（slot → 剩余 tick）：由 {@code CharmEngine} 在触发时写入、每 tick 衰减至 0 移除，
     * 与 {@code STAT_STORE} 中 {@code charm:<slot>:} 临时条目的计时域同步。服务端权威、已同步客户端
     * （客户端 {@code renderCharmEffects} 据此绘制护符物品图标 + 时长）、不随死亡复制（重生后不继承未走完的护符效果）。
     */
    public static final Supplier<AttachmentType<Map<Integer, Integer>>> CHARM_ACTIVE = registerSync(
            "charm_active", Codec.unboundedMap(Codec.INT, Codec.INT), Map.of());
    /**
     * 饰品栏（数据驱动：槽位组由 {@code config/coe/slots.json} 定义，无配置时走内置兜底 amulet/legs/quiver）。
     * 存于玩家附件，背包 UI 的 {@code AccessorySlot} 读写。已同步（客户端 UI 需渲染槽内物品）、
     * <b>死亡保留</b>（与 Flask/Charm 同策略）。
     */
    public static final Supplier<AttachmentType<AccessorySave>> ACCESSORY_SAVE = register(
            "accessory_save", AccessorySave.CODEC, AccessorySave.STREAM_CODEC, AccessorySave.EMPTY, true, true);
    public static final Supplier<AttachmentType<KeyBind.SkillSave>> SKILL_SAVE = registerSync(
            "skill_save", KeyBind.SkillSave.CODEC, KeyBind.SkillSave.DEFAULT);
    /**
     * 统一技能冷却：存储「能力槽 -> 剩余冷却 tick」映射，<b>玩家与任意实体共用同一附件</b>。
     * 冷却读写与每 tick 推进均由 {@link SkillCaster} 负责（见 {@link SkillCaster#tickAll}），
     * 服务端权威并同步到客户端（玩家 HUD 需据此绘制冷却蒙版）。
     */
    public static final Supplier<AttachmentType<Map<Integer, Integer>>> SKILL_COOLDOWN = registerSync(
            "skill_cooldown", Codec.unboundedMap(Codec.INT, Codec.INT), Map.of());
    /** 技能装配槽（9 个）：存储玩家装配的技能石及其辅助宝石，槽位可空。 */
    public static final Supplier<AttachmentType<SkillAssembly>> SKILL_ASSEMBLY = registerSync(
            "skill_assembly", SkillAssembly.CODEC, SkillAssembly.EMPTY);
    /**
     * 装备授予的技能（数据驱动 {@code coe:equipment_template.skills}）：每件已穿戴装备的授予技能
     * 以来源前缀 {@code equip:<slot>:<uuid>} 为键归入本附件（与 StatStore 同款前缀寻址），
     * 脱下时按前缀整体移除，与属性条目（{@link AttributeLimiterHelper}）完全对称、互不残留。
     * 玩家与任意实体共用同一附件；合并视图经 {@link com.cimdy.coe.data.helper.skill.EquipmentSkillHelper#mergedAssembly}
     * 并入统一的 SkillCaster 施法通道。已同步（客户端 HUD / 装配界面需渲染与自动绑定装备技能）。
     */
    public static final Supplier<AttachmentType<Map<String, List<SkillData>>>> EQUIPMENT_SKILLS = registerSync(
            "equipment_skills", Codec.unboundedMap(Codec.STRING, SkillData.CODEC.listOf()), Map.of());
    /** 天赋树分配状态（以树名为键聚合各树数据）：服务端权威、已同步到客户端、死亡保留。 */
    public static final Supplier<AttachmentType<TalentData>> TALENT_TREE = register(
            "talent_tree", TalentData.CODEC, null, TalentData.EMPTY, true, true);
    // 当前激活武器组（0 = 全局，1–9 = 快捷栏栏位 0–8）已迁出本类，改存
    // AdditionData（key = AdditionKeys.ACTIVE_WEAPON_SET，默认 1）：该值由玩家实际选中的快捷栏栏位驱动
    // （NetEvent 的玩家 tick 监听自动同步），仅该栏位（及全局）绑定的天赋节点点亮，其余栏位节点休眠。
    public static final Supplier<AttachmentType<AreaEffectCloudData>> AREA_EFFECT_CLOUD_DATA = registerNotSync(
            "area_effect_cloud_data", AreaEffectCloudData.CODEC, AreaEffectCloudData.EMPTY);
    /**
     * 地面场（Ground Field）子系统持久化载体：保存某维度全部活动地面场（{@link GroundFieldSaveData}），
     * 由 {@code GroundFieldManager} 每 tick 读写、推进生命周期。
     * 服务端权威、不随客户端同步（客户端经 {@code GroundFieldSync} 包渲染）、不随死亡复制
     * （挂在 {@code Level} 维度附件，随维度存档、重启仍在）。
     */
    public static final Supplier<AttachmentType<GroundFieldSaveData>> GROUND_FIELDS = registerNotSync(
            "ground_fields", GroundFieldSaveData.CODEC, GroundFieldSaveData.EMPTY);
    // 战吼强化攻击（Warcry Empower）剩余次数已迁出本类，改存
    // AdditionData（key = AdditionKeys.EMPOWERED_ATTACKS，默认 0）：战吼施放时授予，每次攻击消耗一次。
    // 尸体计数（Corpse）已迁出本类，改存 AdditionData（key = AdditionKeys.CORPSE_COUNT，默认 0）：
    // 使用者（击杀者/召唤者）当前持有的尸体数，封顶见 ResourceRegistry.MAX_CORPSES。
    /**
     * 当前召唤物数量：<b>事件驱动</b>统计（{@code SpawnAction} 生成 +1 / 到期销毁 −1），
     * 服务端权威、<b>已同步</b>（技能 Status 需在 HUD 上实时展示数量）、不随死亡复制（死亡后召唤物也清空）。
     * 事件计数可能漂移（跨维度 / 区块卸载 / 异常移除），由
     * {@link com.cimdy.coe.data.helper.skill.SummonCountHelper#reconcile} 每 100 tick 对账校正，不逐 tick 扫描。
     */
    public static final Supplier<AttachmentType<Integer>> SUMMONED_COUNT = register(
            "summoned_count", Codec.INT, 0, true, false);
    /**
     * 当前图腾数量：同 {@link #SUMMONED_COUNT} 的事件驱动口径，独立链路
     * （图腾走 {@link #SUMMONED_COUNT} 之外的计数，避免与 {@code MINION} 语义混淆）。
     * 真值口径 = 周围 64 格内归属为本实体的 {@code SkillTotem}。
     */
    public static final Supplier<AttachmentType<Integer>> TOTEM_COUNT = register(
            "totem_count", Codec.INT, 0, true, false);

    // 基地数据 存储在主世界Level 统一通过StrongholdHelper获取和修改
    public static final Supplier<AttachmentType<SaveData>> STRONGHOLD_DATA = registerSync(
            "hold_data", SaveData.CODEC, SaveData.EMPTY);
    public static final Supplier<AttachmentType<Stronghold>> STRONGHOLD = registerSync(
            "hold", Stronghold.CODEC, Stronghold.STREAM_CODEC, Stronghold.EMPTY);
    public static final Supplier<AttachmentType<PlayerEntity>> PLAYER_ENTITY = registerSync(
            "player_entity", PlayerEntity.CODEC, PlayerEntity.EMPTY);
    /**
     * 玩家进度运行时状态（等级/科技点/研究点/已解锁科技/任务进度/已授权工种/已用区块数）。
     * 服务端权威、同步到客户端（UI 需读等级与预算）、死亡保留（进度不随死亡清空）。
     */
    public static final Supplier<AttachmentType<PlayerProgress>> PLAYER_PROGRESS = register(
            "player_progress", PlayerProgress.CODEC, null, PlayerProgress.EMPTY, true, true);
    /**
     * 研究台运行状态（发起者 / 研究节点 / 已计时 tick），存于研究台方块实体。
     * 已同步：客户端界面需要读取当前进度绘制进度条。
     */
    public static final Supplier<AttachmentType<ResearchBench>> RESEARCH_BENCH = registerSync(
            "research_bench", ResearchBench.CODEC, ResearchBench.STREAM_CODEC, ResearchBench.EMPTY);
    public static final Supplier<AttachmentType<StrongholdEntity>> ENTITY_DATA = registerSync(
            "entity_data", StrongholdEntity.CODEC, StrongholdEntity.EMPTY);
    public static final Supplier<AttachmentType<StockSet>> STOCK_SET = registerSync(
            "stock_set", StockSet.CODEC, StockSet.STREAM_CODEC, StockSet.empty(1));
    public static final Supplier<AttachmentType<Detailed>> DETAILED = registerSync(
            "detailed", Detailed.CODEC, Detailed.STREAM_CODEC, Detailed.EMPTY);

    //实体数据
    public static final Supplier<AttachmentType<UUID>> OWNER_UUID = registerNotSync(
            "owner_uuid", UUIDUtil.CODEC, UUID.randomUUID());
    public static final Supplier<AttachmentType<Camp>> CAMP = registerSync(
            "camp", Camp.CODEC, Camp.NONE);
    /** 召唤物到期时间（游戏刻绝对时间，0 = 非限时召唤物），由 SpawnAction 写入、SummonEvent 消费。 */
    public static final Supplier<AttachmentType<Long>> MINION_EXPIRE = registerNotSync(
            "minion_expire", Codec.LONG, 0L);
    /**
     * 待复活队列（持有者侧）：每项记录一只「被击败的可复生召唤物」的重生信息（实体 id / 是否盟友 / 剩余 tick）。
     * 由 {@code SummonEvent} 在可复生召唤物死亡时写入、并在持有者每 tick 调度重生；任何一只被击败都会把
     * 全部项剩余时间重置为满（共享 / 联动延迟）。服务端权威、不同步、不随死亡复制。
     */
    public static final Supplier<AttachmentType<List<ReviveEntry>>> PENDING_REVIVES = registerNotSync(
            "pending_revives", ReviveEntry.CODEC.listOf(), List.of());
    /**
     * 可复生召唤物上次受击的游戏刻（用于「一段时间未受击后回血」判据，见 {@code SummonEvent} 的回血逻辑）。
     * 仅可复生召唤物在受击时写入；服务端权威、不同步、不随死亡复制。
     */
    public static final Supplier<AttachmentType<Long>> MINION_LAST_DAMAGED = registerNotSync(
            "minion_last_damaged", Codec.LONG, 0L);
    public static final Supplier<AttachmentType<AdditionData>> ADDITION_DATA = registerSync(
            "addition_data", AdditionData.CODEC, AdditionData.EMPTY);

    public static final Supplier<AttachmentType<EntityAttributeLimiter.Modifiers>> ENTITY_LIMITER_MODIFIER = registerNotSync(
            "entity_limiter_modifier", EntityAttributeLimiter.Modifiers.CODEC, EntityAttributeLimiter.Modifiers.EMPTY);
    public static final Supplier<AttachmentType<Integer>> LEVEL = registerInt(
            "level", 1);
    public static final Supplier<AttachmentType<Rarity>> RARITY = registerNotSync(
            "rarity", Rarity.CODEC, Rarity.NONE);

    public static final Supplier<AttachmentType<LivingModifier>> RARITY_DATA = registerNotSync(
            "rarity_data", LivingModifier.CODEC, LivingModifier.EMPTY);

    public static Supplier<AttachmentType<DamageConvert>> ENTITY_DAMAGE_CONVERT = registerNotSync(
            "entity_damage_convert", DamageConvert.CODEC, DamageConvert.EMPTY);

    public static final Supplier<AttachmentType<LivingMod.ModSet>> ENTITY_MOD = registerNotSync(
            "entity_mods", LivingMod.ModSet.CODEC, LivingMod.ModSet.EMPTY);

    /** 实体工作档案：基础工作速度 / 各工种适应度 / 突变标记（随实体同步，用于显示与求值）。 */
    public static final Supplier<AttachmentType<com.cimdy.coe.entity.work.WorkProfile>> WORK_PROFILE = registerSync(
            "work_profile", com.cimdy.coe.entity.work.WorkProfile.CODEC, com.cimdy.coe.entity.work.WorkProfile.EMPTY);

    // 自定义属性系统：统一 stat 存储（属性亚型 + 触发亚型，全量重建，服务端权威、不同步）
    public static final Supplier<AttachmentType<StatStore>> STAT_STORE = registerNotSync(
            "stat_store", StatStore.CODEC, StatStore.empty());
    // 幼体标记 CHILD 已迁出本类，改存 AdditionData（key = AdditionKeys.CHILD，默认 false）。
    //
    // 技能伴生召唤物标记 IS_COMPANION 已迁出本类，改存
    // AdditionData（key = AdditionKeys.IS_COMPANION，默认 false）：
    // 区别于普通召唤物（coe:summoned）的特殊召唤生物，由 SpawnAction 在 companion=true 时写入，
    // 供 TargetSelector（COMPANION 源）与 PresenceCountValue.COMPANION 解析，
    // 解锁「Companions have X% …」整族 buff；盟友同时被强制为永久召唤物
    // （跳过 MINION_EXPIRE 写入 → 触发 SummonEvent 复活机制）。
    //
    // 据点盟友标记 ALLY 已迁出本类，改存 AdditionData（key = AdditionKeys.ALLY，默认 false）：
    // 玩家在据点终端里对其已捕获实体显式指定的帕鲁式随行伙伴标记，与本条 IS_COMPANION 是两条独立链路。
    //   存储态：落在其 SaveEntity 记录的 ally 字段上；
    //   世界态：实体被放出时由 WorkerAIInjector.spawnPlayerEntity 从存档记录同步到本键，
    //           回收时由 SaveEntity.of 反向写回存档记录，标记不会在「放出↔回收」之间丢失；
    //   效果：TaskDispatcher#canWork 对本键为 true 的实体返回 false → 永不派工，
    //         已派任务自动回退 PENDING（无需另写撤工逻辑）。
    //
    // 据点实体「战斗 ↔ 工作」开关 COMBATANT 已迁出本类，改存
    // AdditionData（key = AdditionKeys.COMBATANT，默认 true）：默认 true —— 据点遭入侵时
    // 工人 / 盟友仍须防御；玩家可在据点终端把特定实体设为 false，使其专注工作、不主动出击。
    // WorkerAIInjector / 盟友分支据此条件注入 FactionDefendGoal（阵营敌对则主动出击、同阵营 / 中立不误伤），
    // 与 CAMP 配合由 CampAllianceMixin 门控。

    // 实现
    // 属性限制器
    // 已通过 registerSync 同步到客户端；客户端由 LivingAttributeValue mixin 在
    // getAttributeValue 返回值时 clamp（见 AttributeLimiters.Cache.clampValue）。
    public static final Supplier<AttachmentType<AttributeLimiters>> ATTRIBUTE_LIMITERS = registerSync(
            "attribute_limiters", AttributeLimiters.CODEC, AttributeLimiters.EMPTY);
    public static final Supplier<AttachmentType<AttributeLimiters.Save>> LIMITERS_SAVE = registerSync(
            "limiters_save", AttributeLimiters.Save.CODEC, AttributeLimiters.Save.EMPTY);

    // 能量护盾 魔力
    /** 能量护盾（ES）当前值：上限 = MAX_ENERGY_SHIELD 属性（装备三围 energy 映射）；承伤吸收层消费
     *  （ResourceRegistry.absorbEnergyShield）。 */
    public static final Supplier<AttachmentType<Float>> ENERGY_SHIELD = registerSyncFloat(
            "energy_shield", 0F);
    public static final Supplier<AttachmentType<Float>> RUNIC_WARD = registerSyncFloat(
            "runic_ward", 0F);
    public static final Supplier<AttachmentType<Float>> MANA = registerSyncFloat(
            "mana", 0F);
    public static final Supplier<AttachmentType<Float>> SPIRIT = registerSyncFloat(
            "spirit", 0F);
    /**
     * 已保留的精魂量（光环 reservation 合计）。
     * {@code ReservationManager} 是纯服务端静态表，客户端读不到；这里把同一份数值同步下来，
     * 供客户端判定「精魂不足」（技能装配界面的不可用标记、HUD 的红色遮罩）。
     */
    public static final Supplier<AttachmentType<Float>> RESERVED_SPIRIT = registerSyncFloat(
            "reserved_spirit", 0F);
    /** 怒气（Rage）资源：POE2 怒气机制当前值，默认 0，上限由属性 MAX_RAGE（默认 50）约束。 */
    public static final Supplier<AttachmentType<Float>> RAGE = registerSyncFloat(
            "rage", 0F);
    /** 波动（Volatility）资源：当前积攒值，默认 0，上限由属性 MAX_VOLATILITY（默认 100）约束。 */
    public static final Supplier<AttachmentType<Float>> VOLATILITY = registerSyncFloat(
            "volatility", 0F);
    /** 黑暗（Darkness）资源当前值：41076 Embrace the Darkness（Acolyte of Chayula）引入的独立承伤预留资源。
     *  上限 = (100 + 10×等级) × MAX_DARKNESS 倍率；承伤优先从本池「预留」（5 秒后返还），
     *  溢出部分才扣生命/能量护盾。启用开关 = NoSpiritBaseDarkness 属性 >1.0。 */
    public static final Supplier<AttachmentType<Float>> DARKNESS = registerSyncFloat(
            "darkness", 0F);
    /** 黑暗预留返还队列（{@code Value}：amount, 剩余 tick）：承伤预留后按 DarknessReservationLasts 时长逐 tick 返还。 */
    public static final Supplier<AttachmentType<List<Value>>> DARKNESS_RESERVATION = registerNotSync(
            "darkness_reservation", Value.CODEC.listOf(), List.of());
    // 闪避 格挡(重度眩晕) 护甲破坏
    public static final Supplier<AttachmentType<EvasionEntropy>> EVASION_ENTROPY = registerNotSync(
            "evasion_entropy", EvasionEntropy.CODEC, EvasionEntropy.EMPTY);
    /** 护甲破坏值（同步到客户端，供 HUD 渲染读取精确数值）。 */
    public static final Supplier<AttachmentType<Float>> ARMOUR_BREAK_DISPLAY = registerSyncFloat(
            "armour_break_display", 0F);
    /** 诅咒激活延迟待施队列（施法者侧）：每项记录一次延迟施放诅咒的元数据与计划触发 tick；由 CurseActivation#tick 每轮消费。服务端权威、不同步、不随死亡复制。 */
    public static final Supplier<AttachmentType<List<CursePending>>> CURSE_PENDING = registerNotSync(
            "curse_pending", CursePending.CODEC.listOf(), List.of());
    /** 最近施放的诅咒技能 id（字符串）：供「引导 N 秒后施加诅咒」类机制（ADD_A_CURSE_AFTER_CHANNELLING_FOR_SECONDS）
     * 取用玩家上次施放的诅咒，于引导达阈值后对附近敌人重放 applyCurseToNearby。服务端权威、不同步、不随死亡复制。 */
    public static final Supplier<AttachmentType<String>> LAST_CAST_CURSE = registerNotSync(
            "last_cast_curse", Codec.STRING, "");

    // 偷取
    public static final Supplier<AttachmentType<LeechValue>> LEECH = registerNotSync(
            "leech", LeechValue.CODEC, LeechValue.EMPTY);

    // 补给（Recoup：受到伤害后随时间恢复生命/魔力/能量）
    public static final Supplier<AttachmentType<RecoupValue>> RECOUP = registerNotSync(
            "recoup", RecoupValue.CODEC, RecoupValue.EMPTY);

    /** 随机类型效果（RandomConvertStatEffect）CYCLE 模式的轮流计数器：{@code 条目来源键 → 当前索引}。
     *  每次伤害结算经 StatEngine.damageConvert 读后 +1 写回，使各随机条目独立顺序轮换候选类型。
     *  服务端权威、不同步、不随死亡复制（随机轮换是战斗期瞬时状态）。 */
    public static final Supplier<AttachmentType<Map<String, Integer>>> RANDOM_CONVERT_CYCLE = registerNotSync(
            "random_convert_cycle", Codec.unboundedMap(Codec.STRING, Codec.INT), Map.of());

    /** 「被你点燃过的敌人」累计点燃秒数：{@code 施加者 UUID → 累计秒数}（node 43939）。
     *  由 {@code StatusBehavior#dealSplitDoT} 在每次 IGNITE 状态 tick 时按施加者累加 0.05s；
     *  消费端 {@code DamagePipeline#applyStatusDamageTakenModifiers} 读「当前攻击者」的累计秒数放大火焰成分。
     *  服务端权威、不同步、不随死亡复制（战斗期瞬时历史）。 */
    public static final Supplier<AttachmentType<Map<UUID, Float>>> IGNITE_HISTORY = registerNotSync(
            "ignite_history", Codec.unboundedMap(UUIDUtil.CODEC, Codec.FLOAT), Map.of());

    /** 「调用技能改为每 X 秒触发链接法术」的周期倒计时：{@code 技能id#动作序号 → 剩余 tick}（node 11230）。
     *  由 {@code InvocationTicker#tickAll} 每 tick 递减、归零时触发元技能链接的法术并重置为周期；
     *  技能摘除后其键随本轮未使用被剪除。服务端权威、不同步、不随死亡复制（战斗期瞬时计时）。 */
    public static final Supplier<AttachmentType<Map<String, Integer>>> INVOCATION_TIMERS = registerNotSync(
            "invocation_timers", Codec.unboundedMap(Codec.STRING, Codec.INT), Map.of());

    // 技能临时条目（timed modifier buff）到期统一走 StatStore 内建计时域
    // （addModifier(key, entry, ticks) + StatStore.tick()，expiries 随附件序列化）；
    // 计时窗口（kill / crit / taken_hit / cause_damage，统一事件名）走 ExpressionContext 的共享 timer Map，
    // 由 tickStore() 每 tick 递增、超 20s 存活上限移除（context_dsl_design.md §7.1）。

    // 异常状态存储（BLEED/IGNITE/POISON/SHOCK/FREEZE/ELECTROCUTE 均已迁至 StatusEngine.StatusStore + buildup）

    // 连击追踪（按 comboId 分槽，服务端权威、不同步）
    public static final Supplier<AttachmentType<ResourceRegistry.ComboSave>> COMBO_TRACKER = registerNotSync(
            "combo_tracker", ResourceRegistry.ComboSave.CODEC, ResourceRegistry.ComboSave.EMPTY);
    // 荣耀追踪（按 gloryId 分槽，服务端权威、不同步）
    public static final Supplier<AttachmentType<ResourceRegistry.GlorySave>> GLORY_TRACKER = registerNotSync(
            "glory_tracker", ResourceRegistry.GlorySave.CODEC, ResourceRegistry.GlorySave.EMPTY);
    // 元技能能量（参考荣耀：按 metaSkillId 分槽；服务端权威、不同步）
    public static final Supplier<AttachmentType<ResourceRegistry.MetaEnergySave>> META_ENERGY_TRACKER = registerNotSync(
            "meta_energy_tracker", ResourceRegistry.MetaEnergySave.CODEC, ResourceRegistry.MetaEnergySave.EMPTY);
    /**
     * 封印（Seal）运行时状态：{@code seals / charge / maxSeals / gainEveryCastTimeMultiplier /
     * repeatPerSealBroken / areaIncPerSealBroken}。由 {@code SkillInstance} 在封印技能施放时读写，
     * 驱动封印累积与释放重放。服务端权威、不同步、不随死亡复制（重生后封印清空）。
     */
    public static final Supplier<AttachmentType<SealState.Save>> SEAL_STATE = registerNotSync(
            "seal_state", SealState.Save.CODEC, SealState.Save.EMPTY);
    /**
     * 元素执政官充能进度：累计已消耗魔力（浮点），达「X% 最大魔力」阈值即授予执政官并扣减阈值（余量结转）。
     * 由 {@code ManaSpentArchonStatEffect} 读写（RESOURCE_SPEND 事件驱动）。
     * 服务端权威、不同步、不随死亡复制（战斗期瞬时进度，重生清零）。
     */
    public static final Supplier<AttachmentType<Float>> ARCHON_MANA_PROGRESS = registerNotSync(
            "archon_mana_progress", Codec.FLOAT, 0F);
    // ------ //
    private static Supplier<AttachmentType<Integer>> registerInt(String name, int defaultValue) {
        return register(name, Codec.INT, defaultValue, false, true);
    }

    private static Supplier<AttachmentType<Float>> registerFloat(String name) {
        return registerFloat(name, 0F);
    }

    private static Supplier<AttachmentType<Float>> registerFloat(String name, float defaultValue) {
        return register(name, Codec.FLOAT, defaultValue, false, true);
    }

    private static Supplier<AttachmentType<Float>> registerSyncFloat(String name, float defaultValue) {
        return register(name, Codec.FLOAT, defaultValue, true, true);
    }

    private static <T> Supplier<AttachmentType<T>> registerNotSync(String name, Codec<T> codec, T defaultValue) {
        return register(name, codec, defaultValue, false, false);
    }

    private static <T> Supplier<AttachmentType<T>> registerSync(String name, Codec<T> codec, T defaultValue) {
        return register(name, codec, defaultValue, true, false);
    }

    private static <T> Supplier<AttachmentType<T>> registerSync(
            String name, Codec<T> codec, StreamCodec<RegistryFriendlyByteBuf, T> streamCodec, T defaultValue) {
        return register(name, codec, streamCodec, defaultValue, true, false);
    }

    private static <T> Supplier<AttachmentType<T>> register(
            String name, Codec<T> codec, T defaultValue, boolean sync, boolean copyOnDeath) {
        return register(name, codec, null, defaultValue, sync, copyOnDeath);
    }

    public static <T> Supplier<AttachmentType<T>> register(
            String name, Codec<T> codec, StreamCodec<RegistryFriendlyByteBuf, T> streamCodec, T defaultValue,
            boolean sync, boolean copyOnDeath) {
        AttachmentType.Builder<T> builder = AttachmentType.builder(() -> defaultValue).serialize(codec.fieldOf(name));
        if (sync) builder.sync(streamCodec == null ? ByteBufCodecs.fromCodec(codec) : streamCodec);
        if (copyOnDeath) builder.copyOnDeath();
        return ATTACHMENTS.register(name, builder::build);
    }
}
