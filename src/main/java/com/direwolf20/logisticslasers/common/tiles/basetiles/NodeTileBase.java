package com.direwolf20.logisticslasers.common.tiles.basetiles;

import com.direwolf20.logisticslasers.common.tiles.ControllerTile;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

public class NodeTileBase extends TileBase {
    private final Set<BlockPos> connectedNodes = new HashSet<>();
    protected BlockPos controllerPos = BlockPos.ZERO;
    private boolean isFindingNodes = false;

    public NodeTileBase(TileEntityType<?> type) {
        super(type);
    }

    public BlockPos getControllerPos() {
        return controllerPos;
    }

    public ControllerTile getControllerTE() {
        TileEntity te = world.getTileEntity(getControllerPos());
        return te instanceof ControllerTile ? (ControllerTile) te : null;
    }

    public boolean isFindingNodes() {
        return isFindingNodes;
    }

    public void setFindingNodes(boolean findingNodes) {
        isFindingNodes = findingNodes;
    }

    public void setControllerPos(BlockPos controllerPos, BlockPos sourcePos) {
        if (this.controllerPos.equals(controllerPos)) return;
        if (controllerPos.equals(BlockPos.ZERO)) {
            removeFromController();
            this.controllerPos = controllerPos;
        } else {
            this.controllerPos = controllerPos;
            addToController();
        }
        //System.out.println("Setting Controller position of Node at : " + this.getPos() + " to " + controllerPos);
        updateNeighbors();
    }

    public void addToController() {
        ControllerTile te = getControllerTE();
        if (te == null) return;
        te.addToAllNodes(pos);
    }

    public void removeFromController() {
        ControllerTile te = getControllerTE();
        if (te == null) return;
        te.removeFromAllNodes(pos);
    }

    public void haveControllerUpdateInv() {
        ControllerTile te = getControllerTE();
        if (te == null) return;
        te.updateInvNodePaths();
    }

    public Set<BlockPos> findAllConnectedNodes() {
        setFindingNodes(true);
        Set<BlockPos> nodes = new HashSet<>();
        for (BlockPos pos : connectedNodes) {
            NodeTileBase te = (NodeTileBase) world.getTileEntity(pos);
            if (te.isFindingNodes()) continue;
            if (nodes.add(pos)) {
                nodes.addAll(te.findAllConnectedNodes());
            }
        }
        setFindingNodes(false);
        return nodes;
    }

    public BlockPos validateController() {
        Set<BlockPos> nodesToCheck = findAllConnectedNodes();
        for (BlockPos pos : nodesToCheck) {
            NodeTileBase te = (NodeTileBase) world.getTileEntity(pos);
            if (te.isController()) {
                setControllerPos(te.getPos(), this.getPos());
                return te.getPos();
            }
        }
        setControllerPos(BlockPos.ZERO, this.pos);
        return getControllerPos();
    }

    public void updateNeighbors() {
        for (BlockPos checkpos : connectedNodes) {
            NodeTileBase checkTE = (NodeTileBase) world.getTileEntity(checkpos);
            if (!checkTE.getControllerPos().equals(getControllerPos()))
                checkTE.setControllerPos(getControllerPos(), this.getPos());
        }
    }

    public boolean hasController() {
        return !getControllerPos().equals(BlockPos.ZERO);
    }

    public boolean isController() {
        return getControllerPos().equals(getPos());
    }

    public boolean addNode(BlockPos pos) {
        boolean success = connectedNodes.add(pos);
        if (success) {
            //System.out.println("Connecting " + this.getPos() + " to " + pos);
            markDirtyClient();
        }
        return success;
    }

    public boolean removeNode(BlockPos pos) {
        boolean success = connectedNodes.remove(pos);
        if (success) {
            //System.out.println("Disconnecting " + this.getPos() + " to " + pos);
            validateController();
            markDirtyClient();
        }
        return success;
    }

    public ArrayList<BlockPos> findRouteToPos(BlockPos targetPos, Set<BlockPos> checkedPos) {
        ArrayList<BlockPos> route = new ArrayList<>();
        if (targetPos.equals(pos)) {
            route.add(pos);
            return route;
        }
        ArrayList<BlockPos> connections = new ArrayList<>(getConnectedNodes());
        connections.sort(Comparator.comparingDouble(blockPos -> blockPos.distanceSq(targetPos)));
        for (BlockPos testPos : connections) {
            if (checkedPos.contains(testPos))
                continue;
            checkedPos.add(testPos);
            NodeTileBase te = (NodeTileBase) world.getTileEntity(testPos);
            ArrayList<BlockPos> tempList = te.findRouteToPos(targetPos, checkedPos);
            if (tempList.contains(testPos)) {
                tempList.add(pos);
                return tempList;

            }
        }
        return route;
    }

    /**
     * @param pos The Position in world you're connecting this TE to.
     * @return Was the connection successful
     * Connects This Pos -> Target Pos, and connects Target Pos -> This pos
     */
    public boolean addConnection(BlockPos pos) {
        NodeTileBase te = (NodeTileBase) world.getTileEntity(pos);

        boolean success = addNode(pos);
        if (success) {
            if (!te.addNode(this.pos)) {
                removeNode(pos);
                return false;
            }
            if (!hasController() && !te.hasController())
                return success;
            if (!hasController()) {
                setControllerPos(te.getControllerPos(), te.getPos());
            } else {
                te.setControllerPos(getControllerPos(), this.getPos());
            }
        }
        if (success) {
            haveControllerUpdateInv();
        }

        return success;
    }

    public boolean removeConnection(BlockPos pos) {
        ControllerTile controllerTE = getControllerTE();
        boolean success = removeNode(pos);
        if (success) {
            NodeTileBase te = (NodeTileBase) world.getTileEntity(pos);
            te.removeNode(this.pos);
            if (controllerTE != null)
                controllerTE.updateInvNodePaths();
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
        haveControllerUpdateInv();
        removeFromController();
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
        controllerPos = NBTUtil.readBlockPos(tag.getCompound("controllerpos"));
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
        tag.put("controllerpos", NBTUtil.writeBlockPos(controllerPos));
        return super.write(tag);
    }

    @Override
    public void remove() {
        if (!world.isRemote)
            disconnectAllNodes();
        super.remove();
    }
}
