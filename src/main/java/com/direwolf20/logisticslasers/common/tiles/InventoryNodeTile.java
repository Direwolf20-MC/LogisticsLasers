package com.direwolf20.logisticslasers.common.tiles;

import com.direwolf20.logisticslasers.common.blocks.ModBlocks;
import com.direwolf20.logisticslasers.common.container.InventoryNodeContainer;
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
import java.util.Set;

public class InventoryNodeTile extends NodeTileBase implements INamedContainerProvider {

    private HashMap<BlockPos, ArrayList<BlockPos>> routeList = new HashMap<>();

    private LazyOptional<ItemStackHandler> inventory = LazyOptional.of(() -> new ItemStackHandler(InventoryNodeContainer.SLOTS));

    public InventoryNodeTile() {
        super(ModBlocks.INVENTORY_NODE_TILE.get());
    }


    @Override
    public void addToController() {
        ControllerTile te = (ControllerTile) world.getTileEntity(controllerPos);
        te.addToInvNodes(pos);
    }

    @Override
    public void removeFromController() {
        ControllerTile te = (ControllerTile) world.getTileEntity(controllerPos);
        te.removeFromInvNodes(pos);
    }

    public ItemStackHandler getInventoryStacks() {
        ItemStackHandler handler = inventory.orElse(new ItemStackHandler(InventoryNodeContainer.SLOTS));
        return handler;
    }

    public void findRoutes() {
        routeList.clear();
        ControllerTile te = getControllerTE();
        if (te == null) return;
        Set<BlockPos> todoList = te.getInventoryNodes();
        for (BlockPos pos : todoList) {
            routeList.put(pos, findRouteToPos(pos));
        }
    }

    public ArrayList<BlockPos> findRouteToPos(BlockPos pos) {
        ArrayList<BlockPos> route = new ArrayList<>();
        Set<BlockPos> connections = getConnectedNodes();
        double testDistance = 10000;
        for (BlockPos testPos : connections) {
            double distance = pos.distanceSq(testPos);
            if (distance < testDistance) testDistance = distance;
        }
        return route;
    }

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        assert world != null;
        return new InventoryNodeContainer(this, i, playerInventory, this.inventory.orElse(new ItemStackHandler(InventoryNodeContainer.SLOTS)));
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