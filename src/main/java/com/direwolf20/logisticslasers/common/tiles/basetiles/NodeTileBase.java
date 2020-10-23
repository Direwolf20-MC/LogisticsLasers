package com.direwolf20.logisticslasers.common.tiles.basetiles;

import com.direwolf20.logisticslasers.common.network.PacketHandler;
import com.direwolf20.logisticslasers.common.network.packets.PacketUpdateLaserRender;
import com.direwolf20.logisticslasers.common.tiles.ControllerTile;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraftforge.common.util.Constants;

import java.util.*;

public class NodeTileBase extends TileBase {
    protected final Set<BlockPos> connectedNodes = new HashSet<>();
    protected BlockPos controllerPos = BlockPos.ZERO;
    private HashMap<BlockPos, List<BlockPos>> routeList = new HashMap<>();

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

    public List<BlockPos> getRouteTo(BlockPos pos) {
        if (!routeList.containsKey(pos))
            findRouteFor(pos);
        return routeList.get(pos);
    }

    public boolean findRouteFor(BlockPos pos) {
        routeList.remove(pos); //Shouldn't be needed but why not
        ControllerTile te = getControllerTE();
        if (te == null) return false; //This also shouldn't happen
        List<BlockPos> routePath = findRouteToPos(pos);
        Collections.reverse(routePath);
        routeList.put(pos, routePath);
        return !routePath.isEmpty();
    }

    public List<BlockPos> findRouteToPos(BlockPos targetPos) {
        List<BlockPos> route = new ArrayList<>(); //The route we're building
        Queue<BlockPos> nodesToCheck = new LinkedList<>(); //A list of nodes remaining to be checked
        Set<BlockPos> checkedNodes = new HashSet<>(); //A list of all nodes we've checked already
        Map<BlockPos, BlockPos> priorNodeMap = new HashMap<>(); //A lookup of the node and the one that lead us to it, used to create the path

        //Initialize the list of nodes to check out by looking at all the nodes this one connects to
        //Also initialize the priorNodeMap
        for (BlockPos connectedNode : getConnectedNodes()) {
            nodesToCheck.add(connectedNode);
            priorNodeMap.put(connectedNode, this.pos);
        }
        boolean foundRoute = false;
        while (!nodesToCheck.isEmpty()) { //Loop through all nodes to check until we run out of find our destination
            BlockPos posToCheck = nodesToCheck.remove(); //Pop the stack
            if (checkedNodes.contains(posToCheck) || posToCheck.equals(this.pos))
                continue; //Don't check nodes we've checked before, and don't operate on the starting node
            checkedNodes.add(posToCheck); //Add this position to the list of nodes we already checked
            TileEntity te = world.getTileEntity(posToCheck);
            if (te instanceof NodeTileBase) {
                for (BlockPos connectedNode : ((NodeTileBase) te).getConnectedNodes()) { //Loop through all the connected nodes
                    if (connectedNode.equals(targetPos)) { //If we found the one we're looking for, CELEBRATE!
                        foundRoute = true;
                    }
                    if (!checkedNodes.contains(connectedNode)) { //As long as we haven't checked this node before, add it to the list and note its prior node
                        nodesToCheck.add(connectedNode);
                        priorNodeMap.put(connectedNode, posToCheck);
                    }
                    if (foundRoute) break;
                }
                if (foundRoute) break;
            }
        }
        if (!foundRoute) return route; //This probably shouldn't be possible?
        BlockPos routePos = targetPos; //Starting at the destination, work back through the list of priorNodeMap objects to find a path
        while (routePos != this.pos) {
            route.add(routePos);
            routePos = priorNodeMap.get(routePos);
        }
        route.add(this.pos); //Finally add this position, since it isn't added above
        return route;
    }

    public void clearRouteList() {
        routeList.clear();
    }

    public void updateLaserConnections() {
        Chunk chunk = world.getChunkAt(this.pos);
        ((ServerChunkProvider) chunk.getWorld().getChunkProvider()).chunkManager.getTrackingPlayers(chunk.getPos(), false).forEach((player) -> {
            PacketHandler.sendTo(new PacketUpdateLaserRender(), player);
        });
    }

    public void controllerReDiscover() {
        if (!hasController()) {
            markDirtyClient();
            updateLaserConnections();
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
