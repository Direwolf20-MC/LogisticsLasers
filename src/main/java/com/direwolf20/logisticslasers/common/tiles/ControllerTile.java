package com.direwolf20.logisticslasers.common.tiles;

import com.direwolf20.logisticslasers.client.particles.itemparticle.ItemFlowParticleData;
import com.direwolf20.logisticslasers.common.blocks.ModBlocks;
import com.direwolf20.logisticslasers.common.capabilities.FEEnergyStorage;
import com.direwolf20.logisticslasers.common.container.ControllerContainer;
import com.direwolf20.logisticslasers.common.items.logiccards.*;
import com.direwolf20.logisticslasers.common.tiles.basetiles.NodeTileBase;
import com.direwolf20.logisticslasers.common.util.ControllerTask;
import com.direwolf20.logisticslasers.common.util.ItemHandlerUtil;
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
    private final HashMap<ControllerTask, ArrayList<ControllerTask>> parentTaskMap = new HashMap<>();

    //Non-Persistent data (Generated if empty)
    private final Set<BlockPos> extractorNodes = new HashSet<>(); //All Inventory nodes that contain an extractor card.
    private final Set<BlockPos> inserterNodes = new HashSet<>(); //All Inventory nodes that contain an inserter card
    private final Set<BlockPos> providerNodes = new HashSet<>(); //All Inventory nodes that contain an provider card
    private final Set<BlockPos> stockerNodes = new HashSet<>(); //All Inventory nodes that contain an stocker card

    private final HashMap<BlockPos, ArrayList<ItemStack>> filterCardCache = new HashMap<>(); //A cache of all cards in the entire network

    private final TreeMap<Integer, Set<BlockPos>> insertPriorities = new TreeMap<>(Collections.reverseOrder()); //A sorted list of inserter cards by priority
    private final HashMap<Item, ArrayList<BlockPos>> inserterCache = new HashMap<>(); //A cache of all insertable items
    private final HashMap<Item, ArrayList<BlockPos>> providerCache = new HashMap<>(); //A cache of all providable items

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
        providerNodes.clear();
        stockerNodes.clear();

        filterCardCache.clear();
        inserterCache.clear();
        providerCache.clear();
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
        providerNodes.remove(pos);
        stockerNodes.remove(pos);

        filterCardCache.remove(pos);
        removeBlockPosFromPriorities(pos);
        if (!te.hasController()) return;

        ItemStackHandler handler = te.getInventoryStacks();
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (stack.isEmpty()) continue;
            addToFilterCache(pos, stack);
            if (stack.getItem() instanceof CardExtractor)
                extractorNodes.add(pos);
            if (stack.getItem() instanceof CardInserter) {
                inserterNodes.add(pos);
                inserterCache.clear(); //Any change to inserter cards will affect the inserter cache
            }
            if (stack.getItem() instanceof CardProvider) {
                providerNodes.add(pos);
                providerCache.clear(); //Any change to inserter cards will affect the inserter cache
            }
            if (stack.getItem() instanceof CardStocker)
                stockerNodes.add(pos);
        }
    }

    public void removeBlockPosFromPriorities(BlockPos pos) {
        for (Map.Entry<Integer, Set<BlockPos>> priorityMap : insertPriorities.entrySet()) {
            priorityMap.getValue().remove(pos);
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
            providerNodes.remove(pos);
            stockerNodes.remove(pos);

            filterCardCache.remove(pos);
            inserterCache.clear(); //Any change to inserter cards will affect the inserter cache
            providerCache.clear(); //Any chance to provider cards will affect the provider cache
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

    //Prevents a block that has another controller from connecting to this controller, including other controllers themselves.
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
        if (filterCardCache.containsKey(pos)) {
            ArrayList<ItemStack> tempList = new ArrayList<>(filterCardCache.get(pos));
            tempList.removeIf(s -> !(s.getItem() instanceof CardExtractor));
            return tempList;
        }
        return new ArrayList<>();
    }

    public ArrayList<ItemStack> getInsertFilters(BlockPos pos) {
        if (filterCardCache.containsKey(pos)) {
            ArrayList<ItemStack> tempList = new ArrayList<>(filterCardCache.get(pos));
            tempList.removeIf(s -> !(s.getItem() instanceof CardInserter));
            return tempList;
        }
        return new ArrayList<>();
    }

    public ArrayList<ItemStack> getProviderFilters(BlockPos pos) {
        if (filterCardCache.containsKey(pos)) {
            ArrayList<ItemStack> tempList = new ArrayList<>(filterCardCache.get(pos));
            tempList.removeIf(s -> !(s.getItem() instanceof CardProvider));
            return tempList;
        }
        return new ArrayList<>();
    }

    public ArrayList<ItemStack> getStockerFilters(BlockPos pos) {
        if (filterCardCache.containsKey(pos)) {
            ArrayList<ItemStack> tempList = new ArrayList<>(filterCardCache.get(pos));
            tempList.removeIf(s -> !(s.getItem() instanceof CardStocker));
            return tempList;
        }
        return new ArrayList<>();
    }

    /**
     * Given an @param itemStack, find a valid destination either from an existing cache, or looping through all known inserters.
     * Excludes @param fromPos to ensure items are not inserted into the source chest
     *
     * @return a list of possible destinations
     */
    public ArrayList<BlockPos> findDestinationForItemstack(ItemStack itemStack) {
        if (inserterCache.containsKey(itemStack.getItem()))
            return inserterCache.get(itemStack.getItem());
        System.out.println("Building Inserter Cache for: " + itemStack.getItem());
        ArrayList<BlockPos> tempArray = new ArrayList<>();
        for (int priority : insertPriorities.keySet()) {
            for (BlockPos toPos : insertPriorities.get(priority)) { //If we found an item to transfer, start looping through the inserters
                for (ItemStack insertCard : getInsertFilters(toPos)) { //Loop through all the cached insertCards
                    Set<Item> filteredInsertItems = BaseCard.getFilteredItems(insertCard); //Get the list of items this card allows
                    if (BaseCard.getWhiteList(insertCard)) {
                        if (!filteredInsertItems.contains(itemStack.getItem()))
                            continue; //Move onto the next card if this card doesn't accept this item
                    } else {
                        if (filteredInsertItems.contains(itemStack.getItem()))
                            continue; //Move onto the next card if this card doesn't accept this item
                    }
                    tempArray.add(toPos);
                }
            }
        }
        inserterCache.put(itemStack.getItem(), tempArray);
        return inserterCache.get(itemStack.getItem());
    }

    /**
     * Given an @param itemStack, find a valid provider either from an existing cache, or looping through all known providers.
     * Excludes @param fromPos to ensure items are not extracted from the stocking chest
     *
     * @return a list of possible destinations
     */
    public ArrayList<BlockPos> findProviderForItemstack(ItemStack itemStack) {
        if (providerCache.containsKey(itemStack.getItem()))
            return providerCache.get(itemStack.getItem());
        System.out.println("Building Provider Cache for: " + itemStack.getItem());
        ArrayList<BlockPos> tempArray = new ArrayList<>();
        //for (int priority : insertPriorities.keySet()) { //In case i decide to implement provider priorities
        for (BlockPos toPos : providerNodes) { //Loop through all provider nodes
            for (ItemStack providerCard : getProviderFilters(toPos)) { //Loop through all the cached providerCards
                Set<Item> filteredInsertItems = BaseCard.getFilteredItems(providerCard); //Get the list of items this card allows
                if (BaseCard.getWhiteList(providerCard)) {
                    if (!filteredInsertItems.contains(itemStack.getItem()))
                        continue; //Move onto the next card if this card doesn't accept this item
                } else {
                    if (filteredInsertItems.contains(itemStack.getItem()))
                        continue; //Move onto the next card if this card doesn't accept this item
                }
                tempArray.add(toPos);
            }
        }
        //}
        providerCache.put(itemStack.getItem(), tempArray);
        return providerCache.get(itemStack.getItem());
    }

    public int testInsertToInventory(IItemHandler destitemHandler, BlockPos toPos, ItemStack stack) {
        ItemHandlerUtil.InventoryInfo tempInventory = new ItemHandlerUtil.InventoryInfo(destitemHandler); //tempInventory tracks all changes that in-route stacks would make
        for (ItemStack inFlightStack : getItemStacksInFlight(toPos)) { //Add all in-flight stacks to the temp inventory
            ItemHandlerUtil.simulateInsert(destitemHandler, tempInventory, inFlightStack, inFlightStack.getCount(), true);
        }
        //At this point in the code, the tempInventory represents what the toPos chest will have INCLUDING all in-flight stacks
        int remainder = ItemHandlerUtil.simulateInsert(destitemHandler, tempInventory, stack, stack.getCount(), false); //Returns the amount of items that don't fit
        int count = stack.getCount() - remainder; //How many items will fit in the inventory
        return count;
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

            for (ItemStack extractCard : getExtractFilters(fromPos)) { //Get all extractor cards in the inventory node we're working on
                if (successfullySent)
                    break; //If we've sent something from this card in the last iteration, break out and go to the next inventory node
                Set<Item> filteredItems = BaseCard.getFilteredItems(extractCard); //Get all the items this card is allowed to extract
                for (int i = 0; i < sourceitemHandler.getSlots(); i++) { //Loop through the slots in the attached inventory
                    if (successfullySent) break;
                    ItemStack stackInSlot = sourceitemHandler.getStackInSlot(i);
                    if (stackInSlot.isEmpty())
                        continue; //If the slot is empty, move onto the next slot

                    if (BaseCard.getWhiteList(extractCard)) {
                        if (!filteredItems.contains(stackInSlot.getItem()))
                            continue;
                    } else {
                        if (filteredItems.contains(stackInSlot.getItem()))
                            continue;
                    }

                    int extractAmt = 16;
                    ItemStack stack = sourceitemHandler.extractItem(i, extractAmt, true); //Pretend to remove the x items from the stack we found
                    ArrayList<BlockPos> possibleDestinations = findDestinationForItemstack(stack); //Find a list of possible destinations
                    possibleDestinations.remove(fromPos); //Remove the block its coming from, no self-sending!
                    if (possibleDestinations.isEmpty())
                        continue; //If we can't send this item anywhere, move onto the next item
                    for (BlockPos toPos : possibleDestinations) { //Loop through all possible destinations
                        IItemHandler destitemHandler = getAttachedInventory(toPos); //Get the inventory handler of the block the inventory node is facing
                        if (destitemHandler == null) continue; //If its empty, move onto the next inserter

                        /*ItemHandlerUtil.InventoryInfo tempInventory = new ItemHandlerUtil.InventoryInfo(destitemHandler); //tempInventory tracks all changes that in-route stacks would make
                        for (ItemStack inFlightStack : getItemStacksInFlight(toPos)) { //Add all in-flight stacks to the temp inventory
                            ItemHandlerUtil.simulateInsert(destitemHandler, tempInventory, inFlightStack, inFlightStack.getCount(), true);
                        }
                        //At this point in the code, the tempInventory represents what the toPos chest will have INCLUDING all in-flight stacks
                        int remainder = ItemHandlerUtil.simulateInsert(destitemHandler, tempInventory, stack, stack.getCount(), false); //Returns the amount of items that don't fit
                        int count = stack.getCount() - remainder; //How many items will fit in the inventory*/
                        int count = testInsertToInventory(destitemHandler, toPos, stack); //Find out how many items can fit in the destination inventory, including inflight items
                        if (count == 0) continue; //If none, try elsewhere!

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
     * Go through each of the stockerNodes and find a provider offering the item - transfer it to this inventory if found.
     */
    public void handleStockers() {
        if (providerNodes.size() == 0) return; //If theres nowhere to get items from, nope out!
        for (BlockPos stockerPos : stockerNodes) { //Loop through all the stocker cards!
            boolean successfullySent = false;
            IItemHandler stockerItemHandler = getAttachedInventory(stockerPos); //Get the inventory handler of the block the stocker's inventory node is facing
            if (stockerItemHandler == null) continue; //If its empty, move onto the next stocker

            for (ItemStack stockerCard : getStockerFilters(stockerPos)) { //Find all stocker cards in this node
                if (successfullySent) break; //If this node already requested an item this tick, cancel out
                Set<Item> filteredItems = BaseCard.getFilteredItems(stockerCard); //Get all the items we should be requesting
                for (Item item : filteredItems) { //Loop through each itemstack in the requested set of items
                    ArrayList<BlockPos> possibleProviders = findProviderForItemstack(new ItemStack(item)); //Find a list of possible Providers TODO: Proper Itemstack
                    possibleProviders.remove(stockerPos);
                    if (possibleProviders.isEmpty()) continue;
                    int countOfItem = 0; //How many are currently in the inventory
                    int desiredAmt = 32; //ToDo filter based
                    for (int i = 0; i < stockerItemHandler.getSlots(); i++) { //Loop through the slots in the attached inventory
                        ItemStack stackInSlot = stockerItemHandler.getStackInSlot(i);
                        if (stackInSlot.isEmpty())
                            continue; //If the slot is empty, move onto the next slot
                        if (!stackInSlot.getItem().equals(item))
                            continue; //Don't count different items, duh!
                        countOfItem += stackInSlot.getCount();
                        if (countOfItem >= desiredAmt)
                            break; //We're done going through the chest if we found enough of the item
                    }

                    if (countOfItem >= desiredAmt)
                        break; //We're done checking for this item if we found enough of the item

                    countOfItem += countItemsInFlight(item, stockerPos); //Count the items in flight to this destination

                    if (countOfItem >= desiredAmt)
                        break; ///We're done checking for this item if we found enough of the item including items in flight

                    int extractAmt = 1;
                    ItemStack stack = new ItemStack(item, extractAmt); //Create an item stack

                    //Before we even look for the item to insert, lets see if it'll fit here first!
                    int count = testInsertToInventory(stockerItemHandler, stockerPos, stack);
                    if (count == 0) continue; //If we can't fit any items in here, nope out!
                    if (count < stack.getCount())
                        stack.setCount(count); //If we can only fit 8 items, but were trying to get 16, adjust to 8

                    for (BlockPos providerPos : possibleProviders) { //Loop through all possible Providers
                        IItemHandler providerItemHandler = getAttachedInventory(providerPos); //Get the inventory handler of the block the inventory node is facing
                        if (providerItemHandler == null) continue; //If its empty, move onto the next provider

                        ItemStack simulated = ItemHandlerUtil.extractItem(providerItemHandler, stack, true); //Pretend to extract the stack from the provider's inventory

                        if (simulated.getCount() == 0) {
                            continue; //If the stack we removed has zero items in it check another provider
                        }
                        int extractCount = simulated.getCount(); //How many items were successfully removed from the inventory
                        stack.setCount(extractCount);
                        ItemStack extractedStack = ItemHandlerUtil.extractItem(providerItemHandler, stack, false); //Actually remove the items this time
                        successfullySent = transferItemStack(providerPos, stockerPos, extractedStack);
                        if (!successfullySent) { //Attempt to send items
                            ItemHandlerHelper.insertItem(providerItemHandler, extractedStack, false); //If failed for some reason, put back in inventory
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
        for (BlockPos toPos : findDestinationForItemstack(stack)) { //Start looping through the inserters
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
        ControllerTask parentTask = new ControllerTask(fromPos, toPos, ControllerTask.TaskType.INSERT, itemStack, null); //Create a parent task, this isn't executed, but is used to track items in flight
        UUID parentGuid = parentTask.guid;
        ArrayList<ControllerTask> taskArrayList = new ArrayList<>();
        for (int r = 0; r < route.size(); r++) {
            if (r == route.size() - 1) { //This is the last step of the route, so insert into the attached inventory
                task = new ControllerTask(route.get(r - 1), route.get(r), ControllerTask.TaskType.INSERT, itemStack, parentGuid);
                taskList.put(tempGameTime, task);
                taskArrayList.add(task);
            } else { //This is not the last step of the route, so schedule particle spawning
                BlockPos from = route.get(r);
                BlockPos to = route.get(r + 1);
                task = new ControllerTask(from, to, ControllerTask.TaskType.PARTICLE, itemStack, parentGuid);
                taskList.put(tempGameTime, task);
                taskArrayList.add(task);
                Vector3d fromVec = new Vector3d(from.getX(), from.getY(), from.getZ());
                Vector3d toVec = new Vector3d(to.getX(), to.getY(), to.getZ());
                double distance = fromVec.distanceTo(toVec);
                int duration = (int) distance * ticksPerBlock;
                tempGameTime += duration;
            }
        }
        parentTaskMap.put(parentTask, taskArrayList);
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
            if (!task.isCancelled)
                executeTask(task);
            ControllerTask parentTask = findParentTaskByGUID(task.parentGUID);
            if (parentTask == null) {
                System.out.println("Something weird happened");
            } else {
                ArrayList<ControllerTask> taskArrayList = parentTaskMap.get(parentTask);
                taskArrayList.remove(task);
                if (taskArrayList.isEmpty())
                    parentTaskMap.remove(parentTask);
                else
                    parentTaskMap.put(parentTask, taskArrayList);
            }
        }
        taskList.removeAll(gameTime);
    }

    /**
     * Given the @param guid of a task, find it in the parentTaskList and @return the task associated with it
     */
    public ControllerTask findParentTaskByGUID(UUID guid) {
        for (ControllerTask parentTask : parentTaskMap.keySet()) {
            if (parentTask.guid == guid) {
                return parentTask;
            }
        }
        return null;
    }

    public int countItemsInFlight(Item item, BlockPos toPos) {
        int count = 0;
        for (ControllerTask parentTask : parentTaskMap.keySet()) {
            if (parentTask.itemStack.getItem().equals(item) && parentTask.toPos.equals(toPos)) {
                count += parentTask.itemStack.getCount();
            }
        }
        return count;
    }

    public Set<ItemStack> getItemStacksInFlight(BlockPos toPos) {
        Set<ItemStack> flightStacks = new HashSet<>();
        for (ControllerTask parentTask : parentTaskMap.keySet()) {
            if (parentTask.toPos.equals(toPos)) {
                flightStacks.add(parentTask.itemStack.copy());
            }
        }
        return flightStacks;
    }

    /**
     * Given a @param task, execute whatever that task is. See the ControllerTask class.
     */
    public void executeTask(ControllerTask task) {
        //System.out.println(task.taskType + ": " + task.fromPos + "->" + task.toPos + ": " + task.itemStack);
        if (task.isParticle()) {
            ItemStack remainingStack = doParticles(task);
            if (!remainingStack.isEmpty()) {
                cancelTask(task.parentGUID);
                Block.spawnAsEntity(world, task.fromPos, remainingStack); //TODO Implement storing in the controller
            }
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
    public ItemStack doParticles(ControllerTask task) {
        TileEntity fromTE = world.getTileEntity(task.fromPos);
        TileEntity toTE = world.getTileEntity(task.toPos);
        if (!(fromTE instanceof NodeTileBase) || !(toTE instanceof NodeTileBase)) {
            return task.itemStack;
        }
        ItemFlowParticleData data = new ItemFlowParticleData(task.itemStack, task.toPos.getX() + 0.5, task.toPos.getY() + 0.5, task.toPos.getZ() + 0.5, ticksPerBlock);
        ServerWorld serverWorld = (ServerWorld) world;
        serverWorld.spawnParticle(data, task.fromPos.getX() + 0.5, task.fromPos.getY() + 0.5, task.fromPos.getZ() + 0.5, 8 * task.itemStack.getCount(), 0.1f, 0.1f, 0.1f, 0);
        return ItemStack.EMPTY;
    }

    /**
     * Called by ExecuteTask - attempt to insert an item into the destination inventory.
     *
     * @return the remains of the itemstack (Anything that failed to insert)
     */
    public ItemStack doInsert(ControllerTask task) {
        if (!stockerNodes.contains(task.toPos)) {
            if (!findDestinationForItemstack(task.itemStack).contains(task.toPos)) return task.itemStack;
        }
        IItemHandler destitemHandler = getAttachedInventory(task.toPos);
        if (destitemHandler == null) return task.itemStack;

        ItemStack stack = task.itemStack;
        ItemStack postInsertStack = ItemHandlerHelper.insertItem(destitemHandler, stack, false);
        return postInsertStack;
    }

    public void cancelTask(UUID parentGUID) {
        for (long gameTime : taskList.keySet()) {
            Set<ControllerTask> tasks = taskList.get(gameTime);
            for (ControllerTask task : tasks) {
                if (task.parentGUID == parentGUID)
                    task.cancel();
            }
        }
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
            if (inventoryNodes.size() > 0 && (extractorNodes.isEmpty() && inserterNodes.isEmpty() && providerNodes.isEmpty() && stockerNodes.isEmpty())) //Todo cleaner
                refreshAllInvNodes();
            handleExtractors();
            handleStockers();
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
