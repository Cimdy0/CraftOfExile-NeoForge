package com.cimdy.coe.data.codec.support.stat.entry;

import com.cimdy.coe.data.codec.support.math.ComparisonOperator;
import com.cimdy.coe.data.codec.support.math.context.nodes.AttributeValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.CompareValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.ConstantValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.EquipmentValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.AccessoryValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.EntityFlagValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.DistanceToNearestEnemyValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.HealthValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.VarRef;
import com.cimdy.coe.data.codec.support.math.context.nodes.EventHistoryValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.MovementValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.StatusValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.SourceScopeNode;
import com.cimdy.coe.data.codec.support.math.context.nodes.PresenceCountValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.TimeValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.TimerValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.SkillStateValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.SkillUsingValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.PresenceRarityValue;
import com.cimdy.coe.data.codec.support.Rarity;

import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import net.minecraft.world.entity.EquipmentSlot;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * A-线（条件覆盖）回归夹具：CompareValue 把左右子树渲染成名词短语 + 比较符值键，
 * 组合为 stat.condition.compare，不再整条降级成 [!] 条件。
 *
 * <p>覆盖的叶子节点（名词短语）：ConstantValue（数字）、AttributeValue（复用 attribute.name.*）、
 * HealthValue（stat.value.resource.*）、DistanceToNearestEnemyValue（stat.value.src.*）、
 * VarRef（stat.value.var.*）。未命名节点可见降级到 stat.quantity.unknown（[?]）。
 *
 * <p>为避免在纯 JUnit 环境触发 Minecraft 注册表 bootstrap，本测试直接构造等价节点，
 * 并把一份 en_us 映射注入为当前 Language 单例（仅含本测试用到的键）。
 */
class StatConditionCoverageTest {

    private static final Map<String, String> EN_US = Map.ofEntries(
            Map.entry("stat.condition.compare", "%1$s %2$s %3$s"),
            Map.entry("stat.quantity.unknown", "[?]"),
            Map.entry("stat.value.cmp.eq", "="),
            Map.entry("stat.value.cmp.ne", "!="),
            Map.entry("stat.value.cmp.lt", "<"),
            Map.entry("stat.value.cmp.le", "<="),
            Map.entry("stat.value.cmp.gt", ">"),
            Map.entry("stat.value.cmp.ge", ">="),
            // HealthValue 以 flat-first 直发 stat.value.resource.energy（StatValueKey.of 无别名层）；
            // 权威 en_us.json 中 resource.energy 与 equip_out.energy_shield 两键同值（均为 "Energy Shield"），
            // 故「当前能量护盾」与「装备产出能量护盾」共用同一名词短语，本夹具两者都注入以贴合线上。
            Map.entry("stat.value.resource.energy", "Energy Shield"),
            Map.entry("stat.value.equip_out.energy_shield", "Energy Shield"),
            // canonical：resource.health → 外部权威裸键 stat.health
            Map.entry("stat.health", "Life"),
            Map.entry("stat.value.src.dist_nearest_enemy", "Nearest Enemy"),
            Map.entry("stat.value.var.stat_range_m", "Range"),
            Map.entry("attribute.name.strength", "Strength"),
            Map.entry("stat.condition.hit_state", "when you %s"),
            Map.entry("stat.value.hit_state.is_entity", "hit an entity"),
            Map.entry("stat.value.hit_state.is_block", "hit a block"),
            Map.entry("stat.value.hit_state.is_miss", "miss"),
            Map.entry("stat.condition.charge", "while you have %s"),
            Map.entry("stat.condition.current_event", "when the event is %s"),
            Map.entry("stat.condition.current_event_tag", "when the event is %1$s (%2$s)"),
            // canonical：event.crit / cond_tag.crit / damage.crit 归一到外部权威裸键 stat.crit
            Map.entry("stat.crit", "Crit"),
            Map.entry("stat.condition.status_age", "%1$s for %2$s"),
            // canonical：status.ignite 归一到外部权威裸键 stat.ignite
            Map.entry("stat.ignite", "Ignite"),
            Map.entry("stat.condition.damage", "when dealing %s"),
            Map.entry("stat.value.damage.fire", "Fire Damage"),
            Map.entry("stat.condition.gem", "when socketed with %s"),
            Map.entry("stat.value.gem.fireball", "Fireball"),
            Map.entry("stat.condition.presence.entered", "when a target recently entered"),
            Map.entry("stat.condition.presence.left", "when a target recently left"),
            Map.entry("stat.value.presence.entered", "entered"),
            Map.entry("stat.value.presence.left", "left"),
            Map.entry("stat.value.gem.any", "Socketed Gem"),
            Map.entry("stat.value.movement.distance", "Distance"),
            // canonical：presence_count 复合键拆成 rel / kind / measure 三个独立原子键
            // （measure=count 走本命名空间原子键 presence_count.measure.count，权威值为 "Count"）
            Map.entry("stat.value.presence_count.rel.ally", "Ally"),
            Map.entry("stat.value.presence_count.kind.totem", "Totem"),
            Map.entry("stat.value.presence_count.measure.count", "Count"),
            Map.entry("stat.number", "Count"),
            Map.entry("stat.value.time.day", "Day"),
            // timer 命名空间已补注册，键生效（此前为 humanize 降级）
            Map.entry("stat.value.timer.burn", "Burn Timer"),
            Map.entry("stat.value.skill_state.skill.level", "Skill Level"),
            // canonical：skill_using 经 NS_REMAP 整命名空间重定向到 using_skill
            Map.entry("stat.value.using_skill.fireball", "Fireball"),
            // canonical：稀有度复合键拆成 rarity + scope 两个独立键
            // （presence_rarity.scope.enemy 经 ALIAS 归一到 stat.enemy）
            Map.entry("stat.value.presence_rarity.rarity.rare_unique", "Rare or Unique"),
            Map.entry("stat.enemy", "Enemy"),
            // 批 2b：装备 / 饰品 / 实体标记 门控覆盖
            Map.entry("stat.value.equip_slot.head", "Head"),
            // canonical：equip_out.armor → 外部权威裸键 stat.armor
            Map.entry("stat.armor", "Armor"),
            Map.entry("stat.value.accessory_group.legs", "Legs"),
            Map.entry("stat.value.equip_out.evasion", "Evasion"),
            Map.entry("stat.condition.accessory", "while %s is equipped"),
            Map.entry("stat.value.flag.summoned", "Summoned"),
            Map.entry("stat.value.flag.owner", "Owner"),
            // canonical：flag.level → 外部权威裸键 stat.level
            Map.entry("stat.level", "Level"),
            Map.entry("stat.condition.flag", "if %s"),
            Map.entry("stat.condition.biome", "in %s"),
            Map.entry("stat.condition.dimension", "in %s")
    );

    @BeforeAll
    static void installLanguage() {
        injectLanguage(EN_US);
    }

    @AfterAll
    static void restoreLanguage() {
        injectLanguage(Map.of());
    }

    // ---------------------------------------------------------------- CompareValue 组合

    @Test
    void compareHealthGreaterThanZero() {
        // 等价 has_energy_shield.json：{compare, left:{type:health,mode:energy}, op:>, right:{const:0}}
        CompareValue c = new CompareValue(new HealthValue("energy"), new ConstantValue(0.0),
                1.0, 1.0, ComparisonOperator.GREATER_THAN);
        assertEquals("Energy Shield > 0", c.describeFragment().get(0).getString());
    }

    @Test
    void compareDistanceLessThanOrEqualRange() {
        // 等价 within_range_of_enemy.json：{compare, left:{type:dist_nearest_enemy}, op:<=, right:{var:stat_range_m}}
        CompareValue c = new CompareValue(new DistanceToNearestEnemyValue(),
                new VarRef("stat_range_m"), 1.0, 1.0, ComparisonOperator.LESS_THAN_OR_EQUAL);
        assertEquals("Nearest Enemy <= Range", c.describeFragment().get(0).getString());
    }

    @Test
    void compareAttributeLessThanConstant() {
        CompareValue c = new CompareValue(
                new AttributeValue(Identifier.fromNamespaceAndPath("coe", "strength")),
                new ConstantValue(50.0), 1.0, 1.0, ComparisonOperator.LESS_THAN);
        assertEquals("Strength < 50", c.describeFragment().get(0).getString());
    }

    @Test
    void compareDoesNotDegradeToUnknownCondition() {
        // 已覆盖的节点产出可见文案，绝不落入 [!] condition。
        CompareValue c = new CompareValue(new HealthValue("energy"), new ConstantValue(0.0),
                1.0, 1.0, ComparisonOperator.GREATER_THAN);
        assertFalse(c.describeFragment().get(0).getString().contains("[!]"),
                "CompareValue 不应降级成 [!] 条件");
    }

    // ---------------------------------------------------------------- 名词短语降级

    @Test
    void unNamedOperandDegradesToQuantityUnknown() {
        // 左右任一侧无可读名称时，单个操作数可见降级到 stat.quantity.unknown（[?]），而非整条丢语义。
        // 用没有 describeQuantity 的节点（此处以仅 describeFragment 的 StatusValue 同族思路，
        // 构造一个未覆盖节点：ConstantValue 已覆盖，故用一个确实无 describeQuantity 的节点）。
        // 直接用 ContextNode 默认实现验证降级契约：
        com.cimdy.coe.data.codec.support.math.context.ContextNode unknown =
                new com.cimdy.coe.data.codec.support.math.context.ContextNode() {
                    @Override
                    public double evaluate(com.cimdy.coe.data.codec.support.math.context.ContextScope scope) {
                        return 0;
                    }
                };
        CompareValue c = new CompareValue(unknown, new ConstantValue(0.0),
                1.0, 1.0, ComparisonOperator.GREATER_THAN);
        assertEquals("[?] > 0", c.describeFragment().get(0).getString());
    }

    @Test
    void constantQuantityFormatsNumber() {
        assertEquals("0", new ConstantValue(0.0).describeQuantity(Locale.ROOT).get(0).getString());
        assertEquals("15", new ConstantValue(15.0).describeQuantity(Locale.ROOT).get(0).getString());
    }

    // ---------------------------------------------------------------- 批 3a：操作数名词短语覆盖

    @Test
    void eventHistoryQuantityReturnsEventValueKey() {
        // EventHistoryValue 作 CompareValue 操作数时，应产出 stat.value.event.<name> 名词短语而非 [?]。
        assertEquals("Crit", EventHistoryValue.count("crit").describeQuantity(Locale.ROOT).get(0).getString());
    }

    @Test
    void movementDistanceQuantityReturnsMovementValueKey() {
        assertEquals("Distance", new MovementValue("distance", 20.0).describeQuantity(Locale.ROOT).get(0).getString());
    }

    @Test
    void statusQuantityReturnsStatusValueKey() {
        assertEquals("Ignite", new StatusValue("ignite", Identifier.fromNamespaceAndPath("coe", "ignite"))
                .describeQuantity(Locale.ROOT).get(0).getString());
    }

    @Test
    void sourceScopeDelegatesQuantityToInner() {
        // SourceScopeNode 必须透传 inner 的 describeQuantity，否则被包裹的数值节点在比较里降级成 [?]。
        assertEquals("Energy Shield",
                new SourceScopeNode(new HealthValue("energy")).describeQuantity(Locale.ROOT).get(0).getString());
    }

    @Test
    void compareEventHistoryCountGreaterThanZero() {
        // 等价 {compare, left:{type:event_history, name:crit, mode:count}, op:>, right:{const:0}}
        CompareValue c = new CompareValue(EventHistoryValue.count("crit"), new ConstantValue(0.0),
                1.0, 1.0, ComparisonOperator.GREATER_THAN);
        assertEquals("Crit > 0", c.describeFragment().get(0).getString());
    }

    @Test
    void compareStatusGreaterThanZero() {
        CompareValue c = new CompareValue(
                new StatusValue("ignite", Identifier.fromNamespaceAndPath("coe", "ignite")),
                new ConstantValue(0.0), 1.0, 1.0, ComparisonOperator.GREATER_THAN);
        assertEquals("Ignite > 0", c.describeFragment().get(0).getString());
    }

    @Test
    void compareMovementDistanceGreaterThanZero() {
        CompareValue c = new CompareValue(new MovementValue("distance", 20.0), new ConstantValue(0.0),
                1.0, 1.0, ComparisonOperator.GREATER_THAN);
        assertEquals("Distance > 0", c.describeFragment().get(0).getString());
    }

    @Test
    void compareSourceScopeHealthGreaterThanZero() {
        CompareValue c = new CompareValue(new SourceScopeNode(new HealthValue("energy")), new ConstantValue(0.0),
                1.0, 1.0, ComparisonOperator.GREATER_THAN);
        assertEquals("Energy Shield > 0", c.describeFragment().get(0).getString());
    }

    // ---------------------------------------------------------------- 批 3b：操作数名词短语覆盖（CompareValue 操作数）

    @Test
    void presenceCountQuantityReturnsPresenceCountValueKey() {
        // rel + kind + measure 三原子键组合（measure=count → presence_count.measure.count = "Count"）
        assertEquals("Ally Totem Count", new PresenceCountValue(PresenceCountValue.Relation.ALLY,
                PresenceCountValue.TOTEM, false).describeQuantity(Locale.ROOT).get(0).getString());
    }

    @Test
    void timeDayQuantityReturnsTimeValueKey() {
        assertEquals("Day", new TimeValue("time.day").describeQuantity(Locale.ROOT).get(0).getString());
    }

    @Test
    void timerBurnQuantityReturnsTimerValueKey() {
        assertEquals("Burn Timer", new TimerValue("burn", 999999.0).describeQuantity(Locale.ROOT).get(0).getString());
    }

    @Test
    void skillStateLevelQuantityReturnsSkillStateValueKey() {
        assertEquals("Skill Level", new SkillStateValue("skill.level").describeQuantity(Locale.ROOT).get(0).getString());
    }

    @Test
    void skillUsingFireballQuantityReturnsSkillUsingValueKey() {
        assertEquals("Fireball", new SkillUsingValue(Identifier.fromNamespaceAndPath("coe", "fireball"))
                .describeQuantity(Locale.ROOT).get(0).getString());
    }

    @Test
    void presenceRarityCountQuantityReturnsPresenceRarityValueKey() {
        // rarity 原子键 + scope.enemy（→ stat.enemy = "Enemy"）两键组合
        assertEquals("Rare or Unique Enemy",
                new PresenceRarityValue(List.of(Rarity.RARE, Rarity.UNIQUE), true, true)
                        .describeQuantity(Locale.ROOT).get(0).getString());
    }

    @Test
    void comparePresenceCountGreaterThanZero() {
        CompareValue c = new CompareValue(
                new PresenceCountValue(PresenceCountValue.Relation.ALLY, PresenceCountValue.TOTEM, false),
                new ConstantValue(0.0), 1.0, 1.0, ComparisonOperator.GREATER_THAN);
        assertEquals("Ally Totem Count > 0", c.describeFragment().get(0).getString());
    }

    @Test
    void compareTimeDayGreaterThanZero() {
        CompareValue c = new CompareValue(new TimeValue("time.day"), new ConstantValue(0.0),
                1.0, 1.0, ComparisonOperator.GREATER_THAN);
        assertEquals("Day > 0", c.describeFragment().get(0).getString());
    }

    @Test
    void compareTimerBurnGreaterThanZero() {
        CompareValue c = new CompareValue(new TimerValue("burn", 999999.0), new ConstantValue(0.0),
                1.0, 1.0, ComparisonOperator.GREATER_THAN);
        assertEquals("Burn Timer > 0", c.describeFragment().get(0).getString());
    }

    @Test
    void compareSkillStateLevelGreaterThanZero() {
        CompareValue c = new CompareValue(new SkillStateValue("skill.level"), new ConstantValue(0.0),
                1.0, 1.0, ComparisonOperator.GREATER_THAN);
        assertEquals("Skill Level > 0", c.describeFragment().get(0).getString());
    }

    @Test
    void compareSkillUsingFireballGreaterThanZero() {
        CompareValue c = new CompareValue(new SkillUsingValue(Identifier.fromNamespaceAndPath("coe", "fireball")),
                new ConstantValue(0.0), 1.0, 1.0, ComparisonOperator.GREATER_THAN);
        assertEquals("Fireball > 0", c.describeFragment().get(0).getString());
    }

    @Test
    void comparePresenceRarityCountGreaterThanZero() {
        CompareValue c = new CompareValue(
                new PresenceRarityValue(List.of(Rarity.RARE, Rarity.UNIQUE), true, true),
                new ConstantValue(0.0), 1.0, 1.0, ComparisonOperator.GREATER_THAN);
        assertEquals("Rare or Unique Enemy > 0", c.describeFragment().get(0).getString());
    }

    // ---------------------------------------------------------------- 批 2b：装备 / 饰品 / 实体标记 门控覆盖（A 线条件）

    @Test
    void equipmentValueQuantityReturnsEquipNounPhrase() {
        // EquipmentValue 作 CompareValue 操作数时，应产出「槽位 + 输出项」名词短语而非 [?]。
        EquipmentValue ev = new EquipmentValue(Optional.of(List.of(EquipmentSlot.HEAD)), Optional.empty(),
                Optional.of("armor"), EquipmentValue.Aggregate.FIRST);
        assertEquals("Head Armor", ev.describeQuantity(Locale.ROOT).get(0).getString());
    }

    @Test
    void compareEquipmentValueGreaterThanZero() {
        // 等价 {compare, left:{type:equipment, slots:[head], output:armor}, op:>, right:{const:0}}
        EquipmentValue ev = new EquipmentValue(Optional.of(List.of(EquipmentSlot.HEAD)), Optional.empty(),
                Optional.of("armor"), EquipmentValue.Aggregate.FIRST);
        CompareValue c = new CompareValue(ev, new ConstantValue(0.0), 1.0, 1.0, ComparisonOperator.GREATER_THAN);
        assertEquals("Head Armor > 0", c.describeFragment().get(0).getString());
    }

    @Test
    void accessoryValueQuantityReturnsAccessoryNounPhrase() {
        assertEquals("Legs Evasion", new AccessoryValue(Optional.of("legs"), Optional.of("evasion"),
                EquipmentValue.Aggregate.SUM).describeQuantity(Locale.ROOT).get(0).getString());
    }

    @Test
    void accessoryValueFragmentReturnsEquippedClause() {
        // AccessoryValue 作为条件从句渲染「while %s is equipped」。
        AccessoryValue av = new AccessoryValue(Optional.of("legs"), Optional.of("evasion"), EquipmentValue.Aggregate.SUM);
        assertEquals("while Legs Evasion is equipped", av.describeFragment().get(0).getString());
    }

    @Test
    void entityFlagValueSummonedFragmentReturnsFlagClause() {
        // EntityFlagValue 仅带 Flag 时，渲染「if %s」标记分句（%s 为 stat.value.flag.<name>）。
        assertEquals("if Summoned", EntityFlagValue.summoned().describeFragment().get(0).getString());
    }

    // ---------------------------------------------------------------- 辅助

    private static void injectLanguage(Map<String, String> map) {
        // 捕获当前（vanilla）Language 实例，供 getVisualOrder 委托，避免与注入后的实例形成递归。
        Language vanilla = Language.getInstance();
        Language lang = new Language() {
            private final Map<String, String> data = new HashMap<>(map);
            @Override
            public String getOrDefault(String key, String def) {
                return data.getOrDefault(key, def);
            }
            @Override
            public boolean has(String key) {
                return data.containsKey(key);
            }
            @Override
            public boolean isDefaultRightToLeft() {
                return false;
            }
            @Override
            public FormattedCharSequence getVisualOrder(FormattedText text) {
                return vanilla.getVisualOrder(text);
            }
        };
        Language.inject(lang);
    }
}
