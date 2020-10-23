package com.direwolf20.logisticslasers.common.network.packets;

import com.direwolf20.logisticslasers.client.renders.LaserConnections;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketUpdateLaserRender {
    public PacketUpdateLaserRender() {

    }

    public static void encode(PacketUpdateLaserRender msg, PacketBuffer buffer) {

    }

    public static PacketUpdateLaserRender decode(PacketBuffer buffer) {
        return new PacketUpdateLaserRender();
    }

    public static class Handler {
        public static void handle(PacketUpdateLaserRender msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> clientPacketHandler(msg)));
            ctx.get().setPacketHandled(true);
        }
    }

    public static void clientPacketHandler(PacketUpdateLaserRender msg) {
        LaserConnections.buildLaserList();
    }
}
