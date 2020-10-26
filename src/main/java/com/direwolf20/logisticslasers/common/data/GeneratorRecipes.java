package com.direwolf20.logisticslasers.common.data;

import net.minecraft.data.*;
import net.minecraft.item.Items;
import net.minecraftforge.common.Tags;

import java.util.function.Consumer;

import static com.direwolf20.logisticslasers.common.items.ModItems.CARD_BLANK;
import static com.direwolf20.logisticslasers.common.items.ModItems.CARD_EXTRACTOR;

public class GeneratorRecipes extends RecipeProvider {
    public GeneratorRecipes(DataGenerator generator) {
        super(generator);
    }

    /**
     * This is basically just a code version of the json file meaning you type less and generate more. To use
     * Tags use Tags and to specific normal Items use their Items class. A Criterion is what the game will
     * use to see if you can make that recipe. I've been pretty lazy and just done the higher tier ones
     * for now. Hopefully this should mean we write less json in the long run :D
     */
    @Override
    protected void registerRecipes(Consumer<IFinishedRecipe> consumer) {
        ShapedRecipeBuilder.shapedRecipe(CARD_BLANK.get())
                .key('g', Tags.Items.NUGGETS_GOLD)
                .key('p', Items.PAPER)
                .key('r', Tags.Items.DUSTS_REDSTONE)
                .key('l', Tags.Items.GEMS_LAPIS)
                .key('q', Tags.Items.GEMS_QUARTZ)
                .patternLine("rlr")
                .patternLine("qpq")
                .patternLine("ggg")
                .addCriterion("has_quartz", hasItem(Items.QUARTZ))
                .build(consumer);
        ShapelessRecipeBuilder.shapelessRecipe(CARD_EXTRACTOR.get())
                .addIngredient(Items.HOPPER)
                .addIngredient(CARD_BLANK.get())
                .addCriterion("has_card_blank", hasItem(CARD_BLANK.get()))
                .build(consumer);
    }
}
