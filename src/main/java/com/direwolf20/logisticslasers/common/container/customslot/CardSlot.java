package com.direwolf20.logisticslasers.common.container.customslot;

import com.direwolf20.logisticslasers.common.items.logiccards.BaseCard;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class CardSlot extends SlotItemHandler {
    public CardSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
    }

    @Override
    public boolean isItemValid(@Nonnull ItemStack stack) {
        return (stack.getItem() instanceof BaseCard);
    }
}
