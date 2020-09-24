package com.direwolf20.logisticslasers.common.tiles.basetiles;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;

import java.util.HashSet;
import java.util.Set;

public class NodeTileBase extends TileBase {
    private final Set<BlockPos> connectedNodes = new HashSet<>();

    public NodeTileBase(TileEntityType<?> type) {
        super(type);
    }

    public boolean addNode(BlockPos pos) {
        return connectedNodes.add(pos);
    }

    public boolean removeNode(BlockPos pos) {
        return connectedNodes.remove(pos);
    }

    public Set<BlockPos> getConnectedNodes() {
        return connectedNodes;
    }

    //Misc Methods for TE's
    @Override
    public void read(BlockState state, CompoundNBT tag) {
        super.read(state, tag);
        ListNBT connections = tag.getList("connections", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < connections.size(); i++) {
            BlockPos blockPos = NBTUtil.readBlockPos(connections.getCompound(i).getCompound("pos"));
            connectedNodes.add(blockPos);
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        ListNBT connections = new ListNBT();
        for (BlockPos blockPos : connectedNodes) {
            CompoundNBT comp = new CompoundNBT();
            comp.put("pos", NBTUtil.writeBlockPos(blockPos));
            connections.add(comp);
        }
        tag.put("connections", connections);
        return super.write(tag);
    }
}
