package com.direwolf20.logisticslasers.common.items.logiccards;

import com.direwolf20.logisticslasers.LogisticsLasers;
import net.minecraft.item.Item;

public class CardExtractor extends Item {
    public CardExtractor() {
        super(new Item.Properties().maxStackSize(1).group(LogisticsLasers.itemGroup));
    }
}
