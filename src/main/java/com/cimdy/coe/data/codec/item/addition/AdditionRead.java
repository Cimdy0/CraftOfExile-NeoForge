package com.cimdy.coe.data.codec.item.addition;

import com.cimdy.coe.data.codec.item.mod.identity.ModConflict;
import com.cimdy.coe.data.codec.support.stat.StatIds;
import com.cimdy.coe.data.codec.support.stat.entry.StatRead;
import com.cimdy.coe.data.codec.support.stat.entry.ValueRange;
import com.cimdy.coe.event.data.LoadEvent;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * 引用型词缀读取器（参考 {@link StatRead} 的引用模式）。
 *
 * <p>仅持有<b>一条路径</b>（{@link Identifier}），指向数据生成缓存的
 * {@link LoadEvent#ADDITION_MAP} 中注册的 {@link AdditionType}；全部行为在消费时
 * 惰性解析后转发。不缓存被引用实体（避免加载期顺序耦合），也不另存中间路径——
 * Load 缓存与直接读取是两件事，本类只持有路径并按需查表。
 *
 * <p><b>数据生成期可直接作为字段使用</b>：生成时本引用器只被<b>编码</b>（写出 {@code ref}），
 * 不触发 {@code resolve()}，故无需数据包加载、无需 {@code ADDITION_MAP} 已填充。</p>
 *
 * <h2>身份（冲突判定）</h2>
 * 本引用器即一条已注册词缀的代理，其身份恒等于 {@link #ref()}（命名空间限定的注册路径
 * {@code coe:<name>}）—— 同一引用 = 同一身份 = 同一冲突组。冲突判定由消费方经
 * {@link ModConflict#resolveConflict} 对 {@code ref} 直接解析，不经反向推导。
 *
 * @param ref 路径（= 身份标识）：指向 ADDITION_MAP 中注册的词缀。
 */
public record AdditionRead(Identifier ref) implements AdditionType {

    /**
     * 以 coe 命名空间下的路径便捷构造引用（内部经 {@link StatIds#id} 生成命名空间限定标识）。
     */
    public static AdditionRead of(String path) {
        return new AdditionRead(StatIds.id(path));
    }

    /**
     * 惰性解析被引用词缀；缺失即报错（与 {@link StatRead} 同口径 fail-fast）。
     *
     * <p><b>生成期无需考虑本方法</b>：数据生成时 {@code AdditionRead} 只是被写出的<b>字段</b>
     * （{@link #MAP_CODEC} 仅编码 {@code ref}），<b>不做解析</b>，故 {@code ADDITION_MAP} 此时为空
     * 不影响生成。本方法只在<b>运行时消费</b>（contribute / describe / effects …）时才被调用，
     * 那时 {@code ADDITION_MAP} 已由资源加载填充完毕。
     */
    private AdditionType resolve() {
        AdditionType t = LoadEvent.ADDITION_MAP.get(ref);
        if (t == null) {
            throw new IllegalStateException("AdditionRead 引用了未注册的词缀: " + ref);
        }
        return t;
    }

    // ── 委托 AdditionType 接口（惰性解析后透明转发）────────────

    @Override
    public String type() {
        return "read";
    }

    @Override
    public void contribute(ContributionSink sink, double rolled) {
        resolve().contribute(sink, rolled);
    }

    @Override
    public List<String> valueSlots() {
        return resolve().valueSlots();
    }

    @Override
    public AdditionType forSlot(String slot) {
        AdditionType delegate = resolve();
        AdditionType sub = delegate.forSlot(slot);
        return sub == delegate ? this : sub;
    }

    @Override
    public List<Component> describe(double rolled, Locale locale) {
        return resolve().describe(rolled, locale);
    }

    @Override
    public List<Component> describe(double rolled, Locale locale, Optional<ValueRange> range) {
        return resolve().describe(rolled, locale, range);
    }

    @Override
    public List<Effect> effects() {
        return resolve().effects();
    }

    // ── 引用型 Codec（{ "type": "read", "ref": "<identifier>" }）────────────

    public static final MapCodec<AdditionRead> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("ref").forGetter(AdditionRead::ref)
    ).apply(instance, AdditionRead::new));

    public static final Codec<AdditionRead> CODEC = MAP_CODEC.codec();
}
