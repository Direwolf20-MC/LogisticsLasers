package com.direwolf20.logisticslasers.common.network.packets;

import com.direwolf20.logisticslasers.common.container.cards.TagFilterContainer;
import com.direwolf20.logisticslasers.common.items.logiccards.BaseCard;
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

                ItemStack itemStack;

                if (container instanceof TagFilterContainer) {
                    itemStack = ((TagFilterContainer) container).filterItemStack;
                } else {
                    if (msg.slotNumber == -1) {
                        itemStack = sender.getHeldItemMainhand();
                        if (!(itemStack.getItem() instanceof BaseCard))
                            itemStack = sender.getHeldItemOffhand();
                    } else {
                        Slot slot = container.inventorySlots.get(msg.slotNumber);
                        itemStack = slot.getStack();
                    }
                }
                if (itemStack.getItem() instanceof BaseCard) {
                    BaseCard.setWhiteList(itemStack, !BaseCard.getWhiteList(itemStack));
                }


            });

            ctx.get().setPacketHandled(true);
        }
    }
}
