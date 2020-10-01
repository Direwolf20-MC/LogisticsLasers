package com.direwolf20.logisticslasers.common.network.packets;

import com.direwolf20.logisticslasers.common.container.BasicFilterContainer;
import com.direwolf20.logisticslasers.common.container.customhandler.FilterSlotHandler;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkHooks;

import java.util.function.Supplier;

import static com.direwolf20.logisticslasers.common.items.logiccards.BaseCard.getInventory;

public class PacketOpenFilter {
    private int slotNumber;

    public PacketOpenFilter(int slotNumber) {
        this.slotNumber = slotNumber;
    }

    public static void encode(PacketOpenFilter msg, PacketBuffer buffer) {
        buffer.writeInt(msg.slotNumber);
    }

    public static PacketOpenFilter decode(PacketBuffer buffer) {
        return new PacketOpenFilter(buffer.readInt());
    }

    public static class Handler {
        public static void handle(PacketOpenFilter msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayerEntity sender = ctx.get().getSender();
                if (sender == null)
                    return;

                Container container = sender.openContainer;
                if (container == null)
                    return;

                Slot slot = container.inventorySlots.get(msg.slotNumber);
                ItemStack itemStack = slot.getStack();

                FilterSlotHandler handler = getInventory(itemStack);
                NetworkHooks.openGui(sender, new SimpleNamedContainerProvider(
                        (windowId, playerInventory, playerEntity) -> new BasicFilterContainer(itemStack, windowId, playerInventory, handler), new StringTextComponent("")));
            });

            ctx.get().setPacketHandled(true);
        }
    }
}
