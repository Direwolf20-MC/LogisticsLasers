package com.direwolf20.logisticslasers.client.jei;

import com.direwolf20.logisticslasers.common.container.CraftingStationContainer;
import com.direwolf20.logisticslasers.common.network.PacketHandler;
import com.direwolf20.logisticslasers.common.network.packets.PacketFilterSlot;
import com.direwolf20.logisticslasers.common.util.ItemHandlerUtil;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiIngredient;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class CraftingStationRecipeTransferHandler implements IRecipeTransferHandler<CraftingStationContainer> {
    @Override
    public Class<CraftingStationContainer> getContainerClass() {
        return CraftingStationContainer.class;
    }

    @Nullable
    @Override
    public IRecipeTransferError transferRecipe(CraftingStationContainer container, Object recipe, IRecipeLayout recipeLayout, PlayerEntity player, boolean maxTransfer, boolean doTransfer) {
        if (doTransfer) {
            Map<Integer, ? extends IGuiIngredient<ItemStack>> guiIngredients = recipeLayout.getItemStacks().getGuiIngredients();
            boolean hasController = container.tile.hasController();
            ItemHandlerUtil.InventoryCounts inventoryCounts = new ItemHandlerUtil.InventoryCounts();
            if (hasController)
                inventoryCounts = container.tile.getControllerTE().getItemCounts();
            for (Map.Entry<Integer, ? extends IGuiIngredient<ItemStack>> entry : guiIngredients.entrySet()) {
                int recipeSlot = entry.getKey();
                List<ItemStack> allIngredients = entry.getValue().getAllIngredients();
                if (!allIngredients.isEmpty()) {
                    if (recipeSlot != 0) { // skip the output slot
                        if (hasController) {
                            int amt = 0;
                            ItemStack mostStack = allIngredients.get(0);
                            for (ItemStack stack : allIngredients) {
                                if (inventoryCounts.getCount(stack) > amt) {
                                    amt = inventoryCounts.getCount(stack);
                                    mostStack = stack.copy();
                                }
                            }
                            PacketHandler.sendToServer(new PacketFilterSlot(26 + recipeSlot, mostStack, mostStack.getCount()));
                        } else {
                            ItemStack firstIngredient = allIngredients.get(0);
                            PacketHandler.sendToServer(new PacketFilterSlot(26 + recipeSlot, firstIngredient, firstIngredient.getCount()));
                        }
                    }
                } else { //If theres no recipe ingredient here, clear the slot
                    PacketHandler.sendToServer(new PacketFilterSlot(26 + recipeSlot, ItemStack.EMPTY, 0));
                }
            }
        }

        return null;
    }
}
