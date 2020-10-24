package com.direwolf20.logisticslasers.common.util;

import com.direwolf20.logisticslasers.common.items.logiccards.BaseCard;
import com.direwolf20.logisticslasers.common.items.logiccards.CardInserterMod;
import com.direwolf20.logisticslasers.common.items.logiccards.CardInserterTag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class MiscTools {
    public static boolean isStackValidForCard(ItemStack filterCard, ItemStack testStack) {
        Set<ItemStack> filteredItems = BaseCard.getFilteredItems(filterCard); //Get the list of items this card allows
        boolean whiteList = BaseCard.getWhiteList(filterCard);
        if (filterCard.getItem() instanceof CardInserterTag) {
            List<String> tags = new ArrayList<>(CardInserterTag.getTags(filterCard));
            for (ResourceLocation tag : testStack.getItem().getTags()) {
                if (tags.contains(tag.toString()))
                    return whiteList;
            }
        } else {
            for (ItemStack stack : filteredItems) {
                if (filterCard.getItem() instanceof CardInserterMod) {
                    if (Objects.equals(stack.getItem().getCreatorModId(stack), testStack.getItem().getCreatorModId(testStack)))
                        return whiteList;
                } else if (BaseCard.getNBTFilter(filterCard)) {
                    if (ItemHandlerHelper.canItemStacksStack(stack, testStack))
                        return whiteList;
                } else {
                    if (stack.isItemEqual(testStack))
                        return whiteList;
                }
            }
        }
        return !whiteList;

    }

    public static boolean inBounds(int x, int y, int w, int h, double ox, double oy) {
        return ox >= x && ox <= x + w && oy >= y && oy <= y + h;
    }

    public static ListNBT stringListToNBT(List<String> list) {
        ListNBT nbtList = new ListNBT();
        for (String string : list) {
            CompoundNBT tag = new CompoundNBT();
            tag.putString("list", string);
            nbtList.add(tag);
        }
        return nbtList;
    }

    public static List<String> NBTToStringList(ListNBT nbtList) {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < nbtList.size(); i++) {
            CompoundNBT tag = nbtList.getCompound(i);
            list.add(tag.getString("list"));
        }
        return list;
    }
}
