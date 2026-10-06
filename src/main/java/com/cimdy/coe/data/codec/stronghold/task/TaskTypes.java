package com.cimdy.coe.data.codec.stronghold.task;

import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.data.codec.stronghold.task.Task.Type;
import com.cimdy.coe.data.codec.stronghold.task.impl.CarryTask;
import com.cimdy.coe.data.codec.stronghold.task.impl.CraftTask;
import com.cimdy.coe.data.codec.stronghold.task.impl.EatTask;
import com.cimdy.coe.data.codec.stronghold.task.impl.work.charge.*;
import com.cimdy.coe.data.codec.stronghold.task.impl.work.enchant.*;
import com.cimdy.coe.data.codec.stronghold.task.impl.work.harvest.*;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.RegistryBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * 工作任务类型注册表（NeoForge 注册表，与 {@code SiteTypes} 同构）。
 * <p>
 * 承载任务类型的编解码分发：{@link #REGISTRY} 供 {@code Task.CODEC} 调度，
 * {@link #STREAM_CODECS}/{@link #TYPE_TO_ID} 供 {@code Task.STREAM_CODEC} 编解码。
 * 新增工种只需在此处增加一行 {@link #register} 即可，无需改动 {@code Task}/{@code WorkTask} 的序列化逻辑。
 */
public class TaskTypes {
    public static final Identifier IDENTIFIER = Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "task");
    static final Map<Identifier, StreamCodec<RegistryFriendlyByteBuf, ? extends Task>> STREAM_CODECS = new HashMap<>();
    static final Map<Type<?>, Identifier> TYPE_TO_ID = new HashMap<>();

    public static final ResourceKey<Registry<Type<?>>> KEY = ResourceKey.createRegistryKey(IDENTIFIER);
    public static final Registry<Type<?>> REGISTRY = new RegistryBuilder<>(KEY).sync(true).defaultKey(IDENTIFIER).maxId(256).create();

    public static final Type<ConvertTask> CONVERT = register("convert", ConvertTask.CODEC, ConvertTask.STREAM_CODEC);
    public static final Type<EnderChargeTask> ENDER_CHARGE = register("ender_charge", EnderChargeTask.CODEC, EnderChargeTask.STREAM_CODEC);
    public static final Type<FarmerTask> FARMER = register("farmer", FarmerTask.CODEC, FarmerTask.STREAM_CODEC);
    public static final Type<FEChargeTask> FE_CHARGE = register("fe_charge", FEChargeTask.CODEC, FEChargeTask.STREAM_CODEC);
    public static final Type<ForestryTask> LOGGER = register("logger", ForestryTask.CODEC, ForestryTask.STREAM_CODEC);
    public static final Type<HeaterTask> HEATER = register("heater", HeaterTask.CODEC, HeaterTask.STREAM_CODEC);
    public static final Type<HuntTask> HUNT = register("hunt", HuntTask.CODEC, HuntTask.STREAM_CODEC);
    public static final Type<MilkTask> MILK = register("milk", MilkTask.CODEC, MilkTask.STREAM_CODEC);
    public static final Type<ShearTask> SHEAR = register("shear", ShearTask.CODEC, ShearTask.STREAM_CODEC);
    public static final Type<MiningTask> MINING = register("mining", MiningTask.CODEC, MiningTask.STREAM_CODEC);
    public static final Type<FisherTask> FISHER = register("fisher", FisherTask.CODEC, FisherTask.STREAM_CODEC);
    public static final Type<EatTask> EAT = register("eat", EatTask.CODEC, EatTask.STREAM_CODEC);
    public static final Type<CraftTask> CRAFT = register("craft", CraftTask.CODEC, CraftTask.STREAM_CODEC);
    public static final Type<CarryTask> CARRY = register("carry", CarryTask.CODEC, CarryTask.STREAM_CODEC);
    public static final Type<EnchantTask> ENCHANT = register("enchant", EnchantTask.CODEC, EnchantTask.STREAM_CODEC);

    public static <T extends Task> Type<T> register(String name, Codec<T> codec,
                                                   StreamCodec<RegistryFriendlyByteBuf, T> streamCodec) {
        Identifier id = Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "task_" + name);
        Type<T> type = Registry.register(REGISTRY, id, new Type<>(codec));
        STREAM_CODECS.put(id, streamCodec);
        TYPE_TO_ID.put(type, id);
        return type;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Task> StreamCodec<RegistryFriendlyByteBuf, T> getStreamCodec(Type<T> type) {
        Identifier id = REGISTRY.getKey(type);
        return (StreamCodec<RegistryFriendlyByteBuf, T>) STREAM_CODECS.get(id);
    }
}
