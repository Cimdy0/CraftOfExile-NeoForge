package com.cimdy.coe.data.codec.support.stat.entry;

import com.cimdy.coe.data.codec.skill.helper.ResourceType;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.stat.GateExpr;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.data.codec.support.status.StatusKind;
import com.cimdy.coe.data.codec.support.status.StatusType;
import com.cimdy.coe.data.codec.support.stat.effect.CullingStatEffect;
import com.cimdy.coe.data.codec.support.stat.effect.DamageConvertStatEffect;
import com.cimdy.coe.data.codec.support.stat.effect.DamageTakenConvertStatEffect;
import com.cimdy.coe.data.codec.support.stat.effect.GrantOnEventStatEffect;
import com.cimdy.coe.data.codec.support.stat.effect.LeechStatEffect;
import com.cimdy.coe.data.codec.support.stat.effect.MarkerStatEffect;
import com.cimdy.coe.data.codec.support.stat.effect.RecoupStatEffect;
import com.cimdy.coe.data.codec.support.stat.effect.ResourceConvertStatEffect;
import com.cimdy.coe.data.codec.support.stat.effect.ResourceStatEffect;
import com.cimdy.coe.data.codec.support.stat.effect.StatusStatEffect;
import com.cimdy.coe.data.codec.support.stat.effect.StatEffect;

import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.server.Bootstrap;
import net.minecraft.util.FormattedCharSequence;

import com.mojang.datafixers.util.Either;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 批次 4（Effect 侧翻译键覆盖）回归夹具：把 {@code StatEffect} 子类里硬编码的英文
 * {@code Component.literal} 替换为「模板键 + 值参数」结构后，锁死下列不变量（架构裁定 #3/#4：
 * 拆分复用、不为每种组合铸整句键）：
 *
 * <ul>
 *   <li>{@link MarkerStatEffect}（一个类背 40 个 kind）渲染不再含裸枚举名 / 冒号（旧 {@code "thorns_ignore_armor: 100%"}），
 *       改走 {@code stat_effect.marker} + {@code stat.value.marker.<kind>}（缺则回落 humanize 英文，可见降级）；</li>
 *   <li>{@link StatusStatEffect} 走 {@code stat_effect.apply_status} + 复用 {@code stat.value.status.<id>}；</li>
 *   <li>{@link ResourceStatEffect} 走 {@code stat_effect.heal}/{@code stat_effect.cost} + 资源组件（{@code stat.<resource>}）；</li>
 *   <li>{@link GrantOnEventStatEffect} 走 {@code stat_effect.grant_on_event} + {@code stat.value.grant_target.<t>} + 值键 + 数字（{@link com.cimdy.coe.data.codec.support.stat.entry.StatNumberFormat}）；</li>
 *   <li>{@link RecoupStatEffect} / {@link DamageConvertStatEffect} / {@link DamageTakenConvertStatEffect} /
 *       {@link ResourceConvertStatEffect} / {@link CullingStatEffect} / {@link LeechStatEffect} 同构：模板键 + 值键 + 数字。</li>
 * </ul>
 *
 * <p>本夹具需实例化 StatEffect 子类，其静态 CODEC 字段会访问 Minecraft 注册表，故在
 * {@code @BeforeAll} 中先调用 {@link Bootstrap#bootStrap()} 冻结内置注册表（离线可跑，无需数据包/资源），
 * 再把一份 en_us 映射注入为当前 {@link Language} 单例（仅含本测试用到的键）。
 * 注意：<b>本夹具不注入任何 {@code stat.value.marker.*}</b>（见 {@link #markerKindsNeverEmitBareEnumName}），
 * 以证明缺失键时回落 humanize 英文（可见降级，绝不显示裸键）。
 */
class StatEffectCoverageTest {

    private static final Map<String, String> EN_US = Map.ofEntries(
            // ── stat_effect.* 模板键（与 StatEffectKeys.TEMPLATE_ROWS 一致）──
            Map.entry("stat_effect.marker", "%1$s %2$s"),
            Map.entry("stat_effect.marker_against", "%1$s against %2$s"),
            Map.entry("stat_effect.apply_status", "apply status %1$s"),
            Map.entry("stat_effect.heal", "+%1$s"),
            Map.entry("stat_effect.cost", "-%1$s"),
            Map.entry("stat_effect.grant_on_event", "%1$s: %2$s @ %3$s"),
            Map.entry("stat_effect.recoup", "recoup a portion of damage taken as %1$s"),
            Map.entry("stat_effect.recoup_from", "%1$s from %2$s"),
            Map.entry("stat_effect.damage_convert", "%1$s → %2$s %3$s"),
            Map.entry("stat_effect.damage_convert_gain", "%1$s also gains %2$s %3$s"),
            Map.entry("stat_effect.damage_taken_convert_reroute", "%1$s reroutes %2$s for %3$s"),
            Map.entry("stat_effect.damage_taken_convert_extra", "%1$s also reduces %2$s for %3$s"),
            Map.entry("stat_effect.culling", "Culling >= %1$s%% max life"),
            Map.entry("stat_effect.leech", "leech life/mana/energy from dealt damage"),
            Map.entry("stat_effect.resource_convert", "%1$s → %2$s"),
            Map.entry("stat_effect.resource_convert_gain", "%1$s also takes %2$s"),
            Map.entry("stat_effect.resource_convert_src", " (only from %1$s)"),
            // ── 复用的 stat.value.* 值键（与 StatEffectKeys.VALUE_ROWS 一致；marker 故意不注入）──
            // canonical：status.ignite / status.poison 归一到外部权威裸键 stat.ignite / stat.poison
            Map.entry("stat.value.status.health", "Life"),
            Map.entry("stat.value.damage.physical", "Physical"),
            Map.entry("stat.value.damage.fire", "Fire"),
            Map.entry("stat.value.reduction.armor", "Armor"),
            Map.entry("stat.value.scope.all", "All"),
            Map.entry("stat.value.grant_target.status", "Status"),
            // canonical：grant_target.charge 并入 status_tag.charge（A2：以结果为一）
            Map.entry("stat.value.status_tag.charge", "Charge"),
            Map.entry("stat.value.grant_target.ancestral_boost", "Ancestral Boost"),
            Map.entry("stat.value.charge.power", "Power"),
            // canonical：cond_tag.crit → 外部权威裸键 stat.crit
            Map.entry("stat.crit", "Crit"),
            // ── 外部权威裸键（stat.*）：ALIAS 归一后的 canonical 靶子 ──
            Map.entry("stat.ignite", "Ignite"),
            Map.entry("stat.poison", "Poison"),
            // ── 资源名（ResourceType#getComponent 内部用 stat.<key>，天然可译）──
            Map.entry("stat.health", "Health"),
            Map.entry("stat.mana", "Mana")
    );

    /**
     * 纯 JUnit 环境无 FML loader，但本夹具需实例化 StatEffect 子类，其静态 CODEC 字段引用
     * {@code Attribute.CODEC}/{@code Attributes}（经由 BuiltInRegistries），在注册表未 bootstrap
     * 时会抛 {@code IllegalArgumentException: Not bootstrapped}。
     *
     * <p>NeoForge 的 {@link Bootstrap#bootStrap()} 在冻结注册表（line 60）之前会先初始化 Blocks
     * （line 51：{@code FireBlock.bootStrap()} → {@code Blocks.<clinit>} →
     * {@code FeatureFlags} → {@code FMLLoader.getCurrent()}），而纯 JUnit 环境无 FML loader，
     * 会抛 {@code IllegalStateException: There is no current FML Loader}。但该方法在抛错前已经：
     * ① 将 {@code isBootstrapped} 置为 true（line 45）；
     * ② 访问 {@code BuiltInRegistries.REGISTRY}（line 47）触发 {@code BuiltInRegistries.<clinit>}，
     *    把各内置注册表创建为“空且未冻结”的状态（其静态初始化里的 {@code checkBootstrapCalled}
     *    因 {@code isBootstrapped} 已 true 而通过）。
     *
     * <p>效果类静态初始化仅“引用” {@code Attribute.CODEC}/{@code Attributes}（注册表存在即可，
     * 无需冻结），故直接吞掉该 FML 异常即可，注册表已就绪。若抛出的是其它 IllegalStateException
     * （如 registries 未加载），则上抛以便暴露真实问题。
     *
     * <p><b>捕获 {@link Throwable} 而非 {@link Exception} 的原因</b>：{@code Bootstrap.bootStrap()}
     * 的 {@code FeatureFlags.<clinit>} 失败会被 JVM 包成 {@link ExceptionInInitializerError}
     * （是 {@link Error}，<b>不是</b> {@link Exception}）后从 {@code Blocks.<clinit>} 抛出 ——
     * 只 catch {@code Exception} 时它整条逃逸，表现为 {@code initializationError}，整个夹具一次都跑不起来。
     * 仍只吞「异常链含 FML Loader」这一种（{@link #isFmlLoaderAbsent}），其余一律上抛，不放水。
     */
    @BeforeAll
    static void bootstrapRegistries() {
        try {
            Bootstrap.bootStrap();
        } catch (Throwable t) {
            if (!isFmlLoaderAbsent(t)) {
                throw t;
            }
        }
    }

    /** 判断异常链是否仅由 NeoForge 缺失 FML loader 引起（纯 JUnit 环境必然如此）。 */
    private static boolean isFmlLoaderAbsent(Throwable t) {
        for (Throwable c = t; c != null; c = c.getCause()) {
            if (c instanceof IllegalStateException
                    && c.getMessage() != null
                    && c.getMessage().contains("FML Loader")) {
                return true;
            }
        }
        return false;
    }

    @BeforeAll
    static void installLanguage() {
        injectLanguage(EN_US);
    }

    @AfterAll
    static void restoreLanguage() {
        injectLanguage(Map.of());
    }

    // ---------------------------------------------------------------- 辅助：构造条目

    private static StatEntry entryOf(StatEffect eff, double amount) {
        return StatEntry.of(new ExpressionCodec.Value(Either.left(amount)), eff);
    }

    private static String frag(Component c) {
        return c.getString();
    }

    // ---------------------------------------------------------------- MarkerStatEffect：40 kind 全覆盖（无裸枚举名）

    /**
     * 核心不变量：MarkerStatEffect 携带 40 个 kind，旧代码直接渲染裸枚举名 + 冒号（如
     * {@code "thorns_ignore_armor: 100%"}）。改造后每个 kind 渲染为「值键名 + 百分比」，且本夹具
     * <b>不注入</b> stat.value.marker.*，故全部回落 humanize 英文（可见降级）。
     * 断言：① 不含裸 snake_case id；② 不含旧格式的冒号；③ 含百分比数字。
     */
    @Test
    void markerKindsNeverEmitBareEnumName() {
        for (MarkerStatEffect.Kind kind : MarkerStatEffect.Kind.values()) {
            MarkerStatEffect eff = new MarkerStatEffect(kind, List.of());
            StatEntry entry = StatEntry.of(new ExpressionCodec.Value(Either.left(1.0)), eff);
            String s = frag(eff.describeFragment(entry, Locale.ROOT).get(0));
            assertFalse(s.contains(kind.getSerializedName()),
                    "kind " + kind + " 不应渲染裸枚举名，实际: " + s);
            assertFalse(s.contains(":"),
                    "kind " + kind + " 不应再含旧格式冒号，实际: " + s);
            assertTrue(s.contains("100%"),
                    "kind " + kind + " 应包含百分比数值，实际: " + s);
        }
    }

    /** 命中键时正确解析译文（改写前此处恒为裸英文 / humanize 英文）。 */
    @Test
    void markerResolvesValueKeyWhenPresent() {
        Map<String, String> withKey = new HashMap<>(EN_US);
        withKey.put("stat.value.marker.defend", "Defend");
        injectLanguage(withKey);
        try {
            MarkerStatEffect eff = new MarkerStatEffect(MarkerStatEffect.Kind.DEFEND, List.of());
            StatEntry entry = StatEntry.of(new ExpressionCodec.Value(Either.left(1.0)), eff);
            assertEquals("Defend 100%",
                    frag(eff.describeFragment(entry, Locale.ROOT).get(0)));
        } finally {
            injectLanguage(EN_US);
        }
    }

    /** describe()（无 locale）必须委托到 describeFragment(entry, Locale.ROOT)（架构裁定）。 */
    @Test
    void markerDescribeDelegatesToFragmentRoot() {
        MarkerStatEffect eff = new MarkerStatEffect(MarkerStatEffect.Kind.DEFEND, List.of());
        StatEntry entry = StatEntry.of(new ExpressionCodec.Value(Either.left(0.5)), eff);
        assertEquals(frag(eff.describeFragment(entry, Locale.ROOT).get(0)),
                frag(eff.describe(entry).get(0)));
    }

    /** 命中上下文门控（conditionTags 非空）追加 against 子句，标签各自走 stat.value.cond_tag.<tag>。 */
    @Test
    void markerConditionTagsRenderViaCondTagKey() {
        MarkerStatEffect eff = new MarkerStatEffect(MarkerStatEffect.Kind.DEFEND, List.of("crit"));
        StatEntry entry = StatEntry.of(new ExpressionCodec.Value(Either.left(1.0)), eff);
        String s = frag(eff.describeFragment(entry, Locale.ROOT).get(0));
        assertTrue(s.contains("against"), "DEFEND 应追加 against 子句，实际: " + s);
        assertTrue(s.contains("Crit"), "conditionTags 应经 cond_tag 渲染为 Crit，实际: " + s);
    }

    // ---------------------------------------------------------------- StatusStatEffect

    @Test
    void statusAppliesStatusValueKey() {
        StatusType ignite = StatusType.dynamic("ignite", StatusKind.CONTROL, 100);
        StatusStatEffect eff = new StatusStatEffect(ignite, StatusStatEffect.Aspect.MAGNITUDE, GateExpr.ALWAYS);
        assertEquals("apply status Ignite", frag(eff.describeFragment(entryOf(eff, 30.0), Locale.ROOT).get(0)));
    }

    // ---------------------------------------------------------------- ResourceStatEffect

    /** HEAL 方向正数：模板 "+%1$s" + 资源组件（stat.health → "Health"）。数字经 df2 规整（5.0 → "5"）。 */
    @Test
    void resourceHealUsesModCoeKey() {
        ResourceStatEffect eff = new ResourceStatEffect(ResourceType.HEALTH, ResourceStatEffect.Direction.HEAL, GateExpr.ALWAYS);
        assertEquals("+5 Health", frag(eff.describeFragment(entryOf(eff, 5.0), Locale.ROOT).get(0)));
    }

    /** 分数值同样经资源组件渲染（5.5 → "5.5"）。 */
    @Test
    void resourceHealFractionalFormatsNumber() {
        ResourceStatEffect eff = new ResourceStatEffect(ResourceType.HEALTH, ResourceStatEffect.Direction.HEAL, GateExpr.ALWAYS);
        assertEquals("+5.5 Health", frag(eff.describeFragment(entryOf(eff, 5.5), Locale.ROOT).get(0)));
    }

    /** COST 方向正数：模板 "-%1$s"。 */
    @Test
    void resourceCostUsesMinusTemplate() {
        ResourceStatEffect eff = new ResourceStatEffect(ResourceType.HEALTH, ResourceStatEffect.Direction.COST, GateExpr.ALWAYS);
        assertEquals("-5 Health", frag(eff.describeFragment(entryOf(eff, 5.0), Locale.ROOT).get(0)));
    }

    /** HEAL 方向负数 = 反方向（COST 语义），模板 "-%1$s"。 */
    @Test
    void resourceHealNegativeFlipsToCost() {
        ResourceStatEffect eff = new ResourceStatEffect(ResourceType.HEALTH, ResourceStatEffect.Direction.HEAL, GateExpr.ALWAYS);
        assertEquals("-5 Health", frag(eff.describeFragment(entryOf(eff, -5.0), Locale.ROOT).get(0)));
    }

    // ---------------------------------------------------------------- GrantOnEventStatEffect

    /** STATUS 授予分支：grant_target.status + stat.value.status.poison + 概率数字。 */
    @Test
    void grantOnEventStatusBranch() {
        GrantOnEventStatEffect eff = new GrantOnEventStatEffect(GrantOnEventStatEffect.GrantTarget.STATUS, "poison", GateExpr.ALWAYS);
        assertEquals("Status: Poison @ 0.25", frag(eff.describeFragment(entryOf(eff, 0.25), Locale.ROOT).get(0)));
    }

    /** CHARGE 授予分支：grant_target.charge + stat.value.charge.power + 概率数字。 */
    @Test
    void grantOnEventChargeBranch() {
        GrantOnEventStatEffect eff = new GrantOnEventStatEffect(GrantOnEventStatEffect.GrantTarget.CHARGE, "power", GateExpr.ALWAYS);
        assertEquals("Charge: Power @ 0.1", frag(eff.describeFragment(entryOf(eff, 0.1), Locale.ROOT).get(0)));
    }

    /** ANCESTRAL_BOOST 分支：param 为字符串 id，按设计以 literal 回落（非值键）。 */
    @Test
    void grantOnEventAncestralBoostLiteralParam() {
        GrantOnEventStatEffect eff = new GrantOnEventStatEffect(
                GrantOnEventStatEffect.GrantTarget.ANCESTRAL_BOOST, "ancestral_boost", GateExpr.ALWAYS);
        String s = frag(eff.describeFragment(entryOf(eff, 1.0), Locale.ROOT).get(0));
        assertTrue(s.contains("Ancestral Boost"), "应渲染 grant_target 译文，实际: " + s);
        assertTrue(s.contains("ancestral_boost"), "ANCESTRAL_BOOST 的 param 为字符串 id，按设计以 literal 回落，实际: " + s);
    }

    // ---------------------------------------------------------------- RecoupStatEffect

    /** 默认（无 fromType）：recoup 模板 + 资源值键（stat.value.status.health，复用命名空间）。 */
    @Test
    void recoupDefaultNoFromType() {
        RecoupStatEffect eff = new RecoupStatEffect(Set.of("health"));
        // RecoupStatEffect.entry 返回 Stat（ratio 即 amount）
        StatEntry entry = RecoupStatEffect.entry(Set.of("health"), 0.08);
        assertEquals("recoup a portion of damage taken as Life", frag(eff.describeFragment(entry, Locale.ROOT).get(0)));
    }

    /** 带 fromType：追加 recoup_from + stat.value.damage.physical。 */
    @Test
    void recoupWithFromType() {
        RecoupStatEffect eff = new RecoupStatEffect(Set.of("health"), GateExpr.of("physical"));
        StatEntry entry = RecoupStatEffect.entry(Set.of("health"), GateExpr.of("physical"), 0.08);
        assertEquals("recoup a portion of damage taken as Life from Physical",
                frag(eff.describeFragment(entry, Locale.ROOT).get(0)));
    }

    // ---------------------------------------------------------------- DamageConvertStatEffect

    /** REPLACE 单来源→目标：damage_convert 模板 + 来源/目标伤害类型值键 + 比例数字。 */
    @Test
    void damageConvertReplace() {
        DamageConvertStatEffect eff = new DamageConvertStatEffect(
                Set.of("physical"), "fire", DamageConvertStatEffect.Mode.REPLACE, false, GateExpr.ALWAYS);
        StatEntry entry = DamageConvertStatEffect.entry("physical", "fire", DamageConvertStatEffect.Mode.REPLACE, 0.25);
        assertEquals("Physical → Fire 0.25", frag(eff.describeFragment(entry, Locale.ROOT).get(0)));
    }

    /** GAIN 模式：damage_convert_gain 模板。 */
    @Test
    void damageConvertGain() {
        DamageConvertStatEffect eff = new DamageConvertStatEffect(
                Set.of("physical"), "fire", DamageConvertStatEffect.Mode.GAIN, false, GateExpr.ALWAYS);
        StatEntry entry = DamageConvertStatEffect.entry("physical", "fire", DamageConvertStatEffect.Mode.GAIN, 0.25);
        assertEquals("Physical also gains Fire 0.25", frag(eff.describeFragment(entry, Locale.ROOT).get(0)));
    }

    // ---------------------------------------------------------------- DamageTakenConvertStatEffect

    /** REROUTE + ALWAYS 作用域（for all）：damage_taken_convert_reroute + reduction + scope.all。 */
    @Test
    void damageTakenConvertRerouteForAll() {
        DamageTakenConvertStatEffect eff = new DamageTakenConvertStatEffect("armor", DamageTakenConvertStatEffect.Mode.REROUTE, GateExpr.ALWAYS);
        StatEntry entry = DamageTakenConvertStatEffect.entry("armor", DamageTakenConvertStatEffect.Mode.REROUTE, 0.25, Set.of());
        assertEquals("25% reroutes Armor for All", frag(eff.describeFragment(entry, Locale.ROOT).get(0)));
    }

    /** EXTRA 模式：damage_taken_convert_extra 模板。 */
    @Test
    void damageTakenConvertExtra() {
        DamageTakenConvertStatEffect eff = new DamageTakenConvertStatEffect("armor", DamageTakenConvertStatEffect.Mode.EXTRA, GateExpr.ALWAYS);
        StatEntry entry = DamageTakenConvertStatEffect.entry("armor", DamageTakenConvertStatEffect.Mode.EXTRA, 0.25, Set.of());
        assertEquals("25% also reduces Armor for All", frag(eff.describeFragment(entry, Locale.ROOT).get(0)));
    }

    // ---------------------------------------------------------------- CullingStatEffect

    /** 百分比数字整数截断（0.10 → 10），模板含字面 "%%"。 */
    @Test
    void cullingFormatsPercentInteger() {
        CullingStatEffect eff = new CullingStatEffect(GateExpr.ALWAYS);
        StatEntry entry = CullingStatEffect.entry(0.10, Set.of());
        assertEquals("Culling >= 10% max life", frag(eff.describeFragment(entry, Locale.ROOT).get(0)));
    }

    // ---------------------------------------------------------------- LeechStatEffect

    /** 常量整句 → 单模板键（无参数）。 */
    @Test
    void leechConstantSentence() {
        LeechStatEffect eff = new LeechStatEffect(GateExpr.ALWAYS);
        StatEntry entry = LeechStatEffect.entry(1.0, Set.of());
        assertEquals("leech life/mana/energy from dealt damage", frag(eff.describeFragment(entry, Locale.ROOT).get(0)));
    }

    // ---------------------------------------------------------------- ResourceConvertStatEffect

    /** REPLACE mana→health：resource_convert 模板 + 两资源组件（各自带数字与 stat 名）。 */
    @Test
    void resourceConvertReplace() {
        ResourceConvertStatEffect eff = new ResourceConvertStatEffect(
                ResourceType.MANA, ResourceType.HEALTH, ResourceConvertStatEffect.Mode.REPLACE, GateExpr.ALWAYS, Set.of());
        StatEntry entry = ResourceConvertStatEffect.entry(ResourceType.MANA, ResourceType.HEALTH, ResourceConvertStatEffect.Mode.REPLACE, 1.0);
        assertEquals("1 Mana → 1 Health", frag(eff.describeFragment(entry, Locale.ROOT).get(0)));
    }

    // ---------------------------------------------------------------- 辅助：Language 注入

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
