package com.direwolf20.logisticslasers.common.network.packets;

import com.direwolf20.logisticslasers.common.container.CraftingStationContainer;
import com.direwolf20.logisticslasers.common.container.customhandler.CraftingStationHandler;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketClearGrid {

    public PacketClearGrid() {

    }

    public static void encode(PacketClearGrid msg, PacketBuffer buffer) {
    }

    public static PacketClearGrid decode(PacketBuffer buffer) {
        return new PacketClearGrid();
    }

    public static class Handler {
        public static void handle(PacketClearGrid msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayerEntity sender = ctx.get().getSender();
                if (sender == null)
                    return;

                Container container = sender.openContainer;
                if (container == null)
                    return;

                if (container instanceof CraftingStationContainer) {
                    CraftingStationHandler craftMatrixHandler = ((CraftingStationContainer) container).craftingHandler;
                    for (int i = 0; i < craftMatrixHandler.getSlots(); i++) {
                        craftMatrixHandler.setStackInSlot(i, ItemStack.EMPTY);
                    }
                }


            });

            ctx.get().setPacketHandled(true);
        }
    }
}
