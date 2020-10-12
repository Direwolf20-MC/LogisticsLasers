package com.direwolf20.logisticslasers.common.container.customslot;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class AvailableItemSlot extends SlotItemHandler {
    public AvailableItemSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
    }

    @Override
    public int getSlotStackLimit() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean canTakeStack(PlayerEntity player) {
        return false;
    }
}
