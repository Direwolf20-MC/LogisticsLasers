package com.direwolf20.logisticslasers.common.items.logiccards;

import com.direwolf20.logisticslasers.LogisticsLasers;
import com.direwolf20.logisticslasers.common.container.BasicFilterContainer;
import com.direwolf20.logisticslasers.common.container.customhandler.FilterSlotHandler;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

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
}
