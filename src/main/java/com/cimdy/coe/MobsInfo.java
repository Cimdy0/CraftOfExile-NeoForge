package com.cimdy.coe;

import com.cimdy.coe.CoeConfig;
import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.data.codec.support.meta.Camp;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.registry.AttributeRegistry;
import com.cimdy.coe.data.codec.entity.LivingMod;
import com.cimdy.coe.data.codec.stronghold.entity.StrongholdEntity;
import com.cimdy.coe.data.codec.support.rarity.Rarity;
import com.cimdy.coe.data.codec.support.stat.engine.StatContext;
import com.cimdy.coe.data.codec.support.stat.engine.StatScopes;
import com.cimdy.coe.data.codec.support.stat.tag.GateTag;
import com.cimdy.coe.data.helper.hub.ResourceRegistry;
import com.cimdy.coe.entity.work.WorkProfile;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import java.util.Set;

public enum MobsInfo implements IServerDataProvider<EntityAccessor> {
    INSTANCE;

    public static final Identifier MOBS_INFO = Identifier.fromNamespaceAndPath(
            CraftOfExile.MODID, "mobs_info");

    @Override
    public void appendServerData(CompoundTag data, EntityAccessor accessor) {
        LivingEntity living = (LivingEntity) accessor.getEntity();
        if (ResourceRegistry.max(living, "energy_shield") > 0) {
            data.putDouble("MaxEnergy", ResourceRegistry.max(living, "energy_shield"));
            data.putDouble("Energy", ResourceRegistry.current(living, "energy_shield"));
        }
        if (living.hasData(AttachmentRegistry.LEVEL)) {
            data.putDouble("Level", living.getData(AttachmentRegistry.LEVEL));
        }
        if (living.hasData(AttachmentRegistry.RARITY)) {
            data.store("Rarity", Rarity.CODEC, living.getData(AttachmentRegistry.RARITY));
        }
        if (living.hasData(AttachmentRegistry.ENTITY_MOD)) {
            data.store("Mod", LivingMod.ModSet.CODEC, living.getData(AttachmentRegistry.ENTITY_MOD));
        }
        if (living.hasData(AttachmentRegistry.OWNER_UUID)) {
            Entity owner = living.level().getEntity(living.getData(AttachmentRegistry.OWNER_UUID));
            if (owner != null) {
                data.putString("Owner", owner.getScoreboardName());
            }
        } else if (living.hasData(AttachmentRegistry.ENTITY_DATA)) {
            Entity owner = living.level().getEntity(living.getData(AttachmentRegistry.ENTITY_DATA).owner());
            if (owner != null) {
                data.putString("Owner", owner.getScoreboardName());
            }
        }
        if (living.hasData(AttachmentRegistry.CAMP) && living instanceof Mob mob) {
            data.putString("Camp", living.getData(AttachmentRegistry.CAMP).getSerializedName() +
                    " " + (mob.getTarget() == null ? "null" : mob.getTarget().getScoreboardName()));
        }
    }

    private static int getResistance(double value) {
        return value >= CoeConfig.renderResistanceMax ?
                1 : value <= CoeConfig.renderResistanceMin ?
                -1 : 0;
    }

    @Override
    public Identifier getUid() {
        return MOBS_INFO;
    }

    public static class Client implements IEntityComponentProvider {
        public static final MobsInfo.Client INSTANCE = new MobsInfo.Client();

        public Client() {
        }

        @Override
        public void appendTooltip(
                ITooltip tooltip,
                EntityAccessor accessor,
                IPluginConfig config
        ) {
            if (accessor.getEntity() instanceof LivingEntity living) {
                if (accessor.getServerData().contains("Level")) {
                    if (accessor.getServerData().contains("MaxEnergy")) {
                        tooltip.add(Component.translatable("entity.coe.energy").append(": ")
                                .append(String.valueOf(accessor.getServerData().getDouble("Energy").get().intValue())).append("/")
                                .append(String.valueOf(accessor.getServerData().getDouble("MaxEnergy").get().intValue())));
                    }

                    tooltip.add(Component.translatable("stat.level").append(": ")
                            .append(String.valueOf(accessor.getServerData().getDouble("Level").get().intValue()))
                            .append("   ")
                            .append(accessor.getServerData().contains("Rarity") ?
                                    Component.translatable("rarity.coe." +
                                                    accessor.getServerData().read("Rarity", Rarity.CODEC).get().getSerializedName())
                                            .withStyle(accessor.getServerData().read("Rarity", Rarity.CODEC).get().getColor())
                                    : Component.empty()));

                    // 归一：抗性已收口为 RESISTANCES 属性 + 元素标签作用域（fire/cold/lightning/chaos），
                    // 不再读 FIRE/COLD/LIGHT/CHAOS_RESISTANCE 等待删旧属性。每个元素进入各自作用域读当前抗性。
                    final String[] elemTags = {GateTag.FIRE, GateTag.COLD, GateTag.LIGHTNING, GateTag.CHAOS};
                    final String[] keys = {"stat.fire", "stat.cold", "stat.light", "stat.chaos"};
                    final int[] resVals = {0, 0, 0, 0};
                    MutableComponent resist = Component.empty();
                    MutableComponent weak = Component.empty();
                    for (int i = 0; i < elemTags.length; i++) {
                        try (var scope = StatScopes.enter(living, StatContext.of(Set.of(elemTags[i]), null))) {
                            double res = living.getAttributeValue(AttributeRegistry.RESISTANCES) * 100;
                            int r = getResistance(res);
                            resVals[i] = r;
                            if (r > 0) {
                                if (!resist.equals(Component.empty())) resist.append(", ");
                                resist.append(Component.translatable(keys[i]));
                            } else if (r < 0) {
                                if (!weak.equals(Component.empty())) weak.append(", ");
                                weak.append(Component.translatable(keys[i]));
                            }
                        } catch (Exception ignored) {
                        }
                    }
                    boolean allRes = resVals[0] > 0 && resVals[1] > 0 && resVals[2] > 0;
                    if (!resist.equals(Component.empty())) {
                        if (allRes) resist = Component.translatable("entity.coe.element").append(" ").append(resist);
                        tooltip.add(resist.append(Component.translatable("stat.resistance")).withStyle(ChatFormatting.GREEN));
                    }
                    if (!weak.equals(Component.empty())) {
                        tooltip.add(weak.append(Component.translatable("item.mod.coe.entity.weakness")).withStyle(ChatFormatting.RED));
                    }

                    accessor.getServerData().read("Mod", LivingMod.ModSet.CODEC).ifPresent(m ->
                            m.names().forEach(name -> {
                                if (!(name.equals("fire_resistant") && resVals[0] > 0 || name.equals("cold_resistant") && resVals[1] > 0 ||
                                        name.equals("light_resistant") && resVals[2] > 0 || name.equals("chaos_resistant") && resVals[3] > 0)) {
                                    tooltip.add(Component.translatable("item.mod.coe.entity." + name));
                                }
                            }));

                    accessor.getServerData().getString("Owner").ifPresent(uuid ->
                            tooltip.add(Component.translatable("entity.coe.owner").append(": ")
                                    .append(uuid)));

                    accessor.getServerData().getString("Camp").ifPresent(camp ->
                            tooltip.add(Component.translatable("entity.coe.camp").append(": ")
                                    .append(camp)));
                }

                if (living.hasData(AttachmentRegistry.ENTITY_DATA)) {
                    StrongholdEntity data = living.getData(AttachmentRegistry.ENTITY_DATA);
                    tooltip.add(Component.literal("hunger: " + data.hunger()));
                    data.stacks().forEach(s -> tooltip.add(s.getDisplayName().copy().append(" x " + s.count())));
                }

                // 工作档案（随实体同步）：显示基础工作速度，若发生个体突变则标注。
                if (living.hasData(AttachmentRegistry.WORK_PROFILE)) {
                    WorkProfile wp = living.getData(AttachmentRegistry.WORK_PROFILE);
                    if (wp.initialized()) {
                        MutableComponent line = Component.translatable("entity.coe.work_speed")
                                .append(String.format("%.2f", wp.baseWorkSpeed()));
                        if (!wp.mutated().isEmpty()) line.append(Component.translatable("entity.coe.mutated"));
                        tooltip.add(line);
                    }
                }
            }
        }

        @Override
        public Identifier getUid() {
            return MOBS_INFO;
        }
    }
}