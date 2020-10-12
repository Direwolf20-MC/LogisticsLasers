package com.direwolf20.logisticslasers.common.network.packets;

import com.direwolf20.logisticslasers.common.container.CraftingStationContainer;
import com.direwolf20.logisticslasers.common.tiles.CraftingStationTile;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketRequestGrid {
    int amount;

    public PacketRequestGrid(int amt) {
        this.amount = amt;
    }

    public static void encode(PacketRequestGrid msg, PacketBuffer buffer) {
        buffer.writeInt(msg.amount);
    }

    public static PacketRequestGrid decode(PacketBuffer buffer) {
        return new PacketRequestGrid(buffer.readInt());
    }

    public static class Handler {
        public static void handle(PacketRequestGrid msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayerEntity sender = ctx.get().getSender();
                if (sender == null)
                    return;

                Container container = sender.openContainer;
                if (container == null)
                    return;

                if (container instanceof CraftingStationContainer) {
                    CraftingStationTile te = ((CraftingStationContainer) container).tile;
                    te.requestGrid(msg.amount, sender);
                }


            });

            ctx.get().setPacketHandled(true);
        }
    }
}
