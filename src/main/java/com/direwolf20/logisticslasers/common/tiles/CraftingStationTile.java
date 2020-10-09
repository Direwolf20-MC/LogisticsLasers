package com.direwolf20.logisticslasers.common.tiles;

import com.direwolf20.logisticslasers.common.blocks.ModBlocks;
import com.direwolf20.logisticslasers.common.container.CraftingStationContainer;
import com.direwolf20.logisticslasers.common.network.PacketHandler;
import com.direwolf20.logisticslasers.common.network.packets.PacketUpdateCraftingRecipe;
import com.direwolf20.logisticslasers.common.tiles.basetiles.NodeTileBase;
import com.direwolf20.logisticslasers.common.util.CraftingStationInventory;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.CraftResultInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.GameRules;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.hooks.BasicEventHooks;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.function.Consumer;

public class CraftingStationTile extends NodeTileBase implements INamedContainerProvider {
    /**
     * Last crafted crafting recipe
     */
    @Nullable
    private ICraftingRecipe lastRecipe;

    public final CraftingStationInventory craftMatrix = new CraftingStationInventory(new ItemStackHandler(9), 3, 3);
    public final CraftResultInventory craftResult = new CraftResultInventory();
    private HashMap<BlockPos, ArrayList<BlockPos>> routeList = new HashMap<>();

    private LazyOptional<ItemStackHandler> inventory = LazyOptional.of(() -> new ItemStackHandler(CraftingStationContainer.SLOTS));

    public CraftingStationTile() {
        super(ModBlocks.CRAFTING_STATION_TILE.get());
    }

    public ItemStack calcResult() {
        if (this.world == null) {
            return ItemStack.EMPTY;
        }
        // assume empty unless we learn otherwise
        ItemStack result = ItemStack.EMPTY;
        for (int i = 0; i < 9; i++) {
            craftMatrix.setInventorySlotContents(i, getInventoryStacks().getStackInSlot(27 + i));
        }
        if (!this.world.isRemote && this.world.getServer() != null) {
            RecipeManager manager = this.world.getServer().getRecipeManager();

            // first, try the cached recipe
            ICraftingRecipe recipe = lastRecipe;
            // if it does not match, find a new recipe
            if (recipe == null || !recipe.matches(this.craftMatrix, this.world)) {
                recipe = manager.getRecipe(IRecipeType.CRAFTING, this.craftMatrix, this.world).orElse(null);
            }

            // if we have a recipe, fetch its result
            if (recipe != null) {
                result = recipe.getCraftingResult(this.craftMatrix);
                // sync if the recipe is different
                if (recipe != lastRecipe) {
                    this.lastRecipe = recipe;
                    this.syncToRelevantPlayers(this::syncRecipe);
                }
            }
        } else if (this.lastRecipe != null && this.lastRecipe.matches(this.craftMatrix, this.world)) {
            result = this.lastRecipe.getCraftingResult(this.craftMatrix);
        }
        craftResult.setInventorySlotContents(0, result);
        return result;
    }

    /**
     * Sends a packet to all players with this container open
     */
    public void syncToRelevantPlayers(Consumer<PlayerEntity> action) {
        if (this.world == null || this.world.isRemote) {
            return;
        }

        this.world.getPlayers().stream()
                // sync if they are viewing this tile
                .filter(player -> {
                    if (player.openContainer instanceof CraftingStationContainer) {
                        return ((CraftingStationContainer) player.openContainer).tile == this;
                    }
                    return false;
                })
                // send packets
                .forEach(action);
    }

    public void syncRecipe(PlayerEntity player) {
        // must have a last recipe and a server world
        if (this.lastRecipe != null && this.world != null && !this.world.isRemote && player instanceof ServerPlayerEntity) {
            PacketHandler.sendTo(new PacketUpdateCraftingRecipe(this.pos, this.lastRecipe), (ServerPlayerEntity) player);
        }
    }

    public ItemStack onCraft(PlayerEntity player, ItemStack result, int amount) {
        if (this.world == null || amount == 0) {
            return ItemStack.EMPTY;
        }

        // check if the player has access to the result
        if (player instanceof ServerPlayerEntity) {
            if (this.lastRecipe != null) {
                // if the player cannot craft this, block crafting
                if (!this.lastRecipe.isDynamic() && world.getGameRules().getBoolean(GameRules.DO_LIMITED_CRAFTING) && !((ServerPlayerEntity) player).getRecipeBook().isUnlocked(this.lastRecipe)) {
                    return ItemStack.EMPTY;
                }
                // unlock the recipe if it was not unlocked
                if (this.lastRecipe != null && !this.lastRecipe.isDynamic()) {
                    player.unlockRecipes(Collections.singleton(this.lastRecipe));
                }
            }

            // fire crafting events
            result.onCrafting(this.world, player, amount);
            BasicEventHooks.firePlayerCraftingEvent(player, result, this.craftMatrix);
        }

        // update all slots in the inventory
        // remove remaining items
        ForgeHooks.setCraftingPlayer(player);
        NonNullList<ItemStack> remaining = this.lastRecipe.getRemainingItems(craftMatrix);
        ForgeHooks.setCraftingPlayer(null);
        for (int i = 0; i < remaining.size(); ++i) {
            ItemStack original = getInventoryStacks().getStackInSlot(i + 27);
            ItemStack newStack = remaining.get(i);

            // if the slot contains a stack, decrease by 1
            if (!original.isEmpty()) {
                original.shrink(1);
            }

            // if we have a new item, try merging it in
            if (!newStack.isEmpty()) {
                // if empty, set directly
                if (original.isEmpty()) {
                    this.setInventorySlotContents(i, newStack);
                } else if (ItemStack.areItemsEqual(original, newStack) && ItemStack.areItemStackTagsEqual(original, newStack)) {
                    // if matching, merge
                    newStack.grow(original.getCount());
                    this.setInventorySlotContents(i, newStack);
                } else {
                    // otherwise, drop the item as the player
                    if (!player.inventory.addItemStackToInventory(newStack)) {
                        player.dropItem(newStack, false);
                    }
                }
            }
        }
        getInventoryStacks().setStackInSlot(0, result);
        return result;
    }

    public void setInventorySlotContents(int slot, ItemStack itemstack) {
        // clear the crafting result when the matrix changes so we recalculate the result
        this.craftResult.clear();
    }

    public void updateRecipe(ICraftingRecipe recipe) {
        this.lastRecipe = recipe;
        this.craftResult.clear();
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