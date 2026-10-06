package com.cimdy.coe.data.codec.skill.gem;

import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.registry.DataComponentRegistry;
import com.cimdy.coe.data.codec.skill.core.Skill;
import com.cimdy.coe.data.codec.skill.core.SkillEntry;
import com.cimdy.coe.data.codec.skill.support.SkillLinkResolver;
import com.cimdy.coe.data.codec.skill.support.SupportGem;
import com.cimdy.coe.data.codec.skill.support.SupportProfile;
import com.cimdy.coe.data.codec.support.value.ModifierValue;
import com.cimdy.coe.data.codec.support.require.Require;
import com.cimdy.coe.data.codec.support.meta.Status;
import com.cimdy.coe.event.data.LoadEvent;
import com.cimdy.coe.registry.item.ItemRegistry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Objects;

/**
 * 技能数据 — 技能石的完整存储数据。
 * 不再持有 GemType（技能/辅助/精魂的区分由数据载体类型决定：
 * 技能石使用 SkillData，辅助宝石使用 SupportData）。
 * supports 存储链接的辅助宝石数据（含 ID 与等级）。
 */
public record SkillData(
        Identifier id,
        Status status,
        ModifierValue.Int level,
        ModifierValue.Int quality,
        ModifierValue.Int sockets,
        List<SupportData> supports,
        boolean spirit
) {
    public static final SkillData EMPTY =
            SkillData.builder(CraftOfExile.EMPTY_LOCATION).build();

    public static Codec<SkillData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("id").forGetter(SkillData::id),
            Status.CODEC.optionalFieldOf("status", Status.EMPTY).forGetter(SkillData::status),
            ModifierValue.Int.CODEC.optionalFieldOf("level",
                    ModifierValue.Int.create(1)).forGetter(SkillData::level),
            ModifierValue.Int.CODEC.optionalFieldOf("quality",
                    ModifierValue.Int.create(0)).forGetter(SkillData::quality),
            ModifierValue.Int.CODEC.optionalFieldOf("sockets",
                    ModifierValue.Int.create(2)).forGetter(SkillData::sockets),
            SupportData.CODEC.listOf().optionalFieldOf("supports", List.of()).forGetter(SkillData::supports),
            Codec.BOOL.optionalFieldOf("spirit", false).forGetter(SkillData::spirit)
    ).apply(instance, SkillData::new));

    /**
     * 解析为最终可用于施放的 Skill，默认使用静态链接组。
     *
     * <p>先取本技能 id 对应的 {@link SkillEntry}（可能是内联完整定义，也可能是
     * {@link SkillRead} 引用形态），经 {@code resolve()} 得到完整技能；再套用辅助宝石。
     */
    public Skill getSkill() {
        SkillEntry base = skillEntry();
        if (base == null) return null;
        List<SupportGem> resolvedSupports = resolveSupports();
        return resolvedSupports.isEmpty() ? base.resolve() : applySupports(base.resolve(), resolvedSupports);
    }

    /**
     * 本技能 id 对应的技能条目：技能宝石表优先，其次精魂宝石表。
     * 返回的可能是引用形态（{@link SkillRead}）——消费方须经 {@code resolve()} 取完整技能。
     */
    public SkillEntry skillEntry() {
        SkillEntry base = LoadEvent.getSkill(id);
        return base != null ? base : LoadEvent.getSpirit(id);
    }

    /**
     * 根据 supports 中的 SupportData 列表解析出 SupportGem 对象列表。
     */
    public List<SupportGem> resolveSupports() {
        return supports.stream()
                .filter(sd -> !sd.isEmpty())
                .map(sd -> LoadEvent.getSupportGem(sd.id()))
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * 获取所有已装配辅助宝石的等级列表（与 resolveSupports 顺序一致）。
     */
    public List<Integer> getSupportLevels() {
        return supports.stream()
                .filter(sd -> !sd.isEmpty())
                .map(SupportData::getLevel)
                .toList();
    }

    /**
     * 将辅助宝石效果应用到基础技能上，生成最终技能。
     * 包含：消耗倍率、速度倍率、附加动作注入。
     */
    private Skill applySupports(Skill base, List<SupportGem> supportGems) {
        return SkillLinkResolver.applyAll(base, supportGems);
    }

    /**
     * 根据「技能自身 modifiers」+「supports 列表」生成运行时修正聚合体。
     * 技能本体从物品技能表查出，其 modifiers 与辅助宝石的 effects 共用同一套形状，
     * 因此二者被统一合并进 SupportProfile。
     */
    public SupportProfile buildSupportProfile() {
        SkillEntry base = skillEntry();
        if (base == null) return SupportProfile.EMPTY;
        return SupportProfile.from(base.resolve(), filteredSupports(), getSupportLevels());
    }

    /**
     * 经装配期约束求解器（{@link SkillLinkResolver#filterCompatible}）过滤后的辅助宝石列表。
     * 使运行期与装配期一致：被拒绝（类型不兼容 / 资源互斥）的宝石既不施加装配期加成，
     * 也不进入运行时 {@link SupportProfile} 的 effects 聚合，修复原运行期/装配期不一致。
     */
    private List<SupportGem> filteredSupports() {
        SkillEntry base = skillEntry();
        if (base == null) return List.of();
        return SkillLinkResolver.filterCompatible(base.resolve(), resolveSupports());
    }

    public Identifier getIcon() {
        return Identifier.fromNamespaceAndPath(id.getNamespace(),
                "textures/gui/gem/" + getNameString() + ".png");
    }

    public Identifier getItemModel() {
        return stripColor(id);
    }

    /**
     * 去掉宝石 ID 路径中的颜色段（首段 red/green/blue），返回末段 Identifier。
     * 例如 {@code coe:red/culling_slam -> coe:culling_slam}，用于生成不含颜色子目录的 ITEM_MODEL。
     */
    public static Identifier stripColor(Identifier id) {
        String path = id.getPath();
        int slash = path.lastIndexOf('/');
        String stripped = slash >= 0 ? path.substring(slash + 1) : path;
        return Identifier.fromNamespaceAndPath(id.getNamespace(), stripped);
    }

    /**
     * 技能的统一名称（去掉颜色段的末段），是技能名 / 模型名 / 图标名 / 技能石贴图名的唯一映射来源。
     * 例如 {@code coe:red/rolling_slam -> rolling_slam}。
     */
    public String getNameString() {
        return stripColor(id).getPath();
    }

    public Component getName() {
        return Component.translatable((spirit ? "gem.spirit.name." : "gem.skill.name.") + id.getNamespace() + "." + getNameString());
    }

    public boolean isEmpty() {
        return id.equals(CraftOfExile.EMPTY_LOCATION);
    }

    /**
     * 将本技能数据还原为对应的宝石物品（技能石 / 精魂石）。
     * 用于从装配槽卸下技能时把物品放回物品栏。
     */
    public ItemStack toItemStack() {
        Item gemItem = spirit ? ItemRegistry.SPIRIT_GEM.get() : ItemRegistry.SKILL_GEM.get();
        ItemStack stack = new ItemStack(gemItem);
        stack.set(DataComponents.ITEM_MODEL, getItemModel());
        stack.set(DataComponents.ITEM_NAME, getName());
        stack.set(DataComponentRegistry.SKILL_DATA, this);
        return stack;
    }

    /** 是否为精魂宝石（光环类，需手动开启，不绑定按键）。 */
    public boolean isSpirit() {
        return spirit;
    }

    public int getSkillLevel() {
        return level.get();
    }

    /** 等效技能等级：基础等级 + 辅助宝石等级加成。 */
    public int getEffectiveSkillLevel() {
        return getSkillLevel() + SkillLinkResolver.getTotalLevelBonus(filteredSupports(), getSupportLevels());
    }

    /**
     * 等效品质：基础品质 + 辅助宝石品质加成（如 Enhance）。
     * <p>品质加成在装配期由 {@link SkillLinkResolver#getTotalQualityBonus} 汇总，
     * 并入等效品质后，技能所有随品质成长的参数（额外投射物、额外范围等）同步放大。
     */
    public int getEffectiveQuality() {
        return quality.get() + SkillLinkResolver.getTotalQualityBonus(filteredSupports());
    }

    /** 辅助技能槽数量：默认 2，上限 5。 */
    public static final int MAX_SOCKETS = 5;

    public static int clampSockets(int sockets) {
        return Math.clamp(sockets, 0, MAX_SOCKETS);
    }

    /** 返回该技能石当前可用的辅助技能槽数量（已钳制到 [0, 5]）。 */
    public int getSockets() {
        return clampSockets(sockets.get());
    }

    public int getChangeLevel() {
        return level.change();
    }

    public boolean hasExtraLevel() {
        return level.change() > 0;
    }

    public static Builder builder(Identifier id) {
        return new Builder(id);
    }

    public static class Builder {
        private final Identifier id;
        private Status status = Status.EMPTY;
        private ModifierValue.Int level = ModifierValue.Int.create(1);
        private ModifierValue.Int quality = ModifierValue.Int.create(0);
        private ModifierValue.Int sockets = ModifierValue.Int.create(2);
        private List<SupportData> supports = List.of();
        private boolean spirit = false;

        private Builder(Identifier id) {
            this.id = id;
        }

        public Builder status(Status status) {
            this.status = status;
            return this;
        }

        public Builder setStatus(String key, boolean value) {
            this.status = this.status.setStatus(key, value);
            return this;
        }

        public Builder level(ModifierValue.Int level) {
            this.level = level;
            return this;
        }

        public Builder setOriginalLevel(int originalLevel) {
            this.level = this.level.setOriginal(originalLevel);
            return this;
        }

        public Builder quality(ModifierValue.Int quality) {
            this.quality = quality;
            return this;
        }

        public Builder setOriginalQuality(int originalQuality) {
            this.quality = this.quality.setOriginal(originalQuality);
            return this;
        }

        public Builder addQuality(int addQuality) {
            this.quality = this.quality.addOriginal(addQuality);
            return this;
        }

        public Builder sockets(ModifierValue.Int sockets) {
            this.sockets = ModifierValue.Int.create(clampSockets(sockets.original()));
            return this;
        }

        public Builder supports(List<SupportData> supports) {
            this.supports = supports;
            return this;
        }

        public Builder spirit(boolean spirit) {
            this.spirit = spirit;
            return this;
        }

        public SkillData build() {
            return new SkillData(id, status, level, quality, sockets, supports, spirit);
        }
    }
}
