package com.cimdy.coe.data.codec.support.math;

import com.cimdy.coe.data.codec.support.meta.DataSourceContext;
import com.cimdy.coe.data.codec.support.math.context.node.ValueExprTypes;
import com.cimdy.coe.data.codec.support.math.context.nodes.scope.BindExpr;
import com.cimdy.coe.data.codec.support.math.context.ValueTarget;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.entity.core.DistanceValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.meta.misc.VarRef;
import com.cimdy.coe.data.codec.support.value.Value;
import com.mojang.datafixers.util.Either;
import net.minecraft.resources.Identifier;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 中缀表达式解析器 / 序列化器：字符串 ↔ {@link MathExpression} AST 双向转换。
 * <p>
 * 支持的语法（覆盖全部 AST 节点类型）：
 * <pre>
 *   数字:         1, 2.5, 3e-4
 *   变量:         health, skill.level, press.time, source.health, candidate.armour, slot(hit).health
 *   带 id 变量:   attribute:coe:attack_speed, effect.level:minecraft:strength
 *   一元:         -a, +a
 *   算术:         a + b, a - b, a * b, a / b, a % b, a ^ b
 *   比较:         a == b, a != b, a &lt; b, a &lt;= b, a > b, a >= b   （真=1，假=0）
 *   逻辑:         a &amp;&amp; b, a || b, !a                              （布尔组合，非零为真）
 *   三元:         a > b ? x : y                                        （Comparison）
 *   函数:         min(a, b), max(a, b), sqrt(x), cbrt(x)
 *   模板引用:     coe:per_level_add_value(base = 10, per = 1, add = 2)   （模板 id 直接作函数名）
 *   模板位置参数: coe:level_table(6.5, 9, 13, ..., def = 0)              （第 n 个无名值 → 变量 "n"，与模板中 $1..$n 一一对应）
 *   查表:         table(skill.level, $1..$40, def)                   （位置/范围：无冒号键=序号；$a..$b 展开连续段，默认值可省略）
 *   括号:         (a + b) * c
 * </pre>
 */
public final class MathParser {

    private MathParser() {}

    // ==================== Token ====================

    private enum TokenType { NUMBER, IDENT, OP, LPAREN, RPAREN, COMMA, EOF }

    private record Token(TokenType type, String text, int pos) {}

    private static final Pattern TOKEN_PATTERN = Pattern.compile(
            "\\s*(?:(\\d+(?:\\.\\d+)?(?:[eE][+-]?\\d+)?)|" +                    // NUMBER
            "([$a-zA-Z_][a-zA-Z0-9_]*(?:[.][a-zA-Z0-9_]+)*(?::[a-zA-Z0-9_./]+)*)|" + // IDENT（$前缀=强制变量，可含 namespace:path 段）
            "(==|!=|<=|>=|&&|\\|\\||\\.\\.|[+\\-*/%^<>(),?:!=]))"                    // OP / 括号 / 逗号 / 范围.. / 逻辑.. / 单= （== 备选在前）
    );

    private static List<Token> tokenize(String src) {
        List<Token> tokens = new ArrayList<>();
        Matcher m = TOKEN_PATTERN.matcher(src);
        int lastEnd = 0;
        while (m.find() && m.start() == lastEnd) {
            lastEnd = m.end();
            if (m.group(1) != null) {
                tokens.add(new Token(TokenType.NUMBER, m.group(1), m.start(1)));
            } else if (m.group(2) != null) {
                tokens.add(new Token(TokenType.IDENT, m.group(2), m.start(2)));
            } else {
                String op = m.group(3);
                TokenType tt = switch (op) {
                    case "(" -> TokenType.LPAREN;
                    case ")" -> TokenType.RPAREN;
                    case "," -> TokenType.COMMA;
                    default  -> TokenType.OP;
                };
                tokens.add(new Token(tt, op, m.start(3)));
            }
            if (lastEnd >= src.length()) break;
        }
        // 检查是否有无法识别的残留字符（跳过尾部空白）
        if (!src.substring(lastEnd).trim().isEmpty()) {
            throw new ParseException("Unexpected character '" + src.charAt(lastEnd) + "'", lastEnd);
        }
        tokens.add(new Token(TokenType.EOF, "", src.length()));
        return tokens;
    }

    // ==================== 运算符优先级 ====================

    // 0=三元 1=等值比较 2=大小比较 3=加减 4=乘除模 5=幂 6=一元 100=原子
    // -1=逻辑与 &&（低于三元，高于 ||） -2=逻辑或 ||（最低）
    private static final int PREC_TERNARY = 0;
    private static final int PREC_AND = -1;
    private static final int PREC_OR = -2;
    private static final int PREC_UNARY = 6;
    // 一元 - / + / ! 优先级（最高）

    private enum Assoc { LEFT, RIGHT }

    private record OpInfo(int prec, Assoc assoc) {}

    /** 返回中缀运算符信息；未知（如 ? : = ）返回 null 由调用方停止。 */
    private static OpInfo infixOp(String op) {
        return switch (op) {
            case "==", "!="            -> new OpInfo(1, Assoc.LEFT);
            case "<", "<=", ">", ">="  -> new OpInfo(2, Assoc.LEFT);
            case "+", "-"              -> new OpInfo(3, Assoc.LEFT);
            case "*", "/", "%"         -> new OpInfo(4, Assoc.LEFT);
            case "^"                   -> new OpInfo(5, Assoc.RIGHT);
            default -> null;
        };
    }

    // ==================== Parser ====================

    private static final class Parser {
        private final List<Token> tokens;
        private int pos;

        Parser(List<Token> tokens) { this.tokens = tokens; }

        private Token peek() { return tokens.get(pos); }
        private Token peekAhead(int n) {
            int i = pos + n;
            return i < tokens.size() ? tokens.get(i) : tokens.get(tokens.size() - 1);
        }
        private Token consume() { return tokens.get(pos++); }

        private boolean peekOp(String text) {
            Token t = peek();
            return t.type == TokenType.OP && t.text.equals(text);
        }

        private void expectOp(String text) {
            Token t = consume();
            if (t.type != TokenType.OP || !t.text.equals(text))
                throw new ParseException("Expected '" + text + "' but got '" + t.text + "'", t.pos);
        }

        private void expect(TokenType type, String what) {
            Token t = consume();
            if (t.type != type)
                throw new ParseException("Expected " + what + " but got '" + t.text + "'", t.pos);
        }

        // ---- 入口：三元表达式（最低优先级，右结合）----

        MathExpression parseTernary() {
            MathExpression cond = parseBinary(1);
            if (!peekOp("?")) return cond;
            consume(); // '?'
            MathExpression value = parseTernary();
            expectOp(":");
            MathExpression def = parseTernary();
            return buildTernary(cond, value, def);
        }

        private static MathExpression buildTernary(MathExpression cond, MathExpression value, MathExpression def) {
            if (cond instanceof ExpressionCodec.BinaryOperation.Comparison c && isBooleanComparison(c)) {
                // "a > b ? x : y" → Comparison(a, b, >, x, y)
                return new ExpressionCodec.BinaryOperation.Comparison(
                        c.left(), c.right(), c.comparison(), value, def);
            }
            // "expr ? x : y" → expr != 0 ? x : y
            return new ExpressionCodec.BinaryOperation.Comparison(
                    cond, new ExpressionCodec.Constant(0.0),
                    ComparisonOperator.NOT_EQUAL, value, def);
        }

        // ---- 逻辑或（最低优先级，左结合）：a || b || c ----
        MathExpression parseOr() {
            MathExpression left = parseAnd();
            while (peekOp("||")) {
                consume(); // '||'
                left = new ExpressionCodec.Logical(left, parseAnd(), false);
            }
            return left;
        }

        // ---- 逻辑与（低于三元，高于 ||，左结合）：a && b && c ----
        MathExpression parseAnd() {
            MathExpression left = parseTernary();
            while (peekOp("&&")) {
                consume(); // '&&'
                left = new ExpressionCodec.Logical(left, parseTernary(), true);
            }
            return left;
        }

        // ---- Pratt 二元 ----

        private MathExpression parseBinary(int minPrec) {
            MathExpression left = parsePrefix(consume());
            while (peek().type == TokenType.OP) {
                OpInfo info = infixOp(peek().text);
                if (info == null || info.prec < minPrec) break;
                String op = consume().text;
                int nextMin = info.assoc == Assoc.LEFT ? info.prec + 1 : info.prec;
                MathExpression right = parseBinary(nextMin);
                left = buildBinary(op, left, right);
            }
            return left;
        }

        private static MathExpression buildBinary(String op, MathExpression left, MathExpression right) {
            return switch (op) {
                case "+" -> new ExpressionCodec.BinaryOperation.Add(left, right);
                case "-" -> new ExpressionCodec.BinaryOperation.Subtract(left, right);
                case "*" -> new ExpressionCodec.BinaryOperation.Multiply(left, right);
                case "/" -> new ExpressionCodec.BinaryOperation.Divide(left, right);
                case "%" -> new ExpressionCodec.BinaryOperation.Modulo(left, right);
                case "^" -> new ExpressionCodec.BinaryOperation.Power(left, right);
                case "==", "!=", "<", "<=", ">", ">=" ->
                        new ExpressionCodec.BinaryOperation.Comparison(
                                left, right, ComparisonOperator.fromSymbol(op),
                                new ExpressionCodec.Constant(1.0),
                                new ExpressionCodec.Constant(0.0));
                default -> throw new ParseException("Unknown binary operator: " + op, -1);
            };
        }

        private MathExpression parsePrefix(Token tok) {
            return switch (tok.type) {
                case NUMBER -> new ExpressionCodec.Constant(parseNumber(tok));
                case IDENT -> {
                    if (peek().type == TokenType.LPAREN) {
                        consume(); // '('
                        yield parseFunction(tok);
                    }
                    yield buildVariable(tok.text);
                }
                case LPAREN -> {
                    // 括号内为完整逻辑层（支持 && / || / 三元），与顶层 parseOr 一致；
                    // 否则 "(a && b)" 类序列化输出（writeLogical 对逻辑运算无条件加括号）无法回读。
                    MathExpression inner = parseOr();
                    expect(TokenType.RPAREN, "')'");
                    yield inner;
                }
                case OP -> {
                    if (tok.text.equals("-"))
                        yield new ExpressionCodec.UnaryOperation.Negate(parseBinary(PREC_UNARY));
                    if (tok.text.equals("+"))
                        yield parseBinary(PREC_UNARY);
                    if (tok.text.equals("!"))
                        yield new ExpressionCodec.Not(parseBinary(PREC_UNARY));
                    throw new ParseException("Unexpected prefix operator: " + tok.text, tok.pos);
                }
                default -> throw new ParseException("Unexpected token: '" + tok.text + "'", tok.pos);
            };
        }

        private static double parseNumber(Token tok) {
            try {
                return Double.parseDouble(tok.text);
            } catch (NumberFormatException e) {
                throw new ParseException("Invalid number: " + tok.text, tok.pos);
            }
        }

        /**
         * IDENT → 变量。
         * <ul>
         *   <li>{@code $} 前缀 = 强制变量字面量（用于数字名变量，如 {@code $1} → Variable("1")）</li>
         *   <li>含冒号时拆为「类型:资源id」，如 attribute:coe:x → Variable("attribute", coe:x)</li>
         * </ul>
         */
        private static MathExpression buildVariable(String ident) {
            if (ident.startsWith("$")) {
                return ExpressionCodec.Variable.local(ident.substring(1));
            }
            // M5 ④：实体域绑定前缀 → BindExpr 包装，与 ValueExprTypes.toInfix 输出对称，保证中缀往返等价。
            // candidate. = filter 候选作用域；source. = 交互对方(sourceEntity())；slot(name). = 预注册实体槽位。
            if (ident.startsWith("candidate.")) {
                String inner = ident.substring("candidate.".length());
                if (inner.isEmpty() || inner.startsWith("candidate."))
                    throw new ParseException("candidate. 后缺少有效属性: " + ident, -1);
                MathExpression innerExpr = buildVariable(inner);
                if (!(innerExpr instanceof ExpressionCodec.Variable v))
                    throw new ParseException("无法解析 candidate. 内部变量: " + ident, -1);
                return new ExpressionCodec.Variable(new BindExpr(ValueTarget.CANDIDATE, v.node()), 0.0);
            }
            if (ident.startsWith("source.")) {
                String inner = ident.substring("source.".length());
                if (inner.isEmpty() || inner.startsWith("source."))
                    throw new ParseException("source. 后缺少有效属性: " + ident, -1);
                MathExpression innerExpr = buildVariable(inner);
                if (!(innerExpr instanceof ExpressionCodec.Variable v))
                    throw new ParseException("无法解析 source. 内部变量: " + ident, -1);
                return new ExpressionCodec.Variable(new BindExpr(ValueTarget.SOURCE, v.node()), 0.0);
            }
            if (ident.startsWith("slot(")) {
                int close = ident.indexOf(')');
                if (close < 0) throw new ParseException("slot( 未闭合: " + ident, -1);
                String slotName = ident.substring("slot(".length(), close);
                String rest = ident.substring(close + 1);
                if (!rest.startsWith(".")) throw new ParseException("slot() 后缺少 '.' : " + ident, -1);
                String inner = rest.substring(1);
                MathExpression innerExpr = buildVariable(inner);
                if (!(innerExpr instanceof ExpressionCodec.Variable v))
                    throw new ParseException("无法解析 slot() 内部变量: " + ident, -1);
                return new ExpressionCodec.Variable(new BindExpr(ValueTarget.SLOT, v.node(), Optional.of(slotName)), 0.0);
            }
            int colon = ident.indexOf(':');
            if (colon > 0) {
                String type = ident.substring(0, colon);
                String idStr = ident.substring(colon + 1);
                Identifier id = Identifier.tryParse(idStr);
                if (id == null)
                    throw new ParseException("Invalid identifier in variable: " + ident, -1);
                return ExpressionCodec.Variable.from(type, id, 0.0);
            }
            return ExpressionCodec.Variable.from(ident);
        }

        // ---- 函数调用（'(' 已消耗）----

        private MathExpression parseFunction(Token name) {
            return switch (name.text) {
                case "min" -> {
                    MathExpression a = parseTernary();
                    expect(TokenType.COMMA, "','");
                    MathExpression b = parseTernary();
                    expect(TokenType.RPAREN, "')'");
                    yield new ExpressionCodec.BinaryOperation.Min(a, b);
                }
                case "max" -> {
                    MathExpression a = parseTernary();
                    expect(TokenType.COMMA, "','");
                    MathExpression b = parseTernary();
                    expect(TokenType.RPAREN, "')'");
                    yield new ExpressionCodec.BinaryOperation.Max(a, b);
                }
                case "sqrt" -> {
                    MathExpression a = parseTernary();
                    expect(TokenType.RPAREN, "')'");
                    yield new ExpressionCodec.UnaryOperation.Sqrt(a);
                }
                case "cbrt" -> {
                    MathExpression a = parseTernary();
                    expect(TokenType.RPAREN, "')'");
                    yield new ExpressionCodec.UnaryOperation.Cbrt(a);
                }
                case "table" -> parseTable();
                // M5 ④：dist() → 参照实体到候选的距离（filter / sort 表达式）
                case "dist" -> {
                    expect(TokenType.RPAREN, "')'");
                    // 合并后 dist() 表达「参照实体到被判断对象」的相对距离（DistanceValue.Mode.REFERRER_TO_CANDIDATE）
                    yield new ExpressionCodec.Variable(new DistanceValue(DistanceValue.Mode.REFERRER_TO_CANDIDATE), 0.0);
                }
                // 非内置函数名 → 模板引用：coe:template_id(name = expr, ...)
                default -> parseTemplateCall(name);
            };
        }

        /**
         * 模板引用 coe:template_id(...)，'(' 已消耗。参数两种形式可混用：
         * <ul>
         *   <li>具名：{@code name = expr}</li>
         *   <li>位置：{@code expr}，第 n 个位置参数绑定到变量名 "n"（对应模板中的 $1..$n）</li>
         * </ul>
         */
        private MathExpression parseTemplateCall(Token idTok) {
            Identifier id = Identifier.tryParse(idTok.text);
            if (id == null)
                throw new ParseException("Invalid template id: " + idTok.text, idTok.pos);

            Map<String, ExpressionCodec.Value> values = new LinkedHashMap<>();
            int positional = 0;
            if (peek().type != TokenType.RPAREN) {
                while (true) {
                    String name;
                    // 前瞻 "IDENT|NUMBER =" → 具名参数；否则位置参数
                    if ((peek().type == TokenType.IDENT || peek().type == TokenType.NUMBER)
                            && peekAhead(1).type == TokenType.OP && peekAhead(1).text.equals("=")) {
                        name = consume().text;
                        consume(); // '='
                    } else {
                        name = String.valueOf(++positional);
                    }
                    MathExpression expr = parseTernary();
                    // 纯常量收缩为 double（Value 的 Either.left），其余保留为表达式
                    if (expr instanceof ExpressionCodec.Constant(double value)) {
                        values.put(name, new ExpressionCodec.Value(Either.left(value)));
                    } else {
                        values.put(name, new ExpressionCodec.Value(Either.right(expr)));
                    }
                    if (peek().type != TokenType.COMMA) break;
                    consume(); // ','
                }
            }
            expect(TokenType.RPAREN, "')'");
            // stat 模板不再是 MathExpression；其引用由 StatEntry / StatRead 在条目层处理，
            // 这里统一按 math 表达式模板引用解析。
            return new ExpressionCodec.Read(id, values);
        }

        /** table(variable, key: value, $a..$b, ..., [def])；缺省键→位置序号，范围→连续段 */
        private MathExpression parseTable() {
            MathExpression variable = parseTernary();
            List<ExpressionCodec.LevelTable.TableEntry> entries = new ArrayList<>();
            MathExpression def = new ExpressionCodec.Constant(0.0);
            while (peek().type == TokenType.COMMA) {
                consume(); // ','
                MathExpression first = parseTernary();
                if (peekOp(":")) {
                    consume(); // ':'
                    MathExpression value = parseTernary();
                    entries.add(new ExpressionCodec.LevelTable.TableEntry(first, value));
                } else if (peekOp("..")) {
                    consume(); // '..'
                    int a = rangeVarIndex(first);
                    int b = rangeEndIndex(consume());
                    if (b < a)
                        throw new ParseException("Range end must be >= start: $" + a + "..$" + b, -1);
                    for (int i = a; i <= b; i++) {
                        entries.add(new ExpressionCodec.LevelTable.TableEntry(
                                new ExpressionCodec.Constant(i),
                                ExpressionCodec.Variable.local(String.valueOf(i))));
                    }
                } else {
                    // 无冒号、无范围 → 默认值，必须是最后一个参数
                    def = first;
                    break;
                }
            }
            expect(TokenType.RPAREN, "')'");
            return new ExpressionCodec.LevelTable(variable, entries, def);
        }

    private static int rangeVarIndex(MathExpression e) {
        if (!(e instanceof ExpressionCodec.Variable v) || !(v.node() instanceof VarRef vr) || !isNumeric(vr.name()))
            throw new ParseException("Range start must be a numeric variable like $1", -1);
        return Integer.parseInt(vr.name());
    }

        private static int rangeEndIndex(Token t) {
            if (t.type != TokenType.IDENT || !t.text.startsWith("$") || !isNumeric(t.text.substring(1)))
                throw new ParseException("Range end must be a numeric variable like $40, got '" + t.text + "'", t.pos);
            return Integer.parseInt(t.text.substring(1));
        }
    }

    // ==================== 公开 API ====================

    /**
     * 解析中缀表达式字符串为 AST。
     *
     * <p>失败时抛出的 {@link ParseException} 会自动附带表达式原文，以及
     * {@link com.cimdy.coe.data.codec.support.meta.DataSourceContext} 中登记的数据来源 id（若加载器已登记）。
     */
    public static MathExpression parse(String source) {
        return parse(source, DataSourceContext.current());
    }

    /**
     * 解析中缀表达式字符串为 AST，并显式指定数据来源 id 用于错误定位。
     *
     * @param sourceId 数据来源标识（如 {@code coe:stat_entry/xxx.json}）；{@code null} 表示未知
     */
    public static MathExpression parse(String source, String sourceId) {
        try {
            // 全角括号归一（与 GateExpr 策略一致）：数据若含全角（）在此收敛为半角，避免 token 白名单拒绝
            if (source.indexOf('（') >= 0 || source.indexOf('）') >= 0) {
                source = source.replace('（', '(').replace('）', ')');
            }
            Parser parser = new Parser(tokenize(source));
            MathExpression expr = parser.parseOr();
            Token tail = parser.peek();
            if (tail.type != TokenType.EOF)
                throw new ParseException("Unexpected trailing token: '" + tail.text + "'", tail.pos);
            return expr;
        } catch (ParseException e) {
            // 统一在出口补齐上下文：解析器内部各 throw 点无需感知来源。
            throw e.withContext(source, sourceId);
        }
    }

    /** 序列化 AST 为中缀字符串（覆盖全部节点类型，总是成功）。 */
    public static String toInfixString(MathExpression expr) {
        StringBuilder sb = new StringBuilder();
        write(expr, sb, 0);
        return sb.toString();
    }

    // ==================== 序列化 ====================

    private static void write(MathExpression expr, StringBuilder sb, int ctxPrec) {
        switch (expr) {
            case ExpressionCodec.Constant c -> sb.append(formatDouble(c.value()));
            case ExpressionCodec.Variable v -> writeVariable(v, sb);
            case ExpressionCodec.UnaryOperation.Negate n -> {
                boolean paren = ctxPrec > PREC_UNARY;
                if (paren) sb.append('(');
                sb.append('-');
                write(n.expression(), sb, PREC_UNARY);
                if (paren) sb.append(')');
            }
            case ExpressionCodec.UnaryOperation.Sqrt s -> writeFunc("sqrt", sb, s.expression());
            case ExpressionCodec.UnaryOperation.Cbrt c -> writeFunc("cbrt", sb, c.expression());
            case ExpressionCodec.BinaryOperation.Min m -> writeFunc("min", sb, m.left(), m.right());
            case ExpressionCodec.BinaryOperation.Max m -> writeFunc("max", sb, m.left(), m.right());
            case ExpressionCodec.BinaryOperation.Comparison cmp -> writeComparison(cmp, sb, ctxPrec);
            case ExpressionCodec.BinaryOperation bin -> writeBinary(bin, sb, ctxPrec);
            case ExpressionCodec.Logical logic -> writeLogical(logic, sb, ctxPrec);
            case ExpressionCodec.Not not -> writeNot(not, sb, ctxPrec);
            case ExpressionCodec.ConditionOperation cond -> writeConditionOperation(cond, sb, ctxPrec);
            case ExpressionCodec.LevelTable table -> writeTable(table, sb);
            case ExpressionCodec.Read read -> writeRead(read, sb);
            default -> throw new IllegalArgumentException(
                    "Cannot serialize expression type: " + expr.getClass().getName());
        }
    }

    private static void writeVariable(ExpressionCodec.Variable v, StringBuilder sb) {
        sb.append(ValueExprTypes.toInfix(v.node()));
    }

    private static void writeFunc(String name, StringBuilder sb, MathExpression... args) {
        sb.append(name).append('(');
        for (int i = 0; i < args.length; i++) {
            if (i > 0) sb.append(", ");
            write(args[i], sb, 0);
        }
        sb.append(')');
    }

    private static void writeBinary(ExpressionCodec.BinaryOperation bin, StringBuilder sb, int ctxPrec) {
        String op = binOpSymbol(bin);
        OpInfo info = infixOp(op);
        boolean paren = ctxPrec > info.prec;
        if (paren) sb.append('(');
        int leftCtx = info.assoc == Assoc.LEFT ? info.prec : info.prec + 1;
        int rightCtx = info.assoc == Assoc.LEFT ? info.prec + 1 : info.prec;
        write(bin.left(), sb, leftCtx);
        sb.append(' ').append(op).append(' ');
        write(bin.right(), sb, rightCtx);
        if (paren) sb.append(')');
    }

    private static void writeComparison(ExpressionCodec.BinaryOperation.Comparison cmp,
                                        StringBuilder sb, int ctxPrec) {
        String op = cmp.comparison().getSerializedName();
        OpInfo info = infixOp(op);
        if (isBooleanComparison(cmp)) {
            // 简单比较：value=1, def=0 → "a > b"
            boolean paren = ctxPrec > info.prec;
            if (paren) sb.append('(');
            write(cmp.left(), sb, info.prec);
            sb.append(' ').append(op).append(' ');
            write(cmp.right(), sb, info.prec + 1);
            if (paren) sb.append(')');
            return;
        }
        // 完整三元："a > b ? x : y"
        boolean paren = ctxPrec > PREC_TERNARY;
        if (paren) sb.append('(');
        write(cmp.left(), sb, info.prec);
        sb.append(' ').append(op).append(' ');
        write(cmp.right(), sb, info.prec + 1);
        sb.append(" ? ");
        write(cmp.value(), sb, 1);   // 值分支中嵌套三元需加括号
        sb.append(" : ");
        write(cmp.def(), sb, 0);     // 右结合，else 分支可直接串联
        if (paren) sb.append(')');
    }

    private static void writeLogical(ExpressionCodec.Logical logic, StringBuilder sb, int ctxPrec) {
        int prec = logic.and() ? PREC_AND : PREC_OR;
        boolean paren = ctxPrec > prec;
        if (paren) sb.append('(');
        int leftCtx = logic.and() ? PREC_AND + 1 : PREC_OR + 1;
        write(logic.left(), sb, leftCtx);
        sb.append(logic.and() ? " && " : " || ");
        write(logic.right(), sb, prec);
        if (paren) sb.append(')');
    }

    private static void writeNot(ExpressionCodec.Not not, StringBuilder sb, int ctxPrec) {
        boolean paren = ctxPrec > PREC_UNARY;
        if (paren) sb.append('(');
        sb.append('!');
        write(not.expression(), sb, PREC_UNARY);
        if (paren) sb.append(')');
    }

    private static void writeConditionOperation(ExpressionCodec.ConditionOperation cond,
                                                StringBuilder sb, int ctxPrec) {
        List<ExpressionCodec.ConditionOperation.Condition> conditions = cond.conditions();
        if (conditions.isEmpty()) {
            write(cond.def(), sb, ctxPrec);
            return;
        }
        // 串联三元："c1 ? v1 : c2 ? v2 : def"
        boolean paren = ctxPrec > PREC_TERNARY;
        if (paren) sb.append('(');
        for (ExpressionCodec.ConditionOperation.Condition c : conditions) {
            OpInfo info = infixOp(c.comparison().getSerializedName());
            write(c.left(), sb, info.prec);
            sb.append(' ').append(c.comparison().getSerializedName()).append(' ');
            write(c.right(), sb, info.prec + 1);
            sb.append(" ? ");
            write(c.value(), sb, 1);
            sb.append(" : ");
        }
        write(cond.def(), sb, 0);
        if (paren) sb.append(')');
    }

    private static void writeTable(ExpressionCodec.LevelTable table, StringBuilder sb) {
        sb.append("table(");
        write(table.variable(), sb, 0);
        List<ExpressionCodec.LevelTable.TableEntry> entries = table.table();
        int n = entries.size();
        int i = 0;
        while (i < n) {
            // 尝试从 i 起最大连续位置段：第 (idx+1) 个 entry 的 key==Constant(idx+1) 且 value==Variable(idx+1)
            int runStart = i;
            while (i < n && isPositionalEntry(entries.get(i), i + 1)) i++;
            if (i - runStart >= 2) {
                // 连续段压缩为 $a..$b（a、b 为位置序号）
                sb.append(", $").append(runStart + 1).append("..$").append(i);
            } else {
                ExpressionCodec.LevelTable.TableEntry e = entries.get(runStart);
                sb.append(", ");
                write(e.key(), sb, 1);   // key 中的三元需括号（避免与表项 ':' 混淆）
                sb.append(": ");
                write(e.value(), sb, 1); // value 同理
                i = runStart + 1;
            }
        }
        // def 恒定写出（非 0 常量时才有信息量，但统一写出保证往返一致）
        if (!(table.def() instanceof ExpressionCodec.Constant c && c.value() == 0.0)) {
            sb.append(", ");
            write(table.def(), sb, 1);
        }
        sb.append(')');
    }

    /** 单条表项是否为「位置段」成员：key==Constant(pos) 且 value==Variable(pos)（即 $pos 形式）。 */
    private static boolean isPositionalEntry(ExpressionCodec.LevelTable.TableEntry e, int pos) {
        if (!(e.key() instanceof ExpressionCodec.Constant k) || k.value() != pos) return false;
        if (!(e.value() instanceof ExpressionCodec.Variable v)) return false;
        return v.node() instanceof VarRef vr && vr.name().equals(String.valueOf(pos));
    }

    private static void writeRead(ExpressionCodec.Read read, StringBuilder sb) {
        sb.append(read.id()).append('(');
        // 排序保证生成文件确定性：数字键按数值序，其余按字典序，数字键在前
        List<Map.Entry<String, ExpressionCodec.Value>> entries = new ArrayList<>(read.value().entrySet());
        entries.sort(Comparator
                .comparing((Map.Entry<String, ExpressionCodec.Value> e) -> !isNumeric(e.getKey()))
                .thenComparing(e -> isNumeric(e.getKey()) ? Double.parseDouble(e.getKey()) : 0.0)
                .thenComparing(Map.Entry::getKey));
        // 前缀数字键恰为连续 "1".."k" 时按位置参数写出（省键），其余仍写 name = value
        int positionalCount = 0;
        for (Map.Entry<String, ExpressionCodec.Value> e : entries) {
            if (e.getKey().equals(String.valueOf(positionalCount + 1))) {
                positionalCount++;
            } else {
                break;
            }
        }
        boolean first = true;
        for (int i = 0; i < entries.size(); i++) {
            Map.Entry<String, ExpressionCodec.Value> e = entries.get(i);
            if (!first) sb.append(", ");
            first = false;
            if (i >= positionalCount) {
                sb.append(e.getKey()).append(" = ");
            }
            Either<Double, MathExpression> v = e.getValue().value();
            if (v.left().isPresent()) {
                sb.append(formatDouble(v.left().get()));
            } else {
                write(v.right().orElseThrow(), sb, 1);
            }
        }
        sb.append(')');
    }

    private static boolean isNumeric(String s) {
        if (s.isEmpty()) return false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if ((c < '0' || c > '9') && c != '.') return false;
        }
        return true;
    }

    private static String binOpSymbol(ExpressionCodec.BinaryOperation bin) {
        if (bin instanceof ExpressionCodec.BinaryOperation.Add) return "+";
        if (bin instanceof ExpressionCodec.BinaryOperation.Subtract) return "-";
        if (bin instanceof ExpressionCodec.BinaryOperation.Multiply) return "*";
        if (bin instanceof ExpressionCodec.BinaryOperation.Divide) return "/";
        if (bin instanceof ExpressionCodec.BinaryOperation.Modulo) return "%";
        if (bin instanceof ExpressionCodec.BinaryOperation.Power) return "^";
        throw new IllegalArgumentException("Unknown binary op: " + bin.getClass().getName());
    }

    private static boolean isBooleanComparison(ExpressionCodec.BinaryOperation.Comparison cmp) {
        return cmp.value() instanceof ExpressionCodec.Constant c1 && c1.value() == 1.0
                && cmp.def() instanceof ExpressionCodec.Constant c2 && c2.value() == 0.0;
    }

    private static String formatDouble(double v) {
        if (v == (long) v) return String.valueOf((long) v);
        return String.valueOf(v);
    }

    // ==================== 异常 ====================

    /**
     * 表达式解析异常。
     *
     * <p>重构 3-5：除原有的字符位置外，额外携带<b>出错的表达式原文</b>与<b>数据来源 id</b>，
     * 使加载期报错可以直接定位到"哪个 JSON 的哪一段表达式的第几个字符"，
     * 而不再只给出一句无上下文的语法错误。
     *
     * <p>上下文由 {@link MathParser#parse(String)} 在抛出前通过 {@link #withContext} 补齐；
     * 解析器内部的各个 throw 点保持原样（只提供原始信息 + 位置），避免逐点改造。
     */
    public static final class ParseException extends RuntimeException {
        /** 出错字符在表达式中的下标；未知时为 -1。 */
        public final int position;
        /** 原始错误描述（不含位置/来源等装饰），用于二次包装时避免信息重复堆叠。 */
        public final String rawMessage;
        /** 出错的表达式原文；未知时为 {@code null}。 */
        public final String expression;
        /** 数据来源 id（如 {@code coe:stat_entry/xxx.json}）；未知时为 {@code null}。 */
        public final String sourceId;

        ParseException(String msg, int pos) {
            this(msg, pos, null, null);
        }

        private ParseException(String rawMessage, int pos, String expression, String sourceId) {
            super(format(rawMessage, pos, expression, sourceId));
            this.rawMessage = rawMessage;
            this.position = pos;
            this.expression = expression;
            this.sourceId = sourceId;
        }

        /**
         * 补齐来源上下文后返回一个新异常（已有的上下文优先保留，不被覆盖）。
         * 原始描述与位置原样透传，因此不会出现 "(at position n)" 重复拼接。
         */
        public ParseException withContext(String expression, String sourceId) {
            return new ParseException(
                    this.rawMessage,
                    this.position,
                    this.expression != null ? this.expression : expression,
                    this.sourceId != null ? this.sourceId : sourceId);
        }

        private static String format(String raw, int pos, String expr, String src) {
            StringBuilder sb = new StringBuilder();
            if (src != null && !src.isEmpty()) sb.append('[').append(src).append("] ");
            sb.append(raw);
            if (pos >= 0) sb.append(" (at position ").append(pos).append(')');
            if (expr != null) {
                sb.append("\n  表达式: ").append(expr);
                // 位置有效时补一行插入符，直观指向出错字符。
                if (pos >= 0 && pos <= expr.length()) {
                    sb.append("\n           ").append(" ".repeat(pos)).append('^');
                }
            }
            return sb.toString();
        }
    }
}
