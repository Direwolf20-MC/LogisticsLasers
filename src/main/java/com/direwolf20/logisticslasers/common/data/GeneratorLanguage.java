package com.direwolf20.logisticslasers.common.data;

import com.direwolf20.logisticslasers.LogisticsLasers;
import com.direwolf20.logisticslasers.common.blocks.ModBlocks;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.LanguageProvider;

public class GeneratorLanguage extends LanguageProvider {
    public GeneratorLanguage(DataGenerator gen) {
        super(gen, LogisticsLasers.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        addBlock(ModBlocks.CONTROLLER, "Laser Controller");

        //addItem(ModItems.FOCUS_T1, "Basic Focus Crystal");

        add("block.logisticslasers.controllerscreen", "Laser Controller");
        add("screen.logisticslasers.energy", "Energy: %s/%s FE");
    }
}
