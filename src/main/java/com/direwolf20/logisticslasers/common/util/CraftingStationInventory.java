package com.direwolf20.logisticslasers.common.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.RecipeItemHelper;
import net.minecraftforge.items.ItemStackHandler;

public class CraftingStationInventory extends CraftingInventory {
    private ItemStackHandler crafter;
    private final int width;
    private final int height;

    public CraftingStationInventory(ItemStackHandler crafter, int width, int height) {
        super(null, width, height);
        this.crafter = crafter;
        this.width = width;
        this.height = height;
    }

    /**
     * Returns the number of slots in the inventory.
     */
    public int getSizeInventory() {
        return this.crafter.getSlots();
    }

    public boolean isEmpty() {
        for (int i = 0; i < crafter.getSlots(); i++) {
            if (!crafter.getStackInSlot(i).isEmpty()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns the stack in the given slot.
     */
    public ItemStack getStackInSlot(int index) {
        return crafter.getStackInSlot(index);
    }

    /**
     * Removes a stack from the given slot and returns it.
     */
    public ItemStack removeStackFromSlot(int index) {
        crafter.setStackInSlot(index, ItemStack.EMPTY);
        return ItemStack.EMPTY;
    }

    /**
     * Removes up to a specified number of items from an inventory slot and returns them in a new stack.
     */
    public ItemStack decrStackSize(int index, int count) {
        crafter.getStackInSlot(index).shrink(count);
        return crafter.getStackInSlot(index);
    }

    /**
     * Sets the given item stack to the specified slot in the inventory (can be crafting or armor sections).
     */
    public void setInventorySlotContents(int index, ItemStack stack) {
        crafter.setStackInSlot(index, stack);
    }

    /**
     * For tile entities, ensures the chunk containing the tile entity is saved to disk later - the game won't think it
     * hasn't changed and skip it.
     */
    public void markDirty() {
    }

    /**
     * Don't rename this method to canInteractWith due to conflicts with Container
     */
    public boolean isUsableByPlayer(PlayerEntity player) {
        return true;
    }

    public void clear() {
        crafter = new ItemStackHandler(crafter.getSlots());
    }

    public int getHeight() {
        return this.height;
    }

    public int getWidth() {
        return this.width;
    }

    public void fillStackedContents(RecipeItemHelper helper) {
        for (int i = 0; i < crafter.getSlots(); i++) {
            helper.accountPlainStack(crafter.getStackInSlot(i));
        }

    }
}
