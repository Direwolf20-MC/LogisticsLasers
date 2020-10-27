package com.direwolf20.logisticslasers.common.data;

import net.minecraft.data.*;
import net.minecraft.item.Items;
import net.minecraftforge.common.Tags;

import java.util.function.Consumer;

import static com.direwolf20.logisticslasers.common.items.ModItems.*;

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
        ShapedRecipeBuilder.shapedRecipe(CARD_INSERTER.get())
                .key('g', Tags.Items.GLASS_PANES)
                .key('b', CARD_BLANK.get())
                .key('i', Items.IRON_BARS)
                .patternLine("igi")
                .patternLine("gbg")
                .patternLine("igi")
                .addCriterion("has_card_blank", hasItem(CARD_BLANK.get()))
                .build(consumer);
        ShapelessRecipeBuilder.shapelessRecipe(CARD_PROVIDER.get())
                .addIngredient(Items.OBSERVER)
                .addIngredient(CARD_BLANK.get())
                .addCriterion("has_card_blank", hasItem(CARD_BLANK.get()))
                .build(consumer);
        ShapelessRecipeBuilder.shapelessRecipe(CARD_STOCKER.get())
                .addIngredient(Items.ENDER_EYE)
                .addIngredient(CARD_BLANK.get())
                .addCriterion("has_card_blank", hasItem(CARD_BLANK.get()))
                .build(consumer);
        ShapelessRecipeBuilder.shapelessRecipe(CARD_INSERTER_TAG.get())
                .addIngredient(Items.BOOK)
                .addIngredient(CARD_INSERTER.get())
                .addCriterion("has_card_inserter", hasItem(CARD_INSERTER.get()))
                .build(consumer);
        ShapedRecipeBuilder.shapedRecipe(CARD_INSERTER_MOD.get())
                .key('g', Tags.Items.GLASS_PANES)
                .key('b', CARD_INSERTER.get())
                .key('i', Tags.Items.DUSTS_GLOWSTONE)
                .patternLine("igi")
                .patternLine("gbg")
                .patternLine("igi")
                .addCriterion("has_card_inserter", hasItem(CARD_INSERTER.get()))
                .build(consumer);
        ShapedRecipeBuilder.shapedRecipe(CARD_POLYMORPH.get())
                .key('g', Tags.Items.GLASS_PANES)
                .key('b', CARD_INSERTER.get())
                .key('i', Items.CLAY_BALL)
                .patternLine("igi")
                .patternLine("gbg")
                .patternLine("igi")
                .addCriterion("has_card_inserter", hasItem(CARD_INSERTER.get()))
                .build(consumer);
    }

}
