package com.direwolf20.logisticslasers.client.renders;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix4f;

import java.awt.*;

public class BlockOverlayRender {

    public static void render(Matrix4f matrix, IVertexBuilder builder, BlockPos pos, Color color) {
        float red = color.getRed() / 255f, green = color.getGreen() / 255f, blue = color.getBlue() / 255f, alpha = .25f;

        float startX = 0, startY = 0, startZ = -1, endX = 1, endY = 1, endZ = 0;

        //down
        builder.pos(matrix, startX, startY, startZ).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, endX, startY, startZ).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, endX, startY, endZ).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, startX, startY, endZ).color(red, green, blue, alpha).endVertex();

        //up
        builder.pos(matrix, startX, endY, startZ).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, startX, endY, endZ).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, endX, endY, endZ).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, endX, endY, startZ).color(red, green, blue, alpha).endVertex();

        //east
        builder.pos(matrix, startX, startY, startZ).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, startX, endY, startZ).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, endX, endY, startZ).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, endX, startY, startZ).color(red, green, blue, alpha).endVertex();

        //west
        builder.pos(matrix, startX, startY, endZ).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, endX, startY, endZ).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, endX, endY, endZ).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, startX, endY, endZ).color(red, green, blue, alpha).endVertex();

        //south
        builder.pos(matrix, endX, startY, startZ).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, endX, endY, startZ).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, endX, endY, endZ).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, endX, startY, endZ).color(red, green, blue, alpha).endVertex();

        //north
        builder.pos(matrix, startX, startY, startZ).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, startX, startY, endZ).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, startX, endY, endZ).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, startX, endY, startZ).color(red, green, blue, alpha).endVertex();
    }

    public static void renderBoxSolid(Matrix4f matrix, IVertexBuilder builder, BlockPos pos, float r, float g, float b, float alpha) {
        double x = pos.getX() - 0.001;
        double y = pos.getY() - 0.001;
        double z = pos.getZ() - 0.001;
        double xEnd = pos.getX() + 1.0015;
        double yEnd = pos.getY() + 1.0015;
        double zEnd = pos.getZ() + 1.0015;

        renderBoxSolid(matrix, builder, x, y, z, xEnd, yEnd, zEnd, r, g, b, alpha);
    }

    public static void renderBoxSolid(Matrix4f matrix, IVertexBuilder builder, double x, double y, double z, double xEnd, double yEnd, double zEnd, float red, float green, float blue, float alpha) {
        //careful: mc want's it's vertices to be defined CCW - if you do it the other way around weird cullling issues will arise
        //CCW herby counts as if you were looking at it from the outside
        float startX = (float) x;
        float startY = (float) y;
        float startZ = (float) z;
        float endX = (float) xEnd;
        float endY = (float) yEnd;
        float endZ = (float) zEnd;

//        float startX = 0, startY = 0, startZ = -1, endX = 1, endY = 1, endZ = 0;

        //down
        builder.pos(matrix, startX, startY, startZ).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, endX, startY, startZ).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, endX, startY, endZ).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, startX, startY, endZ).color(red, green, blue, alpha).endVertex();

        //up
        builder.pos(matrix, startX, endY, startZ).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, startX, endY, endZ).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, endX, endY, endZ).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, endX, endY, startZ).color(red, green, blue, alpha).endVertex();

        //east
        builder.pos(matrix, startX, startY, startZ).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, startX, endY, startZ).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, endX, endY, startZ).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, endX, startY, startZ).color(red, green, blue, alpha).endVertex();

        //west
        builder.pos(matrix, startX, startY, endZ).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, endX, startY, endZ).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, endX, endY, endZ).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, startX, endY, endZ).color(red, green, blue, alpha).endVertex();

        //south
        builder.pos(matrix, endX, startY, startZ).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, endX, endY, startZ).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, endX, endY, endZ).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, endX, startY, endZ).color(red, green, blue, alpha).endVertex();

        //north
        builder.pos(matrix, startX, startY, startZ).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, startX, startY, endZ).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, startX, endY, endZ).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, startX, endY, startZ).color(red, green, blue, alpha).endVertex();
    }

    public static void renderBox(Matrix4f matrix, IVertexBuilder builder, BlockPos startPos, BlockPos endPos) {
        //We want to draw from the starting position to the (ending position)+1
        int x = Math.min(startPos.getX(), endPos.getX()), y = Math.min(startPos.getY(), endPos.getY()), z = Math.min(startPos.getZ(), endPos.getZ());

        int dx = (startPos.getX() > endPos.getX()) ? startPos.getX() + 1 : endPos.getX() + 1;
        int dy = (startPos.getY() > endPos.getY()) ? startPos.getY() + 1 : endPos.getY() + 1;
        int dz = (startPos.getZ() > endPos.getZ()) ? startPos.getZ() + 1 : endPos.getZ() + 1;

        int R = 255, G = 223, B = 127;

        builder.pos(matrix, x, y, z).color(G, G, G, 0.0F).endVertex();
        builder.pos(matrix, x, y, z).color(G, G, G, R).endVertex();
        builder.pos(matrix, dx, y, z).color(G, B, B, R).endVertex();
        builder.pos(matrix, dx, y, dz).color(G, G, G, R).endVertex();
        builder.pos(matrix, x, y, dz).color(G, G, G, R).endVertex();
        builder.pos(matrix, x, y, z).color(B, B, G, R).endVertex();
        builder.pos(matrix, x, dy, z).color(B, G, B, R).endVertex();
        builder.pos(matrix, dx, dy, z).color(G, G, G, R).endVertex();
        builder.pos(matrix, dx, dy, dz).color(G, G, G, R).endVertex();
        builder.pos(matrix, x, dy, dz).color(G, G, G, R).endVertex();
        builder.pos(matrix, x, dy, z).color(G, G, G, R).endVertex();
        builder.pos(matrix, x, dy, dz).color(G, G, G, R).endVertex();
        builder.pos(matrix, x, y, dz).color(G, G, G, R).endVertex();
        builder.pos(matrix, dx, y, dz).color(G, G, G, R).endVertex();
        builder.pos(matrix, dx, dy, dz).color(G, G, G, R).endVertex();
        builder.pos(matrix, dx, dy, z).color(G, G, G, R).endVertex();
        builder.pos(matrix, dx, y, z).color(G, G, G, R).endVertex();
        builder.pos(matrix, dx, y, z).color(G, G, G, 0.0F).endVertex();
    }
}
