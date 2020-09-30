package com.direwolf20.logisticslasers.common.blocks;

import com.direwolf20.logisticslasers.LogisticsLasers;
import com.direwolf20.logisticslasers.common.container.BasicFilterContainer;
import com.direwolf20.logisticslasers.common.container.ControllerContainer;
import com.direwolf20.logisticslasers.common.container.InventoryNodeContainer;
import com.direwolf20.logisticslasers.common.tiles.BasicNodeTile;
import com.direwolf20.logisticslasers.common.tiles.ControllerTile;
import com.direwolf20.logisticslasers.common.tiles.InventoryNodeTile;
import net.minecraft.block.Block;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModBlocks {
    /**
     * Deferred Registers for the our Main class to load.
     */
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, LogisticsLasers.MOD_ID);
    public static final DeferredRegister<TileEntityType<?>> TILES_ENTITIES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, LogisticsLasers.MOD_ID);
    public static final DeferredRegister<ContainerType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.CONTAINERS, LogisticsLasers.MOD_ID);

    /**
     * Register our blocks to the above registers to be loaded when the mod is initialized
     */
    public static final RegistryObject<Block> CONTROLLER = BLOCKS.register("controller", Controller::new);
    public static final RegistryObject<Block> BASIC_NODE = BLOCKS.register("basicnode", BasicNode::new);
    public static final RegistryObject<Block> INVENTORY_NODE = BLOCKS.register("inventorynode", InventoryNode::new);

    /**
     * TileEntity Registers to the above deferred registers to be loaded in from the mods main class.
     */
    public static final RegistryObject<TileEntityType<ControllerTile>> CONTROLLER_TILE =
            TILES_ENTITIES.register("controller", () -> TileEntityType.Builder.create(ControllerTile::new, ModBlocks.CONTROLLER.get()).build(null));
    public static final RegistryObject<TileEntityType<BasicNodeTile>> BASIC_NODE_TILE =
            TILES_ENTITIES.register("basicnode", () -> TileEntityType.Builder.create(BasicNodeTile::new, ModBlocks.BASIC_NODE.get()).build(null));
    public static final RegistryObject<TileEntityType<InventoryNodeTile>> INVENTORY_NODE_TILE =
            TILES_ENTITIES.register("inventorynode", () -> TileEntityType.Builder.create(InventoryNodeTile::new, ModBlocks.INVENTORY_NODE.get()).build(null));

    /**
     * Containers
     */
    public static final RegistryObject<ContainerType<ControllerContainer>> CONTROLLER_CONTAINER = CONTAINERS.register("controller_container", () -> IForgeContainerType.create(ControllerContainer::new));
    public static final RegistryObject<ContainerType<InventoryNodeContainer>> INVENTORY_NODE_CONTAINER = CONTAINERS.register("inventory_node_container", () -> IForgeContainerType.create(InventoryNodeContainer::new));
    public static final RegistryObject<ContainerType<BasicFilterContainer>> BASIC_FILTER_CONTAINER = CONTAINERS.register("basic_filter_container", () -> IForgeContainerType.create(BasicFilterContainer::new));

}
