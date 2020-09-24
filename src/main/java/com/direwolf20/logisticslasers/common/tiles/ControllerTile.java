package com.direwolf20.logisticslasers.common.tiles;

import com.direwolf20.logisticslasers.common.blocks.ModBlocks;
import com.direwolf20.logisticslasers.common.container.ControllerContainer;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import javax.annotation.Nullable;

public class ControllerTile extends FETileBase implements ITickableTileEntity, INamedContainerProvider {

    public ControllerTile() {
        super(ModBlocks.CONTROLLER_TILE.get(), 1000000);
    }

    @Override
    public void tick() {
        //Client only
        if (world.isRemote) {
            //System.out.println("I'm here!");
        }

        //Server Only
        if (!world.isRemote) {
            energyStorage.receiveEnergy(1000, false); //Testing

        }
    }

    @Override
    public void read(BlockState state, CompoundNBT tag) {
        super.read(state, tag);
        //isActive = tag.getBoolean("active");
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        //tag.putBoolean("active", isActive);
        return super.write(tag);
    }

    @Override
    public ITextComponent getDisplayName() {
        return new StringTextComponent("Laser Controller");
    }

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        assert world != null;
        return new ControllerContainer(this, this.FETileData, i, playerInventory);
    }
}
