package com.direwolf20.logisticslasers.common.blocks;

import com.direwolf20.logisticslasers.common.blocks.baseblocks.BaseNode;
import com.direwolf20.logisticslasers.common.tiles.BasicNodeTile;
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
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.stream.Stream;

public class BasicNode extends BaseNode {
    protected static final VoxelShape SHAPE = Stream.of(
            Block.makeCuboidShape(5, 4, 4, 11, 5, 5),
            Block.makeCuboidShape(5, 5, 5, 11, 11, 11),
            Block.makeCuboidShape(4, 11, 5, 5, 12, 11),
            Block.makeCuboidShape(5, 11, 11, 11, 12, 12),
            Block.makeCuboidShape(5, 11, 4, 11, 12, 5),
            Block.makeCuboidShape(4, 12, 3, 12, 13, 4),
            Block.makeCuboidShape(4, 12, 12, 12, 13, 13),
            Block.makeCuboidShape(5, 4, 11, 11, 5, 12),
            Block.makeCuboidShape(4, 3, 3, 12, 4, 4),
            Block.makeCuboidShape(4, 3, 12, 12, 4, 13),
            Block.makeCuboidShape(12, 3, 4, 13, 4, 12),
            Block.makeCuboidShape(3, 3, 4, 4, 4, 12),
            Block.makeCuboidShape(3, 3, 12, 4, 13, 13),
            Block.makeCuboidShape(3, 3, 3, 4, 13, 4),
            Block.makeCuboidShape(12, 3, 12, 13, 13, 13),
            Block.makeCuboidShape(12, 3, 3, 13, 13, 4),
            Block.makeCuboidShape(12, 12, 4, 13, 13, 12),
            Block.makeCuboidShape(3, 12, 4, 4, 13, 12),
            Block.makeCuboidShape(11, 11, 5, 12, 12, 11),
            Block.makeCuboidShape(4, 4, 5, 5, 5, 11),
            Block.makeCuboidShape(11, 4, 5, 12, 5, 11),
            Block.makeCuboidShape(4, 4, 4, 5, 12, 5),
            Block.makeCuboidShape(11, 4, 4, 12, 12, 5),
            Block.makeCuboidShape(4, 4, 11, 5, 12, 12),
            Block.makeCuboidShape(11, 4, 11, 12, 12, 12)
    ).reduce((v1, v2) -> {return VoxelShapes.combineAndSimplify(v1, v2, IBooleanFunction.OR);}).get();

    public BasicNode() {
        super(AbstractBlock.Properties.create(Material.IRON).hardnessAndResistance(2.0f).notSolid());
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return SHAPE;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new BasicNodeTile();
    }

    @Override
    @SuppressWarnings("deprecation")
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult blockRayTraceResult) {
        // Only execute on the server
        if (worldIn.isRemote)
            return ActionResultType.SUCCESS;

        TileEntity te = worldIn.getTileEntity(pos);
        if (!(te instanceof BasicNodeTile))
            return ActionResultType.FAIL;

        //DoStuff

        return super.onBlockActivated(state, worldIn, pos, player, hand, blockRayTraceResult);
    }

    @OnlyIn(Dist.CLIENT)
    public float getAmbientOcclusionLightValue(BlockState state, IBlockReader worldIn, BlockPos pos) {
        return 1.0F;
    }

    public boolean propagatesSkylightDown(BlockState state, IBlockReader reader, BlockPos pos) {
        return true;
    }

}
