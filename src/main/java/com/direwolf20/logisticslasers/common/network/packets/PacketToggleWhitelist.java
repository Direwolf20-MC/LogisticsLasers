package com.direwolf20.logisticslasers.common.network.packets;

import com.direwolf20.logisticslasers.common.container.cards.BasicFilterContainer;
import com.direwolf20.logisticslasers.common.container.cards.TagFilterContainer;
import com.direwolf20.logisticslasers.common.items.logiccards.BaseCard;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketToggleWhitelist {

    public PacketToggleWhitelist() {
    }

    public static void encode(PacketToggleWhitelist msg, PacketBuffer buffer) {
    }

    public static PacketToggleWhitelist decode(PacketBuffer buffer) {
        return new PacketToggleWhitelist();
    }

    public static class Handler {
        public static void handle(PacketToggleWhitelist msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayerEntity sender = ctx.get().getSender();
                if (sender == null)
                    return;

                Container container = sender.openContainer;
                if (container == null) return;
                ItemStack itemStack;

                if (container instanceof TagFilterContainer) {
                    itemStack = ((TagFilterContainer) container).filterItemStack;
                } else {
                    itemStack = ((BasicFilterContainer) container).filterItemStack;
                }
                if (itemStack.getItem() instanceof BaseCard) {
                    BaseCard.setWhiteList(itemStack, !BaseCard.getWhiteList(itemStack));
                }


            });

            ctx.get().setPacketHandled(true);
        }
    }
}
