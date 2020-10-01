package com.direwolf20.logisticslasers.common.items.logiccards;

import com.direwolf20.logisticslasers.LogisticsLasers;
import com.direwolf20.logisticslasers.common.container.BasicFilterContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.ItemStackHandler;

import java.util.HashSet;
import java.util.Set;

public abstract class BaseCard extends Item {

    protected BaseCard.CardType CARDTYPE;

    public BaseCard() {
        super(new Item.Properties().maxStackSize(64).group(LogisticsLasers.itemGroup));
    }

    public enum CardType {
        EXTRACT,
        INSERT,
        CRAFT
    }

    public BaseCard(Properties prop) {
        super(prop);
    }

    public CardType getCardType() {
        return CARDTYPE;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        ItemStack itemstack = player.getHeldItem(hand);
        if (world.isRemote) return new ActionResult<>(ActionResultType.PASS, itemstack);
        ItemStackHandler handler = getInventory(itemstack);
        NetworkHooks.openGui((ServerPlayerEntity) player, new SimpleNamedContainerProvider(
                (windowId, playerInventory, playerEntity) -> new BasicFilterContainer(itemstack, windowId, playerInventory, handler), new StringTextComponent("")));
        return new ActionResult<>(ActionResultType.PASS, itemstack);
    }

    public static ItemStackHandler setInventory(ItemStack stack, ItemStackHandler handler) {
        stack.getOrCreateTag().put("inv", handler.serializeNBT());
        return handler;
    }

    public static ItemStackHandler getInventory(ItemStack stack) {
        CompoundNBT compound = stack.getOrCreateTag();
        ItemStackHandler handler = new ItemStackHandler(BasicFilterContainer.SLOTS);
        handler.deserializeNBT(compound.getCompound("inv"));
        return !compound.contains("inv") ? setInventory(stack, new ItemStackHandler(BasicFilterContainer.SLOTS)) : handler;
    }

    public static Set<Item> getFilteredItems(ItemStack stack) {
        Set<Item> filteredItems = new HashSet<>();
        ItemStackHandler filterSlotHandler = getInventory(stack);
        for (int i = 0; i < filterSlotHandler.getSlots(); i++) {
            ItemStack itemStack = filterSlotHandler.getStackInSlot(i);
            if (!itemStack.isEmpty())
                filteredItems.add(itemStack.getItem());
        }
        return filteredItems;
    }
}
