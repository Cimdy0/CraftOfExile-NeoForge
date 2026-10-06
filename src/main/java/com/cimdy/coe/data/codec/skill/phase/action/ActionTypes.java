package com.cimdy.coe.data.codec.skill.phase.action;

import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.data.codec.skill.phase.action.charge.ChargeAction;
import com.cimdy.coe.data.codec.skill.phase.action.combo.ComboAction;
import com.cimdy.coe.data.codec.skill.phase.action.combo.DeliveryAction;
import com.cimdy.coe.data.codec.skill.phase.action.control.ChanceAction;
import com.cimdy.coe.data.codec.skill.phase.action.control.ConditionalAction;
import com.cimdy.coe.data.codec.skill.phase.action.control.EmitEventAction;
import com.cimdy.coe.data.codec.skill.phase.action.control.TriggerSkillAction;
import com.cimdy.coe.data.codec.skill.phase.action.damage.*;
import com.cimdy.coe.data.codec.skill.phase.action.movement.ConsumeCorpseAction;
import com.cimdy.coe.data.codec.skill.phase.action.movement.CreateGroundAction;
import com.cimdy.coe.data.codec.skill.phase.action.movement.MoveAction;
import com.cimdy.coe.data.codec.skill.phase.action.movement.SpawnAction;
import com.cimdy.coe.data.codec.skill.phase.action.support.*;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.RegistryBuilder;

public class ActionTypes {
    public static final Identifier RESOURCE_LOCATION = Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "skill_action");
    public static final ResourceKey<Registry<Action.Type<?>>> KEY = ResourceKey.createRegistryKey(RESOURCE_LOCATION);
    public static final Registry<Action.Type<?>> REGISTRY = new RegistryBuilder<>(KEY).sync(true).defaultKey(RESOURCE_LOCATION).maxId(256).create();

    public static final Action.Type<DamageAction> DAMAGE = register("damage", DamageAction.CODEC);
    public static final Action.Type<ModifierAction> MODIFIER = register("modifier", ModifierAction.CODEC);
    public static final Action.Type<SpawnAction> SPAWN = register("spawn", SpawnAction.CODEC);
    public static final Action.Type<MoveAction> MOVE = register("move", MoveAction.CODEC);
    public static final Action.Type<TriggerSkillAction> TRIGGER_SKILL = register("trigger_skill", TriggerSkillAction.CODEC);
    public static final Action.Type<ConditionalAction> CONDITIONAL = register("conditional", ConditionalAction.CODEC);
    /** 几率门控：按几率（数值，可写常量或读属性）决定是否执行一次内部动作链；掷骰走 Java 侧（表达式系统刻意无随机原语）。 */
    public static final Action.Type<ChanceAction> CHANCE = register("chance", ChanceAction.CODEC);
    /** 投递：在动作执行点按需生成一次投递物（投射物/范围/残片/危害物…），使「事件 → 投递物」类效果可在事件回调中产出。 */
    public static final Action.Type<DeliveryAction> DELIVERY = register("delivery", DeliveryAction.CODEC);
    public static final Action.Type<EmitEventAction> EMIT_EVENT = register("emit_event", EmitEventAction.CODEC);
    /** 充能球生成/消耗/刷新 */
    public static final Action.Type<ChargeAction> CHARGE = register("charge", ChargeAction.CODEC);
    /** 连击构建/消耗 */
    public static final Action.Type<ComboAction> COMBO = register("combo", ComboAction.CODEC);
    /** 元技能（Cast on X）触发 */
    public static final Action.Type<MetaSkillAction> META_SKILL = register("meta_skill", MetaSkillAction.CODEC);
    /** 直接使用 Stat：向接收者 StatStore 追加条目 / 立即施加触发型效果（作用域标签/条件即时聚合） */
    public static final Action.Type<StatAction> STAT = register("stat", StatAction.CODEC);
    /** 创建地面（Ground 子系统）：登记 GroundFieldManager 地面场，对站立实体刷新存在性标记 */
    public static final Action.Type<CreateGroundAction> CREATE_GROUND = register("create_ground", CreateGroundAction.CODEC);
    /** 创建战旗（Banner 子系统）：投放战旗实体 SkillBanner，对区域内友方刷新增益（离开后残留）。 */
    public static final Action.Type<BannerAction> BANNER = register("banner", BannerAction.CODEC);
    /** 消耗尸体（Corpse 子系统）：消耗尸体、按概率保留、授予「近期消耗」窗口增益。 */
    public static final Action.Type<ConsumeCorpseAction> CONSUME_CORPSE = register("consume_corpse", ConsumeCorpseAction.CODEC);
    /** 消费异常（Ailment 转存）：消费目标/自身异常并记账到施法者 TransferStore，供后续阶段跨阶段取用（P6 转存通道消费方）。 */
    public static final Action.Type<ConsumeAilmentAction> CONSUME_AILMENT = register("consume_ailment", ConsumeAilmentAction.CODEC);    public static final Action.Type<InfusionRemnantAction> INFUSION_REMNANT =
            register("infusion_remnant", InfusionRemnantAction.CODEC);
    /** 奉献（Offer an Offering）：牺牲骷髅召唤物 → 生成伤害型随从「尖刺」→ 施加奉献增益（Limit 1）。 */
    public static final Action.Type<OfferingAction> OFFERING = register("offering", OfferingAction.CODEC);

    public static <T extends Action> Action.Type<T> register(String name, Codec<T> codec) {
        return Registry.register(REGISTRY,
                Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "action_" + name),
                new Action.Type<>(codec));
    }
}
