package com.cimdy.coe.data.codec.stronghold.task.helper.result;

import com.cimdy.coe.data.codec.stronghold.task.helper.recipe.CraftStepDetail;
import com.cimdy.coe.data.codec.stronghold.task.helper.claim.MaterialClaim;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.List;

public record PlanResult(int producedAmount,
                              List<CraftStepDetail> steps,
                              ItemResource producedResource,
                              BlockPos outputSite,
                              List<MaterialClaim> materialClaims) {
        // 合成成功时使用，无原始容器来源
        public PlanResult(int producedAmount, List<CraftStepDetail> steps, ItemResource producedResource, BlockPos outputSite) {
            this(producedAmount, steps, producedResource, outputSite, List.of());
        }
        // 直接供应时使用，无合成步骤
        public PlanResult(int producedAmount, List<MaterialClaim> materialClaims) {
            this(producedAmount, List.of(), null, null, materialClaims);
        }
    }
