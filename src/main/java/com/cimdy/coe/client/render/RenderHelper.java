package com.cimdy.coe.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2fStack;

public class RenderHelper {
                public static void renderStringWithScale(GuiGraphicsExtractor guiGraphics, Component component, int color,
                                             int x, int y, float scale) {
        Matrix3x2fStack poseStack = guiGraphics.pose();
        poseStack.pushMatrix();
        poseStack.scale(scale, scale);
        guiGraphics.text(Minecraft.getInstance().font, component,
                (int) (x / scale) + 2, (int) (y / scale) + 2, color, false);
        poseStack.popMatrix();
    }

    public static void renderStringWithScale(GuiGraphicsExtractor guiGraphics, Component component,
                                             int x, int y, float scale) {
        Matrix3x2fStack poseStack = guiGraphics.pose();
        poseStack.pushMatrix();
        poseStack.scale(scale, scale);
        guiGraphics.text(Minecraft.getInstance().font, component,
                (int) (x / scale) + 2, (int) (y / scale) + 2, -1, false);
        poseStack.popMatrix();
    }

    public static void renderItemWithScale(
            GuiGraphicsExtractor guiGraphics, ItemStack itemStack, int x, int y, float box) {
        Matrix3x2fStack stack = guiGraphics.pose();
        stack.pushMatrix();
        float scale = box / 16.0f;
        stack.scale(scale, scale);
        guiGraphics.item(itemStack,(int) (x / scale), (int) (y / scale));
        stack.popMatrix();
    }

    public static void renderCenteredStringWithScale(
            GuiGraphicsExtractor guiGraphics, Component component, int color, int x, int y, float scale) {
        Matrix3x2fStack stack = guiGraphics.pose();
        stack.pushMatrix();
        stack.scale(scale, scale);
        guiGraphics.centeredText(Minecraft.getInstance().font, component, (int) (x / scale), (int) (y / scale), color);
        stack.popMatrix();
    }

    public static void renderCenteredStringWithScale(
            GuiGraphicsExtractor guiGraphics, Component component, int x, int y, float scale) {
        renderCenteredStringWithScale(guiGraphics, component, -1, x, y, scale);
    }

    public static void drawBorderCorners(GuiGraphicsExtractor guiGraphics, int x, int y, int width, int height, int padding, int length, int color) {
        guiGraphics.horizontalLine(x + padding, x + padding + length, y + padding, color);
        guiGraphics.horizontalLine(x + padding, x + padding + length, y + height - padding, color);
        guiGraphics.horizontalLine(x + width - padding, x + width - padding - length, y + padding, color);
        guiGraphics.horizontalLine(x + width - padding, x + width - padding - length, y + height - padding, color);
    }

    public static void renderBorderBox(
            GuiGraphicsExtractor guiGraphics, int x, int y, int width, int height, int backcolor, int borderColor, int fillColor) {
        guiGraphics.fill(x, y, x + width, y + height, backcolor);
        guiGraphics.outline(x, y, width, height, borderColor);
        guiGraphics.fill(x + 1, y + 1, x, y, fillColor);
        guiGraphics.fill(x + width - 1, y + height - 1, x + width, y + height, fillColor);
        guiGraphics.fill(x + width - 1, y + 1, x + width, y, fillColor);
        guiGraphics.fill(x + 1, y + height - 1, x, y + height, fillColor);
    }

    public static boolean isHoverArea(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

}
