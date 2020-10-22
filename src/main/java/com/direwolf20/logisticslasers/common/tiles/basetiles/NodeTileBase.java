package com.direwolf20.logisticslasers.common.tiles.basetiles;

import com.direwolf20.logisticslasers.client.renders.LaserConnections;
import com.direwolf20.logisticslasers.common.tiles.ControllerTile;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;

import java.util.*;

public class NodeTileBase extends TileBase {
    protected final Set<BlockPos> connectedNodes = new HashSet<>();
    protected BlockPos controllerPos = BlockPos.ZERO;
    private HashMap<BlockPos, ArrayList<BlockPos>> routeList = new HashMap<>();

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

    public ArrayList<BlockPos> getRouteTo(BlockPos pos) {
        if (!routeList.containsKey(pos))
            findRouteFor(pos);
        return routeList.get(pos);
    }

    public boolean findRouteFor(BlockPos pos) {
        System.out.println("Finding route for: " + pos);
        routeList.remove(pos);
        ControllerTile te = getControllerTE();
        if (te == null) return false;
        ArrayList<BlockPos> routePath = findRouteToPos(pos, new HashSet<BlockPos>());
        Collections.reverse(routePath);
        routeList.put(pos, routePath);
        System.out.println("Found route: " + routePath);
        return !routePath.isEmpty();
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

    public void clearRouteList() {
        routeList.clear();
    }

    public void controllerReDiscover() {
        if (!hasController()) {
            markDirtyClient();
            LaserConnections.buildLaserList();
            return;
        }
        ControllerTile te = getControllerTE();
        if (te != null)
            te.discoverAllNodes();
    }

    public void setControllerPos(BlockPos controllerPos) {
        this.controllerPos = controllerPos;
        //markDirtyClient();
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

    /**
     * @param pos The Position in world you're connecting this TE to.
     * @return Was the connection successful
     * Connects This Pos -> Target Pos, and connects Target Pos -> This pos
     */
    public boolean addConnection(BlockPos pos) {
        NodeTileBase te = (NodeTileBase) world.getTileEntity(pos);
        if (te.hasController() && hasController() && !te.getControllerPos().equals(getControllerPos()))
            return false;

        boolean success = addNode(pos);
        if (success) {
            if (!te.addNode(this.pos)) {
                removeNode(pos);
                return false;
            }
            if (te.hasController())
                setControllerPos(te.getControllerPos());
            controllerReDiscover();
        }
        return success;
    }

    public boolean removeConnection(BlockPos pos) {
        boolean success = removeNode(pos);
        if (success) {
            NodeTileBase te = (NodeTileBase) world.getTileEntity(pos);
            te.removeNode(this.pos);
            controllerReDiscover();
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
        controllerReDiscover();
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
