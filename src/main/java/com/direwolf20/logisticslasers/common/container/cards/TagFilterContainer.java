package com.direwolf20.logisticslasers.common.container.cards;

import com.direwolf20.logisticslasers.common.blocks.ModBlocks;
import com.direwolf20.logisticslasers.common.container.customslot.BasicFilterSlot;
import com.direwolf20.logisticslasers.common.items.logiccards.*;
import com.direwolf20.logisticslasers.common.tiles.InventoryNodeTile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
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

public class TagFilterContainer extends Container {
    public static final int SLOTS = 1;
    public ItemStackHandler handler;
    public BlockPos sourceContainer = BlockPos.ZERO;
    public IIntArray data;

    // ItemStack can be null and shouldn't be used for accessing any data that needs to be up to date on both sides
    public ItemStack filterItemStack;

    public TagFilterContainer(int windowId, PlayerInventory playerInventory, PacketBuffer extraData) {
        this(ItemStack.EMPTY, windowId, playerInventory, new ItemStackHandler(SLOTS), new IntArray(2));
        filterItemStack = extraData.readItemStack();
    }

    public TagFilterContainer(@Nullable ItemStack card, int windowId, PlayerInventory playerInventory, ItemStackHandler handler, IIntArray cardData) {
        this(card, windowId, playerInventory, handler, BlockPos.ZERO, cardData);
    }

    public TagFilterContainer(@Nullable ContainerType<?> type, @Nullable ItemStack card, int windowId, PlayerInventory playerInventory, ItemStackHandler handler, BlockPos sourcePos, IIntArray cardData) {
        super(type, windowId);
        this.handler = handler;
        this.filterItemStack = card;
        this.setup(playerInventory);
        this.sourceContainer = sourcePos;
        this.data = cardData;
        trackIntArray(cardData);
    }

    public TagFilterContainer(@Nullable ItemStack card, int windowId, PlayerInventory playerInventory, ItemStackHandler handler, BlockPos sourcePos, IIntArray cardData) {
        super(ModBlocks.TAG_FILTER_CONTAINER.get(), windowId);
        this.handler = handler;
        this.filterItemStack = card;
        this.setup(playerInventory);
        this.sourceContainer = sourcePos;
        this.data = cardData;
        trackIntArray(cardData);
    }

    public boolean showPriority() {
        return filterItemStack.getItem() instanceof CardInserter;
    }

    public boolean showWhiteList() {
        return !(filterItemStack.getItem() instanceof CardStocker);
    }

    public boolean showNBTFilter() {
        return !(filterItemStack.getItem() instanceof CardInserterMod);
    }

    public boolean showExtractAmt() {
        return (filterItemStack.getItem() instanceof CardExtractor);
    }

    public boolean isWhiteList() {
        return BaseCard.getWhiteList(filterItemStack);
    }

    public int getPriority() {
        return this.data.get(0);
    }

    public int getExtractAmt() {
        return this.data.get(1);
    }

    public boolean isNBTFilter() {
        return BaseCard.getNBTFilter(filterItemStack);
    }

    @Override
    public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, PlayerEntity player) {
        if (clickTypeIn == ClickType.SWAP)
            return ItemStack.EMPTY;
        return super.slotClick(slotId, dragType, clickTypeIn, player);
    }

    public void setup(PlayerInventory inventory) {
        //Slots
        int startX = 177;
        int startY = 6;

        addSlot(new BasicFilterSlot(handler, 0, startX, startY));


        // Slots for the hotbar
        for (int row = 0; row < 9; ++row) {
            int x = 8 + row * 18;
            int y = 56 + 86 + 88;
            addSlot(new Slot(inventory, row, x, y));
        }
        // Slots for the main inventory
        for (int row = 1; row < 4; ++row) {
            for (int col = 0; col < 9; ++col) {
                int x = 8 + col * 18;
                int y = row * 18 + (56 + 10 + 88);
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
    }

    @Override
    public void onContainerClosed(PlayerEntity playerIn) {
        World world = playerIn.getEntityWorld();
        if (!world.isRemote) {
            handler.setStackInSlot(0, ItemStack.EMPTY);
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
}
