package com.direwolf20.logisticslasers.common.items.logiccards;

import com.direwolf20.logisticslasers.common.container.cards.BasicFilterContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.IIntArray;
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
                (windowId, playerInventory, playerEntity) -> new BasicFilterContainer(itemStack, windowId, playerInventory, handler, tempArray), new StringTextComponent("")), (buf -> {
            buf.writeItemStack(itemStack);
        }));
        return new ActionResult<>(ActionResultType.PASS, itemStack);
    }
}
