package com.direwolf20.logisticslasers.common.blocks;

import com.direwolf20.logisticslasers.common.tiles.ControllerTile;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;

public class Controller extends Block {

    public Controller() {
        super(
                AbstractBlock.Properties.create(Material.IRON).hardnessAndResistance(2.0f).tickRandomly()
        );
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new ControllerTile();
    }

    @Override
    @SuppressWarnings("deprecation")
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult blockRayTraceResult) {
        // Only execute on the server
        if (worldIn.isRemote)
            return ActionResultType.SUCCESS;

        TileEntity te = worldIn.getTileEntity(pos);
        if (!(te instanceof ControllerTile))
            return ActionResultType.FAIL;

        NetworkHooks.openGui((ServerPlayerEntity) player, (INamedContainerProvider) te, pos);
        return ActionResultType.SUCCESS;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!worldIn.isRemote) {
            if (newState.getBlock() != this) {
                TileEntity tileEntity = worldIn.getTileEntity(pos);
                if (tileEntity != null) {
                    if (tileEntity instanceof ControllerTile) {
                        //((ControllerTile) tileEntity).deactivate((ServerWorld) worldIn);
                    }
                }
                super.onReplaced(state, worldIn, pos, newState, isMoving);
            }
        }
    }

}
