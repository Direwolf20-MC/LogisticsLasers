package com.direwolf20.logisticslasers.common.container.customhandler;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

public class FilterSlotHandler extends ItemStackHandler {
    ItemStack stack;

    public FilterSlotHandler(int size, ItemStack itemStack) {
        super(size);
        this.stack = itemStack;
    }

    /*@Override
    protected void onContentsChanged(int slot) {
        BaseCard.setInventory(stack, this);
    }*/
}
