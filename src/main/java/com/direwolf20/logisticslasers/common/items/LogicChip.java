package com.direwolf20.logisticslasers.common.items;

import com.direwolf20.logisticslasers.LogisticsLasers;
import net.minecraft.item.Item;

public class LogicChip extends Item {
    public LogicChip() {
        super(new Item.Properties().maxStackSize(64).group(LogisticsLasers.itemGroup));
    }
}
