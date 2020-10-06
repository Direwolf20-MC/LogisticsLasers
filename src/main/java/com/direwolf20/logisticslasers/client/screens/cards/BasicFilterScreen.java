package com.direwolf20.logisticslasers.client.screens.cards;

import com.direwolf20.logisticslasers.LogisticsLasers;
import com.direwolf20.logisticslasers.common.container.cards.BasicFilterContainer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class BasicFilterScreen extends BaseFilterScreen<BasicFilterContainer> {
    private static final ResourceLocation background = new ResourceLocation(LogisticsLasers.MOD_ID, "textures/gui/basicfilterscreen.png");

    protected final BasicFilterContainer container;
    private boolean isWhitelist;

    public BasicFilterScreen(BasicFilterContainer container, PlayerInventory playerInventory, ITextComponent title) {
        super(container, playerInventory, title);
        this.container = container;
        isWhitelist = container.isWhiteList();
    }

    public ResourceLocation getBackground() {
        return background;
    }
}
