package com.direwolf20.logisticslasers.common.network.packets;

import com.direwolf20.logisticslasers.common.container.cards.BasicFilterContainer;
import com.direwolf20.logisticslasers.common.items.logiccards.BaseCard;
import com.direwolf20.logisticslasers.common.items.logiccards.CardInserterTag;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketToggleWhitelist {
    private int slotNumber;

    public PacketToggleWhitelist() {
        this.slotNumber = -1;
    }

    public PacketToggleWhitelist(int slot) {
        this.slotNumber = slot;
    }

    public static void encode(PacketToggleWhitelist msg, PacketBuffer buffer) {
        buffer.writeInt(msg.slotNumber);
    }

    public static PacketToggleWhitelist decode(PacketBuffer buffer) {
        return new PacketToggleWhitelist(buffer.readInt());
    }

    public static class Handler {
        public static void handle(PacketToggleWhitelist msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayerEntity sender = ctx.get().getSender();
                if (sender == null)
                    return;

                Container container = sender.openContainer;

                if (msg.slotNumber != -1) {
                    Slot slot = container.inventorySlots.get(msg.slotNumber);
                    ItemStack itemStack = slot.getStack();

                    if (itemStack.getItem() instanceof CardInserterTag) {
                        BaseCard.setWhiteList(itemStack, !BaseCard.getWhiteList(itemStack));
                    }
                }

                if (container instanceof BasicFilterContainer) {
                    ItemStack itemStack = ((BasicFilterContainer) container).filterItemStack;
                    BaseCard.setWhiteList(itemStack, !BaseCard.getWhiteList(itemStack));
                }


            });

            ctx.get().setPacketHandled(true);
        }
    }
}
