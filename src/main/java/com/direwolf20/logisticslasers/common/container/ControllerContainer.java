package com.direwolf20.logisticslasers.common.container;

import com.direwolf20.logisticslasers.common.blocks.ModBlocks;
import com.direwolf20.logisticslasers.common.tiles.ControllerTile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IIntArray;
import net.minecraft.util.IntArray;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

import javax.annotation.Nullable;

public class ControllerContainer extends FEContainerBase {
    public ControllerContainer(int windowId, PlayerInventory playerInventory, PacketBuffer extraData) {
        this((ControllerTile) playerInventory.player.world.getTileEntity(extraData.readBlockPos()), new IntArray(3), windowId, playerInventory);
    }

    public ControllerContainer(@Nullable ControllerTile tile, IIntArray data, int windowId, PlayerInventory playerInventory) {
        super(ModBlocks.CONTROLLER_CONTAINER.get(), tile, data, windowId, playerInventory);
        this.tile = tile;
        this.data = data;
        this.setup(playerInventory);
        trackIntArray(data);
        if (playerInventory.player instanceof ServerPlayerEntity && tile != null)
            tile.markDirtyClient();
    }

    @Override
    public void setup(PlayerInventory inventory) {
        super.setup(inventory);
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        BlockPos pos = this.tile.getPos();
        return this.tile != null && !this.tile.isRemoved() && playerIn.getDistanceSq(new Vector3d(pos.getX(), pos.getY(), pos.getZ()).add(0.5D, 0.5D, 0.5D)) <= 64D;
    }

    public int getRFCost() {
        return this.data.get(2);
    }
}
