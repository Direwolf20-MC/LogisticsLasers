package com.direwolf20.logisticslasers.common.network.packets;

/*public class PacketCardApply {
    private int slotNumber;
    private BlockPos sourcePos;

    public PacketCardApply(int slotNumber, BlockPos pos) {
        this.slotNumber = slotNumber;
        this.sourcePos = pos;
    }

    public static void encode(PacketCardApply msg, PacketBuffer buffer) {
        buffer.writeInt(msg.slotNumber);
        buffer.writeBlockPos(msg.sourcePos);
    }

    public static PacketCardApply decode(PacketBuffer buffer) {
        return new PacketCardApply(buffer.readInt(), buffer.readBlockPos());

    }

    public static class Handler {
        public static void handle(PacketCardApply msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayerEntity sender = ctx.get().getSender();
                if (sender == null)
                    return;

                Container container = sender.openContainer;
                if (container == null)
                    return;

                Slot slot = container.inventorySlots.get(msg.slotNumber);
                ItemStack itemStack = slot.getStack();

                if (itemStack.getItem() instanceof CardPolymorph || itemStack.getItem() instanceof CardInserterTag) {
                    if (container instanceof InventoryNodeContainer) {
                        ((InventoryNodeContainer) container).tile.getControllerTE().checkInvNode(((InventoryNodeContainer) container).tile.getPos());
                    }
                }
            });

            ctx.get().setPacketHandled(true);
        }
    }
}*/
