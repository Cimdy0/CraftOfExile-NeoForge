package com.cimdy.coe.data.gen.provider;

import com.cimdy.coe.data.gen.source.CoeDataPaths;
import com.cimdy.coe.data.gen.source.KeywordSource;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

/**
 * 数据生成：把关键字规范数据源 {@link KeywordSource} 按 id 分文件写出（C.9）。
 *
 * <p>每个关键字一个文件，落点 {@code src/generated/resources/data/coe/keywords/<id>.json}
 * （{@code namespaceRoot} 的 DATA_PACK 输出 = {@code data/coe}，再拼 {@code keywords/}），
 * 运行时由 {@code KeywordRegistry#loadFromResourceManager} 经客户端 {@code ResourceManager}
 * 枚举 {@code data/coe/keywords/*.json} 装载 —— <b>写出路径与装载路径严格对齐</b>。
 *
 * <p>JSON schema（单文件 = 单条关键字，与 {@code KeywordRegistry#parseEntry} 解码一致）：
 * <pre>{@code
 * { "id":"ailment", "title":"@coe:keyword.ailment",
 *   "aliases":[...], "keys":["coe:keyword.ailment"],
 *   "description":[
 *     ["异常状态是...（", "$[debuff]", "）。"],
 *     [...]
 *   ] }
 * }</pre>
 * {@code description} = <b>片段行数组</b>：每行 = 一串片段串（裸串=字面、{@code @key}=翻译键、
 * {@code $[id]}=关键字引用），由 {@link KeywordSource#fragmentsOf} 从既有描述行拆分得到。
 * {@code title} 以 {@code @} 前缀声明为翻译键（en_us / zh_cn 双语由 KeywordLang 生成）。
 */
public class KeywordProvider implements DataProvider {

    private final PackOutput output;

    public KeywordProvider(PackOutput output) {
        this.output = output;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        Path dir = CoeDataPaths.namespaceRoot(output).resolve("keywords");

        var futures = new java.util.ArrayList<CompletableFuture<?>>();
        for (KeywordSource.Entry e : KeywordSource.all()) {
            JsonObject o = new JsonObject();
            o.addProperty("id", e.id);
            // 走 @ 前缀转 translatable（见 KeywordRegistry#readComponent）：en_us 取 KeywordLang 生成的英文标题、
            // zh_cn 取中文标题；若直接写字面量中文，则 en_us 侧关键字内容仍是中文（英文分支等于死数据）。
            o.addProperty("title", "@coe:keyword." + e.id);

            JsonArray aliases = new JsonArray();
            for (String a : e.aliases) aliases.add(a);
            o.add("aliases", aliases);

            // C.9：description = 片段行数组（每行 = 一串片段串）
            JsonArray desc = new JsonArray();
            for (String line : e.description) {
                JsonArray frags = new JsonArray();
                for (String f : KeywordSource.fragmentsOf(line)) frags.add(f);
                desc.add(frags);
            }
            o.add("description", desc);

            JsonArray keys = new JsonArray();
            keys.add("coe:keyword." + e.id);
            o.add("keys", keys);

            futures.add(DataProvider.saveStable(cache, o, dir.resolve(e.id + ".json")));
        }
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0]));
    }

    @Override
    public String getName() {
        return "Keyword";
    }
}
