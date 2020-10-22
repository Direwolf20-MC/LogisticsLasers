package com.direwolf20.logisticslasers.common.util;

import com.direwolf20.logisticslasers.common.items.logiccards.BaseCard;
import com.direwolf20.logisticslasers.common.items.logiccards.CardInserterMod;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.Objects;
import java.util.Set;

public class MiscTools {
    public static boolean isStackValidForCard(ItemStack filterCard, ItemStack testStack) {
        Set<ItemStack> filteredItems = BaseCard.getFilteredItems(filterCard); //Get the list of items this card allows
        boolean whiteList = BaseCard.getWhiteList(filterCard);
            for (ItemStack stack : filteredItems) {
                if (filterCard.getItem() instanceof CardInserterMod) {
                    if (Objects.equals(stack.getItem().getCreatorModId(stack), testStack.getItem().getCreatorModId(testStack)))
                        //if (stack.getItem().getCreatorModId(stack).equals(testStack.getItem().getCreatorModId(testStack)))
                        return whiteList;
                } else if (BaseCard.getNBTFilter(filterCard)) {
                    if (ItemHandlerHelper.canItemStacksStack(stack, testStack))
                        return whiteList;
                } else {
                    if (stack.isItemEqual(testStack))
                        return whiteList;
                }
            }
        return !whiteList;

    }

    public static boolean inBounds(int x, int y, int w, int h, double ox, double oy) {
        return ox >= x && ox <= x + w && oy >= y && oy <= y + h;
    }
}
