package com.direwolf20.logisticslasers.common.util;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

public class ControllerTask {
    public UUID guid;
    public BlockPos fromPos;
    public BlockPos toPos;
    public TaskType taskType;
    public ItemStack itemStack;

    public enum TaskType {
        PARTICLE,
        EXTRACT,
        INSERT
    }

    public ControllerTask(BlockPos from, BlockPos to, TaskType type, ItemStack stack) {
        this.guid = UUID.randomUUID();
        this.fromPos = from;
        this.toPos = to;
        this.taskType = type;
        this.itemStack = stack;
    }

    public boolean isParticle() {
        return taskType == TaskType.PARTICLE;
    }

    public boolean isExtract() {
        return taskType == TaskType.EXTRACT;
    }

    public boolean isInsert() {
        return taskType == TaskType.INSERT;
    }

    //Todo NBT Serializer
}
