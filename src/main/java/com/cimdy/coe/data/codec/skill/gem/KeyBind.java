package com.cimdy.coe.data.codec.skill.gem;

import com.cimdy.coe.data.codec.support.value.Value;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.data.codec.skill.core.SkillCaster;
import com.cimdy.coe.data.codec.skill.gem.SkillData;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.helper.skill.EquipmentSkillHelper;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * 按键绑定数据：将 10 个按键槽映射到 9 个技能装配槽。
 * 按键槽本身只存储装配槽索引（binding），实际的技能石数据从 SkillAssembly 中获取。
 *
 * <p>注意：技能的<b>冷却</b>已不再存于本结构，而是统一存放于任意实体通用的
 * {@link AttachmentRegistry#SKILL_COOLDOWN}（{@code Map<能力槽, 剩余tick>}），
 * 由 {@link SkillCaster} 读写并每 tick 推进。
 * 因此玩家与任意非玩家实体共用同一份冷却数据附件与同一套冷却运算——本结构仅承担
 * 「按键槽 -> 装配槽」的 UI 绑定职责，与冷却完全解耦。
 *
 * <p>按键槽对应的释放键是固定的 {@link com.cimdy.coe.client.InputState#SKILL_KEYS KeyMapping}，
 * 装配界面仅负责把技能石装配到 SkillAssembly，按键槽映射自动按照槽位顺序绑定。
 */
public class KeyBind {
    /** 根据按键索引获取对应的 EntitySkill（不含技能数据，仅键绑定元数据）。 */
    public static EntitySkill getEntitySkill(Entity entity, int key) {
        return entity.getData(AttachmentRegistry.SKILL_SAVE).getData(key);
    }

    /** 获取玩家的技能装配数据（仅手动装配，编辑/界面用）。 */
    public static SkillAssembly getAssembly(Entity entity) {
        return entity instanceof Player player && player.hasData(AttachmentRegistry.SKILL_ASSEMBLY)
                ? player.getData(AttachmentRegistry.SKILL_ASSEMBLY)
                : SkillAssembly.EMPTY;
    }

    /**
     * 获取「手动装配 + 装备授予」合并后的技能装配视图（施放/显示/自动绑定用）。
     * 手动槽位索引保持不变，装备授予技能填入手动表中空闲槽位；不修改任何附件。
     */
    public static SkillAssembly getMergedAssembly(Entity entity) {
        if (!(entity instanceof LivingEntity living)) return SkillAssembly.EMPTY;
        return EquipmentSkillHelper.mergedAssembly(living);
    }

    /**
     * 按槽位顺序自动把装配槽绑定到按键槽（key i → assembly i）。
     * 由 SkillScreen 在装配数据变化 / 界面打开时发包同步；服务端 {@code mergeSetup} 采用
     * 客户端传来的全量绑定（当前 UI 无手动改键入口，autoBind 即唯一绑定来源；若日后加入
     * 手动改键，此处需改为按槽增量合并、保留非自动绑定映射）。
     */
    public static SkillSave autoBind(SkillAssembly asm) {
        List<EntitySkill> list = new ArrayList<>(SkillSave.EMPTY);
        for (int i = 0; i < list.size(); i++) {
            int asmSlot = i;
            if (asmSlot < asm.slots().size() && !asm.get(asmSlot).isEmpty()) {
                list.set(i, new EntitySkill(ExpressionCodec.of(asmSlot)));
            } else {
                list.set(i, EntitySkill.EMPTY);
            }
        }
        return new SkillSave(list);
    }

    /**
     * 玩家按键槽数据：10 个槽位（5 直接键 + 5 辅助键），每个槽位记录其绑定的装配槽索引（-1 = 未绑定）。
     * 冷却已统一存于 {@link AttachmentRegistry#SKILL_COOLDOWN}，不在此处。
     */
    public record SkillSave(List<EntitySkill> save) {
        private static List<EntitySkill> createEmpty() {
            List<EntitySkill> list = new ArrayList<>(10);
            for (int i = 0; i < 10; i++) list.add(new EntitySkill(ExpressionCodec.of(-1)));
            return List.copyOf(list);
        }

        public static final List<EntitySkill> EMPTY = createEmpty();
        public static final SkillSave DEFAULT = new SkillSave(EMPTY);
        public static final Codec<List<EntitySkill>> LIST_CODEC =
                EntitySkill.CODEC.listOf().optionalFieldOf("bind", EMPTY).codec();

        public static final Codec<SkillSave> CODEC =
                LIST_CODEC.xmap(SkillSave::new, SkillSave::save);

        /** 将按键槽绑定到指定装配槽索引，装配新技能时冷却归零（冷却存于 SKILL_COOLDOWN，见下方说明）。 */
        public SkillSave set(int index, int assemblySlot) {
            List<EntitySkill> newSave = new ArrayList<>(this.save);
            while (newSave.size() <= index) newSave.add(new EntitySkill(ExpressionCodec.of(-1)));
            newSave.set(index, new EntitySkill(ExpressionCodec.of(assemblySlot)));
            return new SkillSave(newSave);
        }

        /** 解绑按键槽，设为未绑定状态。 */
        public SkillSave unbind(int index) {
            List<EntitySkill> newSave = new ArrayList<>(this.save);
            if (index < newSave.size()) newSave.set(index, new EntitySkill(ExpressionCodec.of(-1)));
            return new SkillSave(newSave);
        }

        public EntitySkill getData(int index) {
            return index >= 0 && index < this.save.size() ? this.save.get(index) : EntitySkill.EMPTY;
        }

        /**
         * 用客户端发来的按键绑定更新装配槽索引。
         * 冷却已不在本结构内（统一存于 {@code SKILL_COOLDOWN}），故此处仅合并装配槽绑定，
         * 调用方负责在绑定变化时清零对应能力槽的冷却。
         */
        public SkillSave mergeSetup(SkillSave incoming) {
            List<EntitySkill> merged = new ArrayList<>();
            for (int i = 0; i < EMPTY.size(); i++) {
                EntitySkill newEs = i < incoming.save.size() ? incoming.save.get(i) : new EntitySkill(ExpressionCodec.of(-1));
                merged.add(new EntitySkill(newEs.assemblySlot()));
            }
            return new SkillSave(merged);
        }

    }

    /** 单个按键槽：仅记录其绑定的装配槽索引（-1 = 未绑定）。冷却已统一移出，存于 SKILL_COOLDOWN。 */
    public record EntitySkill(ExpressionCodec.Value assemblySlot) {
        public static final EntitySkill EMPTY = new EntitySkill(ExpressionCodec.of(-1));

        public static final Codec<EntitySkill> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ExpressionCodec.VALUE_CODEC.optionalFieldOf("assembly_slot", ExpressionCodec.of(-1)).forGetter(EntitySkill::assemblySlot)
        ).apply(instance, EntitySkill::new));

        /** 该槽是否已绑定一个有效装配槽。冷却判定见 {@link SkillCaster}。 */
        public boolean hasBinding() {
            return this.assemblySlot().getInt() >= 0;
        }

        public boolean isEmpty() {
            return this.assemblySlot().getInt() < 0;
        }
    }
}
