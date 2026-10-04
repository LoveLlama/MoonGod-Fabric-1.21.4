package me.moon.manager

import me.moon.Moon
import me.moon.event.EventBus
import me.moon.event.SubscribeEvent
import me.moon.event.events.PacketEvent
import me.moon.features.Feature
import me.moon.features.command.Command
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket
import net.minecraft.util.Formatting

class ReloadManager : Feature() {
    var prefix: String = ""

    fun init(prefix: String) {
        this.prefix = prefix
        EventBus.register(this)
        if (!fullNullCheck()) {
            Command.sendMessage(Formatting.RED.toString() + "Moon has been unloaded. Type " + prefix + "reload to reload.")
        }
    }

    fun unload() {
        EventBus.unregister(this)
    }

    @SubscribeEvent
    fun onPacketSend(event: PacketEvent.Send) {
        val packet: ChatMessageC2SPacket?
        if (event.getPacket<net.minecraft.network.packet.Packet<*>>() is ChatMessageC2SPacket) {
            packet = event.getPacket<ChatMessageC2SPacket>()
            if (packet.chatMessage.startsWith(prefix) && packet.chatMessage.contains("reload")) {
                Moon.load()
                event.isCanceled = true
            }
        }
    }
}
