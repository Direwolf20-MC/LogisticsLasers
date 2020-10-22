package com.direwolf20.logisticslasers.common.network.packets;

import com.direwolf20.logisticslasers.common.container.cards.BasicFilterContainer;
import com.direwolf20.logisticslasers.common.items.logiccards.BaseCard;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketChangeExtractAmt {
    private int extractChange;

    public PacketChangeExtractAmt(int extractChange) {
        this.extractChange = extractChange;
    }

    public static void encode(PacketChangeExtractAmt msg, PacketBuffer buffer) {
        buffer.writeInt(msg.extractChange);
    }

    public static PacketChangeExtractAmt decode(PacketBuffer buffer) {
        return new PacketChangeExtractAmt(buffer.readInt());
    }

    public static class Handler {
        public static void handle(PacketChangeExtractAmt msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayerEntity sender = ctx.get().getSender();
                if (sender == null)
                    return;

                Container container = sender.openContainer;
                if (container == null)
                    return;

                if (container instanceof BasicFilterContainer) {
                    ItemStack itemStack = ((BasicFilterContainer) container).filterItemStack;
                    BaseCard.setExtractAmt(itemStack, BaseCard.getExtractAmt(itemStack) + msg.extractChange);
                }


            });

            ctx.get().setPacketHandled(true);
        }
    }
}
