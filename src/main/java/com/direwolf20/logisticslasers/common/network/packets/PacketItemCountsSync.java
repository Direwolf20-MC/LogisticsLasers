package com.direwolf20.logisticslasers.common.network.packets;

import com.direwolf20.logisticslasers.common.tiles.ControllerTile;
import com.direwolf20.logisticslasers.common.util.ItemHandlerUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketItemCountsSync {
    private final ItemHandlerUtil.InventoryCounts itemCounts;
    private final BlockPos controllerPos;

    public PacketItemCountsSync(ItemHandlerUtil.InventoryCounts itemCounts, BlockPos pos) {
        this.itemCounts = itemCounts;
        this.controllerPos = pos;
    }

    public static void encode(PacketItemCountsSync msg, PacketBuffer buffer) {
        ItemHandlerUtil.InventoryCounts thisList = msg.itemCounts;
        CompoundNBT tag = new CompoundNBT();
        ListNBT nbtList = thisList.serialize();

        tag.put("list", nbtList);
        buffer.writeCompoundTag(tag);
        buffer.writeBlockPos(msg.controllerPos);
    }

    public static PacketItemCountsSync decode(PacketBuffer buffer) {
        CompoundNBT tag = buffer.readCompoundTag();
        ListNBT nbtList = tag.getList("list", Constants.NBT.TAG_COMPOUND);
        ItemHandlerUtil.InventoryCounts thisList = new ItemHandlerUtil.InventoryCounts(nbtList);
        BlockPos pos = buffer.readBlockPos();
        return new PacketItemCountsSync(thisList, pos);
    }

    public static class Handler {
        public static void handle(PacketItemCountsSync msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> clientPacketHandler(msg)));
            ctx.get().setPacketHandled(true);
        }
    }

    public static void clientPacketHandler(PacketItemCountsSync msg) {
        TileEntity te = Minecraft.getInstance().world.getTileEntity(msg.controllerPos);
        if (te instanceof ControllerTile) {
            ((ControllerTile) te).setItemCounts(msg.itemCounts);
        }
    }
}
