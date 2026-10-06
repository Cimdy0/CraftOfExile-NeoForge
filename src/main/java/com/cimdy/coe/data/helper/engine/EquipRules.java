package com.cimdy.coe.data.helper.engine;

import com.cimdy.coe.item.accessory.AccessoryContainer;
import com.cimdy.coe.data.gen.stat.effect.StatEffectGen;
import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.registry.DataComponentRegistry;
import com.cimdy.coe.data.codec.item.mod.state.EquipmentState;
import com.cimdy.coe.data.codec.item.mod.identity.ItemTypeChecker;
import com.cimdy.coe.data.codec.support.stat.engine.StatEngine;
import com.cimdy.coe.data.codec.item.equipment.EquipRuleDef;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import com.cimdy.coe.data.helper.item.ModifiableStateOps;
import com.cimdy.coe.event.data.LoadEvent;
import com.cimdy.coe.data.codec.support.rarity.Rarity;

/**
 * <b>通用「装备 / 持握规则」引擎</b>。
 *
 * <p>PoE2 里「能否拿得动 / 能否同时持有」是反复出现的维度：双手武器占双槽、法杖与护符互斥、
 * 副手限定、单手限定、某槽位只接受某类物品 …… 本设施把它做成一条<b>与具体类别无关</b>的链路：
 * <ol>
 *   <li><b>声明</b>：规则由 {@link LoadEvent#EQUIP_RULE_KEY}（{@code coe:equip_rule}）路径扫描加载，
 *       条目 = {@link EquipRuleDef}（已转 Provider：数据经 CoeDataProvider 写 {@code data/coe/equip_rule/<id>.json}，
 *       运行期填入 {@link LoadEvent#EQUIP_RULE_MAP}）；类别身份一律是 <b>类型键</b>
 *       （{@code ModifiableState.types()} 中的叶类型键），Java 无类别字面量。</li>
 *   <li><b>求值</b>：{@link #blocked(LivingEntity, ItemStack, Set)} 按「物品类型命中规则 +
 *       槽位上下文 + 另一手状态」逐条判定；<b>规则表为空 / 无规则命中 → 一律放行</b>
 *       （保证未配置时行为与既有版本完全一致）。</li>
 *   <li><b>豁免</b>：命中规则若被 {@code MarkerStatEffect} 标记按
 *       <b>规则 id 的 path</b> 豁免则放行（幅度 &gt; 0 即豁免）。</li>
 *   <li><b>强制</b>：消费端为持握路径（{@code mixin.attribute.EquipmentChange}）与
 *       饰品入槽路径（{@code AccessoryContainer#canPlaceItem}）。</li>
 * </ol>
 *
 * <p><b>「另一手」求值是方向敏感的</b>，这是为了得到确定顺序而非双向互锁：
 * <ul>
 *   <li>{@code conflict_other_hand_type} / {@code requires_other_hand_empty}：<b>本物品</b>被另一手顶掉；</li>
 *   <li>{@code occupies_both_hands}：<b>本物品永不被顶</b>（双手武器始终可用），
 *       改为<b>另一手的物品</b>被本物品顶掉。</li>
 * </ul>
 * 若二者都双向生效，双手武器 + 副手物品会互相顶掉、两手皆废，故取上述单一方向。
 *
 * <p><b>关于「稀有度 / 词缀」维度</b>：语料 {@code non-Item Rarity} 一类修饰的来源不可考，
 * 本轮按「不臆造玩法」处理：维度以 {@link EquipRuleDef#applyOnlyIfAnyTypes()} /
 * {@link EquipRuleDef#exemptIfAnyTypes()} 两个可选类型字段预留（默认空 = 该维度不启用），
 * 语义只取可确定部分（「解除一条另一手冲突约束」）。待内容侧给出稀有度 / 词缀的类型键，
 * 填数据即可闭合，零 Java 改动。
 *
 * <p><b>无类别字面量</b>：本类仅出现规则 id 常量（机制名，非类别名）与类型判定逻辑；
 * 具体「什么物品受什么约束」全在 {@code data/coe/coe/equip_rule/*.json}（或等价注册表）。
 */
public final class EquipRules {

    private EquipRules() {
    }

    /** 非手部槽位（饰品 / 护符 / 药剂等自定义槽）的槽位上下文名。 */
    public static final String ACCESSORY_SLOT = "accessory";

    /** 饰品槽上下文（供 {@code AccessoryContainer#canPlaceItem} 求值；未来「某槽位只接受某类型」复用同一入口）。 */
    public static final Set<String> ACCESSORY_CONTEXT = Set.of(ACCESSORY_SLOT);

    /**
     * 规则实例 id —— 「另一手类别冲突」约束（本轮代表「持 talisman 时不可持 sceptre」）。
     * <p><b>id 的 path 同时是 stat 侧 EFF 的 conditionTag</b>（单一来源，
     * 见 {@code StatEffectGen#EFF_EQUIP_RULE_EXEMPT_OTHER_HAND}），故改名必须两侧同步。
     */
    public static final Identifier OTHER_HAND_CONFLICT = id("other_hand_conflict");

    /**
     * 规则实例 id —— 「占双槽」约束（本轮代表「双手武器需占双手」）。
     * <p>同上：path = stat 侧 conditionTag（{@code EFF_EQUIP_RULE_EXEMPT_BOTH_HANDS}）。
     */
    public static final Identifier BOTH_HANDS_OCCUPANCY = id("both_hands_occupancy");

    /** 规则视图：注册表条目 id + 定义。 */
    public record Rule(Identifier id, EquipRuleDef def) {
        /** 该规则是否对「位于 {@code slotContext} 的 {@code stack}」适用（不含另一手判定）。 */
        public boolean applies(ItemStack stack, Set<String> slotContext) {
            if (stack == null || stack.isEmpty()) return false;
            if (!slotMatches(slotContext)) return false;
            EquipmentState state =ModifiableStateOps.equipment(stack);
            if (!ItemTypeChecker.hasType(state, def.type())) return false;
            if (!def.requireAllTypes().isEmpty()) {
                for (String t : def.requireAllTypes()) {
                    if (!ItemTypeChecker.hasType(state, t)) return false;
                }
            }
            if (!def.anyOfTypes().isEmpty() && !ItemTypeChecker.hasAnyTypeName(state, def.anyOfTypes())) return false;
            // 稀有度 / 词缀维度门闩：默认空 = 不启用
            if (!def.applyOnlyIfAnyTypes().isEmpty() && !ItemTypeChecker.hasAnyTypeName(state, def.applyOnlyIfAnyTypes())) return false;
            if (!def.exemptIfAnyTypes().isEmpty() && ItemTypeChecker.hasAnyTypeName(state, def.exemptIfAnyTypes())) return false;
            return true;
        }

        private boolean slotMatches(Set<String> slotContext) {
            if (slotContext == null || slotContext.isEmpty()) return false;
            String s = def.slot() == null ? EquipRuleDef.ANY_SLOT : def.slot();
            return EquipRuleDef.ANY_SLOT.equals(s) || slotContext.contains(s);
        }
    }

    /**
     * 读取装备规则定义（已转 Provider：数据由 CoeDataProvider 写 {@code data/coe/equip_rule/<id>.json}，
     * 运行期经 {@link LoadEvent#EQUIP_RULE_MAP} 路径扫描缓存）为规则列表；定义表缺失 / 为空 → 空列表（引擎恒放行）。
     * <p>规则数量极少（默认 2 条），每次调用就地构建列表开销可忽略；规则表仅在资源重载时变更。
     *
     * @param access 保留参数以兼容既有调用点（原 registry lookup 入参），转 Provider 后不再使用。
     */
    public static List<Rule> rules(RegistryAccess access) {
        List<Rule> out = new ArrayList<>();
        List<Identifier> ids = new ArrayList<>(LoadEvent.EQUIP_RULE_MAP.keySet());
        ids.sort(Comparator.comparing(Identifier::getPath));
        for (Identifier id : ids) {
            EquipRuleDef def = LoadEvent.EQUIP_RULE_MAP.get(id);
            if (def != null) out.add(new Rule(id, def));
        }
        return out;
    }

    /**
     * 判定：<b>{@code stack} 置于 {@code slot} 时是否被任一装备规则阻止</b>（已计入 stat 豁免）。
     *
     * @return true = 不可持握 / 不可装备（消费端据此按「未装备」处理）
     */
    public static boolean blocked(LivingEntity living, ItemStack stack, EquipmentSlot slot) {
        if (living == null || stack == null || stack.isEmpty() || slot == null) return false;
        return blocked(living, stack, Set.of(slot.getName()));
    }

    /**
     * 判定（通用入口）：{@code stack} 置于槽位上下文 {@code slotContext} 时是否被阻止。
     * <p>{@code slotContext} 为槽位名集合（{@code mainhand} / {@code offhand} / {@code accessory} …），
     * 由调用方按其实际语义给出——引擎不认识任何具体槽位名，故新增槽位零 Java 改动。
     */
    public static boolean blocked(LivingEntity living, ItemStack stack, Set<String> slotContext) {
        if (living == null || stack == null || stack.isEmpty()) return false;
        if (slotContext == null || slotContext.isEmpty()) return false;
        List<Rule> rules = rules(accessOf(living));
        if (rules.isEmpty()) return false;

        ItemStack other = otherHand(living, slotContext);
        boolean otherOccupied = other != null && !other.isEmpty();
        if (!otherOccupied) return false;

        EquipmentState otherState =ModifiableStateOps.equipment(other);

        // 方向一：本物品被另一手顶掉（conflict_other_hand_type / requires_other_hand_empty）
        for (Rule r : rules) {
            if (!r.applies(stack, slotContext)) continue;
            boolean violated = r.def.requiresOtherHandEmpty()
                    || (r.def.conflictOtherHandType().isPresent()
                        && ItemTypeChecker.hasType(otherState, r.def.conflictOtherHandType().get()));
            if (violated && !exempt(living, r.id())) return true;
        }
        // 方向二：另一手的物品占双槽 → 本物品被顶掉（双手武器始终可用，副手被抑制）
        Set<String> otherContext = otherHandContext(slotContext);
        if (otherContext != null) {
            for (Rule r : rules) {
                if (!r.def.occupiesBothHands()) continue;
                if (!r.applies(other, otherContext)) continue;
                if (!exempt(living, r.id())) return true;
            }
        }
        return false;
    }

    /** 该规则是否被 stat 豁免（{@code MarkerStatEffect}，幅度 &gt; 0 即豁免；身份 = 规则 id 的 path）。 */
    public static boolean exempt(LivingEntity living, Identifier ruleId) {
        if (living == null || ruleId == null) return false;
        return StatEngine.gateOf(living, "equip_rule_exempt",
                Set.of(ruleId.getPath()));
    }

    /** 另一手的物品；非手部上下文（或无法解析）返回 null（= 不做另一手判定）。 */
    private static ItemStack otherHand(LivingEntity living, Set<String> slotContext) {
        if (slotContext.contains(EquipmentSlot.MAINHAND.getName())) {
            return living.getItemBySlot(EquipmentSlot.OFFHAND);
        }
        if (slotContext.contains(EquipmentSlot.OFFHAND.getName())) {
            return living.getItemBySlot(EquipmentSlot.MAINHAND);
        }
        return null;
    }

    /** 另一手的槽位上下文（供反向「占双槽」规则求值）。 */
    private static Set<String> otherHandContext(Set<String> slotContext) {
        if (slotContext.contains(EquipmentSlot.MAINHAND.getName())) return Set.of(EquipmentSlot.OFFHAND.getName());
        if (slotContext.contains(EquipmentSlot.OFFHAND.getName())) return Set.of(EquipmentSlot.MAINHAND.getName());
        return null;
    }

    private static RegistryAccess accessOf(LivingEntity living) {
        return living == null || living.level() == null ? null : living.level().registryAccess();
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(CraftOfExile.MODID, path);
    }
}
