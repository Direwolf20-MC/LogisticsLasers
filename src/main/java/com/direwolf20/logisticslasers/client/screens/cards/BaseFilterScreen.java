package com.direwolf20.logisticslasers.client.screens.cards;

import com.direwolf20.logisticslasers.LogisticsLasers;
import com.direwolf20.logisticslasers.client.screens.widgets.WhiteListButton;
import com.direwolf20.logisticslasers.common.container.cards.BasicFilterContainer;
import com.direwolf20.logisticslasers.common.container.customslot.BasicFilterSlot;
import com.direwolf20.logisticslasers.common.network.PacketHandler;
import com.direwolf20.logisticslasers.common.network.packets.PacketChangePriority;
import com.direwolf20.logisticslasers.common.network.packets.PacketFilterSlot;
import com.direwolf20.logisticslasers.common.network.packets.PacketToggleNBTFilter;
import com.direwolf20.logisticslasers.common.network.packets.PacketToggleWhitelist;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class BaseFilterScreen<T extends BasicFilterContainer> extends ContainerScreen<T> {
    private static final ResourceLocation background = new ResourceLocation(LogisticsLasers.MOD_ID, "textures/gui/basicfilterscreen.png");

    protected final BasicFilterContainer container;
    private boolean isWhitelist;
    private boolean isNBTFilter;

    public BaseFilterScreen(T container, PlayerInventory playerInventory, ITextComponent title) {
        super(container, playerInventory, title);
        this.container = container;
        isWhitelist = container.isWhiteList();
        isNBTFilter = container.isNBTFilter();
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
        if (container.showWhiteList()) {
            WhiteListButton blackwhitelist;
            leftWidgets.add(blackwhitelist = new WhiteListButton(guiLeft + 10, guiTop + 40, 20, 20, isWhitelist, (button) -> {
                isWhitelist = !isWhitelist;
                ((WhiteListButton) button).setWhitelist(isWhitelist);
                PacketHandler.sendToServer(new PacketToggleWhitelist());
            }));
        }

        if (container.showNBTFilter()) {
            leftWidgets.add(new WhiteListButton(guiLeft + 10, guiTop + 70, 20, 20, isNBTFilter, (button) -> {
                isNBTFilter = !isNBTFilter;
                ((WhiteListButton) button).setWhitelist(isNBTFilter);
                PacketHandler.sendToServer(new PacketToggleNBTFilter());
            }));
        }

        if (container.showPriority()) {
            Button plusPriority;
            leftWidgets.add(plusPriority = new Button(guiLeft + 30, guiTop + 25, 15, 10, new StringTextComponent("+"), (button) -> {
                PacketHandler.sendToServer(new PacketChangePriority(1));
            }));
            Button minusPriority;
            leftWidgets.add(minusPriority = new Button(guiLeft + 2, guiTop + 25, 15, 10, new StringTextComponent("-"), (button) -> {
                PacketHandler.sendToServer(new PacketChangePriority(-1));
            }));
        }

        // Lay the buttons out, too lazy to figure out the math every damn time.
        // Ordered by where you add them.
        for (int i = 0; i < leftWidgets.size(); i++) {
            addButton(leftWidgets.get(i));
        }
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
        if (!container.showPriority()) return;
        Minecraft.getInstance().fontRenderer.drawString(stack, new TranslationTextComponent("item.logisticslasers.basicfilterscreen.priority").getString(), 3, 15, Color.DARK_GRAY.getRGB());
        Minecraft.getInstance().fontRenderer.drawString(stack, new StringTextComponent("" + container.getPriority()).getString(), 18, 25, Color.DARK_GRAY.getRGB());
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

        PacketHandler.sendToServer(new PacketFilterSlot(hoveredSlot.slotNumber, stack, stack.getCount()));
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
