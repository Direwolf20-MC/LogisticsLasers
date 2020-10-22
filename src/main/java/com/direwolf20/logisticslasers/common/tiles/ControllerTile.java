package com.direwolf20.logisticslasers.common.tiles;

import com.direwolf20.logisticslasers.client.particles.itemparticle.ItemFlowParticleData;
import com.direwolf20.logisticslasers.client.renders.LaserConnections;
import com.direwolf20.logisticslasers.common.blocks.ModBlocks;
import com.direwolf20.logisticslasers.common.capabilities.FEEnergyStorage;
import com.direwolf20.logisticslasers.common.container.ControllerContainer;
import com.direwolf20.logisticslasers.common.items.logiccards.*;
import com.direwolf20.logisticslasers.common.network.PacketHandler;
import com.direwolf20.logisticslasers.common.network.packets.PacketItemCountsSync;
import com.direwolf20.logisticslasers.common.tiles.basetiles.NodeTileBase;
import com.direwolf20.logisticslasers.common.util.ControllerTask;
import com.direwolf20.logisticslasers.common.util.ItemHandlerUtil;
import com.direwolf20.logisticslasers.common.util.ItemStackKey;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
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
import java.util.*;

import static com.direwolf20.logisticslasers.common.util.MiscTools.isStackValidForCard;

public class ControllerTile extends NodeTileBase implements ITickableTileEntity, INamedContainerProvider {

    //Data about the energy stored in this tile entity
    public FEEnergyStorage energyStorage;
    private LazyOptional<FEEnergyStorage> energy;

    //Data about the nodes this controller manages
    //Persistent data
    private final Set<BlockPos> crafterNodes = new HashSet<>();
    private final Set<BlockPos> inventoryNodes = new HashSet<>();
    private final Set<BlockPos> allNodes = new HashSet<>();
    private final Set<ControllerTask> taskList = new HashSet<>();
    private final HashMap<ControllerTask, ArrayList<ControllerTask>> parentTaskMap = new HashMap<>();
    private ItemHandlerUtil.InventoryCounts storedItems = new ItemHandlerUtil.InventoryCounts();

    //Non-Persistent data (Generated if empty)
    private final Set<BlockPos> extractorNodes = new HashSet<>(); //All Inventory nodes that contain an extractor card.
    private final Set<BlockPos> inserterNodes = new HashSet<>(); //All Inventory nodes that contain an inserter card
    private final Set<BlockPos> providerNodes = new HashSet<>(); //All Inventory nodes that contain a provider card
    private final Set<BlockPos> stockerNodes = new HashSet<>(); //All Inventory nodes that contain a stocker card
    private final HashMap<BlockPos, ArrayList<ItemStack>> filterCardCache = new HashMap<>(); //A cache of all cards in the entire network
    private final TreeMap<Integer, Set<BlockPos>> insertPriorities = new TreeMap<>(Collections.reverseOrder()); //A sorted list of inserter cards by priority
    private final HashMap<ItemStackKey, ArrayList<BlockPos>> extractorCache = new HashMap<>(); //A cache of all insertable items
    private final HashMap<ItemStackKey, ArrayList<BlockPos>> inserterCache = new HashMap<>(); //A cache of all insertable items
    private final HashMap<ItemStackKey, ArrayList<BlockPos>> providerCache = new HashMap<>(); //A cache of all providable items
    private final HashMap<BlockPos, ArrayList<ItemStack>> stockerCache = new HashMap<>(); //A cache of all stocker requests
    private ItemHandlerUtil.InventoryCounts itemCounts = new ItemHandlerUtil.InventoryCounts(); //A cache of all items available via providerCards for the CraftingStations to use
    private Object2IntMap<BlockPos> invNodeSlot = new Object2IntOpenHashMap<>(); //Used to track which slot an inventory node is currently working on.
    private Object2IntMap<BlockPos> stockerSlot = new Object2IntOpenHashMap<>(); //Used to track which stocker item an inventory node is currently working on.
    private Table<BlockPos, ItemStackKey, Integer> extractorAmounts = HashBasedTable.create();


    private final IItemHandler EMPTY = new ItemStackHandler(0);

    private int ticksPerBlock = 4; //How fast the items move through the network, x ticks per block length

    public ControllerTile() {
        super(ModBlocks.CONTROLLER_TILE.get());
        this.energyStorage = new FEEnergyStorage(this, 0, 1000000);
        this.energy = LazyOptional.of(() -> this.energyStorage);
    }

    //Misc getters and setters
    public Set<BlockPos> getProviderNodes() {
        return providerNodes;
    }

    public void setItemCounts(ItemHandlerUtil.InventoryCounts itemCounts) {
        this.itemCounts = itemCounts;
    }

    public ItemHandlerUtil.InventoryCounts getItemCounts() {
        return itemCounts;
    }

    public Set<BlockPos> getInventoryNodes() {
        return inventoryNodes;
    }


    public ItemHandlerUtil.InventoryCounts getStoredItems() {
        return storedItems;
    }

    public boolean hasStoredItems() {
        return !storedItems.getItemCounts().isEmpty();
    }


    /**
     * Resets all the cached node data and rediscovers the network by depth first searching (I think).
     */
    public void discoverAllNodes() {
        System.out.println("Discovering All Nodes!");
        Set<BlockPos> oldNodes = new HashSet<>(allNodes); //Store the list of nodes this used to control, used to remove controller data from that pos later
        //Clear all the cached node data
        allNodes.clear();
        crafterNodes.clear();
        inventoryNodes.clear();

        Queue<BlockPos> nodesToCheck = new LinkedList<>();
        Set<BlockPos> checkedNodes = new HashSet<>();
        nodesToCheck.addAll(connectedNodes); //Add all the nodes connected to this controller to the list of nodes to check out
        clearCachedRoutes(); //Clear the cached routing of all nodes in the network

        while (nodesToCheck.size() > 0) {
            BlockPos posToCheck = nodesToCheck.remove(); //Pop the stack
            if (checkedNodes.contains(posToCheck) || posToCheck.equals(this.pos))
                continue; //Don't check nodes we've checked before, and don't operate on the controller itself
            checkedNodes.add(posToCheck);
            TileEntity te = world.getTileEntity(posToCheck);
            if (te instanceof NodeTileBase) {
                addToAllNodes(posToCheck); //Add this node to the all nodes list
                Set<BlockPos> connectedNodes = ((NodeTileBase) te).getConnectedNodes(); //Get all the nodes this node is connected to
                nodesToCheck.addAll(connectedNodes); //Add them to the list to check
                ((NodeTileBase) te).setControllerPos(this.pos); //Set this node's controller to this position
                oldNodes.remove(posToCheck);
                if (te instanceof CraftingStationTile)
                    addToCraftNodes(posToCheck);
                else if (te instanceof InventoryNodeTile)
                    addToInvNodes(posToCheck);
            }
        }
        for (BlockPos confirmPos : oldNodes) { //Loop through all the nodes we 'used' to be connected to, if we're not connected anymore, set their controller to Zero
            //if (!allNodes.contains(confirmPos)) {
            TileEntity te = world.getTileEntity(confirmPos);
            if (te instanceof NodeTileBase)
                ((NodeTileBase) te).setControllerPos(BlockPos.ZERO);
            //}

        }
        refreshAllInvNodes();
        LaserConnections.buildLaserList();
    }

    /**
     * Builds the itemCounts cache, used to display contents of the network at the crafting station
     * Sends a packet to the @param player's client updating it for client-side display in CraftingStationScreen
     */
    public void updateItemCounts(ServerPlayerEntity player) {
        itemCounts = new ItemHandlerUtil.InventoryCounts();
        Set<BlockPos> providers = getProviderNodes();
        for (BlockPos pos : providers) {
            ArrayList<ItemStack> providerFilters = getProviderFilters(pos);
            IItemHandler handler = getAttachedInventory(pos);
            for (ItemStack providerFilter : providerFilters) {
                itemCounts.addHandlerWithFilter(handler, providerFilter);
            }
        }
        PacketHandler.sendTo(new PacketItemCountsSync(itemCounts, pos), player);
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
        extractorCache.clear();
        stockerCache.clear();
        extractorAmounts.clear();
        for (BlockPos pos : inventoryNodes) {
            checkInvNode(pos);
        }
    }

    /**
     * Given a @param pos, look up the inventory node at that position in the world, and cache each of the cards in the cardCache Variable
     * Also populates the extractorNodes and inserterNodes variables, so we know which inventory nodes send/receive items.
     * Also populates the providerNodes and stockerNodes variables, so we know which inventory nodes provide or keep in stock items.
     * This method is called by refreshAllInvNodes() or on demand when the contents of an inventory node's container is changed
     */
    public void checkInvNode(BlockPos pos) {
        System.out.println("Updating cache at: " + pos);
        InventoryNodeTile te = (InventoryNodeTile) world.getTileEntity(pos);
        //Remove this position from all caches, so we can repopulate below
        extractorNodes.remove(pos);
        inserterNodes.remove(pos);
        providerNodes.remove(pos);
        stockerNodes.remove(pos);

        filterCardCache.remove(pos);
        inserterCache.clear(); //Any change to inserter cards will affect the inserter cache
        providerCache.clear(); //Any change to provider cards will affect the provider cache
        extractorCache.clear();
        stockerCache.clear();
        extractorAmounts.clear();
        removeBlockPosFromPriorities(pos); //Remove this position form the inserter priorities
        if (!te.hasController()) return; //If this tile was removed from the network, don't recalculate its contents

        ItemStackHandler handler = te.getInventoryStacks();
        //Loop through all cards and update the cache'd data
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (stack.isEmpty()) continue;
            addToFilterCache(pos, stack);
            if (stack.getItem() instanceof CardExtractor)
                extractorNodes.add(pos);
            if (stack.getItem() instanceof CardInserter) {
                inserterNodes.add(pos);
            }
            if (stack.getItem() instanceof CardProvider) {
                providerNodes.add(pos);
            }
            if (stack.getItem() instanceof CardStocker)
                stockerNodes.add(pos);
        }
    }

    /**
     * Given a @param pos, remove it from the priorities list.
     * This is used when an inserter card is removed/changed to ensure its no longer listen in the inserterPriorities
     */
    public void removeBlockPosFromPriorities(BlockPos pos) {
        for (Map.Entry<Integer, Set<BlockPos>> priorityMap : insertPriorities.entrySet()) {
            priorityMap.getValue().remove(pos);
        }
    }

    /**
     * Adds a @param itemStack to the filterCardCache variable for @param pos
     */
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

    /**
     * Clears the cached route list of all inventory nodes - used when a network change occurs to rebuild the route table on next send.
     */
    public void clearCachedRoutes() {
        for (BlockPos pos : inventoryNodes) {
            InventoryNodeTile te = (InventoryNodeTile) world.getTileEntity(pos);
            if (te == null) continue;
            te.clearRouteList();
        }
        for (BlockPos pos : crafterNodes) {
            CraftingStationTile te = (CraftingStationTile) world.getTileEntity(pos);
            if (te == null) continue;
            te.clearRouteList();
        }
        clearRouteList(); //Also clear the controller's route list
    }

    /**
     * Called when a crafter is added to the network at @param pos
     *
     * @return if this was successful, which it should always be
     */
    public boolean addToCraftNodes(BlockPos pos) {
        return crafterNodes.add(pos);
    }

    /**
     * Called when an inventory node is added to the network at @param pos
     *
     * @return if this was successful, which it should always be
     */
    public boolean addToInvNodes(BlockPos pos) {
        return inventoryNodes.add(pos);
    }

    /**
     * Adds the @param pos to the 'all nodes' cache
     *
     * @return successful
     */
    public boolean addToAllNodes(BlockPos pos) {
        return allNodes.add(pos);
    }

    /**
     * Gets the controller position for other nodes, since this is the controller return itself
     *
     * @return BlockPos of this block
     */
    @Override
    public BlockPos getControllerPos() {
        return this.getPos();
    }

    /**
     * Used by other nodes to set the @param controllerPos
     * Not used here since this is the controller
     */
    @Override
    public void setControllerPos(BlockPos controllerPos) {
        return; //NOOP
    }

    @Override
    public void disconnectAllNodes() {
        for (BlockPos pos : allNodes) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof NodeTileBase) {
                ((NodeTileBase) te).setControllerPos(BlockPos.ZERO);
            }
        }
        for (BlockPos pos : connectedNodes) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof NodeTileBase) {
                ((NodeTileBase) te).removeNode(this.pos);
            }
        }
    }

    /**
     * Prevents a block that has another controller from connecting to this controller, including other controllers themselves.
     */
    @Override
    public boolean addNode(BlockPos pos) {
        NodeTileBase te = (NodeTileBase) world.getTileEntity(pos);
        if (te.hasController() && !te.getControllerPos().equals(getControllerPos()))
            return false;
        return super.addNode(pos);
    }

    /**
     * Get the item handler attached to an inventory node (Like an adjacent chest or furnace) at @param pos
     * If this is a crafting station, return the crafter's inventory
     *
     * @return the item handler
     */
    public IItemHandler getAttachedInventory(BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        if (te == null)
            return null;
        if (te instanceof InventoryNodeTile) {
            InventoryNodeTile sourceTE = (InventoryNodeTile) te;
            IItemHandler sourceitemHandler = sourceTE.getHandler().orElse(EMPTY); //Get the inventory handler of the block the inventory node is facing
            if (sourceitemHandler.getSlots() == 0) return null; //If its empty, return null
            return sourceitemHandler;
        }
        if (te instanceof CraftingStationTile) {
            CraftingStationTile sourceTE = (CraftingStationTile) te;
            IItemHandler sourceitemHandler = sourceTE.getInventoryStacks(); //Get the inventory handler of the crafter
            if (sourceitemHandler.getSlots() == 0) return null; //If its empty, return null
            return sourceitemHandler;
        }
        return null;
    }

    /**
     * Get all the filters at @param pos that are extractor filters
     *
     * @return List of itemstacks which represent all the extractor filter cards
     */
    public ArrayList<ItemStack> getExtractFilters(BlockPos pos) {
        if (filterCardCache.containsKey(pos)) {
            ArrayList<ItemStack> tempList = new ArrayList<>(filterCardCache.get(pos));
            tempList.removeIf(s -> !(s.getItem() instanceof CardExtractor));
            return tempList;
        }
        return new ArrayList<>();
    }

    /**
     * Get all the filters at @param pos that are inserter filters
     *
     * @return List of itemstacks which represent all the inserter filter cards
     */
    public ArrayList<ItemStack> getInsertFilters(BlockPos pos) {
        if (filterCardCache.containsKey(pos)) {
            ArrayList<ItemStack> tempList = new ArrayList<>(filterCardCache.get(pos));
            tempList.removeIf(s -> !(s.getItem() instanceof CardInserter));
            return tempList;
        }
        return new ArrayList<>();
    }

    /**
     * Get all the filters at @param pos that are provider filters
     *
     * @return List of itemstacks which represent all the provider filter cards
     */
    public ArrayList<ItemStack> getProviderFilters(BlockPos pos) {
        if (filterCardCache.containsKey(pos)) {
            ArrayList<ItemStack> tempList = new ArrayList<>(filterCardCache.get(pos));
            tempList.removeIf(s -> !(s.getItem() instanceof CardProvider));
            return tempList;
        }
        return new ArrayList<>();
    }

    /**
     * Get all the filters at @param pos that are stocker filters
     *
     * @return List of itemstacks which represent all the stocker filter cards
     */
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
     *
     * @return a list of possible destinations
     */
    public ArrayList<BlockPos> findDestinationForItemstack(ItemStack itemStack) {
        ItemStackKey key = new ItemStackKey(itemStack);
        if (inserterCache.containsKey(key)) {
            return inserterCache.get(key);
        }
        System.out.println("Building Inserter Cache for: " + itemStack);
        ArrayList<BlockPos> tempArray = new ArrayList<>();
        for (int priority : insertPriorities.keySet()) {
            for (BlockPos toPos : insertPriorities.get(priority)) { //If we found an item to transfer, start looping through the inserters
                for (ItemStack insertCard : getInsertFilters(toPos)) { //Loop through all the cached insertCards
                    if (isStackValidForCard(insertCard, itemStack))
                        tempArray.add(toPos);
                }
            }
        }
        inserterCache.put(key, tempArray);
        return inserterCache.get(key);
    }

    /**
     * Given an @param itemStack, find a valid provider either from an existing cache, or looping through all known providers.
     * Excludes @param fromPos to ensure items are not extracted from the stocking chest
     *
     * @return a list of possible destinations
     */
    public ArrayList<BlockPos> findProviderForItemstack(ItemStack itemStack) {
        ItemStackKey key = new ItemStackKey(itemStack);
        if (providerCache.containsKey(key)) {
            return providerCache.get(key);
        }
        System.out.println("Building Provider Cache for: " + itemStack);
        ArrayList<BlockPos> tempArray = new ArrayList<>();
        for (BlockPos toPos : providerNodes) { //Loop through all provider nodes
            for (ItemStack providerCard : getProviderFilters(toPos)) { //Loop through all the cached providerCards
                if (isStackValidForCard(providerCard, itemStack))
                    tempArray.add(toPos);
            }
        }
        providerCache.put(key, tempArray);
        return providerCache.get(key);
    }

    /**
     * Given an @param pos, find a list of all items to keep stocked in this inventory, including multiple cards, and cache it.
     *
     * @return a list of ItemStacks this invNode wants to keep stocked
     */
    public ArrayList<ItemStack> findStockersForPos(BlockPos pos) {
        if (stockerCache.containsKey(pos))
            return stockerCache.get(pos);
        System.out.println("Building Stocker Cache for: " + pos);
        ArrayList<ItemStack> tempArray = new ArrayList<>();
        for (ItemStack stockerCard : getStockerFilters(pos)) { //Loop through all the cached stockerCards
            tempArray.addAll(BaseCard.getFilteredItems(stockerCard)); //Get all the items we should be requesting
        }
        stockerCache.put(pos, tempArray);
        return stockerCache.get(pos);
    }

    /**
     * Attempts to insert @param stack into @param destitemHandler at @param toPos
     * Takes into account items currently traveling through the network, and whether they will also fit in the destination
     *
     * @return how many items fit
     */
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
     * Attempts to extract @param stack from @param fromPos in slot @param slot
     * Loops through all possible destinations (Inserters that accept this item) attempting to insert the whole stack, even if it needs to be split up to do so
     *
     * @return the amount of items that remain in the stack post extraction
     */
    public int extractItemFromPos(ItemStack stack, BlockPos fromPos, int slot) {
        if (stack.isEmpty()) return stack.getCount(); //No empty stacks!

        IItemHandler sourceitemHandler = getAttachedInventory(fromPos);
        ArrayList<BlockPos> possibleDestinations = new ArrayList<>(findDestinationForItemstack(stack)); //Find a list of possible destinations
        possibleDestinations.remove(fromPos); //Remove the block its coming from, no self-sending!
        int stackSize = stack.getCount(); //The number of items we are extracting

        if (possibleDestinations.isEmpty())
            return stackSize; //If we can't send this item anywhere, stop processing

        for (BlockPos toPos : possibleDestinations) { //Loop through all possible destinations
            IItemHandler destitemHandler = getAttachedInventory(toPos); //Get the inventory handler of the block the inventory node is facing
            if (destitemHandler == null) continue; //If its empty, move onto the next inserter

            int count = testInsertToInventory(destitemHandler, toPos, stack); //Find out how many items can fit in the destination inventory, including inflight items
            if (count == 0) continue; //If none, try elsewhere!

            ItemStack extractedStack;
            if (slot == -1) //Slot -1 indicates this method was called by the controller's internal inventory.
                extractedStack = storedItems.removeStack(stack, count);
            else
                extractedStack = sourceitemHandler.extractItem(slot, count, false); //Actually remove the items this time

            boolean successfullySent = transferItemStack(fromPos, toPos, extractedStack);
            if (!successfullySent) { //Attempt to send items
                ItemHandlerHelper.insertItem(sourceitemHandler, extractedStack, false); //If failed for some reason, put back in inventory
            } else {
                stackSize -= extractedStack.getCount();
            }
            if (stackSize == 0)
                break; //If we successfully sent all items in this stack, we're done
        }
        return stackSize;
    }

    /**
     * Given a @param stack, find providers that offer it and send to @param toPos
     * Retrieve from multiple providers if necessary
     *
     * @return the stack of items we couldn't source
     */
    public ItemStack provideItemStacksToPos(ItemStack stack, BlockPos toPos) {
        boolean successfullySent = false;
        ArrayList<BlockPos> possibleProviders = new ArrayList<>(findProviderForItemstack(stack)); //Find a list of possible Providers
        possibleProviders.remove(toPos); //Remove this chest
        if (possibleProviders.isEmpty()) return stack; //If nothing can provide to here, stop working
        int desiredAmt = stack.getCount();
        for (BlockPos providerPos : possibleProviders) { //Loop through all possible Providers
            IItemHandler providerItemHandler = getAttachedInventory(providerPos); //Get the inventory handler of the block the inventory node is facing
            if (providerItemHandler == null) continue; //If its empty, move onto the next provider

            do {
                ItemStack simulated = ItemHandlerUtil.extractItem(providerItemHandler, stack, true); //Pretend to extract the stack from the provider's inventory

                if (simulated.getCount() == 0) {
                    break; //If the stack we removed has zero items in it check another provider
                }
                int extractCount = simulated.getCount(); //How many items were successfully removed from the inventory
                stack.setCount(extractCount);
                ItemStack extractedStack = ItemHandlerUtil.extractItem(providerItemHandler, stack, false); //Actually remove the items this time
                successfullySent = transferItemStack(providerPos, toPos, extractedStack);
                if (!successfullySent) { //Attempt to send items
                    ItemHandlerHelper.insertItem(providerItemHandler, extractedStack, false); //If failed for some reason, put back in inventory
                    break;
                } else {
                    desiredAmt -= extractedStack.getCount();
                }
            } while (desiredAmt > 0 || !successfullySent);
            stack.setCount(desiredAmt);
            if (stack.getCount() == 0) break;
        }
        return stack;
    }

    /**
     * Increment the current processing slot for the @param fromPos inventory node, up to @param max
     */
    public void incrementInvNodeSlot(BlockPos fromPos, int max) {
        int currentSlot = invNodeSlot.getOrDefault(fromPos, 0);
        if (currentSlot + 1 >= max)
            invNodeSlot.put(fromPos, 0);
        else
            invNodeSlot.put(fromPos, currentSlot + 1);
    }

    /**
     * Increment the current processing slot for the @param fromPos stocker node, up to @param max
     */
    public void incrementStockerSlot(BlockPos fromPos, int max) {
        int currentSlot = stockerSlot.getOrDefault(fromPos, 0);
        if (currentSlot + 1 >= max)
            stockerSlot.put(fromPos, 0);
        else
            stockerSlot.put(fromPos, currentSlot + 1);
    }

    public boolean canExtractItemFromPos(ItemStack itemStack, BlockPos fromPos) {
        ItemStackKey key = new ItemStackKey(itemStack);
        if (extractorCache.containsKey(key)) {
            return extractorCache.get(key).contains(fromPos);
        }
        System.out.println("Building Extractor Cache for: " + itemStack);
        ArrayList<BlockPos> tempArray = new ArrayList<>();
        for (BlockPos toPos : extractorNodes) { //Loop through all extractor nodes
            for (ItemStack extractorCard : getExtractFilters(toPos)) { //Loop through all the cached extractorCards
                if (isStackValidForCard(extractorCard, itemStack)) {
                    extractorAmounts.put(toPos, key, BaseCard.getExtractAmt(extractorCard));
                    tempArray.add(toPos);
                }
            }
        }
        extractorCache.put(key, tempArray);
        return extractorCache.get(key).contains(fromPos);
    }

    /**
     * Attempts to extract an item from the current processing slot in the @param fromPos inventory
     * If successful - do not increment current processing slot, if not ++
     *
     * @return if an item was extracted.
     */
    public boolean attemptExtract(BlockPos fromPos) {
        IItemHandler sourceitemHandler = getAttachedInventory(fromPos); //Get the inventory handler of the block the inventory node is facing
        if (sourceitemHandler == null) return false; //If its empty, return false

        int slot = invNodeSlot.getOrDefault(fromPos, 0);
        ItemStack stackInSlot = sourceitemHandler.getStackInSlot(slot);
        if (!stackInSlot.isEmpty()) {
            if (canExtractItemFromPos(stackInSlot, fromPos)) {
                int extractAmt = Math.min(extractorAmounts.get(fromPos, new ItemStackKey(stackInSlot)), stackInSlot.getCount());
                ItemStack stack = sourceitemHandler.extractItem(slot, extractAmt, true); //Pretend to remove the x items from the stack we found
                //System.out.println("Extracting " + stack.getCount() + " " + stack.getItem() + "from " + fromPos);
                if (!stack.isEmpty())
                    if (extractItemFromPos(stack, fromPos, slot) < extractAmt) //if we extracted SOMETHING
                        return true;
            }
        }
        incrementInvNodeSlot(fromPos, sourceitemHandler.getSlots());
        return false;
    }

    public boolean attemptStock(BlockPos stockerPos) {
        IItemHandler stockerItemHandler = getAttachedInventory(stockerPos); //Get the inventory handler of the block the stocker's inventory node is facing
        if (stockerItemHandler == null) return false; //If its empty, move onto the next stocker

        ArrayList<ItemStack> tempArray = findStockersForPos(stockerPos); //Get the list of items to keep in stock for this node
        if (tempArray.isEmpty()) return false; //If nothing to stock, move onto the next one!

        ItemHandlerUtil.InventoryCounts invCache = new ItemHandlerUtil.InventoryCounts(stockerItemHandler); //Get a count of all itemstacks in this inventory

        int slot = stockerSlot.getOrDefault(stockerPos, 0); //Which item in the list of stockerItems we are working on this time around
        ItemStack item = tempArray.get(slot); //The item stack itself

        //First check if the stocker is satisfied, including stacks 'in flight'
        int countOfItem = invCache.getCount(item); //How many items are currently in the inventory
        int desiredAmt = item.getCount(); //How many we want
        if (countOfItem >= desiredAmt) { //Compare what we want to the itemstack cache, if we have enough go to next item
            incrementStockerSlot(stockerPos, findStockersForPos(stockerPos).size());
            return false;
        }

        countOfItem += countItemsInFlight(item, stockerPos); //Count the items in flight to this destination
        if (countOfItem >= desiredAmt) { //We're done checking for this item if we found enough of the item including items in flight, move onto next item
            incrementStockerSlot(stockerPos, findStockersForPos(stockerPos).size());
            return false;
        }

        //Doing this rather than copying.
        int extractAmt = item.getCount() - countOfItem;
        int stockerCount = item.getCount();
        //ItemStack stack = item.copy();
        item.setCount(extractAmt);

        //Before we even look for the item to insert, lets see if it'll fit here first!
        int count = testInsertToInventory(stockerItemHandler, stockerPos, item);
        if (count == 0) { //If we can't fit any items in here, nope out!
            incrementStockerSlot(stockerPos, findStockersForPos(stockerPos).size());
            item.setCount(stockerCount);
            return false;
        }
        if (count < item.getCount())
            item.setCount(count); //If we can only fit 8 items, but were trying to get 16, adjust to 8

        if (!(provideItemStacksToPos(item, stockerPos).getCount() == 0)) {//If we couldn't find any items
            incrementStockerSlot(stockerPos, findStockersForPos(stockerPos).size());
            item.setCount(stockerCount);
            return false;
        }
        item.setCount(stockerCount);
        return true; //Don't increment the stockerSlot - this keeps stocking the same item until its satisfied, then moves onto the next
    }

    public void handleInternalInventory() {
        ArrayList<ItemStack> stored = new ArrayList(storedItems.getItemCounts().values());
        boolean success = false;
        for (ItemStack stack : stored) {
            int amt = stack.getCount();
            int remaining = extractItemFromPos(stack, this.pos, -1);
            if (remaining < amt)
                success = true;
        }
        if (success)
            markDirtyClient();
    }

    /**
     * Go through each of the extractorNodes and extract a single item based on the extractorCards they have. Send to an appropriate inserter.
     */
    public void handleExtractors() {
        if (inserterNodes.size() == 0) return; //If theres nowhere to put items, nope out!
        for (BlockPos fromPos : extractorNodes) { //Loop through all the extractors!
            attemptExtract(fromPos);
        }
    }

    /**
     * Go through each of the stockerNodes and find a provider offering the item - transfer it to this inventory if found.
     */
    public void handleStockers() {
        if (providerNodes.size() == 0) return; //If theres nowhere to get items from, nope out!
        for (BlockPos stockerPos : stockerNodes) { //Loop through all the stocker cards!
            attemptStock(stockerPos);
        }
    }

    /**
     * If a @param stack attempts to insert into an already full inventory, we need to re-route it. From the position it failed to insert to (@param lostAt) try to send it to another
     * Valid inserter
     *
     * @return the remains of the itemstack that could not be inserted anywhere else in the network
     */
    public ItemStack handleLostStack(ItemStack stack, BlockPos lostAt) {
        System.out.println("Stack Lost");
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
            }
        }
        if (stack.getCount() > 0) {
            ItemStack extractedStack = stack.split(stack.getCount());
            if (!transferItemStack(lostAt, this.pos, extractedStack)) { //If we failed to send the stack to the remote destination, store directly in controller.
                insertIntoController(extractedStack);
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
        ArrayList<BlockPos> route;
        //if (!(te instanceof InventoryNodeTile) && !(te instanceof CraftingStationTile)) return false;
        if (te instanceof InventoryNodeTile)
            route = ((InventoryNodeTile) te).getRouteTo(toPos);
        else if (te instanceof CraftingStationTile)
            route = ((CraftingStationTile) te).getRouteTo(toPos);
        else if (te instanceof ControllerTile)
            route = getRouteTo(toPos);
        else
            return false;
        if (route == null || route.size() <= 1) {
            return false;
        }

        long tempGameTime = world.getGameTime() + 1;
        ControllerTask task;
        ControllerTask parentTask = new ControllerTask(fromPos, toPos, ControllerTask.TaskType.INSERT, itemStack, null, tempGameTime); //Create a parent task, this isn't executed, but is used to track items in flight
        UUID parentGuid = parentTask.guid;
        ArrayList<ControllerTask> taskArrayList = new ArrayList<>();
        for (int r = 0; r < route.size(); r++) {
            if (r == route.size() - 1) { //This is the last step of the route, so insert into the attached inventory
                task = new ControllerTask(route.get(r - 1), route.get(r), ControllerTask.TaskType.INSERT, itemStack, parentGuid, tempGameTime);
                taskList.add(task);
                taskArrayList.add(task);
            } else { //This is not the last step of the route, so schedule particle spawning
                BlockPos from = route.get(r);
                BlockPos to = route.get(r + 1);
                task = new ControllerTask(from, to, ControllerTask.TaskType.PARTICLE, itemStack, parentGuid, tempGameTime);
                taskList.add(task);
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

    public boolean canExecuteTask(ControllerTask task) {
        if (task.isCancelled) return false;
        if (!world.isAreaLoaded(task.fromPos, 3) || !(world.isAreaLoaded(task.toPos, 3)) || !(world.isAreaLoaded(this.pos, 3))) {
            return false;
        }
        if (task.scheduledTime > world.getGameTime()) return false;
        return true;
    }

    public void removeTasksFromList() {
        taskList.removeIf(o -> o.isCancelled || o.isComplete);
        //Cleanup parent task list
        for (ArrayList<ControllerTask> childTasks : parentTaskMap.values()) {
            childTasks.removeIf(o -> o.isCancelled || o.isComplete);
        }
        parentTaskMap.entrySet().removeIf(entries -> entries.getValue().size() == 0 || entries == null);
    }

    /**
     * Handle all scheduled tasks due at (or before) the current gametime
     */
    public void handleTasks() {
        Set<ControllerTask> temptaskList = new HashSet<>(taskList);
        for (ControllerTask task : temptaskList) {
            if (canExecuteTask(task)) {
                executeTask(task);
                task.complete();
            }
        }
        removeTasksFromList();
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

    public int countItemsInFlight(ItemStack itemStack, BlockPos toPos) {
        int count = 0;
        for (ControllerTask parentTask : parentTaskMap.keySet()) {
            if (parentTask.itemStack.isItemEqual(itemStack) && parentTask.toPos.equals(toPos)) {
                count += parentTask.itemStack.getCount();
            }
        }
        return count;
    }

    public Set<ItemStack> getItemStacksInFlight(BlockPos toPos) {
        Set<ItemStack> flightStacks = new HashSet<>();
        for (ControllerTask parentTask : parentTaskMap.keySet()) {
            if (parentTask.toPos.equals(toPos)) {
                flightStacks.add(parentTask.itemStack);
            }
        }
        return flightStacks;
    }

    /**
     * Given a @param task, execute whatever that task is. See the ControllerTask class.
     */
    public void executeTask(ControllerTask task) {
        if (task.isParticle()) {
            ItemStack remainingStack = doParticles(task);
            if (!remainingStack.isEmpty()) {
                cancelTask(task.parentGUID);
                handleLostStack(remainingStack, task.toPos);
                //Block.spawnAsEntity(world, task.fromPos, remainingStack); //TODO Implement storing in the controller
            }
        } else if (task.isInsert()) {
            ItemStack remainingStack = doInsert(task);
            if (!remainingStack.isEmpty()) {
                handleLostStack(remainingStack, task.toPos);
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

    public boolean isStackValidForDestination(ItemStack stack, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof CraftingStationTile)
            return true;
        if (te instanceof ControllerTile)
            return true;
        if (te instanceof InventoryNodeTile) {
            for (ItemStack card : getInsertFilters(pos)) {
                if (isStackValidForCard(card, stack))
                    return true;
            }
            for (ItemStack card : getStockerFilters(pos)) {
                if (isStackValidForCard(card, stack))
                    return true;
            }
        }
        return false;
    }

    public void insertIntoController(ItemStack stack) {
        storedItems.setCount(stack);
        markDirtyClient();
    }

    /**
     * Called by ExecuteTask - attempt to insert an item into the destination inventory.
     *
     * @return the remains of the itemstack (Anything that failed to insert)
     */
    public ItemStack doInsert(ControllerTask task) {
        if (!isStackValidForDestination(task.itemStack, task.toPos)) return task.itemStack;
        if (world.getTileEntity(task.toPos) instanceof ControllerTile) {
            insertIntoController(task.itemStack);
            return ItemStack.EMPTY;
        }
        IItemHandler destitemHandler = getAttachedInventory(task.toPos);
        if (destitemHandler == null) return task.itemStack;

        ItemStack stack = task.itemStack;
        ItemStack postInsertStack = ItemHandlerHelper.insertItem(destitemHandler, stack, false);
        return postInsertStack;
    }

    /**
     * Cancel the parent and all child tasks with @param parentGUID
     */
    public void cancelTask(UUID parentGUID) {
        for (ControllerTask task : taskList) {
            if (task.parentGUID == parentGUID) {
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
            //System.out.println("I'm here!");
            energyStorage.receiveEnergy(1000, false); //Testing
            if (inventoryNodes.size() > 0 && (extractorNodes.isEmpty() && inserterNodes.isEmpty() && providerNodes.isEmpty() && stockerNodes.isEmpty())) //Todo cleaner
                refreshAllInvNodes();
            handleInternalInventory();
            handleExtractors();
            if (world.getGameTime() % 20 == 0)
                handleStockers(); //Stocking is somewhat expensive operation, so only do it once a second, rather than once a tick.
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

        crafterNodes.clear();
        ListNBT craftnodes = tag.getList("craftnodes", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < craftnodes.size(); i++) {
            BlockPos blockPos = NBTUtil.readBlockPos(craftnodes.getCompound(i).getCompound("pos"));
            crafterNodes.add(blockPos);
        }
        //refreshAllInvNodes();
        //System.out.println("Reading");

        parentTaskMap.clear();
        ListNBT parentTasks = tag.getList("parentTasks", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < parentTasks.size(); i++) {
            ControllerTask task = new ControllerTask(parentTasks.getCompound(i));
            parentTaskMap.put(task, new ArrayList<>());
        }

        taskList.clear();
        ListNBT tasks = tag.getList("tasks", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < tasks.size(); i++) {
            ControllerTask task = new ControllerTask(tasks.getCompound(i));
            taskList.add(task);
            for (ControllerTask parentTask : parentTaskMap.keySet()) {
                if (task.parentGUID.equals(parentTask.guid)) {
                    parentTaskMap.get(parentTask).add(task);
                    break;
                }

            }
        }

        storedItems = new ItemHandlerUtil.InventoryCounts(tag.getList("storedItems", Constants.NBT.TAG_COMPOUND));

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

        ListNBT craftnodes = new ListNBT();
        for (BlockPos blockPos : crafterNodes) {
            CompoundNBT comp = new CompoundNBT();
            comp.put("pos", NBTUtil.writeBlockPos(blockPos));
            craftnodes.add(comp);
        }
        tag.put("craftnodes", craftnodes);

        ListNBT parentTasks = new ListNBT();
        for (ControllerTask task : parentTaskMap.keySet()) {
            CompoundNBT nbt = task.serialize();
            parentTasks.add(nbt);
        }
        tag.put("parentTasks", parentTasks);

        ListNBT tasks = new ListNBT();
        for (ControllerTask task : taskList) {
            CompoundNBT nbt = task.serialize();
            tasks.add(nbt);
        }
        tag.put("tasks", tasks);

        ListNBT storedItem = storedItems.serialize();
        tag.put("storedItems", storedItem);

        //System.out.println("Writing");
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
