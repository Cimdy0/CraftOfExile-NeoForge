package com.cimdy.coe.data.codec.support.damage;

import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.data.codec.support.stat.tag.GateTag;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import com.cimdy.coe.registry.DamageTypeRegistry;

/**
 * ADR-003 阶段1 · 伤害类型元数据索引（装载体一次性建好，热路径查表，消除 8 处"5"的散落）。
 *
 * <p>两表：{@link #BY_ID}（小写 id → 定义）与 {@link #BY_SLOT}（槽位 → 定义），外加
 * {@link #width}（已声明槽位数）。阶段1 恒为 5（不触碰 {@code DamageConvert}/{@code MixedDamage}
 * 的具名字段），行为零变化；阶段2 才把"伤害包"从定长具名结构改为按槽位索引。
 *
 * <p>内置默认在静态块与 {@link #load(ResourceManager)} 两处都建（保证"无数据包也能跑"），
 * 数据包 {@code coe/damage_type/<id>.json} 可覆盖同 id 或追加新类型；槽位冲突在装载期
 * <b>硬失败</b>（不再静默错位）。
 */
public final class DamageTypes {

    /** 数据包路径：coe/damage_type/<id>.json */
    public static final Identifier PATH = Identifier.parse("coe/damage_type");

    /** 数组宽度固定，避免热路径 float[] 长度随数据包变化（见 ADR-003 后果-性能）。 */
    public static final int SLOT_CAPACITY = 16;

    static final Map<String, DamageTypeDef> BY_ID = new HashMap<>();
    static final DamageTypeDef[] BY_SLOT = new DamageTypeDef[SLOT_CAPACITY];
    static int width = 5;

    static {
        builtinDefaults();
    }

    private DamageTypes() {
    }

    /** 内置默认（5 主类型 + 5 DoT 变体 + skill），与既有 {@code DamageTypeRegistry} 一致。 */
    public static void builtinDefaults() {
        register(new DamageTypeDef("skill", -1, "", null, Set.of(), false, false, false));
        register(new DamageTypeDef("physical", 0, GateTag.PHYSICAL, dotId("dot_physical"), Set.of(), false, false, false));
        register(new DamageTypeDef("chaos", 1, GateTag.CHAOS, dotId("dot_chaos"), Set.of(), false, false, false));
        register(new DamageTypeDef("fire", 2, GateTag.FIRE, dotId("dot_fire"), Set.of(), false, false, false));
        register(new DamageTypeDef("cold", 3, GateTag.COLD, dotId("dot_cold"), Set.of(), false, false, false));
        register(new DamageTypeDef("lightning", 4, GateTag.LIGHTNING, dotId("dot_light"), Set.of(), false, false, false));
        // 合成伤害类型（heart_of_ice 等「视作造成 X 冰霜伤害」类）：载体与 cold 同槽、同 statTag，
        // 但打 synthetic 标记——DamagePipeline 据此跳过真实生命/资源扣除（stageReduction 归零 newDamage），
        // 仅借伤害管线触发冰缓（stageAilmentAndCrit 的 applyColdAilment）。不占独立槽（slot 复用 cold=3）。
        register(new DamageTypeDef("synthetic_cold", 3, GateTag.COLD, null, Set.of("synthetic"), false, false, true));
        // DoT 变体：独立 ailment 输出类型，不占 damages[] 槽位（slot=-1），仅作元数据登记供阶段2扩展
        register(new DamageTypeDef("dot_physical", -1, GateTag.PHYSICAL, null, Set.of("damage_over_time"), false, true, false));
        register(new DamageTypeDef("dot_chaos", -1, GateTag.CHAOS, null, Set.of("damage_over_time"), false, true, false));
        register(new DamageTypeDef("dot_fire", -1, GateTag.FIRE, null, Set.of("damage_over_time"), false, true, false));
        register(new DamageTypeDef("dot_cold", -1, GateTag.COLD, null, Set.of("damage_over_time"), false, true, false));
        register(new DamageTypeDef("dot_light", -1, GateTag.LIGHTNING, null, Set.of("damage_over_time"), false, true, false));
        width = 5;
    }

    private static Identifier dotId(String name) {
        return Identifier.fromNamespaceAndPath(CraftOfExile.MODID, name);
    }

    /** 注册/覆盖一个伤害类型定义（小写 id 索引；slot>=0 写入 BY_SLOT 并刷新 width）。 */
    public static void register(DamageTypeDef d) {
        BY_ID.put(d.id().toLowerCase(Locale.ROOT), d);
        if (d.slot() >= 0 && d.slot() < SLOT_CAPACITY) {
            BY_SLOT[d.slot()] = d;
            if (d.slot() + 1 > width) width = d.slot() + 1;
        }
    }

    @Nullable
    public static DamageTypeDef defOf(String type) {
        return type == null ? null : BY_ID.get(type.toLowerCase(Locale.ROOT));
    }

    public static int slotOf(String type) {
        DamageTypeDef d = defOf(type);
        return d == null ? -1 : d.slot();
    }

    @Nullable
    public static Identifier dotOf(int slot) {
        DamageTypeDef d = (slot >= 0 && slot < SLOT_CAPACITY) ? BY_SLOT[slot] : null;
        return d == null ? null : d.dot();
    }

    /** 已声明槽位数（= 已声明伤害类型数，阶段1 恒为 5）。 */
    public static int width() {
        return width;
    }

    public static Set<String> scopeTagsOf(String type) {
        DamageTypeDef d = defOf(type);
        return d == null ? Set.of() : d.scopeTags();
    }

    /** 槽位 → 伤害类型 id（小写）；越界/空槽返回 null。 */
    public static String typeOf(int slot) {
        DamageTypeDef d = (slot >= 0 && slot < SLOT_CAPACITY) ? BY_SLOT[slot] : null;
        return d == null ? null : d.id();
    }

    /**
     * 主类型 stat 标签数组（长度=width，顺序与 damages[] 槽位一致：0=物理 1=混沌 2=火 3=冰 4=电）。
     * ADR-003 阶段2： formerly 硬编码于 {@link com.cimdy.coe.registry.DamageTypeRegistry#DAMAGE_TYPE_TAGS}，
     * 现统一从本表派生，使新增伤害类型经数据包即可扩展、不再写死 "5"。
     */
    public static String[] mainTypeTags() {
        String[] tags = new String[width];
        for (int i = 0; i < width; i++) {
            tags[i] = BY_SLOT[i] == null ? null : BY_SLOT[i].statTag();
        }
        return tags;
    }

    /** 全部已登记定义快照（供 datagen provider 导出）。 */
    public static List<DamageTypeDef> all() {
        return new ArrayList<>(BY_ID.values());
    }

    /**
     * 装载期从 {@code coe/damage_type/*.json} 扩展/覆盖内置表。先重置为内置默认再叠加数据包；
     * 槽位冲突（同槽被不同 id 占用）抛 {@link IllegalStateException} 使装载硬失败。
     */
    public static void load(ResourceManager manager) {
        BY_ID.clear();
        Arrays.fill(BY_SLOT, null);
        width = 5;
        builtinDefaults();
        Map<Identifier, Resource> files = manager.listResources(PATH.getPath(),
                id -> id.getPath().endsWith(".json"));
        for (Map.Entry<Identifier, Resource> entry : files.entrySet()) {
            try (InputStream stream = entry.getValue().open()) {
                JsonElement json = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
                DataResult<DamageTypeDef> result = DamageTypeDef.CODEC.parse(JsonOps.INSTANCE, json);
                DamageTypeDef def = result.result().orElse(null);
                if (def == null) {
                    result.error().ifPresent(err -> CraftOfExile.LOGGER.error(
                            "装载伤害类型失败 " + entry.getKey().getPath() + err.message()));
                    continue;
                }
                if (def.slot() >= 0 && def.slot() < SLOT_CAPACITY) {
                    DamageTypeDef occupied = BY_SLOT[def.slot()];
                    if (occupied != null && !occupied.id().equalsIgnoreCase(def.id())) {
                        throw new IllegalStateException("伤害类型槽位冲突：slot=" + def.slot()
                                + " 已被 " + occupied.id() + " 占用，新定义 " + def.id());
                    }
                }
                register(def);
            } catch (IOException e) {
                CraftOfExile.LOGGER.error("读取伤害类型资源失败 " + entry.getKey().getPath(), e);
            }
        }
    }
}
