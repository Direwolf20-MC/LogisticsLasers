package com.direwolf20.logisticslasers.common.items.logiccards;

import com.direwolf20.logisticslasers.LogisticsLasers;
import net.minecraft.item.Item;

public class RoutingLogicModule extends Item {
    public RoutingLogicModule() {
        super(new Item.Properties().maxStackSize(64).group(LogisticsLasers.itemGroup));
    }
}
