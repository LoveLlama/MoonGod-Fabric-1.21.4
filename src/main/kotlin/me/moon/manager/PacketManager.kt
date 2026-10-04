package me.moon.manager

import me.moon.features.Feature
import net.minecraft.network.packet.Packet

class PacketManager : Feature() {
    private val noEventPackets: MutableList<Packet<*>> = ArrayList()

    fun sendPacketNoEvent(packet: Packet<*>?) {
        if (packet != null && !nullCheck()) {
            val networkHandler = mc.player?.networkHandler ?: return
            noEventPackets.add(packet)
            networkHandler.sendPacket(packet)
        }
    }

    fun shouldSendPacket(packet: Packet<*>): Boolean {
        if (noEventPackets.contains(packet)) {
            noEventPackets.remove(packet)
            return false
        } else {
            return true
        }
    }
}
