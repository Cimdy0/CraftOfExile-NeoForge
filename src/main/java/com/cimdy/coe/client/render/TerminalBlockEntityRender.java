package com.cimdy.coe.client.render;

import com.cimdy.coe.block.entity.TerminalBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class TerminalBlockEntityRender implements BlockEntityRenderer<TerminalBlockEntity, TerminalBlockEntityRenderState> {
    private final EntityModelSet entityModelSet;
    public TerminalBlockEntityRender(BlockEntityRendererProvider.Context context) {
         this.entityModelSet = context.entityModelSet();
    }

    public static final Identifier BEAM_LOCATION = Identifier.withDefaultNamespace("textures/entity/beacon_beam.png");

    @Override
    public void submit(
            TerminalBlockEntityRenderState state, PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        long i = state.animationTime;
        float f1 = state.beamRadiusScale;
        float partialTick = state.partialTick;
    }

    @Override
    public TerminalBlockEntityRenderState createRenderState() {
        return new TerminalBlockEntityRenderState();
    }

    @Override
    public void extractRenderState(
            TerminalBlockEntity terminal, TerminalBlockEntityRenderState state, float partialTick,
            Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay overlay) {

        long i = terminal.getLevel().getGameTime();
        float f = (float) cameraPos.subtract(Vec3.atCenterOf(terminal.getBlockPos())).horizontalDistance();
        LocalPlayer localplayer = Minecraft.getInstance().player;
        float f1 = localplayer != null && localplayer.isScoping() ? 1.0F : Math.max(1.0F, f / 96.0F);
        int j = 0;
        state.animationTime = i;
        state.beamRadiusScale = f1;
        state.partialTick = partialTick;
        BlockEntityRenderer.super.extractRenderState(terminal, state, partialTick, cameraPos, overlay);
    }
}
