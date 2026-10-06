package com.cimdy.coe.data.codec.entity.trigger;

import com.cimdy.coe.data.codec.entity.EntityDef;

import com.cimdy.coe.data.codec.entity.render.EntityRenderTypes;
import com.cimdy.coe.data.codec.skill.phase.action.combo.DeliveryAction;
import com.cimdy.coe.CraftOfExile;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.RegistryBuilder;

/**
 * 道具触发效果的分发注册表 —— 写法与 {@code ActionTypes} / {@code DeliveryTypes} / {@code EntityRenderTypes} 对齐。
 *
 * <p>注册名即 JSON 的派发键（如 {@code "phase_on_hit"}），多个效果按 {@code EntityDef.on_trigger}
 * 数组顺序执行。
 */
public class EntityTriggerTypes {
    public static final Identifier RESOURCE_LOCATION =
            Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "entity_trigger");
    public static final ResourceKey<Registry<EntityTrigger.Type<?>>> KEY =
            ResourceKey.createRegistryKey(RESOURCE_LOCATION);

    public static final Registry<EntityTrigger.Type<?>> REGISTRY =
            new RegistryBuilder<>(KEY).sync(true).defaultKey(RESOURCE_LOCATION).maxId(64).create();

    /** 点燃所属阶段的 ON_HIT（效果由技能数据 actions 定义）：所有残片/危害物的核心行为。 */
    public static final EntityTrigger.Type<EntityTriggers.PhaseOnHit> PHASE_ON_HIT =
            register("phase_on_hit", EntityTriggers.PhaseOnHit.CODEC);
    /** 发射事件（owner 可为 target/caster）：残片拾取 emit collect_remnant 即走此通道。 */
    public static final EntityTrigger.Type<EntityTriggers.EmitEvent> EMIT_EVENT =
            register("emit_event", EntityTriggers.EmitEvent.CODEC);
    /** 记录计时事件（on 可为 caster/target）：危害物记录 hazard_triggered（「recently」窗口）走此。 */
    public static final EntityTrigger.Type<EntityTriggers.RecordEvent> RECORD_EVENT =
            register("record_event", EntityTriggers.RecordEvent.CODEC);
    /** 施加状态（on 可为 target/caster）：危害物对穿越目标施 hazard_mark 走此。 */
    public static final EntityTrigger.Type<EntityTriggers.ApplyStatus> APPLY_STATUS =
            register("apply_status", EntityTriggers.ApplyStatus.CODEC);
    /** 实体触发一次投递物（实体侧 DeliveryAction）：残迹/危害物到期/触发时经此生成残片/投射物/范围等，复用 DeliveryExecutors。 */
    public static final EntityTrigger.Type<EntityTriggers.Deliver> DELIVER =
            register("deliver", EntityTriggers.Deliver.CODEC);
    /** 一次性爆发粒子（实体侧）：复用 EntityRenderLayer，触发瞬间各层各发一次（不经 interval 节流）。 */
    public static final EntityTrigger.Type<EntityTriggers.EmitVfx> EMIT_VFX =
            register("emit_vfx", EntityTriggers.EmitVfx.CODEC);
    /** 触发一次音效（实体侧）：于实体位置播放一条原版/已注册 SoundEvent（无数据则不发声）。 */
    public static final EntityTrigger.Type<EntityTriggers.PlaySound> PLAY_SOUND =
            register("play_sound", EntityTriggers.PlaySound.CODEC);

    public static <T extends EntityTrigger> EntityTrigger.Type<T> register(String name, Codec<T> codec) {
        return Registry.register(REGISTRY,
                Identifier.fromNamespaceAndPath(CraftOfExile.MODID, name),
                new EntityTrigger.Type<>(codec));
    }
}
