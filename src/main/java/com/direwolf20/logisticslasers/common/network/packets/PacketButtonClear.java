package com.direwolf20.logisticslasers.common.network.packets;

import com.direwolf20.logisticslasers.common.container.cards.PolyFilterContainer;
import com.direwolf20.logisticslasers.common.container.cards.TagFilterContainer;
import com.direwolf20.logisticslasers.common.items.logiccards.CardInserterTag;
import com.direwolf20.logisticslasers.common.items.logiccards.CardPolymorph;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketButtonClear {
    private BlockPos sourcePos;

    public PacketButtonClear(BlockPos pos) {
        this.sourcePos = pos;
    }

    public static void encode(PacketButtonClear msg, PacketBuffer buffer) {
        buffer.writeBlockPos(msg.sourcePos);
    }

    public static PacketButtonClear decode(PacketBuffer buffer) {
        return new PacketButtonClear(buffer.readBlockPos());

    }

    public static class Handler {
        public static void handle(PacketButtonClear msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayerEntity sender = ctx.get().getSender();
                if (sender == null)
                    return;

                Container container = sender.openContainer;

                ItemStack itemStack;

                if (container instanceof TagFilterContainer) {
                    itemStack = ((TagFilterContainer) container).filterItemStack;
                    if (itemStack.getItem() instanceof CardInserterTag) {
                        CardInserterTag.clearTags(itemStack);
                    }
                } else if (container instanceof PolyFilterContainer) {
                    itemStack = ((PolyFilterContainer) container).filterItemStack;
                    if (itemStack.getItem() instanceof CardPolymorph) {
                        CardPolymorph.clearList(itemStack);
                    }
                } else {
                    return;
                }
            });

            ctx.get().setPacketHandled(true);
        }
    }
}
