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
        addBlock(ModBlocks.CRAFTING_STATION, "Crafting Station");
        addBlock(ModBlocks.BASIC_NODE, "Basic Node");
        addBlock(ModBlocks.INVENTORY_NODE, "Inventory Node");

        addItem(ModItems.RAW_LOGIC_CHIP, "Raw Logic Chip");
        addItem(ModItems.LOGIC_CHIP, "Logic Chip");
        addItem(ModItems.WRENCH, "Laser Wrench");
        addItem(ModItems.CARD_BLANK, "Blank Module");
        addItem(ModItems.ROUTING_LOGIC_MODULE, "Routing Logic Module");
        addItem(ModItems.CARD_EXTRACTOR, "Extractor Module");
        addItem(ModItems.CARD_INSERTER, "Inserter Module");
        addItem(ModItems.CARD_INSERTER_MOD, "Mod Inserter Module");
        addItem(ModItems.CARD_INSERTER_TAG, "Tag Inserter Module");
        addItem(ModItems.CARD_STOCKER, "Stocker Module");
        addItem(ModItems.CARD_PROVIDER, "Provider Module");
        addItem(ModItems.CARD_POLYMORPH, "Polymorph Module");

        add("block.logisticslasers.controllerscreen", "Laser Controller");
        add("block.logisticslasers.craftingstationscreen", "Crafting Station");
        add("item.logisticslasers.basicfilterscreen", "Basic Filter");
        add("item.logisticslasers.stockerfilterscreen", "Stocker Filter");
        add("item.logisticslasers.polyfilterscreen", "Polymorph Filter");
        add("item.logisticslasers.tagfilterscreen", "Tag Filter");


        add("item.logisticslasers.basicfilterscreen.priority", "Priority: %d");

        add("screen.logisticslasers.energy", "Energy: %s/%s FE");
        add("screen.logisticslasers.fepertick", "FE/T: %s FE");
        add("screen.logisticslasers.add", "Add");
        add("screen.logisticslasers.remove", "Remove");
        add("screen.logisticslasers.clear", "Clear");
        add("screen.logisticslasers.set", "Set");
        add("screen.logisticslasers.refresh", "Refresh");
        add("screen.logisticslasers.request", "Request");
        add("screen.logisticslasers.nbt", "NBT Match");
        add("screen.logisticslasers.nonbt", "No NBT Match");
        add("screen.logisticslasers.whitelist", "Whitelist");
        add("screen.logisticslasers.blacklist", "Blacklist");
        add("screen.logisticslasers.extractamt", "Extract");


        add("block.logisticslasers.inventorynodescreen", "Inventory Node");
        add("message.logisticslasers.controllerat", "Controller Located at: %d");
        add("message.logisticslasers.connections", "Connected to: %d");
        add("message.logisticslasers.connectionmade", "Connection made to: %d");
        add("message.logisticslasers.connectionfailed", "Connection failed to: %d");
        add("message.logisticslasers.failedRequest", "Failed to request %d %d");
        add("message.logisticslasers.wrenchrange", "Connection exceeds maximum range of %d");

    }
}
