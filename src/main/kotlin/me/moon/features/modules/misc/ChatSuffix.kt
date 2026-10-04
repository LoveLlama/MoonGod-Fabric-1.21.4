package me.moon.features.modules.misc

import net.minecraft.network.packet.Packet

import me.moon.event.SubscribeEvent
import me.moon.event.events.PacketEvent
import me.moon.features.modules.Module
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket

class ChatSuffix : Module("ChatSuffix", "ChatSuffix", Module.Category.MISC, true, false, false) {
    @SubscribeEvent
    fun onPacketSend(event: PacketEvent.Send) {
        if (event.stage == 0 && event.getPacket<Packet<*>>() is ChatMessageC2SPacket) {
            val packet = event.getPacket<ChatMessageC2SPacket>()
            var message = packet.chatMessage
            if (message.startsWith("/")) {
                return
            }
            if (message.endsWith("  >  Μ Ο Ο Ν")) {
                return
            }
            message = message + "  >  Μ Ο Ο Ν"
            if (message.length >= 256) {
                message = message.substring(0, 256)
            }
            event.isCanceled = true
            mc.player!!.networkHandler.sendPacket(ChatMessageC2SPacket(message, packet.timestamp, packet.salt, null, packet.acknowledgment))
        }
    }
}
