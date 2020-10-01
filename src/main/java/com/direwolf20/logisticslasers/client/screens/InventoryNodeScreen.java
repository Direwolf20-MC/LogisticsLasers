package com.direwolf20.logisticslasers.client.screens;

import com.direwolf20.logisticslasers.LogisticsLasers;
import com.direwolf20.logisticslasers.common.container.InventoryNodeContainer;
import com.direwolf20.logisticslasers.common.network.PacketHandler;
import com.direwolf20.logisticslasers.common.network.packets.PacketOpenFilter;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.awt.*;

public class InventoryNodeScreen extends ContainerScreen<InventoryNodeContainer> {
    private static final ResourceLocation background = new ResourceLocation(LogisticsLasers.MOD_ID, "textures/gui/inventorynode.png");

    protected final InventoryNodeContainer container;

    public InventoryNodeScreen(InventoryNodeContainer container, PlayerInventory playerInventory, ITextComponent title) {
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
        Minecraft.getInstance().fontRenderer.drawString(stack, I18n.format("block.logisticslasers.inventorynodescreen"), 50, 5, Color.DARK_GRAY.getRGB());
    }

    protected static TranslationTextComponent getTrans(String key, Object... args) {
        return new TranslationTextComponent(LogisticsLasers.MOD_ID + "." + key, args);
    }

    @Override
    public boolean mouseClicked(double x, double y, int btn) {
        if (hoveredSlot == null || hoveredSlot.getStack().isEmpty())
            return super.mouseClicked(x, y, btn);

        if (btn == 1) { //Right click
            int slot = hoveredSlot.slotNumber;

            PacketHandler.sendToServer(new PacketOpenFilter(hoveredSlot.slotNumber));
            //FilterSlotHandler handler = getInventory(itemstack);
            /*NetworkHooks.openGui((ServerPlayerEntity) getMinecraft().player, new SimpleNamedContainerProvider(
                    (windowId, playerInventory, playerEntity) -> new BasicFilterContainer(itemstack, windowId, playerInventory, handler), new StringTextComponent("")));*/
            return true;
        }
        return super.mouseClicked(x, y, btn);
    }

    @Override
    public boolean mouseReleased(double x, double y, int btn) {
        if (hoveredSlot == null)
            return super.mouseReleased(x, y, btn);

        return super.mouseReleased(x, y, btn);
    }
}
