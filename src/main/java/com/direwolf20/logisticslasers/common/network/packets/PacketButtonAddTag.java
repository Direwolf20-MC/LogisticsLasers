package com.direwolf20.logisticslasers.common.network.packets;

/*public class PacketButtonAddTag {
    private BlockPos sourcePos;
    private String tag;

    public PacketButtonAddTag(BlockPos pos, String tag) {
        this.sourcePos = pos;
        this.tag = tag;
    }

    public static void encode(PacketButtonAddTag msg, PacketBuffer buffer) {
        buffer.writeBlockPos(msg.sourcePos);
        buffer.writeString(msg.tag);
    }

    public static PacketButtonAddTag decode(PacketBuffer buffer) {
        return new PacketButtonAddTag(buffer.readBlockPos(), buffer.readString());

    }

    public static class Handler {
        public static void handle(PacketButtonAddTag msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayerEntity sender = ctx.get().getSender();
                if (sender == null)
                    return;

                Container container = sender.openContainer;
                ItemStack itemStack;

                if (container instanceof TagFilterContainer) {
                    itemStack = ((TagFilterContainer) container).filterItemStack;
                    if (itemStack.getItem() instanceof CardInserterTag) {
                        CardInserterTag.addTag(itemStack, msg.tag);
                    }
                }
            });

            ctx.get().setPacketHandled(true);
        }
    }
}*/
