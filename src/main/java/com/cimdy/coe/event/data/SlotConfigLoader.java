package com.cimdy.coe.event.data;

import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.data.codec.item.equipment.AccessorySlotDef;
import com.cimdy.coe.data.codec.item.equipment.FlaskSlotDef;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * 服务端配置加载器：从 {@code config/coe/slots.json} 读取饰品 / 药剂槽位组定义，
 * 取代原有的数据包生成（原 {@code data/coe/coe/accessory_slot|flask_slot/*.json}）。
 *
 * <p>文件不存在时写入内置默认种子（复刻迁移前的 5 个数据包条目），随后解码填充两个静态表
 * {@link #ACCESSORY_SLOTS} / {@link #FLASK_SLOTS}，供 {@link LoadEvent} 在服务端资源重载
 * （{@code AddServerReloadListenersEvent}）时拷入其运行期缓存
 * （{@code LoadEvent.ACCESSORY_SLOT_MAP} / {@code LoadEvent.FLASK_SLOT_MAP}）。
 *
 * <p>仿 {@code ui/widget/render/StylePresetLoader} 的骨架（load/read/write/seed），但本类纯服务端、
 * 不引用任何 client 包；解码用 {@link JsonOps#INSTANCE}（槽位定义仅含 int + Identifier，
 * 无注册表依赖，故无需 {@code RegistryOps}）。
 */
public final class SlotConfigLoader {

    /** 配置根对象：饰品槽组列表 + 药剂槽组列表。 */
    public record SlotConfig(List<AccessorySlotDef> accessorySlots, List<FlaskSlotDef> flaskSlots) {
        public static final Codec<SlotConfig> CODEC = RecordCodecBuilder.create(in -> in.group(
                Codec.list(AccessorySlotDef.CODEC).fieldOf("accessory_slots").forGetter(SlotConfig::accessorySlots),
                Codec.list(FlaskSlotDef.CODEC).fieldOf("flask_slots").forGetter(SlotConfig::flaskSlots)
        ).apply(in, SlotConfig::new));
    }

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(SlotConfigLoader.class);

    /** 配置基目录：{@code config/coe}。 */
    private static Path dir() { return FMLPaths.CONFIGDIR.get().resolve(CraftOfExile.MODID); }
    private static Path file() { return dir().resolve("slots.json"); }

    /** 饰品槽定义：groupId -> AccessorySlotDef（key 取 category 末段，详见 {@link #groupId}）。 */
    public static final Map<Identifier, AccessorySlotDef> ACCESSORY_SLOTS = new HashMap<>();
    /** 药剂槽定义：groupId -> FlaskSlotDef。 */
    public static final Map<Identifier, FlaskSlotDef> FLASK_SLOTS = new HashMap<>();

    private SlotConfigLoader() {}

    /**
     * 在同一把类锁内「读盘 + 发布快照」一步完成，为 LoadEvent 的两个并行监听器提供原子入口。
     *
     * <p>发布的是 {@link Map#copyOf} 不可变副本，调用方整体替换字段引用（而非 {@code clear()+putAll()}），
     * 故读侧完全无锁、永远看到一份完整快照：既消除了两个 bg 线程之间的并发读写，也消除了
     * 「bg 阶段写 / 主线程 tick 路径读」之间的中间态（后者会让 AccessorySlotRegistry 读到部分表，
     * 因 {@code out.isEmpty()} 只对整表空兜底，进而产生全局下标位移）。
     *
     * <p>模式与 {@code ItemTypeHierarchy} 的「构造新 Map → 不可变化 → 整体替换字段引用」一致。
     */
    public static synchronized void publish(
            BiConsumer<Map<Identifier, AccessorySlotDef>, Map<Identifier, FlaskSlotDef>> sink) {
        readAndFill();
        sink.accept(Map.copyOf(ACCESSORY_SLOTS), Map.copyOf(FLASK_SLOTS));
    }

    /** 实际读盘 / 播种并重建两张表（仅允许在持有类锁时调用）。 */
    private static void readAndFill() {
        SlotConfig cfg;
        if (!Files.exists(file())) {
            cfg = seed();
            writeConfig(cfg);
        } else {
            cfg = readConfig();
            if (cfg == null) cfg = seed();
        }
        ACCESSORY_SLOTS.clear();
        FLASK_SLOTS.clear();
        for (AccessorySlotDef d : cfg.accessorySlots()) putSlot(ACCESSORY_SLOTS, d.category(), d);
        for (FlaskSlotDef d : cfg.flaskSlots()) putSlot(FLASK_SLOTS, d.category(), d);
        LOGGER.info("Loaded {} accessory slots and {} flask slots from {}",
                ACCESSORY_SLOTS.size(), FLASK_SLOTS.size(), file());
    }

    /**
     * 写入单条槽位定义，并对 groupId 碰撞告警（R1）：groupId 只取 category 末段且强制 {@code coe}
     * 命名空间，故 {@code coe:armor/legs} 与 {@code coe:accessory/legs}、或 {@code other:armor/legs}
     * 都会压成同一个 {@code coe:legs}。保留后写覆盖（与数据包覆盖语义一致），但不静默。
     */
    private static <T> void putSlot(Map<Identifier, T> map, Identifier category, T def) {
        Identifier id = groupId(category);
        if (map.put(id, def) != null) {
            LOGGER.warn("slots.json: duplicate slot group id {} (category {}); the earlier entry for this group is overridden",
                    id, category);
        }
    }

    /**
     * 槽位组的运行期标识（groupId）：取 category 路径的末段（如 {@code coe:armor/legs} -> {@code coe:legs}），
     * 与迁移前数据包文件名（amulet/legs/quiver/life_flask/mana_flask）一致，
     * 使 {@code AccessorySlotRegistry.groupSlots} 等按分组名查询的逻辑保持兼容。
     */
    public static Identifier groupId(Identifier category) {
        String p = category.getPath();
        int idx = p.lastIndexOf('/');
        String leaf = idx >= 0 ? p.substring(idx + 1) : p;
        return Identifier.fromNamespaceAndPath(CraftOfExile.MODID, leaf);
    }

    /** 内置默认种子：复刻迁移前的 5 个数据包条目（3 饰品槽 + 2 药剂槽）。 */
    private static SlotConfig seed() {
        List<AccessorySlotDef> acc = new ArrayList<>();
        acc.add(new AccessorySlotDef(1, Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "accessory/amulet")));
        acc.add(new AccessorySlotDef(1, Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "armor/legs")));
        acc.add(new AccessorySlotDef(1, Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "weapon/off_hand/quiver")));
        List<FlaskSlotDef> fl = new ArrayList<>();
        fl.add(new FlaskSlotDef(1, Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "life_flask")));
        fl.add(new FlaskSlotDef(1, Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "mana_flask")));
        return new SlotConfig(acc, fl);
    }

    private static SlotConfig readConfig() {
        try {
            String text = Files.readString(file());
            JsonElement el = JsonParser.parseString(text);
            var res = SlotConfig.CODEC.parse(JsonOps.INSTANCE, el);
            if (res.result().isPresent()) return res.result().get();
            res.error().ifPresent(e -> LOGGER.error("slots.json parse error: {}", e.message()));
            return null;
        } catch (Exception e) {
            LOGGER.error("failed reading slots.json", e);
            return null;
        }
    }

    private static void writeConfig(SlotConfig cfg) {
        try {
            if (!Files.exists(dir())) Files.createDirectories(dir());
            var res = SlotConfig.CODEC.encodeStart(JsonOps.INSTANCE, cfg);
            if (res.result().isPresent()) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                Files.writeString(file(), gson.toJson(res.result().get()));
            }
        } catch (Exception e) {
            LOGGER.error("failed writing slots.json", e);
        }
    }
}
