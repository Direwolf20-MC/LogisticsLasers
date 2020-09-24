package com.direwolf20.logisticslasers.common.container;

import com.direwolf20.logisticslasers.common.tiles.basetiles.FETileBase;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.util.IIntArray;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;

public abstract class FEContainerBase extends Container {
    public IIntArray data;
    public ItemStackHandler handler;

    // Tile can be null and shouldn't be used for accessing any data that needs to be up to date on both sides
    public FETileBase tile;

    public FEContainerBase(ContainerType<?> container, @Nullable FETileBase tile, IIntArray FETileData, int windowId, PlayerInventory playerInventory) {
        super(container, windowId);

        this.tile = tile;

        this.data = FETileData;
        this.setup(playerInventory);

        trackIntArray(FETileData);
    }

    public FEContainerBase(ContainerType<?> container, @Nullable FETileBase tile, IIntArray FETileData, int windowId, PlayerInventory playerInventory, ItemStackHandler handler) {
        super(container, windowId);

        this.handler = handler;
        this.tile = tile;

        this.data = FETileData;
        this.setup(playerInventory);

        trackIntArray(FETileData);
    }

    public void setup(PlayerInventory inventory) {
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

    /*@Override
    public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack currentStack = slot.getStack();
            itemstack = currentStack.copy();

            if (index < SLOTS) {
                if (! this.mergeItemStack(currentStack, SLOTS, this.inventorySlots.size(), false)) {
                    return ItemStack.EMPTY;
                }
            } else if (! this.mergeItemStack(currentStack, 0, SLOTS, false)) {
                return ItemStack.EMPTY;
            }

            if (currentStack.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }
        }

        return itemstack;
    }*/

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        BlockPos pos = this.tile.getPos();
        return this.tile != null && !this.tile.isRemoved() && playerIn.getDistanceSq(new Vector3d(pos.getX(), pos.getY(), pos.getZ()).add(0.5D, 0.5D, 0.5D)) <= 64D;
    }

    public int getMaxPower() {
        return this.data.get(1) * 32;
    }

    public int getEnergy() {
        return this.data.get(0) * 32;
    }
}
