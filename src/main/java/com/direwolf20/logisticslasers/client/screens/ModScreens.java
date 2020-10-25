package com.direwolf20.logisticslasers.client.screens;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public class ModScreens {
    public static void openPolymorphScreen(ItemStack itemstack) {
        Minecraft.getInstance().displayGuiScreen(new PolymorphScreen(itemstack));
    }

    public static void openPolymorphScreen(ItemStack itemstack, BlockPos pos, int slot) {
        Minecraft.getInstance().displayGuiScreen(new PolymorphScreen(itemstack, pos, slot));
    }
}
