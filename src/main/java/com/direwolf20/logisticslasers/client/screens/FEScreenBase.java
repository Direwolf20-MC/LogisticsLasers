package com.direwolf20.logisticslasers.client.screens;

import com.direwolf20.logisticslasers.LogisticsLasers;
import com.direwolf20.logisticslasers.common.container.FEContainerBase;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public abstract class FEScreenBase<T extends FEContainerBase> extends ContainerScreen<T> {
    private static final ResourceLocation background = new ResourceLocation(LogisticsLasers.MOD_ID, "textures/gui/controller.png");

    protected final T container;

    public FEScreenBase(T container, PlayerInventory playerInventory, ITextComponent title) {
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

        this.renderHoveredTooltip(stack, mouseX, mouseY); // @mcp: func_230459_a_ = renderHoveredToolTip
        /*if (mouseX > (guiLeft + 7) && mouseX < (guiLeft + 7) + 18 && mouseY > (guiTop + 7) && mouseY < (guiTop + 7) + 73)
            if (Screen.hasShiftDown())
                this.renderTooltip(stack, LanguageMap.getInstance().func_244260_a(Arrays.asList(
                        new TranslationTextComponent("screen.logisticslasers.energy", this.container.getEnergy(), MagicHelpers.withSuffix(this.container.getMaxPower())))
                ), mouseX, mouseY);
            else
                this.renderTooltip(stack, LanguageMap.getInstance().func_244260_a(Arrays.asList(
                        new TranslationTextComponent("screen.logisticslasers.energy", MagicHelpers.withSuffix(this.container.getEnergy()), MagicHelpers.withSuffix(this.container.getMaxPower())))
                ), mouseX, mouseY);*/
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

        int maxEnergy = this.container.getMaxPower(), height = 70;
        if (maxEnergy > 0) {
            int remaining = (this.container.getEnergy() * height) / maxEnergy;
            this.blit(stack, guiLeft + 8, guiTop + 78 - remaining, 176, 84 - remaining, 16, remaining + 1);
        }
    }

    protected static TranslationTextComponent getTrans(String key, Object... args) {
        return new TranslationTextComponent(LogisticsLasers.MOD_ID + "." + key, args);
    }
}
