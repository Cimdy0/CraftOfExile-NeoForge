package com.cimdy.coe.ui.widget.keyword;

import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.ui.widget.render.Draw;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 关键字注册表：数据驱动的「翻译键 / 文本 ↔ 关键字条目」索引。
 *
 * <p>匹配源有两种（对齐用户需求「按 tooltip 文字或某个与关键字匹配的翻译键」）：
 * <ul>
 *   <li><b>纯文本</b>：对 tooltip 每一行的展示字符串做大小写不敏感 + 词边界的子串匹配；
 *       这是覆盖「物品栏 / 任意未知 tooltip」的主力路径，无需业务方任何配合。</li>
 *   <li><b>翻译键（精确匹配）</b>：若某 tooltip 行本身是一个 {@link TranslatableComponent}，
 *       取其翻译键在 {@link #byKey} 中做<b>精确</b>查找（大小写不敏感）。这覆盖了「行文本与
 *       关键字标题不同、但翻译键能唯一确定语义」的情形，是比模糊子串更稳的维度。
 *       数据文件里一个条目可声明 {@code key} / {@code keys} 字段（以 {@code @} 开头即视为翻译键）。</li>
 * </ul>
 *
 * <p>数据来源：{@link #loadDefault} 经客户端 {@link ResourceManager} 枚举 {@code data/coe/keywords/*.json}
 * （每个关键字一个文件，按 id 组织；文件由 {@code KeywordProvider} 生成到
 * {@code src/generated/resources/data/coe/keywords/<id>.json}），因此新增/修正机制知识只需改
 * {@code KeywordSource} 并重跑 datagen、不动运行时代码。运行时装载路径与 datagen 写出路径一致。
 */
public class KeywordRegistry {

    private static KeywordRegistry INSTANCE;

    private final Map<String, KeywordEntry> byId = new LinkedHashMap<>();
    private final Map<String, KeywordEntry> byKey = new HashMap<>();
    private final List<KeywordEntry> all = new ArrayList<>();

    /** 懒加载单例：首次访问即枚举客户端资源包 {@code data/coe/keywords/*.json} 装载。 */
    public static KeywordRegistry instance() {
        if (INSTANCE == null) {
            INSTANCE = new KeywordRegistry();
            INSTANCE.loadDefault();
        }
        return INSTANCE;
    }

    public void register(KeywordEntry e) {
        if (e == null || byId.containsKey(e.id)) return;
        byId.put(e.id, e);
        all.add(e);
        // 统一 Locale.ROOT：tr_TR 下无 Locale 的 "I"→"ı" 会让大小写折叠结果与写入侧不一致而漏匹配
        for (String k : e.keyStrings()) byKey.putIfAbsent(k.toLowerCase(Locale.ROOT), e);
    }

    public KeywordEntry get(String id) { return byId.get(id); }

    public List<KeywordEntry> all() { return all; }

    /** 翻译键精确查找：命中返回条目，未命中返回 null（大小写不敏感）。 */
    public KeywordEntry findByKey(String key) {
        if (key == null) return null;
        return byKey.get(key.toLowerCase(Locale.ROOT));
    }

    /** 懒加载单例的默认装载入口：枚举客户端资源包 {@code data/coe/keywords/*.json}（按 id 分文件）。 */
    public void loadDefault() {
        ResourceManager rm;
        try {
            rm = Minecraft.getInstance().getResourceManager();
        } catch (Exception e) {
            CraftOfExile.LOGGER.warn("[coe-keyword] 客户端资源管理器暂不可用，关键字注册表为空", e);
            return;
        }
        if (rm == null) {
            CraftOfExile.LOGGER.warn("[coe-keyword] 客户端资源管理器为空，关键字注册表为空"
                    + "（请先运行 datagen 生成 data/coe/keywords/*.json）");
            return;
        }
        loadFromResourceManager(rm);
    }

    /**
     * 从客户端 {@link ResourceManager} 枚举 {@code data/coe/keywords/*.json} 并逐个解码（按 id 分文件）。
     *
     * <p>单文件 JSON 形状（与 {@code KeywordProvider} 写出一致）：
     * <pre>{@code
     * { "id":"ailment", "title":"@coe:keyword.ailment",
     *   "aliases":["Ailment","Ailments"],
     *   "keys":["coe:keyword.ailment"],
     *   "description":[
     *     ["异常状态是...负面效果（", "$[debuff]", "）。"],
     *     ["...", "$[freeze]", "..."]
     *   ] }
     * }</pre>
     * {@code description} = 片段行数组（C.9）：每行 = 一串片段串；裸串=字面、{@code @key}=翻译键、
     * {@code $[id]}=关键字引用。{@code title} 以 {@code @} 前缀声明为翻译键。
     * 加载失败（目录缺失/损坏）不致命、注册表保持为空不影响其它 tooltip，但<b>不再静默</b>：
     * 缺文件与解析异常都会打 WARN（空注册表曾因此毫无征兆）。
     */
    public void loadFromResourceManager(ResourceManager manager) {
        Map<Identifier, Resource> resources;
        try {
            resources = manager.listResources("keywords", p -> p.getPath().endsWith(".json"));
        } catch (Exception e) {
            CraftOfExile.LOGGER.warn("[coe-keyword] 枚举 data/coe/keywords 失败，注册表为空", e);
            return;
        }
        if (resources.isEmpty()) {
            // 曾因 datagen 落点与装载路径差一层而静默返回空表，使整个关键字系统无效果且零日志，故补 WARN。
            CraftOfExile.LOGGER.warn("[coe-keyword] 未找到关键字数据 data/coe/keywords/*.json：注册表为空，"
                    + "悬停 / ALT / 左键 pin / Ctrl 窗口均无效果（请先运行 datagen 生成 keywords/*.json）");
            return;
        }
        for (Map.Entry<Identifier, Resource> en : resources.entrySet()) {
            // 只吃本命名空间，避免别的 mod 同名目录污染
            if (!CraftOfExile.MODID.equals(en.getKey().getNamespace())) continue;
            try (InputStream in = en.getValue().open()) {
                JsonElement root = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8));
                if (!root.isJsonObject()) continue;
                parseEntry(root.getAsJsonObject());
            } catch (Exception e) {
                // 数据驱动容错：坏数据不致命，但必须留痕，否则注册表空表无从排查
                CraftOfExile.LOGGER.warn("[coe-keyword] 解析关键字数据 " + en.getKey() + " 失败，跳过该条", e);
            }
        }
    }

    /** 解码单条关键字 JSON 对象并注册。 */
    private void parseEntry(JsonObject o) {
        if (!o.has("id") || !o.has("title")) return;
        String id = o.get("id").getAsString();
        Component title = readComponent(o.get("title"));
        KeywordEntry.Builder b = KeywordEntry.builder(id, title);
        if (o.has("key")) b.key(stripKey(o.get("key")));
        if (o.has("keys") && o.get("keys").isJsonArray()) {
            for (JsonElement k : o.getAsJsonArray("keys")) b.key(stripKey(k));
        }
        if (o.has("aliases") && o.get("aliases").isJsonArray()) {
            for (JsonElement a : o.getAsJsonArray("aliases")) b.alias(readComponent(a));
        }
        if (o.has("description") && o.get("description").isJsonArray()) {
            // C.9：description = 片段行数组；逐行把片段串解析为 KeywordFragment。
            for (JsonElement lineEl : o.getAsJsonArray("description")) {
                if (!lineEl.isJsonArray()) continue;
                List<KeywordFragment> line = new ArrayList<>();
                for (JsonElement f : lineEl.getAsJsonArray()) line.add(KeywordFragment.of(f.getAsString()));
                b.fragments(line);
            }
        }
        register(b.build());
    }

    private static String stripKey(JsonElement e) {
        String s = e.getAsString();
        return s.startsWith("@") ? s.substring(1) : s;
    }

    private static Component readComponent(JsonElement e) {
        if (e == null) return Component.empty();
        String s = e.getAsString();
        if (s.startsWith("@")) return Component.translatable(s.substring(1));
        return Component.literal(s);
    }

    // ============================ 匹配 ============================

    /** 一处命中：行内 [start,end) 字符区间 + 命中的关键字条目。 */
    public static class Match {
        public final int start, end;
        public final KeywordEntry entry;
        Match(int s, int e, KeywordEntry en) { start = s; end = e; entry = en; }
    }

    /**
     * 在一行文本里找出全部非重叠的命中，最长优先、同长按出现序。
     * 返回的区间是<b>字符</b>区间（小写化不改变长度，故与原文坐标一致）。
     */
    public List<Match> find(String line) {
        if (line == null || line.isEmpty()) return Collections.emptyList();
        String low = line.toLowerCase(Locale.ROOT);
        List<Cand> cands = new ArrayList<>();
        for (KeywordEntry e : all) {
            for (String alias : e.aliasStrings()) {
                String a = alias.toLowerCase(Locale.ROOT);
                if (a.isEmpty()) continue;
                int idx = 0;
                while ((idx = low.indexOf(a, idx)) >= 0) {
                    int end = idx + a.length();
                    if (isBoundary(low, idx, end)) cands.add(new Cand(idx, end, a.length(), e));
                    idx = end;
                }
            }
        }
        cands.sort((x, y) -> y.len != x.len ? y.len - x.len : x.start - y.start);
        List<Match> out = new ArrayList<>();
        // 非重叠筛选必须用「占用表」而非单调 lastEnd：候选是按长度降序遍历的（不是按 start 递增），
        // 若用单调 lastEnd，一个靠后的长候选会把它之前所有更靠前的短候选整段挡掉
        // （例："Ignite deals fire damage." 会吞掉 ignite；"冰冻是一种元素异常…" 会吞掉 冰冻）。
        // 占用表保留「最长优先」语义：长候选先占位，与之重叠的短候选落选，不重叠的照常收录。
        boolean[] occupied = new boolean[low.length()];
        for (Cand c : cands) {
            boolean clash = false;
            for (int i = c.start; i < c.end; i++) {
                if (occupied[i]) { clash = true; break; }
            }
            if (clash) continue;
            for (int i = c.start; i < c.end; i++) occupied[i] = true;
            out.add(new Match(c.start, c.end, c.entry));
        }
        out.sort(Comparator.comparingInt(m -> m.start));
        return out;
    }

    /**
     * 词边界：命中串前后不能是「词内字符」，避免把 {@code ail} 命中 {@code frail}。
     *
     * <p>CJK 表意文字虽被 {@link Character#isLetterOrDigit} 判为字母，但中文里<b>每个字独立成词</b>、
     * 词间无空格，若沿用 ASCII 的词边界规则，「造成点燃伤害」中的「点燃」两侧都是汉字 → 永远判不到边界 →
     * 中文 tooltip 几乎命中不到关键字。故 CJK 字符一律视为合法边界；其余字符保持原规则。
     */
    private static boolean isBoundary(String s, int start, int end) {
        boolean beforeOk = start == 0 || !isWordChar(s.charAt(start - 1));
        boolean afterOk  = end >= s.length() || !isWordChar(s.charAt(end));
        return beforeOk && afterOk;
    }

    /** 词内字符（其相邻会破坏词边界）：字母/数字算词内；CJK 表意文字独立成词，不算词内。 */
    private static boolean isWordChar(char c) {
        if (isCjkIdeograph(c)) return false;
        return Character.isLetterOrDigit(c);
    }

    /** CJK 表意文字（基本区 + 扩展 A + 兼容区 + 部首补充）。 */
    private static boolean isCjkIdeograph(char c) {
        return (c >= 0x4E00 && c <= 0x9FFF)     // CJK 统一表意文字
            || (c >= 0x3400 && c <= 0x4DBF)     // 扩展 A
            || (c >= 0xF900 && c <= 0xFAFF)     // 兼容表意文字
            || (c >= 0x2E80 && c <= 0x2EFF);    // CJK 部首补充
    }

    private static class Cand {
        final int start, end, len;
        final KeywordEntry entry;
        Cand(int s, int e, int l, KeywordEntry en) { start = s; end = e; len = l; entry = en; }
    }

    /**
     * 解析描述文本里的 {@code $[id]} 占位符：将其替换为对应关键字的<b>本地化</b>标题
     * （{@code Component.translatable("coe:keyword."+id).getString()}，随游戏语言切换）。
     * 这样描述可安全嵌套引用其它关键字，且渲染层重解析该文本时会把被替换出的标题再识别为关键字高亮/可点。
     * id 匹配<b>大小写敏感</b>；无占位符或占位符无对应关键字时原样返回。
     */
    public String resolveText(String raw) {
        if (raw == null || raw.indexOf("$[") < 0) return raw;
        Matcher m = PLACEHOLDER.matcher(raw);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String id = m.group(1);
            KeywordEntry e = byId.get(id); // 大小写敏感
            if (e != null) {
                String title = Component.translatable("coe:keyword." + id).getString();
                m.appendReplacement(sb, Matcher.quoteReplacement(title));
            } else {
                m.appendReplacement(sb, Matcher.quoteReplacement(m.group(0)));
            }
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private static final Pattern PLACEHOLDER = Pattern.compile("\\$\\[([^\\]]+)\\]");

    /** 把一行 tooltip 解析成带关键字跨度的结构，供渲染层使用。 */
    public ParsedLine parse(Component line) {
        String raw = line.getString();
        String text = resolveText(raw);
        List<Match> matches = find(text);
        List<KeywordSpan> spans = new ArrayList<>();
        for (Match m : matches) spans.add(new KeywordSpan(m.entry, m.start, m.end));
        // 翻译键精确匹配维度：整行若是某个翻译键，则整行作为该关键字的命中
        // 注意：TranslatableContents 实现的是 ComponentContents 而非 Component，必须经 getContents() 取，
        // 直接对 Component 做 instanceof 恒为 false（曾使整条「按翻译键匹配」链路成为死代码）。
        // 去重：仅当 find() 未为该 entry 生成子串 span 时才补整行 span，避免同一关键字自重叠。
        // 若解析文本本身已含该关键字 token（关键字标题行最常见，如 Component.translatable("coe:keyword.ignite")
        // 的解析文本里就含 find() 能匹配的词），find() 的子串 span 已覆盖命中区，整行 span 会与之完全重叠
        // → 渲染层对同区域双重高亮、且点击/悬停区被错误扩到整行；此时跳过。
        // 若翻译键行文本里嵌的是「另一个」关键字 token，find() 命中的是别的 entry，整行 span 仍保留
        // （同一条 tooltip 行同时是匹配的翻译键 + 内含另一关键字，双关键字场景）。
        if (line.getContents() instanceof TranslatableContents tc) {
            KeywordEntry e = findByKey(tc.getKey());
            if (e != null && matches.stream().noneMatch(m -> m.entry == e)) spans.add(new KeywordSpan(e, 0, text.length()));
        }
        // 绘制组件：未被 $[id] 改写时沿用原组件（保留行内配色），否则用解析后的字面量文本
        Component display = text.equals(raw) ? line : Component.literal(text);
        return new ParsedLine(display, text, spans, Draw.width(text));
    }

    /**
     * C.9：把一个关键字的<b>片段数组描述</b>编译成渲染用 {@link ParsedLine} 列表。
     *
     * <p>每行片段按序拼成展示文本（字面→原文；{@code @key}→本地化串；{@code $[id]}→被引关键字的
     * 本地化标题），引用片段同时登记一个<b>结构化</b>命中（携带 {@link KeywordEntry} 引用，恒可点/可钻取，
     * 不再仅靠子串命中）。拼好后仍对整行跑一次 {@link #find}，使字面文本里<b>顺带</b>出现的关键字
     * （既有 CJK 词边界 + Locale.ROOT 折叠质量不回退）也高亮；与结构化引用重叠的偶然命中被剔除。
     */
    public List<ParsedLine> compileDescription(KeywordEntry e) {
        List<ParsedLine> out = new ArrayList<>();
        for (List<KeywordFragment> line : e.descFragments()) out.add(compileLine(line));
        return out;
    }

    private ParsedLine compileLine(List<KeywordFragment> frags) {
        StringBuilder sb = new StringBuilder();
        // 结构化引用区间 [start,end) 及其被引条目
        List<int[]> refRegions = new ArrayList<>();
        List<KeywordEntry> refEntries = new ArrayList<>();
        for (KeywordFragment f : frags) {
            String piece;
            switch (f.type) {
                case TRANSLATABLE -> piece = Component.translatable(f.value).getString();
                case REF -> {
                    KeywordEntry target = byId.get(f.value); // 大小写敏感
                    if (target == null) {
                        // 未解析到被引条目：原样保留 $[id]（与旧 resolveText 一致），不登记命中
                        piece = "$[" + f.value + "]";
                    } else {
                        String title = Component.translatable("coe:keyword." + f.value).getString();
                        refRegions.add(new int[]{ sb.length(), sb.length() + title.length() });
                        refEntries.add(target);
                        piece = title;
                    }
                }
                default -> piece = f.value; // LITERAL
            }
            sb.append(piece);
        }
        String text = sb.toString();

        List<KeywordSpan> spans = new ArrayList<>();
        boolean[] occupied = text.isEmpty() ? new boolean[0] : new boolean[text.length()];
        // 结构化引用优先占位（恒命中）
        for (int i = 0; i < refRegions.size(); i++) {
            int[] r = refRegions.get(i);
            for (int x = r[0]; x < r[1]; x++) occupied[x] = true;
            spans.add(new KeywordSpan(refEntries.get(i), r[0], r[1]));
        }
        // 字面文本里的偶然关键字命中：与结构化引用重叠则剔除（避免双重高亮 / 点击区错误扩大）
        for (Match m : find(text)) {
            boolean clash = false;
            for (int x = m.start; x < m.end; x++) if (occupied[x]) { clash = true; break; }
            if (!clash) spans.add(new KeywordSpan(m.entry, m.start, m.end));
        }
        spans.sort(Comparator.comparingInt(s -> s.charStart));
        // 描述行无原始组件可保色，统一用字面量组件（文本即 text）
        return new ParsedLine(Component.literal(text), text, spans, Draw.width(text));
    }

    /** 整组行里是否存在任意关键字（决定走自定义渲染还是回退原生 tooltip）。 */
    public boolean hasAnyKeyword(List<Component> lines) {
        if (lines == null) return false;
        for (Component c : lines) {
            if (c == null) continue;
            // 必须与 parse() 一致：先 resolveText 展开 $[id]，否则含占位符的行会与渲染结果判定不一致
            String text = resolveText(c.getString());
            if (!find(text).isEmpty()) return true;
            if (c.getContents() instanceof TranslatableContents tc && findByKey(tc.getKey()) != null) return true;
        }
        return false;
    }
}
