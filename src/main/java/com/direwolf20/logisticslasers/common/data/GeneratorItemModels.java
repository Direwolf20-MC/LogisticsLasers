package com.direwolf20.logisticslasers.common.data;

import com.direwolf20.logisticslasers.LogisticsLasers;
import com.direwolf20.logisticslasers.common.blocks.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

public class GeneratorItemModels extends ItemModelProvider {
    public GeneratorItemModels(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, LogisticsLasers.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        // Our block items
        registerBlockModel(ModBlocks.CONTROLLER.get());
        registerBlockModel(ModBlocks.BASIC_NODE.get());

    }

    private void registerBlockModel(Block block) {
        String path = block.getRegistryName().getPath();
        getBuilder(path).parent(new ModelFile.UncheckedModelFile(modLoc("block/" + path)));
    }

    private void registerBasicItem(Item item) {
        String path = item.getRegistryName().getPath();
        singleTexture(path, mcLoc("item/handheld"), "layer0", modLoc("item/" + path));
    }

    @Override
    public String getName() {
        return "Item Models";
    }
}