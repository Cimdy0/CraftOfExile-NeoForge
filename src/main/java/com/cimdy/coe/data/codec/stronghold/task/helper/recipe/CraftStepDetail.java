package com.cimdy.coe.data.codec.stronghold.task.helper.recipe;

import com.cimdy.coe.data.codec.stronghold.task.helper.claim.MaterialClaim;
import com.cimdy.coe.data.codec.stronghold.task.helper.claim.ProductClaim;
import com.cimdy.coe.data.codec.stronghold.ItemCount;
import com.cimdy.coe.data.codec.stronghold.task.Task;
import com.cimdy.coe.data.codec.stronghold.task.impl.CarryTask;
import com.cimdy.coe.data.codec.stronghold.task.impl.CraftTask;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;

import java.util.ArrayList;
import java.util.List;

public record CraftStepDetail(
            ResourceKey<Recipe<?>> recipe,
            BlockPos site,
            int times,
            List<MaterialClaim> materials,
            List<ProductClaim> products
    ) {
        public static final Codec<CraftStepDetail> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceKey.codec(Registries.RECIPE).fieldOf("recipe").forGetter(CraftStepDetail::recipe),
                BlockPos.CODEC.fieldOf("site").forGetter(CraftStepDetail::site),
                Codec.INT.fieldOf("times").forGetter(CraftStepDetail::times),
                MaterialClaim.CODEC.listOf().fieldOf("materials").forGetter(CraftStepDetail::materials),
                ProductClaim.CODEC.listOf().fieldOf("products").forGetter(CraftStepDetail::products)
        ).apply(instance, CraftStepDetail::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, CraftStepDetail> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.fromCodec(ResourceKey.codec(Registries.RECIPE)), CraftStepDetail::recipe,
                BlockPos.STREAM_CODEC, CraftStepDetail::site,
                ByteBufCodecs.INT, CraftStepDetail::times,
                MaterialClaim.STREAM_CODEC.apply(ByteBufCodecs.list()), CraftStepDetail::materials,
                ProductClaim.STREAM_CODEC.apply(ByteBufCodecs.list()), CraftStepDetail::products,
                CraftStepDetail::new
        );

        public Task createTaskChainForStep(GlobalPos terminalPos) {
            Task head = null, tail = null;
            for (MaterialClaim claim : this.materials()) {
                Task carry = new CarryTask(
                        new ItemCount(claim.resource(), claim.amount()),
                        claim.containerPos(),
                        this.site(),
                        terminalPos,
                        terminalPos
                );
                if (head == null) head = carry;
                else tail.setNext(carry);
                tail = carry;
            }

            List<ItemCount> items = materials.stream()
                    .map(mc -> new ItemCount(mc.resource(), mc.amount()))
                    .toList();
            // 方案 C：产品搬运作为合成的「并行后继」（DAG 分支），而非串行链。
            // 材料仍串行（保证合成前材料已到位），产品由调度器 advanceTaskChain 并行分发。
            List<Task> productCarries = new ArrayList<>();
            for (ProductClaim claim : this.products()) {
                productCarries.add(new CarryTask(
                        new ItemCount(claim.resource(), claim.amount()),
                        this.site(),
                        claim.containerPos(),
                        terminalPos,
                        terminalPos
                ));
            }
            Task craft = new CraftTask(items, this.recipe(), this.site(), this.times(), productCarries);
            if (head == null) head = craft;
            else tail.setNext(craft);
            tail = craft;
            return head;
        }
    }
