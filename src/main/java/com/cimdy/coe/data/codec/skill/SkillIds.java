package com.cimdy.coe.data.codec.skill;

import com.cimdy.coe.CraftOfExile;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;
import com.cimdy.coe.data.codec.support.stat.StatIds;

/**
 * Skill 路径唯一缓存词表 —— 与 Stat 侧 {@code StatIds} 同构的技能侧对应物：集中缓存全部技能 id 的
 * {@link Identifier}，避免散落的字符串字面量造成拼写漂移与改名遗漏。
 *
 * <p>与 {@code StatIds} 的<b>唯一差别</b>是装载方式：{@code StatIds} 的条目由脚本生成为静态常量字段
 * （反射收集建索引），而技能 id 的权威来源是数据生成本身（宝石 {@code red/xxx} / {@code blue/xxx} /
 * {@code green/xxx} 与其 {@code preset_<gem>} 预设、共享 {@code generic_*} 预设），
 * 故本类改为<b>生成期登记</b>：注册点经 {@link #id(String)} / {@link #register(Identifier)} 把路径写入
 * 缓存，引用方经 {@link #byName(String)} 取用。这正是「生成期按路径生成」的落点。
 *
 * <h2>用法</h2>
 * <ul>
 *   <li><b>生成期登记</b>（注册点）：{@code SkillIds.id("generic_curse")} /
 *       {@code SkillIds.register(alreadyBuiltId)} —— 幂等，同 path 恒返回同一实例。</li>
 *   <li><b>引用</b>（其他位置使用技能）：{@code new SkillRead(SkillIds.byName("generic_curse"), vars)}
 *       —— 走 {@code SkillIds} + {@code SkillRead} 引用系统，未登记即抛错（fail-fast）。</li>
 * </ul>
 *
 * <p>本类<b>只做路径缓存</b>：不做内容寻址去重（技能定义本身不重复，无需该能力）、不做哈希/指纹、
 * 不做共享合并、不建任何额外索引；技能实体的装载与引用链展开仍是
 * {@code LoadEvent.SKILL_ENTRY_MAP} + {@code SkillRead.resolve()} 的职责，本类不介入。
 */
public final class SkillIds {

    /** path → Identifier（技能 id 恒为 {@code coe:<path>} 命名空间，故按 path 单键索引即可）。 */
    private static final Map<Identifier, Identifier> BY_PATH = new HashMap<>();

    private SkillIds() {
    }

    /**
     * 登记路径（幂等）：缓存 {@code coe:<path>} 并返回其 {@link Identifier}。
     * 同一 path 反复调用恒返回同一实例，供生成期各注册点安全复用。
     */
    public static Identifier id(String path) {
        Identifier id = Identifier.fromNamespaceAndPath(CraftOfExile.MODID, path);
        return BY_PATH.computeIfAbsent(id, p -> p);
    }

    /** 登记既有 {@link Identifier}（按 path 建索引，与 {@link #id(String)} 共用同一缓存）。 */
    public static Identifier register(Identifier id) {
        BY_PATH.putIfAbsent(id, id);
        return id;
    }

    /**
     * 取已登记路径：供 {@code SkillRead} 引用（{@code new SkillRead(SkillIds.byName(path))}）。
     * 未登记即抛错（fail-fast），避免拼写漂移导致的静默悬空引用。
     */
    public static Identifier byName(String path) {
        Identifier r = BY_PATH.get(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, path));
        if (r == null) {
            throw new IllegalStateException("SkillIds 未缓存技能路径: " + path);
        }
        return r;
    }

    /** 判断路径是否已登记。 */
    public static boolean contains(String path) {
        return BY_PATH.containsKey(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, path));
    }

}
