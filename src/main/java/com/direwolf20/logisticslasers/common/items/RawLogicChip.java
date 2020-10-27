package com.direwolf20.logisticslasers.common.items;

import com.direwolf20.logisticslasers.LogisticsLasers;
import net.minecraft.item.Item;

public class RawLogicChip extends Item {
    public RawLogicChip() {
        super(new Properties().maxStackSize(64).group(LogisticsLasers.itemGroup));
    }
}
