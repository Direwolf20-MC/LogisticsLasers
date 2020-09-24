package com.direwolf20.logisticslasers.common.capabilities;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemEnergyProvider implements ICapabilityProvider {
    private ItemStack stack;
    private int energyCapacity;
    private LazyOptional<IEnergyStorage> capability = LazyOptional.of(() -> new EnergisedItem(stack, energyCapacity));

    public ItemEnergyProvider(ItemStack stack, int energyCapacity) {
        this.stack = stack;
        this.energyCapacity = energyCapacity;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return cap == CapabilityEnergy.ENERGY ? capability.cast() : LazyOptional.empty();
    }
}
