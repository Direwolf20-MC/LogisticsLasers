package com.direwolf20.logisticslasers.common.network.packets;

import com.direwolf20.logisticslasers.common.container.InventoryNodeContainer;
import com.direwolf20.logisticslasers.common.items.logiccards.BaseCard;
import com.direwolf20.logisticslasers.common.items.logiccards.CardInserterTag;
import com.direwolf20.logisticslasers.common.items.logiccards.CardPolymorph;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.items.ItemStackHandler;

import java.util.function.Supplier;

public class PacketButtonAdd {
    private int slotNumber;
    private BlockPos sourcePos;
    private String tag;

    public PacketButtonAdd(int slotNumber, BlockPos pos) {
        this.slotNumber = slotNumber;
        this.sourcePos = pos;
        this.tag = "";
    }

    public PacketButtonAdd(int slotNumber, BlockPos pos, String tag) {
        this.slotNumber = slotNumber;
        this.sourcePos = pos;
        this.tag = tag;
    }

    public static void encode(PacketButtonAdd msg, PacketBuffer buffer) {
        buffer.writeInt(msg.slotNumber);
        buffer.writeBlockPos(msg.sourcePos);
        buffer.writeString(msg.tag);
    }

    public static PacketButtonAdd decode(PacketBuffer buffer) {
        return new PacketButtonAdd(buffer.readInt(), buffer.readBlockPos(), buffer.readString());

    }

    public static class Handler {
        public static void handle(PacketButtonAdd msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayerEntity sender = ctx.get().getSender();
                if (sender == null)
                    return;

                Container container = sender.openContainer;

                ItemStack itemStack;
                if (msg.slotNumber == -1) {
                    itemStack = sender.getHeldItemMainhand();
                    if (!(itemStack.getItem() instanceof BaseCard))
                        itemStack = sender.getHeldItemOffhand();
                } else {
                    Slot slot = container.inventorySlots.get(msg.slotNumber);
                    itemStack = slot.getStack();
                }

                if (itemStack.getItem() instanceof CardPolymorph) {
                    if (container instanceof InventoryNodeContainer) {
                        CardPolymorph.addContainerToList(itemStack, ((InventoryNodeContainer) container).tile.getHandler().orElse(new ItemStackHandler(0)));
                    }
                } else if (itemStack.getItem() instanceof CardInserterTag) {
                    CardInserterTag.addTag(itemStack, msg.tag);
                }
            });

            ctx.get().setPacketHandled(true);
        }
    }
}
