package com.direwolf20.logisticslasers.common.util;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import java.util.HashMap;

public class ItemHandlerUtil {
    @Nonnull
    public static ItemStack extractItem(IItemHandler source, @Nonnull ItemStack incstack, boolean simulate) {
        if (source == null || incstack.isEmpty())
            return incstack;

        int amtGotten = 0;
        int amtRemaining = incstack.getCount();
        ItemStack stack = incstack.copy();
        for (int i = 0; i < source.getSlots(); i++) {
            ItemStack stackInSlot = source.getStackInSlot(i);
            if (stackInSlot.isItemEqual(stack)) {
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

    /**
     * Everything below here was shameless stolen from Mekanism and pupnewfster :)
     * https://github.com/mekanism/Mekanism/blob/1.16.x/src/main/java/mekanism/common/content/transporter/TransporterManager.java
     * This method allows you to simulate inserting many stacks into an inventory before doing so
     * The purpose of which is to track stacks in transit
     *
     * @param handler       - The item handler we are inserting into
     * @param inventoryInfo A semi-copy of the destination handler that we can update with multiple stacks
     * @param stack         The stack we are trying to insert this iteration
     * @param count         The number of items in the stack we are trying to insert (stack.getCount())
     * @param inFlight      Whether or not this itemstack is currently traveling through the network, or if it hasn't left yet
     * @return The amount of items that failed to insert in the simulation.
     */
    public static int simulateInsert(IItemHandler handler, InventoryInfo inventoryInfo, ItemStack stack, int count, boolean inFlight) {
        int maxStackSize = stack.getMaxStackSize();
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            if (count == 0) {
                // Nothing more to insert
                break;
            }
            int max = handler.getSlotLimit(slot);
            //If no items are allowed in the slot, pass it up before checking anything about the items
            if (max == 0) {
                continue;
            }

            // Make sure that the item is valid for the handler
            if (!handler.isItemValid(slot, stack)) {
                continue;
            }

            // Simulate the insert; note that we can't depend solely on the "normal" simulate, since it would only tell us about
            // _this_ stack, not the cumulative set of stacks. Use our best guess about stacking/maxes to figure out
            // how the inventory would look after the insertion

            // Number of items in the destination
            int destCount = inventoryInfo.stackSizes.getInt(slot);

            int mergedCount = count + destCount;
            int toAccept = count;
            boolean needsSimulation = false;
            if (destCount > 0) {
                if (!areItemsStackable(inventoryInfo.inventory.get(slot), stack) || destCount >= max) {
                    //If the destination isn't empty and not stackable or it is currently full, move along
                    continue;
                } else if (max > maxStackSize && mergedCount > maxStackSize) {
                    //If we have items in the destination, and the max amount is larger than
                    // the max size of the stack, and the total number of items will also be larger
                    // than the max stack size, we need to simulate to see how much we are actually
                    // able to insert
                    needsSimulation = true;
                    //If the stack's actual size is less than or equal to the max stack size
                    // then we need to increase the size by one for purposes of properly
                    // being able to simulate what the "limit" of the slot is rather
                    // than sending one extra item to the slot only for it to fail
                    //Note: Because we check the size of the stack against the max stack size
                    // even if there are multiple slots that need this, we only end up copying
                    // our stack a single time to resize it. We do however make sure to update
                    // the toAccept value again if it is needed.
                    if (count <= maxStackSize) {
                        if (stack.getCount() <= maxStackSize) {
                            stack = size(stack, maxStackSize + 1);
                        }
                        //Update our amount that we expect to accept from simulation to represent the amount we actually
                        // are trying to insert this way if we can't accept it all then we know that the slot actually
                        // has a lower limit than it returned for getSlotLimit
                        toAccept = stack.getCount();
                    } else if (stack.getCount() <= maxStackSize) {
                        //Note: If we have more we are trying to insert than the max stack size, just take the number we are trying to insert
                        // so that we have an accurate amount for checking the real slot stack size
                        stack = size(stack, count);
                    }
                } else if (!inFlight) {
                    //Otherwise if we are not in flight yet, we should simulate before we actually start sending the item
                    // in case it isn't currently accepting new items even though it is not full
                    // For in flight items we follow our own logic for calculating insertions so that we are not having to
                    // query potentially expensive simulation options as often
                    needsSimulation = true;
                }
            } else {
                // If the item stack is empty, we need to do a simulated insert since we can't tell if the stack
                // in question would be allowed in this slot. Otherwise, we depend on areItemsStackable to keep us
                // out of trouble
                needsSimulation = true;
            }
            if (needsSimulation) {
                ItemStack simulatedRemainder = handler.insertItem(slot, stack, true);
                int accepted = stack.getCount() - simulatedRemainder.getCount();
                if (accepted == 0) {
                    // Insert will fail; bail
                    continue;
                } else if (accepted < toAccept) {
                    //If we accepted less than the amount we expected to, the slot actually has a lower limit
                    // so we mark the amount we accepted plus the amount already in the slot as the slot's
                    // actual limit
                    max = handler.getStackInSlot(slot).getCount() + accepted;
                }
                if (destCount == 0) {
                    //If we actually are going to insert it, because there are currently no items
                    // in the destination, we set the item to the one we destCountare sending so that we can compare
                    // it with InventoryUtils.areItemsStackable. This makes it so that we do not send multiple
                    // items of different types to the same slot just because they are not there yet
                    inventoryInfo.inventory.set(slot, size(stack, 1));
                }
            }
            if (mergedCount > max) {
                // Not all the items will fit; put max in and save leftovers
                inventoryInfo.stackSizes.set(slot, max);
                count = mergedCount - max;
            } else {
                // All items will fit; set the destination count as the new combined amount
                inventoryInfo.stackSizes.set(slot, mergedCount);
                return 0;
            }
        }
        return count;
    }

    public static boolean areItemsStackable(ItemStack toInsert, ItemStack inSlot) {
        if (toInsert.isEmpty() || inSlot.isEmpty()) {
            return true;
        }
        return ItemHandlerHelper.canItemStacksStack(inSlot, toInsert);
    }

    public static ItemStack size(ItemStack stack, int size) {
        if (size <= 0 || stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return ItemHandlerHelper.copyStackWithSize(stack, size);
    }

    public static class InventoryInfo {

        private final NonNullList<ItemStack> inventory;
        private final IntList stackSizes = new IntArrayList();

        public InventoryInfo(IItemHandler handler) {
            inventory = NonNullList.withSize(handler.getSlots(), ItemStack.EMPTY);
            for (int i = 0; i < handler.getSlots(); i++) {
                ItemStack stack = handler.getStackInSlot(i);
                inventory.set(i, stack);
                stackSizes.add(stack.getCount());
            }
        }
    }

    public static class InventoryCounts {
        private final HashMap<ItemStack, Integer> itemCounts = new HashMap<>();

        public InventoryCounts(IItemHandler handler) {
            for (int i = 0; i < handler.getSlots(); i++) {
                ItemStack stack = handler.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    setCount(stack);
                }
            }
        }

        public void setCount(ItemStack stack) {
            for (ItemStack cacheStack : itemCounts.keySet()) {
                if (cacheStack.isItemEqual(stack)) {
                    itemCounts.put(cacheStack, itemCounts.get(cacheStack) + stack.getCount());
                    return;
                }
            }
            itemCounts.put(stack, stack.getCount());
        }

        public int getCount(ItemStack stack) {
            for (ItemStack cacheStack : itemCounts.keySet()) {
                if (cacheStack.isItemEqual(stack))
                    return (itemCounts.get(cacheStack));
            }
            return 0;
        }
    }
}
