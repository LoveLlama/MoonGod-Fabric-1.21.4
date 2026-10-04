package me.moon.event.events

import me.moon.event.EventStage
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction

class PlayerDamageBlockEvent(stage: Int, @JvmField var pos: BlockPos, @JvmField var facing: Direction) : EventStage(stage)
