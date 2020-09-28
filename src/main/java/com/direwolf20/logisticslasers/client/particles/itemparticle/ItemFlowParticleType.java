package com.direwolf20.logisticslasers.client.particles.itemparticle;

import com.mojang.serialization.Codec;
import net.minecraft.particles.ParticleType;

public class ItemFlowParticleType extends ParticleType<ItemFlowParticleData> {
    public ItemFlowParticleType() {
        super(false, ItemFlowParticleData.DESERIALIZER);
    }

    @Override
    public Codec<ItemFlowParticleData> func_230522_e_() {
        return null;
    }


}
