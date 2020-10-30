package com.direwolf20.logisticslasers.client.screens;

import com.direwolf20.logisticslasers.LogisticsLasers;
import com.direwolf20.logisticslasers.client.screens.widgets.DireButton;
import com.direwolf20.logisticslasers.client.screens.widgets.GuiIncrementer;
import com.direwolf20.logisticslasers.common.container.ControllerContainer;
import com.direwolf20.logisticslasers.common.tiles.ControllerTile;
import com.direwolf20.logisticslasers.common.util.MagicHelpers;
import com.direwolf20.logisticslasers.common.util.MiscTools;
import com.google.common.collect.ArrayListMultimap;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.*;
import net.minecraftforge.items.ItemHandlerHelper;

import java.awt.Color;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class ControllerScreen extends FEScreenBase<ControllerContainer> {
    private static final ResourceLocation background = new ResourceLocation(LogisticsLasers.MOD_ID, "textures/gui/controller.png");

    private int Z_LEVEL_ITEMS = 100;
    private int Z_LEVEL_QTY = 300;
    private int Z_LEVEL_TOOLTIPS = 500;
    private int slotSize = 18;
    private int availableItemsstartX;
    private int availableItemstartY;
    private int overSlot = -1;
    private ArrayListMultimap<Item, ItemStack> itemMap;
    private ArrayList<ItemStack> itemStacks;
    private GuiIncrementer requestCounter;
    private int page = 0;
    private int maxPages = 0;
    DireButton leftButton;
    DireButton rightButton;

    public ControllerScreen(ControllerContainer container, PlayerInventory playerInventory, ITextComponent title) {
        super(container, playerInventory, title);
    }

    public ResourceLocation getBackground() {
        return background;
    }

    @Override
    public void init() {
        super.init();
        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;
        List<Widget> leftWidgets = new ArrayList<>();
        rightButton = new DireButton(guiLeft + 160, guiTop + 4, 15, 10, new StringTextComponent(">"), (button) -> {
            if (page < maxPages) page++;
        });
        leftWidgets.add(rightButton);

        leftButton = new DireButton(guiLeft + 135, guiTop + 4, 15, 10, new StringTextComponent("<"), (button) -> {
            if (page > 0) page--;
        });
        leftWidgets.add(leftButton);

        for (int i = 0; i < leftWidgets.size(); i++) {
            addButton(leftWidgets.get(i));
        }
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        super.render(stack, mouseX, mouseY, partialTicks);

        if (mouseX > (guiLeft + 7) && mouseX < (guiLeft + 7) + 18 && mouseY > (guiTop + 7) && mouseY < (guiTop + 7) + 73)
            if (mouseX > (guiLeft + 7) && mouseX < (guiLeft + 7) + 18 && mouseY > (guiTop + 7) && mouseY < (guiTop + 7) + 73)
                if (Screen.hasShiftDown())
                    this.renderTooltip(stack, LanguageMap.getInstance().func_244260_a(Arrays.asList(
                            new TranslationTextComponent("screen.logisticslasers.energy", this.container.getEnergy(), MagicHelpers.withSuffix(this.container.getMaxPower())),
                            new TranslationTextComponent("screen.logisticslasers.fepertick", MagicHelpers.withSuffix(this.container.getRFCost()), MagicHelpers.withSuffix(this.container.getMaxPower()))
                            )
                    ), mouseX, mouseY);
                else
                    this.renderTooltip(stack, LanguageMap.getInstance().func_244260_a(Arrays.asList(
                            new TranslationTextComponent("screen.logisticslasers.energy", MagicHelpers.withSuffix(this.container.getEnergy()), MagicHelpers.withSuffix(this.container.getMaxPower())),
                            new TranslationTextComponent("screen.logisticslasers.fepertick", MagicHelpers.withSuffix(this.container.getRFCost()), MagicHelpers.withSuffix(this.container.getMaxPower()))
                            )
                    ), mouseX, mouseY);

        leftButton.visible = false;
        rightButton.visible = false;
        ControllerTile controllerTile;
        if (container.tile instanceof ControllerTile)
            controllerTile = (ControllerTile) container.tile;
        else
            return;

        if (!controllerTile.hasStoredItems()) return;

        int availableItemsstartX = this.guiLeft + 29;
        int availableItemstartY = this.guiTop + 19;
        int color = 0x885B5B5B;

        stack.push();
        RenderSystem.disableLighting();
        RenderSystem.disableDepthTest();
        RenderSystem.colorMask(true, true, true, false);
        fillGradient(stack, availableItemsstartX - 2, availableItemstartY - 2, availableItemsstartX + 128, availableItemstartY + 60, color, color);
        RenderSystem.colorMask(true, true, true, true);
        stack.pop();

        itemMap = controllerTile.getStoredItems().getItemCounts();
        int totalItems = itemMap.values().size();
        int itemsPerRow = 7;
        int rows = (int) Math.ceil((double) totalItems / (double) itemsPerRow);
        int maxRows = 3;
        itemStacks = new ArrayList(itemMap.values().stream()
                .sorted(Comparator.comparingInt(itemstack -> itemstack.getCount()))
                .collect(Collectors.toList())
        );

        if (itemStacks.isEmpty()) return;
        Collections.reverse(itemStacks);

        leftButton.visible = true;
        rightButton.visible = true;
        int itemsPerPage = itemsPerRow * maxRows;
        maxPages = (int) Math.floor((double) itemStacks.size() / itemsPerPage);
        int itemStackMin = (page * itemsPerPage);
        int itemStackMax = Math.min((page * itemsPerPage) + itemsPerPage, itemStacks.size());
        List<ItemStack> displayStacks = itemStacks.subList(itemStackMin, itemStackMax);
        font.drawString(stack, MagicHelpers.withSuffix(page), guiLeft + 155 - font.getStringWidth(MagicHelpers.withSuffix(page)) * 0.65f, guiTop + 5, TextFormatting.DARK_GRAY.getColor());

        int slot = 0;
        overSlot = -1;
        //for (int k = 0; k < 15; k++) {
        for (int i = 0; i < displayStacks.size(); i++) {
            ItemStack itemStack = displayStacks.get(i);
            int row = (int) Math.floor((double) slot / itemsPerRow);
            if (row >= maxRows) break;
            int col = slot % itemsPerRow;
            int count = itemStack.getCount();
            int x = availableItemsstartX + col * 18;
            int y = availableItemstartY + row * 18;

            setBlitOffset(Z_LEVEL_ITEMS);
            itemRenderer.zLevel = Z_LEVEL_ITEMS;
            this.itemRenderer.renderItemIntoGUI(itemStack, x, y);
            this.itemRenderer.renderItemOverlayIntoGUI(font, ItemHandlerHelper.copyStackWithSize(itemStack, 1), x, y, null);
            stack.push();
            stack.translate(x, y, Z_LEVEL_QTY);
            stack.scale(0.65f, 0.65f, 0.65f);
            setBlitOffset(0);

            itemRenderer.zLevel = 0;


            font.drawStringWithShadow(stack, MagicHelpers.withSuffix(count), 19 - font.getStringWidth(MagicHelpers.withSuffix(count)) * 0.65f, 18, TextFormatting.WHITE.getColor());

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

            slot++;
        }
        //}

    }

    @Override
    protected void drawGuiContainerForegroundLayer(MatrixStack stack, int mouseX, int mouseY) {
        Minecraft.getInstance().fontRenderer.drawString(stack, I18n.format("block.logisticslasers.controllerscreen"), 28, 8, Color.DARK_GRAY.getRGB());
    }
}
