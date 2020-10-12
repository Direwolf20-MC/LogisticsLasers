package com.direwolf20.logisticslasers.common.network.packets;

import com.direwolf20.logisticslasers.common.container.CraftingStationContainer;
import com.direwolf20.logisticslasers.common.tiles.CraftingStationTile;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketRequestGridMissing {

    public PacketRequestGridMissing() {

    }

    public static void encode(PacketRequestGridMissing msg, PacketBuffer buffer) {
    }

    public static PacketRequestGridMissing decode(PacketBuffer buffer) {
        return new PacketRequestGridMissing();
    }

    public static class Handler {
        public static void handle(PacketRequestGridMissing msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayerEntity sender = ctx.get().getSender();
                if (sender == null)
                    return;

                Container container = sender.openContainer;
                if (container == null)
                    return;

                if (container instanceof CraftingStationContainer) {
                    CraftingStationTile te = ((CraftingStationContainer) container).tile;
                    te.requestGridOnlyMissing(sender);
                }


            });

            ctx.get().setPacketHandled(true);
        }
    }
}
