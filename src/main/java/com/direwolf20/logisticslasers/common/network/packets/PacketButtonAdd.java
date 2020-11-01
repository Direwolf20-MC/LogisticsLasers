package com.direwolf20.logisticslasers.common.network.packets;

import com.direwolf20.logisticslasers.common.container.cards.PolyFilterContainer;
import com.direwolf20.logisticslasers.common.container.cards.TagFilterContainer;
import com.direwolf20.logisticslasers.common.items.logiccards.CardInserterTag;
import com.direwolf20.logisticslasers.common.items.logiccards.CardPolymorph;
import com.direwolf20.logisticslasers.common.tiles.InventoryNodeTile;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.items.ItemStackHandler;

import java.util.function.Supplier;

public class PacketButtonAdd {
    private BlockPos sourcePos;
    private String tag;

    public PacketButtonAdd(BlockPos pos, String tag) {
        this.sourcePos = pos;
        this.tag = tag;
    }

    public static void encode(PacketButtonAdd msg, PacketBuffer buffer) {
        buffer.writeBlockPos(msg.sourcePos);
        buffer.writeString(msg.tag);
    }

    public static PacketButtonAdd decode(PacketBuffer buffer) {
        return new PacketButtonAdd(buffer.readBlockPos(), buffer.readString(255));
    }

    public static class Handler {
        public static void handle(PacketButtonAdd msg, Supplier<NetworkEvent.Context> ctx) {
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
                } else if (container instanceof PolyFilterContainer) {
                    itemStack = ((PolyFilterContainer) container).filterItemStack;
                    World world = sender.getServerWorld();
                    TileEntity te = world.getTileEntity(msg.sourcePos);
                    if (te instanceof InventoryNodeTile) {
                        CardPolymorph.addContainerToList(itemStack, ((InventoryNodeTile) te).getHandler().orElse(new ItemStackHandler(0)));
                        ((InventoryNodeTile) te).markDirtyClient();
                    }
                } else {
                    return;
                }
            });

            ctx.get().setPacketHandled(true);
        }
    }
}
