package com.direwolf20.logisticslasers.client.screens;

import com.direwolf20.logisticslasers.LogisticsLasers;
import com.direwolf20.logisticslasers.client.screens.widgets.DireButton;
import com.direwolf20.logisticslasers.client.screens.widgets.WhiteListButton;
import com.direwolf20.logisticslasers.common.container.cards.TagFilterContainer;
import com.direwolf20.logisticslasers.common.container.customslot.BasicFilterSlot;
import com.direwolf20.logisticslasers.common.items.logiccards.BaseCard;
import com.direwolf20.logisticslasers.common.items.logiccards.CardInserterTag;
import com.direwolf20.logisticslasers.common.network.PacketHandler;
import com.direwolf20.logisticslasers.common.network.packets.*;
import com.direwolf20.logisticslasers.common.util.MagicHelpers;
import com.direwolf20.logisticslasers.common.util.MiscTools;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TagFilterScreen extends ContainerScreen<TagFilterContainer> {
    private static final ResourceLocation background = new ResourceLocation(LogisticsLasers.MOD_ID, "textures/gui/tagfilterscreen.png");

    int guiLeft;
    int guiTop;
    protected int xSize = 200;
    protected int ySize = 254;
    private TextFieldWidget tagField;
    private int page = 0;
    private int maxPages = 0;
    private int overSlot = -1;
    private int selectedSlot = -1;
    List<String> displayTags;
    private boolean isWhitelist;

    ItemStack card;
    int cardSlot;
    public BlockPos sourceContainer;

    public TagFilterScreen(TagFilterContainer container, PlayerInventory playerInventory, ITextComponent title) {
        super(container, playerInventory, title);
        card = container.filterItemStack;
        sourceContainer = container.sourceContainer;
        isWhitelist = BaseCard.getWhiteList(card);
        cardSlot = -1;
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

        leftWidgets.add(new DireButton(guiLeft + 160, guiTop + 4, 15, 10, new StringTextComponent(">"), (button) -> {
            if (page < maxPages) page++;
        }));

        leftWidgets.add(new DireButton(guiLeft + 135, guiTop + 4, 15, 10, new StringTextComponent("<"), (button) -> {
            if (page > 0) page--;
        }));

        leftWidgets.add(new DireButton(guiLeft + 85, guiTop + 15, 40, 10, new TranslationTextComponent("screen.logisticslasers.remove"), (button) -> {
            if (selectedSlot != -1) {
                PacketHandler.sendToServer(new PacketButtonSetOrRemove(cardSlot, sourceContainer, displayTags.get(selectedSlot)));
                CardInserterTag.removeTag(card, displayTags.get(selectedSlot));
                selectedSlot = -1;
            }
        }));

        leftWidgets.add(new DireButton(guiLeft + 130, guiTop + 15, 30, 10, new TranslationTextComponent("screen.logisticslasers.clear"), (button) -> {
            PacketHandler.sendToServer(new PacketButtonClear(cardSlot, sourceContainer));
            CardInserterTag.clearTags(card);
            selectedSlot = -1;
            page = 0;
        }));

        leftWidgets.add(new DireButton(guiLeft + 60, guiTop + 15, 20, 10, new TranslationTextComponent("screen.logisticslasers.add"), (button) -> {
            if (!tagField.getText().isEmpty()) {
                PacketHandler.sendToServer(new PacketButtonAdd(cardSlot, sourceContainer, tagField.getText().toLowerCase(Locale.ROOT)));
                CardInserterTag.addTag(card, tagField.getText().toLowerCase(Locale.ROOT));
                tagField.setText("");
            } else {
                if (!container.handler.getStackInSlot(0).isEmpty()) {
                    ItemStack stack = container.handler.getStackInSlot(0);
                    for (ResourceLocation res : stack.getItem().getTags()) {
                        PacketHandler.sendToServer(new PacketButtonAdd(cardSlot, sourceContainer, res.toString().toLowerCase(Locale.ROOT)));
                        CardInserterTag.addTag(card, res.toString().toLowerCase(Locale.ROOT));
                    }
                }
            }
        }));

        tagField = new TextFieldWidget(font, guiLeft + 7, guiTop + 30, 155, 15, StringTextComponent.EMPTY);
        leftWidgets.add(tagField);

        WhiteListButton blackwhitelist;
        leftWidgets.add(blackwhitelist = new WhiteListButton(guiLeft + 110, guiTop + 3, 10, 10, isWhitelist, (button) -> {
            isWhitelist = !isWhitelist;
            ((WhiteListButton) button).setWhitelist(isWhitelist);
            PacketHandler.sendToServer(new PacketToggleWhitelist());
        }));


        // Lay the buttons out, too lazy to figure out the math every damn time.
        // Ordered by where you add them.
        for (int i = 0; i < leftWidgets.size(); i++) {
            addButton(leftWidgets.get(i));
        }
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(stack);
        super.render(stack, mouseX, mouseY, partialTicks);

        int availableItemsstartX = guiLeft + 7;
        int availableItemstartY = guiTop + 50;
        int color = 0x885B5B5B;

        stack.push();
        RenderSystem.disableLighting();
        RenderSystem.disableDepthTest();
        RenderSystem.colorMask(true, true, true, false);
        fillGradient(stack, availableItemsstartX - 2, availableItemstartY - 2, availableItemsstartX + 162, availableItemstartY + 110, color, color);
        RenderSystem.colorMask(true, true, true, true);
        stack.pop();
        font.drawString(stack, MagicHelpers.withSuffix(page), guiLeft + 155 - font.getStringWidth(MagicHelpers.withSuffix(page)) * 0.65f, guiTop + 5, TextFormatting.DARK_GRAY.getColor());

        List<String> tags = new ArrayList<>(CardInserterTag.getTags(card));
        int tagsPerPage = 11;
        maxPages = (int) Math.floor((double) tags.size() / tagsPerPage);
        int itemStackMin = (page * tagsPerPage);
        int itemStackMax = Math.min((page * tagsPerPage) + tagsPerPage, tags.size());
        displayTags = tags.subList(itemStackMin, itemStackMax);
        int tagStartY = availableItemstartY;

        int slot = 0;
        overSlot = -1;
        for (String tag : displayTags) {
            Minecraft.getInstance().fontRenderer.drawString(stack, tag, availableItemsstartX, tagStartY, Color.BLUE.getRGB());
            //int x = availableItemsstartX;
            //int y = availableItemstartY + row * 18;

            if (MiscTools.inBounds(availableItemsstartX, tagStartY - 2, 160, 8, mouseX, mouseY)) {
                overSlot = slot;
                color = -2130706433;// : 0xFF5B5B5B;

                stack.push();
                RenderSystem.disableLighting();
                RenderSystem.disableDepthTest();
                RenderSystem.colorMask(true, true, true, false);
                fillGradient(stack, availableItemsstartX - 1, tagStartY - 2, availableItemsstartX + 160, tagStartY + 8, color, color);
                RenderSystem.colorMask(true, true, true, true);
                stack.pop();
            }

            if (slot == selectedSlot) {
                color = 0xFFFF0000;

                stack.push();
                RenderSystem.disableLighting();
                RenderSystem.disableDepthTest();
                RenderSystem.colorMask(true, true, true, false);

                int x1 = availableItemsstartX + 160;
                int y1 = tagStartY + 10;
                hLine(stack, availableItemsstartX - 2, x1 - 0, tagStartY - 2, color);
                hLine(stack, availableItemsstartX - 2, x1 - 0, y1 - 2, color);
                vLine(stack, availableItemsstartX - 2, tagStartY - 2, y1 - 2, color);
                vLine(stack, x1 - 0, tagStartY - 2, y1 - 2, color);

                RenderSystem.colorMask(true, true, true, true);
                stack.pop();
            }

            tagStartY += 10;
            slot++;
        }
    }

    /*@Override
    public void renderBackground(MatrixStack stack) {
        RenderSystem.color4f(1, 1, 1, 1);
        getMinecraft().getTextureManager().bindTexture(getBackground());
        this.blit(stack, guiLeft, guiTop, 0, 0, xSize, ySize);
    }*/

    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack stack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.color4f(1, 1, 1, 1);
        getMinecraft().getTextureManager().bindTexture(getBackground());
        this.blit(stack, guiLeft, guiTop, 0, 0, xSize, ySize);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(MatrixStack stack, int x, int y) {
        Minecraft.getInstance().fontRenderer.drawString(stack, I18n.format("item.logisticslasers.tagfilterscreen"), 40, -40, Color.DARK_GRAY.getRGB());
        Minecraft.getInstance().fontRenderer.drawString(stack, new TranslationTextComponent("item.logisticslasers.basicfilterscreen.priority").getString(), -7, -40, Color.DARK_GRAY.getRGB());
        String priority = Integer.toString(container.getPriority());
        Minecraft.getInstance().fontRenderer.drawString(stack, new StringTextComponent(priority).getString(), 8, -27, Color.DARK_GRAY.getRGB());
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        if (!sourceContainer.equals(BlockPos.ZERO))
            PacketHandler.sendToServer(new PacketCardApply(cardSlot, sourceContainer)); //Notify controller of changes to this card
        super.onClose();
    }

    private static TranslationTextComponent getTrans(String key, Object... args) {
        return new TranslationTextComponent(LogisticsLasers.MOD_ID + "." + key, args);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (MiscTools.inBounds(tagField.x, tagField.y, tagField.getWidth(), 15, mouseX, mouseY) && button == 1)
            tagField.setText("");

        if (overSlot >= 0) {
            selectedSlot = overSlot;
            //tagField.setText(displayTags.get(selectedSlot));
            return true;
        }

        if (hoveredSlot instanceof BasicFilterSlot) {
            // By splitting the stack we can get air easily :) perfect removal basically
            ItemStack stack = getMinecraft().player.inventory.getItemStack();
            stack = stack.copy().split(hoveredSlot.getSlotStackLimit()); // Limit to slot limit
            hoveredSlot.putStack(stack); // Temporarily update the client for continuity purposes

            PacketHandler.sendToServer(new PacketFilterSlot(hoveredSlot.slotNumber, stack, stack.getCount()));
        }
        return super.mouseClicked(mouseX, mouseY, button);
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

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        InputMappings.Input mouseKey = InputMappings.getInputByCode(keyCode, scanCode);
        if (this.minecraft.gameSettings.keyBindInventory.isActiveAndMatches(mouseKey)) {
            if (tagField.isFocused())
                return true;
            else {
                closeScreen();
                return true;
            }
        }
        if (tagField.isFocused() && (keyCode == 257 || keyCode == 335)) { //enter key
            if (!tagField.getText().isEmpty()) {
                PacketHandler.sendToServer(new PacketButtonAdd(cardSlot, sourceContainer, tagField.getText().toLowerCase(Locale.ROOT)));
                CardInserterTag.addTag(card, tagField.getText().toLowerCase(Locale.ROOT));
                tagField.setText("");
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

}
