package com.direwolf20.logisticslasers.common.container;

import com.direwolf20.logisticslasers.common.blocks.ModBlocks;
import com.direwolf20.logisticslasers.common.container.customhandler.InventoryNodeHandler;
import com.direwolf20.logisticslasers.common.container.customslot.CardSlot;
import com.direwolf20.logisticslasers.common.tiles.InventoryNodeTile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

import javax.annotation.Nullable;

public class InventoryNodeContainer extends Container {
    public static final int SLOTS = 9;
    public InventoryNodeHandler handler;

    // Tile can be null and shouldn't be used for accessing any data that needs to be up to date on both sides
    public InventoryNodeTile tile;

    public InventoryNodeContainer(int windowId, PlayerInventory playerInventory, PacketBuffer extraData) {
        this((InventoryNodeTile) playerInventory.player.world.getTileEntity(extraData.readBlockPos()), windowId, playerInventory, new InventoryNodeHandler(SLOTS));
    }

    public InventoryNodeContainer(@Nullable InventoryNodeTile tile, int windowId, PlayerInventory playerInventory, InventoryNodeHandler handler) {
        super(ModBlocks.INVENTORY_NODE_CONTAINER.get(), windowId);
        this.handler = handler;
        this.tile = tile;
        this.setup(playerInventory);

    }

    public void setup(PlayerInventory inventory) {
        //Slots
        addSlot(new CardSlot(handler, 0, 62, 17));
        addSlot(new CardSlot(handler, 1, 80, 17));
        addSlot(new CardSlot(handler, 2, 98, 17));
        addSlot(new CardSlot(handler, 3, 62, 35));
        addSlot(new CardSlot(handler, 4, 80, 35));
        addSlot(new CardSlot(handler, 5, 98, 35));
        addSlot(new CardSlot(handler, 6, 62, 53));
        addSlot(new CardSlot(handler, 7, 80, 53));
        addSlot(new CardSlot(handler, 8, 98, 53));
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
            ItemStack currentStack = slot.getStack();
            itemstack = currentStack.copy();

            if (index < SLOTS) {
                if (!this.mergeItemStack(currentStack, SLOTS, this.inventorySlots.size(), false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.mergeItemStack(currentStack, 0, SLOTS, false)) {
                return ItemStack.EMPTY;
            }

            if (currentStack.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }
        }

        return itemstack;
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        BlockPos pos = this.tile.getPos();
        return this.tile != null && !this.tile.isRemoved() && playerIn.getDistanceSq(new Vector3d(pos.getX(), pos.getY(), pos.getZ()).add(0.5D, 0.5D, 0.5D)) <= 64D;
    }
}
