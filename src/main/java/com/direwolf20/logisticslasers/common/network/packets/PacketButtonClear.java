package com.direwolf20.logisticslasers.common.network.packets;

import com.direwolf20.logisticslasers.common.items.logiccards.CardInserterTag;
import com.direwolf20.logisticslasers.common.items.logiccards.CardPolymorph;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketButtonClear {
    private int slotNumber;
    private BlockPos sourcePos;

    public PacketButtonClear(int slotNumber, BlockPos pos) {
        this.slotNumber = slotNumber;
        this.sourcePos = pos;
    }

    public static void encode(PacketButtonClear msg, PacketBuffer buffer) {
        buffer.writeInt(msg.slotNumber);
        buffer.writeBlockPos(msg.sourcePos);
    }

    public static PacketButtonClear decode(PacketBuffer buffer) {
        return new PacketButtonClear(buffer.readInt(), buffer.readBlockPos());

    }

    public static class Handler {
        public static void handle(PacketButtonClear msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayerEntity sender = ctx.get().getSender();
                if (sender == null)
                    return;

                Container container = sender.openContainer;
                if (container == null)
                    return;

                Slot slot = container.inventorySlots.get(msg.slotNumber);
                ItemStack itemStack = slot.getStack();

                if (itemStack.getItem() instanceof CardPolymorph) {
                    CardPolymorph.clearList(itemStack);
                } else if (itemStack.getItem() instanceof CardInserterTag) {
                    CardInserterTag.clearTags(itemStack);
                }

            });

            ctx.get().setPacketHandled(true);
        }
    }
}
