package me.moon.util

import net.minecraft.client.MinecraftClient

interface Util {
    val mc: MinecraftClient
        get() = MinecraftClient.getInstance()
}
