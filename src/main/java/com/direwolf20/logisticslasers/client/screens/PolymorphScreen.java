package com.direwolf20.logisticslasers.client.screens;

import com.direwolf20.logisticslasers.LogisticsLasers;
import com.direwolf20.logisticslasers.client.screens.widgets.DireButton;
import com.direwolf20.logisticslasers.common.items.logiccards.BaseCard;
import com.direwolf20.logisticslasers.common.items.logiccards.CardPolymorph;
import com.direwolf20.logisticslasers.common.network.PacketHandler;
import com.direwolf20.logisticslasers.common.network.packets.PacketChangePriority;
import com.direwolf20.logisticslasers.common.network.packets.PacketPolymorphClear;
import com.direwolf20.logisticslasers.common.network.packets.PacketPolymorphSet;
import com.direwolf20.logisticslasers.common.util.MagicHelpers;
import com.direwolf20.logisticslasers.common.util.MiscTools;
import com.google.common.collect.ArrayListMultimap;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.items.ItemHandlerHelper;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class PolymorphScreen extends Screen {
    private static final ResourceLocation background = new ResourceLocation(LogisticsLasers.MOD_ID, "textures/gui/polymorphscreen.png");

    int guiLeft;
    int guiTop;
    protected int xSize = 176;
    protected int ySize = 166;
    private ArrayListMultimap<Item, ItemStack> itemMap;
    private int page = 0;
    private int maxPages = 0;
    private int overSlot = -1;
    private int Z_LEVEL_ITEMS = 100;
    private int Z_LEVEL_QTY = 300;

    ItemStack card;
    int cardSlot;
    public BlockPos sourceContainer;

    public PolymorphScreen(ItemStack stack) {
        super(new StringTextComponent("title"));
        card = stack;
        sourceContainer = BlockPos.ZERO;
    }

    public PolymorphScreen(ItemStack stack, BlockPos sourceContainerPos, int sourceContainerSlot) {
        super(new StringTextComponent("title"));
        card = stack;
        sourceContainer = sourceContainerPos;
        cardSlot = sourceContainerSlot;
    }

    public ResourceLocation getBackground() {
        return background;
    }

    @Override
    protected void init() {
        super.init();
        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;
        List<Widget> leftWidgets = new ArrayList<>();

        Button plusPriority;
        leftWidgets.add(plusPriority = new DireButton(guiLeft + 30, guiTop + 15, 15, 10, new StringTextComponent("+"), (button) -> {
            PacketHandler.sendToServer(new PacketChangePriority(1));
        }));
        Button minusPriority;
        leftWidgets.add(minusPriority = new DireButton(guiLeft + 2, guiTop + 15, 15, 10, new StringTextComponent("-"), (button) -> {
            PacketHandler.sendToServer(new PacketChangePriority(-1));
        }));

        leftWidgets.add(new DireButton(guiLeft + 60, guiTop + 15, 15, 10, new StringTextComponent("Set"), (button) -> {
            PacketHandler.sendToServer(new PacketPolymorphSet(cardSlot, sourceContainer));
        }));

        leftWidgets.add(new DireButton(guiLeft + 80, guiTop + 15, 15, 10, new StringTextComponent("Clear"), (button) -> {
            PacketHandler.sendToServer(new PacketPolymorphClear(cardSlot, sourceContainer));
        }));


        // Lay the buttons out, too lazy to figure out the math every damn time.
        // Ordered by where you add them.
        for (int i = 0; i < leftWidgets.size(); i++) {
            addButton(leftWidgets.get(i));
        }
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        renderBackground(stack);
        drawGuiContainerForegroundLayer(stack);
        super.render(stack, mouseX, mouseY, partialTicks);

        int availableItemsstartX = guiLeft + 7;
        int availableItemstartY = guiTop + 30;
        int color = 0x885B5B5B;

        stack.push();
        RenderSystem.disableLighting();
        RenderSystem.disableDepthTest();
        RenderSystem.colorMask(true, true, true, false);
        fillGradient(stack, availableItemsstartX - 2, availableItemstartY - 2, availableItemsstartX + 162, availableItemstartY + 130, color, color);
        RenderSystem.colorMask(true, true, true, true);
        stack.pop();

        ArrayList<ItemStack> filterStacks = CardPolymorph.getListFromCard(card);
        int totalItems = filterStacks.size();
        int itemsPerRow = 9;
        int rows = (int) Math.ceil((double) totalItems / (double) itemsPerRow);
        int maxRows = 9;

        if (filterStacks.isEmpty()) return;

        int itemsPerPage = 81;
        maxPages = (int) Math.floor((double) filterStacks.size() / itemsPerPage);
        int itemStackMin = (page * itemsPerPage);
        int itemStackMax = Math.min((page * itemsPerPage) + itemsPerPage, filterStacks.size());
        List<ItemStack> displayStacks = filterStacks.subList(itemStackMin, itemStackMax);
        font.drawString(stack, MagicHelpers.withSuffix(page), guiLeft + 260 - font.getStringWidth(MagicHelpers.withSuffix(page)) * 0.65f, guiTop + 5, TextFormatting.DARK_GRAY.getColor());

        int slot = 0;
        overSlot = -1;
        for (int i = 0; i < displayStacks.size(); i++) {
            ItemStack filterstack = displayStacks.get(i);
            int row = (int) Math.floor((double) slot / 9);
            if (row >= maxRows) break;
            int col = slot % 9;
            int count = filterstack.getCount();
            int x = availableItemsstartX + col * 18;
            int y = availableItemstartY + row * 18;

            setBlitOffset(Z_LEVEL_ITEMS);
            itemRenderer.zLevel = Z_LEVEL_ITEMS;
            this.itemRenderer.renderItemIntoGUI(filterstack, x, y);
            this.itemRenderer.renderItemOverlayIntoGUI(font, ItemHandlerHelper.copyStackWithSize(filterstack, 1), x, y, null);
            stack.push();
            stack.translate(x, y, Z_LEVEL_QTY);
            stack.scale(0.65f, 0.65f, 0.65f);
            setBlitOffset(0);

            itemRenderer.zLevel = 0;


            //font.drawStringWithShadow(stack, MagicHelpers.withSuffix(count), 19 - font.getStringWidth(MagicHelpers.withSuffix(count)) * 0.65f, 18, TextFormatting.WHITE.getColor());

            stack.pop();

            if (MiscTools.inBounds(x, y, 18, 18, mouseX, mouseY)) {
                overSlot = slot;
                color = -2130706433;// : 0xFF5B5B5B;

                stack.push();
                RenderSystem.disableLighting();
                RenderSystem.disableDepthTest();
                RenderSystem.colorMask(true, true, true, false);
                fillGradient(stack, x, y, x + 18, y + 18, color, color);
                RenderSystem.colorMask(true, true, true, true);
                stack.pop();
            }

            /*if (slot == selectedSlot) {
                color = 0xFFFF0000;

                stack.push();
                RenderSystem.disableLighting();
                RenderSystem.disableDepthTest();
                RenderSystem.colorMask(true, true, true, false);

                int x1 = x + 18;
                int y1 = y + 18;
                hLine(stack, x - 1, x1 - 1, y - 1, color);
                hLine(stack, x - 1, x1 - 1, y1 - 1, color);
                vLine(stack, x - 1, y - 1, y1 - 1, color);
                vLine(stack, x1 - 1, y - 1, y1 - 1, color);

                RenderSystem.colorMask(true, true, true, true);
                stack.pop();
            }*/


            slot++;
        }
    }

    @Override
    public void renderBackground(MatrixStack stack) {
        RenderSystem.color4f(1, 1, 1, 1);
        getMinecraft().getTextureManager().bindTexture(getBackground());
        this.blit(stack, guiLeft, guiTop, 0, 0, xSize, ySize);
    }

    protected void drawGuiContainerForegroundLayer(MatrixStack stack) {
        Minecraft.getInstance().fontRenderer.drawString(stack, I18n.format("item.logisticslasers.polyfilterscreen"), guiLeft + 50, guiTop + 5, Color.DARK_GRAY.getRGB());
        Minecraft.getInstance().fontRenderer.drawString(stack, new TranslationTextComponent("item.logisticslasers.basicfilterscreen.priority").getString(), guiLeft + 3, guiTop + 5, Color.DARK_GRAY.getRGB());
        Minecraft.getInstance().fontRenderer.drawString(stack, new StringTextComponent("" + BaseCard.getPriority(card)).getString(), guiLeft + 18, guiTop + 15, Color.DARK_GRAY.getRGB());
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        super.onClose();
    }

    private static TranslationTextComponent getTrans(String key, Object... args) {
        return new TranslationTextComponent(LogisticsLasers.MOD_ID + "." + key, args);
    }
}
