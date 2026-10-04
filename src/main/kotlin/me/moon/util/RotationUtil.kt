package me.moon.util

import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d

object RotationUtil : Util {
    @JvmStatic
    fun getEyesPos(): Vec3d {
        val p = mc.player!!
        return Vec3d(p.x, p.eyeY, p.z)
    }

    @JvmStatic
    fun calculateLookAt(px: Double, py: Double, pz: Double, me: PlayerEntity): DoubleArray {
        val dirx = me.x - px
        val diry = me.y - py
        val dirz = me.z - pz
        val len = Math.sqrt(dirx * dirx + diry * diry + dirz * dirz)
        val pitch = Math.asin(diry / len)
        val yaw = Math.atan2(dirz / len, dirx / len)
        val pitchDeg = pitch * 180.0 / Math.PI
        val yawDeg = yaw * 180.0 / Math.PI
        return doubleArrayOf(yawDeg + 90.0, pitchDeg)
    }

    @JvmStatic
    fun getLegitRotations(vec: Vec3d): FloatArray {
        val p = mc.player!!
        val eyesPos = getEyesPos()
        val diffX = vec.x - eyesPos.x
        val diffY = vec.y - eyesPos.y
        val diffZ = vec.z - eyesPos.z
        val diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ)
        val yaw = Math.toDegrees(Math.atan2(diffZ, diffX)).toFloat() - 90.0f
        val pitch = (-Math.toDegrees(Math.atan2(diffY, diffXZ))).toFloat()
        return floatArrayOf(
            p.yaw + MathHelper.wrapDegrees(yaw - p.yaw),
            p.pitch + MathHelper.wrapDegrees(pitch - p.pitch)
        )
    }

    @JvmStatic
    fun faceYawAndPitch(yaw: Float, pitch: Float) {
        val p = mc.player!!
        p.networkHandler.sendPacket(
            PlayerMoveC2SPacket.LookAndOnGround(yaw, pitch, p.isOnGround, p.horizontalCollision)
        )
    }

    @JvmStatic
    fun faceVector(vec: Vec3d, normalizeAngle: Boolean) {
        val p = mc.player!!
        val rotations = getLegitRotations(vec)
        p.networkHandler.sendPacket(
            PlayerMoveC2SPacket.LookAndOnGround(
                rotations[0],
                if (normalizeAngle) normalizeAngle(rotations[1].toInt(), 360).toFloat() else rotations[1],
                p.isOnGround,
                p.horizontalCollision
            )
        )
    }

    @JvmStatic
    fun faceEntity(entity: Entity) {
        val angle = MathUtil.calcAngle(
            mc.player!!.getCameraPosVec(mc.renderTickCounter.getTickDelta(true)),
            entity.getCameraPosVec(mc.renderTickCounter.getTickDelta(true))
        )
        faceYawAndPitch(angle[0], angle[1])
    }

    @JvmStatic
    fun getAngle(entity: Entity): FloatArray {
        return MathUtil.calcAngle(
            mc.player!!.getCameraPosVec(mc.renderTickCounter.getTickDelta(true)),
            entity.getCameraPosVec(mc.renderTickCounter.getTickDelta(true))
        )
    }

    @JvmStatic
    fun getDirection4D(): Int {
        return MathHelper.floor((mc.player!!.yaw * 4.0f / 360.0f) + 0.5f) and 3
    }

    @JvmStatic
    fun getDirection4D(northRed: Boolean): String {
        val dirnumber = getDirection4D()
        if (dirnumber == 0) {
            return "South (+Z)"
        } else if (dirnumber == 1) {
            return "West (-X)"
        } else if (dirnumber == 2) {
            return (if (northRed) "§c" else "") + "North (-Z)"
        } else {
            return if (dirnumber == 3) "East (+X)" else "Loading..."
        }
    }

    private fun normalizeAngle(angle: Int, divisor: Int): Int {
        return angle - Math.floor(angle.toDouble() / divisor.toDouble()).toInt() * divisor
    }
}
