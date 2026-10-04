package me.moon.features.modules.combat

import net.minecraft.network.packet.Packet

import me.moon.event.SubscribeEvent
import me.moon.event.events.PacketEvent
import me.moon.features.modules.Module
import me.moon.features.setting.Setting
import me.moon.util.EntityUtil
import me.moon.util.Timer
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket
import net.minecraft.util.Hand
import net.minecraft.util.math.Vec3d

class Criticals : Module("Criticals", "Scores criticals for you", Module.Category.COMBAT, true, false, false) {
    private val packets = register(Setting("Packets", 2, 1, 5))
    private val desyncDelay = register(Setting("DesyncDelay", 10, 0, 500))
    private val onlySharp = register(Setting("SwordOnly", true))
    private val timer = Timer()

    @SubscribeEvent
    fun onPacketSend(event: PacketEvent.Send) {
        if (event.getPacket<Packet<*>>() is PlayerInteractEntityC2SPacket) {
            val packet = event.getPacket<PlayerInteractEntityC2SPacket>()
            var isAttack = false
            packet.handle(object : PlayerInteractEntityC2SPacket.Handler {
                override fun attack() {
                    isAttack = true
                }

                override fun interact(hand: Hand) {
                }

                override fun interactAt(hand: Hand, pos: Vec3d) {
                }
            })
            if (isAttack) {
                if (fullNullCheck()) {
                    return
                }

                if (this.onlySharp.getValue() && !EntityUtil.holdingWeapon(mc.player!!)) {
                    return
                }

                if (!this.timer.passedMs(this.desyncDelay.getValue().toLong())) {
                    return
                }

                if (mc.player!!.isOnGround
                    && !mc.options.jumpKey.isPressed()
                    && !mc.player!!.isTouchingWater
                    && !mc.player!!.isInLava
                ) {
                    when (this.packets.getValue()) {
                        1 -> {
                            mc.player!!.networkHandler.sendPacket(PlayerMoveC2SPacket.PositionAndOnGround(mc.player!!.x, mc.player!!.y + 0.1, mc.player!!.z, false, mc.player!!.horizontalCollision))
                            mc.player!!.networkHandler.sendPacket(PlayerMoveC2SPacket.PositionAndOnGround(mc.player!!.x, mc.player!!.y, mc.player!!.z, false, mc.player!!.horizontalCollision))
                        }
                        2 -> {
                            mc.player!!.networkHandler.sendPacket(PlayerMoveC2SPacket.PositionAndOnGround(mc.player!!.x, mc.player!!.y + 0.0625101, mc.player!!.z, false, mc.player!!.horizontalCollision))
                            mc.player!!.networkHandler.sendPacket(PlayerMoveC2SPacket.PositionAndOnGround(mc.player!!.x, mc.player!!.y, mc.player!!.z, false, mc.player!!.horizontalCollision))
                            mc.player!!.networkHandler.sendPacket(PlayerMoveC2SPacket.PositionAndOnGround(mc.player!!.x, mc.player!!.y + 1.1E-5, mc.player!!.z, false, mc.player!!.horizontalCollision))
                            mc.player!!.networkHandler.sendPacket(PlayerMoveC2SPacket.PositionAndOnGround(mc.player!!.x, mc.player!!.y, mc.player!!.z, false, mc.player!!.horizontalCollision))
                        }
                        3 -> {
                            mc.player!!.networkHandler.sendPacket(PlayerMoveC2SPacket.PositionAndOnGround(mc.player!!.x, mc.player!!.y + 0.0625101, mc.player!!.z, false, mc.player!!.horizontalCollision))
                            mc.player!!.networkHandler.sendPacket(PlayerMoveC2SPacket.PositionAndOnGround(mc.player!!.x, mc.player!!.y, mc.player!!.z, false, mc.player!!.horizontalCollision))
                            mc.player!!.networkHandler.sendPacket(PlayerMoveC2SPacket.PositionAndOnGround(mc.player!!.x, mc.player!!.y + 0.0125, mc.player!!.z, false, mc.player!!.horizontalCollision))
                            mc.player!!.networkHandler.sendPacket(PlayerMoveC2SPacket.PositionAndOnGround(mc.player!!.x, mc.player!!.y, mc.player!!.z, false, mc.player!!.horizontalCollision))
                        }
                        4 -> {
                            mc.player!!.networkHandler.sendPacket(PlayerMoveC2SPacket.PositionAndOnGround(mc.player!!.x, mc.player!!.y + 0.05, mc.player!!.z, false, mc.player!!.horizontalCollision))
                            mc.player!!.networkHandler.sendPacket(PlayerMoveC2SPacket.PositionAndOnGround(mc.player!!.x, mc.player!!.y, mc.player!!.z, false, mc.player!!.horizontalCollision))
                            mc.player!!.networkHandler.sendPacket(PlayerMoveC2SPacket.PositionAndOnGround(mc.player!!.x, mc.player!!.y + 0.03, mc.player!!.z, false, mc.player!!.horizontalCollision))
                            mc.player!!.networkHandler.sendPacket(PlayerMoveC2SPacket.PositionAndOnGround(mc.player!!.x, mc.player!!.y, mc.player!!.z, false, mc.player!!.horizontalCollision))
                        }
                        5 -> {
                            mc.player!!.networkHandler.sendPacket(PlayerMoveC2SPacket.PositionAndOnGround(mc.player!!.x, mc.player!!.y + 0.1625, mc.player!!.z, false, mc.player!!.horizontalCollision))
                            mc.player!!.networkHandler.sendPacket(PlayerMoveC2SPacket.PositionAndOnGround(mc.player!!.x, mc.player!!.y, mc.player!!.z, false, mc.player!!.horizontalCollision))
                            mc.player!!.networkHandler.sendPacket(PlayerMoveC2SPacket.PositionAndOnGround(mc.player!!.x, mc.player!!.y + 4.0E-6, mc.player!!.z, false, mc.player!!.horizontalCollision))
                            mc.player!!.networkHandler.sendPacket(PlayerMoveC2SPacket.PositionAndOnGround(mc.player!!.x, mc.player!!.y, mc.player!!.z, false, mc.player!!.horizontalCollision))
                            mc.player!!.networkHandler.sendPacket(PlayerMoveC2SPacket.PositionAndOnGround(mc.player!!.x, mc.player!!.y + 1.0E-6, mc.player!!.z, false, mc.player!!.horizontalCollision))
                            mc.player!!.networkHandler.sendPacket(PlayerMoveC2SPacket.PositionAndOnGround(mc.player!!.x, mc.player!!.y, mc.player!!.z, false, mc.player!!.horizontalCollision))
                            mc.player!!.networkHandler.sendPacket(PlayerMoveC2SPacket.OnGroundOnly(mc.player!!.isOnGround, mc.player!!.horizontalCollision))
                        }
                    }

                    this.timer.reset()
                }
            }
        }
    }

    override fun getDisplayInfo(): String {
        return "Packet"
    }
}
