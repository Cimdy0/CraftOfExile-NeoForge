package com.cimdy.coe.data.codec.entity.render;

import com.cimdy.coe.data.codec.entity.EntityRead;
import com.cimdy.coe.data.codec.skill.core.Skill;
import com.cimdy.coe.data.codec.skill.phase.SkillPhase;
import com.cimdy.coe.data.codec.skill.phase.SkillPhasePlayer;
import com.cimdy.coe.data.codec.skill.phase.delivery.Delivery;
import com.cimdy.coe.data.codec.skill.phase.delivery.impl.area.AreaDelivery;
import com.cimdy.coe.data.codec.skill.phase.delivery.impl.projectile.ProjectileDelivery;
import com.cimdy.coe.CraftOfExile;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 技能皮肤运行时注册表 —— {@code data/<ns>/coe/skill_skin/*.json} 全部条目，按<b>技能 id</b>索引。
 *
 * <p>与 {@link EntityRenderDefs} 同范式：datagen 生成 JSON，由 {@code LoadEvent} 在资源重载时整体读入
 * 内存 Map；定义只读，运行期不修改，故无需同步。
 *
 * <p>关键语义：本表<b>按技能 id 索引</b>（如 {@code coe:fireball}），而投递物在装配时把
 * {@code EntityRead.render} 覆盖为技能 id（见 {@link Skill#withDeliveryRender} + {@link EntityRead#resolve}）。
 * 于是「同一份底层渲染定义（{@code coe:fire}）」可被多个技能各自锁定为不同外观，
 * 且「冰霜箭转火伤」后外观仍跟随技能而非伤害类型。
 */
public final class SkillSkinDefs {

    /** 数据根路径（位于 {@code data/<ns>/} 之下）。 */
    public static final String ROOT = "coe/skill_skin";
    /** 资源重载监听器键（{@code AddServerReloadListenersEvent#addListener} 取 Identifier）。 */
    public static final Identifier KEY = Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "skill_skin");

    private static final Map<Identifier, SkillSkinDef> MAP = new HashMap<>();

    /**
     * gen 期渲染映射（仅 datagen 期填充，供 {@code SkillSkinProvider} 落盘；运行时以 JSON 加载的 {@link #MAP} 为准）。
     * key = 技能 id（{@code coe:<path>}），value = 该技能当前（迁移前）使用的底层实体渲染 id。
     */
    public static final Map<Identifier, Identifier> GEN_RENDER = new HashMap<>();

    /**
     * 实体定义自带的渲染（当投递 {@code ref.render} 为空时回退用）：
     * 投掷物/危害物/残片体本身不含渲染覆盖，其外观来自实体定义的 render。
     */
    private static final Map<Identifier, Identifier> ENTITY_DEF_RENDER = Map.of(
            Identifier.fromNamespaceAndPath("coe", "arrow"), Identifier.fromNamespaceAndPath("coe", "arrow"),
            Identifier.fromNamespaceAndPath("coe", "grenade"), Identifier.fromNamespaceAndPath("coe", "grenade"),
            Identifier.fromNamespaceAndPath("coe", "hazard"), Identifier.fromNamespaceAndPath("coe", "smoke"),
            Identifier.fromNamespaceAndPath("coe", "remnant"), Identifier.fromNamespaceAndPath("coe", "arcane_blue"));

    private SkillSkinDefs() {
    }

    /** 按技能 id 取皮肤；缺失返回空（调用方应把「缺失」当作「无覆盖」而非报错）。 */
    public static Optional<SkillSkinDef> get(Identifier skillId) {
        return Optional.ofNullable(MAP.get(skillId));
    }

    public static Map<Identifier, SkillSkinDef> all() {
        return Map.copyOf(MAP);
    }

    /**
     * 把「技能渲染引用」解析为底层实体渲染定义 id。
     * 若 {@link #MAP} 含该 id（即它是技能 id）且该皮肤声明了 render → 返回 render；
     * 否则原样返回 id，<b>即视为「无覆盖」，交回投递/实体定义自带的默认外观</b>。
     * 装配期（{@link EntityRead#resolve}）据此把投递的 render 引用从技能 id 还原为底层渲染定义 id。
     *
     * <p><b>皮肤 render 为空时为何不返回技能 id</b>：技能 id 不是合法的
     * {@code coe:entity_render/*}，把它当渲染 id 传给 {@link com.cimdy.coe.data.codec.entity.EntityDef#withRender} 后，
     * {@code renderDef()} 在 {@code EntityRenderDefs} 查不到 → 渲染层 model 为 null →
     * <b>实体彻底不可见</b>。而「皮肤为空」的真实含义是「本技能不锁定外观、沿用默认」，
     * 故须让调用方的默认外观生效。故本方法<b>只做「有则还原、无则不变」</b>，
     * 绝不凭空造一个渲染 id。
     *
     * <p><b>外观锁定语义的边界</b>：本方法只做单点还原，不校验还原结果是否存在。
     * 若数据包被手改成悬空 id，实体仍会不可见 —— 故生成侧（{@link #recordRender}）
     * 必须只录真实存在的渲染 id，运行时不做兜底猜测（宁可显式不可见，也不静默换外观）。
     */
    public static Identifier resolveRender(Identifier id) {
        SkillSkinDef def = MAP.get(id);
        if (def == null) return id;
        return def.render().orElse(id);
    }

    /** 客户端兜底加载：仅在服务端重载尚未填充时从客户端资源包加载。 */
    public static void ensureClientLoaded() {
        if (FMLEnvironment.getDist() != Dist.CLIENT) return;
        if (MAP.isEmpty()) {
            load(Minecraft.getInstance().getResourceManager());
        }
    }

    /**
     * gen 期记录某技能<b>当前的底层渲染 id</b>（在 {@link Skill#withDeliveryRender} 覆盖之前抓取），
     * 供 {@code SkillSkinProvider} 落盘，使迁移后视觉零回归。
     *
     * <p><b>返回「是否录得」并由调用方据此决定是否覆盖外观</b>：覆盖与记录必须同生共死——
     * 若录不到底层渲染（技能无实体投递）却仍把投递外观覆写为技能 id，则该 id 既是
     * {@code coe:entity_render/*} 查不到的悬空引用（渲染层 model 解析为 null → 实体不可见），
     * 又因皮肤 render 为空而无法在装配期被 {@link #resolveRender} 还原。二者同进同退才自洽。
     *
     * @return 录得底层渲染 id 返回 {@code true}；无实体投递 / 无可用外观返回 {@code false}，
     *         调用方应<b>跳过</b> {@link Skill#withDeliveryRender}
     */
    public static boolean recordRender(Identifier skillId, Skill skill) {
        Identifier render = extractRender(skill);
        if (render == null) return false;
        GEN_RENDER.put(skillId, render);
        return true;
    }

    /** 遍历阶段树取出第一个实体投递的底层渲染 id（ref.render 或实体定义自带 render）。 */
    private static Identifier extractRender(Skill skill) {
        for (SkillPhasePlayer player : skill.phases()) {
            Identifier r = extractPhaseRender(player.phase());
            if (r != null) return r;
        }
        return null;
    }

    private static Identifier extractPhaseRender(SkillPhase phase) {
        Optional<Identifier> fromDelivery = phase.delivery().flatMap(SkillSkinDefs::deliveryRender);
        if (fromDelivery.isPresent()) return fromDelivery.get();
        for (SkillPhasePlayer child : phase.children()) {
            Identifier r = extractPhaseRender(child.phase());
            if (r != null) return r;
        }
        return null;
    }

    /** 单个投递的底层渲染：优先 ref.render，其次回退实体定义自带 render。 */
    private static Optional<Identifier> deliveryRender(Delivery d) {
        EntityRead ref = switch (d) {
            case ProjectileDelivery p -> p.ref();
            case AreaDelivery a -> a.ref().orElse(null);
            default -> null;
        };
        if (ref == null) return Optional.empty();
        if (ref.render().isPresent()) return ref.render();
        return Optional.ofNullable(ENTITY_DEF_RENDER.get(ref.ref()));
    }

    /** 由资源管理器整体重载（服务端资源包 / datapack 重载时调用）。 */
    public static int load(ResourceManager manager) {
        MAP.clear();
        Map<Identifier, Resource> resources = manager.listResources(
                ROOT, id -> id.getPath().endsWith(".json"));
        int loaded = 0;
        for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
            String path = entry.getKey().getPath();
            try (InputStream stream = entry.getValue().open()) {
                JsonElement json = JsonParser.parseReader(new InputStreamReader(stream));
                DataResult<SkillSkinDef> result =
                        SkillSkinDef.CODEC.parse(com.mojang.serialization.JsonOps.INSTANCE, json);
                result.result().ifPresent(def -> {
                    String relative = path.substring(path.indexOf(ROOT) + ROOT.length() + 1);
                    String name = relative.substring(0, relative.lastIndexOf(".json"));
                    MAP.put(Identifier.fromNamespaceAndPath(entry.getKey().getNamespace(), name), def);
                });
                result.error().ifPresent(error -> CraftOfExile.LOGGER.error(
                        "Error loading skill skin {}: {}", path, error.message()));
                if (result.result().isPresent()) loaded++;
            } catch (IOException e) {
                CraftOfExile.LOGGER.error("Failed to read skill skin resource: {}", path, e);
            }
        }
        return loaded;
    }
}
