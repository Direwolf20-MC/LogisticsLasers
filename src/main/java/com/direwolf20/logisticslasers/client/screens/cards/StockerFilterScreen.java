package com.direwolf20.logisticslasers.client.screens.cards;

import com.direwolf20.logisticslasers.LogisticsLasers;
import com.direwolf20.logisticslasers.common.container.cards.StockerFilterContainer;
import com.direwolf20.logisticslasers.common.container.customslot.StockerFilterSlot;
import com.direwolf20.logisticslasers.common.network.PacketHandler;
import com.direwolf20.logisticslasers.common.network.packets.PacketFilterSlot;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.awt.*;

public class StockerFilterScreen extends BaseFilterScreen<StockerFilterContainer> {
    private static final ResourceLocation background = new ResourceLocation(LogisticsLasers.MOD_ID, "textures/gui/basicfilterscreen.png");

    protected final StockerFilterContainer container;
    private boolean isWhitelist;

    public StockerFilterScreen(StockerFilterContainer container, PlayerInventory playerInventory, ITextComponent title) {
        super(container, playerInventory, title);
        this.container = container;
        isWhitelist = container.isWhiteList();
    }

    public ResourceLocation getBackground() {
        return background;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(MatrixStack stack, int mouseX, int mouseY) {
        Minecraft.getInstance().fontRenderer.drawString(stack, I18n.format("item.logisticslasers.stockerfilterscreen"), 50, 5, Color.DARK_GRAY.getRGB());
        if (!container.showPriority()) return;
        Minecraft.getInstance().fontRenderer.drawString(stack, new TranslationTextComponent("item.logisticslasers.basicfilterscreen.priority").getString(), 3, 15, Color.DARK_GRAY.getRGB());
        Minecraft.getInstance().fontRenderer.drawString(stack, new StringTextComponent("" + container.getPriority()).getString(), 18, 25, Color.DARK_GRAY.getRGB());
    }

    protected static TranslationTextComponent getTrans(String key, Object... args) {
        return new TranslationTextComponent(LogisticsLasers.MOD_ID + "." + key, args);
    }

    @Override
    public boolean mouseClicked(double x, double y, int btn) {
        if (hoveredSlot == null || !(hoveredSlot instanceof StockerFilterSlot))
            return super.mouseClicked(x, y, btn);

        // By splitting the stack we can get air easily :) perfect removal basically
        ItemStack stack = getMinecraft().player.inventory.getItemStack();
        if (!stack.isEmpty()) {
            stack = stack.copy().split(hoveredSlot.getSlotStackLimit()); // Limit to slot limit
            hoveredSlot.putStack(stack); // Temporarily update the client for continuity purposes
            PacketHandler.sendToServer(new PacketFilterSlot(hoveredSlot.slotNumber, stack, stack.getCount()));
        } else {
            ItemStack slotStack = hoveredSlot.getStack();
            if (!slotStack.isEmpty()) {
                if (btn == 2) {
                    slotStack.setCount(0);
                    PacketHandler.sendToServer(new PacketFilterSlot(hoveredSlot.slotNumber, slotStack, slotStack.getCount()));
                    return true;
                }
                int amt = (btn == 0) ? 1 : -1;
                if (Screen.hasShiftDown()) amt *= 10;
                if (Screen.hasControlDown()) amt *= 100;
                slotStack.grow(amt);

                PacketHandler.sendToServer(new PacketFilterSlot(hoveredSlot.slotNumber, slotStack, slotStack.getCount()));
            }
        }
        return true;
    }

    @Override
    public boolean mouseReleased(double x, double y, int btn) {
        if (hoveredSlot == null || !(hoveredSlot instanceof StockerFilterSlot))
            return super.mouseReleased(x, y, btn);

        return true;
    }

    @Override
    public boolean mouseScrolled(double x, double y, double amt) {
        if (hoveredSlot == null || !(hoveredSlot instanceof StockerFilterSlot))
            return super.mouseScrolled(x, y, amt);
        ItemStack slotStack = hoveredSlot.getStack();
        if (!slotStack.isEmpty()) {
            slotStack.grow((int) amt);
            PacketHandler.sendToServer(new PacketFilterSlot(hoveredSlot.slotNumber, slotStack, slotStack.getCount()));
        }

        return true;
    }
}
