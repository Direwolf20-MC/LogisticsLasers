package com.direwolf20.logisticslasers.common.data;

import com.direwolf20.logisticslasers.LogisticsLasers;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

@Mod.EventBusSubscriber(modid = LogisticsLasers.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Generator {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        if (event.includeServer())
            registerServerProviders(event.getGenerator(), event);

        if (event.includeClient())
            registerClientProviders(event.getGenerator(), event);
    }

    private static void registerServerProviders(DataGenerator generator, GatherDataEvent event) {
        ExistingFileHelper helper = event.getExistingFileHelper();
        generator.addProvider(new GeneratorLoots(generator));
        generator.addProvider(new GeneratorBlockTags(generator, helper));
        generator.addProvider(new GeneratorRecipes(generator));
    }

    private static void registerClientProviders(DataGenerator generator, GatherDataEvent event) {
        ExistingFileHelper helper = event.getExistingFileHelper();

        generator.addProvider(new GeneratorBlockStates(generator, helper));
        generator.addProvider(new GeneratorItemModels(generator, helper));
        generator.addProvider(new GeneratorLanguage(generator));
    }
}
