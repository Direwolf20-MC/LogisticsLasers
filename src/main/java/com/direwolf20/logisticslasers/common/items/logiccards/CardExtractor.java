package com.direwolf20.logisticslasers.common.items.logiccards;

import com.direwolf20.logisticslasers.common.container.BasicFilterContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.ItemStackHandler;

public class CardExtractor extends BaseCard {

    public CardExtractor() {
        super();
        CARDTYPE = CardType.EXTRACT;
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

}
