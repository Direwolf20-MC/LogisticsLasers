package com.direwolf20.logisticslasers.common.blocks.baseblocks;

import com.direwolf20.logisticslasers.common.tiles.basetiles.NodeTileBase;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BaseNode extends Block {
    public BaseNode() {
        super(AbstractBlock.Properties.create(Material.IRON).hardnessAndResistance(2.0f));
    }

    public BaseNode(Properties prop) {
        super(prop);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!worldIn.isRemote) {
            if (newState.getBlock() != this) {
                TileEntity tileEntity = worldIn.getTileEntity(pos);
                if (tileEntity != null) {
                    if (tileEntity instanceof NodeTileBase) {
                        //((ControllerTile) tileEntity).deactivate((ServerWorld) worldIn);
                    }
                }
                super.onReplaced(state, worldIn, pos, newState, isMoving);
            }
        }
    }
}
