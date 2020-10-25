package com.direwolf20.logisticslasers.common.items.logiccards;

import com.direwolf20.logisticslasers.common.container.cards.TagFilterContainer;
import com.direwolf20.logisticslasers.common.util.MiscTools;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.IIntArray;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.ItemStackHandler;

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
        ItemStackHandler handler = getInventory(itemStack);
        IIntArray tempArray = new IIntArray() {
            @Override
            public int get(int index) {
                switch (index) {
                    case 0:
                        return getPriority(itemStack);
                    case 1:
                        return getExtractAmt(itemStack);
                    default:
                        throw new IllegalArgumentException("Invalid index: " + index);
                }
            }

            @Override
            public void set(int index, int value) {
                throw new IllegalStateException("Cannot set values through IIntArray");
            }

            @Override
            public int size() {
                return 2;
            }
        };
        NetworkHooks.openGui((ServerPlayerEntity) player, new SimpleNamedContainerProvider(
                (windowId, playerInventory, playerEntity) -> new TagFilterContainer(itemStack, windowId, playerInventory, handler, tempArray), new StringTextComponent("")), (buf -> {
            buf.writeItemStack(itemStack);
        }));
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
