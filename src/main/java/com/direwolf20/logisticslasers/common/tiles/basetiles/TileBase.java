package com.direwolf20.logisticslasers.common.tiles.basetiles;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ServerChunkProvider;

public class TileBase extends TileEntity {

    public TileBase(TileEntityType<?> type) {
        super(type);
    }

    //Misc Methods for TE's
    @Override
    public void read(BlockState state, CompoundNBT tag) {
        super.read(state, tag);
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        return super.write(tag);
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        // Vanilla uses the type parameter to indicate which type of tile entity (command block, skull, or beacon?) is receiving the packet, but it seems like Forge has overridden this behavior
        return new SUpdateTileEntityPacket(pos, 0, getUpdateTag());
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return write(new CompoundNBT());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        read(this.getBlockState(), pkt.getNbtCompound());
    }

    public void markDirtyClient() {
        markDirty();
        if (getWorld() != null) {
            SUpdateTileEntityPacket supdatetileentitypacket = this.getUpdatePacket();
            BlockState state = world.getBlockState(this.pos);
            if (state.isAir(world, this.pos))
                return; //If the block is being broken, the TE stick around a bit longer and this might fire
            if (supdatetileentitypacket == null) return;
            Chunk chunk = world.getChunkAt(this.pos);
            ((ServerChunkProvider) chunk.getWorld().getChunkProvider()).chunkManager.getTrackingPlayers(chunk.getPos(), false).forEach((player) -> {
                player.connection.sendPacket(supdatetileentitypacket);
            });
        }
    }
}
