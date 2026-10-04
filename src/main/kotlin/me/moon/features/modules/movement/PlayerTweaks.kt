package me.moon.features.modules.movement

import net.minecraft.network.packet.Packet

import me.moon.event.SubscribeEvent
import me.moon.event.events.PacketEvent
import me.moon.event.events.PushEvent
import me.moon.features.modules.Module
import me.moon.features.setting.Setting
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket

class PlayerTweaks : Module("PlayerTweaks", "XD", Module.Category.MOVEMENT, true, false, false) {
    val noSlow = register(Setting("No Slow", true))
    private val antiKnockBack = register(Setting("Velocity", true))
    private val noEntityPush = register(Setting("No PlayerPush", true))
    private val noBlockPush = register(Setting("No BlockPush", true))
    private val noWaterPush = register(Setting("No LiquidPush", true))
    private val guiMove = register(Setting("Gui Move", true))

    init { setInstance() }
    private fun setInstance() { INSTANCE = this }

    @SubscribeEvent
    fun onPacketReceived(event: PacketEvent.Receive) {
        if (!fullNullCheck()) {
            if (this.antiKnockBack.getValue()) {
                if (event.getPacket<Packet<*>>() is EntityVelocityUpdateS2CPacket && event.getPacket<EntityVelocityUpdateS2CPacket>().entityId == mc.player!!.id) {
                    event.isCanceled = true
                }
                if (event.getPacket<Packet<*>>() is ExplosionS2CPacket) {
                    event.isCanceled = true
                }
            }
        }
    }

    @SubscribeEvent
    fun onPush(event: PushEvent) {
        val player = mc.player
        if (player != null && !fullNullCheck()) {
            if (event.stage == 0 && this.noEntityPush.getValue() && event.entity == player) {
                event.x = -event.x * 0.0
                event.y = -event.y * 0.0
                event.z = -event.z * 0.0
            } else if (event.stage == 1 && this.noBlockPush.getValue()) {
                event.isCanceled = true
            } else if (event.stage == 2 && this.noWaterPush.getValue() && event.entity == player) {
                event.isCanceled = true
            }
        }
    }

    companion object {
        @JvmField
        var INSTANCE: PlayerTweaks = PlayerTweaks()

        @JvmStatic
        fun getInstance(): PlayerTweaks = INSTANCE
    }
}
