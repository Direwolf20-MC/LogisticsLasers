package com.direwolf20.logisticslasers.common.util;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;

public class ItemHandlerUtil {
    @Nonnull
    public static ItemStack extractItem(IItemHandler source, @Nonnull ItemStack stack, boolean simulate) {
        if (source == null || stack.isEmpty())
            return stack;

        int amtGotten = 0;
        int amtRemaining = stack.getCount();
        for (int i = 0; i < source.getSlots(); i++) {
            ItemStack stackInSlot = source.getStackInSlot(i);
            if (stackInSlot.getItem().equals(stack.getItem())) {
                int extractAmt = Math.min(amtRemaining, stackInSlot.getCount());
                ItemStack tempStack = source.extractItem(i, extractAmt, simulate);
                amtGotten += tempStack.getCount();
                amtRemaining -= tempStack.getCount();
                if (amtRemaining == 0) break;
            }
        }
        stack.setCount(amtGotten);
        return stack;
    }
}
