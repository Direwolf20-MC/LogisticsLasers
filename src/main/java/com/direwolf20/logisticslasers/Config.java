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
    public static ForgeConfigSpec.IntValue PASSIVE_CONTROLLER_COST;

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
        COMMON_BUILDER.comment("Goo Removal Turret Settings").push(SUBCATEGORY_CONTROLLER);
        PASSIVE_CONTROLLER_COST = COMMON_BUILDER.comment("The passive RF/Tick cost of the controller - ")
                .defineInRange("rf_controller_passive", 100, 0, Integer.MAX_VALUE);

        COMMON_BUILDER.pop();
    }
}
