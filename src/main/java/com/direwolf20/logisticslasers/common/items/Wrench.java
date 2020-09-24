package com.direwolf20.logisticslasers.common.items;

import com.direwolf20.logisticslasers.LogisticsLasers;
import net.minecraft.item.Item;

public class Wrench extends Item {
    public Wrench() {
        super(new Item.Properties().maxStackSize(1).group(LogisticsLasers.itemGroup));
    }
}
