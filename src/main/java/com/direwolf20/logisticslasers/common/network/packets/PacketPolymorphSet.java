package com.direwolf20.logisticslasers.common.network.packets;

import com.direwolf20.logisticslasers.common.container.InventoryNodeContainer;
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

public class PacketPolymorphSet {
    private int slotNumber;
    private BlockPos sourcePos;

    public PacketPolymorphSet(int slotNumber, BlockPos pos) {
        this.slotNumber = slotNumber;
        this.sourcePos = pos;
    }

    public static void encode(PacketPolymorphSet msg, PacketBuffer buffer) {
        buffer.writeInt(msg.slotNumber);
        buffer.writeBlockPos(msg.sourcePos);
    }

    public static PacketPolymorphSet decode(PacketBuffer buffer) {
        return new PacketPolymorphSet(buffer.readInt(), buffer.readBlockPos());

    }

    public static class Handler {
        public static void handle(PacketPolymorphSet msg, Supplier<NetworkEvent.Context> ctx) {
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
                    if (container instanceof InventoryNodeContainer) {
                        CardPolymorph.setListFromContainer(itemStack, ((InventoryNodeContainer) container).tile.getHandler().orElse(new ItemStackHandler(0)));
                        ((InventoryNodeContainer) container).tile.getControllerTE().checkInvNode(((InventoryNodeContainer) container).tile.getPos());
                    }
                }
            });

            ctx.get().setPacketHandled(true);
        }
    }
}
