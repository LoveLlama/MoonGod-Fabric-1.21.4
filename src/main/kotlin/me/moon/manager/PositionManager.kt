package me.moon.manager

import me.moon.features.Feature
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket

class PositionManager : Feature() {
    private var x = 0.0
    private var y = 0.0
    private var z = 0.0
    private var onground = false

    fun updatePosition() {
        x = mc.player!!.x
        y = mc.player!!.y
        z = mc.player!!.z
        onground = mc.player!!.isOnGround
    }

    fun restorePosition() {
        mc.player!!.setPos(x, y, z)
        mc.player!!.setOnGround(onground)
    }

    fun setPlayerPosition(x: Double, y: Double, z: Double) {
        mc.player!!.setPos(x, y, z)
    }

    fun setPlayerPosition(x: Double, y: Double, z: Double, onground: Boolean) {
        mc.player!!.setPos(x, y, z)
        mc.player!!.setOnGround(onground)
    }

    fun setPositionPacket(x: Double, y: Double, z: Double, onGround: Boolean, setPos: Boolean, noLagBack: Boolean) {
        mc.player!!.networkHandler.sendPacket(PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, onGround, false))
        if (setPos) {
            mc.player!!.setPos(x, y, z)
            if (noLagBack) {
                updatePosition()
            }
        }
    }

    fun getX(): Double = x

    fun setX(x: Double) {
        this.x = x
    }

    fun getY(): Double = y

    fun setY(y: Double) {
        this.y = y
    }

    fun getZ(): Double = z

    fun setZ(z: Double) {
        this.z = z
    }
}
