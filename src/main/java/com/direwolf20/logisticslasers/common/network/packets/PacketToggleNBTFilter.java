package com.direwolf20.logisticslasers.common.network.packets;

import com.direwolf20.logisticslasers.common.container.cards.BasicFilterContainer;
import com.direwolf20.logisticslasers.common.items.logiccards.BaseCard;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketToggleNBTFilter {

    public PacketToggleNBTFilter() {
    }

    public static void encode(PacketToggleNBTFilter msg, PacketBuffer buffer) {
    }

    public static PacketToggleNBTFilter decode(PacketBuffer buffer) {
        return new PacketToggleNBTFilter();
    }

    public static class Handler {
        public static void handle(PacketToggleNBTFilter msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayerEntity sender = ctx.get().getSender();
                if (sender == null)
                    return;

                Container container = sender.openContainer;
                if (container == null)
                    return;

                if (container instanceof BasicFilterContainer) {
                    ItemStack itemStack = ((BasicFilterContainer) container).filterItemStack;
                    BaseCard.setNBTFilter(itemStack, !BaseCard.getNBTFilter(itemStack));
                }


            });

            ctx.get().setPacketHandled(true);
        }
    }
}
