package com.direwolf20.logisticslasers.common.tiles;

import com.direwolf20.logisticslasers.common.blocks.ModBlocks;
import com.direwolf20.logisticslasers.common.container.CraftingStationContainer;
import com.direwolf20.logisticslasers.common.container.customhandler.CraftingStationHandler;
import com.direwolf20.logisticslasers.common.tiles.basetiles.NodeTileBase;
import com.direwolf20.logisticslasers.common.util.CraftingStationInventory;
import com.direwolf20.logisticslasers.common.util.ItemHandlerUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.GameRules;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.hooks.BasicEventHooks;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CraftingStationTile extends NodeTileBase implements INamedContainerProvider {
    /**
     * Last crafted crafting recipe
     */
    @Nullable
    public ICraftingRecipe lastRecipe;

    public CraftingStationHandler craftMatrixHandler = new CraftingStationHandler(9, this);
    public final CraftingStationInventory craftMatrix = new CraftingStationInventory(craftMatrixHandler, 3, 3);
    public final CraftingStationInventory fakecraftMatrix = new CraftingStationInventory(craftMatrixHandler, 3, 3); //Used to track items ACTUALLY used to do the craft
    public final ItemStackHandler craftResult = new ItemStackHandler(1);
    private LazyOptional<ItemStackHandler> inventory = LazyOptional.of(() -> new ItemStackHandler(27));
    //private Int2ObjectMap<Set<ItemStack>> alternateIngredients = new Int2ObjectOpenHashMap<>();

    public CraftingStationTile() {
        super(ModBlocks.CRAFTING_STATION_TILE.get());
    }

    public void calcResult() {
        if (this.world == null) {
            return;
        }

        if (this.world.isRemote || this.world.getServer() == null) return;

        // assume empty unless we learn otherwise
        ItemStack result = ItemStack.EMPTY;
        RecipeManager manager = this.world.getServer().getRecipeManager(); //Get the server recipe list i think

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
                craftResult.setStackInSlot(0, result);
                this.lastRecipe = recipe;
                //findAlternateRecipes(recipe, manager);
                markDirtyClient();
            }
        } else {
            //If the recipe is not valid, clear the last recipe and output slot.
            this.lastRecipe = null;
            craftResult.setStackInSlot(0, ItemStack.EMPTY);
        }
    }

    /*public void findAlternateRecipes(ICraftingRecipe recipe, RecipeManager manager) {
        alternateIngredients.clear();
        List<Ingredient> ingredients = recipe.getIngredients();
        for (int i = 0; i < recipe.getIngredients().size(); i++) {
            Ingredient ingredient = ingredients.get(i);
            if (ingredient.getMatchingStacks().length > 1) {
                Set<ItemStack> tempSet = new HashSet<>(Arrays.asList(ingredient.getMatchingStacks()));
                alternateIngredients.put(i, tempSet);
            }
        }
    }*/

    /**
     * Loops through all the recipe slots and looks for a matching item in an inventory slot
     *
     * @param handler            Inventory of this crafter
     * @param itemStacksToremove List of inventory slots to decrement when we find an item we can use, used later when we actually 'do' the craft
     * @return if we found ALL the items needed to craft the result
     */
    private boolean hasAllItems(ItemStackHandler handler, ArrayList<Integer> itemStacksToremove) {
        List<Ingredient> ingredients = lastRecipe.getIngredients().stream().filter(o -> !o.hasNoMatchingItems()).collect(Collectors.toList());
        ItemHandlerUtil.InventoryCounts inventoryCounts = new ItemHandlerUtil.InventoryCounts(handler);
        int ingredientCounter = -1;
        for (int i = 0; i < 9; i++) {
            if (craftMatrix.getStackInSlot(i).isEmpty()) continue; //Skip empty slots in a recipe
            ingredientCounter++;
            Ingredient ingredient = ingredients.get(ingredientCounter);
            if (!hasItem(handler, inventoryCounts, itemStacksToremove, ingredient, i))
                return false;
        }
        return true;
    }

    /**
     * Loops through all the inventory slots of the crafter, looking to match the ingredient passed in
     *
     * @return If we found a match for the ingredient
     */
    private boolean hasItem(ItemStackHandler handler, ItemHandlerUtil.InventoryCounts inventoryCounts, ArrayList<Integer> itemStacksToremove, Ingredient ingredient, int i) {
        for (int k = 0; k < handler.getSlots(); k++) {
            ItemStack stackInSlot = handler.getStackInSlot(k);
            if (ingredient.test(stackInSlot) && inventoryCounts.getCount(stackInSlot) > 0) {
                int alreadyUsed = 0;
                for (Integer slot : itemStacksToremove) {
                    if (slot == k)
                        alreadyUsed++;
                }
                if (handler.getStackInSlot(k).getCount() > alreadyUsed) {
                    itemStacksToremove.add(k);
                    inventoryCounts.removeStack(stackInSlot, 1);
                    this.fakecraftMatrix.setInventorySlotContents(i, stackInSlot);
                    return true;
                }
            }
        }
        return false;
    }

    public ItemStack onCraft(PlayerEntity player, ItemStack result, int amount, boolean bulk) {
        if (this.world == null || amount == 0) {
            return ItemStack.EMPTY;
        }

        ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
        do {
            this.fakecraftMatrix.clear();
            ArrayList<Integer> itemStacksToremove = new ArrayList<>();
            ItemStackHandler handler = getInventoryStacks();
            // check if the player has access to the result
            if (player instanceof ServerPlayerEntity && this.lastRecipe != null) {
                // if the player cannot craft this, block crafting
                if (!this.lastRecipe.isDynamic() && world.getGameRules().getBoolean(GameRules.DO_LIMITED_CRAFTING) && !((ServerPlayerEntity) player).getRecipeBook().isUnlocked(this.lastRecipe)) {
                    return ItemStack.EMPTY;
                }

                //Check if the inventory slots have enough items to craft this.
                if (!hasAllItems(handler, itemStacksToremove))
                    break;

                RecipeManager manager = this.world.getServer().getRecipeManager();
                ICraftingRecipe madeRecipe = manager.getRecipe(IRecipeType.CRAFTING, this.fakecraftMatrix, this.world).orElse(null);

                if (madeRecipe == null)
                    break;

                // unlock the recipe if it was not unlocked
                if (madeRecipe != null && !madeRecipe.isDynamic()) {
                    player.unlockRecipes(Collections.singleton(madeRecipe));
                }


                // fire crafting events
                result.onCrafting(this.world, player, amount);
                BasicEventHooks.firePlayerCraftingEvent(player, result, this.fakecraftMatrix);


                //Try to give the item to the player (On their cursor)
                ItemStack heldItem = serverPlayer.inventory.getItemStack();
                ItemStack resultItem = madeRecipe.getRecipeOutput().copy();
                boolean success = false;
                if (heldItem.isEmpty()) {
                    serverPlayer.inventory.setItemStack(resultItem);
                    success = true;
                } else {
                    if (resultItem.isItemEqual(heldItem) && (resultItem.getCount() + heldItem.getCount() <= resultItem.getMaxStackSize())) {
                        heldItem.grow(resultItem.getCount());
                        success = true;
                    }
                }
                if (!success) break; //If it failed, return without deleting items from contents
                serverPlayer.sendContainerToPlayer(serverPlayer.openContainer); //Update player's client with the itemstack now on the cursor

                ForgeHooks.setCraftingPlayer(player);
                List<ItemStack> remaining = madeRecipe.getRemainingItems(fakecraftMatrix).stream().filter(o -> !o.isEmpty()).collect(Collectors.toList()); //Get remaining items like buckets
                ForgeHooks.setCraftingPlayer(null);
                //Remove items from inventory that we found earlier
                for (Integer slot : itemStacksToremove) {
                    handler.getStackInSlot(slot).shrink(1);
                }
                //Put items into inventory like empty buckets. Drop in world if failed somehow.
                for (ItemStack remainingStack : remaining) {
                    ItemStack postInsert = ItemHandlerHelper.insertItem(handler, remainingStack, false);
                    if (!postInsert.isEmpty()) {
                        Block.spawnAsEntity(world, pos, postInsert);
                    }
                }
            }
        } while (bulk);
        ItemStack heldStack = player.inventory.getItemStack();
        if (bulk && !heldStack.isEmpty()) {
            ItemHandlerHelper.giveItemToPlayer(player, heldStack);
            player.inventory.setItemStack(ItemStack.EMPTY);
            serverPlayer.sendContainerToPlayer(serverPlayer.openContainer); //Update player's client with the itemstack now on the cursor
        }
        return result;
    }

    public boolean requestItem(ItemStack stack, PlayerEntity requestor) {
        ControllerTile te = getControllerTE();
        if (te == null) return false;
        ItemStack returnedStack = te.provideItemStacksToPos(stack, pos);
        te.updateItemCounts((ServerPlayerEntity) requestor);
        return returnedStack.getCount() == 0;
    }

    public boolean requestIngredient(Ingredient ingredient, PlayerEntity requestor) {
        ControllerTile te = getControllerTE();
        if (te == null) return false;
        boolean success = te.findIngredient(ingredient, pos);
        if (!success)
            requestor.sendStatusMessage((new TranslationTextComponent("message.logisticslasers.failedRequest", 1, ingredient.getMatchingStacks()[0].getItem())), false);
        else
            te.updateItemCounts((ServerPlayerEntity) requestor);
        return success;
    }

    public void requestGrid(PlayerEntity requestor, int amt) {
        if (lastRecipe == null) return;
        List<Ingredient> ingredients = lastRecipe.getIngredients().stream().filter(o -> !o.hasNoMatchingItems()).collect(Collectors.toList());
        for (Ingredient ingredient : ingredients) {
            requestIngredient(ingredient, requestor);
        }
    }

    public void clearGrid() {
        for (int i = 0; i < craftMatrixHandler.getSlots(); i++) {
            craftMatrixHandler.setStackInSlot(i, ItemStack.EMPTY);
        }
    }

    public void requestGridOnlyMissing(PlayerEntity requestor) {
        if (lastRecipe == null) return;
        ItemStackHandler handler = getInventoryStacks();
        List<Ingredient> ingredients = lastRecipe.getIngredients().stream().filter(o -> !o.hasNoMatchingItems()).collect(Collectors.toList());
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stackInSlot = handler.getStackInSlot(i);
            int count = stackInSlot.getCount();
            for (Ingredient ingredient : lastRecipe.getIngredients()) {
                if (ingredients.isEmpty()) break;
                if (count == 0) break;
                if (ingredient.test(stackInSlot) && count >= 1) {
                    if (ingredients.remove(ingredient)) {
                        count--;
                    }
                }
            }
        }
        for (Ingredient ingredient : ingredients) {
            requestIngredient(ingredient, requestor);
        }
        /*ItemHandlerUtil.InventoryCounts storageCounts = new ItemHandlerUtil.InventoryCounts(handler);
        for (int i = 0; i < craftMatrixHandler.getSlots(); i++) {
            ItemStack requestStack = craftMatrixHandler.getStackInSlot(i).copy();
            if (requestStack.isEmpty()) continue;
            if (storageCounts.getCount(requestStack) > 0) { //If we have 1 of these items in the inventory already, decrement the storageCount of it and move onto the next
                storageCounts.removeStack(requestStack, 1);
                continue;
            } else { //otherwise request the item
                requestItem(new ItemStack(requestStack.getItem(), 1), requestor, i);
            }
        }*/
    }

    public ItemStackHandler getInventoryStacks() {
        ItemStackHandler handler = inventory.orElse(new ItemStackHandler(27));
        return handler;
    }

    @Override
    public void setControllerPos(BlockPos controllerPos) {
        super.setControllerPos(controllerPos);
        markDirtyClient();
    }

    //Ensure mods and hoppers and such can interact with this inventory - but only the first 27 slots
    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, final @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            return inventory.cast();

        return super.getCapability(cap, side);
    }

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        assert world != null;
        //getAvailableItems();
        return new CraftingStationContainer(this, i, playerInventory, this.inventory.orElse(new ItemStackHandler(27)));
    }

    @Override
    public void read(BlockState state, CompoundNBT tag) {
        super.read(state, tag);
        inventory.ifPresent(h -> h.deserializeNBT(tag.getCompound("inv")));
        craftMatrixHandler.deserializeNBT(tag.getCompound("craftInv"));
        //availableItems.deserializeNBT(tag.getCompound("availableItems"));
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        inventory.ifPresent(h -> tag.put("inv", h.serializeNBT()));
        tag.put("craftInv", craftMatrixHandler.serializeNBT());
        //tag.put("availableItems", availableItems.serializeNBT());
        return super.write(tag);
    }

    @Override
    public ITextComponent getDisplayName() {
        return new StringTextComponent("Crafting Station");
    }
}