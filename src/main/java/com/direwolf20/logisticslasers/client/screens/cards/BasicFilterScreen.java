package com.direwolf20.logisticslasers.client.screens.cards;

import com.direwolf20.logisticslasers.LogisticsLasers;
import com.direwolf20.logisticslasers.common.container.BasicFilterContainer;
import com.direwolf20.logisticslasers.common.container.customslot.BasicFilterSlot;
import com.direwolf20.logisticslasers.common.network.PacketHandler;
import com.direwolf20.logisticslasers.common.network.packets.PacketFilterSlot;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.awt.*;

public class BasicFilterScreen extends ContainerScreen<BasicFilterContainer> {
    private static final ResourceLocation background = new ResourceLocation(LogisticsLasers.MOD_ID, "textures/gui/basicfilterscreen.png");

    protected final BasicFilterContainer container;

    public BasicFilterScreen(BasicFilterContainer container, PlayerInventory playerInventory, ITextComponent title) {
        super(container, playerInventory, title);
        this.container = container;
    }

    public ResourceLocation getBackground() {
        return background;
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(stack);
        super.render(stack, mouseX, mouseY, partialTicks);
        this.func_230459_a_(stack, mouseX, mouseY); // @mcp: func_230459_a_ = renderHoveredToolTip
    }

    @Override
    public void init() {
        super.init();
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack stack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.color4f(1, 1, 1, 1);
        getMinecraft().getTextureManager().bindTexture(getBackground());
        this.blit(stack, guiLeft, guiTop, 0, 0, xSize, ySize);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(MatrixStack stack, int mouseX, int mouseY) {
        Minecraft.getInstance().fontRenderer.drawString(stack, I18n.format("item.logisticslasers.basicfilterscreen"), 50, 5, Color.DARK_GRAY.getRGB());
    }

    protected static TranslationTextComponent getTrans(String key, Object... args) {
        return new TranslationTextComponent(LogisticsLasers.MOD_ID + "." + key, args);
    }

    @Override
    public boolean mouseClicked(double x, double y, int btn) {
        if (hoveredSlot == null || !(hoveredSlot instanceof BasicFilterSlot))
            return super.mouseClicked(x, y, btn);

        // By splitting the stack we can get air easily :) perfect removal basically
        ItemStack stack = getMinecraft().player.inventory.getItemStack();
        stack = stack.copy().split(hoveredSlot.getSlotStackLimit()); // Limit to slot limit
        hoveredSlot.putStack(stack); // Temporarily update the client for continuity purposes

        PacketHandler.sendToServer(new PacketFilterSlot(hoveredSlot.slotNumber, stack));
        return true;
    }

    @Override
    public boolean mouseReleased(double x, double y, int btn) {
        if (hoveredSlot == null || !(hoveredSlot instanceof BasicFilterSlot))
            return super.mouseReleased(x, y, btn);

        return true;
    }

    @Override
    public boolean mouseScrolled(double x, double y, double amt) {
        if (hoveredSlot == null || !(hoveredSlot instanceof BasicFilterSlot))
            return super.mouseScrolled(x, y, amt);

        return true;
    }
}
