package com.direwolf20.logisticslasers.common.util;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

import java.util.Objects;

public class ItemStackKey {
    public final Item item;
    public final CompoundNBT nbt;
    private final int hash;


    public ItemStackKey(ItemStack stack) {
        this.item = stack.getItem();
        this.nbt = stack.getTag();
        this.hash = Objects.hash(item, nbt);
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ItemStackKey) {
            return (((ItemStackKey) obj).item == this.item) && Objects.equals(((ItemStackKey) obj).nbt, this.nbt);
        }
        return false;
    }
}
