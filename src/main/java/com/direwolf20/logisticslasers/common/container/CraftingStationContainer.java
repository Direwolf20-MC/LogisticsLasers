package com.direwolf20.logisticslasers.common.container;

import com.direwolf20.logisticslasers.common.blocks.ModBlocks;
import com.direwolf20.logisticslasers.common.container.customhandler.CraftingStationHandler;
import com.direwolf20.logisticslasers.common.container.customslot.BasicFilterSlot;
import com.direwolf20.logisticslasers.common.container.customslot.CraftingSlot;
import com.direwolf20.logisticslasers.common.tiles.CraftingStationTile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nullable;

public class CraftingStationContainer extends Container {
    public static final int SLOTS = 37;
    public ItemStackHandler handler;
    public CraftingStationHandler craftingHandler;

    // Tile can be null and shouldn't be used for accessing any data that needs to be up to date on both sides
    public CraftingStationTile tile;

    public CraftingStationContainer(int windowId, PlayerInventory playerInventory, PacketBuffer extraData) {
        this((CraftingStationTile) playerInventory.player.world.getTileEntity(extraData.readBlockPos()), windowId, playerInventory, new ItemStackHandler(27));
    }

    public CraftingStationContainer(@Nullable CraftingStationTile tile, int windowId, PlayerInventory playerInventory, ItemStackHandler handler) {
        super(ModBlocks.CRAFTING_STATION_CONTAINER.get(), windowId);
        this.handler = handler;
        this.tile = tile;
        this.craftingHandler = tile.craftMatrixHandler;
        this.setup(playerInventory);
        tile.calcResult();
        if (playerInventory.player instanceof ServerPlayerEntity && tile.getControllerTE() != null) {
            tile.getControllerTE().updateItemCounts((ServerPlayerEntity) playerInventory.player);
            tile.markDirtyClient();
        }
    }

    public void setup(PlayerInventory inventory) {
        //Chest like Slots
        int startX = 8;
        int startY = 17 + 66;

        for (int filterRow = 0; filterRow < 3; ++filterRow) {
            for (int filterCol = 0; filterCol < 9; ++filterCol) {
                int x = startX + filterCol * 18;
                int y = startY + filterRow * 18;
                addSlot(new SlotItemHandler(handler, filterCol + filterRow * 9, x, y));
            }
        }

        startX = 30;
        startY = 17;

        for (int filterRow = 0; filterRow < 3; ++filterRow) {
            for (int filterCol = 0; filterCol < 3; ++filterCol) {
                int x = startX + filterCol * 18;
                int y = startY + filterRow * 18;
                addSlot(new BasicFilterSlot(craftingHandler, (filterCol + filterRow * 3), x, y));
            }
        }

        this.addSlot(new CraftingSlot(tile.craftResult, 0, 124, 35));

        // Slots for the hotbar
        for (int row = 0; row < 9; ++row) {
            int x = 8 + row * 18;
            int y = 56 + 151;
            addSlot(new Slot(inventory, row, x, y));
        }
        // Slots for the main inventory
        for (int row = 1; row < 4; ++row) {
            for (int col = 0; col < 9; ++col) {
                int x = 8 + col * 18;
                int y = row * 18 + (56 + 10 + 65);
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

            if (index < 27) {
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
