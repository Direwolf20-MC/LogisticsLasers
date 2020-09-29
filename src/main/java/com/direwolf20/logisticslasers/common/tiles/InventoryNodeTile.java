package com.direwolf20.logisticslasers.common.tiles;

import com.direwolf20.logisticslasers.common.blocks.ModBlocks;
import com.direwolf20.logisticslasers.common.container.InventoryNodeContainer;
import com.direwolf20.logisticslasers.common.container.customhandler.InventoryNodeHandler;
import com.direwolf20.logisticslasers.common.tiles.basetiles.NodeTileBase;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.*;

public class InventoryNodeTile extends NodeTileBase implements INamedContainerProvider {

    private HashMap<BlockPos, ArrayList<BlockPos>> routeList = new HashMap<>();

    private LazyOptional<InventoryNodeHandler> inventory = LazyOptional.of(() -> new InventoryNodeHandler(InventoryNodeContainer.SLOTS, this));

    @Nullable
    private LazyOptional<IItemHandler> facingHandler;

    public InventoryNodeTile() {
        super(ModBlocks.INVENTORY_NODE_TILE.get());
    }

    public ArrayList<BlockPos> getRouteTo(BlockPos pos) {
        if (!routeList.containsKey(pos))
            findRouteFor(pos);
        return routeList.get(pos);
    }

    public void notifyControllerOfChanges() {
        ControllerTile te = getControllerTE();
        if (te == null) return;
        System.out.println("Telling controller at " + getControllerPos() + " to check inventory at " + this.pos);
        te.checkInvNode(this.pos);
    }

    @Override
    public void addToController() {
        ControllerTile te = getControllerTE();
        if (te == null) return;
        te.addToInvNodes(pos);
        //findRoutes();
    }

    @Override
    public void removeFromController() {
        routeList.clear();
        ControllerTile te = getControllerTE();
        if (te == null) return;
        te.removeFromInvNodes(pos);
    }

    public ItemStackHandler getInventoryStacks() {
        ItemStackHandler handler = inventory.orElse(new InventoryNodeHandler(InventoryNodeContainer.SLOTS, this));
        return handler;
    }

    public LazyOptional<IItemHandler> getHandler() {
        if (facingHandler != null) {
            return facingHandler;
        }

        // if no inventory cached yet, find a new one
        Direction facing = getBlockState().get(BlockStateProperties.FACING);
        assert world != null;
        TileEntity te = world.getTileEntity(pos.offset(facing));
        // if we have a TE and its an item handler, try extracting from that
        if (te != null) {
            LazyOptional<IItemHandler> handler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing.getOpposite());
            if (handler.isPresent()) {
                // add the invalidator
                handler.addListener((handler1) -> clearCachedInventories());
                // cache and return
                return facingHandler = handler;
            }
        }
        // no item handler, cache empty
        facingHandler = null;
        return LazyOptional.empty();
    }

    /**
     * Called when a neighbor updates to invalidate the inventory cache
     */
    public void clearCachedInventories() {
        this.facingHandler = null;
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

    public void clearRouteList() {
        routeList.clear();
    }

    public void findAllRoutes() {
        clearRouteList();
        ControllerTile te = getControllerTE();
        if (te == null) return;
        Set<BlockPos> todoList = new HashSet<>(te.getInventoryNodes());
        todoList.remove(pos);
        for (BlockPos pos : todoList) {
            ArrayList<BlockPos> routePath = findRouteToPos(pos, new HashSet<BlockPos>());
            //routePath.remove(this.pos);
            Collections.reverse(routePath);
            routeList.put(pos, routePath);
        }
        System.out.println(routeList);
    }

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        assert world != null;
        return new InventoryNodeContainer(this, i, playerInventory, this.inventory.orElse(new InventoryNodeHandler(InventoryNodeContainer.SLOTS, this)));
    }

    @Override
    public void read(BlockState state, CompoundNBT tag) {
        super.read(state, tag);
        inventory.ifPresent(h -> h.deserializeNBT(tag.getCompound("inv")));
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        inventory.ifPresent(h -> tag.put("inv", h.serializeNBT()));
        return super.write(tag);
    }

    @Override
    public ITextComponent getDisplayName() {
        return new StringTextComponent("Inventory Node");
    }
}