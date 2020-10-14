package com.direwolf20.logisticslasers.common.network.packets;

import com.direwolf20.logisticslasers.common.container.CraftingStationContainer;
import com.direwolf20.logisticslasers.common.tiles.ControllerTile;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.items.ItemStackHandler;

import java.util.function.Supplier;

public class PacketSortStackFromCrafter {
    private int slot;

    public PacketSortStackFromCrafter(int slot) {
        this.slot = slot;
    }

    public static void encode(PacketSortStackFromCrafter msg, PacketBuffer buffer) {
        buffer.writeInt(msg.slot);
    }

    public static PacketSortStackFromCrafter decode(PacketBuffer buffer) {
        return new PacketSortStackFromCrafter(buffer.readInt());
    }

    public static class Handler {
        public static void handle(PacketSortStackFromCrafter msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayerEntity sender = ctx.get().getSender();
                if (sender == null)
                    return;

                Container container = sender.openContainer;
                if (container == null)
                    return;

                if (container instanceof CraftingStationContainer) {
                    ItemStackHandler handler = ((CraftingStationContainer) container).handler;
                    ItemStack stack = handler.getStackInSlot(msg.slot);
                    ControllerTile te = ((CraftingStationContainer) container).tile.getControllerTE();
                    if (te != null) {
                        te.extractItemFromPos(stack, ((CraftingStationContainer) container).tile.getPos(), msg.slot);
                    }
                }


            });

            ctx.get().setPacketHandled(true);
        }
    }
}
