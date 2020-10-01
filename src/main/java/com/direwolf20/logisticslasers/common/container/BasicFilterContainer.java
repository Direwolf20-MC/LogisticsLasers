package com.direwolf20.logisticslasers.common.container;

import com.direwolf20.logisticslasers.common.blocks.ModBlocks;
import com.direwolf20.logisticslasers.common.container.customslot.BasicFilterSlot;
import com.direwolf20.logisticslasers.common.items.logiccards.BaseCard;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;

public class BasicFilterContainer extends Container {
    public static final int SLOTS = 15;
    public ItemStackHandler handler;

    // Tile can be null and shouldn't be used for accessing any data that needs to be up to date on both sides
    public ItemStack filterItemStack;

    public BasicFilterContainer(int windowId, PlayerInventory playerInventory, PacketBuffer extraData) {
        this(ItemStack.EMPTY, windowId, playerInventory, new ItemStackHandler(SLOTS));
    }

    public BasicFilterContainer(@Nullable ItemStack card, int windowId, PlayerInventory playerInventory, ItemStackHandler handler) {
        super(ModBlocks.BASIC_FILTER_CONTAINER.get(), windowId);
        this.handler = handler;
        this.filterItemStack = card;
        this.setup(playerInventory);
    }

    public void setup(PlayerInventory inventory) {
        //Slots
        /*addSlot(new BasicFilterSlot(handler, 0, 62, 17));
        addSlot(new BasicFilterSlot(handler, 1, 80, 17));
        addSlot(new BasicFilterSlot(handler, 2, 98, 17));
        addSlot(new BasicFilterSlot(handler, 3, 62, 35));
        addSlot(new BasicFilterSlot(handler, 4, 80, 35));
        addSlot(new BasicFilterSlot(handler, 5, 98, 35));
        addSlot(new BasicFilterSlot(handler, 6, 62, 53));
        addSlot(new BasicFilterSlot(handler, 7, 80, 53));
        addSlot(new BasicFilterSlot(handler, 8, 98, 53));*/
        int startX = 44;
        int startY = 17;

        for (int filterRow = 0; filterRow < 3; ++filterRow) {
            for (int filterCol = 0; filterCol < 5; ++filterCol) {
                int x = startX + filterCol * 18;
                int y = startY + filterRow * 18;
                addSlot(new BasicFilterSlot(handler, filterCol + filterRow * 5, x, y));
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
            currentStack.setCount(1);

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
        /*ItemStack stack = playerIn.getHeldItemMainhand();
        return stack.equals(this.filterItemStack) && !stack.isEmpty();*/
    }

    @Override
    public void onContainerClosed(PlayerEntity playerIn) {
        BaseCard.setInventory(filterItemStack, handler);
        super.onContainerClosed(playerIn);
    }
}
