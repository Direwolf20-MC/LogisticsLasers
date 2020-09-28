package com.direwolf20.logisticslasers.client.particles;

import com.direwolf20.logisticslasers.LogisticsLasers;
import com.direwolf20.logisticslasers.client.particles.itemparticle.ItemFlowParticle;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LogisticsLasers.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ParticleRenderDispatcher {

    @SubscribeEvent
    public static void registerFactories(ParticleFactoryRegisterEvent evt) {
        Minecraft.getInstance().particles.registerFactory(ModParticles.ITEMFLOWPARTICLE, ItemFlowParticle.FACTORY);
        //Minecraft.getInstance().particles.registerFactory(ModParticles.PLAYERPARTICLE, PlayerParticleType.FACTORY::new);
        //Minecraft.getInstance().particles.registerFactory(ModParticles.LIGHT_PARTICLE, LightParticleType.LightParticleFactory::new);
    }
}
