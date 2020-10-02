package com.direwolf20.logisticslasers.common.network.packets;

import com.direwolf20.logisticslasers.common.container.BasicFilterContainer;
import com.direwolf20.logisticslasers.common.items.logiccards.BaseCard;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketChangePriority {
    private int priorityChange;

    public PacketChangePriority(int priorityChange) {
        this.priorityChange = priorityChange;
    }

    public static void encode(PacketChangePriority msg, PacketBuffer buffer) {
        buffer.writeInt(msg.priorityChange);
    }

    public static PacketChangePriority decode(PacketBuffer buffer) {
        return new PacketChangePriority(buffer.readInt());
    }

    public static class Handler {
        public static void handle(PacketChangePriority msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayerEntity sender = ctx.get().getSender();
                if (sender == null)
                    return;

                Container container = sender.openContainer;
                if (container == null)
                    return;

                if (container instanceof BasicFilterContainer) {
                    ItemStack itemStack = ((BasicFilterContainer) container).filterItemStack;
                    BaseCard.setPriority(itemStack, BaseCard.getPriority(itemStack) + msg.priorityChange);
                }


            });

            ctx.get().setPacketHandled(true);
        }
    }
}
