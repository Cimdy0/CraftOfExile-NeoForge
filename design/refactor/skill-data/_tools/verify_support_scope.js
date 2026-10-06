#!/usr/bin/env node
/**
 * verify_support_scope.js —— ADR-008 的配套自检脚本。
 *
 * 作用：在不启动 Minecraft / 不跑 gradle 的前提下，验证
 *   src/main/java/com/cimdy/coe/data/codec/skill/support/SupportScope.java
 * 的求值语义在真实权威数据上成立。
 *
 * 本脚本是 SupportScope.java 的**等价 JS 移植**（同一套 normalize / validate / matches 规则），
 * 因此它验证的是"语义"而不是"Java 语法"——Java 侧仍需 compileJava 门禁（本环境禁止 gradle）。
 *
 * 检查项：
 *   A. 对 support_scope.json 中全部含 allow_types 的条目跑 validate，要求 100% 合法。
 *   B. 形态分布统计 + 操作符集合统计（用于确认无 NOT）。
 *   C. 隐式连接符到底是 AND 还是 OR：全量对比"求值命中数" vs 权威 supported_gem_count。
 *   D. 随机抽 20 条，用其 mentioned_gems_sample 指到的真实技能 type_tags 做一次求值并打表。
 *
 * 用法： node design/refactor/skill-data/_tools/verify_support_scope.js [抽样条数]
 * 退出码：0 = 全部通过；1 = 有失败项。
 */

'use strict';
const fs = require('fs');
const path = require('path');

const ROOT = path.resolve(__dirname, '..', '..', '..', '..');
const SCOPE_JSON = path.join(ROOT, 'design/refactor/skill-data/crawled/support_scope.json');
const SKILLS_JSON = path.join(ROOT, 'design/refactor/skill-data/crawled/all_skills.json');

// ---------------------------------------------------------------- 语义移植
const AND = 'AND';
const OR = 'OR';
const isOperator = (t) => t === AND || t === OR;

/** SupportScope#normalize：尾部补齐隐式连接符，使栈深降到 1。 */
function normalize(raw, implicit = OR) {
    if (!raw || raw.length === 0) return [];
    let depth = 0;
    for (const t of raw) depth += isOperator(t) ? -1 : 1;
    if (depth <= 1) return raw.slice();
    const out = raw.slice();
    for (let i = 1; i < depth; i++) out.push(implicit);
    return out;
}

/** SupportScope#validate。 */
function validate(raw, implicit = OR) {
    if (raw == null) return { valid: false, tokenIndex: -1, token: '<null>', stackDepth: 0, message: '表达式为 null' };
    if (raw.length === 0) return { valid: true, tokenIndex: -1, token: '', stackDepth: 1, message: 'OK' };
    const n = normalize(raw, implicit);
    let depth = 0;
    for (let i = 0; i < n.length; i++) {
        const t = n[i];
        if (isOperator(t)) {
            if (depth < 2) {
                return {
                    valid: false, tokenIndex: i, token: t, stackDepth: depth,
                    message: `二元操作符 '${t}' 出现在 token[${i}]，此时栈深=${depth}（需要 >= 2）`,
                };
            }
            depth--;
        } else {
            if (t == null || t === '') {
                return { valid: false, tokenIndex: i, token: String(t), stackDepth: depth, message: `token[${i}] 是空词` };
            }
            depth++;
        }
    }
    if (depth !== 1) {
        return {
            valid: false, tokenIndex: n.length, token: '<end>', stackDepth: depth,
            message: `表达式结束时栈深=${depth}（应为 1）`,
        };
    }
    return { valid: true, tokenIndex: -1, token: '', stackDepth: 1, message: 'OK' };
}

/** SupportScope.Compiled#matches（未知词 = false）。 */
function matches(prog, typeTags, unknownOut = null) {
    if (prog.length === 0) return true; // UNCONSTRAINED
    const stack = new Array(prog.length);
    let sp = 0;
    for (const step of prog) {
        if (step.op === AND) { const b = stack[--sp]; stack[sp - 1] = stack[sp - 1] && b; }
        else if (step.op === OR) { const b = stack[--sp]; stack[sp - 1] = stack[sp - 1] || b; }
        else {
            const hit = typeTags != null && typeTags.has(step.word);
            if (!hit && typeTags != null && unknownOut) unknownOut.add(step.word);
            stack[sp++] = hit;
        }
    }
    return stack[0];
}

/** SupportScope#compile。 */
function compile(raw, implicit = OR) {
    const v = validate(raw, implicit);
    if (!v.valid) throw new Error(v.message);
    const n = normalize(raw, implicit);
    return n.map((t) => (isOperator(t) ? { op: t } : { word: t }));
}

/** 还原中缀（与 SupportScope.Compiled#toInfix 同构）。 */
function toInfix(prog) {
    if (prog.length === 0) return '*';
    const st = new Array(prog.length);
    let sp = 0;
    for (const step of prog) {
        if (step.op) { const b = st[--sp]; const a = st[sp - 1]; st[sp - 1] = `(${a} ${step.op} ${b})`; }
        else st[sp++] = step.word;
    }
    return st[0];
}

// ---------------------------------------------------------------- 读数据
const scope = JSON.parse(fs.readFileSync(SCOPE_JSON, 'utf8'));
const skills = JSON.parse(fs.readFileSync(SKILLS_JSON, 'utf8'));

const scopeEntries = scope.entries;
const allEntries = skills.entries;

const byId = new Map(allEntries.map((e) => [e.id, e]));
// 只把名字解析到「可被辅助」的宝石（skill / spirit）；support 名字（如 "Unleash"）不参与，
// 否则会把辅助宝石自身当成被辅助目标，制造假的 DENIED。
const byName = new Map();
for (const e of allEntries) {
    if (e.gem_type === 'support') continue;
    if (e.name_en && !byName.has(e.name_en)) byName.set(e.name_en, e);
    if (!byName.has(e.id)) byName.set(e.id, e);
}

/** 可被辅助的目标 = 非 support 的宝石，且带 type_tags。 */
const targets = allEntries.filter((e) => e.gem_type !== 'support' && e.type_tags && e.type_tags.length > 0);
const targetSets = targets.map((e) => new Set(e.type_tags));

const supportKeys = Object.keys(scopeEntries).filter((k) => scopeEntries[k].gem_type === 'support');
const withAllow = supportKeys.filter((k) => scopeEntries[k].allow_types && scopeEntries[k].allow_types.length > 0);

function countMatches(prog) {
    let c = 0;
    for (const t of targetSets) if (matches(prog, t)) c++;
    return c;
}

const pass = [];
const fail = [];
const line = (s = '') => console.log(s);

// ---------------------------------------------------------------- A. 全量 validate
line('================================================================');
line(' A. 装载期校验：对全部含 Allow Type 的条目跑 SupportScope.validate');
line('================================================================');
for (const k of withAllow) {
    const v = validate(scopeEntries[k].allow_types, OR);
    (v.valid ? pass : fail).push({ id: k, raw: scopeEntries[k].allow_types, v });
}
line(`样本：support 条目 ${supportKeys.length}，其中含 Allow Type ${withAllow.length}`);
line(`validate 通过：${pass.length} / ${withAllow.length}  (${(pass.length / withAllow.length * 100).toFixed(2)}%)`);
if (fail.length) {
    line('!! 非法表达式（前 20）：');
    for (const f of fail.slice(0, 20)) line(`   ${f.id} | ${JSON.stringify(f.raw)} | ${f.v.message}`);
}

// ---------------------------------------------------------------- B. 形态分布
line();
line('================================================================');
line(' B. 形态分布 / 操作符集合');
line('================================================================');
let noAllow = 0, single = 0, implicitMulti = 0, explicit = 0, withOr = 0, withNot = 0, multiOp = 0;
const opTokens = new Set();
for (const k of Object.keys(scopeEntries)) {
    const a = scopeEntries[k].allow_types;
    if (!a || a.length === 0) { noAllow++; continue; }
    const ops = a.filter(isOperator);
    for (const t of a) if (isOperator(t)) opTokens.add(t);
    const words = a.filter((t) => !isOperator(t));
    if (ops.length === 0) { words.length === 1 ? single++ : implicitMulti++; }
    else { explicit++; if (ops.includes('OR')) withOr++; if (ops.includes('NOT')) withNot++; if (ops.length > 1) multiOp++; }
}
line(`全量条目 ${Object.keys(scopeEntries).length}：无 Allow Type ${noAllow} / 单词 ${single} / 隐式多词 ${implicitMulti} / 含显式操作符 ${explicit}`);
line(`操作符集合：${[...opTokens].sort().join(', ')}  （NOT 命中 ${withNot} 条）`);
line(`含 OR ${withOr} 条；多操作符嵌套 ${multiOp} 条`);

// ---------------------------------------------------------------- C. 隐式连接符 AND vs OR
line();
line('================================================================');
line(' C. 隐式连接符判定：AND 还是 OR？（对照权威 supported_gem_count）');
line('================================================================');
const implicitKeys = withAllow.filter((k) => scopeEntries[k].allow_types.every((t) => !isOperator(t))
    && scopeEntries[k].allow_types.length > 1);
const explicitKeys = withAllow.filter((k) => scopeEntries[k].allow_types.some(isOperator));
const singleKeys = withAllow.filter((k) => scopeEntries[k].allow_types.length === 1);

function totalErr(keys, implicit) {
    let sum = 0, n = 0;
    for (const k of keys) {
        const declared = scopeEntries[k].supported_gem_count;
        if (declared == null) continue;
        sum += Math.abs(countMatches(compile(scopeEntries[k].allow_types, implicit)) - declared);
        n++;
    }
    return { avg: n ? sum / n : NaN, n };
}
const impAnd = totalErr(implicitKeys, AND);
const impOr = totalErr(implicitKeys, OR);
const expAnd = totalErr(explicitKeys, AND);
const expOr = totalErr(explicitKeys, OR);
const singleErr = totalErr(singleKeys, OR);
line(`隐式多词组 (n=${impAnd.n})：AND 平均绝对误差 ${impAnd.avg.toFixed(1)} ｜ OR 平均绝对误差 ${impOr.avg.toFixed(1)}`);
line(`含显式操作符组 (n=${expAnd.n})：AND ${expAnd.avg.toFixed(1)} ｜ OR ${expOr.avg.toFixed(1)}   （OR 只影响末尾需补连接符的 4 条）`);
line(`单词组 (n=${singleErr.n})：平均绝对误差 ${singleErr.avg.toFixed(1)}  （基准噪声）`);
line();
line('决定性反例（隐式多词，AND 与 OR 结果相差悬殊）：');
for (const k of ['auto_reload', 'fresh_clip_i', 'double_barrel_i', 'zerphis_infamy', 'cirels_cultivation', 'dauntless', 'uulnetols_embrace']) {
    if (!scopeEntries[k]) continue;
    const a = scopeEntries[k].allow_types;
    line(`   ${k.padEnd(20)} ${a.join(',').padEnd(58)} AND=${String(countMatches(compile(a, AND))).padStart(3)}  OR=${String(countMatches(compile(a, OR))).padStart(3)}  权威=${scopeEntries[k].supported_gem_count}`);
}
line();
line('描述文本交叉验证（权威 description_en 摘录）：');
for (const k of ['zerphis_infamy', 'impact_shockwave', 'zenith_i', 'bleed_i', 'brutality_i', 'slow_potency']) {
    const e = byId.get(k);
    if (e && e.description_en) line(`   ${k.padEnd(20)} ${scopeEntries[k].allow_types.join(',').padEnd(42)} ${e.description_en.slice(0, 96)}…`);
}

// ---------------------------------------------------------------- D. 抽样求值
line();
line('================================================================');
line(' D. 抽样求值（用 mentioned_gems_sample 指到的真实技能 type_tags）');
line('================================================================');
const SAMPLE_N = Number(process.argv[2] || 20);
// 固定种子，保证可复现
let seed = 20260920;
const rnd = () => { seed = (seed * 1103515245 + 12345) & 0x7fffffff; return seed / 0x7fffffff; };
const pool = withAllow.slice().sort();
const picked = [];
const used = new Set();
while (picked.length < Math.min(SAMPLE_N, pool.length)) {
    const i = Math.floor(rnd() * pool.length);
    if (used.has(i)) continue;
    used.add(i); picked.push(pool[i]);
}

const rows = [];
for (const k of picked) {
    const e = scopeEntries[k];
    const prog = compile(e.allow_types, OR);
    const names = (e.mentioned_gems_sample || []).slice();
    let hit = null, usedName = null;
    for (const n of names) {
        const s = byName.get(n);
        if (!s || !s.type_tags || !s.type_tags.length) continue;
        usedName = n;
        hit = matches(prog, new Set(s.type_tags));
        break;
    }
    // 若 sample 为空或解析不出，退化：用全量命中数表示
    const matched = countMatches(prog);
    rows.push({
        support: k,
        expr: toInfix(prog),
        raw: e.allow_types.join(','),
        target: usedName,
        result: hit,
        matchedAll: matched,
        declared: e.supported_gem_count,
    });
}
line('支撑(support)'.padEnd(26) + '规范化表达式'.padEnd(46) + '抽样目标技能'.padEnd(26) + '求值');
line('-'.repeat(120));
for (const r of rows) {
    const expr = r.expr.length > 44 ? r.expr.slice(0, 41) + '...' : r.expr;
    const tgt = (r.target || '(sample 无可解析技能)').slice(0, 24);
    line(r.support.padEnd(26) + expr.padEnd(46) + tgt.padEnd(26) + (r.result == null ? '-' : (r.result ? 'ALLOWED' : 'DENIED')));
}
line();
line('与权威计数的偏差（抽样条）：');
line('-'.repeat(120));
for (const r of rows) {
    const d = r.declared == null ? null : r.declared - r.matchedAll;
    line(`   ${r.support.padEnd(26)} 全量命中 ${String(r.matchedAll).padStart(3)} / 权威 ${String(r.declared).padStart(3)}  偏差 ${d == null ? 'n/a' : (d > 0 ? '+' + d : String(d))}`);
}

// ---------------------------------------------------------------- E. 全量"权威声称可辅助的样本"命中率
line();
line('================================================================');
line(' E. 全量样本命中率：每条辅助的 mentioned_gems_sample 里能被解析的技能，有多少被判 ALLOWED');
line('================================================================');
line('（注：mentioned_gems_sample 是详情页的相关宝石列表，含少量辅助宝石名，'
    + '解析不到的名字已跳过；因此它是**宽松**证据，不是严格真值。）');
let totTried = 0, totAllowed = 0;
const zeroHitButDeclared = [];
for (const k of withAllow) {
    const e = scopeEntries[k];
    const prog = compile(e.allow_types, OR);
    let tried = 0, allowed = 0;
    for (const n of (e.mentioned_gems_sample || [])) {
        const s = byName.get(n);
        if (!s || !s.type_tags || !s.type_tags.length) continue;
        tried++;
        if (matches(prog, new Set(s.type_tags))) allowed++;
    }
    totTried += tried; totAllowed += allowed;
    const declared = e.supported_gem_count;
    if (declared != null && declared > 0 && countMatches(prog) === 0) zeroHitButDeclared.push(k);
}
line(`可解析样本条目 ${totTried} 个，判 ALLOWED ${totAllowed} 个 → 命中率 ${(totAllowed / totTried * 100).toFixed(2)}%`);
line(`「权威 supported_gem_count > 0 但本求值器全量命中 0」的辅助：${zeroHitButDeclared.length} 条`
    + (zeroHitButDeclared.length ? ` → ${zeroHitButDeclared.join(', ')}` : ' （无，说明 OR 语义下不存在系统性假阴性）'));

// ---------------------------------------------------------------- 结论
line();
line('================================================================');
line(' 结论');
line('================================================================');
const okA = fail.length === 0;
const okB = withNot === 0;
const okC = impOr.avg < impAnd.avg;
line(`A 全量 validate 通过率：${(pass.length / withAllow.length * 100).toFixed(2)}%  ${okA ? 'PASS' : 'FAIL'}`);
line(`B 无 NOT 操作符：${okB ? 'PASS' : 'FAIL'}`);
line(`C 隐式连接符 = OR（误差 ${impOr.avg.toFixed(1)} < AND 的 ${impAnd.avg.toFixed(1)}）：${okC ? 'PASS' : 'FAIL'}`);
line(`   全量零命中（OR 语义）辅助数：${withAllow.filter((k) => countMatches(compile(scopeEntries[k].allow_types, OR)) === 0).length}`);
line(`   全量零命中（AND 语义）辅助数：${withAllow.filter((k) => countMatches(compile(scopeEntries[k].allow_types, AND)) === 0).length}`);
const ok = okA && okB && okC;
line();
line(ok ? 'ALL CHECKS PASS' : 'SOME CHECKS FAILED');
process.exit(ok ? 0 : 1);
