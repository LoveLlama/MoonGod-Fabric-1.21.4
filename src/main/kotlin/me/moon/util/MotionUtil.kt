package me.moon.util

import net.minecraft.entity.LivingEntity
import net.minecraft.entity.effect.StatusEffects

object MotionUtil : Util {
    @JvmStatic
    fun isMoving(entity: LivingEntity): Boolean {
        return entity.forwardSpeed != 0.0f || entity.sidewaysSpeed != 0.0f
    }

    @JvmStatic
    fun setSpeed(entity: LivingEntity, speed: Double) {
        val dir = forward(speed)
        entity.setVelocity(dir[0], entity.velocity.y, dir[1])
    }

    @JvmStatic
    fun getBaseMoveSpeed(): Double {
        var baseSpeed = 0.2873
        val p = mc.player
        if (p != null && p.hasStatusEffect(StatusEffects.SPEED)) {
            val amplifier = p.getStatusEffect(StatusEffects.SPEED)!!.amplifier
            baseSpeed *= 1.0 + 0.2 * (amplifier + 1)
        }

        return baseSpeed
    }

    @JvmStatic
    fun forward(speed: Double): DoubleArray {
        val p = mc.player!!
        val forward = p.input.movementForward
        val side = p.input.movementSideways
        var yaw = p.prevYaw + (p.yaw - p.prevYaw) * mc.renderTickCounter.getTickDelta(true)
        var f = forward
        var s = side
        if (f != 0.0f) {
            if (s > 0.0f) {
                yaw += if (f > 0.0f) -45 else 45
            } else if (s < 0.0f) {
                yaw += if (f > 0.0f) 45 else -45
            }

            s = 0.0f
            if (f > 0.0f) {
                f = 1.0f
            } else if (f < 0.0f) {
                f = -1.0f
            }
        }

        val sin = Math.sin(Math.toRadians((yaw + 90.0f).toDouble()))
        val cos = Math.cos(Math.toRadians((yaw + 90.0f).toDouble()))
        val posX = f.toDouble() * speed * cos + s.toDouble() * speed * sin
        val posZ = f.toDouble() * speed * sin - s.toDouble() * speed * cos
        return doubleArrayOf(posX, posZ)
    }
}
