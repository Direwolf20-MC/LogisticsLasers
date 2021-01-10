package com.direwolf20.logisticslasers.common.blocks;

import com.direwolf20.logisticslasers.common.blocks.baseblocks.BaseNode;
import com.direwolf20.logisticslasers.common.tiles.InventoryNodeTile;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
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
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.stream.Stream;

public class InventoryNode extends BaseNode {
    protected static final VoxelShape[] shapes = new VoxelShape[]{
            Stream.of(
                    Block.makeCuboidShape(6.5, 4.25, 6.5, 9.5, 4.75, 9.5),
                    Block.makeCuboidShape(5, 0, 5, 11, 1, 11),
                    Block.makeCuboidShape(5, -2, 5, 11, -1.5, 11),
                    Block.makeCuboidShape(6.75, 4, 6.75, 9.25, 5, 9.25),
                    Block.makeCuboidShape(7.75, 5, 7.75, 8.25, 7.5, 8.25),
                    Block.makeCuboidShape(7.5, 7.5, 7.5, 8.5, 8.5, 8.5),
                    Block.makeCuboidShape(6.5, 1, 6.5, 9.5, 1.25, 9.5),
                    Block.makeCuboidShape(7.5, 5.25, 7.5, 8.5, 5.5, 8.5),
                    Block.makeCuboidShape(7.25, 5, 7.25, 8.75, 5.25, 8.75),
                    Block.makeCuboidShape(6.75, 1, 9.5, 9.25, 1.25, 10.5),
                    Block.makeCuboidShape(9.5, 1, 6.75, 10.5, 1.25, 9.25),
                    Block.makeCuboidShape(6.75, 1, 5.5, 9.25, 1.25, 6.5),
                    Block.makeCuboidShape(5.5, 1, 6.75, 6.5, 1.25, 9.25),
                    Block.makeCuboidShape(5.25, -1.5, 5.25, 10.75, 0, 10.75),
                    Block.makeCuboidShape(7, 1, 7, 9, 4, 9)
            ).reduce((v1, v2) -> {return VoxelShapes.combineAndSimplify(v1, v2, IBooleanFunction.OR);}).get(), //DOWN
            Stream.of(
                    Block.makeCuboidShape(6.5, 11.25, 6.5, 9.5, 11.75, 9.5),
                    Block.makeCuboidShape(5, 15, 5, 11, 16, 11),
                    Block.makeCuboidShape(5, 17.5, 5, 11, 18, 11),
                    Block.makeCuboidShape(6.75, 11, 6.75, 9.25, 12, 9.25),
                    Block.makeCuboidShape(7.75, 8.5, 7.75, 8.25, 11, 8.25),
                    Block.makeCuboidShape(7.5, 7.5, 7.5, 8.5, 8.5, 8.5),
                    Block.makeCuboidShape(6.5, 14.75, 6.5, 9.5, 15, 9.5),
                    Block.makeCuboidShape(7.5, 10.5, 7.5, 8.5, 10.75, 8.5),
                    Block.makeCuboidShape(7.25, 10.75, 7.25, 8.75, 11, 8.75),
                    Block.makeCuboidShape(6.75, 14.75, 9.5, 9.25, 15, 10.5),
                    Block.makeCuboidShape(9.5, 14.75, 6.75, 10.5, 15, 9.25),
                    Block.makeCuboidShape(6.75, 14.75, 5.5, 9.25, 15, 6.5),
                    Block.makeCuboidShape(5.5, 14.75, 6.75, 6.5, 15, 9.25),
                    Block.makeCuboidShape(5.25, 16, 5.25, 10.75, 17.5, 10.75),
                    Block.makeCuboidShape(7, 12, 7, 9, 15, 9)
            ).reduce((v1, v2) -> {return VoxelShapes.combineAndSimplify(v1, v2, IBooleanFunction.OR);}).get(), //UP
            Stream.of(
                    Block.makeCuboidShape(6.5, 6.5, 4.25, 9.5, 9.5, 4.75),
                    Block.makeCuboidShape(5, 5, 0, 11, 11, 1),
                    Block.makeCuboidShape(5, 5, -2, 11, 11, -1.5),
                    Block.makeCuboidShape(6.75, 6.75, 4, 9.25, 9.25, 5),
                    Block.makeCuboidShape(7.75, 7.75, 5, 8.25, 8.25, 7.5),
                    Block.makeCuboidShape(7.5, 7.5, 7.5, 8.5, 8.5, 8.5),
                    Block.makeCuboidShape(6.5, 6.5, 1, 9.5, 9.5, 1.25),
                    Block.makeCuboidShape(7.5, 7.5, 5.25, 8.5, 8.5, 5.5),
                    Block.makeCuboidShape(7.25, 7.25, 5, 8.75, 8.75, 5.25),
                    Block.makeCuboidShape(6.75, 5.5, 1, 9.25, 6.5, 1.25),
                    Block.makeCuboidShape(9.5, 6.75, 1, 10.5, 9.25, 1.25),
                    Block.makeCuboidShape(6.75, 9.5, 1, 9.25, 10.5, 1.25),
                    Block.makeCuboidShape(5.5, 6.75, 1, 6.5, 9.25, 1.25),
                    Block.makeCuboidShape(5.25, 5.25, -1.5, 10.75, 10.75, 0),
                    Block.makeCuboidShape(7, 7, 1, 9, 9, 4)
            ).reduce((v1, v2) -> {return VoxelShapes.combineAndSimplify(v1, v2, IBooleanFunction.OR);}).get(), //NORTH
            Stream.of(
                    Block.makeCuboidShape(6.5, 6.5, 11.25, 9.5, 9.5, 11.75),
                    Block.makeCuboidShape(5, 5, 15, 11, 11, 16),
                    Block.makeCuboidShape(5, 5, 17.5, 11, 11, 18),
                    Block.makeCuboidShape(6.75, 6.75, 11, 9.25, 9.25, 12),
                    Block.makeCuboidShape(7.75, 7.75, 8.5, 8.25, 8.25, 11),
                    Block.makeCuboidShape(7.5, 7.5, 7.5, 8.5, 8.5, 8.5),
                    Block.makeCuboidShape(6.5, 6.5, 14.75, 9.5, 9.5, 15),
                    Block.makeCuboidShape(7.5, 7.5, 10.5, 8.5, 8.5, 10.75),
                    Block.makeCuboidShape(7.25, 7.25, 10.75, 8.75, 8.75, 11),
                    Block.makeCuboidShape(6.75, 5.5, 14.75, 9.25, 6.5, 15),
                    Block.makeCuboidShape(9.5, 6.75, 14.75, 10.5, 9.25, 15),
                    Block.makeCuboidShape(6.75, 9.5, 14.75, 9.25, 10.5, 15),
                    Block.makeCuboidShape(5.5, 6.75, 14.75, 6.5, 9.25, 15),
                    Block.makeCuboidShape(5.25, 5.25, 16, 10.75, 10.75, 17.5),
                    Block.makeCuboidShape(7, 7, 12, 9, 9, 15)
            ).reduce((v1, v2) -> {return VoxelShapes.combineAndSimplify(v1, v2, IBooleanFunction.OR);}).get(), //SOUTH
            Stream.of(
                    Block.makeCuboidShape(4.25, 6.5, 6.5, 4.75, 9.5, 9.5),
                    Block.makeCuboidShape(0, 5, 5, 1, 11, 11),
                    Block.makeCuboidShape(-2, 5, 5, -1.5, 11, 11),
                    Block.makeCuboidShape(4, 6.75, 6.75, 5, 9.25, 9.25),
                    Block.makeCuboidShape(5, 7.75, 7.75, 7.5, 8.25, 8.25),
                    Block.makeCuboidShape(7.5, 7.5, 7.5, 8.5, 8.5, 8.5),
                    Block.makeCuboidShape(1, 6.5, 6.5, 1.25, 9.5, 9.5),
                    Block.makeCuboidShape(5.25, 7.5, 7.5, 5.5, 8.5, 8.5),
                    Block.makeCuboidShape(5, 7.25, 7.25, 5.25, 8.75, 8.75),
                    Block.makeCuboidShape(1, 5.5, 6.75, 1.25, 6.5, 9.25),
                    Block.makeCuboidShape(1, 6.75, 9.5, 1.25, 9.25, 10.5),
                    Block.makeCuboidShape(1, 9.5, 6.75, 1.25, 10.5, 9.25),
                    Block.makeCuboidShape(1, 6.75, 5.5, 1.25, 9.25, 6.5),
                    Block.makeCuboidShape(-1.5, 5.25, 5.25, 0, 10.75, 10.75),
                    Block.makeCuboidShape(1, 7, 7, 4, 9, 9)
            ).reduce((v1, v2) -> {return VoxelShapes.combineAndSimplify(v1, v2, IBooleanFunction.OR);}).get(), //WEST
            Stream.of(
                    Block.makeCuboidShape(11.25, 6.5, 6.5, 11.75, 9.5, 9.5),
                    Block.makeCuboidShape(15, 5, 5, 16, 11, 11),
                    Block.makeCuboidShape(17.5, 5, 5, 18, 11, 11),
                    Block.makeCuboidShape(11, 6.75, 6.75, 12, 9.25, 9.25),
                    Block.makeCuboidShape(8.5, 7.75, 7.75, 11, 8.25, 8.25),
                    Block.makeCuboidShape(7.5, 7.5, 7.5, 8.5, 8.5, 8.5),
                    Block.makeCuboidShape(14.75, 6.5, 6.5, 15, 9.5, 9.5),
                    Block.makeCuboidShape(10.5, 7.5, 7.5, 10.75, 8.5, 8.5),
                    Block.makeCuboidShape(10.75, 7.25, 7.25, 11, 8.75, 8.75),
                    Block.makeCuboidShape(14.75, 5.5, 6.75, 15, 6.5, 9.25),
                    Block.makeCuboidShape(14.75, 6.75, 9.5, 15, 9.25, 10.5),
                    Block.makeCuboidShape(14.75, 9.5, 6.75, 15, 10.5, 9.25),
                    Block.makeCuboidShape(14.75, 6.75, 5.5, 15, 9.25, 6.5),
                    Block.makeCuboidShape(16, 5.25, 5.25, 17.5, 10.75, 10.75),
                    Block.makeCuboidShape(12, 7, 7, 15, 9, 9)
            ).reduce((v1, v2) -> {return VoxelShapes.combineAndSimplify(v1, v2, IBooleanFunction.OR);}).get()//EAST
    };
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    public InventoryNode() {
        super(AbstractBlock.Properties.create(Material.IRON).hardnessAndResistance(2.0f).notSolid());
        setDefaultState(getStateContainer().getBaseState().with(FACING, Direction.NORTH));
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return shapes[state.get(FACING).getIndex()];
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return getDefaultState().with(FACING, context.getFace().getOpposite());
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new InventoryNodeTile();
    }

    @Override
    @SuppressWarnings("deprecation")
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult blockRayTraceResult) {
        // Only execute on the server
        if (worldIn.isRemote)
            return ActionResultType.SUCCESS;

        TileEntity te = worldIn.getTileEntity(pos);
        if (!(te instanceof InventoryNodeTile))
            return ActionResultType.FAIL;

        //DoStuff
        NetworkHooks.openGui((ServerPlayerEntity) player, (INamedContainerProvider) te, pos);

        return super.onBlockActivated(state, worldIn, pos, player, hand, blockRayTraceResult);
    }

    @OnlyIn(Dist.CLIENT)
    public float getAmbientOcclusionLightValue(BlockState state, IBlockReader worldIn, BlockPos pos) {
        return 1.0F;
    }

    public boolean propagatesSkylightDown(BlockState state, IBlockReader reader, BlockPos pos) {
        return true;
    }

    @Override
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.isIn(newState.getBlock())) {
            TileEntity tileentity = worldIn.getTileEntity(pos);
            if (tileentity instanceof InventoryNodeTile) {
                dropInventoryItems(worldIn, pos, ((InventoryNodeTile) tileentity).getInventoryStacks());
                worldIn.updateComparatorOutputLevel(pos, this);
            }

            super.onReplaced(state, worldIn, pos, newState, isMoving);
        }
    }

    private static void dropInventoryItems(World worldIn, BlockPos pos, IItemHandler inventory) {
        for (int i = 0; i < inventory.getSlots(); ++i) {
            ItemStack itemstack = inventory.getStackInSlot(i);

            if (itemstack.getCount() > 0) {
                InventoryHelper.spawnItemStack(worldIn, (double) pos.getX(), (double) pos.getY(), (double) pos.getZ(), itemstack);
            }
        }
    }
}
