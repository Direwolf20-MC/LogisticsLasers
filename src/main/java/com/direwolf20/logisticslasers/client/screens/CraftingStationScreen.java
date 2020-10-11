package com.direwolf20.logisticslasers.client.screens;

import com.direwolf20.logisticslasers.LogisticsLasers;
import com.direwolf20.logisticslasers.common.container.CraftingStationContainer;
import com.direwolf20.logisticslasers.common.container.customslot.BasicFilterSlot;
import com.direwolf20.logisticslasers.common.container.customslot.CraftingSlot;
import com.direwolf20.logisticslasers.common.network.PacketHandler;
import com.direwolf20.logisticslasers.common.network.packets.PacketDoCraft;
import com.direwolf20.logisticslasers.common.network.packets.PacketFilterSlot;
import com.direwolf20.logisticslasers.common.network.packets.PacketRequestItem;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CraftingStationScreen extends ContainerScreen<CraftingStationContainer> {
    private static final ResourceLocation background = new ResourceLocation(LogisticsLasers.MOD_ID, "textures/gui/crafting_station.png");

    protected final CraftingStationContainer container;

    public CraftingStationScreen(CraftingStationContainer container, PlayerInventory playerInventory, ITextComponent title) {
        super(container, playerInventory, title);
        this.container = container;
        this.xSize = 320;
        this.ySize = 256;
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
        List<Widget> leftWidgets = new ArrayList<>();


        Button requestTest;
        leftWidgets.add(requestTest = new Button(guiLeft + 2, guiTop + 25, 15, 10, new StringTextComponent("requestTest"), (button) -> {
            PacketHandler.sendToServer(new PacketRequestItem(new ItemStack(Items.DIAMOND), 5));
        }));


        // Lay the buttons out, too lazy to figure out the math every damn time.
        // Ordered by where you add them.
        for (int i = 0; i < leftWidgets.size(); i++) {
            addButton(leftWidgets.get(i));
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(MatrixStack stack, int mouseX, int mouseY) {
        //super.drawGuiContainerForegroundLayer(stack, mouseX, mouseY);
        Minecraft.getInstance().fontRenderer.drawString(stack, I18n.format("block.logisticslasers.craftingstationscreen"), 125, 5, Color.DARK_GRAY.getRGB());
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack stack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.color4f(1, 1, 1, 1);
        getMinecraft().getTextureManager().bindTexture(getBackground());
        this.blit(stack, guiLeft, guiTop, 0, 0, xSize, ySize, xSize, ySize);
    }

    protected static TranslationTextComponent getTrans(String key, Object... args) {
        return new TranslationTextComponent(LogisticsLasers.MOD_ID + "." + key, args);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (hoveredSlot != null && hoveredSlot instanceof CraftingSlot) {
            PacketHandler.sendToServer(new PacketDoCraft(hoveredSlot.getStack(), hoveredSlot.getStack().getCount(), Screen.hasShiftDown()));
            return true;
        }

        if (hoveredSlot == null || !(hoveredSlot instanceof BasicFilterSlot))
            return super.mouseClicked(mouseX, mouseY, button);

        // By splitting the stack we can get air easily :) perfect removal basically
        ItemStack stack = getMinecraft().player.inventory.getItemStack();
        stack = stack.copy().split(hoveredSlot.getSlotStackLimit()); // Limit to slot limit
        hoveredSlot.putStack(stack); // Temporarily update the client for continuity purposes

        PacketHandler.sendToServer(new PacketFilterSlot(hoveredSlot.slotNumber, stack, stack.getCount()));
        return true;
    }

    @Override
    public boolean mouseReleased(double x, double y, int btn) {
        if (hoveredSlot != null && hoveredSlot instanceof CraftingSlot) {
            return true;
        }

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
