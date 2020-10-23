package com.direwolf20.logisticslasers.client.renders;

import com.direwolf20.logisticslasers.client.renderhelpers.LaserRendering;
import com.direwolf20.logisticslasers.common.blocks.baseblocks.BaseNode;
import com.direwolf20.logisticslasers.common.tiles.basetiles.NodeTileBase;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class LaserConnections {
    public static long lastRefreshTime;
    public static SetMultimap<BlockPos, BlockPos> lasers = HashMultimap.create();

    public static void renderLasers(RenderWorldLastEvent evt) {
        if (((System.currentTimeMillis() - lastRefreshTime) / 1000) >= 5) {
            //System.out.println("Refreshing Laser List");
            buildLaserList();
        }
        drawLasers(evt);
    }

    public static void renderSelectedBlock(RenderWorldLastEvent evt, BlockPos pos) {
        final Minecraft mc = Minecraft.getInstance();

        IRenderTypeBuffer.Impl buffer = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();

        Vector3d view = mc.gameRenderer.getActiveRenderInfo().getProjectedView();

        MatrixStack matrix = evt.getMatrixStack();
        matrix.push();
        matrix.translate(-view.getX(), -view.getY(), -view.getZ());

        IVertexBuilder builder;
        builder = buffer.getBuffer(OurRenderTypes.SolidBlockOverlay);

        matrix.push();
        matrix.translate(pos.getX(), pos.getY(), pos.getZ());
        matrix.translate(-0.005f, -0.005f, -0.005f);
        matrix.scale(1.01f, 1.01f, 1.01f);
        matrix.rotate(Vector3f.YP.rotationDegrees(-90.0F));
        Matrix4f positionMatrix = matrix.getLast().getMatrix();
        BlockOverlayRender.render(positionMatrix, builder, pos, Color.GREEN);
        matrix.pop();

        buffer.finish(OurRenderTypes.SolidBlockOverlay);
    }

    public static void drawLasers(RenderWorldLastEvent evt) {
        final Minecraft mc = Minecraft.getInstance();
        World world = mc.world;
        IRenderTypeBuffer.Impl buffer = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
        long gameTime = world.getGameTime();
        double v = gameTime * 0.04;
        Vector3d view = mc.gameRenderer.getActiveRenderInfo().getProjectedView();

        MatrixStack matrix = evt.getMatrixStack();
        matrix.push();
        matrix.translate(-view.getX(), -view.getY(), -view.getZ());
        IVertexBuilder builder;
        builder = buffer.getBuffer(OurRenderTypes.LASER_MAIN_BEAM);
        SetMultimap<BlockPos, BlockPos> lasersCopy = HashMultimap.create(lasers);
        lasersCopy.forEach((source, target) -> {
            matrix.push();
            matrix.translate(source.getX(), source.getY(), source.getZ());
            float diffX = target.getX() + .5f - source.getX();
            float diffY = target.getY() + .5f - source.getY();
            float diffZ = target.getZ() + .5f - source.getZ();
            Vector3f startLaser = new Vector3f(0.5f, .5f, 0.5f);
            Vector3f endLaser = new Vector3f(diffX, diffY, diffZ);
            Vector3f sortPos = new Vector3f(source.getX(), source.getY(), source.getZ());

            Matrix4f positionMatrix = matrix.getLast().getMatrix();
            LaserRendering.drawLaser(builder, positionMatrix, endLaser, startLaser, 1, 0, 0, 1f, 0.025f, v, v + diffY * 1.5, sortPos);
            matrix.pop();
        });
        matrix.pop();
        buffer.finish(OurRenderTypes.LASER_MAIN_BEAM);
    }

    public static void buildLaserList() {
        lasers.clear();
        lastRefreshTime = System.currentTimeMillis();
        PlayerEntity player = Minecraft.getInstance().player;
        World world = player.world;
        BlockPos playerPos = new BlockPos(player.getPosX(), player.getPosY(), player.getPosZ());
        int radius = 35;

        List<BlockPos> nodeBlocksList = new ArrayList<>();
        nodeBlocksList = BlockPos.getAllInBox(playerPos.add(-radius, -radius, -radius), playerPos.add(radius, radius, radius))
                .filter(blockPos -> player.world.getBlockState(blockPos).getBlock() instanceof BaseNode)
                .map(BlockPos::toImmutable)
                .collect(Collectors.toList());

        for (BlockPos sourcepos : nodeBlocksList) {
            NodeTileBase te = (NodeTileBase) world.getTileEntity(sourcepos);
            Set<BlockPos> connectedNodes = te.getConnectedNodes();
            for (BlockPos targetpos : connectedNodes) {
                if (canAdd(sourcepos, targetpos)) {
                    lasers.put(sourcepos, targetpos);
                }
            }
        }
    }

    public static boolean canAdd(BlockPos sourcePos, BlockPos targetPos) {
        if (!lasers.containsKey(targetPos))
            return true;
        Set<BlockPos> tempSet = lasers.get(targetPos);
        if (!tempSet.contains(sourcePos))
            return true;
        return false;
    }
}
