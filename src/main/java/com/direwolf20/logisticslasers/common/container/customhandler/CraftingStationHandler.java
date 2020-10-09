package com.direwolf20.logisticslasers.common.container.customhandler;

import com.direwolf20.logisticslasers.common.tiles.CraftingStationTile;
import net.minecraftforge.items.ItemStackHandler;

public class CraftingStationHandler extends ItemStackHandler {
    CraftingStationTile tile;

    public CraftingStationHandler(int size) {
        super(size);
    }

    public CraftingStationHandler(int size, CraftingStationTile tile) {
        super(size);
        this.tile = tile;
    }

    @Override
    protected void onContentsChanged(int slot) {
        if (tile == null) return;
        tile.calcResult();
    }
}
