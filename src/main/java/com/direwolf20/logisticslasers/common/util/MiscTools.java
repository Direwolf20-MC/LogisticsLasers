package com.direwolf20.logisticslasers.common.util;

import com.direwolf20.logisticslasers.common.items.logiccards.BaseCard;
import net.minecraft.item.ItemStack;

import java.util.Set;

public class MiscTools {
    public static boolean isStackValidForCard(ItemStack filterCard, ItemStack testStack) {
        Set<ItemStack> filteredItems = BaseCard.getFilteredItems(filterCard); //Get the list of items this card allows
        boolean whiteList = BaseCard.getWhiteList(filterCard);
        if (whiteList) {
            for (ItemStack stack : filteredItems) {
                if (stack.isItemEqual(testStack))
                    return true;
            }
            return false;
        } else {
            for (ItemStack stack : filteredItems) {
                if (stack.isItemEqual(testStack))
                    return false;
            }
            return true;
        }

    }

    public static boolean inBounds(int x, int y, int w, int h, double ox, double oy) {
        return ox >= x && ox <= x + w && oy >= y && oy <= y + h;
    }

    /*public static boolean isItemStackEqualNoCount(ItemStack stack, ItemStack other) {
        if (stack.getItem() != other.getItem()) {
            return false;
        } else if (stack.getTag() == null && other.getTag() != null) {
            return false;
        } else {
            return (stack.getTag() == null || stack.getTag().equals(other.getTag())) && stack.areCapsCompatible(other);
        }
    }*/
}
