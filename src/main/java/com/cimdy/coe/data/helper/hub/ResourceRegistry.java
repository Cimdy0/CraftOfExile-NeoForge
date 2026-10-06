package com.cimdy.coe.data.helper.hub;

import com.cimdy.coe.data.codec.skill.helper.state.ResourcePool;
import com.cimdy.coe.data.codec.support.stat.tag.GateTag;
import com.cimdy.coe.data.codec.support.status.def.StatusDefs;
import com.cimdy.coe.data.codec.support.status.def.StatusType;
import com.cimdy.coe.data.codec.support.status.runtime.StatusEngine;
import com.cimdy.coe.data.codec.support.status.runtime.StatusInstance;
import com.cimdy.coe.data.codec.support.status.runtime.StatusStatBridge;
import com.cimdy.coe.data.codec.support.stat.engine.StatContext;
import com.cimdy.coe.data.codec.support.stat.engine.StatEngine;
import com.cimdy.coe.data.codec.support.stat.engine.StatScopes;
import com.cimdy.coe.data.codec.support.stat.effect.GateTypeIds;
import com.cimdy.coe.data.codec.support.math.ExpressionContext;
import com.cimdy.coe.data.codec.support.addition.AdditionData;
import com.cimdy.coe.data.codec.support.addition.AdditionKeys;
import com.cimdy.coe.data.helper.combat.DamageHelper;
import com.cimdy.coe.data.helper.status.AilmentHelper;
import com.cimdy.coe.data.helper.hub.ReservationManager;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.registry.AttributeRegistry;
import com.cimdy.coe.data.codec.skill.core.SkillInstance;
import com.cimdy.coe.data.codec.skill.phase.action.Action;
import com.cimdy.coe.data.codec.skill.phase.action.ActionContext;
import com.cimdy.coe.data.codec.skill.phase.action.charge.ChargeType;
import com.cimdy.coe.data.codec.skill.phase.action.support.BannerAction;
import com.cimdy.coe.data.codec.skill.phase.event.SkillEventBus;
import com.cimdy.coe.data.codec.skill.phase.event.trigger.ChargeChangeEvent;
import com.cimdy.coe.data.codec.skill.phase.event.trigger.ComboEvent;
import com.cimdy.coe.event.Event;
import com.cimdy.coe.event.EventName;
import com.cimdy.coe.event.combat.GameEvent;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 资源统一注册表 —— 所有资源（基础资源 + 技能专用资源 + 数据包装义资源）的<b>单一真相源</b>。
 *
 * <p>资源键即 {@link GateTag} 资源标签（{@code health}/{@code mana}/{@code energy} ...）。
 * 每个资源注册一个 {@link ResourceEntry}（持 {@link ResourcePool} 状态池 + 元数据）。
 * {@link ResourceEventHelper} 的读取（current/max）优先经本表，操作（onHeal/onSpend）后续也将统一经本表调度，
 * 从而消除「Hub 表 / ResourcePool 适配器 / ResourceType 大 switch」三套并存。
 *
 * <p>G 阶段（中枢自给）：原资源抽象层旧实现（基础资源 / 技能资源 / 衍生资源）的逻辑已<b>内联</b>进本类，
 * 并暴露为公开的 hub 静态方法（方法签名与旧实现公共 API 一致，供 B/C 阶段机械重路由调用方）。
 * 本文件不再 import 或引用任何旧资源类。
 * 元技能能量 / 荣耀 / 连击三类附件（META_ENERGY_TRACKER / GLORY_TRACKER / COMBO_TRACKER）已由
 * {@link AttachmentRegistry} 改指向本类的嵌套记录 {@link MetaEnergySave} / {@link GlorySave} / {@link ComboSave}
 * （字段名 / 类型 / Codec 与旧 Save 逐字一致，存档按注册键 + codec 结构反序列化，不依赖 Java 类型名，故序列化兼容）。
 *
 * <p>{@link #bootstrap()} 幂等注册全部内置资源（基础 7 种 + 技能资源 + 数据包装义资源）；
 * 数据包装义资源经 {@link ResourceDataLoader} 在资源重载时注册 {@link ResourceDescriptor#toEntry()}。
 */
public final class ResourceRegistry {
    private static final Map<String, ResourceEntry> REGISTRY = new HashMap<>();
    private static boolean bootstrapped = false;

    // ───────────────────────────── 元素 / 维金灌入枚举（取代旧灌入实现中的 Element / InfusionKind） ─────────────────────────────
    /** 元素类型（火/冰/雷）；维金灌入为独立通道，不在此枚举内。 */
    public enum Element { FIRE, COLD, LIGHTNING }
    /** 被消耗的灌入种类（含维金），供调用方区分「消耗的是元素还是维金」。 */
    public enum InfusionKind { FIRE, COLD, LIGHTNING, VAAL }

    private ResourceRegistry() {
    }

    private static void ensure() {
        if (!bootstrapped) bootstrap();
    }

    public static void register(ResourceEntry entry) {
        REGISTRY.put(entry.key(), entry);
    }

    public static ResourceEntry get(String key) {
        ensure();
        return REGISTRY.get(key);
    }

    public static boolean has(String key) {
        ensure();
        return REGISTRY.containsKey(key);
    }

    /** 当前量（未注册返回 0）。 */
    public static double current(LivingEntity living, String key) {
        ResourceEntry e = get(key);
        return e == null ? 0.0 : e.pool().get(living, "default");
    }

    /** 上限（未注册返回 -1）。 */
    public static double max(LivingEntity living, String key) {
        ResourceEntry e = get(key);
        return e == null ? -1.0 : e.max(living);
    }

    /** 生成资源，返回实际增量（已处理溢出/上限）。 */
    public static double generate(LivingEntity living, String key, double amount, Double cap, long gameTime) {
        ResourceEntry e = get(key);
        if (e == null || !e.recoverable()) return 0.0;
        double before = e.pool().get(living, "default");
        e.pool().generate(living, "default", amount, cap, gameTime);
        return e.pool().get(living, "default") - before;
    }

    /** 消耗资源，返回实际消耗量。 */
    public static double consume(LivingEntity living, String key, double amount) {
        ResourceEntry e = get(key);
        if (e == null) return 0.0;
        return e.pool().consume(living, "default", amount);
    }

    /** 每 tick 推进所有已注册资源的衰减/自动重置。 */
    public static void tickAll(LivingEntity living, long gameTime) {
        ensure();
        for (ResourceEntry e : REGISTRY.values()) e.pool().tick(living, gameTime);
    }

    public static void bootstrap() {
        if (bootstrapped) return;
        bootstrapped = true;
        registerBasic();
        registerSkill();
        registerDerived();
    }

    // ───────────────────────────── 基础 7 资源（health/mana/energy_shield/runic_ward/rage/stun/spirit） ─────────────────────────────

    private static void registerBasic() {
        register(ResourceEntry.builder(GateTag.HEALTH, new ResourcePool() {
            @Override public double get(LivingEntity l, String id) { return l.getHealth(); }
            @Override public void generate(LivingEntity l, String id, double inc, Double cap, long t) { l.heal((float) inc); }
            @Override public double consume(LivingEntity l, String id, double amt) {
                float before = l.getHealth();
                l.setHealth(Math.max(0f, before - (float) amt));
                return before - l.getHealth();
            }
            @Override public void clear(LivingEntity l, String id) { l.setHealth(0f); }
            @Override public void tick(LivingEntity l, long t) { }
        }).isFloat(true).maxReader(LivingEntity::getMaxHealth).overflow(OverflowMode.REPLACE).recoverable(true).build());

        register(ResourceEntry.builder(GateTag.MANA, new ResourcePool() {
            @Override public double get(LivingEntity l, String id) { return getMana(l); }
            @Override public void generate(LivingEntity l, String id, double inc, Double cap, long t) { healMana(l, (float) inc); }
            @Override public double consume(LivingEntity l, String id, double amt) {
                float before = getMana(l);
                hurtMana(l, (float) amt);
                return before - getMana(l);
            }
            @Override public void clear(LivingEntity l, String id) { setMana(l, 0f); }
            @Override public void tick(LivingEntity l, long t) { }
        }).isFloat(true).maxReader(ResourceRegistry::getMaxMana).recoverable(true).build());

        register(ResourceEntry.builder(GateTag.ENERGY_SHIELD, new ResourcePool() {
            @Override public double get(LivingEntity l, String id) { return getEnergyShield(l); }
            @Override public void generate(LivingEntity l, String id, double inc, Double cap, long t) { healEnergyShield(l, (float) inc); }
            @Override public double consume(LivingEntity l, String id, double amt) {
                float before = getEnergyShield(l);
                setEnergyShield(l, Math.max(0f, before - (float) amt));
                return before - getEnergyShield(l);
            }
            @Override public void clear(LivingEntity l, String id) { setEnergyShield(l, 0f); }
            @Override public void tick(LivingEntity l, long t) { }
        }).isFloat(true).maxReader(ResourceRegistry::getMaxEnergyShield).recoverable(true).build());

        register(ResourceEntry.builder(GateTag.RUNIC_WARD, new ResourcePool() {
            @Override public double get(LivingEntity l, String id) { return getRunicWard(l); }
            @Override public void generate(LivingEntity l, String id, double inc, Double cap, long t) { healRunicWard(l, (float) inc); }
            @Override public double consume(LivingEntity l, String id, double amt) {
                float before = getRunicWard(l);
                setRunicWard(l, Math.max(0f, before - (float) amt));
                return before - getRunicWard(l);
            }
            @Override public void clear(LivingEntity l, String id) { setRunicWard(l, 0f); }
            @Override public void tick(LivingEntity l, long t) { }
        }).isFloat(true).maxReader(ResourceRegistry::getMaxRunicWard).recoverable(true).build());

        register(ResourceEntry.builder(GateTag.RAGE, new ResourcePool() {
            @Override public double get(LivingEntity l, String id) { return getRage(l); }
            @Override public void generate(LivingEntity l, String id, double inc, Double cap, long t) { healRageBackend(l, (float) inc); }
            @Override public double consume(LivingEntity l, String id, double amt) { return spendRageBackend(l, (float) amt); }
            @Override public void clear(LivingEntity l, String id) { }
            @Override public void tick(LivingEntity l, long t) { }
        }).isFloat(true).maxReader(ResourceRegistry::getMaxRage).recoverable(true).build());

        register(ResourceEntry.builder(GateTag.STUN, new ResourcePool() {
            @Override public double get(LivingEntity l, String id) { return getStunBuildup(l); }
            @Override public void generate(LivingEntity l, String id, double inc, Double cap, long t) { healStunBackend(l, (float) inc); }
            @Override public double consume(LivingEntity l, String id, double amt) { return spendStunBackend(l, (float) amt); }
            @Override public void clear(LivingEntity l, String id) { }
            @Override public void tick(LivingEntity l, long t) { }
        }).isFloat(true).fixedMax(1.0).recoverable(true).build());

        register(ResourceEntry.builder(GateTag.SPIRIT, new ResourcePool() {
            @Override public double get(LivingEntity l, String id) { return getSpirit(l); }
            @Override public void generate(LivingEntity l, String id, double inc, Double cap, long t) { setSpirit(l, getSpirit(l) + (float) inc); }
            @Override public double consume(LivingEntity l, String id, double amt) { return hurtSpirit(l, (float) amt); }
            @Override public void clear(LivingEntity l, String id) { setSpirit(l, 0f); }
            @Override public void tick(LivingEntity l, long t) { }
        }).isFloat(true).maxReader(l -> l.getAttributeValue(AttributeRegistry.MAX_SPIRIT)).build());
    }

    // ───────────────────────────── 技能资源（energy/glory/combo/corpse） ─────────────────────────────

    private static void registerSkill() {
        register(ResourceEntry.builder(GateTag.ENERGY, new ResourcePool() {
            @Override public double get(LivingEntity l, String id) {
                return (id == null || id.equals("default")) ? totalMetaEnergy(l) : getMetaEnergy(l, id);
            }
            @Override public void generate(LivingEntity l, String id, double inc, Double cap, long t) {
                generateMetaEnergy(l, id, (int) Math.round(inc), t);
            }
            @Override public double consume(LivingEntity l, String id, double amount) {
                return consumeMetaEnergy(l, id, (int) Math.round(amount));
            }
            @Override public void clear(LivingEntity l, String id) { clearMetaEnergy(l, id); }
            @Override public void tick(LivingEntity l, long t) { tickMetaEnergy(l, t); }
        }).isFloat(false).maxReader(l -> l.getAttributeValue(AttributeRegistry.META_ENERGY_MAX)).recoverable(true).build());

        register(ResourceEntry.builder(GateTag.GLORY, new ResourcePool() {
            @Override public double get(LivingEntity l, String id) { return getGlory(l, id); }
            @Override public void generate(LivingEntity l, String id, double inc, Double cap, long t) {
                generateGlory(l, id, (int) Math.round(inc), cap == null ? GLORY_CAP : cap.intValue(), t);
            }
            @Override public double consume(LivingEntity l, String id, double amount) {
                return consumeGlory(l, id, (int) Math.round(amount));
            }
            @Override public void clear(LivingEntity l, String id) { clearGlory(l, id); }
            @Override public void tick(LivingEntity l, long t) { tickGlory(l, t); }
        }).isFloat(false).fixedMax(GLORY_CAP).build());

        register(ResourceEntry.builder(GateTag.COMBO, new ResourcePool() {
            @Override public double get(LivingEntity l, String id) { return getCombo(l, id).count(); }
            @Override public void generate(LivingEntity l, String id, double inc, Double cap, long t) {
                int maxStacks = cap == null ? Integer.MAX_VALUE : cap.intValue();
                int times = (int) Math.round(Math.max(0, inc));
                for (int i = 0; i < times; i++) {
                    buildCombo(l, id, maxStacks, 0, List.of(), null, null, null, t);
                }
            }
            @Override public double consume(LivingEntity l, String id, double amount) {
                return consumeCombo(l, id, (int) Math.round(amount), List.of(), null, null, null);
            }
            @Override public void clear(LivingEntity l, String id) {
                Map<String, ComboData> map = new HashMap<>(l.getData(AttachmentRegistry.COMBO_TRACKER).combos());
                map.remove(id);
                l.setData(AttachmentRegistry.COMBO_TRACKER, new ComboSave(map));
            }
            @Override public void tick(LivingEntity l, long t) { tickCombo(l, t); }
        }).isFloat(false).build());

        register(ResourceEntry.builder(GateTag.CORPSE, new ResourcePool() {
            @Override public double get(LivingEntity l, String id) { return getCorpse(l); }
            @Override public void generate(LivingEntity l, String id, double inc, Double cap, long t) { addCorpse(l, (int) Math.round(inc)); }
            @Override public double consume(LivingEntity l, String id, double amount) {
                return consumeCorpse(l, (int) Math.round(amount)) ? amount : 0.0;
            }
            @Override public void clear(LivingEntity l, String id) { AdditionData.addOrReplaceData(l, AdditionKeys.CORPSE_COUNT, 0.0); }
            @Override public void tick(LivingEntity l, long t) { }
        }).isFloat(false).build());
    }

    // ───────────────────────────── 既有可变数值资源（充能球 / 波动 / 元素灌入） ─────────────────────────────

    private static void registerDerived() {
        register(ResourceEntry.builder("endurance_charge",
                        new BoundResourcePool(new ResourceSource("status_stacks", "endurance_charge", null), 0, false, 0))
                .isFloat(false).maxReader(l -> l.getAttributeValue(AttributeRegistry.MAX_ENDURANCE_CHARGES)).build());
        register(ResourceEntry.builder("frenzy_charge",
                        new BoundResourcePool(new ResourceSource("status_stacks", "frenzy_charge", null), 0, false, 0))
                .isFloat(false).maxReader(l -> l.getAttributeValue(AttributeRegistry.MAX_FRENZY_CHARGES)).build());
        register(ResourceEntry.builder("power_charge",
                        new BoundResourcePool(new ResourceSource("status_stacks", "power_charge", null), 0, false, 0))
                .isFloat(false).maxReader(l -> l.getAttributeValue(AttributeRegistry.MAX_POWER_CHARGES)).build());
        register(ResourceEntry.builder(GateTag.VOLATILITY, new ResourcePool() {
            @Override public double get(LivingEntity l, String id) { return getVolatility(l); }
            @Override public void generate(LivingEntity l, String id, double inc, Double cap, long t) {
                setVolatility(l, getVolatility(l) + (float) inc); // 钳制到 [0, max]，与原 setVolatility 同语义，仅经中枢新增转化/门控/事件
            }
            @Override public double consume(LivingEntity l, String id, double amt) {
                float before = getVolatility(l);
                setVolatility(l, before - (float) amt);
                return before - getVolatility(l);
            }
            @Override public void clear(LivingEntity l, String id) { setVolatility(l, 0f); }
            @Override public void tick(LivingEntity l, long t) { }
        }).isFloat(true).maxReader(ResourceRegistry::getMaxVolatility).recoverable(true).build());
        register(ResourceEntry.builder("infusion_fire",
                        new BoundResourcePool(new ResourceSource("status_stacks", "infusion_fire", null), 0, false, 0))
                .isFloat(false).maxReader(l -> l.getAttributeValue(AttributeRegistry.MAX_INFUSION)).build());
        register(ResourceEntry.builder("infusion_cold",
                        new BoundResourcePool(new ResourceSource("status_stacks", "infusion_cold", null), 0, false, 0))
                .isFloat(false).maxReader(l -> l.getAttributeValue(AttributeRegistry.MAX_INFUSION)).build());
        register(ResourceEntry.builder("infusion_lightning",
                        new BoundResourcePool(new ResourceSource("status_stacks", "infusion_lightning", null), 0, false, 0))
                .isFloat(false).maxReader(l -> l.getAttributeValue(AttributeRegistry.MAX_INFUSION)).build());
        register(ResourceEntry.builder("infusion_vaal",
                        new BoundResourcePool(new ResourceSource("status_stacks", "infusion_vaal", null), 0, false, 0))
                .isFloat(false).maxReader(l -> l.getAttributeValue(AttributeRegistry.MAX_INFUSION)).build());

        register(ResourceEntry.builder("infusion",
                        new ResourcePool() {
                            @Override public double get(LivingEntity l, String id) { return getInfusion(l); }
                            @Override public void generate(LivingEntity l, String id, double inc, Double cap, long t) {
                                gainInfusion(l, Element.FIRE, (float) inc);
                            }
                            @Override public double consume(LivingEntity l, String id, double amt) {
                                float before = getInfusion(l);
                                consumeInfusionAny(l, (float) amt);
                                return before - getInfusion(l);
                            }
                            @Override public void clear(LivingEntity l, String id) { }
                            @Override public void tick(LivingEntity l, long t) { }
                        })
                .isFloat(false).maxReader(l -> l.getAttributeValue(AttributeRegistry.MAX_TOTAL_ELEMENTAL_INFUSIONS)).build());
    }

    // ════════════════════════════════════════════════════ 元技能能量（MetaEnergy） ════════════════════════════════════════════════════

    /** 能量衰减宽限期（tick）。 */
    public static final int META_ENERGY_DECAY_GRACE_TICKS = 300;
    /** 衰减速率：每 tick 衰减量。 */
    public static final int META_ENERGY_DECAY_PER_SECOND = 4;

    /** 单技能能量条目：当前量 + 最近获得 tick。 */
    public record MetaEnergyData(int amount, long lastGainTick) {
        public static final MetaEnergyData EMPTY = new MetaEnergyData(0, 0);
        public static final Codec<MetaEnergyData> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.INT.fieldOf("amount").forGetter(MetaEnergyData::amount),
                Codec.LONG.fieldOf("last_gain_tick").forGetter(MetaEnergyData::lastGainTick)
        ).apply(i, MetaEnergyData::new));
    }

    /** 实体附件的值类型（由 AttachmentRegistry.META_ENERGY_TRACKER 持有）。 */
    public record MetaEnergySave(Map<String, MetaEnergyData> tracks) {
        public static final MetaEnergySave EMPTY = new MetaEnergySave(Map.of());
        public static final Codec<MetaEnergySave> CODEC = Codec.unboundedMap(Codec.STRING, MetaEnergyData.CODEC)
                .xmap(MetaEnergySave::new, MetaEnergySave::tracks);

        private static Map<String, MetaEnergyData> tracksOf(LivingEntity living) {
            return living.getData(AttachmentRegistry.META_ENERGY_TRACKER).tracks();
        }

        private static void set(LivingEntity living, String skillId, MetaEnergyData data) {
            Map<String, MetaEnergyData> map = new HashMap<>(tracksOf(living));
            if (data.amount() <= 0) map.remove(skillId);
            else map.put(skillId, data);
            living.setData(AttachmentRegistry.META_ENERGY_TRACKER, new MetaEnergySave(map));
        }

        /** 读取某元技能当前能量（无记录返回 0）。 */
        public static int get(LivingEntity living, String skillId) {
            return tracksOf(living).getOrDefault(skillId, MetaEnergyData.EMPTY).amount();
        }

        /** 生成能量：增加 increment（封顶 = META_ENERGY_MAX 属性），重置衰减计时。 */
        public static void generate(LivingEntity living, String skillId, int increment, long gameTime) {
            int cap = Math.max(1, (int) living.getAttributeValue(AttributeRegistry.META_ENERGY_MAX));
            MetaEnergyData cur = tracksOf(living).getOrDefault(skillId, MetaEnergyData.EMPTY);
            int next = Math.min(cur.amount() + increment, cap);
            set(living, skillId, new MetaEnergyData(next, gameTime));
        }

        /** 消耗能量：减少 amount，返回实际消耗量。 */
        public static int consume(LivingEntity living, String skillId, int amount) {
            MetaEnergyData cur = tracksOf(living).getOrDefault(skillId, MetaEnergyData.EMPTY);
            int real = Math.min(Math.max(0, amount), cur.amount());
            set(living, skillId, new MetaEnergyData(cur.amount() - real, cur.lastGainTick()));
            return real;
        }

        /** 重置某技能能量为 0（触发后清空）。 */
        public static void clear(LivingEntity living, String skillId) {
            set(living, skillId, MetaEnergyData.EMPTY);
        }

        /** 每 tick 衰减：超过宽限期未获得能量，按 DECAY_PER_SECOND/20 每 tick 回落。 */
        public static void tick(LivingEntity living, long gameTime) {
            MetaEnergySave save = living.getData(AttachmentRegistry.META_ENERGY_TRACKER);
            if (save.tracks().isEmpty()) return;
            Map<String, MetaEnergyData> updated = new HashMap<>(save.tracks());
            boolean changed = false;
            for (Map.Entry<String, MetaEnergyData> e : save.tracks().entrySet()) {
                MetaEnergyData g = e.getValue();
                if (g.amount() <= 0) continue;
                long elapsed = gameTime - g.lastGainTick();
                if (elapsed > META_ENERGY_DECAY_GRACE_TICKS) {
                    int decay = Math.max(1, (int) ((elapsed - META_ENERGY_DECAY_GRACE_TICKS) * META_ENERGY_DECAY_PER_SECOND / 20.0));
                    int left = g.amount() - decay;
                    if (left <= 0) updated.remove(e.getKey());
                    else updated.put(e.getKey(), new MetaEnergyData(left, g.lastGainTick()));
                    changed = true;
                }
            }
            if (changed) living.setData(AttachmentRegistry.META_ENERGY_TRACKER, new MetaEnergySave(updated));
        }

        /** 全技能能量聚合。 */
        public static int total(LivingEntity living) {
            return tracksOf(living).values().stream().mapToInt(MetaEnergyData::amount).sum();
        }
    }

    /** 读取某元技能当前能量。 */
    public static int getMetaEnergy(LivingEntity living, String skillId) { return MetaEnergySave.get(living, skillId); }
    /** 生成元技能能量。 */
    public static void generateMetaEnergy(LivingEntity living, String skillId, int increment, long gameTime) {
        MetaEnergySave.generate(living, skillId, increment, gameTime);
    }
    /** 消耗元技能能量，返回实际消耗量。 */
    public static int consumeMetaEnergy(LivingEntity living, String skillId, int amount) {
        return MetaEnergySave.consume(living, skillId, amount);
    }
    /** 清空某元技能能量。 */
    public static void clearMetaEnergy(LivingEntity living, String skillId) { MetaEnergySave.clear(living, skillId); }
    /** 每 tick 衰减元技能能量。 */
    public static void tickMetaEnergy(LivingEntity living, long gameTime) { MetaEnergySave.tick(living, gameTime); }
    /** 全技能元能量聚合。 */
    public static int totalMetaEnergy(LivingEntity living) { return MetaEnergySave.total(living); }

    // ════════════════════════════════════════════════════ 荣耀（Glory） ════════════════════════════════════════════════════

    /** 荣耀衰减宽限期：15 秒 = 300 tick。 */
    public static final int GLORY_DECAY_GRACE_TICKS = 300;
    /** 荣耀衰减速率：每秒 2 点。 */
    public static final int GLORY_DECAY_PER_SECOND = 2;
    /** 单槽位顶层上限。 */
    public static final int GLORY_CAP = 25;
    /** 战旗荣耀槽 id（BannerAction 消耗、DeathHelper 累积，两处同口径）。 */
    public static final String BANNER_GLORY = "banner";

    /** 单个荣耀槽位数据。 */
    public record GloryData(int amount, long lastGainTick) {
        public static final GloryData EMPTY = new GloryData(0, 0);
        public static final Codec<GloryData> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.INT.fieldOf("amount").forGetter(GloryData::amount),
                Codec.LONG.fieldOf("last_gain_tick").forGetter(GloryData::lastGainTick)
        ).apply(i, GloryData::new));
    }

    /** 实体附件的值类型（由 AttachmentRegistry.GLORY_TRACKER 持有）。 */
    public record GlorySave(Map<String, GloryData> tracks) {
        public static final GlorySave EMPTY = new GlorySave(Map.of());
        public static final Codec<GlorySave> CODEC = Codec.unboundedMap(Codec.STRING, GloryData.CODEC)
                .xmap(GlorySave::new, GlorySave::tracks);

        private static Map<String, GloryData> tracksOf(LivingEntity living) {
            return living.getData(AttachmentRegistry.GLORY_TRACKER).tracks();
        }

        private static void set(LivingEntity living, String gloryId, GloryData data) {
            Map<String, GloryData> map = new HashMap<>(tracksOf(living));
            if (data.amount() <= 0) map.remove(gloryId);
            else map.put(gloryId, data);
            living.setData(AttachmentRegistry.GLORY_TRACKER, new GlorySave(map));
        }

        /** 读取荣耀值（不存在返回 0）。 */
        public static int get(LivingEntity living, String gloryId) {
            return tracksOf(living).getOrDefault(gloryId, GloryData.EMPTY).amount();
        }

        /** 生成荣耀：增加 increment（封顶 cap），重置衰减计时（荣耀生成速率 GLORY_GENERATION 经 fold 通道读取）。 */
        public static void generate(LivingEntity living, String gloryId, int increment, int cap, long gameTime) {
            double rate = readGloryGeneration(living);
            int scaled = (int) Math.round(increment * Math.max(0.0, rate));
            GloryData cur = tracksOf(living).getOrDefault(gloryId, GloryData.EMPTY);
            int next = Math.min(cur.amount() + scaled, Math.max(1, Math.min(cap, GLORY_CAP)));
            set(living, gloryId, new GloryData(next, gameTime));
        }

        private static double readGloryGeneration(LivingEntity living) {
            if (living == null) return 1.0;
            try (AutoCloseable scope = StatScopes.enter(living, StatContext.of(Set.of(GateTag.GLORY), null))) {
                return living.getAttributeValue(AttributeRegistry.GLORY_GENERATION);
            } catch (Exception ignored) {
                return 1.0;
            }
        }

        /** 消耗荣耀：减少 amount，返回实际消耗量；真实扣减时发射 CONSUME_GLORY。 */
        public static int consume(LivingEntity living, String gloryId, int amount) {
            GloryData cur = tracksOf(living).getOrDefault(gloryId, GloryData.EMPTY);
            int real = Math.min(Math.max(0, amount), cur.amount());
            if (real > 0 && rollSkipConsumeGlory(living)) return 0;
            set(living, gloryId, new GloryData(cur.amount() - real, cur.lastGainTick()));
            if (real > 0) {
                GameEvent.emit(EventName.CONSUME_GLORY, living, null, Set.of(GateTag.GLORY), null, real);
            }
            return real;
        }

        private static boolean rollSkipConsumeGlory(LivingEntity living) {
            if (living == null) return false;
            double chance;
            try (AutoCloseable scope = StatScopes.enter(living, StatContext.of(Set.of(GateTag.GLORY), null))) {
                chance = living.getAttributeValue(AttributeRegistry.GLORY_CONSUME_SKIP_CHANCE);
            } catch (Exception ignored) {
                return false;
            }
            if (chance <= 0) return false;
            return living.getRandom().nextDouble() < Math.min(1.0, chance);
        }

        /** 重置荣耀为 0。 */
        public static void clear(LivingEntity living, String gloryId) {
            set(living, gloryId, GloryData.EMPTY);
        }

        /** 回收荣耀（战旗到期等）：直接按 amount 平加，封顶全局 CAP，重置衰减计时。 */
        public static void recover(LivingEntity living, String gloryId, int amount, long gameTime) {
            if (amount <= 0) return;
            int cur = tracksOf(living).getOrDefault(gloryId, GloryData.EMPTY).amount();
            int next = Math.min(cur + amount, GLORY_CAP);
            set(living, gloryId, new GloryData(next, gameTime));
        }

        /** 每 tick 衰减。 */
        public static void tick(LivingEntity living, long gameTime) {
            GlorySave save = living.getData(AttachmentRegistry.GLORY_TRACKER);
            if (save.tracks().isEmpty()) return;
            Map<String, GloryData> updated = new HashMap<>(save.tracks());
            boolean changed = false;
            for (Map.Entry<String, GloryData> e : save.tracks().entrySet()) {
                GloryData g = e.getValue();
                if (g.amount() <= 0) continue;
                long elapsed = gameTime - g.lastGainTick();
                if (elapsed > GLORY_DECAY_GRACE_TICKS) {
                    int decay = Math.max(1, (int) ((elapsed - GLORY_DECAY_GRACE_TICKS) * GLORY_DECAY_PER_SECOND / 20.0));
                    int left = g.amount() - decay;
                    if (left <= 0) updated.remove(e.getKey());
                    else updated.put(e.getKey(), new GloryData(left, g.lastGainTick()));
                    changed = true;
                }
            }
            if (changed) living.setData(AttachmentRegistry.GLORY_TRACKER, new GlorySave(updated));
        }
    }

    /** 读取荣耀值。 */
    public static int getGlory(LivingEntity living, String gloryId) { return GlorySave.get(living, gloryId); }
    /** 生成荣耀。 */
    public static void generateGlory(LivingEntity living, String gloryId, int increment, int cap, long gameTime) {
        GlorySave.generate(living, gloryId, increment, cap, gameTime);
    }
    /** 消耗荣耀，返回实际消耗量。 */
    public static int consumeGlory(LivingEntity living, String gloryId, int amount) {
        return GlorySave.consume(living, gloryId, amount);
    }
    /** 清空荣耀。 */
    public static void clearGlory(LivingEntity living, String gloryId) { GlorySave.clear(living, gloryId); }
    /** 回收荣耀。 */
    public static void recoverGlory(LivingEntity living, String gloryId, int amount, long gameTime) {
        GlorySave.recover(living, gloryId, amount, gameTime);
    }
    /** 每 tick 衰减荣耀。 */
    public static void tickGlory(LivingEntity living, long gameTime) { GlorySave.tick(living, gameTime); }

    // ════════════════════════════════════════════════════ 连击（Combo） ════════════════════════════════════════════════════

    /** 单个连击槽位数据（每个 comboId 一组）。progress = 分数累积层进度（连击变慢用，默认 0）。 */
    public record ComboData(int count, long lastBuildTick, int resetDelayTicks, double progress) {
        public static final ComboData EMPTY = new ComboData(0, 0, -1, 0.0);
        public static final Codec<ComboData> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.INT.fieldOf("count").forGetter(ComboData::count),
                Codec.LONG.fieldOf("last_build_tick").forGetter(ComboData::lastBuildTick),
                Codec.INT.optionalFieldOf("reset_delay_ticks", -1).forGetter(ComboData::resetDelayTicks),
                Codec.DOUBLE.optionalFieldOf("progress", 0.0).forGetter(ComboData::progress)
        ).apply(i, ComboData::new));
    }

    /** 实体附件的值类型：Map<comboId, ComboData>（由 AttachmentRegistry.COMBO_TRACKER 持有）。 */
    public record ComboSave(Map<String, ComboData> combos) {
        public static final ComboSave EMPTY = new ComboSave(Map.of());
        public static final Codec<ComboSave> CODEC = Codec.unboundedMap(Codec.STRING, ComboData.CODEC)
                .xmap(ComboSave::new, ComboSave::combos);

        public static ComboData get(LivingEntity living, String comboId) {
            return living.getData(AttachmentRegistry.COMBO_TRACKER).combos().getOrDefault(comboId, ComboData.EMPTY);
        }

        /** 构建连击：按 COMBO_SLOWDOWN 分数累积层进度，满 1.0 才 +1 层；到顶时执行 onMaxStackActions 并发布 ComboEvent。 */
        public static void build(LivingEntity living, String comboId, int maxStacks, int resetDelay,
                                 List<Action> onMaxStackActions, ActionContext ctx,
                                 SkillEventBus bus, SkillInstance skill, long gameTime) {
            ComboData cur = get(living, comboId);
            int capped = Math.max(1, maxStacks);
            double slow = Math.min(living.getAttributeValue(AttributeRegistry.COMBO_SLOWDOWN) / 100.0, 0.99);
            double progress = cur.progress() + (1.0 - slow);
            int gain = (int) Math.floor(progress);
            if (gain <= 0) {
                living.setData(AttachmentRegistry.COMBO_TRACKER, new ComboSave(replace(living, comboId,
                        new ComboData(cur.count(), gameTime, resetDelay, progress))));
                return;
            }
            int next = Math.min(cur.count() + gain, capped);
            double remain = progress - gain;
            living.setData(AttachmentRegistry.COMBO_TRACKER, new ComboSave(replace(living, comboId,
                    new ComboData(next, gameTime, resetDelay, remain))));
            if (next >= capped && cur.count() < capped && !onMaxStackActions.isEmpty()) {
                for (Action a : onMaxStackActions) a.execute(ctx);
            }
            if (bus != null) {
                bus.publish(new ComboEvent(living, skill, comboId, next, capped));
            }
        }

        /** 消耗连击层，返回实际消耗数量；消耗成功额外发射 COMBO_SPENT。 */
        public static int consume(LivingEntity living, String comboId, int amount,
                                  List<Action> onConsumeActions, ActionContext ctx,
                                  SkillEventBus bus, SkillInstance skill) {
            ComboData cur = get(living, comboId);
            int real = Math.min(Math.max(0, amount), cur.count());
            int left = cur.count() - real;
            living.setData(AttachmentRegistry.COMBO_TRACKER, new ComboSave(replace(living, comboId,
                    new ComboData(left, cur.lastBuildTick(), cur.resetDelayTicks(), cur.progress()))));
            if (real > 0) {
                Event.clear(living, EventName.COMBO_SPENT);
                GameEvent.emit(EventName.COMBO_SPENT, living, null, Set.of(GateTag.COMBO), null, real);
                if (!onConsumeActions.isEmpty()) {
                    for (Action a : onConsumeActions) a.execute(ctx);
                }
            }
            if (bus != null && real > 0) {
                bus.publish(new ComboEvent(living, skill, comboId, left, -1));
            }
            return real;
        }

        public static boolean isMax(LivingEntity living, String comboId, int maxStacks) {
            return get(living, comboId).count() >= Math.max(1, maxStacks);
        }

        /** 每 tick 检查自动重置。 */
        public static void tick(LivingEntity living, long gameTime) {
            ComboSave save = living.getData(AttachmentRegistry.COMBO_TRACKER);
            if (save.combos().isEmpty()) return;
            Map<String, ComboData> updated = new HashMap<>(save.combos());
            boolean changed = false;
            for (Map.Entry<String, ComboData> e : save.combos().entrySet()) {
                ComboData s = e.getValue();
                if (s.resetDelayTicks() > 0 && s.count() > 0 && gameTime - s.lastBuildTick() > s.resetDelayTicks()) {
                    updated.remove(e.getKey());
                    changed = true;
                }
            }
            if (changed) living.setData(AttachmentRegistry.COMBO_TRACKER, new ComboSave(updated));
        }

        private static Map<String, ComboData> replace(LivingEntity living, String comboId, ComboData data) {
            Map<String, ComboData> map = new HashMap<>(living.getData(AttachmentRegistry.COMBO_TRACKER).combos());
            map.put(comboId, data);
            return map;
        }
    }

    /** 读取连击槽（返回本类嵌套记录，供 .count() 等读点）。 */
    public static ComboData getCombo(LivingEntity living, String comboId) { return ComboSave.get(living, comboId); }
    /** 构建连击层。 */
    public static void buildCombo(LivingEntity living, String comboId, int maxStacks, int resetDelay,
                                  List<Action> onMaxStackActions, ActionContext ctx,
                                  SkillEventBus bus, SkillInstance skill, long gameTime) {
        ComboSave.build(living, comboId, maxStacks, resetDelay, onMaxStackActions, ctx, bus, skill, gameTime);
    }
    /** 消耗连击层，返回实际消耗量。 */
    public static int consumeCombo(LivingEntity living, String comboId, int amount,
                                   List<Action> onConsumeActions, ActionContext ctx,
                                   SkillEventBus bus, SkillInstance skill) {
        return ComboSave.consume(living, comboId, amount, onConsumeActions, ctx, bus, skill);
    }
    /** 是否达满层。 */
    public static boolean isComboMax(LivingEntity living, String comboId, int maxStacks) {
        return ComboSave.isMax(living, comboId, maxStacks);
    }
    /** 每 tick 检查连击自动重置。 */
    public static void tickCombo(LivingEntity living, long gameTime) { ComboSave.tick(living, gameTime); }

    // ════════════════════════════════════════════════════ 魔力 / 精魂 ════════════════════════════════════════════════════

    public static float getMaxMana(LivingEntity living) {
        return (float) living.getAttributeValue(AttributeRegistry.MAX_MANA);
    }

    public static float getMana(LivingEntity living) {
        return living.getData(AttachmentRegistry.MANA);
    }

    public static void setMana(LivingEntity living, float amount) {
        living.setData(AttachmentRegistry.MANA, amount);
    }

    public static float getSpirit(LivingEntity living) {
        return living.getData(AttachmentRegistry.SPIRIT);
    }

    public static void setSpirit(LivingEntity living, float amount) {
        living.setData(AttachmentRegistry.SPIRIT, amount);
    }

    public static float getMaxSpirit(LivingEntity living) {
        return (float) living.getAttributeValue(AttributeRegistry.MAX_SPIRIT);
    }

    /** 已保留的精魂总量（光环 reservation + 施放型保留之和）。 */
    public static float getReservedSpirit(LivingEntity living) {
        if (living == null) return 0f;
        if (living.level() != null && living.level().isClientSide()) {
            return living.getData(AttachmentRegistry.RESERVED_SPIRIT);
        }
        return ReservationManager.getReserved(living.getUUID(), GateTag.SPIRIT);
    }

    public static float hurtSpirit(LivingEntity living, float amount) {
        float spirit = getSpirit(living);
        if (spirit > amount) {
            setSpirit(living, spirit - amount);
            return 0;
        } else {
            setSpirit(living, 0);
            return amount - spirit;
        }
    }

    public static void healMana(LivingEntity living, float amount) {
        setMana(living, Math.min(getMana(living) + amount, getMaxMana(living)));
    }

    public static float hurtMana(LivingEntity living, float amount) {
        float mana = getMana(living);
        if (mana > amount) {
            setMana(living, mana - amount);
            return 0;
        } else {
            setMana(living, 0);
            return amount - mana;
        }
    }

    public static boolean isFullMana(LivingEntity living) {
        return getMana(living) == getMaxMana(living);
    }

    public static float getManaRegeneration(LivingEntity living) {
        double rate = DamageHelper.readInScopePublic(living, Set.of(GateTag.MANA, "regen"), AttributeRegistry.REGENERATION_RATE);
        double pct = DamageHelper.readInScopePublic(living, Set.of(GateTag.MANA, "regen"), AttributeRegistry.MAX_REGENERATION);
        double recovery = DamageHelper.readInScopePublic(living, Set.of(GateTag.MANA, "regen"), AttributeRegistry.RECOVERY_RATE);
        return (float) ((rate + getMaxMana(living) * pct) * 0.05F * recovery);
    }

    public static void tickManaRegeneration(LivingEntity living) {
        ResourceEventHelper.onHeal(living, GateTag.MANA, getManaRegeneration(living), "regen", null);
    }

    // ════════════════════════════════════════════════════ 能量护盾（Energy Shield） ════════════════════════════════════════════════════

    public static float getMaxEnergyShield(LivingEntity living) {
        return (float) living.getAttributeValue(AttributeRegistry.MAX_ENERGY_SHIELD);
    }

    public static float getEnergyShield(LivingEntity living) {
        return living.getData(AttachmentRegistry.ENERGY_SHIELD);
    }

    /** ES 有效上限：默认 MAX_ENERGY_SHIELD；「ES 不能回复到护甲值以上」时钳到护甲值。 */
    public static float effectiveMaxEnergyShield(LivingEntity living) {
        float max = getMaxEnergyShield(living);
        if (StatEngine.gateOf(living, GateTypeIds.ENERGY_SHIELD_CAPPED_BY_ARMOR, Set.of())) {
            max = Math.min(max, (float) living.getAttributeValue(Attributes.ARMOR));
        }
        return Math.max(0, max);
    }

    /** ES 自然充能是否被禁用。 */
    public static boolean isEnergyShieldRechargeDisabled(LivingEntity living) {
        return StatEngine.gateOf(living, "energy_shield_recharge_disabled", Set.of());
    }

    /** ES 是否不能从再生回复。 */
    public static boolean isEnergyShieldRegenDisabled(LivingEntity living) {
        return StatEngine.gateOf(living, GateTypeIds.ENERGY_SHIELD_REGEN_DISABLED, Set.of());
    }

    public static void setEnergyShield(LivingEntity living, float amount) {
        living.setData(AttachmentRegistry.ENERGY_SHIELD, Math.max(0, Math.min(amount, effectiveMaxEnergyShield(living))));
    }

    public static boolean isFullEnergyShield(LivingEntity living) {
        return getEnergyShield(living) >= effectiveMaxEnergyShield(living);
    }

    /** 充能启动基准延迟（tick）。 */
    public static final int BASE_ENERGY_SHIELD_RECHARGE_DELAY = 4 * 20;

    public static double getEnergyShieldRechargeDelay(LivingEntity living) {
        double speed = living.getAttributeValue(AttributeRegistry.ENERGY_RECHARGE);
        return BASE_ENERGY_SHIELD_RECHARGE_DELAY / Math.max(speed, 0.01);
    }

    public static float healEnergyShield(LivingEntity living, float amount) {
        float before = getEnergyShield(living);
        setEnergyShield(living, before + amount);
        return getEnergyShield(living) - before;
    }

    /** ES 吸收：优先扣 ES，返回 (吸收量, 溢出量)。 */
    public static Pair<Float, Float> absorbEnergyShield(LivingEntity living, float amount) {
        float es = getEnergyShield(living);
        if (es <= 0) return Pair.of(0F, amount);
        if (es >= amount) {
            setEnergyShield(living, es - amount);
            return Pair.of(amount, 0F);
        }
        setEnergyShield(living, 0);
        return Pair.of(es, amount - es);
    }

    public static float getEnergyShieldRegeneration(LivingEntity living) {
        double rate = DamageHelper.readInScopePublic(living, Set.of(GateTag.ENERGY_SHIELD, "regen"),
                AttributeRegistry.REGENERATION_RATE);
        double pct = DamageHelper.readInScopePublic(living, Set.of(GateTag.ENERGY_SHIELD, "regen"),
                AttributeRegistry.MAX_REGENERATION);
        double recovery = DamageHelper.readInScopePublic(living, Set.of(GateTag.ENERGY_SHIELD, "regen"),
                AttributeRegistry.RECOVERY_RATE);
        double baseMul = living.getAttributeValue(AttributeRegistry.BASE_ENERGY_SHIELD_RECHARGE);
        double curMul = living.getAttributeValue(AttributeRegistry.CURRENT_ENERGY_SHIELD_RECHARGE);
        return (float) ((rate + effectiveMaxEnergyShield(living) * pct) * 0.05F * recovery * baseMul * curMul);
    }

    public static void tickEnergyShieldRecharge(LivingEntity living) {
        ResourceEventHelper.onHeal(living, GateTag.ENERGY_SHIELD, getEnergyShieldRegeneration(living),
                GateTag.RECHARGE, null);
    }

    // ════════════════════════════════════════════════════ 符文护盾（Runic Ward） ════════════════════════════════════════════════════

    public static float getMaxRunicWard(LivingEntity living) {
        return (float) living.getAttributeValue(AttributeRegistry.MAX_RUNIC_WARD);
    }

    public static float getRunicWard(LivingEntity living) {
        return living.getData(AttachmentRegistry.RUNIC_WARD);
    }

    public static float effectiveMaxRunicWard(LivingEntity living) {
        return Math.max(0, getMaxRunicWard(living));
    }

    public static boolean isRunicWardRechargeDisabled(LivingEntity living) {
        return StatEngine.gateOf(living, GateTypeIds.RUNIC_WARD_RECHARGE_DISABLED, Set.of());
    }

    public static void setRunicWard(LivingEntity living, float amount) {
        living.setData(AttachmentRegistry.RUNIC_WARD, Math.max(0, Math.min(amount, effectiveMaxRunicWard(living))));
    }

    public static boolean isFullRunicWard(LivingEntity living) {
        return getRunicWard(living) >= effectiveMaxRunicWard(living);
    }

    public static final int BASE_RUNIC_WARD_RECHARGE_DELAY = 4 * 20;

    public static double getRunicWardRechargeDelay(LivingEntity living) {
        return BASE_RUNIC_WARD_RECHARGE_DELAY;
    }

    public static float healRunicWard(LivingEntity living, float amount) {
        float before = getRunicWard(living);
        setRunicWard(living, before + amount);
        return getRunicWard(living) - before;
    }

    public static Pair<Float, Float> absorbRunicWard(LivingEntity living, float amount) {
        float rw = getRunicWard(living);
        if (rw <= 0) return Pair.of(0F, amount);
        if (rw >= amount) {
            setRunicWard(living, rw - amount);
            return Pair.of(amount, 0F);
        }
        setRunicWard(living, 0);
        return Pair.of(rw, amount - rw);
    }

    public static float getRunicWardRegeneration(LivingEntity living) {
        double rate = DamageHelper.readInScopePublic(living, Set.of(GateTag.RUNIC_WARD, "regen"),
                AttributeRegistry.REGENERATION_RATE);
        double pct = DamageHelper.readInScopePublic(living, Set.of(GateTag.RUNIC_WARD, "regen"),
                AttributeRegistry.MAX_REGENERATION);
        double recovery = DamageHelper.readInScopePublic(living, Set.of(GateTag.RUNIC_WARD, "regen"),
                AttributeRegistry.RECOVERY_RATE);
        return (float) ((rate + effectiveMaxRunicWard(living) * pct) * 0.05F * recovery);
    }

    public static void tickRunicWardRecharge(LivingEntity living) {
        ResourceEventHelper.onHeal(living, GateTag.RUNIC_WARD, getRunicWardRegeneration(living),
                GateTag.RECHARGE, null);
    }

    // ════════════════════════════════════════════════════ 怒气（Rage） ════════════════════════════════════════════════════

    private static final int RAGE_LOSS_GRACE_TICKS = 80;
    private static final float RAGE_LOSS_PER_TICK = 1f / 4f;
    private static final int RAGE_RECENT_WINDOW_TICKS = 80;

    public static float getMaxRage(LivingEntity living) {
        return (float) Math.max(0, living.getAttributeValue(AttributeRegistry.MAX_RAGE));
    }

    public static float getRage(LivingEntity living) {
        return living.getData(AttachmentRegistry.RAGE);
    }

    public static void setRage(LivingEntity living, float amount) {
        living.setData(AttachmentRegistry.RAGE, Math.max(0, Math.min(amount, getMaxRage(living))));
    }

    public static boolean isFullRage(LivingEntity living) {
        return getRage(living) >= getMaxRage(living);
    }

    /** 消耗怒气（走统一资源枢纽）。 */
    public static void spendRage(LivingEntity living, float amount) {
        ResourceEventHelper.onSpend(living, GateTag.RAGE, amount, "skill", null);
    }

    /** 后端写入：直接加值并返回实际增加量。 */
    public static float healRageBackend(LivingEntity living, float amount) {
        float before = getRage(living);
        setRage(living, before + amount);
        float actual = getRage(living) - before;
        if (actual > 0) {
            int grace = RAGE_LOSS_GRACE_TICKS + (int) living.getAttributeValue(AttributeRegistry.RAGE_LOSS_GRACE);
            ExpressionContext.get(living).refreshTimer(EventName.RAGE_GAINED, Math.max(0, grace));
        }
        return actual;
    }

    /** 后端扣减：返回实际扣减量。 */
    public static float spendRageBackend(LivingEntity living, float amount) {
        float before = getRage(living);
        setRage(living, before - amount);
        return before - getRage(living);
    }

    /** 业务入口：获得怒气。 */
    public static void gainRage(LivingEntity living, float amount, String sourceTag) {
        ResourceEventHelper.onHeal(living, GateTag.RAGE, amount, sourceTag, null);
    }

    /** 业务入口：消耗怒气。 */
    public static float consumeRage(LivingEntity living, float amount, String sourceTag) {
        return ResourceEventHelper.onSpend(living, GateTag.RAGE, amount, sourceTag, null).actual();
    }

    /** 每 tick 固有流失。 */
    public static void tickRageInherentLoss(LivingEntity living) {
        if (getRage(living) <= 0) return;
        ExpressionContext ctx = ExpressionContext.get(living);
        if (ctx.isTimerActive(EventName.RAGE_GAINED)) return;
        float lossPerTick = RAGE_LOSS_PER_TICK
                * (float) living.getAttributeValue(AttributeRegistry.RAGE_LOSS_RATE);
        if (lossPerTick <= 0) return;
        ResourceEventHelper.onSpend(living, GateTag.RAGE, lossPerTick, "inherent_loss", null);
    }

    /** 每 tick 怒气回复（REGENERATE_RAGE_PER_SECOND_PER_RAGE_SPENT_RECENTLY）。 */
    public static void tickRageRegenFromSpent(LivingEntity living) {
        if (getRage(living) >= getMaxRage(living)) return;
        float mult = (float) living.getAttributeValue(AttributeRegistry.RAGE_REGEN_PER_RAGE_SPENT);
        if (mult <= 0) return;
        int spentRecently = Event.get(living).sum(EventName.RAGE_SPENT, RAGE_RECENT_WINDOW_TICKS);
        if (spentRecently <= 0) return;
        float regenPerTick = mult * spentRecently / 20f;
        if (regenPerTick <= 0) return;
        ResourceEventHelper.onHeal(living, GateTag.RAGE, regenPerTick, "regen_rage_spent", null);
    }

    /** 每 tick 怒气回复（REGENERATE_RAGE_PER_SECOND，盟友 presence 内）。 */
    public static void tickRageRegenPerSecond(LivingEntity living) {
        if (getRage(living) >= getMaxRage(living)) return;
        float regenPerSecond = (float) living.getAttributeValue(AttributeRegistry.RAGE_REGEN_PER_SECOND);
        if (regenPerSecond <= 0) return;
        float regenPerTick = regenPerSecond / 20f;
        if (regenPerTick <= 0) return;
        ResourceEventHelper.onHeal(living, GateTag.RAGE, regenPerTick, "regen_rage_per_second", null);
    }

    // ════════════════════════════════════════════════════ 眩晕条（Stun Meter） ════════════════════════════════════════════════════

    /** 眩晕门槛（绝对值，单位=击晕伤害）。 */
    public static float stunThreshold(LivingEntity living) {
        double esFrac = living.getAttributeValue(AttributeRegistry.STUN_THRESHOLD_FROM_ES);
        if (esFrac > 0) {
            double esBase = StatusEngine.ailmentThreshold(living, getEnergyShield(living) * esFrac);
            return (float) (esBase
                    * AilmentHelper.ailmentThresholdMul(living, StatusDefs.STUN)
                    + AilmentHelper.ailmentThresholdAdd(living, StatusDefs.STUN));
        }
        return (float) (StatusEngine.ailmentThreshold(living)
                * AilmentHelper.ailmentThresholdMul(living, StatusDefs.STUN)
                + AilmentHelper.ailmentThresholdAdd(living, StatusDefs.STUN));
    }

    public static float getStunBuildup(LivingEntity living) {
        return (float) StatusEngine.getBuildup(living, StatusDefs.STUN);
    }

    public static boolean isStunFull(LivingEntity living) {
        return getStunBuildup(living) >= 1.0f;
    }

    public static float healStunBackend(LivingEntity living, float amount) {
        float before = getStunBuildup(living);
        StatusEngine.addBuildup(living, StatusDefs.STUN, amount);
        return getStunBuildup(living) - before;
    }

    public static float spendStunBackend(LivingEntity living, float amount) {
        float before = getStunBuildup(living);
        if (before <= 0) return 0f;
        float after = Math.max(0f, before - amount);
        StatusEngine.resetBuildup(living, StatusDefs.STUN, after);
        return before - after;
    }

    /** 非伤害来源的重晕累积（当前用于格挡）。 */
    public static boolean accumulateStun(LivingEntity living, @Nullable LivingEntity attacker,
                                         float rawDamage, String sourceTag) {
        if (rawDamage <= 0) return false;
        if (AilmentHelper.avoidsPublic(living, living.getRandom(), StatusDefs.STUN.id)) return false;
        if (StatusEngine.has(living, StatusDefs.STUN_IMMUNE)) return false;
        if (StatusEngine.has(living, StatusDefs.STUN)) return false;
        double threshold = stunThreshold(living);
        if (threshold <= 0) return false;
        double gain = (rawDamage / threshold)
                * AilmentHelper.buildupMultiplier(attacker, StatusDefs.STUN);
        ResourceEventHelper.onHeal(living, GateTag.STUN, (float) gain, sourceTag, attacker);
        if (!isStunFull(living)) return false;
        double stunDuration = AilmentHelper.stunDurationMul(living);
        StatusEngine.apply(living, StatusDefs.STUN, attacker, 1.0, (int) (2 * 20 * stunDuration));
        AilmentHelper.fireStunEvents(attacker, living);
        StatusEngine.applyIfAbsent(living, StatusDefs.STUN_IMMUNE, attacker, 1.0, 30);
        StatusEngine.resetBuildup(living, StatusDefs.STUN, getStunBuildup(living) - 1.0);
        return true;
    }

    /** 每 tick 自然衰减。 */
    public static void tickStunDecay(LivingEntity living) {
        if (getStunBuildup(living) <= 0) return;
        if (StatusEngine.has(living, StatusDefs.STUN)) return;
        ResourceEventHelper.onSpend(living, GateTag.STUN, 0.01f, "decay", null);
    }

    // ════════════════════════════════════════════════════ 波动（Volatility） ════════════════════════════════════════════════════

    public static float getMaxVolatility(LivingEntity living) {
        return (float) Math.max(0, living.getAttributeValue(AttributeRegistry.MAX_VOLATILITY));
    }

    public static float getVolatility(LivingEntity living) {
        return living.getData(AttachmentRegistry.VOLATILITY);
    }

    public static void setVolatility(LivingEntity living, float amount) {
        living.setData(AttachmentRegistry.VOLATILITY,
                Math.max(0, Math.min(amount, getMaxVolatility(living))));
    }

    public static boolean isVolatilityFull(LivingEntity living) {
        return getVolatility(living) >= getMaxVolatility(living);
    }

    public static void gainVolatility(LivingEntity living, float amount) {
        float scaled = amount * (float) living.getAttributeValue(AttributeRegistry.VOLATILITY_GENERATION);
        ResourceEventHelper.onHeal(living, GateTag.VOLATILITY, scaled, null, null);
    }

    public static void spendVolatility(LivingEntity living, float amount) {
        ResourceEventHelper.onSpend(living, GateTag.VOLATILITY, amount, null, null);
    }

    // ════════════════════════════════════════════════════ 元素灌入（Infusion） ════════════════════════════════════════════════════

    /** 维金（Vaal）灌入持续时间：20 秒。 */
    public static final int VAAL_DURATION_TICKS = 400;

    private static StatusType infusionStatusOf(Element element) {
        return switch (element) {
            case FIRE -> StatusDefs.INFUSION_FIRE;
            case COLD -> StatusDefs.INFUSION_COLD;
            case LIGHTNING -> StatusDefs.INFUSION_LIGHTNING;
        };
    }

    public static float getMaxInfusion(LivingEntity living, Element element) {
        return (float) Math.max(0, living.getAttributeValue(AttributeRegistry.MAX_INFUSION));
    }

    public static float getMaxTotalInfusion(LivingEntity living) {
        return (float) Math.max(0, living.getAttributeValue(AttributeRegistry.MAX_TOTAL_ELEMENTAL_INFUSIONS));
    }

    public static float getInfusion(LivingEntity living, Element element) {
        return StatusEngine.stacks(living, infusionStatusOf(element));
    }

    public static float vaalRemaining(LivingEntity living) {
        return StatusEngine.stacks(living, StatusDefs.INFUSION_VAAL);
    }

    /** 全部元素灌入层数之和（含 VAAL）。 */
    public static float getInfusion(LivingEntity living) {
        return getInfusion(living, Element.FIRE) + getInfusion(living, Element.COLD)
                + getInfusion(living, Element.LIGHTNING) + vaalRemaining(living);
    }

    public static void setInfusion(LivingEntity living, Element element, float amount) {
        float perCap = getMaxInfusion(living, element);
        float totalCap = getMaxTotalInfusion(living);
        float others = totalExcludingInfusion(living, element);
        float allowed = Math.min(perCap, Math.max(0, totalCap - others));
        float v = Math.max(0, Math.min(amount, allowed));
        StatusEngine.setStacks(living, infusionStatusOf(element), (int) Math.round(v));
    }

    public static void gainInfusion(LivingEntity living, Element element, float amount) {
        setInfusion(living, element, getInfusion(living, element) + amount);
        double chance = Math.max(0.0, Math.min(1.0,
                living.getAttributeValue(AttributeRegistry.ADDITIONAL_INFUSION_CHANCE)));
        if (chance > 0 && living.getRandom().nextDouble() < chance) {
            setInfusion(living, element, getInfusion(living, element) + 1.0f);
        }
    }

    public static void spendInfusion(LivingEntity living, Element element, float amount) {
        setInfusion(living, element, getInfusion(living, element) - amount);
    }

    public static void setVaal(LivingEntity living, float amount) {
        float perCap = getMaxInfusion(living, Element.FIRE);
        float totalCap = getMaxTotalInfusion(living);
        float others = totalExcludingVaal(living);
        float allowed = Math.min(perCap, Math.max(0, totalCap - others));
        float v = Math.max(0, Math.min(amount, allowed));
        if (v <= 0) {
            StatusEngine.clear(living, StatusDefs.INFUSION_VAAL);
            return;
        }
        StatusEngine.clear(living, StatusDefs.INFUSION_VAAL);
        StatusEngine.apply(living, StatusDefs.INFUSION_VAAL, living, 1.0f, VAAL_DURATION_TICKS, (int) Math.round(v));
    }

    public static void gainVaal(LivingEntity living, float amount) {
        setVaal(living, vaalRemaining(living) + amount);
    }

    public static void spendVaal(LivingEntity living, float amount) {
        setVaal(living, vaalRemaining(living) - amount);
    }

    /** 消耗任意一种元素灌入：优先非维金元素，否则维金。返回实际被消耗种类（null = 无可消耗）。 */
    @Nullable
    public static InfusionKind consumeInfusionAny(LivingEntity living, float amount) {
        boolean skip = rollChance(living, AttributeRegistry.INFUSION_CONSUME_SKIP_CHANCE);
        for (Element e : new Element[]{Element.FIRE, Element.COLD, Element.LIGHTNING}) {
            if (getInfusion(living, e) > 0) {
                if (!skip) spendInfusion(living, e, amount);
                emitConsumeInfusion(living);
                return switch (e) {
                    case FIRE -> InfusionKind.FIRE;
                    case COLD -> InfusionKind.COLD;
                    case LIGHTNING -> InfusionKind.LIGHTNING;
                };
            }
        }
        if (vaalRemaining(living) > 0) {
            if (!skip) spendVaal(living, amount);
            emitConsumeInfusion(living);
            return InfusionKind.VAAL;
        }
        return null;
    }

    public static boolean hasAnyInfusion(LivingEntity living) {
        return getInfusion(living) > 0;
    }

    public static void grantInfusion(LivingEntity living, InfusionKind kind, float amount) {
        if (kind == InfusionKind.VAAL) gainVaal(living, amount);
        else gainInfusion(living, elementOfKind(kind), amount);
    }

    private static Element elementOfKind(InfusionKind kind) {
        return switch (kind) {
            case FIRE -> Element.FIRE;
            case COLD -> Element.COLD;
            case LIGHTNING -> Element.LIGHTNING;
            case VAAL -> Element.FIRE;
        };
    }

    private static float totalExcludingInfusion(LivingEntity living, Element exclude) {
        float sum = 0;
        for (Element e : Element.values()) {
            if (e != exclude) sum += getInfusion(living, e);
        }
        sum += vaalRemaining(living);
        return sum;
    }

    private static float totalExcludingVaal(LivingEntity living) {
        float sum = 0;
        for (Element e : Element.values()) sum += getInfusion(living, e);
        return sum;
    }

    private static boolean rollChance(LivingEntity living, net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attr) {
        double chance = Math.max(0.0, Math.min(1.0, living.getAttributeValue(attr)));
        return chance > 0 && living.getRandom().nextDouble() < chance;
    }

    private static void emitConsumeInfusion(LivingEntity living) {
        GameEvent.emit(EventName.CONSUME_INFUSION, living, (LivingEntity) null, Set.of(GateTag.INFUSION));
    }

    // ════════════════════════════════════════════════════ 充能球（Charges） ════════════════════════════════════════════════════

    private static final int NEVER_EXPIRE = Integer.MAX_VALUE;

    public static int getChargeCount(LivingEntity living, ChargeType chargeType) {
        return StatusEngine.stacks(living, chargeType.statusType());
    }

    public static int maxCharges(LivingEntity living, ChargeType type) {
        net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attr = switch (type) {
            case POWER -> AttributeRegistry.MAX_POWER_CHARGES;
            case FRENZY -> AttributeRegistry.MAX_FRENZY_CHARGES;
            case ENDURANCE -> AttributeRegistry.MAX_ENDURANCE_CHARGES;
        };
        return living.getAttributes().hasAttribute(attr)
                ? (int) Math.max(0, living.getAttributeValue(attr)) : type.defaultMaxCharges();
    }

    public static void generateCharge(LivingEntity living, ChargeType chargeType, int amount, int max, long expireTick,
                                       SkillEventBus bus, SkillInstance skill) {
        StatusType type = chargeType.statusType();
        int old = getChargeCount(living, chargeType);
        int cap = Math.max(0, maxCharges(living, chargeType));
        int add = Math.clamp(amount, 0, Math.max(0, cap - old));
        if (add <= 0) return;
        int duration = resolveChargeDuration(living, chargeType, expireTick);
        StatusEngine.apply(living, type, null, 0, duration, add);
        if (bus != null) {
            bus.publish(new ChargeChangeEvent(living, skill,
                    chargeType.getSerializedName(), old, old + add, "generate"));
        }
        Event.record(living, EventName.gainCharge(chargeType.getSerializedName()), add);
        Event.record(living, EventName.GAIN_CHARGE, add);
        fireChargeEvent(living, EventName.gainCharge(chargeType.getSerializedName()), EventName.GAIN_CHARGE);
    }

    /** 消耗充能球，返回实际消耗数量，发布 consume 事件。 */
    public static int consumeCharge(LivingEntity living, ChargeType chargeType, int amount,
                                    SkillEventBus bus, SkillInstance skill, Set<String> contextTags) {
        StatusType type = chargeType.statusType();
        int old = getChargeCount(living, chargeType);
        int real = Math.min(Math.max(0, amount), old);
        if (real <= 0) return 0;
        StatusInstance inst = StatusEngine.query(living, type);
        if (inst == null) return 0;
        boolean exempt = isChargeExempt(living, contextTags);
        if (!exempt && !rollChargeChance(living, AttributeRegistry.CHARGE_CONSUME_SKIP_CHANCE)) {
            inst.stacks -= real;
            if (inst.stacks <= 0) {
                StatusEngine.clear(living, type);
            } else {
                StatusStatBridge.remove(living, inst);
                StatusStatBridge.add(living, inst);
            }
        }
        if (bus != null) {
            bus.publish(new ChargeChangeEvent(living, skill,
                    chargeType.getSerializedName(), old, old - real, "consume"));
        }
        Event.record(living, EventName.consumeCharge(chargeType.getSerializedName()), real);
        Event.record(living, EventName.CONSUME_CHARGE, real);
        fireChargeEvent(living, EventName.consumeCharge(chargeType.getSerializedName()), EventName.CONSUME_CHARGE);
        return real;
    }

    public static void refreshCharge(LivingEntity living, ChargeType chargeType, long expireTick,
                                     SkillEventBus bus, SkillInstance skill) {
        StatusType type = chargeType.statusType();
        StatusInstance inst = StatusEngine.query(living, type);
        if (inst == null) return;
        inst.durationTicks = resolveChargeDuration(living, chargeType, expireTick);
        if (bus != null) {
            bus.publish(new ChargeChangeEvent(living, skill,
                    chargeType.getSerializedName(), inst.stacks, inst.stacks, "refresh"));
        }
    }

    public static double chargeDurationMul(LivingEntity living, ChargeType type) {
        if (living == null) return 1.0;
        try (AutoCloseable s = StatScopes.enterMerged(living,
                StatContext.of(Set.of(GateTag.CHARGE, type.getSerializedName()), null))) {
            return Math.max(0, living.getAttributeValue(AttributeRegistry.AILMENT_DURATION));
        } catch (Exception ignored) {
            return 1.0;
        }
    }

    private static int resolveChargeDuration(LivingEntity living, ChargeType chargeType, long expireTick) {
        if (expireTick <= 0) return NEVER_EXPIRE;
        long gameTime = living.level().getGameTime();
        int base = Math.max(1, (int) (expireTick - gameTime));
        return (int) Math.min(NEVER_EXPIRE, Math.max(1, base * chargeDurationMul(living, chargeType)));
    }

    private static boolean isChargeExempt(LivingEntity living, Set<String> contextTags) {
        if (living == null || contextTags == null || contextTags.isEmpty()) return false;
        return StatEngine.gateOf(living, "charge_cost_exempt", contextTags);
    }

    private static boolean rollChargeChance(LivingEntity living, net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attr) {
        double chance = Math.max(0.0, Math.min(1.0, living.getAttributeValue(attr)));
        return chance > 0 && living.getRandom().nextDouble() < chance;
    }

    /**
     * 发射充能球事件至 {@code StatEngine.fire}：{@code eventName} 为具体类型键（{@code gain_<type>} / {@code consume_<type>}），
     * {@code genericEventName} 为与之并行的通用键（{@link EventName#GAIN_CHARGE} / {@link EventName#CONSUME_CHARGE}）。
     * <p><b>方向由调用点显式传入，不再从键名前缀反推</b>：两个调用点（{@link #generateCharge} / {@link #consumeCharge}）
     * 本就各自已知自身方向，而此前 {@code startsWith("gain_")} 的 else 分支会把任何意外键名静默归为「消耗」。
     * 两键仍同时并入 fire 作用域，故数据侧以通用键做 {@code GateExpr} 门控的用法不受影响。
     */
    private static void fireChargeEvent(LivingEntity living, String eventName, String genericEventName) {
        if (living == null) return;
        StatEngine.fire(living, null,
                StatContext.of(Set.of(eventName, genericEventName), null, eventName, null), null);
    }

    // ════════════════════════════════════════════════════ 尸体（Corpse） ════════════════════════════════════════════════════

    public static final int MAX_CORPSES = 20;

    public static int getCorpse(LivingEntity living) {
        return (int) AdditionData.getOrDefault(living, AdditionKeys.CORPSE_COUNT, 0.0);
    }

    public static void addCorpse(LivingEntity living, int n) {
        if (n <= 0) return;
        int v = Math.min(MAX_CORPSES, getCorpse(living) + n);
        AdditionData.addOrReplaceData(living, AdditionKeys.CORPSE_COUNT, v);
    }

    public static boolean consumeCorpse(LivingEntity living, int n) {
        int cur = getCorpse(living);
        if (cur < n) return false;
        AdditionData.addOrReplaceData(living, AdditionKeys.CORPSE_COUNT, cur - n);
        return true;
    }

    public static boolean consumeAndEmpowerCorpse(LivingEntity living, int n) {
        if (!consumeCorpse(living, n)) return false;
        double retain = StatEngine.foldDelta(living, AttributeRegistry.CORPSE_RETAIN_CHANCE);
        if (living.getRandom().nextDouble() * 100.0 < retain) {
            addCorpse(living, n);
        }
        GameEvent.emit(EventName.CONSUME_CORPSE, living, (LivingEntity) null, Set.of());
        return true;
    }

    // ════════════════════════════════════════════════════ 灌入残迹（Remnant） ════════════════════════════════════════════════════

    private static final double REMNANT_PICKUP_RANGE = 2.5;
    private static final int REMNANT_TTL = 200;
    private static final int REMNANT_EMPOWER_DURATION = 100;
    private static final Map<ServerLevel, List<RemnantMarker>> REMNANT_STORE = new ConcurrentHashMap<>();

    /** 消耗灌入层数（技能成本路径）：扣减并（仅玩家）留下残迹。 */
    public static void onConsumeInfusion(LivingEntity living, float value) {
        InfusionKind consumed = consumeInfusionAny(living, value);
        if (living instanceof ServerPlayer sp) {
            dropRemnant(sp, consumed);
        }
    }

    /** 生成灌入残迹（数据层 / 事件回调通用入口）。 */
    public static void dropInfusionRemnant(LivingEntity living, InfusionKind kind) {
        if (living instanceof ServerPlayer sp) dropRemnant(sp, kind);
    }

    private static void dropRemnant(ServerPlayer player, @Nullable InfusionKind kind) {
        double extra = player.getAttributeValue(AttributeRegistry.REMNANT_EXTRA_CHANCE);
        double dbl = player.getAttributeValue(AttributeRegistry.REMNANT_DOUBLE_CHANCE);
        double mag = player.getAttributeValue(AttributeRegistry.REMNANT_MAGNITUDE) - 1.0;
        int count = player.getRandom().nextDouble() < extra ? 2 : 1;
        Vec3 pos = player.position();
        List<RemnantMarker> list = REMNANT_STORE.computeIfAbsent((ServerLevel) player.level(), k -> new ArrayList<>());
        InfusionKind k = remapRemnantElement(player, kind == null ? InfusionKind.VAAL : kind);
        for (int i = 0; i < count; i++) {
            list.add(new RemnantMarker(pos, mag, dbl, REMNANT_TTL, k));
        }
    }

    /** 每 tick 由 RemnantManager 对每个维度调用。 */
    public static void tickRemnants(ServerLevel level) {
        List<RemnantMarker> list = REMNANT_STORE.get(level);
        if (list == null || list.isEmpty()) return;
        Iterator<RemnantMarker> it = list.iterator();
        while (it.hasNext()) {
            RemnantMarker m = it.next();
            if (--m.ttl <= 0) {
                it.remove();
                continue;
            }
            double r2 = REMNANT_PICKUP_RANGE * REMNANT_PICKUP_RANGE;
            for (Player p : level.players()) {
                double mult = p.getAttributeValue(AttributeRegistry.REMNANTS_COLLECTED_FURTHER_AWAY);
                double pr2 = r2 * mult * mult;
                if (p.position().distanceToSqr(m.pos) <= pr2) {
                    collectRemnant(p, m);
                    it.remove();
                    break;
                }
            }
        }
    }

    private static void collectRemnant(Player player, RemnantMarker m) {
        GameEvent.emit(EventName.COLLECT_REMNANT, player, (LivingEntity) null, Set.of(GateTag.REMNANT));
        grantInfusion(player, m.kind, 1.0f);
        double mag = m.mag;
        if (player.getRandom().nextDouble() < m.dbl) {
            mag *= 2.0;
        }
        if (mag > 0) {
            StatusEngine.apply(player, StatusDefs.REMNANT_EMPOWERED, player, mag, REMNANT_EMPOWER_DURATION, 1);
        }
    }

    private static InfusionKind remapRemnantElement(Player player, InfusionKind kind) {
        if (kind == InfusionKind.VAAL) return kind;
        double toFire = DamageHelper.readInScopePublic(player, Set.of(GateTag.COLD, GateTag.FIRE), AttributeRegistry.REMNANT_CONVERSION);
        double toCold = DamageHelper.readInScopePublic(player, Set.of(GateTag.LIGHTNING, GateTag.COLD), AttributeRegistry.REMNANT_CONVERSION);
        double toLightning = DamageHelper.readInScopePublic(player, Set.of(GateTag.FIRE, GateTag.LIGHTNING), AttributeRegistry.REMNANT_CONVERSION);
        return switch (kind) {
            case COLD -> toFire >= 1.0 ? InfusionKind.FIRE : kind;
            case LIGHTNING -> toCold >= 1.0 ? InfusionKind.COLD : kind;
            case FIRE -> toLightning >= 1.0 ? InfusionKind.LIGHTNING : kind;
            default -> kind;
        };
    }

    /** 残迹标记：位置 + 创建者残迹幅度加成 + 双倍几率 + 剩余寿命 + 所代表的灌入种类。 */
    private static final class RemnantMarker {
        final Vec3 pos;
        final double mag;
        final double dbl;
        final InfusionKind kind;
        int ttl;

        RemnantMarker(Vec3 pos, double mag, double dbl, int ttl, InfusionKind kind) {
            this.pos = pos;
            this.mag = mag;
            this.dbl = dbl;
            this.kind = kind;
            this.ttl = ttl;
        }
    }
}
