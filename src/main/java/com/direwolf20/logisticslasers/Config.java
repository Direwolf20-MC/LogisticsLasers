package com.direwolf20.logisticslasers;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class Config {
    public static final String CATEGORY_GENERAL = "general";

    private static final ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();
    private static final ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();

    public static ForgeConfigSpec COMMON_CONFIG;
    public static ForgeConfigSpec CLIENT_CONFIG;

    public static final String CATEGORY_RFCOSTS = "rfcosts";
    public static final String SUBCATEGORY_CONTROLLER = "rf_controller";
    public static ForgeConfigSpec.IntValue CONTROLLER_PASSIVE;
    public static ForgeConfigSpec.IntValue CONTROLLER_INTERNAL;
    public static ForgeConfigSpec.IntValue CONTROLLER_INTERNAL_REMOVE;
    public static ForgeConfigSpec.IntValue CONTROLLER_EXTRACTOR;
    public static ForgeConfigSpec.IntValue CONTROLLER_STOCKER;
    public static final String SUBCATEGORY_CRAFTING_STATION = "rf_crafting_station";
    public static ForgeConfigSpec.IntValue CRAFTING_STATION_REQUEST;

    static {

        COMMON_BUILDER.comment("General settings").push(CATEGORY_GENERAL);
        setupGeneralConfig();
        COMMON_BUILDER.pop();

        COMMON_BUILDER.comment("RF SETTINGS").push(CATEGORY_RFCOSTS);
        setupRFCostConfig();
        COMMON_BUILDER.pop();

        COMMON_CONFIG = COMMON_BUILDER.build();
        CLIENT_CONFIG = CLIENT_BUILDER.build();
    }

    private static void setupGeneralConfig() {

    }

    private static void setupRFCostConfig() {
        COMMON_BUILDER.comment("Controller").push(SUBCATEGORY_CONTROLLER);
        CONTROLLER_PASSIVE = COMMON_BUILDER.comment("The passive RF/Tick cost of the controller")
                .defineInRange("rf_controller_passive", 100, 0, Integer.MAX_VALUE);
        CONTROLLER_INTERNAL = COMMON_BUILDER.comment("The RF cost per item to store items in the controller's internal inventory.")
                .defineInRange("rf_controller_internal", 10, 0, Integer.MAX_VALUE);
        CONTROLLER_INTERNAL_REMOVE = COMMON_BUILDER.comment("The RF cost per item to remove an item from the internal stored inventory")
                .defineInRange("rf_controller_internal_remove", 50, 0, Integer.MAX_VALUE);
        CONTROLLER_EXTRACTOR = COMMON_BUILDER.comment("The RF cost per item to extract items from any inventory using extractor cards")
                .defineInRange("rf_controller_extractor", 10, 0, Integer.MAX_VALUE);
        CONTROLLER_STOCKER = COMMON_BUILDER.comment("The RF cost per item to stock an item in an inventory via the stocker cards")
                .defineInRange("rf_controller_stocker", 20, 0, Integer.MAX_VALUE);
        COMMON_BUILDER.pop();

        COMMON_BUILDER.comment("Crafting Station").push(SUBCATEGORY_CRAFTING_STATION);
        CRAFTING_STATION_REQUEST = COMMON_BUILDER.comment("The RF cost per item when requesting from a crafting station")
                .defineInRange("rf_crafting_station_request", 10, 0, Integer.MAX_VALUE);
        COMMON_BUILDER.pop();
    }
}
