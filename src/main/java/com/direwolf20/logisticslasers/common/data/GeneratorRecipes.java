package com.direwolf20.logisticslasers.common.data;

import net.minecraft.data.*;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
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
                .key('p', LOGIC_CHIP.get())
                .key('r', Tags.Items.DUSTS_REDSTONE)
                .key('l', Tags.Items.GEMS_LAPIS)
                .key('q', Tags.Items.GEMS_QUARTZ)
                .patternLine("rlr")
                .patternLine("qpq")
                .patternLine("ggg")
                .addCriterion("has_logic_chip", hasItem(LOGIC_CHIP.get()))
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
        ShapedRecipeBuilder.shapedRecipe(ROUTING_LOGIC_MODULE.get())
                .key('g', Tags.Items.GLASS)
                .key('b', CARD_BLANK.get())
                .key('r', Tags.Items.DUSTS_REDSTONE)
                .key('c', Items.COMPASS)
                .key('l', LOGIC_CHIP.get())
                .patternLine("rgr")
                .patternLine("lbl")
                .patternLine("gcg")
                .addCriterion("has_card_blank", hasItem(CARD_BLANK.get()))
                .build(consumer);
        ShapedRecipeBuilder.shapedRecipe(BASIC_NODE_ITEM.get())
                .key('g', Tags.Items.GLASS)
                .key('b', ROUTING_LOGIC_MODULE.get())
                .key('i', Tags.Items.INGOTS_IRON)
                .patternLine("igi")
                .patternLine("gbg")
                .patternLine("igi")
                .addCriterion("has_routing_module", hasItem(ROUTING_LOGIC_MODULE.get()))
                .build(consumer);
        ShapedRecipeBuilder.shapedRecipe(INVENTORY_NODE_ITEM.get())
                .key('r', Tags.Items.DUSTS_REDSTONE)
                .key('b', BASIC_NODE_ITEM.get())
                .key('i', Tags.Items.INGOTS_IRON)
                .key('d', Tags.Items.GEMS_DIAMOND)
                .patternLine("idi")
                .patternLine("rbr")
                .patternLine("iri")
                .addCriterion("has_basic_node", hasItem(BASIC_NODE_ITEM.get()))
                .build(consumer);
        ShapedRecipeBuilder.shapedRecipe(CONTROLLER_ITEM.get())
                .key('b', BASIC_NODE_ITEM.get())
                .key('g', Tags.Items.INGOTS_GOLD)
                .key('d', Tags.Items.GEMS_DIAMOND)
                .patternLine("gdg")
                .patternLine("dbd")
                .patternLine("gdg")
                .addCriterion("has_basic_node", hasItem(BASIC_NODE_ITEM.get()))
                .build(consumer);
        ShapedRecipeBuilder.shapedRecipe(CRAFTING_STATION_ITEM.get())
                .key('r', Tags.Items.DUSTS_REDSTONE)
                .key('b', BASIC_NODE_ITEM.get())
                .key('i', Tags.Items.INGOTS_IRON)
                .key('d', Items.CRAFTING_TABLE)
                .patternLine("idi")
                .patternLine("rbr")
                .patternLine("iri")
                .addCriterion("has_basic_node", hasItem(BASIC_NODE_ITEM.get()))
                .build(consumer);
        ShapedRecipeBuilder.shapedRecipe(WRENCH.get())
                .key('b', ROUTING_LOGIC_MODULE.get())
                .key('i', Tags.Items.INGOTS_IRON)
                .patternLine("i i")
                .patternLine(" b ")
                .patternLine(" i ")
                .addCriterion("has_routing_module", hasItem(ROUTING_LOGIC_MODULE.get()))
                .build(consumer);
        ShapedRecipeBuilder.shapedRecipe(RAW_LOGIC_CHIP.get())
                .key('r', Tags.Items.DUSTS_REDSTONE)
                .key('q', Items.QUARTZ_BLOCK)
                .key('g', Tags.Items.NUGGETS_GOLD)
                .key('c', Items.CLAY_BALL)
                .patternLine("rgr")
                .patternLine("cqc")
                .patternLine("rgr")
                .addCriterion("has_quartz", hasItem(Items.QUARTZ_BLOCK))
                .build(consumer);
        CookingRecipeBuilder.smeltingRecipe(Ingredient.fromItems(RAW_LOGIC_CHIP.get()), LOGIC_CHIP.get(), 0.1f, 100)
                .addCriterion("has_raw_chip", hasItem(RAW_LOGIC_CHIP.get()))
                .build(consumer);


        //NBT Clearing recipes
        ShapelessRecipeBuilder.shapelessRecipe(CARD_EXTRACTOR.get())
                .addIngredient(CARD_EXTRACTOR.get())
                .addCriterion("has_card_blank", hasItem(CARD_BLANK.get()))
                .build(consumer, "extractor_clear");
    }

}
