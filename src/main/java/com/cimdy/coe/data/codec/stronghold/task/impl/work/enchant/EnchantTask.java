package com.cimdy.coe.data.codec.stronghold.task.impl.work.enchant;

import com.cimdy.coe.data.codec.stronghold.task.helper.dispatch.TaskGenerator;
import com.cimdy.coe.data.codec.stronghold.task.Task;
import com.cimdy.coe.data.codec.stronghold.task.TaskTypes;
import com.cimdy.coe.data.codec.stronghold.task.impl.WorkTask;
import com.cimdy.coe.data.codec.stronghold.task.site.Site;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * 附魔应用任务（合成链中的「附魔」节点，与 {@link ConvertTask} 同构但支持多输入与末影能量消耗）。
 * <p>
 * 由 {@code TaskGenerator.generateEnchantTasks} 在「需求/供应/末影能量」判断后生成链：
 * 前驱 {@code CarryTask} 把附魔书/目标/青金石搬到站点存储，本任务在站点消耗三者与末影能量，
 * 产出附魔后的目标物品（进度 {@code produced} 持久化），后继 {@code CarryTask} 把产物搬往需求方。
 * <p>
 * 因处于链中，本任务不参与 pending 合并 / 拆分（否则会破坏 carry→enchant→carry 链路），
 * 故 {@link #getGroupingKey()} 返回空、{@link #isSplittable()} 为 false。
 */
public class EnchantTask extends WorkTask {
    public static final Codec<EnchantTask> CODEC;
    public static final StreamCodec<RegistryFriendlyByteBuf, EnchantTask> STREAM_CODEC;

    private final EnchantType enchantType;
    private final ItemResource bookRes;
    private final ItemResource targetRes;
    private final ItemResource lapisRes;
    private final ItemResource outputRes;
    private final int lapisCost;
    private final int enderEnergyCost;
    private final int amount;

    public EnchantTask(EnchantType enchantType, BlockPos workPos, ItemResource bookRes, ItemResource targetRes,
                       ItemResource lapisRes, ItemResource outputRes, int lapisCost, int enderEnergyCost, int amount) {
        this(Optional.empty(), enchantType, workPos, bookRes, targetRes, lapisRes, outputRes, lapisCost, enderEnergyCost, amount, 0);
    }

    public EnchantTask(Optional<Task> next, EnchantType enchantType, BlockPos workPos, ItemResource bookRes, ItemResource targetRes,
                       ItemResource lapisRes, ItemResource outputRes, int lapisCost, int enderEnergyCost, int amount, int produced) {
        super(next, workPos, 1);
        this.enchantType = enchantType;
        this.bookRes = bookRes;
        this.targetRes = targetRes;
        this.lapisRes = lapisRes;
        this.outputRes = outputRes;
        this.lapisCost = lapisCost;
        this.enderEnergyCost = enderEnergyCost;
        this.amount = amount;
        this.setProduced(produced);
    }

    @Override
    public Class<? extends Site> requiredSiteType() { return enchantType.getSiteType(); }

    @Override
    public boolean matchesSite(Site site) { return enchantType.matchesSite(site); }

    @Override
    public Predicate<BlockState> getPredicate() { return enchantType.getBlockPredicate(); }

    public EnchantType getEnchantType() { return enchantType; }
    public ItemResource getBookRes() { return bookRes; }
    public ItemResource getTargetRes() { return targetRes; }
    public ItemResource getLapisRes() { return lapisRes; }
    public ItemResource getOutputRes() { return outputRes; }
    public int getLapisCost() { return lapisCost; }
    public int getEnderEnergyCost() { return enderEnergyCost; }
    public int getAmount() { return amount; }

    @Override
    public WorkTask createSubTask(int workers) {
        return new EnchantTask(enchantType, workPos, bookRes, targetRes, lapisRes, outputRes, lapisCost, enderEnergyCost, amount);
    }

    // ---------- 链节点约定：不参与合并 / 拆分 ----------
    @Override public boolean isSplittable() { return false; }
    @Override public Task split(int amount) { return this; }
    @Override public Optional<?> getGroupingKey() { return Optional.empty(); }
    @Override public int getVolume() { return amount; }

    @Override
    public boolean validate(ServerLevel level) {
        if (!super.validate(level)) return false;
        return true;
    }

    @Override public Type<?> getType() { return TaskTypes.ENCHANT; }
    @Override public StreamCodec<RegistryFriendlyByteBuf, ? extends Task> getStreamCodec() { return STREAM_CODEC; }

    static {
        CODEC = RecordCodecBuilder.create(instance -> instance.group(
                nextCodec(),
                Codec.STRING.xmap(EnchantType::byName, EnchantType::getName)
                        .fieldOf("enchant_type").forGetter(EnchantTask::getEnchantType),
                BlockPos.CODEC.fieldOf("work_pos").forGetter(EnchantTask::getWorkPos),
                ItemResource.CODEC.fieldOf("book").forGetter(EnchantTask::getBookRes),
                ItemResource.CODEC.fieldOf("target").forGetter(EnchantTask::getTargetRes),
                ItemResource.CODEC.fieldOf("lapis").forGetter(EnchantTask::getLapisRes),
                ItemResource.CODEC.fieldOf("output").forGetter(EnchantTask::getOutputRes),
                Codec.INT.fieldOf("lapis_cost").forGetter(EnchantTask::getLapisCost),
                Codec.INT.fieldOf("ender_energy_cost").forGetter(EnchantTask::getEnderEnergyCost),
                Codec.INT.fieldOf("amount").forGetter(EnchantTask::getAmount),
                Codec.INT.optionalFieldOf("produced", 0).forGetter(EnchantTask::getProduced)
        ).apply(instance, (next, et, workPos, book, target, lapis, output, lc, eec, amt, prod) ->
                new EnchantTask(next, et, workPos, book, target, lapis, output, lc, eec, amt, prod)));

        STREAM_CODEC = StreamCodec.composite(
                nextStreamCodec(), EnchantTask::getOptionNext,
                ByteBufCodecs.STRING_UTF8.map(EnchantType::byName, EnchantType::getName), EnchantTask::getEnchantType,
                BlockPos.STREAM_CODEC, EnchantTask::getWorkPos,
                ItemResource.STREAM_CODEC, EnchantTask::getBookRes,
                ItemResource.STREAM_CODEC, EnchantTask::getTargetRes,
                ItemResource.STREAM_CODEC, EnchantTask::getLapisRes,
                ItemResource.STREAM_CODEC, EnchantTask::getOutputRes,
                ByteBufCodecs.INT, EnchantTask::getLapisCost,
                ByteBufCodecs.INT, EnchantTask::getEnderEnergyCost,
                ByteBufCodecs.INT, EnchantTask::getAmount,
                ByteBufCodecs.INT, EnchantTask::getProduced,
                EnchantTask::new
        );
    }
}
