package me.moon.event.events

import me.moon.event.EventStage
import net.minecraft.client.MinecraftClient

class Render2DEvent : EventStage {
    @JvmField var partialTicks: Float
    private var screenWidth: Double
    private var screenHeight: Double

    constructor(partialTicks: Float, screenWidth: Double, screenHeight: Double) {
        this.partialTicks = partialTicks
        this.screenWidth = screenWidth
        this.screenHeight = screenHeight
    }

    constructor(partialTicks: Float) {
        this.partialTicks = partialTicks
        val window = MinecraftClient.getInstance().window
        this.screenWidth = window?.scaledWidth?.toDouble() ?: 0.0
        this.screenHeight = window?.scaledHeight?.toDouble() ?: 0.0
    }

    fun setPartialTicks(partialTicks: Float) {
        this.partialTicks = partialTicks
    }

    fun setScreenWidth(screenWidth: Double) {
        this.screenWidth = screenWidth
    }

    fun setScreenHeight(screenHeight: Double) {
        this.screenHeight = screenHeight
    }

    fun getScreenWidth(): Double = screenWidth

    fun getScreenHeight(): Double = screenHeight
}
