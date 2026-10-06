package com.cimdy.coe.data.gen;

import com.cimdy.coe.data.codec.support.math.context.ContextNode;
import com.cimdy.coe.data.codec.support.stat.effect.StatEffect;
import com.cimdy.coe.data.codec.support.stat.effect.StatEffectTypes;
import com.cimdy.coe.data.codec.support.stat.effect.StatTargets;
import com.google.gson.*;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.Identifier;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Stat 数据生成共享 IO / 内容命名工具：被各子部件 read 字段类（{@link com.cimdy.coe.data.codec.support.stat.effect.StatEffectRead} /
 * {@link com.cimdy.coe.data.codec.support.stat.effect.StatTargetsRead} /
 * {@link com.cimdy.coe.data.codec.support.math.context.ContextNodeRead}）与各子 Provider 复用，
 * 提供「规范化 + 基于内容的可读命名」与稳定落盘能力。
 *
 * <p>本类不持有索引构建逻辑：
 * 子部件的路径键由各自 read 字段类在正向生成期经 {@link #nameId} 算得并登记，文件名取自
 * {@link #partName}（基于部件内容与所引用 id 的可读名，取代旧 64 位 SHA-256 slug），
 * 跨运行稳定、与运行期 LoadEvent 仅取文件名提取键（裸 {@code coe:<name>}）对齐。
 *
 * <p>数值（amount）不作为独立部件生成——Stat 生成本就不含具体 amount，数值以 {@code $value}
 * 模板内联保留在条目内，由使用方经 {@code StatRead.vars} 注入，故本类不提供数值内容寻址。
 */
public final class StatDataIO {
    private StatDataIO() {
    }

    // ============================ 规范化 ============================

    /** 递归规范化：对象键按字典序排序，数组元素按规范化字符串排序，消除编码迭代顺序不确定。 */
    public static JsonElement canonicalize(JsonElement e) {
        if (e instanceof JsonObject o) {
            JsonObject out = new JsonObject();
            ArrayList<String> keys = new ArrayList<>(o.keySet());
            Collections.sort(keys);
            for (String k : keys) out.add(k, canonicalize(o.get(k)));
            return out;
        }
        if (e instanceof JsonArray a) {
            ArrayList<JsonElement> items = new ArrayList<>();
            for (JsonElement x : a) items.add(canonicalize(x));
            items.sort(Comparator.comparing(JsonElement::toString));
            JsonArray out = new JsonArray();
            for (JsonElement x : items) out.add(x);
            return out;
        }
        return e;
    }

    /**
     * 子部件内容寻址命名：从规范化 JSON 提取全部叶值（字符串 / 数值 / 布尔）做内容词元，排序后以下划线
     * 拼接。词元天然来自部件内容与其引用的 id（属性 / 状态 / 资源 id 等），故文件名即「内容 + ids」
     * 可读名，取代旧 64 位 SHA-256 slug。数组元素顺序不影响命名（词元集合与顺序无关），
     * 故同语义不同排列的部件恒得同名。同名冲突或超长时回退为「基础名 + _ + 短哈希」后缀，保证唯一稳定。
     */
    public static String partName(String canonicalJson) {
        JsonElement e = JsonParser.parseString(canonicalJson);
        TreeSet<String> tokens = new TreeSet<>();
        collectTokens(e, tokens);
        StringBuilder sb = new StringBuilder();
        for (String t : tokens) {
            if (!sb.isEmpty()) sb.append('_');
            sb.append(t);
        }
        String name = sanitize(sb.toString());
        if (name.isEmpty()) name = "empty";
        if (name.length() > 180) {
            String head = sanitize(name.substring(0, 170));
            name = head + "_" + sha256hex(canonicalJson).substring(0, 8);
        }
        return name;
    }

    /** 递归收集 JSON 全部叶值词元（字符串 / 数值 / 布尔），忽略对象键。 */
    private static void collectTokens(JsonElement e, TreeSet<String> out) {
        if (e.isJsonObject()) {
            for (Map.Entry<String, JsonElement> me : e.getAsJsonObject().entrySet()) {
                collectTokens(me.getValue(), out);
            }
        } else if (e.isJsonArray()) {
            for (JsonElement x : e.getAsJsonArray()) collectTokens(x, out);
        } else if (e.isJsonPrimitive()) {
            JsonPrimitive p = e.getAsJsonPrimitive();
            String s = p.isString() ? p.getAsString()
                    : p.isBoolean() ? Boolean.toString(p.getAsBoolean())
                    : p.getAsString();
            String t = sanitize(s);
            if (!t.isEmpty()) out.add(t);
        }
    }

    /** 路径安全清洗：小写字母 / 数字保留，其余（含 : . / 等）转为下划线，折叠多余下划线并去首尾下划线。 */
    private static String sanitize(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c >= 'a' && c <= 'z') sb.append(c);
            else if (c >= 'A' && c <= 'Z') sb.append((char) (c - 'A' + 'a'));
            else if (c >= '0' && c <= '9') sb.append(c);
            else sb.append('_');
        }
        String r = sb.toString().replaceAll("__+", "_");
        while (r.startsWith("_")) r = r.substring(1);
        while (r.endsWith("_")) r = r.substring(0, r.length() - 1);
        return r;
    }

    /**
     * 短哈希（仅用于命名冲突 / 超长回退后缀）：对规范化 JSON 取 SHA-256 十六进制。
     */
    public static String sha256hex(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] b = md.digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(b.length * 2);
            for (byte x : b) sb.append(String.format("%02x", x));
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /** 把 effect 经 CODEC 编码为规范化（键排序）JSON 字符串，供内容命名与稳定键。 */
    public static String canonicalEffectJson(StatEffect eff) {
        DataResult<JsonElement> res = StatEffectTypes.CODEC.encodeStart(JsonOps.INSTANCE, eff);
        JsonElement json = res.result().orElseThrow(() ->
                new IllegalStateException("Failed to encode effect: " + eff.type()));
        return canonicalize(json).toString();
    }

    /** 把 targets 经 CODEC 编码为规范化（键排序）JSON 字符串，供内容命名与稳定键。 */
    public static String canonicalTargetJson(StatTargets tgt) {
        DataResult<JsonElement> res = StatTargets.CODEC.encodeStart(JsonOps.INSTANCE, tgt);
        JsonElement json = res.result().orElseThrow(() ->
                new IllegalStateException("Failed to encode target: " + tgt));
        return canonicalize(json).toString();
    }

    /** 把 conditions 列表经 CODEC 编码为规范化（键排序）JSON 字符串，供内容命名与稳定键。 */
    public static String canonicalConditionJson(java.util.List<ContextNode> conds) {
        DataResult<JsonElement> res = ContextNode.CODEC.listOf().encodeStart(JsonOps.INSTANCE, conds);
        JsonElement json = res.result().orElseThrow(() ->
                new IllegalStateException("Failed to encode conditions: " + conds));
        return canonicalize(json).toString();
    }

    /**
     * 共享「内容寻址命名 + 去重登记」：供各子部件 read 字段类（{@link com.cimdy.coe.data.codec.support.stat.effect.StatEffectRead} /
     * {@link com.cimdy.coe.data.codec.support.stat.effect.StatTargetsRead} /
     * {@link com.cimdy.coe.data.codec.support.math.context.ContextNodeRead}）复用。
     * 先查内容 JSON 是否已分配 id；否则用 {@link #partName} 算可读名，遇同名冲突（不同内容）追加
     * {@code _2 / _3 …} 后缀，再把内容存入 {@code store} 落盘（键即路径键 / 落盘文件名）。
     */
    public static <T> Identifier nameId(Map<String, Identifier> jsonToId, Map<String, String> used,
                                       String canonicalJson, Map<Identifier, T> store, T content, String modid) {
        Identifier id = jsonToId.get(canonicalJson);
        if (id != null) return id;
        String name = partName(canonicalJson);
        String base = name;
        int n = 2;
        while (used.containsKey(name) && !used.get(name).equals(canonicalJson)) {
            name = base + "_" + (n++);
        }
        used.put(name, canonicalJson);
        id = Identifier.fromNamespaceAndPath(modid, name);
        jsonToId.put(canonicalJson, id);
        store.put(id, content);
        return id;
    }

    /** 经 CODEC 编码为 JSON 并稳定落盘（CachedOutput）。 */
    public static <T> CompletableFuture<?> save(CachedOutput cache, Path dir, String name,
                                                Codec<T> codec, T def) {
        DataResult<JsonElement> res = codec.encodeStart(JsonOps.INSTANCE, def);
        JsonElement json = res.result().orElseThrow(() ->
                new IllegalStateException("Failed to encode stat " + name + ": " + res.error().orElse(null)));
        return DataProvider.saveStable(cache, json, dir.resolve(name + ".json"));
    }

}
