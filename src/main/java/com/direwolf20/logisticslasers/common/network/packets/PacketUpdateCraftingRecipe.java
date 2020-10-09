package com.direwolf20.logisticslasers.common.network.packets;

import com.direwolf20.logisticslasers.common.tiles.CraftingStationTile;
import net.minecraft.client.Minecraft;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketUpdateCraftingRecipe {
    private BlockPos pos;
    private ResourceLocation recipe;

    public PacketUpdateCraftingRecipe(BlockPos pos, ICraftingRecipe recipe) {
        this.pos = pos;
        this.recipe = recipe.getId();
    }

    public PacketUpdateCraftingRecipe(BlockPos pos, ResourceLocation recipe) {
        this.pos = pos;
        this.recipe = recipe;
    }

    public static void encode(PacketUpdateCraftingRecipe msg, PacketBuffer buffer) {
        buffer.writeBlockPos(msg.pos);
        buffer.writeResourceLocation(msg.recipe);
    }

    public static PacketUpdateCraftingRecipe decode(PacketBuffer buffer) {
        BlockPos pos = buffer.readBlockPos();
        ResourceLocation recipe = buffer.readResourceLocation();
        return new PacketUpdateCraftingRecipe(pos, recipe);
    }

    public static class Handler {
        public static void handle(PacketUpdateCraftingRecipe msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> clientPacketHandler(msg)));
            ctx.get().setPacketHandled(true);
        }
    }

    public static void clientPacketHandler(PacketUpdateCraftingRecipe msg) {
        World world = Minecraft.getInstance().world;
        if (world != null) {
            CraftingStationTile te = (CraftingStationTile) world.getTileEntity(msg.pos);
            RecipeManager manager = world.getRecipeManager();
            ICraftingRecipe recipe = manager.getRecipe(msg.recipe).filter(ICraftingRecipe.class::isInstance).map(ICraftingRecipe.class::cast).get();
            te.updateRecipe(recipe);

        }
    }
}
