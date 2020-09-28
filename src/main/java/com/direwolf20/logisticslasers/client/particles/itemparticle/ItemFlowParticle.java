package com.direwolf20.logisticslasers.client.particles.itemparticle;

import net.minecraft.client.particle.BreakingParticle;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;

public class ItemFlowParticle extends BreakingParticle {

    public ItemFlowParticle(ClientWorld world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, ItemStack itemStack) {
        this(world, x, y, z, itemStack);
        this.motionX = 0;
        this.motionY = 0;
        this.motionZ = 0;
        this.motionX += xSpeed;
        this.motionY += ySpeed;
        this.motionZ += zSpeed;
        this.particleGravity = 0.0f;
        this.maxAge = 40;
    }

    public ItemFlowParticle(ClientWorld world, double x, double y, double z, ItemStack itemStack) {
        super(world, x, y, z, itemStack);
    }

    @Override
    public void tick() {
        //System.out.println("I'm Alive");
        super.tick();
    }

    public static IParticleFactory<ItemFlowParticleData> FACTORY =
            (data, world, x, y, z, xSpeed, ySpeed, zSpeed) ->
                    new ItemFlowParticle(world, x, y, z, xSpeed, ySpeed, zSpeed, data.getItemStack());
}
