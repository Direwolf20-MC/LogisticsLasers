package com.direwolf20.logisticslasers.common.container.customhandler;

import com.direwolf20.logisticslasers.common.tiles.InventoryNodeTile;
import net.minecraftforge.items.ItemStackHandler;

public class InventoryNodeHandler extends ItemStackHandler {
    InventoryNodeTile tile;

    public InventoryNodeHandler(int size) {
        super(size);
    }

    public InventoryNodeHandler(int size, InventoryNodeTile tile) {
        super(size);
        this.tile = tile;
    }

    @Override
    protected void onContentsChanged(int slot) {
        if (tile == null) return;
        tile.notifyControllerOfChanges();
    }
}
