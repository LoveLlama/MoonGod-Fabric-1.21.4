package me.moon.event.events

import me.moon.event.EventStage
import net.minecraft.util.Hand
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos

class ProcessRightClickBlockEvent(@JvmField var pos: BlockPos, @JvmField var hand: Hand, @JvmField var stack: ItemStack) : EventStage()
