package me.moon.event.events

import me.moon.event.EventStage
import net.minecraft.network.packet.Packet

open class PacketEvent(private val packet: Packet<*>?) : EventStage() {
    @Suppress("UNCHECKED_CAST")
    fun <T : Packet<*>> getPacket(): T = packet as T

    class Receive(packet: Packet<*>?) : PacketEvent(packet)

    class Send(packet: Packet<*>?) : PacketEvent(packet)
}
