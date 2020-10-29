package com.direwolf20.logisticslasers.client.jei;

import com.direwolf20.logisticslasers.LogisticsLasers;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

import static com.direwolf20.logisticslasers.common.items.ModItems.CARD_EXTRACTOR;

@JeiPlugin
public class JEIIntegration implements IModPlugin {

    @Nonnull
    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(LogisticsLasers.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registry) {
        registry.addUniversalRecipeTransferHandler(new CraftingStationRecipeTransferHandler());
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        IRecipeManager recipeRegistry = jeiRuntime.getRecipeManager();
        RecipeManager recipeManager = Minecraft.getInstance().world.getRecipeManager();
        recipeManager.getRecipe(new ResourceLocation(CARD_EXTRACTOR.getId() + "_nbtclear"))
                .ifPresent(r -> recipeRegistry.hideRecipe(r, VanillaRecipeCategoryUid.CRAFTING));

    }
}
