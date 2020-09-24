package com.direwolf20.logisticslasers.common.tiles;

import com.direwolf20.logisticslasers.common.capabilities.FEEnergyStorage;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.IIntArray;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class FETileBase extends TileEntity implements ITickableTileEntity {

    //Data about the energy stored in this tile entity
    public FEEnergyStorage energyStorage;
    private LazyOptional<FEEnergyStorage> energy;

    // Handles tracking changes, kinda messy but apparently this is how the cool kids do it these days
    public final IIntArray FETileData = new IIntArray() {
        @Override
        public int get(int index) {
            switch (index) {
                case 0:
                    return FETileBase.this.energyStorage.getEnergyStored() / 32;
                case 1:
                    return FETileBase.this.energyStorage.getMaxEnergyStored() / 32;
                default:
                    throw new IllegalArgumentException("Invalid index: " + index);
            }
        }

        @Override
        public void set(int index, int value) {
            throw new IllegalStateException("Cannot set values through IIntArray");
        }

        @Override
        public int size() {
            return 2;
        }
    };

    public FETileBase(TileEntityType<?> type) {
        super(type);
        this.energyStorage = new FEEnergyStorage(this, 0, 1000000);
        this.energy = LazyOptional.of(() -> this.energyStorage);
    }

    public FETileBase(TileEntityType<?> type, int maxPower) {
        super(type);
        this.energyStorage = new FEEnergyStorage(this, 0, maxPower);
        this.energy = LazyOptional.of(() -> this.energyStorage);
    }

    @Override
    public void tick() {
    }

    //Misc Methods for TE's
    @Override
    public void read(BlockState state, CompoundNBT tag) {
        super.read(state, tag);
        energy.ifPresent(h -> h.deserializeNBT(tag.getCompound("energy")));
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        energy.ifPresent(h -> tag.put("energy", h.serializeNBT()));
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
            /*BlockState state = getWorld().getBlockState(getPos());
            getWorld().notifyBlockUpdate(getPos(), state, state, 3);*/
        }
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, final @Nullable Direction side) {
        if (cap == CapabilityEnergy.ENERGY)
            return energy.cast();

        return super.getCapability(cap, side);
    }

    @Override
    public void remove() {
        energy.invalidate();
        super.remove();
    }
}
