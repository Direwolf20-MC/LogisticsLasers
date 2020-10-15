package com.direwolf20.logisticslasers.client.screens;

import com.direwolf20.logisticslasers.LogisticsLasers;
import com.direwolf20.logisticslasers.client.screens.widgets.DireButton;
import com.direwolf20.logisticslasers.client.screens.widgets.GuiIncrementer;
import com.direwolf20.logisticslasers.common.container.CraftingStationContainer;
import com.direwolf20.logisticslasers.common.container.customslot.BasicFilterSlot;
import com.direwolf20.logisticslasers.common.container.customslot.CraftingSlot;
import com.direwolf20.logisticslasers.common.network.PacketHandler;
import com.direwolf20.logisticslasers.common.network.packets.*;
import com.direwolf20.logisticslasers.common.util.MagicHelpers;
import com.direwolf20.logisticslasers.common.util.MiscTools;
import com.google.common.collect.ArrayListMultimap;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.items.ItemHandlerHelper;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class CraftingStationScreen extends ContainerScreen<CraftingStationContainer> {
    private static final ResourceLocation background = new ResourceLocation(LogisticsLasers.MOD_ID, "textures/gui/crafting_station.png");

    protected final CraftingStationContainer container;

    private int Z_LEVEL_ITEMS = 100;
    private int Z_LEVEL_QTY = 300;
    private int Z_LEVEL_TOOLTIPS = 500;
    private int slotSize = 18;
    private int availableItemsstartX;
    private int availableItemstartY;
    private int overSlot = -1;
    private int selectedSlot = -1;
    private ArrayListMultimap<Item, ItemStack> itemMap;
    private ArrayList<ItemStack> itemStacks;
    private GuiIncrementer requestCounter;
    private TextFieldWidget searchField;

    public CraftingStationScreen(CraftingStationContainer container, PlayerInventory playerInventory, ITextComponent title) {
        super(container, playerInventory, title);
        this.container = container;
        this.xSize = 353;
        this.ySize = 256;
    }

    public ResourceLocation getBackground() {
        return background;
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(stack);
        super.render(stack, mouseX, mouseY, partialTicks);
        this.renderForeground(stack, mouseX, mouseY);
        this.func_230459_a_(stack, mouseX, mouseY); // @mcp: func_230459_a_ = renderHoveredToolTip
    }

    public void renderForeground(MatrixStack matrixStack, int mouseX, int mouseY) {

        int availableItemsstartX = guiLeft + 179;
        int availableItemstartY = guiTop + 17;
        int color = 0x885B5B5B;

        matrixStack.push();
        RenderSystem.disableLighting();
        RenderSystem.disableDepthTest();
        RenderSystem.colorMask(true, true, true, false);
        fillGradient(matrixStack, availableItemsstartX - 2, availableItemstartY - 2, availableItemsstartX + 162, availableItemstartY + 164, color, color);
        RenderSystem.colorMask(true, true, true, true);
        matrixStack.pop();

        itemMap = container.tile.getControllerTE().getItemCounts().getItemCounts();
        int totalItems = itemMap.values().size();
        int itemsPerRow = 9;
        int rows = (int) Math.ceil((double) totalItems / (double) itemsPerRow);
        int maxRows = 9;
        itemStacks = new ArrayList(itemMap.values().stream()
                .filter(p -> p.getDisplayName().getString().toLowerCase().contains(searchField.getText().toLowerCase()))
                .sorted(Comparator.comparingInt(itemstack -> itemstack.getCount()))
                .collect(Collectors.toList())
        );
        if (itemStacks.isEmpty()) return;

        Collections.reverse(itemStacks);
        int slot = 0;
        overSlot = -1;
        for (int i = 0; i < itemStacks.size(); i++) {
            ItemStack stack = itemStacks.get(i);
            int row = (int) Math.floor((double) slot / 9);
            if (row >= maxRows) break;
            int col = slot % 9;
            int count = stack.getCount();
            int x = availableItemsstartX + col * 18;
            int y = availableItemstartY + row * 18;

            setBlitOffset(Z_LEVEL_ITEMS);
            itemRenderer.zLevel = Z_LEVEL_ITEMS;
            this.itemRenderer.renderItemIntoGUI(stack, x, y);
            this.itemRenderer.renderItemOverlayIntoGUI(font, ItemHandlerHelper.copyStackWithSize(stack, 1), x, y, null);
            matrixStack.push();
            matrixStack.translate(x, y, Z_LEVEL_QTY);
            matrixStack.scale(0.65f, 0.65f, 0.65f);
            setBlitOffset(0);

            itemRenderer.zLevel = 0;


            font.drawStringWithShadow(matrixStack, MagicHelpers.withSuffix(count), 19 - font.getStringWidth(MagicHelpers.withSuffix(count)) * 0.65f, 18, TextFormatting.WHITE.getColor());

            matrixStack.pop();

            if (MiscTools.inBounds(x, y, 18, 18, mouseX, mouseY)) {
                overSlot = slot;
                color = -2130706433;// : 0xFF5B5B5B;

                matrixStack.push();
                RenderSystem.disableLighting();
                RenderSystem.disableDepthTest();
                RenderSystem.colorMask(true, true, true, false);
                fillGradient(matrixStack, x, y, x + 18, y + 18, color, color);
                RenderSystem.colorMask(true, true, true, true);
                matrixStack.pop();
            }

            if (slot == selectedSlot) {
                color = 0xFFFF0000;

                matrixStack.push();
                RenderSystem.disableLighting();
                RenderSystem.disableDepthTest();
                RenderSystem.colorMask(true, true, true, false);

                int x1 = x + 18;
                int y1 = y + 18;
                hLine(matrixStack, x - 1, x1 - 1, y - 1, color);
                hLine(matrixStack, x - 1, x1 - 1, y1 - 1, color);
                vLine(matrixStack, x - 1, y - 1, y1 - 1, color);
                vLine(matrixStack, x1 - 1, y - 1, y1 - 1, color);

                RenderSystem.colorMask(true, true, true, true);
                matrixStack.pop();
            }


            slot++;
        }


    }


    @Override
    public void init() {
        super.init();
        this.guiLeft = 80;
        availableItemsstartX = guiLeft + 195;
        availableItemstartY = guiTop + 17;
        List<Widget> leftWidgets = new ArrayList<>();

        leftWidgets.add(new Button(guiLeft + 2, guiTop + 45, 15, 10, new StringTextComponent("+"), (button) -> {
            PacketHandler.sendToServer(new PacketRequestGrid(1));
        }));

        leftWidgets.add(new Button(guiLeft + 2, guiTop + 65, 15, 10, new StringTextComponent("~"), (button) -> {
            PacketHandler.sendToServer(new PacketRequestGridMissing());
        }));

        leftWidgets.add(new Button(guiLeft + 177, guiTop + 185, 55, 20, new StringTextComponent("Refresh"), (button) -> {
            PacketHandler.sendToServer(new PacketItemCountsRefresh());
        }));

        requestCounter = new GuiIncrementer(guiLeft + 240, guiTop + 185, 1, 999, 28, 10, null);
        leftWidgets.add(requestCounter);

        leftWidgets.add(new DireButton(guiLeft + 240, guiTop + 195, 55, 15, new StringTextComponent("Request"), (button) -> {
            requestItem();
        }));

        searchField = new TextFieldWidget(font, guiLeft + 177, guiTop + 210, 155, 15, StringTextComponent.EMPTY);
        leftWidgets.add(searchField);

        // Lay the buttons out, too lazy to figure out the math every damn time.
        // Ordered by where you add them.
        for (int i = 0; i < leftWidgets.size(); i++) {
            addButton(leftWidgets.get(i));
        }
    }

    private void requestItem() {
        if (selectedSlot == -1) return;
        ItemStack stack = itemStacks.get(selectedSlot);
        stack.setCount(requestCounter.getValue());
        PacketHandler.sendToServer(new PacketRequestItem(stack, requestCounter.getValue()));
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
        if (MiscTools.inBounds(searchField.x, searchField.y, searchField.getWidth(), 15, mouseX, mouseY) && button == 1)
            searchField.setText("");

        if (hoveredSlot != null && hoveredSlot instanceof CraftingSlot) {
            PacketHandler.sendToServer(new PacketDoCraft(hoveredSlot.getStack(), hoveredSlot.getStack().getCount(), Screen.hasShiftDown()));
            return true;
        }

        if (hoveredSlot != null && hoveredSlot instanceof BasicFilterSlot) {
            // By splitting the stack we can get air easily :) perfect removal basically
            ItemStack stack = getMinecraft().player.inventory.getItemStack();
            stack = stack.copy().split(hoveredSlot.getSlotStackLimit()); // Limit to slot limit
            hoveredSlot.putStack(stack); // Temporarily update the client for continuity purposes

            PacketHandler.sendToServer(new PacketFilterSlot(hoveredSlot.slotNumber, stack, stack.getCount()));
            return true;
        }

        if (overSlot >= 0) {
            selectedSlot = overSlot;
            return true;
        }

        if (hoveredSlot != null && hoveredSlot.slotNumber < 27 && button == 2) {
            //System.out.println(hoveredSlot.slotNumber);
            if (!hoveredSlot.getStack().isEmpty())
                PacketHandler.sendToServer(new PacketSortStackFromCrafter(hoveredSlot.slotNumber));
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
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

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        InputMappings.Input mouseKey = InputMappings.getInputByCode(keyCode, scanCode);
        if (searchField.isFocused() && this.minecraft.gameSettings.keyBindInventory.isActiveAndMatches(mouseKey))
            return true;
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
