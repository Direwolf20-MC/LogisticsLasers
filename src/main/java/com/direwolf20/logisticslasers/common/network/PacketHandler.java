package com.direwolf20.logisticslasers.common.network;

import com.direwolf20.logisticslasers.LogisticsLasers;
import com.direwolf20.logisticslasers.common.network.packets.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = Integer.toString(2);
    private static short index = 0;

    public static final SimpleChannel HANDLER = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(LogisticsLasers.MOD_ID, "main_network_channel"))
            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .simpleChannel();

    public static void register() {
        //Going to Server side
        registerMessage(PacketFilterSlot.class, PacketFilterSlot::encode, PacketFilterSlot::decode, PacketFilterSlot.Handler::handle);
        registerMessage(PacketOpenFilter.class, PacketOpenFilter::encode, PacketOpenFilter::decode, PacketOpenFilter.Handler::handle);
        registerMessage(PacketChangePriority.class, PacketChangePriority::encode, PacketChangePriority::decode, PacketChangePriority.Handler::handle);
        registerMessage(PacketToggleWhitelist.class, PacketToggleWhitelist::encode, PacketToggleWhitelist::decode, PacketToggleWhitelist.Handler::handle);
        registerMessage(PacketDoCraft.class, PacketDoCraft::encode, PacketDoCraft::decode, PacketDoCraft.Handler::handle);
        registerMessage(PacketRequestItem.class, PacketRequestItem::encode, PacketRequestItem::decode, PacketRequestItem.Handler::handle);
        registerMessage(PacketRequestGrid.class, PacketRequestGrid::encode, PacketRequestGrid::decode, PacketRequestGrid.Handler::handle);
        registerMessage(PacketRequestGridMissing.class, PacketRequestGridMissing::encode, PacketRequestGridMissing::decode, PacketRequestGridMissing.Handler::handle);
        registerMessage(PacketItemCountsRefresh.class, PacketItemCountsRefresh::encode, PacketItemCountsRefresh::decode, PacketItemCountsRefresh.Handler::handle);

        //Going to Client Side
        registerMessage(PacketItemCountsSync.class, PacketItemCountsSync::encode, PacketItemCountsSync::decode, PacketItemCountsSync.Handler::handle);
    }

    public static void sendTo(Object msg, ServerPlayerEntity player) {
        if (!(player instanceof FakePlayer))
            HANDLER.sendTo(msg, player.connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
    }

    public static void sendToAll(Object msg, World world) {
        //Todo Maybe only send to nearby players?
        for (PlayerEntity player : world.getPlayers()) {
            if (!(player instanceof FakePlayer))
                HANDLER.sendTo(msg, ((ServerPlayerEntity) player).connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
        }
    }

    public static void sendToServer(Object msg) {
        HANDLER.sendToServer(msg);
    }

    private static <MSG> void registerMessage(Class<MSG> messageType, BiConsumer<MSG, PacketBuffer> encoder, Function<PacketBuffer, MSG> decoder, BiConsumer<MSG, Supplier<NetworkEvent.Context>> messageConsumer) {
        HANDLER.registerMessage(index, messageType, encoder, decoder, messageConsumer);
        index++;
        if (index > 0xFF)
            throw new RuntimeException("Too many messages!");
    }
}
