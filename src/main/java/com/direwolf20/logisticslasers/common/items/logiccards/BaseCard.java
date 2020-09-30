package com.direwolf20.logisticslasers.common.items.logiccards;

import com.direwolf20.logisticslasers.LogisticsLasers;
import com.direwolf20.logisticslasers.common.container.BasicFilterContainer;
import com.direwolf20.logisticslasers.common.container.customhandler.FilterSlotHandler;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

import java.util.HashSet;
import java.util.Set;

public abstract class BaseCard extends Item {

    protected BaseCard.CardType CARDTYPE;

    public BaseCard() {
        super(new Item.Properties().maxStackSize(64).group(LogisticsLasers.itemGroup));
    }

    public enum CardType {
        EXTRACT,
        INSERT,
        CRAFT
    }

    public BaseCard(Properties prop) {
        super(prop);
    }

    public CardType getCardType() {
        return CARDTYPE;
    }

    public static FilterSlotHandler setInventory(ItemStack stack, FilterSlotHandler handler) {
        stack.getOrCreateTag().put("inv", handler.serializeNBT());
        return handler;
    }

    public static FilterSlotHandler getInventory(ItemStack stack) {
        CompoundNBT compound = stack.getOrCreateTag();
        FilterSlotHandler handler = new FilterSlotHandler(BasicFilterContainer.SLOTS, stack);
        handler.deserializeNBT(compound.getCompound("inv"));
        return !compound.contains("inv") ? setInventory(stack, new FilterSlotHandler(BasicFilterContainer.SLOTS, stack)) : handler;
    }

    public static Set<Item> getFilteredItems(ItemStack stack) {
        Set<Item> filteredItems = new HashSet<>();
        FilterSlotHandler filterSlotHandler = getInventory(stack);
        for (int i = 0; i < filterSlotHandler.getSlots(); i++) {
            ItemStack itemStack = filterSlotHandler.getStackInSlot(i);
            if (!itemStack.isEmpty())
                filteredItems.add(itemStack.getItem());
        }
        return filteredItems;
    }
}
