package me.moon.event.events

import me.moon.event.EventStage
import net.minecraft.entity.player.PlayerEntity

class DeathEvent(@JvmField var player: PlayerEntity) : EventStage()
