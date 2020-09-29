package com.direwolf20.logisticslasers.common.tiles;

import com.direwolf20.logisticslasers.client.particles.itemparticle.ItemFlowParticleData;
import com.direwolf20.logisticslasers.common.blocks.ModBlocks;
import com.direwolf20.logisticslasers.common.capabilities.FEEnergyStorage;
import com.direwolf20.logisticslasers.common.container.ControllerContainer;
import com.direwolf20.logisticslasers.common.items.logiccards.CardExtractor;
import com.direwolf20.logisticslasers.common.items.logiccards.CardInserter;
import com.direwolf20.logisticslasers.common.tiles.basetiles.NodeTileBase;
import com.direwolf20.logisticslasers.common.util.ControllerTask;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.IIntArray;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ControllerTile extends NodeTileBase implements ITickableTileEntity, INamedContainerProvider {

    //Data about the energy stored in this tile entity
    public FEEnergyStorage energyStorage;
    private LazyOptional<FEEnergyStorage> energy;
    //Data about the nodes this controller manages
    private final Set<BlockPos> inventoryNodes = new HashSet<>();
    private final Set<BlockPos> extractorNodes = new HashSet<>();
    private final Set<BlockPos> inserterNodes = new HashSet<>();
    private final Set<BlockPos> allNodes = new HashSet<>();
    private final SetMultimap<Long, ControllerTask> taskList = HashMultimap.create();

    private final IItemHandler EMPTY = new ItemStackHandler(0);

    private int ticksPerBlock = 4;

    public ControllerTile() {
        super(ModBlocks.CONTROLLER_TILE.get());
        this.energyStorage = new FEEnergyStorage(this, 0, 1000000);
        this.energy = LazyOptional.of(() -> this.energyStorage);
    }

    public void checkInvNode(BlockPos pos) {
        InventoryNodeTile te = (InventoryNodeTile) world.getTileEntity(pos);
        extractorNodes.remove(pos);
        inserterNodes.remove(pos);
        if (!te.hasController()) return;

        ItemStackHandler handler = te.getInventoryStacks();
        for (int i = 0; i < handler.getSlots(); i++) {
            if (handler.getStackInSlot(i).getItem() instanceof CardExtractor)
                extractorNodes.add(pos);
            if (handler.getStackInSlot(i).getItem() instanceof CardInserter)
                inserterNodes.add(pos);
        }
    }

    public Set<BlockPos> getInventoryNodes() {
        return inventoryNodes;
    }

    public void updateInvNodePaths() {
        for (BlockPos pos : inventoryNodes) {
            InventoryNodeTile te = (InventoryNodeTile) world.getTileEntity(pos);
            if (te == null) continue;
            te.clearRouteList();
        }
    }

    public boolean addToInvNodes(BlockPos pos) {
        if (inventoryNodes.add(pos)) {
            addToAllNodes(pos);
            checkInvNode(pos);
            return true;
        }
        return false;
    }

    public boolean removeFromInvNodes(BlockPos pos) {
        boolean inv = inventoryNodes.remove(pos);
        if (inv) {
            extractorNodes.remove(pos);
            inserterNodes.remove(pos);
        }
        boolean all = removeFromAllNodes(pos);
        return (inv && all);
    }

    @Override
    public void addToController() {
        return; //NOOP
    }

    public boolean addToAllNodes(BlockPos pos) {
        boolean success = allNodes.add(pos);
        return success;
    }

    public boolean removeFromAllNodes(BlockPos pos) {
        boolean success = allNodes.remove(pos);
        return success;
    }

    @Override
    public BlockPos getControllerPos() {
        return this.getPos();
    }

    @Override
    public void setControllerPos(BlockPos controllerPos, BlockPos sourcePos) {
        System.out.println("Not Setting Controller Pos at: " + getControllerPos());
    }

    @Override
    public BlockPos validateController() {
        return this.getPos();
    }

    @Override
    public boolean addNode(BlockPos pos) {
        NodeTileBase te = (NodeTileBase) world.getTileEntity(pos);
        if (te.hasController() && !te.getControllerPos().equals(getControllerPos()))
            return false;
        return super.addNode(pos);
    }

    public void handleExtractors() {
        for (BlockPos fromPos : extractorNodes) {
            InventoryNodeTile sourceTE = (InventoryNodeTile) world.getTileEntity(fromPos);
            if (sourceTE == null) continue;
            IItemHandler sourceitemHandler = sourceTE.getHandler().orElse(EMPTY);
            if (sourceitemHandler.getSlots() > 0 && inserterNodes.size() > 0) {
                for (int i = 0; i < sourceitemHandler.getSlots(); i++) {
                    if (!sourceitemHandler.getStackInSlot(i).isEmpty()) {
                        BlockPos toPos = inserterNodes.iterator().next();
                        InventoryNodeTile destTE = (InventoryNodeTile) world.getTileEntity(toPos);

                        IItemHandler destitemHandler = destTE.getHandler().orElse(EMPTY);

                        if (destitemHandler.getSlots() > 0) {
                            ItemStack stack = sourceitemHandler.extractItem(i, 1, true);
                            ItemStack simulated = ItemHandlerHelper.insertItem(destitemHandler, stack, true);
                            if (simulated.getCount() < stack.getCount()) {
                                int count = stack.getCount() - simulated.getCount();
                                ItemStack extractedStack = sourceitemHandler.extractItem(i, count, false);
                                if (!transferItemStack(fromPos, toPos, extractedStack)) {
                                    ItemHandlerHelper.insertItem(sourceitemHandler, extractedStack, false);
                                }
                                return;
                            }
                        }
                    }
                }
            }
        }
    }


    public boolean transferItemStack(BlockPos fromPos, BlockPos toPos, ItemStack itemStack) {
        TileEntity te = world.getTileEntity(fromPos);
        if (!(te instanceof InventoryNodeTile)) return false;
        ArrayList<BlockPos> route = ((InventoryNodeTile) te).getRouteTo(toPos);
        if (route == null || route.size() <= 0) {
            return false;
        }

        long tempGameTime = world.getGameTime() + 1;
        ControllerTask task;
        for (int r = 0; r < route.size(); r++) {
            if (r == route.size() - 1) {
                task = new ControllerTask(route.get(r - 1), route.get(r), ControllerTask.TaskType.INSERT, itemStack);
                taskList.put(tempGameTime, task);
            } else {
                BlockPos from = route.get(r);
                BlockPos to = route.get(r + 1);
                task = new ControllerTask(from, to, ControllerTask.TaskType.PARTICLE, itemStack);
                taskList.put(tempGameTime, task);
                Vector3d fromVec = new Vector3d(from.getX(), from.getY(), from.getZ());
                Vector3d toVec = new Vector3d(to.getX(), to.getY(), to.getZ());
                double distance = fromVec.distanceTo(toVec);
                int duration = (int) distance * ticksPerBlock;
                tempGameTime += duration;
            }
        }
        return true;
    }

    public void handleTasks() {
        long gameTime = world.getGameTime();
        Set<ControllerTask> tasksThisTick = taskList.get(gameTime);

        for (ControllerTask task : tasksThisTick) {
            System.out.println("Executing task: " + task);
            executeTask(task);
        }
        taskList.removeAll(gameTime);
    }

    public void executeTask(ControllerTask task) {
        if (task.isParticle()) {
            doParticles(task);
        } else if (task.isInsert()) {
            doInsert(task);
        } else if (task.isExtract()) {

        }
    }

    public void doParticles(ControllerTask task) {
        System.out.println("Spawning Particle at: " + task.fromPos + " to: " + task.toPos);
        ItemFlowParticleData data = new ItemFlowParticleData(task.itemStack, task.toPos.getX() + 0.5, task.toPos.getY() + 0.5, task.toPos.getZ() + 0.5, ticksPerBlock);
        ServerWorld serverWorld = (ServerWorld) world;
        serverWorld.spawnParticle(data, task.fromPos.getX() + 0.5, task.fromPos.getY() + 0.5, task.fromPos.getZ() + 0.5, 5, 0.15f, 0.15f, 0.15f, 0);
    }

    public boolean doInsert(ControllerTask task) {
        InventoryNodeTile destTE = (InventoryNodeTile) world.getTileEntity(task.toPos);
        IItemHandler destitemHandler = destTE.getHandler().orElse(EMPTY);

        if (destitemHandler.getSlots() > 0) {
            ItemStack stack = task.itemStack;
            ItemStack simulated = ItemHandlerHelper.insertItem(destitemHandler, stack, true);
            if (simulated.isEmpty()) { //TODO: Allow partial inserts
                ItemHandlerHelper.insertItem(destitemHandler, stack, false);
                return true;
            }
        }
        return false;
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
            if (world.getGameTime() % 2 == 0)
                handleExtractors();
            else
                handleTasks();
        }
    }

    // Handles tracking changes, kinda messy but apparently this is how the cool kids do it these days
    public final IIntArray FETileData = new IIntArray() {
        @Override
        public int get(int index) {
            switch (index) {
                case 0:
                    return energyStorage.getEnergyStored() / 32;
                case 1:
                    return energyStorage.getMaxEnergyStored() / 32;
                default:
                    throw new IllegalArgumentException("Invalid index: " + index);
            }
        }

        @Override
        public void set(int index, int value) {
            throw new IllegalStateException("Cannot set values through IIntArray");
        }

        @Override
        public int size() {
            return 2;
        }
    };

    //Misc Methods for TE's
    @Override
    public void read(BlockState state, CompoundNBT tag) {
        super.read(state, tag);
        energy.ifPresent(h -> h.deserializeNBT(tag.getCompound("energy")));
        allNodes.clear();
        ListNBT allnodes = tag.getList("allnodes", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < allnodes.size(); i++) {
            BlockPos blockPos = NBTUtil.readBlockPos(allnodes.getCompound(i).getCompound("pos"));
            allNodes.add(blockPos);
        }

        inventoryNodes.clear();
        ListNBT invnodes = tag.getList("invnodes", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < invnodes.size(); i++) {
            BlockPos blockPos = NBTUtil.readBlockPos(invnodes.getCompound(i).getCompound("pos"));
            inventoryNodes.add(blockPos);
        }

        extractorNodes.clear();
        ListNBT extractorNodes = tag.getList("extractorNodes", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < extractorNodes.size(); i++) {
            BlockPos blockPos = NBTUtil.readBlockPos(extractorNodes.getCompound(i).getCompound("pos"));
            this.extractorNodes.add(blockPos);
        }

        inserterNodes.clear();
        ListNBT inserterNodes = tag.getList("inserterNodes", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < inserterNodes.size(); i++) {
            BlockPos blockPos = NBTUtil.readBlockPos(inserterNodes.getCompound(i).getCompound("pos"));
            this.inserterNodes.add(blockPos);
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        energy.ifPresent(h -> tag.put("energy", h.serializeNBT()));
        ListNBT allnodes = new ListNBT();
        for (BlockPos blockPos : allNodes) {
            CompoundNBT comp = new CompoundNBT();
            comp.put("pos", NBTUtil.writeBlockPos(blockPos));
            allnodes.add(comp);
        }
        tag.put("allnodes", allnodes);

        ListNBT invnodes = new ListNBT();
        for (BlockPos blockPos : inventoryNodes) {
            CompoundNBT comp = new CompoundNBT();
            comp.put("pos", NBTUtil.writeBlockPos(blockPos));
            invnodes.add(comp);
        }
        tag.put("invnodes", invnodes);

        ListNBT extractorNodes = new ListNBT();
        for (BlockPos blockPos : this.extractorNodes) {
            CompoundNBT comp = new CompoundNBT();
            comp.put("pos", NBTUtil.writeBlockPos(blockPos));
            extractorNodes.add(comp);
        }
        tag.put("extractorNodes", extractorNodes);

        ListNBT inserterNodes = new ListNBT();
        for (BlockPos blockPos : this.inserterNodes) {
            CompoundNBT comp = new CompoundNBT();
            comp.put("pos", NBTUtil.writeBlockPos(blockPos));
            inserterNodes.add(comp);
        }
        tag.put("inserterNodes", inserterNodes);
        return super.write(tag);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, final @Nullable Direction side) {
        if (cap == CapabilityEnergy.ENERGY)
            return energy.cast();

        return super.getCapability(cap, side);
    }

    @Override
    public void remove() {
        energy.invalidate();
        super.remove();
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
