package com.direwolf20.logisticslasers.client.particles;

import com.direwolf20.logisticslasers.LogisticsLasers;
import com.direwolf20.logisticslasers.client.particles.itemparticle.ItemFlowParticleData;
import com.direwolf20.logisticslasers.client.particles.itemparticle.ItemFlowParticleType;
import net.minecraft.particles.ParticleType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;

// TODO: 12/07/2020 Replaces this with a deffered register
@Mod.EventBusSubscriber(modid = LogisticsLasers.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
@ObjectHolder(LogisticsLasers.MOD_ID)
public class ModParticles {
    @ObjectHolder("itemflowparticle")
    public static ParticleType<ItemFlowParticleData> ITEMFLOWPARTICLE;

    @SubscribeEvent
    public static void registerParticles(RegistryEvent.Register<ParticleType<?>> evt) {
        evt.getRegistry().registerAll(
                new ItemFlowParticleType().setRegistryName("itemflowparticle")
        );
    }
}
