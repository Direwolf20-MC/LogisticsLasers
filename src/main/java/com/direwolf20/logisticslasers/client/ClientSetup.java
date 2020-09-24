package com.direwolf20.logisticslasers.client;

import com.direwolf20.logisticslasers.client.screens.ControllerScreen;
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
