package com.direwolf20.logisticslasers.common.items.logiccards;

import com.direwolf20.logisticslasers.LogisticsLasers;
import com.direwolf20.logisticslasers.common.container.customhandler.FilterSlotHandler;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public abstract class BaseCard extends Item {
    public BaseCard() {
        super(new Item.Properties().maxStackSize(64).group(LogisticsLasers.itemGroup));
    }

    public BaseCard(Properties prop) {
        super(prop);
    }

    public static FilterSlotHandler setInventory(ItemStack stack, FilterSlotHandler handler) {
        stack.getOrCreateTag().put("inv", handler.serializeNBT());
        return handler;
    }
}
