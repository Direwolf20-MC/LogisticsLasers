package com.direwolf20.logisticslasers.common.tiles;

import com.direwolf20.logisticslasers.client.particles.itemparticle.ItemFlowParticleData;
import com.direwolf20.logisticslasers.common.blocks.ModBlocks;
import com.direwolf20.logisticslasers.common.capabilities.FEEnergyStorage;
import com.direwolf20.logisticslasers.common.container.ControllerContainer;
import com.direwolf20.logisticslasers.common.items.logiccards.BaseCard;
import com.direwolf20.logisticslasers.common.items.logiccards.CardExtractor;
import com.direwolf20.logisticslasers.common.items.logiccards.CardInserter;
import com.direwolf20.logisticslasers.common.tiles.basetiles.NodeTileBase;
import com.direwolf20.logisticslasers.common.util.ControllerTask;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.Item;
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
import java.util.*;

public class ControllerTile extends NodeTileBase implements ITickableTileEntity, INamedContainerProvider {

    //Data about the energy stored in this tile entity
    public FEEnergyStorage energyStorage;
    private LazyOptional<FEEnergyStorage> energy;

    //Data about the nodes this controller manages
    //Persistent data
    private final Set<BlockPos> inventoryNodes = new HashSet<>();
    private final Set<BlockPos> allNodes = new HashSet<>();
    private final SetMultimap<Long, ControllerTask> taskList = HashMultimap.create();

    //Non-Persistent data (Generated if empty)
    private final Set<BlockPos> extractorNodes = new HashSet<>(); //All Inventory nodes that contain an extractor card.
    private final Set<BlockPos> inserterNodes = new HashSet<>(); //All Inventory nodes that contain an inserter card
    private final HashMap<BlockPos, ArrayList<ItemStack>> filterCardCache = new HashMap<>(); //A cache of all cards in the entire network
    private final TreeMap<Integer, Set<BlockPos>> insertPriorities = new TreeMap<>(Collections.reverseOrder());

    private final IItemHandler EMPTY = new ItemStackHandler(0);

    private int ticksPerBlock = 4;

    public ControllerTile() {
        super(ModBlocks.CONTROLLER_TILE.get());
        this.energyStorage = new FEEnergyStorage(this, 0, 1000000);
        this.energy = LazyOptional.of(() -> this.energyStorage);
    }

    /**
     * This method clears the non-persistent inventory node data variables and regenerates them from scratch
     */
    public void refreshAllInvNodes() {
        System.out.println("Scanning all inventory nodes");
        extractorNodes.clear();
        inserterNodes.clear();
        filterCardCache.clear();
        for (BlockPos pos : inventoryNodes) {
            checkInvNode(pos);
        }
    }

    /**
     * Given a @param pos, look up the inventory node at that position in the world, and cache each of the cards in the cardCache Variable
     * Also populates the extractorNodes and inserterNodes variables, so we know which inventory nodes send/receive items.
     * This method is called by refreshAllInvNodes() or on demand when the contents of an inventory node's container is changed
     */
    public void checkInvNode(BlockPos pos) {
        InventoryNodeTile te = (InventoryNodeTile) world.getTileEntity(pos);
        extractorNodes.remove(pos);
        inserterNodes.remove(pos);
        filterCardCache.remove(pos);
        if (!te.hasController()) return;

        ItemStackHandler handler = te.getInventoryStacks();
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (stack.isEmpty()) continue;
            addToFilterCache(pos, stack);
            if (stack.getItem() instanceof CardExtractor)
                extractorNodes.add(pos);
            if (stack.getItem() instanceof CardInserter)
                inserterNodes.add(pos);
        }
    }

    //Adds an itemstack to the filterCardCache variable
    public void addToFilterCache(BlockPos pos, ItemStack itemStack) {
        ArrayList<ItemStack> tempArray = filterCardCache.getOrDefault(pos, new ArrayList<>());
        tempArray.add(itemStack);
        filterCardCache.put(pos, tempArray);
        if (!(itemStack.getItem() instanceof CardInserter)) return;

        //If Inserter - add to the insertPriorities
        int priority = BaseCard.getPriority(itemStack);
        Set<BlockPos> tempSet = insertPriorities.getOrDefault(priority, new HashSet<>());
        tempSet.add(pos);
        insertPriorities.put(priority, tempSet);
    }

    public Set<BlockPos> getInventoryNodes() {
        return inventoryNodes;
    }

    /**
     * Clears the cached route list of all inventory nodes - used when a network change occurs to rebuild the route table.
     */
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
        return allNodes.add(pos);
    }

    public boolean removeFromAllNodes(BlockPos pos) {
        return allNodes.remove(pos);
    }

    @Override
    public BlockPos getControllerPos() {
        return this.getPos();
    }

    @Override
    public void setControllerPos(BlockPos controllerPos, BlockPos sourcePos) {
        return; //NOOP
    }

    @Override
    public BlockPos validateController() {
        return this.getPos();  //I AM THE CONTROLLER!!!
    }

    @Override
    public boolean addNode(BlockPos pos) {
        NodeTileBase te = (NodeTileBase) world.getTileEntity(pos);
        if (te.hasController() && !te.getControllerPos().equals(getControllerPos()))
            return false;
        return super.addNode(pos);
    }

    /**
     * Get the item handler attached to an inventory node (Like an adjacent chest or furnace) at @param pos
     *
     * @return the item handler
     */
    public IItemHandler getAttachedInventory(BlockPos pos) {
        InventoryNodeTile sourceTE = (InventoryNodeTile) world.getTileEntity(pos);
        if (sourceTE == null || !(sourceTE instanceof InventoryNodeTile))
            return null; //Make sure the inventory node is still there
        IItemHandler sourceitemHandler = sourceTE.getHandler().orElse(EMPTY); //Get the inventory handler of the block the inventory node is facing
        if (sourceitemHandler.getSlots() == 0) return null; //If its empty, return null
        return sourceitemHandler;
    }

    public ArrayList<ItemStack> getExtractFilters(BlockPos pos) {
        ArrayList<ItemStack> tempList = new ArrayList<>(filterCardCache.get(pos));
        tempList.removeIf(s -> !(s.getItem() instanceof CardExtractor));
        return tempList;
    }

    public ArrayList<ItemStack> getInsertFilters(BlockPos pos) {
        ArrayList<ItemStack> tempList = new ArrayList<>(filterCardCache.get(pos));
        tempList.removeIf(s -> !(s.getItem() instanceof CardInserter));
        return tempList;
    }

    /**
     * Go through each of the extractorNodes and extract a single item based on the extractorCards they have. Send to an appropriate inserter.
     */
    public void handleExtractors() {
        boolean successfullySent = false;
        if (inserterNodes.size() == 0) return; //If theres nowhere to put items, nope out!
        for (BlockPos fromPos : extractorNodes) { //Loop through all the extractors!
            successfullySent = false;
            IItemHandler sourceitemHandler = getAttachedInventory(fromPos); //Get the inventory handler of the block the inventory node is facing
            if (sourceitemHandler == null) continue; //If its empty, move onto the next extractor

            for (ItemStack extractCard : getExtractFilters(fromPos)) {
                if (successfullySent) break;
                Set<Item> filteredItems = BaseCard.getFilteredItems(extractCard);
                for (int i = 0; i < sourceitemHandler.getSlots(); i++) { //Loop through the slots in the attached inventory
                    if (successfullySent) break;
                    ItemStack stackInSlot = sourceitemHandler.getStackInSlot(i);
                    if (stackInSlot.isEmpty())
                        continue; //If the slot is empty, move onto the next slot

                    if (!filteredItems.contains(stackInSlot.getItem()))
                        continue;

                    int extractAmt = 1;
                    ItemStack stack = sourceitemHandler.extractItem(i, extractAmt, true); //Pretend to remove the 1 item from the stack we found

                    for (BlockPos toPos : inserterNodes) { //If we found an item to transfer, start looping through the inserters
                        if (toPos.equals(fromPos)) continue; //No sending to yourself!
                        IItemHandler destitemHandler = getAttachedInventory(toPos); //Get the inventory handler of the block the inventory node is facing
                        if (destitemHandler == null) continue; //If its empty, move onto the next inserter

                        ItemStack simulated = ItemHandlerHelper.insertItem(destitemHandler, stack, true); //Pretend to insert it into the target inventory
                        if (simulated.equals(stack))
                            continue; //If the stack we removed matches the stack we simulated inserting, no changes happened (insert failed), so try another inserter

                        int count = stack.getCount() - simulated.getCount(); //If we had a full stack of 64 items, but only 32 fit into the chest, get the appropriate amount
                        ItemStack extractedStack = sourceitemHandler.extractItem(i, count, false); //Actually remove the items this time
                        successfullySent = transferItemStack(fromPos, toPos, extractedStack);
                        if (!successfullySent) { //Attempt to send items
                            ItemHandlerHelper.insertItem(sourceitemHandler, extractedStack, false); //If failed for some reason, put back in inventory
                        } else {
                            break; //If we successfully sent items to this inserter, stop finding inserters and move onto the next extractor.
                        }
                    }
                }
            }
        }
    }

    /**
     * If a @param stack attempts to insert into an already full inventory, we need to re-route it. From the position it failed to insert to (@param lostAt) try to send it to another
     * Valid inserter
     *
     * @return the remains of the itemstack that could not be inserted anywhere else in the network
     */
    public ItemStack handleLostStack(ItemStack stack, BlockPos lostAt) {
        for (BlockPos toPos : inserterNodes) { //Start looping through the inserters
            IItemHandler destitemHandler = getAttachedInventory(toPos); //Get the inventory handler of the block the inventory node is facing
            if (destitemHandler == null) continue; //If its empty, move onto the next inserter

            ItemStack simulated = ItemHandlerHelper.insertItem(destitemHandler, stack, true); //Pretend to insert it into the target inventory
            if (simulated.equals(stack))
                continue; //If the stack we removed matches the stack we simulated inserting, no changes happened (insert failed), so try another inserter

            int count = stack.getCount() - simulated.getCount(); //If we had a full stack of 64 items, but only 32 fit into the chest, get the appropriate amount
            ItemStack extractedStack = stack.split(count);
            if (!transferItemStack(lostAt, toPos, extractedStack)) { //Attempt to send items
                stack.grow(count); //If failed for some reason, put back into the stack
            } else {
                if (stack.isEmpty())
                    break; //If we successfully sent items to this inserter, stop finding inserters and move onto the next extractor.
                else
                    continue;
            }
        }
        return stack;
    }


    /**
     * Send a @param itemStack from @param fromPos to @param toPos, scheduling each step along the way for particle rendering purposes
     *
     * @return if this was successful
     */
    public boolean transferItemStack(BlockPos fromPos, BlockPos toPos, ItemStack itemStack) {
        ticksPerBlock = 4;
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

    /**
     * Handle all scheduled tasks due at the current gametime.
     * TODO Deal with gametime being in the past (Unloaded chunks situation)
     */
    public void handleTasks() {
        long gameTime = world.getGameTime();
        Set<ControllerTask> tasksThisTick = taskList.get(gameTime);

        for (ControllerTask task : tasksThisTick) {
            executeTask(task);
        }
        taskList.removeAll(gameTime);
    }

    /**
     * Given a @param task, execute whatever that task is. See the ControllerTask class.
     */
    public void executeTask(ControllerTask task) {
        //System.out.println(task.taskType + ": " + task.fromPos + "->" + task.toPos + ": " + task.itemStack);
        if (task.isParticle()) {
            doParticles(task);
        } else if (task.isInsert()) {
            ItemStack remainingStack = doInsert(task);
            if (!remainingStack.isEmpty()) {
                ItemStack stillRemaining = handleLostStack(remainingStack, task.toPos);
                Block.spawnAsEntity(world, task.toPos, stillRemaining); //TODO Implement storing in the controller
            }
        } else if (task.isExtract()) {

        }
    }

    /**
     * Called by ExecuteTask - spawn particles from one node to another for transit
     */
    public void doParticles(ControllerTask task) {
        ItemFlowParticleData data = new ItemFlowParticleData(task.itemStack, task.toPos.getX() + 0.5, task.toPos.getY() + 0.5, task.toPos.getZ() + 0.5, ticksPerBlock);
        ServerWorld serverWorld = (ServerWorld) world;
        serverWorld.spawnParticle(data, task.fromPos.getX() + 0.5, task.fromPos.getY() + 0.5, task.fromPos.getZ() + 0.5, 8, 0.1f, 0.1f, 0.1f, 0);
    }

    /**
     * Called by ExecuteTask - attempt to insert an item into the destination inventory.
     *
     * @return the remains of the itemstack (Anything that failed to insert)
     */
    public ItemStack doInsert(ControllerTask task) {
        IItemHandler destitemHandler = getAttachedInventory(task.toPos);
        if (destitemHandler == null) return task.itemStack;

        ItemStack stack = task.itemStack;
        ItemStack postInsertStack = ItemHandlerHelper.insertItem(destitemHandler, stack, false);
        return postInsertStack;
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
            if (inventoryNodes.size() > 0 && (extractorNodes.isEmpty() && inserterNodes.isEmpty()))
                refreshAllInvNodes();
            handleExtractors();
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

        /*extractorNodes.clear();
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
        }*/
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

        /*ListNBT extractorNodes = new ListNBT();
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
        tag.put("inserterNodes", inserterNodes);*/
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
