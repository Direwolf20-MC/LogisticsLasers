package com.direwolf20.logisticslasers.common.network.packets;

import com.direwolf20.logisticslasers.common.tiles.ControllerTile;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
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
    private final Object2IntOpenHashMap<ItemStack> itemCounts;
    private final BlockPos controllerPos;

    public PacketItemCountsSync(Object2IntOpenHashMap<ItemStack> itemCounts, BlockPos pos) {
        this.itemCounts = itemCounts;
        this.controllerPos = pos;
    }

    public static void encode(PacketItemCountsSync msg, PacketBuffer buffer) {
        Object2IntOpenHashMap<ItemStack> thisList = msg.itemCounts;
        CompoundNBT tag = new CompoundNBT();
        ListNBT nbtList = new ListNBT();
        int i = 0;
        for (Object2IntMap.Entry<ItemStack> entry : thisList.object2IntEntrySet()) {
            CompoundNBT nbt = new CompoundNBT();
            nbt.put("itemStack", entry.getKey().serializeNBT());
            nbt.putInt("count", entry.getIntValue());
            nbtList.add(i, nbt);
            i++;
        }
        tag.put("list", nbtList);
        buffer.writeCompoundTag(tag);
        buffer.writeBlockPos(msg.controllerPos);
    }

    public static PacketItemCountsSync decode(PacketBuffer buffer) {
        CompoundNBT tag = buffer.readCompoundTag();
        ListNBT nbtList = tag.getList("list", Constants.NBT.TAG_COMPOUND);
        Object2IntOpenHashMap<ItemStack> thisList = new Object2IntOpenHashMap<>();
        for (int i = 0; i < nbtList.size(); i++) {
            CompoundNBT nbt = nbtList.getCompound(i);
            thisList.put(ItemStack.read(nbt.getCompound("itemStack")), nbt.getInt("count"));
        }
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
