package com.direwolf20.logisticslasers.client.renderhelpers;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;

public class LaserRendering {

    public static Vector3f adjustBeamToEyes(Vector3f from, Vector3f to, Vector3f sortPos) {
        //This method takes the player's position into account, and adjusts the beam so that its rendered properly whereever you stand
        PlayerEntity player = Minecraft.getInstance().player;
        Vector3f P = new Vector3f((float) player.getPosX() - sortPos.getX(), (float) player.getPosYEye() - sortPos.getY(), (float) player.getPosZ() - sortPos.getZ());

        Vector3f PS = from.copy();
        PS.sub(P);
        Vector3f SE = to.copy();
        SE.sub(from);

        Vector3f adjustedVec = PS.copy();
        adjustedVec.cross(SE);
        adjustedVec.normalize();
        return adjustedVec;
    }

    public static void drawLaser(IVertexBuilder builder, Matrix4f positionMatrix, Vector3f from, Vector3f to, float r, float g, float b, float alpha, float thickness, double v1, double v2, Vector3f sortPos) {
        Vector3f adjustedVec = adjustBeamToEyes(from, to, sortPos);
        adjustedVec.mul(thickness); //Determines how thick the beam is

        Vector3f p1 = from.copy();
        p1.add(adjustedVec);
        Vector3f p2 = from.copy();
        p2.sub(adjustedVec);
        Vector3f p3 = to.copy();
        p3.add(adjustedVec);
        Vector3f p4 = to.copy();
        p4.sub(adjustedVec);

        builder.pos(positionMatrix, p1.getX(), p1.getY(), p1.getZ())
                .color(r, g, b, alpha)
                .tex(1, (float) v1)
                .overlay(OverlayTexture.NO_OVERLAY)
                .lightmap(15728880)
                .endVertex();
        builder.pos(positionMatrix, p3.getX(), p3.getY(), p3.getZ())
                .color(r, g, b, alpha)
                .tex(1, (float) v2)
                .overlay(OverlayTexture.NO_OVERLAY)
                .lightmap(15728880)
                .endVertex();
        builder.pos(positionMatrix, p4.getX(), p4.getY(), p4.getZ())
                .color(r, g, b, alpha)
                .tex(0, (float) v2)
                .overlay(OverlayTexture.NO_OVERLAY)
                .lightmap(15728880)
                .endVertex();
        builder.pos(positionMatrix, p2.getX(), p2.getY(), p2.getZ())
                .color(r, g, b, alpha)
                .tex(0, (float) v1)
                .overlay(OverlayTexture.NO_OVERLAY)
                .lightmap(15728880)
                .endVertex();
    }
}
