package me.moon.features.modules.movement

import net.minecraft.network.packet.Packet

import me.moon.event.SubscribeEvent
import me.moon.event.events.MoveEvent
import me.moon.event.events.PacketEvent
import me.moon.features.modules.Module
import me.moon.features.setting.Setting
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket
import net.minecraft.util.math.MathHelper
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class ElytraFlight : Module("ElytraFlight", "Makes Elytra Flight better.", Module.Category.MOVEMENT, true, false, false) {
    var upSpeed = register(Setting("UpSpeed", 2.0f, 0.0f, 3.0f))
    var PitchSpoof = register(Setting("PitchSpoof", true))
    var oneSpeed = register(Setting("1 Speed", 1.0f, 0.0f, 10.0f))
    var twoSpeed = register(Setting("2 Speed", 1.8f, 1.0f, 2.0f))

    @SubscribeEvent
    fun onSendPacket(event: PacketEvent.Send) {
        if (fullNullCheck()) {
            return
        }
        if (event.getPacket<Packet<*>>() is PlayerMoveC2SPacket
            && !mc.player!!.isGliding
            && event.getPacket<Packet<*>>() is PlayerMoveC2SPacket
            && mc.player!!.isGliding
        ) {
            if (event.getPacket<Packet<*>>() is PlayerMoveC2SPacket.Full) {
                val rotation = event.getPacket<PlayerMoveC2SPacket.Full>()
                mc.player!!.networkHandler.sendPacket(PlayerMoveC2SPacket.PositionAndOnGround(mc.player!!.x, mc.player!!.y, mc.player!!.z, rotation.isOnGround(), rotation.horizontalCollision()))
                event.isCanceled = true
            } else if (event.getPacket<Packet<*>>() is PlayerMoveC2SPacket.PositionAndOnGround) {
                event.isCanceled = true
            }
        }
    }

    @SubscribeEvent
    fun onMove(event: MoveEvent) {
        val yaw = Math.toRadians(mc.player!!.yaw.toDouble())
        val motionA = sqrt(mc.player!!.velocity.x * mc.player!!.velocity.x + mc.player!!.velocity.z * mc.player!!.velocity.z)
        if (mc.player!!.isGliding) {
            if (mc.options.sneakKey.isPressed()) {
                mc.player!!.velocity = mc.player!!.velocity.add(0.0, -1.0 - mc.player!!.velocity.y, 0.0)
            } else if (!mc.options.jumpKey.isPressed() || !(motionA > 1.0)) {
                if (event.getY() != -1.01E-4) {
                    event.setY(-1.01E-4)
                    mc.player!!.velocity = mc.player!!.velocity.add(0.0, -1.01E-4 - mc.player!!.velocity.y, 0.0)
                }

                this.setMoveSpeed(event, this.oneSpeed.getValue().toDouble() * 1.8)
            } else if (mc.player!!.velocity.x == 0.0 && mc.player!!.velocity.z == 0.0) {
                mc.player!!.velocity = mc.player!!.velocity.add(0.0, this.upSpeed.getValue().toDouble() - mc.player!!.velocity.y, 0.0)
            } else {
                val motionB = motionA * 0.008
                mc.player!!.velocity = mc.player!!.velocity.add(
                    -(-sin(yaw)) * motionB / 1.0,
                    motionB * 3.2,
                    -cos(yaw) * motionB / 1.0
                )
                mc.player!!.velocity = mc.player!!.velocity.multiply(0.98, 0.99, 0.99)
            }
        }
    }

    private fun setMoveSpeed(event: MoveEvent, speed: Double) {
        var forward = mc.player!!.input.movementForward.toDouble()
        var strafe = mc.player!!.input.movementSideways.toDouble()
        var yaw = mc.player!!.yaw
        if (forward == 0.0 && strafe == 0.0) {
            event.setX(0.0)
            event.setZ(0.0)
            mc.player!!.velocity = net.minecraft.util.math.Vec3d(0.0, mc.player!!.velocity.y, 0.0)
        } else {
            if (forward != 0.0) {
                if (strafe > 0.0) {
                    yaw += (if (forward > 0.0) -45 else 45).toFloat()
                } else if (strafe < 0.0) {
                    yaw += (if (forward > 0.0) 45 else -45).toFloat()
                }

                strafe = 0.0
                if (forward > 0.0) {
                    forward = 1.0
                } else if (forward < 0.0) {
                    forward = -1.0
                }
            }

            val x = forward * speed * -sin(Math.toRadians(yaw.toDouble())) + strafe * speed * cos(Math.toRadians(yaw.toDouble()))
            val z = forward * speed * cos(Math.toRadians(yaw.toDouble())) - strafe * speed * -sin(Math.toRadians(yaw.toDouble()))
            event.setX(x)
            event.setZ(z)
            mc.player!!.velocity = net.minecraft.util.math.Vec3d(x, mc.player!!.velocity.y, z)
        }
    }

    override fun onDisable() {
        if (!fullNullCheck() && !mc.player!!.isCreative) {
            mc.player!!.abilities.flying = false
        }
    }
}
