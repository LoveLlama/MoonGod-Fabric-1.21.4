package me.moon.features.modules.misc

import net.minecraft.network.packet.Packet

import me.moon.event.SubscribeEvent
import me.moon.event.events.PacketEvent
import me.moon.features.modules.Module
import me.moon.features.setting.Setting
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket
import net.minecraft.util.math.Direction

class Interact : Module("Interact", "ForceInteract", Module.Category.MISC, true, false, false) {
    var buildHeight = register(Setting("BuildHeight", false))
    var liquid = register(Setting("Liquid", false))

    init { setInstance() }
    private fun setInstance() { INSTANCE = this }

    @SubscribeEvent
    fun onPacketSend(event: PacketEvent.Send) {
        if (this.buildHeight.getValue() && event.getPacket<Packet<*>>() is PlayerInteractBlockC2SPacket) {
            val packet = event.getPacket<PlayerInteractBlockC2SPacket>()
            val bhr = packet.blockHitResult
            if (bhr.blockPos.y >= 255 && bhr.side == Direction.UP) {
                event.isCanceled = true
                mc.player!!.networkHandler.sendPacket(PlayerInteractBlockC2SPacket(packet.hand, bhr.withSide(Direction.DOWN), packet.sequence))
            }
        }
    }

    companion object {
        @JvmField
        var INSTANCE: Interact = Interact()

        @JvmStatic
        fun getInstance(): Interact = INSTANCE
    }
}
