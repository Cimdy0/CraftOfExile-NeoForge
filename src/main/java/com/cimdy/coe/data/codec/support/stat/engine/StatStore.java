package com.cimdy.coe.data.codec.support.stat.engine;

import com.cimdy.coe.data.codec.support.stat.tag.GateTag;
import com.cimdy.coe.data.codec.support.stat.tag.GateExpr;

import com.cimdy.coe.data.codec.skill.helper.resource.ResourceType;
import com.cimdy.coe.data.codec.support.math.EventRefCollector;
import com.cimdy.coe.data.codec.support.stat.effect.modifier.ModifierStatEffect;
import com.cimdy.coe.data.codec.support.stat.effect.convert.ResourceConvertStatEffect;
import com.cimdy.coe.data.codec.support.stat.effect.base.StatEffect;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.data.codec.support.stat.entry.StatGroup;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import org.jspecify.annotations.Nullable;

import java.util.*;

/**
 * 统一 stat 存储（单一附件 STAT_STORE）：聚合某实体全部已分配 stat 来源（天赋 / 技能 action / 词缀 / 装备 …）
 * 产出的所有<b>条目</b>（{@link StatEntry} = 数值 + 条件 + 选择器 + 效果）——
 * 既包括属性亚型条目（效果为 {@link ModifierStatEffect}，由 {@link StatEngine#fold}
 * 读时按作用域即时聚合），也包括触发亚型条目（heal / damage / status / ailment_*，
 * 由 {@link StatEngine#fire} 事件触发时过筛执行）。
 * <p>
 * 数据全量重建由 {@code TalentEffectApplier} / 技能 action 清空或追加本附件后逐条累积；
 * 本类仅为<b>不可变数据源</b>，不向原版属性写入任何全局修饰符。
 * <p>
 * 内部以 {@code Map<String, StatEntry>} 存储，键为来源 id：
 * <ul>
 *   <li>从 {@link List} 构造时，按 {@link StatEntry#id()} 自适应分配——非空则直接用，空则回退 {@code auto_n}；</li>
 *   <li>装备修饰符等需稳定寻址的来源，经 {@link #addModifier(String, StatEntry)} /
 *       {@link #removeModifier(String)} 以显式 id（约定前缀 {@code "equip:" + slot + ":" + itemUuid}）精确增删。</li>
 * </ul>
 * {@code index} 按属性预分组，{@code tagIndex} 按标签反向索引，{@code conditionOnlyIndex}
 * 收集纯条件门控属性，使读取 / 广播路径只遍历相关条目、避免热路径全表扫描。
 */
public class StatStore {
    /**
     * 带来源键的索引条目（B-1.8 稳定 id 依赖）：limiter 等需要「条目级稳定 id」的消费方
     * 用 {@code key} 派生（替代运行时 {@code UUID.randomUUID()}），并支持按来源键精确寻址。
     */
    public record IndexedEntry(String key, StatEntry entry) {
    }

    private final Map<String, StatEntry> entries;
    private final Map<String, Integer> expiries;
    /** 内容版本号：每次 entries 逻辑变更（增/删/改）单调递增，无变更短路（return this）不递增。
     *  供 {@link ExpressionContext} 的 stat 读时缓存做 (scopeVersion, dataVersion) 双键失效——
     *  作用域栈变化与数据内容变化是两个正交的失效维度（B-0.1）。 */
    private final int dataVersion;
    private final Map<Holder<Attribute>, List<IndexedEntry>> index;
    private final Map<String, Set<Holder<Attribute>>> tagIndex;
    private final Set<Holder<Attribute>> conditionOnlyIndex;
    private final List<StatEntry> triggers;
    /** 触发反向索引（B-2.7）：事件标签 → 候选触发条目，使 fire 不再全量遍历。 */
    private final Map<String, List<StatEntry>> triggersByTag;
    /** 无标签的触发条目（任何事件均候选）。 */
    private final List<StatEntry> unconditionalTriggers;
    /** 含 proj: 前缀要求的触发条目（P1-1 通配：任何投射物实例标签命中）。 */
    private final List<StatEntry> projTriggers;
    private final Map<ResourceType, List<StatEntry>> convertIndex;
    /** 伤害类型转换规则（固定 / 随机类型）全量列表（带来源键）：随机转换（RandomConvertStatEffect）的
     *  CYCLE 轮流计数器须按条目来源键寻址，故升级为 {@link IndexedEntry}。 */
    private final List<IndexedEntry> damageConvertIndex;
    private final List<IndexedEntry> damageTakenConvertIndex;
    /** 状态免疫条目（ImmuneStatEffect，fold 型查询）：供 StatusEngine.apply 免疫短路。 */
    private final List<StatEntry> immuneIndex;
    /** 掷骰修饰条目（LuckyStatEffect，fold 型查询）：供 DamageHelper 双掷取优/劣。 */
    private final List<StatEntry> luckyIndex;
    /** 命中标志条目（HitFlagStatEffect，fold 型查询）：供 DamageHelper 绝对短路。 */
    private final List<StatEntry> hitFlagIndex;
    /** 投送修饰条目（DeliveryStatEffect，fold 型查询）：供 SupportProfile 聚合投送参数。 */
    private final List<StatEntry> deliveryIndex;
    /** 周期施加条目（MultiStatEffect，PERIODIC 型，带来源键）：供 PeriodicDriver 每 tick 节流驱动
     *  （见 {@link com.cimdy.coe.data.helper.engine.periodic.PeriodicDriver}）。 */
    private final List<IndexedEntry> periodicIndex;
    /** 本 store 条目引用的事件名集合（按需订阅静态侧，构造期由 EventRefCollector 提取）。
     *  供 AttributeEvent tick 对齐点 diff 驱动全局 EventSubscription。 */
    private final Set<String> referencedEvents;
    /** 本 store 条目引用的事件名 → 该事件名在 Stat 条件中声明的最大记忆窗口（tick）。
     *  供 Event.State 按事件名做差异化 TTL 清理（取代固定 MAX_TRIGGER_MEMORY 常数）。 */
    private final Map<String, Integer> eventMaxWindow;
    /** 本 store 是否引用 presence 节点（驱动 EntityOperationEvent 每 tick 全域进/出扫描）。 */
    private final boolean presenceNeeded;

    public static final Codec<StatStore> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.unboundedMap(Codec.STRING, StatEntry.CODEC)
                    .optionalFieldOf("effects", Map.of())
                    .forGetter(StatStore::keyedEntries),
            // 计时域随附件一起序列化：STAT_STORE 是持久附件，expiries 不进 codec 时
            // 临时条目（grant:* / effect:*）存档重载后会失去到期时刻而变成永久加成。
            // 序列化为「剩余 tick」，重载后 tick() 继续倒数，行为与存档前连续。
            Codec.unboundedMap(Codec.STRING, Codec.INT)
                    .optionalFieldOf("expiries", Map.of())
                    .forGetter(StatStore::keyedExpiries)
    ).apply(i, StatStore::new));

    /** 计时域只读视图（codec 序列化用）：键 = 来源 id，值 = 剩余 tick（仅临时条目存在）。 */
    public Map<String, Integer> keyedExpiries() {
        return expiries;
    }

    /** 由列表构造：键按 {@link StatEntry#id()} 自适应分配。 */
    public StatStore(List<StatEntry> entries) {
        this(toKeyedMap(entries));
    }

    /** 由既有 keyed map 构造（codec 反序列化 / 显式 id 增删路径）。计时域默认空（datagen 不含持续时长）。 */
    public StatStore(Map<String, StatEntry> entries) {
        this(entries, Map.of(), 0);
    }

    /** 由 keyed map + 计时域构造（codec 反序列化路径）：携带临时条目的剩余时长，重载后继续倒数。 */
    public StatStore(Map<String, StatEntry> entries, Map<String, Integer> expiries) {
        this(entries, expiries, 0);
    }

    /** 全参私有构造：持有预计算索引，供 {@link #tick()} 在无到期时复用（零索引重建）。
     *  @param dataVersion 内容版本号（见 {@link #dataVersion()}）；公开构造恒为 0，变更方法构造时 +1 */
    private StatStore(Map<String, StatEntry> entries,
                      Map<String, Integer> expiries,
                      int dataVersion,
                      Map<Holder<Attribute>, List<IndexedEntry>> index,
                      Map<String, Set<Holder<Attribute>>> tagIndex,
                      Set<Holder<Attribute>> conditionOnlyIndex,
                      List<StatEntry> triggers,
                      Map<String, List<StatEntry>> triggersByTag,
                      List<StatEntry> unconditionalTriggers,
                      List<StatEntry> projTriggers,
                      Map<ResourceType, List<StatEntry>> convertIndex,
                      List<IndexedEntry> damageConvertIndex,
                      List<IndexedEntry> damageTakenConvertIndex,
                      List<StatEntry> immuneIndex,
                      List<StatEntry> luckyIndex,
                      List<StatEntry> hitFlagIndex,
                      List<StatEntry> deliveryIndex,
                      List<IndexedEntry> periodicIndex) {
        this.entries = entries;
        this.expiries = expiries;
        this.dataVersion = dataVersion;
        this.index = index;
        this.tagIndex = tagIndex;
        this.conditionOnlyIndex = conditionOnlyIndex;
        this.triggers = triggers;
        this.triggersByTag = triggersByTag;
        this.unconditionalTriggers = unconditionalTriggers;
        this.projTriggers = projTriggers;
        this.convertIndex = convertIndex;
        this.damageConvertIndex = damageConvertIndex;
        this.damageTakenConvertIndex = damageTakenConvertIndex;
        this.immuneIndex = immuneIndex;
        this.luckyIndex = luckyIndex;
        this.hitFlagIndex = hitFlagIndex;
        this.deliveryIndex = deliveryIndex;
        this.periodicIndex = periodicIndex;
        // 事件按需订阅静态侧（tick 复用路径：entries 内容未变，直接按同一集合提取引用集）
        this.referencedEvents = collectReferencedEvents(entries);
        this.eventMaxWindow = collectEventMaxWindow(entries);
        this.presenceNeeded = collectPresenceNeeded(entries);
    }

    /** 从 keyed map 提取全部条目引用的事件名集合（按需订阅静态侧）。 */
    private static Set<String> collectReferencedEvents(Map<String, StatEntry> entries) {
        Set<String> refEvents = new HashSet<>();
        for (StatEntry e : entries.values()) refEvents.addAll(EventRefCollector.collect(e).events());
        return Set.copyOf(refEvents);
    }

    /** 从 keyed map 提取各事件名在 Stat 条件中声明的最大记忆窗口（按需 TTL 清理静态侧）。 */
    private static Map<String, Integer> collectEventMaxWindow(Map<String, StatEntry> entries) {
        Map<String, Integer> maxWindow = new HashMap<>();
        for (StatEntry e : entries.values()) {
            for (var en : EventRefCollector.collect(e).maxWindow().entrySet()) {
                maxWindow.merge(en.getKey(), en.getValue(), Integer::max);
            }
        }
        return Map.copyOf(maxWindow);
    }

    /** 从 keyed map 提取是否引用 presence 节点（按需启用全域进/出扫描）。 */
    private static boolean collectPresenceNeeded(Map<String, StatEntry> entries) {
        for (StatEntry e : entries.values()) {
            if (EventRefCollector.collect(e).presence()) return true;
        }
        return false;
    }

    /** 由 keyed map + 计时域构造（重建全部索引）。
     *  @param dataVersion 内容版本号：公开构造传 0，变更方法构造时传 {@code this.dataVersion + 1} */
    private StatStore(Map<String, StatEntry> entries, Map<String, Integer> expiries, int dataVersion) {
        this.entries = Map.copyOf(entries);
        this.expiries = Map.copyOf(expiries);
        this.dataVersion = dataVersion;
        // 效果分类经 indexKind() 策略（开闭原则）：新增效果类型零改本构造器。
        List<IndexedEntry> attrEntries = new ArrayList<>();
        List<StatEntry> trg = new ArrayList<>();
        List<StatEntry> convertEntries = new ArrayList<>();
        List<IndexedEntry> damageConvertEntries = new ArrayList<>();
        List<IndexedEntry> damageTakenConvertEntries = new ArrayList<>();
        List<StatEntry> immuneEntries = new ArrayList<>();
        List<StatEntry> luckyEntries = new ArrayList<>();
        List<StatEntry> hitFlagEntries = new ArrayList<>();
        List<StatEntry> deliveryEntries = new ArrayList<>();
        List<IndexedEntry> periodicEntries = new ArrayList<>();
        for (Map.Entry<String, StatEntry> e : this.entries.entrySet()) {
            StatEffect fx = e.getValue().effect();
            if (fx == null) continue;   // Either.left(null) 显式跳过（配合阶段三 3-2 安全封装）
            // 条目层时长判据（与 StatEntry#isTrigger 同一规则）：Effect 只表达「赋予什么」，
            // 是否为触发型由条目决定。折叠亚型（FOLD）一旦声明时长就必须物化——
            // 折叠是「每次读取重估」，无法表达「持续 N tick 后消失」，故重分类进 FIRE。
            StatEffect.EffectKind kind = fx.indexKind() == StatEffect.EffectKind.FOLD
                    && e.getValue().duration() != null
                    ? StatEffect.EffectKind.FIRE : fx.indexKind();
            switch (kind) {
                case FOLD -> attrEntries.add(new IndexedEntry(e.getKey(), e.getValue()));
                case FIRE -> trg.add(e.getValue());
                case RESOURCE_CONVERT -> convertEntries.add(e.getValue());
                case DAMAGE_CONVERT -> damageConvertEntries.add(new IndexedEntry(e.getKey(), e.getValue()));
                case DAMAGE_TAKEN_CONVERT -> damageTakenConvertEntries.add(new IndexedEntry(e.getKey(), e.getValue()));
                case IMMUNE -> immuneEntries.add(e.getValue());
                case LUCKY -> luckyEntries.add(e.getValue());
                case HIT_FLAG -> hitFlagEntries.add(e.getValue());
                case DELIVERY -> deliveryEntries.add(e.getValue());
                // 周期施加（MultiStatEffect，PERIODIC 型）：独立索引，由 PeriodicDriver 每 tick
                // 节流驱动（见 helper/engine/periodic/PeriodicDriver）。
                case PERIODIC -> periodicEntries.add(new IndexedEntry(e.getKey(), e.getValue()));
                // 地面生成（CreateGroundStatEffect，GROUND 型）：不单独建索引，由 CreateGroundDriver
                // 每 tick 对持有者条目全量扫描驱动（见 event/CreateGroundDriver）。
                case GROUND -> { /* 驱动见 CreateGroundDriver，不进索引 */ }
                case INERT -> { /* requirement 不进索引 */ }
                // 兜底：新增 EffectKind 但漏了本 switch 的 case 时**显式抛出**，
                // 而不是让条目静默落进「无桶」状态（那会让效果永不生效且无任何告警）。
                default -> throw new IllegalStateException("EffectKind 未在 StatStore 分派表中登记: "
                        + fx.indexKind() + "（效果 " + e.getKey() + " / " + fx.type()
                        + "）——新增 EffectKind 须同时在此加 case");
            }
        }
        this.index = buildIndex(attrEntries);
        this.tagIndex = buildTagIndex(this.index);
        this.conditionOnlyIndex = buildConditionOnlyIndex(this.index);
        this.triggers = List.copyOf(trg);
        // 触发反向索引（B-2.7）：叶子标签 → 条目；恒真（无标签）条目进无条件；
        // 含 NOT 的表达式进无条件（NOT 对几乎任何作用域都可能命中，保守不漏，最终仍由 GateExpr#matches / StatEngine#conditionsHold 精确判定）；proj: 前缀进通配列表
        Map<String, List<StatEntry>> tbt = new HashMap<>();
        List<StatEntry> unc = new ArrayList<>();
        List<StatEntry> proj = new ArrayList<>();
        for (StatEntry e : trg) {
            GateExpr expr = e.tags();
            if (expr.isAlwaysTrue()) { unc.add(e); continue; }
            if (expr.containsNot()) unc.add(e);
            boolean hasProj = false;
            for (String t : expr.leaves()) {
                if (t.startsWith("proj:")) hasProj = true;
                tbt.computeIfAbsent(t, k -> new ArrayList<>()).add(e);
            }
            if (hasProj) proj.add(e);
        }
        Map<String, List<StatEntry>> frozenTbt = new HashMap<>();
        tbt.forEach((k, v) -> frozenTbt.put(k, List.copyOf(v)));
        this.triggersByTag = Map.copyOf(frozenTbt);
        this.unconditionalTriggers = List.copyOf(unc);
        this.projTriggers = List.copyOf(proj);
        this.convertIndex = buildConvertIndex(convertEntries);
        this.damageConvertIndex = List.copyOf(damageConvertEntries);
        this.damageTakenConvertIndex = List.copyOf(damageTakenConvertEntries);
        this.immuneIndex = List.copyOf(immuneEntries);
        this.luckyIndex = List.copyOf(luckyEntries);
        this.hitFlagIndex = List.copyOf(hitFlagEntries);
        this.deliveryIndex = List.copyOf(deliveryEntries);
        this.periodicIndex = List.copyOf(periodicEntries);
        // 事件按需订阅静态侧：构造期一次提取本 store 全部条目的引用事件名 + presence 需求
        //（conditions 全量 + condition（StatCondition 树，按 presenceDependent 判定）+ amount 递归，含 Read/ref 跟进；见 EventRefCollector）
        this.referencedEvents = collectReferencedEvents(this.entries);
        this.eventMaxWindow = collectEventMaxWindow(this.entries);
        this.presenceNeeded = collectPresenceNeeded(this.entries);
    }

    /** 本 store 条目引用的事件名集合（按需订阅；空 = 无事件依赖）。 */
    public Set<String> referencedEvents() {
        return referencedEvents;
    }

    /** 本 store 条目引用的事件名 → 该事件名的最大记忆窗口（tick）；空 = 无事件依赖。 */
    public Map<String, Integer> eventMaxWindow() {
        return eventMaxWindow;
    }

    /** 本 store 是否引用 presence 节点（需启用全域进/出扫描）。 */
    public boolean presenceNeeded() {
        return presenceNeeded;
    }

    /** 列表 → 自适应 keyed map：非空 id 直接用，空 id 回退 auto_n，重复 key 追加后缀。 */
    private static Map<String, StatEntry> toKeyedMap(List<StatEntry> list) {
        Map<String, StatEntry> m = new LinkedHashMap<>();
        int i = 0;
        for (StatEntry e : list) {
            for (StatEntry member : flatten(e)) {
                String base = member.id();
                if (base == null || base.isEmpty()) base = "auto_" + i;
                String key = base;
                int suffix = 1;
                while (m.containsKey(key)) key = base + "_" + (suffix++);
                m.put(key, member);
                i++;
            }
        }
        return m;
    }

    /** 摊平：group 展开为 members，非 group 原样返回（hybrid 词缀全量生效）。 */
    private static List<StatEntry> flatten(StatEntry e) {
        return e instanceof StatGroup g ? g.flatten() : List.of(e);
    }

    /** 取条目效果的属性 Holder（调用方须确保为属性亚型）。 */
    public static Holder<Attribute> attributeOf(StatEntry entry) {
        return ((ModifierStatEffect) entry.effect()).attribute();
    }

    private static Map<Holder<Attribute>, List<IndexedEntry>> buildIndex(List<IndexedEntry> attrEntries) {
        Map<Holder<Attribute>, List<IndexedEntry>> m = new HashMap<>();
        for (IndexedEntry ie : attrEntries) {
            m.computeIfAbsent(attributeOf(ie.entry()), k -> new ArrayList<>()).add(ie);
        }
        Map<Holder<Attribute>, List<IndexedEntry>> r = new HashMap<>();
        m.forEach((k, v) -> r.put(k, List.copyOf(v)));
        return Map.copyOf(r);
    }

    private static Map<String, Set<Holder<Attribute>>> buildTagIndex(
            Map<Holder<Attribute>, List<IndexedEntry>> index) {
        Map<String, Set<Holder<Attribute>>> m = new HashMap<>();
        for (Map.Entry<Holder<Attribute>, List<IndexedEntry>> e : index.entrySet()) {
            for (IndexedEntry me : e.getValue()) {
                // 表达式叶子标签作为候选键（含 NOT 的表达式无法反向索引，由 GateExpr#matches / StatEngine#conditionsHold 兜底精确判定）。
                // 取 effectiveTags()：与聚合侧门控同一表达式，故索引与判定恒一致——
                //   · 外溢条目：tags ∪ spillTo，外溢标签同样能激活本属性，否则进入只带外溢标签的
                //     作用域时该属性不在刷新候选内，UI 广播会漏更新；
                //   · 转换条目：仅 convertTo 的标签能激活本属性（来源标签<b>不</b>入索引——该条目在
                //     来源作用域已不贡献，无需因进入来源作用域而刷新）。
                for (String t : me.entry().effectiveTags().leaves()) {
                    m.computeIfAbsent(t, k -> new HashSet<>()).add(e.getKey());
                }
            }
        }
        Map<String, Set<Holder<Attribute>>> r = new HashMap<>();
        m.forEach((k, v) -> r.put(k, Set.copyOf(v)));
        return Map.copyOf(r);
    }

    /**
     * 纯条件门控属性集合：条目门控无标签要求（恒真表达式，否则已被 {@link #tagIndex} 覆盖），
     * 仅含非空的 {@code conditions()}。进入作用域时这些属性可能由「无标签」激活为「条件满足」，需纳入刷新候选。
     * 判据取 {@code effectiveTags()}（聚合侧同一门控）：声明了转换的条目门控非恒真，
     * 已由 {@link #tagIndex} 按 convertTo 叶子覆盖，不在此重复纳入。
     */
    private static Set<Holder<Attribute>> buildConditionOnlyIndex(
            Map<Holder<Attribute>, List<IndexedEntry>> index) {
        Set<Holder<Attribute>> s = new HashSet<>();
        for (Map.Entry<Holder<Attribute>, List<IndexedEntry>> e : index.entrySet()) {
            for (IndexedEntry me : e.getValue()) {
                if (me.entry().effectiveTags().isAlwaysTrue() && !me.entry().conditions().isEmpty()) {
                    s.add(e.getKey());
                }
            }
        }
        return Set.copyOf(s);
    }

    /** 全部条目（属性亚型 + 触发亚型，只读）。 */
    public List<StatEntry> entries() {
        return List.copyOf(entries.values());
    }

    /** keyed 全量映射（只读）：键为来源 id（词缀 auto_* / 装备 equip:* 等）。 */
    public Map<String, StatEntry> keyedEntries() {
        return entries;
    }

    /** 内容版本号：entries 每发生一次逻辑变更单调递增，供 stat 读时缓存双键失效（B-0.1）。 */
    public int dataVersion() {
        return dataVersion;
    }

    /**
     * 装备修饰符接口：以显式稳定 id 追加一条条目，返回新不可变 StatStore。
     * 约定 id 形如 {@code "equip:" + slot + ":" + itemUuid}，以便 {@link #removeModifier} 精确移除。
     */
    public StatStore addModifier(String id, StatEntry entry) {
        return addModifier(id, entry, 0);
    }

    /**
     * 追加一条目并（可选）登记持续时长（单位 tick）：{@code ticks <= 0} 表示永久（与
     * {@link #addModifier(String, StatEntry)} 等价，不进计时域）；{@code ticks > 0} 表示
     * 临时条目，由 {@link #tick()} 每游戏刻递减，归零后自动移除（无需外部显式回滚）。
     * 仅临时条目进入 {@code expiries} 计时域，永久条目零计时开销。
     */
    public StatStore addModifier(String id, StatEntry entry, int ticks) {
        // 单条精确寻址不接受 StatGroup：group 语义是"多条成员同时生效"，
        // 直接 put 会把 group 当单一条目处理（索引只按首 member 建、其余成员静默丢失）。
        // 需要摊平注入请用 addModifiers(Map)。
        if (entry instanceof StatGroup) {
            throw new IllegalArgumentException(
                    "StatStore.addModifier does not accept StatGroup (use addModifiers for flattened group): " + id);
        }
        Map<String, StatEntry> m = new LinkedHashMap<>(entries);
        m.put(id, entry);
        Map<String, Integer> ex = expiries;
        if (ticks > 0) {
            ex = new LinkedHashMap<>(expiries);
            ex.put(id, ticks);
        } else if (expiries.containsKey(id)) {
            // 覆盖为永久：清掉同键残留计时（旧临时条目被永久条目覆盖时不应继承倒计时）
            ex = new LinkedHashMap<>(expiries);
            ex.remove(id);
        }
        return new StatStore(m, ex, this.dataVersion + 1);
    }

    /** 批量追加（单次重建索引）：合并一组来源键 → 条目，返回新不可变 StatStore（永久条目）。 */
    public StatStore addModifiers(Map<String, StatEntry> additions) {
        return addModifiers(additions, 0);
    }

    /**
     * 批量追加并（可选）统一登记持续时长（tick）：{@code ticks <= 0} 永久；{@code ticks > 0}
     * 全部条目进入计时域（语义同 {@link #addModifier(String, StatEntry, int)}，单次索引重建）。
     */
    public StatStore addModifiers(Map<String, StatEntry> additions, int ticks) {
        if (additions.isEmpty()) return this;
        Map<String, StatEntry> m = new LinkedHashMap<>(entries);
        for (var en : additions.entrySet()) {
            StatEntry e = en.getValue();
            if (e instanceof StatGroup g) {
                List<StatEntry> flat = g.flatten();
                for (int k = 0; k < flat.size(); k++) m.put(en.getKey() + "#" + k, flat.get(k));
            } else {
                m.put(en.getKey(), e);
            }
        }
        Map<String, Integer> ex = expiries;
        boolean exDirty = false;
        Map<String, Integer> nx = null;
        if (ticks > 0) {
            nx = new LinkedHashMap<>(expiries);
            for (String key : additions.keySet()) nx.put(key, ticks);
            exDirty = true;
        } else {
            for (String key : additions.keySet()) {
                if (expiries.containsKey(key)) {
                    if (nx == null) nx = new LinkedHashMap<>(expiries);
                    nx.remove(key);
                    exDirty = true;
                }
            }
        }
        if (exDirty) ex = nx;
        return new StatStore(m, ex, this.dataVersion + 1);
    }

    /**
     * 反查条目在本 store 内的来源键（同一实例，identity 比较；{@code Map<StatEntry,...>} 全量引用语义）。
     * 供触发型效果（如带 duration 的 {@link ModifierStatEffect} 授予形态）把
     * 「条目实例」翻译回「稳定来源键」——来源键跨 store 重建保持不变（换装 / 天赋重载 / 状态重同步
     * 重建的是条目对象，键由来源规则决定），从而让 grant 物化条目的刷新语义跨重建稳定，
     * 不因 identityHashCode 漂移产生同词缀双份叠加。O(n) 线性扫描，仅事件触发路径调用（非热路径）。
     */
    @Nullable
    public String keyOf(StatEntry entry) {
        for (Map.Entry<String, StatEntry> e : entries.entrySet()) {
            if (e.getValue() == entry) return e.getKey();
        }
        return null;
    }

    /** 装备修饰符接口：按来源 id 移除一条目，返回新不可变 StatStore（同步清计时域）。 */
    public StatStore removeModifier(String id) {
        if (!entries.containsKey(id) && !expiries.containsKey(id)) return this;
        Map<String, StatEntry> m = new LinkedHashMap<>(entries);
        m.remove(id);
        Map<String, Integer> ex = expiries;
        if (ex.containsKey(id)) {
            ex = new LinkedHashMap<>(ex);
            ex.remove(id);
        }
        return new StatStore(m, ex, this.dataVersion + 1);
    }

    /**
     * 按来源前缀移除：移除所有键中 {@code '#'} 之前部分等于 {@code prefix} 的条目
     * （如 {@code "modName"} 匹配 {@code "modName#0"} / {@code "modName#1"}）。
     * 同时清理计时域中同前缀的残留键，保证「显式移除」与「计时自动过期」两条路径互补、
     * 不会因事件漏触发而残留孤儿计时键。用于回滚某来源（词缀 / 稀有度 / 全局修饰器 /
     * 药水效果）贡献的全部 Stat 条目。返回新不可变 StatStore。
     */
    public StatStore removeBySourcePrefix(String prefix) {
        Map<String, StatEntry> m = new LinkedHashMap<>();
        boolean changed = false;
        for (Map.Entry<String, StatEntry> e : entries.entrySet()) {
            if (srcOf(e.getKey()).equals(prefix)) changed = true;
            else m.put(e.getKey(), e.getValue());
        }
        boolean exChanged = false;
        Map<String, Integer> ex = expiries;
        if (!expiries.isEmpty()) {
            Map<String, Integer> nx = new LinkedHashMap<>();
            for (Map.Entry<String, Integer> e : expiries.entrySet()) {
                if (srcOf(e.getKey()).equals(prefix)) exChanged = true;
                else nx.put(e.getKey(), e.getValue());
            }
            if (exChanged) ex = nx;
        }
        if (!changed && !exChanged) return this;
        return new StatStore(m, ex, this.dataVersion + 1);
    }

    /** 取来源键的来源前缀（'#' 之前部分；无 '#' 则整键）。 */
    private static String srcOf(String key) {
        int i = key.indexOf('#');
        return i >= 0 ? key.substring(0, i) : key;
    }

    /**
     * 按完整键前缀移除（{@code key.startsWith(prefix)}）：用于「全量重建」来源，如天赋树
     * 的 {@code coe.talent.} 命名空间——每个节点 / 珠宝 / 多选一选项都有独立的完整键，
     * 其来源前缀（'#' 之前）各不相同，故需对完整键做 {@code startsWith} 匹配。
     * 同步清理计时域中同前缀键。返回新不可变 StatStore。
     */
    public StatStore removeByKeyPrefix(String prefix) {
        Map<String, StatEntry> m = new LinkedHashMap<>();
        boolean changed = false;
        for (Map.Entry<String, StatEntry> e : entries.entrySet()) {
            if (e.getKey().startsWith(prefix)) changed = true;
            else m.put(e.getKey(), e.getValue());
        }
        boolean exChanged = false;
        Map<String, Integer> ex = expiries;
        if (!expiries.isEmpty()) {
            Map<String, Integer> nx = new LinkedHashMap<>();
            for (Map.Entry<String, Integer> e : expiries.entrySet()) {
                if (e.getKey().startsWith(prefix)) exChanged = true;
                else nx.put(e.getKey(), e.getValue());
            }
            if (exChanged) ex = nx;
        }
        if (!changed && !exChanged) return this;
        return new StatStore(m, ex, this.dataVersion + 1);
    }

    /** 按属性预分组的属性亚型条目索引（只读，带来源键），供 {@link StatEngine#fold} 聚合与 limiter 稳定 id。 */
    public Map<Holder<Attribute>, List<IndexedEntry>> keyedIndex() {
        return index;
    }

    /** 标签 → 含该标签的属性的反向索引（只读），供事件驱动广播取候选集。 */
    public Map<String, Set<Holder<Attribute>>> tagIndex() {
        return tagIndex;
    }

    /** 纯条件门控属性集合（只读）：仅由 {@code conditions()} 门控、无标签的属性。 */
    public Set<Holder<Attribute>> conditionOnlyIndex() {
        return conditionOnlyIndex;
    }

    /**
     * 按事件作用域标签取候选触发条目（B-2.7）：无条件条目恒入候选；事件标签精确命中 +
     * 父类别反向展开（事件发子标签、条目要求父类别）+ {@code proj:} 前缀通配（P1-1）。
     * 候选集只求「不漏」，最终命中仍由 {@link GateExpr#matches} / {@link StatEngine#conditionsHold} 的标签子集 + 条件精确判定。
     */
    public List<StatEntry> triggersFor(Set<String> eventTags) {
        // 空触发域短路：unconditionalTriggers / triggersByTag / projTriggers 三者
        // 均由构造期 FIRE 桶 trg 派生（见下方索引构建循环），trg 为空则三者皆空
        // → 候选集必为空。绝大多数实体只有属性型条目、无触发型条目，此处直接返回
        // 共享空列表，免去 LinkedHashSet + 逐条深哈希 + List.copyOf 的全部开销。
        if (triggers.isEmpty()) return List.of();
        Set<StatEntry> out = new LinkedHashSet<>(unconditionalTriggers);
        // proj: 通配命中标记：与主循环同趟遍历 eventTags 求得，替代独立的
        // eventTags.stream().anyMatch(...)（Stream 管线为纯读、无副作用，前移不改结果）。
        // addAll(projTriggers) 仍在循环之后执行，故插入序与去重结果与原实现逐项一致。
        boolean hasProjTag = false;
        for (String t : eventTags) {
            List<StatEntry> l = triggersByTag.get(t);
            if (l != null) out.addAll(l);
            Set<String> parents = GateTag.parentsOf(t);
            // 绝大多数标签无父类别：空集直接跳过，省一次迭代器建立
            //（parentsOf 本身已是构建期预计算表，查询 O(1)，不是全表遍历）。
            if (!parents.isEmpty()) {
                for (String parent : parents) {
                    List<StatEntry> pl = triggersByTag.get(parent);
                    if (pl != null) out.addAll(pl);
                }
            }
            if (t.startsWith("proj:")) hasProjTag = true;
        }
        if (hasProjTag) out.addAll(projTriggers);
        return List.copyOf(out);
    }

    /** 资源转换规则索引：按「源资源」分组（只读），供资源扣费点折叠查询（扣A转扣B）。 */
    public Map<ResourceType, List<StatEntry>> convertIndex() {
        return convertIndex;
    }

    /** 伤害类型转换规则全量列表（只读，带来源键）：固定转换（DamageConvertStatEffect）与随机类型转换
     *  （RandomConvertStatEffect）同列表；来源为 tag 组合，运行时由 stage 逐伤害槽位用
     *  {@code slotTags.containsAll(fromTags)} 匹配，故不预分组。来源键供随机 CYCLE 计数器寻址。 */
    public List<IndexedEntry> damageConvertIndexed() {
        return damageConvertIndex;
    }

    /** 伤害类型转换规则全量列表（只读）：等价 {@code damageConvertIndexed()} 的 entry 投影。 */
    public List<StatEntry> damageConvertIndex() {
        return damageConvertIndex.stream().map(IndexedEntry::entry).toList();
    }

    /**
     * 周期施加条目全量列表（只读，带来源键）：供
     * {@link com.cimdy.coe.data.helper.engine.periodic.PeriodicDriver} 每 tick 节流驱动。
     */
    public List<IndexedEntry> periodicIndexed() {
        return periodicIndex;
    }

    /**
     * 本 store 是否需要为持有者维护 Presence 扫描（驱动 EntityOperationEvent 每 tick 进/出记录）：
     * 条目引用了 presence 事件节点（{@link #presenceNeeded()}）或存在周期施加条目
     * （{@link com.cimdy.coe.data.codec.support.stat.effect.grant.MultiStatEffect}，施加目标经 PRESENCE_* 选择器解析），
     * 或任一条目的实体获取条件树（{@link StatEntry#condition()}）依赖 presence 成员集。
     */
    public boolean needsPresenceTracking() {
        if (presenceNeeded || !periodicIndex.isEmpty()) return true;
        // 原实现判「target 的 Source 枚举名以 PRESENCE_ 开头」，但 TargetSelector.Source 现只剩
        // SELF / TRIGGER_TARGET 两个值（PRESENCE_* 枚举值已物理删除），该条件恒为 false —— 死分支。
        // 真实语义已迁到「条目的 condition 实体获取树是否依赖 presence」，故改判该树，
        // 判定复用 EventRefCollector 的同一方法（conditionNeedsPresence），与收集侧口径恒一致。
        for (StatEntry e : entries.values()) {
            if (EventRefCollector.conditionNeedsPresence(e.condition())) return true;
        }
        return false;
    }

    /**
     * 承伤转化规则全量列表（只读，含条目来源键）：维度靠 tag 组合表达，运行时由 stage 逐伤害槽位用 tag 匹配，
     * 故不预分组。CYCLE 随机承伤转换（{@code RandomTakenConvertStatEffect}）须按条目来源键寻址计数器，
     * 故升级为 {@link IndexedEntry}（与 {@link #damageConvertIndexed()} 同构）。
     */
    public List<IndexedEntry> damageTakenConvertIndexed() {
        return damageTakenConvertIndex;
    }

    private static Map<ResourceType, List<StatEntry>> buildConvertIndex(List<StatEntry> convertEntries) {
        Map<ResourceType, List<StatEntry>> m = new HashMap<>();
        for (StatEntry e : convertEntries) {
            ResourceConvertStatEffect c = (ResourceConvertStatEffect) e.effect();
            if (c == null) continue;
            m.computeIfAbsent(c.from(), k -> new ArrayList<>()).add(e);
        }
        Map<ResourceType, List<StatEntry>> r = new HashMap<>();
        m.forEach((k, v) -> r.put(k, List.copyOf(v)));
        return Map.copyOf(r);
    }

    /** 状态免疫条目全量列表（只读）：供 {@code StatEngine.isImmune} 扫描（免疫条目通常极少，全量遍历开销可忽略）。 */
    public List<StatEntry> immuneIndex() {
        return immuneIndex;
    }

    /** 掷骰修饰条目全量列表（只读）：供 {@code StatEngine.rollModifier} 扫描。 */
    public List<StatEntry> luckyIndex() {
        return luckyIndex;
    }

    /** 命中标志条目全量列表（只读）：供 {@code StatEngine.hasHitFlag} 扫描。 */
    public List<StatEntry> hitFlagIndex() {
        return hitFlagIndex;
    }

    /** 投送修饰条目全量列表（只读）：供 {@code StatEngine.delivery} 扫描。 */
    public List<StatEntry> deliveryIndex() {
        return deliveryIndex;
    }

    public static StatStore empty() {
        return new StatStore(Map.of());
    }

    /**
     * 计时推进：每游戏刻调用一次，递减全部临时条目的剩余时长，归零者自动移除。
     * 性能特征（按实体）：
     * <ul>
     *   <li>无计时条目（绝大多数实体，{@code expiries} 为空）→ 直接返回 {@code this}，仅一次 {@code isEmpty()} 判断，零 Map 复制、零索引重建；</li>
     *   <li>含计时条目但本 tick 均未满 → 仅复制一个很小的 {@code expiries} Map 并复用预计算索引（不触碰 {@code entries} 索引），开销恒定且与该实体 Stat 条目总数无关；</li>
     *   <li>仅当某条目到期的那一 tick → 触发一次 {@code entries} 重建（移除到期键并重算索引），属一次性代价。</li>
     * </ul>
     * 调用方：若返回值 {@code == this} 表示无变化，可跳过 {@code setData} 写回，进一步省去附件写入。
     */
    public StatStore tick() {
        if (expiries.isEmpty()) return this;
        Map<String, Integer> next = new LinkedHashMap<>();
        List<String> expired = new ArrayList<>();
        for (Map.Entry<String, Integer> e : expiries.entrySet()) {
            int remain = e.getValue() - 1;
            if (remain <= 0) expired.add(e.getKey());
            else next.put(e.getKey(), remain);
        }
        if (expired.isEmpty()) {
            // 仅计时域推进，条目内容未变 → 复用全部预计算索引，零索引重建；dataVersion 不变
            return new StatStore(entries, Map.copyOf(next), this.dataVersion,
                    index, tagIndex, conditionOnlyIndex, triggers,
                    triggersByTag, unconditionalTriggers, projTriggers,
                    convertIndex, damageConvertIndex, damageTakenConvertIndex,
                    immuneIndex, luckyIndex, hitFlagIndex, deliveryIndex, periodicIndex);
        }
        Map<String, StatEntry> m = new LinkedHashMap<>(entries);
        for (String id : expired) m.remove(id);
        return new StatStore(m, next, this.dataVersion + 1);
    }

}
