package me.moon.event.events

import me.moon.event.EventStage

class Render3DEvent(@JvmField val partialTicks: Float) : EventStage() {
    fun getPartialTicks(): Float = partialTicks
}
