package com.direwolf20.logisticslasers.client.particles.itemparticle;

import net.minecraft.client.particle.BreakingParticle;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Vector3d;

import java.util.Random;

public class ItemFlowParticle extends BreakingParticle {

    private double targetX, targetY, targetZ;
    Random random = new Random();

    public ItemFlowParticle(ClientWorld world, double x, double y, double z, double targetX, double targetY, double targetZ, ItemStack itemStack, int ticksPerBlock) {
        this(world, x, y, z, itemStack);
        this.motionX = 0;
        this.motionY = 0;
        this.motionZ = 0;
        this.targetX = targetX;
        this.targetY = targetX;
        this.targetZ = targetX;
        Vector3d target = new Vector3d(targetX, targetY, targetZ);
        Vector3d source = new Vector3d(this.posX, this.posY, this.posZ);
        Vector3d path = target.subtract(source).normalize().mul(1, 1, 1);
        this.motionX += path.x / ticksPerBlock;
        this.motionY += path.y / ticksPerBlock;
        this.motionZ += path.z / ticksPerBlock;
        this.particleGravity = 0.0f;
        double distance = target.distanceTo(source);
        this.maxAge = (int) distance * ticksPerBlock;
        //System.out.println(source +":"+target);
        this.canCollide = false;
        float minSize = 0.015f;
        float maxSize = 0.035f;
        float partSize = minSize + random.nextFloat() * (maxSize - minSize);
        this.particleScale = partSize;
    }

    public ItemFlowParticle(ClientWorld world, double x, double y, double z, ItemStack itemStack) {
        super(world, x, y, z, itemStack);
    }

    @Override
    public void tick() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        if (this.age++ >= this.maxAge) {
            this.setExpired();
        } else {
            this.motionY -= 0.04D * (double) this.particleGravity;
            this.move(this.motionX, this.motionY, this.motionZ);
        }
    }

    public static IParticleFactory<ItemFlowParticleData> FACTORY =
            (data, world, x, y, z, xSpeed, ySpeed, zSpeed) ->
                    new ItemFlowParticle(world, x, y, z, data.targetX, data.targetY, data.targetZ, data.getItemStack(), data.ticksPerBlock);
}
