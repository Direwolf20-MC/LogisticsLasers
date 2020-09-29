package com.direwolf20.logisticslasers.common.items.logiccards;

import com.direwolf20.logisticslasers.LogisticsLasers;
import net.minecraft.item.Item;

public abstract class BaseCard extends Item {
    public BaseCard() {
        super(new Item.Properties().maxStackSize(64).group(LogisticsLasers.itemGroup));
    }

    public BaseCard(Properties prop) {
        super(prop);
    }
}
