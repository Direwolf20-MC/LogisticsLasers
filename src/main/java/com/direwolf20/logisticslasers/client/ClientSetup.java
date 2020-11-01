package com.direwolf20.logisticslasers.client;

import com.direwolf20.logisticslasers.client.screens.ControllerScreen;
import com.direwolf20.logisticslasers.client.screens.CraftingStationScreen;
import com.direwolf20.logisticslasers.client.screens.InventoryNodeScreen;
import com.direwolf20.logisticslasers.client.screens.cards.BasicFilterScreen;
import com.direwolf20.logisticslasers.client.screens.cards.PolymorphScreen;
import com.direwolf20.logisticslasers.client.screens.cards.StockerFilterScreen;
import com.direwolf20.logisticslasers.client.screens.cards.TagFilterScreen;
import com.direwolf20.logisticslasers.common.blocks.ModBlocks;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;

/**
 * Only put client code here plz.
 */
public final class ClientSetup {
    public static void setup() {
        registerRenderers();
        registerContainerScreens();
        registerTransparentBlocks();
    }

    /**
     * Called from some Client Dist runner in the main class
     */
    private static void registerContainerScreens() {
        ScreenManager.registerFactory(ModBlocks.CONTROLLER_CONTAINER.get(), ControllerScreen::new);
        ScreenManager.registerFactory(ModBlocks.INVENTORY_NODE_CONTAINER.get(), InventoryNodeScreen::new);
        ScreenManager.registerFactory(ModBlocks.BASIC_FILTER_CONTAINER.get(), BasicFilterScreen::new);
        ScreenManager.registerFactory(ModBlocks.STOCKER_FILTER_CONTAINER.get(), StockerFilterScreen::new);
        ScreenManager.registerFactory(ModBlocks.CRAFTING_STATION_CONTAINER.get(), CraftingStationScreen::new);
        ScreenManager.registerFactory(ModBlocks.TAG_FILTER_CONTAINER.get(), TagFilterScreen::new);
        ScreenManager.registerFactory(ModBlocks.POLY_FILTER_CONTAINER.get(), PolymorphScreen::new);
    }

    /**
     * Client Registry for renders
     */
    private static void registerRenderers() {
        //ClientRegistry.bindTileEntityRenderer(ModBlocks.TURRETBLOCK_TILE.get(), TurretBlockTileEntityRender::new);
    }

    /**
     * Setup transparent blocks
     */
    private static void registerTransparentBlocks() {
        RenderTypeLookup.setRenderLayer(ModBlocks.BASIC_NODE.get(), RenderType.getCutout());
    }
}
