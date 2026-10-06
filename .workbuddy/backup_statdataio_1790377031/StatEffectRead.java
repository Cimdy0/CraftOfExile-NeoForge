package com.cimdy.coe.data.codec.support.stat.effect;

import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.data.codec.support.stat.StatContext;
import com.cimdy.coe.data.codec.support.stat.StatEvalContext;
import com.cimdy.coe.data.codec.support.stat.TagExpr;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.data.gen.StatDataIO;
import com.cimdy.coe.event.TriggerContext;
import com.cimdy.coe.event.data.LoadEvent;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Stat 效果的「read」形态：仅持有路径 {@code ref}，调用时从 {@link LoadEvent#STAT_EFFECT_MAP}
 * 读取对应的 {@link StatEffect} 并转发全部行为（与 {@link com.cimdy.coe.data.codec.support.stat.entry.StatRead}、
 * {@code ExpressionCodec.Read} 同源）。用于复用「数据生成时落盘的 stat_effect 资源」，避免重复定义。
 *
 * <p>此即「节点自身实现 read 形式」——引用能力内聚在 {@link StatEffect} 类型内部（经
 * {@link StatEffectTypes} 按 {@code type:"read"} 分派），而非由外层 {@code Either<StatEffect, Identifier>} 包裹。
 *
 * <p>JSON 形态：{@code { "type": "read", "ref": "coe:stat_effect/<slug>" }}。
 *
 * <p>缓存字段类：生成期经 {@link #of} 单独生产、内容命名、去重并缓存为路径键（基于内容与所引用 id
 * 的可读名，见 {@link StatDataIO#partName}），本类即该路径键的持有者；{@link #all()} 暴露去重后的
 * 内容供 Provider 落盘（取代旧 {@code StatPartRegistry} 中心化登记）。
 */
public record StatEffectRead(Identifier ref) implements StatEffect {

    // 内容去重缓存：规范化 JSON → 路径键 / 已分配文件名 / 内容（键即落盘文件名）
    private static final Map<String, Identifier> JSON_TO_ID = new LinkedHashMap<>();
    private static final Map<String, String> NAMES = new LinkedHashMap<>();
    private static final Map<Identifier, StatEffect> BY_ID = new LinkedHashMap<>();

    /** effect 部件生产者：登记 + 缓存 + 返回路径引用（read）。 */
    public static StatEffectRead of(StatEffect eff) {
        Identifier id = StatDataIO.nameId(JSON_TO_ID, NAMES,
                StatDataIO.canonicalEffectJson(eff), BY_ID, eff, CraftOfExile.MODID);
        return new StatEffectRead(id);
    }

    /** 去重后的 effect 部件（键即落盘文件名）。 */
    public static Map<Identifier, StatEffect> all() {
        return Map.copyOf(BY_ID);
    }

    /** 引用型 MapCodec：仅含 {@code ref}。 */
    public static final MapCodec<StatEffectRead> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Identifier.CODEC.fieldOf("ref").forGetter(StatEffectRead::ref)
    ).apply(i, StatEffectRead::new));

    /** 引用实现对应的完整 Codec（供 {@link StatEffectTypes} 注册表登记）。 */
    public static final Codec<StatEffectRead> CODEC = MAP_CODEC.codec();

    /** 从数据生成缓存解析本引用指向的 StatEffect（缺失则报错）。 */
    private StatEffect resolve() {
        StatEffect e = LoadEvent.STAT_EFFECT_MAP.get(this.ref);
        if (e == null) {
            throw new IllegalStateException("StatEffectRead 引用了未注册的效果: " + this.ref);
        }
        return e;
    }

    /** 分发标记：引用缓存。 */
    @Override
    public String type() {
        return "read";
    }

    @Override
    public TagExpr tags() {
        return resolve().tags();
    }

    @Override
    public StatTargets defaultTargets() {
        return resolve().defaultTargets();
    }

    @Override
    public boolean isTrigger() {
        return resolve().isTrigger();
    }

    @Override
    public EffectKind indexKind() {
        return resolve().indexKind();
    }

    @Override
    public void onTrigger(StatEntry entry, StatContext scope, StatEvalContext evalCtx,
                          @Nullable TriggerContext tc) {
        resolve().onTrigger(entry, scope, evalCtx, tc);
    }

    @Override
    public List<Component> describe(StatEntry entry) {
        return resolve().describe(entry);
    }
}
