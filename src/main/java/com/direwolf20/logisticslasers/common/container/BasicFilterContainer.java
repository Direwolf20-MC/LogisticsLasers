package com.direwolf20.logisticslasers.common.container;

import com.direwolf20.logisticslasers.common.blocks.ModBlocks;
import com.direwolf20.logisticslasers.common.container.customslot.BasicFilterSlot;
import com.direwolf20.logisticslasers.common.items.logiccards.BaseCard;
import com.direwolf20.logisticslasers.common.tiles.InventoryNodeTile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIntArray;
import net.minecraft.util.IntArray;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;

public class BasicFilterContainer extends Container {
    public static final int SLOTS = 15;
    public ItemStackHandler handler;
    public BlockPos sourceContainer = BlockPos.ZERO;
    public IIntArray data;
    public boolean showPriority;

    // ItemStack can be null and shouldn't be used for accessing any data that needs to be up to date on both sides
    public ItemStack filterItemStack;

    public BasicFilterContainer(int windowId, PlayerInventory playerInventory, PacketBuffer extraData) {
        this(ItemStack.EMPTY, windowId, playerInventory, new ItemStackHandler(SLOTS), new IntArray(2));
        showPriority = extraData.getBoolean(0);
    }

    public BasicFilterContainer(@Nullable ItemStack card, int windowId, PlayerInventory playerInventory, ItemStackHandler handler, IIntArray cardData) {
        this(card, windowId, playerInventory, handler, BlockPos.ZERO, cardData);
    }

    public BasicFilterContainer(@Nullable ItemStack card, int windowId, PlayerInventory playerInventory, ItemStackHandler handler, BlockPos sourcePos, IIntArray cardData) {
        super(ModBlocks.BASIC_FILTER_CONTAINER.get(), windowId);
        this.handler = handler;
        this.filterItemStack = card;
        this.setup(playerInventory);
        this.sourceContainer = sourcePos;
        this.data = cardData;
        trackIntArray(cardData);
    }

    public void setup(PlayerInventory inventory) {
        //Slots
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
        World world = playerIn.getEntityWorld();
        if (!world.isRemote) {
            BaseCard.setInventory(filterItemStack, handler);
            if (!sourceContainer.equals(BlockPos.ZERO)) {
                TileEntity te = world.getTileEntity(sourceContainer);
                if (te instanceof InventoryNodeTile) {
                    ((InventoryNodeTile) te).notifyControllerOfChanges();
                }
            }
        }
        super.onContainerClosed(playerIn);
    }

    public int getPriority() {
        return this.data.get(0);
    }
}
