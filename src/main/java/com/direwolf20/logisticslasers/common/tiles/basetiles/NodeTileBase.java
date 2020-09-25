package com.direwolf20.logisticslasers.common.tiles.basetiles;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
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
        boolean success = connectedNodes.add(pos);
        if (success) {
            markDirtyClient();
        }
        return success;
    }

    public boolean removeNode(BlockPos pos) {
        boolean success = connectedNodes.remove(pos);
        if (success) {
            markDirtyClient();
        }
        return success;
    }

    public boolean addConnection(BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        if (!(te instanceof NodeTileBase))
            return false;
        boolean success = addNode(pos);
        if (success) {
            ((NodeTileBase) te).addNode(this.pos);
        }
        return success;
    }

    public boolean removeConnection(BlockPos pos) {
        boolean success = removeNode(pos);
        if (success) {
            TileEntity te = world.getTileEntity(pos);
            if (!(te instanceof NodeTileBase))
                return success;
            ((NodeTileBase) te).removeNode(this.pos);
        }
        return success;
    }

    public Set<BlockPos> getConnectedNodes() {
        return connectedNodes;
    }

    public void disconnectAllNodes() {
        for (BlockPos pos : connectedNodes) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof NodeTileBase) {
                ((NodeTileBase) te).removeNode(this.pos);
            }
        }
    }

    //Misc Methods for TE's
    @Override
    public void read(BlockState state, CompoundNBT tag) {
        super.read(state, tag);
        connectedNodes.clear();
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

    @Override
    public void remove() {
        if (!world.isRemote)
            disconnectAllNodes();
        super.remove();
    }
}
