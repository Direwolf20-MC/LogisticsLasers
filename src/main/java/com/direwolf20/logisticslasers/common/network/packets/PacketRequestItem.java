package com.direwolf20.logisticslasers.common.network.packets;

import com.direwolf20.logisticslasers.common.container.CraftingStationContainer;
import com.direwolf20.logisticslasers.common.tiles.CraftingStationTile;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketRequestItem {
    ItemStack stack;
    int amount;

    public PacketRequestItem(ItemStack stack, int amt) {
        this.stack = stack;
        this.amount = amt;
    }

    public static void encode(PacketRequestItem msg, PacketBuffer buffer) {
        buffer.writeItemStack(msg.stack);
        buffer.writeInt(msg.amount);
    }

    public static PacketRequestItem decode(PacketBuffer buffer) {
        return new PacketRequestItem(buffer.readItemStack(), buffer.readInt());
    }

    public static class Handler {
        public static void handle(PacketRequestItem msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayerEntity sender = ctx.get().getSender();
                if (sender == null)
                    return;

                Container container = sender.openContainer;
                if (container == null)
                    return;

                if (container instanceof CraftingStationContainer) {
                    CraftingStationTile te = ((CraftingStationContainer) container).tile;
                    ItemStack stack = msg.stack;
                    stack.setCount(msg.amount);
                    te.requestItem(msg.stack, sender);
                }


            });

            ctx.get().setPacketHandled(true);
        }
    }
}
