package com.direwolf20.logisticslasers.common.tiles;

import com.direwolf20.logisticslasers.common.blocks.ModBlocks;
import com.direwolf20.logisticslasers.common.tiles.basetiles.TileBase;
import net.minecraft.tileentity.TileEntityType;

public class BasicNodeTile extends TileBase {

    public BasicNodeTile() {
        super(ModBlocks.BASIC_NODE_TILE.get());
    }

    public BasicNodeTile(TileEntityType<?> type) {
        super(type);
    }

}