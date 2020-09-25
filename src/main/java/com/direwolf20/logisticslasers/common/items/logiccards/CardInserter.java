package com.direwolf20.logisticslasers.common.items.logiccards;

import com.direwolf20.logisticslasers.LogisticsLasers;
import net.minecraft.item.Item;

public class CardInserter extends Item {
    public CardInserter() {
        super(new Item.Properties().maxStackSize(1).group(LogisticsLasers.itemGroup));
    }
}
