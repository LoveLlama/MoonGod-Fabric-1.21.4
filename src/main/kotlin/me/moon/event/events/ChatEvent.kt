package me.moon.event.events

import me.moon.event.EventStage

class ChatEvent(@JvmField val msg: String) : EventStage() {
    fun getMsg(): String = msg
}
