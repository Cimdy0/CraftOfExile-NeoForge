package com.cimdy.coe.data.codec.support.addition;

import com.cimdy.coe.data.codec.skill.phase.action.movement.SpawnAction;
import com.cimdy.coe.registry.world.AttachmentRegistry;

/**
 * {@link AdditionData#data()}（{@code Map<String, Double>}）内标量键的常量表。
 *
 * <p>只收录「标记 / 简单计数」类标量，避免裸字符串在业务代码里到处漂移：
 * <ul>
 *   <li>布尔以 {@code 1.0 = true}、{@code 0.0 = false} 承载，读取用
 *       {@code AdditionData.getOrDefault(...) != 0.0}；</li>
 *   <li>整数以 double 承载，读取时 {@code (int)} 强转。</li>
 * </ul>
 * 复杂对象（Camp / UUID / StatStore / StatusStore 等）仍各自使用独立附件，不进本表。
 *
 * <p>各键的语义与来源：原本都是 {@code AttachmentRegistry} 上的独立附件，
 * 因仅用于标记与简单判定而统一迁入 {@link AdditionData}。
 */
public class AdditionKeys {
    /** 幼体标记（原 {@code AttachmentRegistry.CHILD}）：分裂 / 召唤产生的幼体实体，默认 false。 */
    public static final String CHILD = "child";
    /** 技能伴生召唤物标记（原 {@code AttachmentRegistry.IS_COMPANION}）：{@code SpawnAction} 在 companion=true 时写入，默认 false。 */
    public static final String IS_COMPANION = "is_companion";
    /** 召唤物标记（原 NBT {@code coe:summoned}）：{@code SpawnAction} 生成召唤物时写入，默认 false。 */
    public static final String SUMMONED = "summoned";
    /** 据点实体「战斗 ↔ 工作」开关（原 {@code AttachmentRegistry.COMBATANT}），<b>默认 true</b>（缺省值 1.0）。 */
    public static final String COMBATANT = "combatant";
    /** 据点盟友标记（原 {@code AttachmentRegistry.ALLY}）：据点终端对已捕获实体显式指定，默认 false。 */
    public static final String ALLY = "ally";
    /** 地雷标记（PoE2 地雷实体）：技能生成地雷时写入，作为「地雷的 X 提高 / 命中」类词缀的作用域标签来源，默认 false。 */
    public static final String MINE = "mine";
    /** 魔像标记（PoE2 魔像实体）：技能生成魔像时写入，作为「魔像的 X 提高 / 命中」类词缀的作用域标签来源，默认 false。 */
    public static final String GOLEM = "golem";
    /** 烙印标记（PoE2 烙印实体）：技能生成烙印时写入，作为「烙印的 X 提高 / 命中」类词缀的作用域标签来源，默认 false。 */
    public static final String BRAND = "brand";
    /** 尸体计数（原 {@code AttachmentRegistry.CORPSE_COUNT}）：使用者当前持有的尸体数，默认 0。 */
    public static final String CORPSE_COUNT = "corpse_count";
    /** 战吼强化攻击剩余次数（原 {@code AttachmentRegistry.EMPOWERED_ATTACKS}），默认 0。 */
    public static final String EMPOWERED_ATTACKS = "empowered_attacks";
    /** slam 授予的先祖强化攻击剩余次数（slam 技能完整执行退出时授予，纯「下一次攻击」语义、不过期），默认 0。 */
    public static final String ANCESTRAL_BOOST_ATTACKS = "ancestral_boost_attacks";
    /** 重晕授予的先祖强化攻击剩余次数（仅「重晕稀有/独特敌人」路径写入，受 4 秒窗口约束），默认 0。 */
    public static final String ANCESTRAL_BOOST_HEAVY_STUN_ATTACKS = "ancestral_boost_heavy_stun_attacks";
    /** 当前激活武器组（原 {@code AttachmentRegistry.ACTIVE_WEAPON_SET}）：0 = 全局，1–9 = 快捷栏栏位，默认 1。 */
    public static final String ACTIVE_WEAPON_SET = "active_weapon_set";
    /** 金币余额（PoE2 货币）：角色当前持有的金币数，默认 0。以 double 承载整数，读取时转 long。 */
    public static final String GOLD = "gold";

    private AdditionKeys() {
    }
}
