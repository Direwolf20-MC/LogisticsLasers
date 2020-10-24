package com.direwolf20.logisticslasers.common.items.logiccards;

import com.direwolf20.logisticslasers.client.screens.ModScreens;
import com.direwolf20.logisticslasers.common.util.MiscTools;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.List;

public class CardInserterTag extends CardInserter {
    public CardInserterTag() {
        super();
        CARDTYPE = CardType.INSERT;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        ItemStack itemStack = player.getHeldItem(hand);
        if (world.isRemote) return new ActionResult<>(ActionResultType.PASS, itemStack);
        ModScreens.openInsertTagScreen(itemStack);
        return new ActionResult<>(ActionResultType.PASS, itemStack);
    }

    public static void addTag(ItemStack card, String tag) {
        List<String> tags = getTags(card);
        if (!tags.contains(tag)) {
            tags.add(tag);
            CompoundNBT compound = card.getOrCreateTag();
            compound.put("tags", MiscTools.stringListToNBT(tags));
        }
    }

    public static void removeTag(ItemStack card, String tag) {
        List<String> tags = getTags(card);
        if (tags.contains(tag)) {
            tags.remove(tag);
            CompoundNBT compound = card.getOrCreateTag();
            compound.put("tags", MiscTools.stringListToNBT(tags));
        }
    }

    public static void clearTags(ItemStack card) {
        List<String> tags = new ArrayList();
        CompoundNBT compound = card.getOrCreateTag();
        compound.put("tags", MiscTools.stringListToNBT(tags));
    }

    public static List<String> getTags(ItemStack card) {
        List<String> tags = new ArrayList();
        CompoundNBT compound = card.getOrCreateTag();
        if (compound.contains("tags")) {
            ListNBT listNBT = compound.getList("tags", Constants.NBT.TAG_COMPOUND);
            tags = new ArrayList<>(MiscTools.NBTToStringList(listNBT));
        } else {
            compound.put("tags", MiscTools.stringListToNBT(tags));
        }
        return tags;
    }


}
