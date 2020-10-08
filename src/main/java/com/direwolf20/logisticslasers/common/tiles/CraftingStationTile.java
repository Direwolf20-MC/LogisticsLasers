package com.direwolf20.logisticslasers.common.tiles;

import com.direwolf20.logisticslasers.common.blocks.ModBlocks;
import com.direwolf20.logisticslasers.common.container.CraftingStationContainer;
import com.direwolf20.logisticslasers.common.tiles.basetiles.NodeTileBase;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;

public class CraftingStationTile extends NodeTileBase implements INamedContainerProvider {

    private HashMap<BlockPos, ArrayList<BlockPos>> routeList = new HashMap<>();

    private LazyOptional<ItemStackHandler> inventory = LazyOptional.of(() -> new ItemStackHandler(CraftingStationContainer.SLOTS));

    public CraftingStationTile() {
        super(ModBlocks.CRAFTING_STATION_TILE.get());
    }

    /*public ArrayList<BlockPos> getRouteTo(BlockPos pos) {
        if (!routeList.containsKey(pos))
            findRouteFor(pos);
        return routeList.get(pos);
    }*/

    /*public void notifyControllerOfChanges() {
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
    }*/

    public ItemStackHandler getInventoryStacks() {
        ItemStackHandler handler = inventory.orElse(new ItemStackHandler(CraftingStationContainer.SLOTS));
        return handler;
    }

    /*public boolean findRouteFor(BlockPos pos) {
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
    }*/

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        assert world != null;
        return new CraftingStationContainer(this, i, playerInventory, this.inventory.orElse(new ItemStackHandler(CraftingStationContainer.SLOTS)));
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
        return new StringTextComponent("Crafting Station");
    }
}