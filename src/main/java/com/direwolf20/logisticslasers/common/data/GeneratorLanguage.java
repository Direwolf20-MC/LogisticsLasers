package com.direwolf20.logisticslasers.common.data;

import com.direwolf20.logisticslasers.LogisticsLasers;
import com.direwolf20.logisticslasers.common.blocks.ModBlocks;
import com.direwolf20.logisticslasers.common.items.ModItems;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.LanguageProvider;

public class GeneratorLanguage extends LanguageProvider {
    public GeneratorLanguage(DataGenerator gen) {
        super(gen, LogisticsLasers.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        addBlock(ModBlocks.CONTROLLER, "Laser Controller");
        addBlock(ModBlocks.BASIC_NODE, "Basic Node");
        addBlock(ModBlocks.INVENTORY_NODE, "Inventory Node");

        addItem(ModItems.WRENCH, "Laser Wrench");
        addItem(ModItems.CARD_EXTRACTOR, "Extractor Module");
        addItem(ModItems.CARD_INSERTER, "Inserter Module");

        add("block.logisticslasers.controllerscreen", "Laser Controller");
        add("item.logisticslasers.basicfilterscreen", "Basic Filter");
        add("screen.logisticslasers.energy", "Energy: %s/%s FE");
        add("block.logisticslasers.inventorynodescreen", "Inventory Node");
        add("message.logisticslasers.controllerat", "Controller Located at: %d");
        add("message.logisticslasers.connections", "Connected to: %d");
        add("message.logisticslasers.connectionmade", "Connection made to: %d");
        add("message.logisticslasers.connectionfailed", "Connection failed to: %d");
    }
}
