package com.direwolf20.logisticslasers.client.events;

import com.direwolf20.logisticslasers.client.renders.LaserConnections;
import com.direwolf20.logisticslasers.common.items.Wrench;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ClientEvents {
    @SubscribeEvent
    static void renderWorldLastEvent(RenderWorldLastEvent evt) {
        PlayerEntity myplayer = Minecraft.getInstance().player;

        ItemStack heldItem = getWrench(myplayer);
        if (heldItem.getItem() instanceof Wrench) {
            LaserConnections.renderLasers(evt);
            BlockPos selectedPos = Wrench.getConnectionPos(heldItem);
            if (!selectedPos.equals(BlockPos.ZERO))
                LaserConnections.renderSelectedBlock(evt, selectedPos);
        }

    }

    public static ItemStack getWrench(PlayerEntity player) {
        ItemStack heldItem = player.getHeldItemMainhand();
        if (!(heldItem.getItem() instanceof Wrench)) {
            heldItem = player.getHeldItemOffhand();
            if (!(heldItem.getItem() instanceof Wrench)) {
                return ItemStack.EMPTY;
            }
        }
        return heldItem;
    }
}
