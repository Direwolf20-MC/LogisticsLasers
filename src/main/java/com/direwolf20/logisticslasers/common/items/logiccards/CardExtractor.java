package com.direwolf20.logisticslasers.common.items.logiccards;

import com.direwolf20.logisticslasers.common.container.BasicFilterContainer;
import com.direwolf20.logisticslasers.common.container.customhandler.FilterSlotHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class CardExtractor extends BaseCard {
    public CardExtractor() {
        super();
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        ItemStack itemstack = player.getHeldItem(hand);
        if (world.isRemote) return new ActionResult<>(ActionResultType.PASS, itemstack);
        FilterSlotHandler handler = getInventory(itemstack);
        NetworkHooks.openGui((ServerPlayerEntity) player, new SimpleNamedContainerProvider(
                (windowId, playerInventory, playerEntity) -> new BasicFilterContainer(itemstack, windowId, playerInventory, handler), new StringTextComponent("")));
        return new ActionResult<>(ActionResultType.PASS, itemstack);
    }

    public static FilterSlotHandler getInventory(ItemStack stack) {
        CompoundNBT compound = stack.getOrCreateTag();
        FilterSlotHandler handler = new FilterSlotHandler(BasicFilterContainer.SLOTS, stack);
        handler.deserializeNBT(compound.getCompound("inv"));
        return !compound.contains("inv") ? setInventory(stack, new FilterSlotHandler(BasicFilterContainer.SLOTS, stack)) : handler;
    }
}
