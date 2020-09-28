package com.direwolf20.logisticslasers.common.items;

import com.direwolf20.logisticslasers.LogisticsLasers;
import com.direwolf20.logisticslasers.client.particles.itemparticle.ItemFlowParticleData;
import com.direwolf20.logisticslasers.common.tiles.basetiles.NodeTileBase;
import com.direwolf20.logisticslasers.common.util.VectorHelper;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class Wrench extends Item {
    public Wrench() {
        super(new Item.Properties().maxStackSize(1).group(LogisticsLasers.itemGroup));
    }

    public static BlockPos storeConnectionPos(ItemStack wrench, BlockPos pos) {
        wrench.getOrCreateTag().put("connectionpos", NBTUtil.writeBlockPos(pos));
        return pos;
    }

    public static BlockPos getConnectionPos(ItemStack wrench) {
        CompoundNBT compound = wrench.getOrCreateTag();
        return !compound.contains("connectionpos") ? storeConnectionPos(wrench, BlockPos.ZERO) : NBTUtil.readBlockPos(compound.getCompound("connectionpos"));
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        ItemStack wrench = player.getHeldItem(hand);

        if (world.isRemote) //No client
            return new ActionResult<>(ActionResultType.PASS, wrench);

        ItemFlowParticleData data = new ItemFlowParticleData(new ItemStack(Items.BEDROCK, 1), -169.5, 76.5, -648.5, 4);
        ServerWorld serverWorld = (ServerWorld) world;
        serverWorld.spawnParticle(data, -169.5, 76.5, -645.5, 5, 0.15f, 0.15f, 0.15f, 0);

        int range = 20;
        BlockRayTraceResult lookingAt = VectorHelper.getLookingAt((PlayerEntity) player, RayTraceContext.FluidMode.NONE, range);
        if (lookingAt == null || (world.getBlockState(VectorHelper.getLookingAt((PlayerEntity) player, wrench, range).getPos()) == Blocks.AIR.getDefaultState())) {
            if (player.isSneaking()) {
                storeConnectionPos(wrench, BlockPos.ZERO);
                return new ActionResult<>(ActionResultType.PASS, wrench);
            }
        }
        BlockPos pos = lookingAt.getPos();
        TileEntity te = world.getTileEntity(pos);
        if (!(te instanceof NodeTileBase))
            return new ActionResult<>(ActionResultType.PASS, wrench);

        if (player.isSneaking()) {
            if (getConnectionPos(wrench).equals(BlockPos.ZERO))
                storeConnectionPos(wrench, pos);
            else {
                if (pos.equals(getConnectionPos(wrench)))
                    return new ActionResult<>(ActionResultType.PASS, wrench);
                BlockPos sourcePos = getConnectionPos(wrench);
                TileEntity sourceTE = world.getTileEntity(sourcePos);
                if (!(sourceTE instanceof NodeTileBase)) {
                    storeConnectionPos(wrench, BlockPos.ZERO);
                    return new ActionResult<>(ActionResultType.PASS, wrench);
                }
                if (!((NodeTileBase) te).addConnection(sourcePos))
                    ((NodeTileBase) te).removeConnection(sourcePos);
            }
        }
        return new ActionResult<>(ActionResultType.SUCCESS, wrench);
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        return ActionResultType.PASS;
    }

}
