package com.direwolf20.logisticslasers.common.items;

import com.direwolf20.logisticslasers.LogisticsLasers;
import com.direwolf20.logisticslasers.common.blocks.ModBlocks;
import com.direwolf20.logisticslasers.common.items.logiccards.*;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModItems {
    // The item group is the creative tab it will go into.
    public static final Item.Properties ITEM_GROUP = new Item.Properties().group(LogisticsLasers.itemGroup);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, LogisticsLasers.MOD_ID);
    public static final DeferredRegister<Item> BASICITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, LogisticsLasers.MOD_ID);

    // Block items
    public static final RegistryObject<Item> CONTROLLER_ITEM = ITEMS.register("controller", () -> new BlockItem(ModBlocks.CONTROLLER.get(), ITEM_GROUP));
    public static final RegistryObject<Item> BASIC_NODE_ITEM = ITEMS.register("basicnode", () -> new BlockItem(ModBlocks.BASIC_NODE.get(), ITEM_GROUP));
    public static final RegistryObject<Item> INVENTORY_NODE_ITEM = ITEMS.register("inventorynode", () -> new BlockItem(ModBlocks.INVENTORY_NODE.get(), ITEM_GROUP));
    public static final RegistryObject<Item> CRAFTING_STATION_ITEM = ITEMS.register("crafting_station", () -> new BlockItem(ModBlocks.CRAFTING_STATION.get(), ITEM_GROUP));

    // Items
    public static final RegistryObject<Item> WRENCH = BASICITEMS.register("wrench", Wrench::new);
    public static final RegistryObject<Item> CARD_EXTRACTOR = BASICITEMS.register("card_extractor", CardExtractor::new);
    public static final RegistryObject<Item> CARD_INSERTER = BASICITEMS.register("card_inserter", CardInserter::new);
    public static final RegistryObject<Item> CARD_INSERTER_MOD = BASICITEMS.register("card_inserter_mod", CardInserterMod::new);
    public static final RegistryObject<Item> CARD_INSERTER_TAG = BASICITEMS.register("card_inserter_tag", CardInserterTag::new);
    public static final RegistryObject<Item> CARD_POLYMORPH = BASICITEMS.register("card_polymorph", CardPolymorph::new);
    public static final RegistryObject<Item> CARD_PROVIDER = BASICITEMS.register("card_provider", CardProvider::new);
    public static final RegistryObject<Item> CARD_STOCKER = BASICITEMS.register("card_stocker", CardStocker::new);
}
