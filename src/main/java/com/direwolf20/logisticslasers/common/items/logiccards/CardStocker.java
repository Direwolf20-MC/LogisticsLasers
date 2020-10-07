package com.direwolf20.logisticslasers.common.items.logiccards;

import com.direwolf20.logisticslasers.common.container.cards.BasicFilterContainer;
import com.direwolf20.logisticslasers.common.container.cards.StockerFilterContainer;
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

public class CardStocker extends BaseCard {

    public CardStocker() {
        super();
        CARDTYPE = CardType.STOCK;
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
                return 1;
            }
        };
        NetworkHooks.openGui((ServerPlayerEntity) player, new SimpleNamedContainerProvider(
                (windowId, playerInventory, playerEntity) -> new StockerFilterContainer(itemStack, windowId, playerInventory, handler, tempArray), new StringTextComponent("")), (buf -> {
            buf.writeItemStack(itemStack);
        }));
        return new ActionResult<>(ActionResultType.PASS, itemStack);
    }

    public static ItemStackHandler setInventory(ItemStack stack, ItemStackHandler handler) {
        stack.getOrCreateTag().put("inv", handler.serializeNBT());
        ListNBT countList = new ListNBT();
        for (int i = 0; i < handler.getSlots(); i++) {
            CompoundNBT countTag = new CompoundNBT();
            ItemStack itemStack = handler.getStackInSlot(i);
            if (itemStack.getCount() > itemStack.getMaxStackSize()) {
                countTag.putInt("Slot", i);
                countTag.putInt("Count", itemStack.getCount());
                countList.add(countTag);
            }
        }
        stack.getOrCreateTag().put("counts", countList);
        return handler;
    }

    public static ItemStackHandler getInventory(ItemStack stack) {
        CompoundNBT compound = stack.getOrCreateTag();
        ItemStackHandler handler = new ItemStackHandler(BasicFilterContainer.SLOTS);
        handler.deserializeNBT(compound.getCompound("inv"));
        ListNBT countList = compound.getList("counts", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < countList.size(); i++) {
            CompoundNBT countTag = countList.getCompound(i);
            int slot = countTag.getInt("Slot");
            ItemStack itemStack = handler.getStackInSlot(slot);
            itemStack.setCount(countTag.getInt("Count"));
            handler.setStackInSlot(slot, itemStack);
        }
        return !compound.contains("inv") ? setInventory(stack, new ItemStackHandler(BasicFilterContainer.SLOTS)) : handler;
    }
}
