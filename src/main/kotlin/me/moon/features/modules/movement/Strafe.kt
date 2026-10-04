package me.moon.features.modules.movement

import me.moon.event.SubscribeEvent
import me.moon.event.events.MoveEvent
import me.moon.event.events.UpdateWalkingPlayerEvent
import me.moon.features.modules.Module
import me.moon.features.setting.Setting
import net.minecraft.entity.effect.StatusEffects
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class Strafe : Module("Strafe", "Modifies sprinting", Module.Category.MOVEMENT, true, false, false) {
    var mode = register(Setting("Mode", Mode.NORMAL))
    private var lastDist = 0.0
    private var moveSpeed = 0.0
    private var stage = 0

    init { setInstance() }
    private fun setInstance() { INSTANCE = this }

    @SubscribeEvent
    fun onUpdateWalkingPlayerEvent(event: UpdateWalkingPlayerEvent) {
        if (event.stage != 1 || !fullNullCheck()) {
            this.lastDist = sqrt(
                (mc.player!!.x - mc.player!!.prevX) * (mc.player!!.x - mc.player!!.prevX)
                    + (mc.player!!.z - mc.player!!.prevZ) * (mc.player!!.z - mc.player!!.prevZ)
            )
        }
    }

    @SubscribeEvent
    fun onStrafe(event: MoveEvent) {
        if (!fullNullCheck()) {
            if (!mc.player!!.isTouchingWater && !mc.player!!.isInLava) {
                if (mc.player!!.isOnGround) {
                    this.stage = 2
                }

                when (this.stage) {
                    0 -> {
                        ++this.stage
                        this.lastDist = 0.0
                    }
                    1 -> {
                        if ((mc.world!!.getEntityCollisions(mc.player!!, mc.player!!.boundingBox.offset(0.0, mc.player!!.velocity.y, 0.0)).isNotEmpty()
                                || mc.player!!.verticalCollision)
                            && this.stage > 0
                        ) {
                            this.stage = if (mc.player!!.input.movementForward == 0.0f && mc.player!!.input.movementSideways == 0.0f) 0 else 1
                        }
                        this.moveSpeed = this.lastDist - this.lastDist / (if (this.mode.getValue() == Mode.NORMAL) 730.0 else 159.0)
                    }
                    2 -> {
                        var motionY = 0.40123128
                        if (mc.player!!.isOnGround && mc.options.jumpKey.isPressed()) {
                            val jump = mc.player!!.getStatusEffect(StatusEffects.JUMP_BOOST)
                            if (jump != null) {
                                motionY += (jump.amplifier + 1) * 0.1f.toDouble()
                            }
                            event.setY(motionY)
                            mc.player!!.velocity = net.minecraft.util.math.Vec3d(mc.player!!.velocity.x, motionY, mc.player!!.velocity.z)
                            this.moveSpeed *= if (this.mode.getValue() == Mode.NORMAL) 1.67 else 2.149
                        }
                    }
                    3 -> this.moveSpeed = this.lastDist - (if (this.mode.getValue() == Mode.NORMAL) 0.6896 else 0.795) * (this.lastDist - this.getBaseMoveSpeed())
                }

                if (!mc.options.jumpKey.isPressed() && mc.player!!.isOnGround) {
                    this.moveSpeed = this.getBaseMoveSpeed()
                } else {
                    this.moveSpeed = maxOf(this.moveSpeed, this.getBaseMoveSpeed())
                }

                var n = mc.player!!.input.movementForward.toDouble()
                var n2 = mc.player!!.input.movementSideways.toDouble()
                val n3 = mc.player!!.yaw.toDouble()
                if (n == 0.0 && n2 == 0.0) {
                    event.setX(0.0)
                    event.setZ(0.0)
                } else if (n != 0.0 && n2 != 0.0) {
                    n *= sin(Math.PI / 4)
                    n2 *= cos(Math.PI / 4)
                }

                val n4 = if (this.mode.getValue() == Mode.NORMAL) 0.993 else 0.99
                event.setX((n * this.moveSpeed * -sin(Math.toRadians(n3)) + n2 * this.moveSpeed * cos(Math.toRadians(n3))) * n4)
                event.setZ((n * this.moveSpeed * cos(Math.toRadians(n3)) - n2 * this.moveSpeed * -sin(Math.toRadians(n3))) * n4)
                ++this.stage
                event.isCanceled = true
            }
        }
    }

    fun getBaseMoveSpeed(): Double {
        var n = 0.2873
        val speed = mc.player!!.getStatusEffect(StatusEffects.SPEED)
        if (speed != null) {
            n *= 1.0 + 0.2 * (speed.amplifier + 1)
        }
        return n
    }

    override fun getDisplayInfo(): String = this.mode.getValue().name

    enum class Mode {
        NORMAL,
        Strict
    }

    companion object {
        @JvmField
        var INSTANCE: Strafe = Strafe()

        @JvmStatic
        fun getInstance(): Strafe = INSTANCE
    }
}
