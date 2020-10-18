package com.direwolf20.logisticslasers.common.util;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.UUID;

public class ControllerTask {
    public UUID guid;
    public BlockPos fromPos;
    public BlockPos toPos;
    public TaskType taskType;
    public ItemStack itemStack;
    public UUID parentGUID;
    public boolean isCancelled;
    public boolean isComplete;
    public long scheduledTime;

    public enum TaskType {
        PARTICLE,
        EXTRACT,
        INSERT
    }

    public ControllerTask(BlockPos from, BlockPos to, TaskType type, ItemStack stack, @Nullable UUID parentGUID, long gameTime) {
        this.guid = UUID.randomUUID();
        this.fromPos = from;
        this.toPos = to;
        this.taskType = type;
        this.itemStack = stack;
        this.parentGUID = parentGUID;
        this.isComplete = false;
        this.scheduledTime = gameTime;
    }

    public ControllerTask(CompoundNBT nbt) {
        this.guid = nbt.getUniqueId("guid");
        this.fromPos = NBTUtil.readBlockPos(nbt.getCompound("fromPos"));
        this.toPos = NBTUtil.readBlockPos(nbt.getCompound("toPos"));
        this.taskType = TaskType.values()[nbt.getInt("taskType")];
        this.itemStack = ItemStack.read(nbt.getCompound("itemStack"));
        if (nbt.contains("parentGUID"))
            this.parentGUID = nbt.getUniqueId("parentGUID");
        this.isCancelled = nbt.getBoolean("isCancelled");
        this.isComplete = nbt.getBoolean("isComplete");
        this.scheduledTime = nbt.getLong("scheduledTime");
    }

    public void complete() {
        this.isComplete = true;
    }

    public void cancel() {
        this.isCancelled = true;
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

    public CompoundNBT serialize() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putUniqueId("guid", guid);
        nbt.put("fromPos", NBTUtil.writeBlockPos(fromPos));
        nbt.put("toPos", NBTUtil.writeBlockPos(toPos));
        nbt.putInt("taskType", taskType.ordinal());
        nbt.put("itemStack", itemStack.serializeNBT());
        if (parentGUID != null) //parentTasks have null parentGUIDs
            nbt.putUniqueId("parentGUID", parentGUID);
        nbt.putBoolean("isCancelled", isCancelled);
        nbt.putBoolean("isComplete", isComplete);
        nbt.putLong("scheduledTime", scheduledTime);
        return nbt;
    }

}
