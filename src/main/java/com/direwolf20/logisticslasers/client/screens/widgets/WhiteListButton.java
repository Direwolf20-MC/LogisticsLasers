package com.direwolf20.logisticslasers.client.screens.widgets;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;

public class WhiteListButton extends Button {
    private boolean isWhitelist;

    public WhiteListButton(int widthIn, int heightIn, int width, int height, boolean isWhitelist, IPressable onPress) {
        super(widthIn, heightIn, width, height, StringTextComponent.EMPTY, onPress);
        this.isWhitelist = isWhitelist;
    }

    @Override
    public void render(MatrixStack stack, int p_renderButton_1_, int p_renderButton_2_, float p_renderButton_3_) {
        RenderSystem.disableTexture();
        RenderSystem.color4f(.4f, .4f, .4f, 1f);
        this.blit(stack, this.x, this.y, 0, 0, this.width, this.height);

        if (this.isWhitelist)
            RenderSystem.color4f(1f, 1f, 1f, 1f);
        else
            RenderSystem.color4f(0f, 0f, 0f, 1f);

        this.blit(stack, this.x + 2, this.y + 2, 0, 0, this.width - 4, this.height - 4);
        RenderSystem.enableTexture();
    }

    public void setWhitelist(boolean whitelist) {
        isWhitelist = whitelist;
    }
}
