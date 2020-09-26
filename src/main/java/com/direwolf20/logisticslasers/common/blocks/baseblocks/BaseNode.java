package com.direwolf20.logisticslasers.common.blocks.baseblocks;

import com.direwolf20.logisticslasers.common.tiles.basetiles.NodeTileBase;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.TranslationTextComponent;
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
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult blockRayTraceResult) {
        if (worldIn.isRemote)
            return ActionResultType.FAIL;
        TileEntity te = worldIn.getTileEntity(pos);
        if (!(te instanceof NodeTileBase))
            return ActionResultType.PASS;

        player.sendStatusMessage(new TranslationTextComponent("message.logisticslasers.controllerat", ((NodeTileBase) te).getControllerPos()), true);
        System.out.println("Connected Nodes: " + ((NodeTileBase) te).getConnectedNodes());
        System.out.println("Controller: " + ((NodeTileBase) te).getControllerPos());
        return ActionResultType.SUCCESS;
    }

    /*@Override
    @SuppressWarnings("deprecation")
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!worldIn.isRemote) {
            if (newState.getBlock() != this) {
                TileEntity tileEntity = worldIn.getTileEntity(pos);
                if (tileEntity != null) {
                    if (tileEntity instanceof NodeTileBase) {
                        ((NodeTileBase) tileEntity).disconnectAllNodes();
                    }
                }
                super.onReplaced(state, worldIn, pos, newState, isMoving);
            }
        }
    }*/
}
