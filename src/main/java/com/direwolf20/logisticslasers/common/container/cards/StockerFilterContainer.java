package com.direwolf20.logisticslasers.common.container.cards;

import com.direwolf20.logisticslasers.common.blocks.ModBlocks;
import com.direwolf20.logisticslasers.common.container.customslot.StockerFilterSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IIntArray;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;

public class StockerFilterContainer extends BasicFilterContainer {

    public StockerFilterContainer(int windowId, PlayerInventory playerInventory, PacketBuffer extraData) {
        super(windowId, playerInventory, extraData);
    }

    public StockerFilterContainer(@Nullable ItemStack card, int windowId, PlayerInventory playerInventory, ItemStackHandler handler, IIntArray cardData) {
        this(card, windowId, playerInventory, handler, BlockPos.ZERO, cardData);
    }

    public StockerFilterContainer(@Nullable ItemStack card, int windowId, PlayerInventory playerInventory, ItemStackHandler handler, BlockPos sourcePos, IIntArray cardData) {
        super(ModBlocks.STOCKER_FILTER_CONTAINER.get(), card, windowId, playerInventory, handler, sourcePos, cardData);
    }

    //Stocker cards don't use priority or whitelist.
    @Override
    public boolean showPriority() {
        return false;
    }

    @Override
    public boolean showWhiteList() {
        return false;
    }

    @Override
    public boolean isWhiteList() {
        return true;
    }

    public void setup(PlayerInventory inventory) {
        //Slots
        int startX = 44;
        int startY = 17;

        for (int filterRow = 0; filterRow < 3; ++filterRow) {
            for (int filterCol = 0; filterCol < 5; ++filterCol) {
                int x = startX + filterCol * 18;
                int y = startY + filterRow * 18;
                addSlot(new StockerFilterSlot(handler, filterCol + filterRow * 5, x, y));
            }
        }

        // Slots for the hotbar
        for (int row = 0; row < 9; ++row) {
            int x = 8 + row * 18;
            int y = 56 + 86;
            addSlot(new Slot(inventory, row, x, y));
        }
        // Slots for the main inventory
        for (int row = 1; row < 4; ++row) {
            for (int col = 0; col < 9; ++col) {
                int x = 8 + col * 18;
                int y = row * 18 + (56 + 10);
                addSlot(new Slot(inventory, col + row * 9, x, y));
            }
        }
    }

    @Override
    public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack currentStack = slot.getStack().copy();
            //currentStack.setCount(1);

            if (index < SLOTS) {
                if (!this.mergeItemStack(currentStack, SLOTS, this.inventorySlots.size(), false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                for (int i = 0; i < SLOTS; i++) { //Prevents the same item from going in there more than once.
                    if (this.inventorySlots.get(i).getStack().getItem().equals(currentStack.getItem()))
                        return ItemStack.EMPTY;
                }
                if (!this.mergeItemStack(currentStack, 0, SLOTS, false)) {
                    return ItemStack.EMPTY;
                }
            }

        }

        return itemstack;
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return true;
    }

    @Override
    public void onContainerClosed(PlayerEntity playerIn) {
        super.onContainerClosed(playerIn);
    }

    public int getPriority() {
        return this.data.get(0);
    }
}
