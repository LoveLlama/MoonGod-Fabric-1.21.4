package me.moon.event.events

import me.moon.event.EventStage

class KeyEvent(@JvmField val key: Int) : EventStage() {
    fun getKey(): Int = key
}
