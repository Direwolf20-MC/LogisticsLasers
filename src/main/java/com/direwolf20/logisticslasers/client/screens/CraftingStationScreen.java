package com.direwolf20.logisticslasers.client.screens;

import com.direwolf20.logisticslasers.LogisticsLasers;
import com.direwolf20.logisticslasers.client.screens.widgets.DireButton;
import com.direwolf20.logisticslasers.client.screens.widgets.GuiIncrementer;
import com.direwolf20.logisticslasers.common.container.CraftingStationContainer;
import com.direwolf20.logisticslasers.common.container.customhandler.CraftingStationHandler;
import com.direwolf20.logisticslasers.common.container.customslot.BasicFilterSlot;
import com.direwolf20.logisticslasers.common.container.customslot.CraftingSlot;
import com.direwolf20.logisticslasers.common.network.PacketHandler;
import com.direwolf20.logisticslasers.common.network.packets.*;
import com.direwolf20.logisticslasers.common.util.*;
import com.google.common.collect.ArrayListMultimap;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

import java.awt.*;
import java.util.List;
import java.util.*;
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
    private int page = 0;
    private int maxPages = 0;
    ItemStackKey selectedItem = new ItemStackKey(ItemStack.EMPTY);
    List<ItemStack> displayStacks;

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
        this.renderHoveredTooltip(stack, mouseX, mouseY); // @mcp: func_230459_a_ = renderHoveredToolTip
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

        if (container.tile.getControllerTE() == null) {
            selectedSlot = -1;
            return;
        }

        itemMap = container.tile.getControllerTE().getItemCounts().getItemCounts();
        int totalItems = itemMap.values().size();
        int itemsPerRow = 9;
        int rows = (int) Math.ceil((double) totalItems / (double) itemsPerRow);
        int maxRows = 9;
        itemStacks = new ArrayList(itemMap.values().stream()
                .sorted(Comparator.comparingInt(itemstack -> itemstack.getCount()))
                .collect(Collectors.toList())
        );
        String[] searchTerms = searchField.getText().toLowerCase(Locale.ROOT).split("\\s+");
        for (int i = 0; i < searchTerms.length; i++) {
            String search = searchTerms[i];
            if (search.startsWith("@")) {
                itemStacks.removeIf(p -> !p.getItem().getCreatorModId(p).toLowerCase(Locale.ROOT).contains(search.substring(1)));
            } else
                itemStacks.removeIf(p -> !p.getDisplayName().getString().toLowerCase(Locale.ROOT).contains(search));
        }
        if (itemStacks.isEmpty()) {
            selectedSlot = -1;
            return;
        }
        Collections.reverse(itemStacks);

        int itemsPerPage = 81;
        maxPages = (int) Math.floor((double) itemStacks.size() / itemsPerPage);
        if (page > maxPages) page = 0;
        int itemStackMin = (page * itemsPerPage);
        int itemStackMax = Math.min((page * itemsPerPage) + itemsPerPage, itemStacks.size());
        displayStacks = itemStacks.subList(itemStackMin, itemStackMax);
        font.drawString(matrixStack, MagicHelpers.withSuffix(page), guiLeft + 260 - font.getStringWidth(MagicHelpers.withSuffix(page)) * 0.65f, guiTop + 5, TextFormatting.DARK_GRAY.getColor());

        int slot = 0;
        overSlot = -1;
        selectedSlot = -1;
        for (int i = 0; i < displayStacks.size(); i++) {
            ItemStack stack = displayStacks.get(i);
            ItemStackKey stackKey = new ItemStackKey(stack);
            if (selectedItem.equals(stackKey))
                selectedSlot = i;
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

            if (MiscTools.inBounds(x, y, 17, 17, mouseX, mouseY)) {
                overSlot = slot;
                color = -2130706433;// : 0xFF5B5B5B;

                matrixStack.push();
                RenderSystem.disableLighting();
                RenderSystem.disableDepthTest();
                RenderSystem.colorMask(true, true, true, false);
                fillGradient(matrixStack, x, y, x + 17, y + 17, color, color);
                RenderSystem.colorMask(true, true, true, true);
                matrixStack.pop();
                matrixStack.push();
                matrixStack.translate(0, 0, Z_LEVEL_TOOLTIPS);
                this.renderTooltip(matrixStack, stack, mouseX, mouseY); // @mcp: func_230459_a_ = renderHoveredToolTip
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

        World world = Minecraft.getInstance().world;
        RecipeManager manager = world.getRecipeManager(); //Get the server recipe list i think
        CraftingStationHandler craftingHandler = container.craftingHandler;
        ICraftingRecipe recipe = manager.getRecipe(IRecipeType.CRAFTING, new CraftingStationInventory(craftingHandler, 3, 3), world).orElse(null);
        if (recipe == null) return;
        ItemStackHandler handler = container.handler;
        ItemHandlerUtil.InventoryCounts inventoryCounts = new ItemHandlerUtil.InventoryCounts(handler);
        List<ItemStack> invItemStacks = new ArrayList(inventoryCounts.getItemCounts().values());
        List<ItemStack> tempItemMap = new ArrayList();
        for (ItemStack tempStack : itemMap.values()) {
            tempItemMap.add(tempStack.copy());
        }
        int overlayColorRed = MiscTools.rgbaToInt(255, 75, 75, 55);
        int overlayColorYellow = MiscTools.rgbaToInt(255, 255, 0, 55);
        int startX = guiLeft + 29;
        int startY = guiTop + 16;
        List<Ingredient> ingredients = recipe.getIngredients();
        List<Integer> slotsChecked = new ArrayList<>();
        for (int i = 0; i < ingredients.size(); i++) {
            Ingredient ingredient = ingredients.get(i);
            if (ingredient.hasNoMatchingItems()) continue;
            boolean foundItem = false;
            for (ItemStack testStack : invItemStacks) { //Loop through all slots in internal inventory
                if (ingredient.test(testStack) && testStack.getCount() > 0) {
                    foundItem = true;
                    testStack.shrink(1);
                    break;
                }
            }
            if (!foundItem) {
                for (ItemStack testStack : tempItemMap) {
                    if (ingredient.test(testStack) && testStack.getCount() > 0) {
                        foundItem = true;
                        testStack.shrink(1);
                        RenderSystem.pushMatrix();
                        RenderSystem.translated(0, 0, 1000);
                        for (int j = 0; j < 9; j++) {
                            ItemStack stackInSlot = container.craftingHandler.getStackInSlot(j);
                            if (!stackInSlot.isEmpty() && !slotsChecked.contains(j) && ingredient.test(stackInSlot)) {
                                int x = startX + (j % 3) * 18 + 1;
                                int y = startY + (j / 3) * 18 + 1;
                                fill(matrixStack, x, y, x + 16, y + 16, overlayColorYellow);
                                slotsChecked.add(j);
                                break;
                            }
                        }
                        RenderSystem.translated(0, 0, -1000);
                        RenderSystem.popMatrix();
                        break;
                    }
                }
                if (!foundItem) {
                    RenderSystem.pushMatrix();
                    RenderSystem.translated(0, 0, 1000);
                    for (int j = 0; j < 9; j++) {
                        ItemStack stackInSlot = container.craftingHandler.getStackInSlot(j);
                        if (!stackInSlot.isEmpty() && !slotsChecked.contains(j) && ingredient.test(stackInSlot)) {
                            int x = startX + (j % 3) * 18 + 1;
                            int y = startY + (j / 3) * 18 + 1;
                            fill(matrixStack, x, y, x + 16, y + 16, overlayColorRed);
                            slotsChecked.add(j);
                            break;
                        }
                    }
                    RenderSystem.translated(0, 0, -1000);
                    RenderSystem.popMatrix();
                }
            }
        }

    }


    @Override
    public void init() {
        super.init();
        this.guiLeft = 100;
        availableItemsstartX = guiLeft + 195;
        availableItemstartY = guiTop + 17;
        List<Widget> leftWidgets = new ArrayList<>();

        leftWidgets.add(new DireButton(guiLeft + 90, guiTop + 30, 10, 10, new StringTextComponent("+"), (button) -> {
            int amt = Screen.hasShiftDown() ? 10 : 1;
            PacketHandler.sendToServer(new PacketRequestGrid(amt));
        }));

        leftWidgets.add(new DireButton(guiLeft + 18, guiTop + 16, 10, 10, new StringTextComponent("X"), (button) -> {
            PacketHandler.sendToServer(new PacketClearGrid());
        }));

        leftWidgets.add(new DireButton(guiLeft + 90, guiTop + 45, 10, 10, new StringTextComponent("~"), (button) -> {
            PacketHandler.sendToServer(new PacketRequestGridMissing());
        }));

        leftWidgets.add(new DireButton(guiLeft + 268, guiTop + 4, 15, 10, new StringTextComponent(">"), (button) -> {
            if (page < maxPages) page++;
        }));

        leftWidgets.add(new DireButton(guiLeft + 235, guiTop + 4, 15, 10, new StringTextComponent("<"), (button) -> {
            if (page > 0) page--;
        }));

        leftWidgets.add(new DireButton(guiLeft + 177, guiTop + 185, 55, 20, new TranslationTextComponent("screen.logisticslasers.refresh"), (button) -> {
            PacketHandler.sendToServer(new PacketItemCountsRefresh());
        }));

        requestCounter = new GuiIncrementer(guiLeft + 240, guiTop + 185, 1, 999, 28, 10, null);
        leftWidgets.add(requestCounter);

        leftWidgets.add(new DireButton(guiLeft + 240, guiTop + 195, 55, 15, new TranslationTextComponent("screen.logisticslasers.request"), (button) -> {
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
        ItemStack stack = displayStacks.get(selectedSlot).copy();
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
            this.dragSplitting = true;
            PacketHandler.sendToServer(new PacketFilterSlot(hoveredSlot.slotNumber, stack, stack.getCount()));
            return true;
        }

        if (overSlot >= 0) {
            //selectedSlot = overSlot;
            selectedItem = new ItemStackKey(displayStacks.get(overSlot));
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
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (hoveredSlot instanceof BasicFilterSlot) {
            ItemStack stack = getMinecraft().player.inventory.getItemStack();
            ItemStackKey heldStackKey = new ItemStackKey(stack);
            ItemStackKey slotStackKey = new ItemStackKey(hoveredSlot.getStack());
            if (slotStackKey.equals(heldStackKey)) return true;
            stack = stack.copy().split(hoveredSlot.getSlotStackLimit()); // Limit to slot limit
            hoveredSlot.putStack(stack); // Temporarily update the client for continuity purposes
            PacketHandler.sendToServer(new PacketFilterSlot(hoveredSlot.slotNumber, stack, stack.getCount()));
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double x, double y, int btn) {
        if (hoveredSlot != null && hoveredSlot instanceof CraftingSlot) {
            this.dragSplitting = false;
            return true;
        }

        if (hoveredSlot == null || !(hoveredSlot instanceof BasicFilterSlot))
            return super.mouseReleased(x, y, btn);
        this.dragSplitting = false;
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
