package com.cimdy.coe.entity.work;

import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.data.codec.stronghold.task.work.WorkType;
import com.cimdy.coe.data.codec.stronghold.task.work.WorkTypeRegistry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 实体的<b>工作档案</b>（NeoForge 附件，随实体同步到客户端用于显示）。
 * <p>
 * 承载个体工作属性：
 * <ul>
 *   <li>{@code baseWorkSpeed} —— 实体自身的工作速度数值（不同实体类型经 {@link WorkBias} 而不同）；</li>
 *   <li>{@code aptitude} —— 各工种的工作适应度等级（相同工种下，适应度越高工作越快）；</li>
 *   <li>{@code mutated} —— 发生个体突变的工种列表（极小概率，用于点睛式差异）。</li>
 * </ul>
 * <p>
 * 档案在首次访问时惰性生成（{@link #get}）：由实体类型基础偏置 + 村民职业挂钩 + 极小概率突变 三重叠加，
 * 生成后写入附件并随实体同步。Stat 词缀系统不直接改此附件，而是通过 {@link WorkSpeed} 读取的属性
 * （{@code work_speed} / {@code work_aptitude}）在求有效速度时叠加（见 {@link WorkSpeed}）。
 */
public record WorkProfile(double baseWorkSpeed, Map<String, Double> aptitude, List<String> mutated, boolean initialized) {
    public static final WorkProfile EMPTY = new WorkProfile(1.0, Map.of(), List.of(), false);

    public static final Codec<WorkProfile> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.DOUBLE.optionalFieldOf("base_speed", 1.0).forGetter(WorkProfile::baseWorkSpeed),
            Codec.unboundedMap(Codec.STRING, Codec.DOUBLE).optionalFieldOf("aptitude", Map.of())
                    .forGetter(WorkProfile::aptitude),
            Codec.STRING.listOf().optionalFieldOf("mutated", List.of()).forGetter(WorkProfile::mutated),
            Codec.BOOL.optionalFieldOf("init", false).forGetter(WorkProfile::initialized)
    ).apply(i, WorkProfile::new));

    /** 个体突变概率（0.5%）：发生则随机一个工种获得额外大幅适应度。 */
    public static final double MUTATION_CHANCE = 0.005;
    /** 突变带来的适应度增量。 */
    public static final double MUTATION_BONUS = 2.0;
    /** 村民职业对其对应工种附加的适应度增量。 */
    public static final double PROFESSION_BONUS = 1.5;

    /** 取某工种的适应度等级（无记录返回 0）。 */
    public double aptitude(String workType) {
        return aptitude.getOrDefault(workType, 0.0);
    }

    /** 惰性获取（首次访问时生成并持久化）。服务端生成后写入附件；客户端每次实时计算（仅用于显示，近似）。 */
    public static WorkProfile get(LivingEntity entity) {
        WorkProfile p = entity.getData(AttachmentRegistry.WORK_PROFILE);
        if (p == null || !p.initialized()) {
            p = compute(entity);
            if (entity.level() instanceof ServerLevel && p.initialized()) {
                entity.setData(AttachmentRegistry.WORK_PROFILE, p);
            }
        }
        return p;
    }

    /** 依据实体类型基础偏置、村民职业、个体突变生成档案。 */
    private static WorkProfile compute(LivingEntity entity) {
        WorkBias bias = WorkBiasRegistry.forType(entity.getType());
        double base = bias.baseSpeed();
        Map<String, Double> apt = new LinkedHashMap<>(bias.aptitude());

        // 村民职业挂钩：对应工种适应度提升（NBT 差异生效的范例）。
        if (entity instanceof Villager villager) {
            String wt = professionWorkType(villager.getVillagerData().profession());
            if (wt != null) apt.merge(wt, PROFESSION_BONUS, Double::sum);
        }

        // 个体突变：极小概率使某一工种适应度跃升。
        List<String> mutated = List.of();
        RandomSource r = entity.getRandom();
        if (r.nextDouble() < MUTATION_CHANCE) {
            List<String> types = WorkTypeRegistry.workTypes().stream().map(WorkType::name).toList();
            if (!types.isEmpty()) {
                String pick = types.get(r.nextInt(types.size()));
                apt.merge(pick, MUTATION_BONUS, Double::sum);
                mutated = List.of(pick);
            }
        }

        return new WorkProfile(base, Map.copyOf(apt), mutated, true);
    }

    /** 村民职业 → 工种名（与 {@code WorkTypeRegistry} 的 name() 保持一致）。无对应返回 null。
     *  MC 26.2 起 {@code VillagerProfession} 为注册表对象（常量即 {@code ResourceKey}），
     *  故先经 {@link Holder#unwrapKey()} 取出键，再按引用比较。 */
    private static String professionWorkType(Holder<VillagerProfession> profession) {
        ResourceKey<VillagerProfession> key = profession.unwrapKey().orElse(null);
        if (key == null) return null;
        if (key == VillagerProfession.FARMER || key == VillagerProfession.NITWIT) return "agriculture";
        if (key == VillagerProfession.FISHERMAN) return "fisher";
        if (key == VillagerProfession.SHEPHERD) return "shear";
        if (key == VillagerProfession.BUTCHER) return "hunt";
        return null;
    }
}
