package me.moon.features.modules.movement

import me.moon.Moon
import me.moon.event.SubscribeEvent
import me.moon.event.events.MoveEvent
import me.moon.event.events.PacketEvent
import me.moon.event.events.PushEvent
import me.moon.event.events.UpdateWalkingPlayerEvent
import me.moon.features.modules.Module
import me.moon.features.setting.Setting
import me.moon.util.EntityUtil
import me.moon.util.MathUtil
import me.moon.util.MotionUtil
import me.moon.util.Timer
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.network.packet.Packet
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket
import net.minecraft.util.math.Vec3d
import java.util.function.Predicate
import kotlin.math.sqrt

class Speed : Module("Speed", "3ar", Module.Category.MOVEMENT, true, false, false) {
    private val mode = register(Setting("Mode", Mode.Instant))
    private val jump = register(Setting("Jump", true, Predicate { mode.getValue() == Mode.Strafe }))
    private val inWater = register(Setting("InWater", false))
    private val strafeSpeed = register(Setting("StrafeSpeed", 287.3, 100.0, 1000.0, Predicate { mode.getValue() == Mode.Strafe }))
    private val speedSet = register(Setting("Speed", 4.0, 0.1, 10.0, Predicate { mode.getValue() == Mode.Vanilla }))
    private val constTicks = register(Setting("ConstTicks", 10, 1, 40, Predicate { mode.getValue() == Mode.Constantiam }))
    private val constOff = register(Setting("ConstOff", 3, 1, 10, Predicate { mode.getValue() == Mode.Constantiam }))
    private val constFactor = register(Setting("ConstFactor", 2.149, 1.0, 5.0, Predicate { mode.getValue() == Mode.Constantiam }))
    private val useTimer = register(Setting("UseTimer", false, Predicate { mode.getValue() == Mode.Strafe || mode.getValue() == Mode.LowHop }))
    private val noWaterInstant = register(Setting("NoLiquidInstant", false, Predicate { mode.getValue() == Mode.Instant }))
    private val explosions = register(Setting("Explosions", false))
    private val velocity = register(Setting("Velocity", false))
    private val multiplier = register(Setting("H-Factor", 1.0f, 0.0f, 5.0f))
    private val vertical = register(Setting("V-Factor", 1.0f, 0.0f, 5.0f))
    private val coolDown = register(Setting("CoolDown", 1000, 0, 5000))
    private val directional = register(Setting("Directional", false))
    private val lagOut = register(Setting("LagOutBlocks", false))
    private val lagTime = register(Setting("LagTime", 500, 0, 1000, Predicate { mode.getValue() == Mode.Strafe }))
    private val cap = register(Setting("Cap", 10.0, 0.0, 10.0))
    private val scaleCap = register(Setting("ScaleCap", false))
    private val slow = register(Setting("Slowness", false))
    private val modify = register(Setting("Modify", false))
    private val xzFactor = register(Setting("XZ-Factor", 1.0, 0.0, 5.0, Predicate { modify.getValue() }))
    private val yFactor = register(Setting("Y-Factor", 1.0, 0.0, 5.0, Predicate { modify.getValue() }))

    private val expTimer = Timer()
    private val lagTimer = Timer()
    private var stop = false
    private var vanillaStage = 0
    private var onGroundStage = 2
    private var oldGroundStage = 2
    private var speed = 0.0
    private var distance = 0.0
    private var gayStage = 1
    private var stage = 4
    private var ncpStage = 0
    private var bhopStage = 4
    private var vStage = 1
    private var lowStage = 4
    private var constStage = 0
    private var lastExp = 0.0
    private var lastDist = 0.0
    private var boost = false
    private var timerActive = false

    override fun onEnable() {
        val p = mc.player
        if (p != null) {
            speed = MotionUtil.getBaseMoveSpeed()
            distance = getDistance2D()
        }
        vanillaStage = 0
        onGroundStage = 2
        oldGroundStage = 2
        ncpStage = 0
        gayStage = 1
        vStage = 1
        bhopStage = 4
        stage = 4
        lowStage = 4
        lastDist = 0.0
        constStage = 0
    }

    override fun onDisable() {
        EntityUtil.resetTimer()
        timerActive = false
    }

    override fun getDisplayInfo(): String = mode.getValue().name

    @SubscribeEvent
    fun onMotion(event: UpdateWalkingPlayerEvent) {
        if (event.stage != 1 || fullNullCheck()) {
            return
        }
        distance = getDistance2D()
    }

    @SubscribeEvent
    fun onPacket(event: PacketEvent.Receive) {
        if (fullNullCheck()) {
            return
        }
        val packet = event.getPacket<Packet<*>>()
        if (packet is EntityVelocityUpdateS2CPacket) {
            val player = mc.player
            if (player != null && packet.entityId == player.id && !directional.getValue() && velocity.getValue()) {
                val sp = sqrt(packet.velocityX * packet.velocityX + packet.velocityZ * packet.velocityZ)
                lastExp = if (expTimer.passedMs(coolDown.getValue().toLong())) sp else sp - lastExp
                if (lastExp > 0.0) {
                    expTimer.reset()
                    mc.execute {
                        val p = mc.player ?: return@execute
                        speed += lastExp * multiplier.getValue().toDouble()
                        distance += lastExp * multiplier.getValue().toDouble()
                        if (p.velocity.y > 0.0 && vertical.getValue() != 0.0f) {
                            p.velocity = Vec3d(p.velocity.x, p.velocity.y * vertical.getValue().toDouble(), p.velocity.z)
                        }
                    }
                }
            }
        } else if (packet is PlayerPositionLookS2CPacket) {
            lagTimer.reset()
            if (mc.player != null) {
                distance = 0.0
            }
            speed = 0.0
            vanillaStage = 0
            onGroundStage = 2
            ncpStage = 1
            gayStage = 1
            vStage = 1
            bhopStage = 4
            stage = 4
            lowStage = 4
            constStage = 0
        } else if (packet is ExplosionS2CPacket && explosions.getValue() && isMoving()) {
            val center = packet.center
            val p = mc.player ?: return
            if (p.squaredDistanceTo(center) < 100.0 && (!directional.getValue() || !isInMovementDirection(center.x, center.y, center.z))) {
                val kb = packet.playerKnockback.orElse(Vec3d.ZERO)
                val sp = sqrt(kb.x * kb.x + kb.z * kb.z)
                lastExp = if (expTimer.passedMs(coolDown.getValue().toLong())) sp else sp - lastExp
                if (lastExp > 0.0) {
                    expTimer.reset()
                    mc.execute {
                        val pl = mc.player ?: return@execute
                        speed += lastExp * multiplier.getValue().toDouble()
                        distance += lastExp * multiplier.getValue().toDouble()
                        if (pl.velocity.y > 0.0) {
                            pl.velocity = Vec3d(pl.velocity.x, pl.velocity.y * vertical.getValue().toDouble(), pl.velocity.z)
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    fun onMove(event: MoveEvent) {
        if (fullNullCheck()) {
            return
        }
        if (noMovementKeys()) {
            val v = mc.player!!.velocity
            mc.player!!.velocity = Vec3d(0.0, v.y, 0.0)
        }
        if (mode.getValue() == Mode.OldGround) {
            if (notColliding()) {
                oldGroundStage++
            } else {
                oldGroundStage = 0
            }
            if (oldGroundStage == 4) {
                event.setY(event.getY() + (if (isBoxColliding()) 0.2 else 0.4) + getJumpSpeed())
            }
            if (oldGroundStage == 3) {
                event.setX(event.getX() * 3.25)
                event.setZ(event.getZ() * 3.25)
                event.isCanceled = true
            } else if (oldGroundStage == 4) {
                event.setX(event.getX() / 1.4)
                event.setZ(event.getZ() / 1.4)
                oldGroundStage = 2
                event.isCanceled = true
            }
        }
        if (mode.getValue() == Mode.OnGround && onGroundStage == 3) {
            event.setY(event.getY() + (if (isBoxColliding()) 0.2 else 0.4) + getJumpSpeed())
            event.isCanceled = true
        }
        if ((inWater.getValue() || !inLiquid()) && !mc.player!!.isClimbing() && !mc.player!!.isInsideWall()) {
            if (stop) {
                stop = false
            } else {
                move(mode.getValue(), event)
                if (modify.getValue()) {
                    event.setX(event.getX() * xzFactor.getValue())
                    event.setY(event.getY() * yFactor.getValue())
                    event.setZ(event.getZ() * xzFactor.getValue())
                    event.isCanceled = true
                }
            }
        } else {
            stop = true
        }
    }

    @SubscribeEvent
    fun onPush(event: PushEvent) {
        if (lagOut.getValue()) {
            event.isCanceled = false
        }
    }

    private fun move(mode: Mode, event: MoveEvent) {
        when (mode) {
            Mode.Instant -> {
                if (mc.player!!.isGliding()) {
                    return
                }
                if (!noWaterInstant.getValue() || !(mc.player!!.isTouchingWater || mc.player!!.isInLava)) {
                    strafe(event, getSpeed(slow.getValue()))
                }
            }
            Mode.OldGround, Mode.None -> {}
            Mode.Vanilla -> strafe(event, speedSet.getValue() / 10.0)
            Mode.OnGround -> {
                if (mc.player!!.isOnGround || onGroundStage == 3) {
                    if ((!mc.player!!.horizontalCollision && mc.player!!.input.movementForward != 0.0f) || mc.player!!.input.movementSideways != 0.0f) {
                        if (onGroundStage == 2) {
                            speed *= 2.149
                            onGroundStage = 3
                        } else if (onGroundStage == 3) {
                            onGroundStage = 2
                            speed = distance - 0.66 * (distance - getSpeed(slow.getValue()))
                        } else if (isBoxColliding() || mc.player!!.verticalCollision) {
                            onGroundStage = 1
                        }
                    }
                    speed = minOf(speed, getCap())
                    speed = maxOf(speed, getSpeed(slow.getValue()))
                    strafe(event, speed)
                }
            }
            Mode.NCP -> {
                if (mc.player!!.isGliding()) {
                    return
                }
                when (ncpStage) {
                    0 -> {
                        ncpStage++
                        lastDist = 0.0
                    }
                    2 -> {
                        if ((mc.player!!.input.movementForward != 0.0f || mc.player!!.input.movementSideways != 0.0f) && mc.player!!.isOnGround) {
                            val y = (if (isBoxColliding()) 0.2 else 0.3999) + getJumpSpeed()
                            setMotion(y, event)
                            speed *= 2.149
                        }
                    }
                    3 -> speed = lastDist - 0.7095 * (lastDist - getSpeed(slow.getValue()))
                    else -> {
                        if ((mc.world!!.getEntityCollisions(mc.player!!, mc.player!!.boundingBox.offset(0.0, mc.player!!.velocity.y, 0.0)).isNotEmpty() || mc.player!!.verticalCollision) && ncpStage > 0) {
                            ncpStage = if (mc.player!!.input.movementForward == 0.0f && mc.player!!.input.movementSideways == 0.0f) 0 else 1
                        }
                        speed = lastDist - lastDist / 159.0
                    }
                }
                speed = minOf(speed, getCap())
                speed = maxOf(speed, getSpeed(slow.getValue()))
                strafe(event, speed)
                ncpStage++
            }
            Mode.Strafe -> {
                if (!isMoving()) {
                    return
                }
                if (mc.player!!.isGliding()) {
                    return
                }
                if (!lagTimer.passedMs(lagTime.getValue().toLong())) {
                    return
                }
                if (useTimer.getValue() && lagTimer.passedMs(250L)) {
                    if (!timerActive) {
                        EntityUtil.setTimer(1.0888f)
                        timerActive = true
                    }
                } else if (timerActive) {
                    EntityUtil.resetTimer()
                    timerActive = false
                }
                if (stage == 1 && isMoving()) {
                    speed = 1.35 * getSpeed(slow.getValue(), strafeSpeed.getValue() / 1000.0) - 0.01
                } else if (stage != 2 || !isMoving() || (!isJumping() && !jump.getValue())) {
                    if (stage == 3) {
                        speed = distance - 0.66 * (distance - getSpeed(slow.getValue(), strafeSpeed.getValue() / 1000.0))
                        boost = !boost
                    } else {
                        if ((mc.world!!.getEntityCollisions(mc.player!!, mc.player!!.boundingBox.offset(0.0, mc.player!!.velocity.y, 0.0)).isNotEmpty() || mc.player!!.verticalCollision) && stage > 0) {
                            stage = if (isMoving()) 1 else 0
                        }
                        speed = distance - distance / 159.0
                    }
                } else {
                    val yMotion = 0.3999 + getJumpSpeed()
                    setMotion(yMotion, event)
                    speed *= if (boost) 1.6835 else 1.395
                }
                speed = minOf(speed, getCap())
                speed = maxOf(speed, getSpeed(slow.getValue(), strafeSpeed.getValue() / 1000.0))
                strafe(event, speed)
                if (isMoving()) {
                    stage++
                }
            }
            Mode.GayHop -> {
                if (!lagTimer.passedMs(100L)) {
                    gayStage = 1
                    return
                }
                if (!isMoving()) {
                    speed = getSpeed(slow.getValue())
                }
                if (gayStage == 1 && mc.player!!.verticalCollision && isMoving()) {
                    speed = 0.25 + getSpeed(slow.getValue()) - 0.01
                } else if (gayStage == 2 && mc.player!!.verticalCollision && isMoving()) {
                    val y = (if (isBoxColliding()) 0.2 else 0.4) + getJumpSpeed()
                    setMotion(y, event)
                    speed *= 2.149
                } else if (gayStage == 3) {
                    speed = distance - 0.66 * (distance - getSpeed(slow.getValue()))
                } else {
                    if (mc.player!!.isOnGround && gayStage > 0) {
                        gayStage = if (1.35 * getSpeed(slow.getValue()) - 0.01 > speed) 0 else if (isMoving()) 1 else 0
                    }
                    speed = distance - distance / 159.0
                }
                speed = minOf(speed, getCap())
                speed = maxOf(speed, getSpeed(slow.getValue()))
                if (gayStage > 0) {
                    strafe(event, speed)
                }
                if (isMoving()) {
                    gayStage++
                }
            }
            Mode.Bhop -> {
                if (!lagTimer.passedMs(100L)) {
                    bhopStage = 4
                    return
                }
                if (MathUtil.round(mc.player!!.y - Math.floor(mc.player!!.y), 3) == MathUtil.round(0.138, 3)) {
                    mc.player!!.velocity = Vec3d(mc.player!!.velocity.x, mc.player!!.velocity.y - (0.08 + getJumpSpeed()), mc.player!!.velocity.z)
                    event.setY(event.getY() - (0.0931 + getJumpSpeed()))
                    mc.player!!.setPosition(mc.player!!.x, mc.player!!.y - (0.0931 + getJumpSpeed()), mc.player!!.z)
                }
                if (bhopStage == 2 && isMoving()) {
                    val y = (if (isBoxColliding()) 0.2 else 0.4) + getJumpSpeed()
                    setMotion(y, event)
                    speed *= 2.149
                } else if (bhopStage == 3) {
                    speed = distance - 0.66 * (distance - getSpeed(slow.getValue()))
                } else {
                    if (mc.player!!.isOnGround) {
                        bhopStage = 1
                    }
                    speed = distance - distance / 159.0
                }
                speed = minOf(speed, getCap())
                speed = maxOf(speed, getSpeed(slow.getValue()))
                strafe(event, speed)
                bhopStage++
            }
            Mode.VHop -> {
                if (!lagTimer.passedMs(100L)) {
                    vStage = 1
                    return
                }
                if (!isMoving()) {
                    speed = getSpeed(slow.getValue())
                }
                val yFrac = MathUtil.round(mc.player!!.y - Math.floor(mc.player!!.y), 3)
                if (yFrac == MathUtil.round(0.4, 3)) {
                    setMotion(0.31 + getJumpSpeed(), event)
                } else if (yFrac == MathUtil.round(0.71, 3)) {
                    setMotion(0.04 + getJumpSpeed(), event)
                } else if (yFrac == MathUtil.round(0.75, 3)) {
                    setMotion(-0.2 + getJumpSpeed(), event)
                }
                if (mc.world!!.getEntityCollisions(mc.player!!, mc.player!!.boundingBox.offset(0.0, -0.56, 0.0)).isNotEmpty() && yFrac == MathUtil.round(0.55, 3)) {
                    setMotion(-0.14 + getJumpSpeed(), event)
                }
                if (vStage == 1 && mc.player!!.verticalCollision && isMoving()) {
                    speed = 2.0 * getSpeed(slow.getValue()) - 0.01
                } else if (vStage == 2 && mc.player!!.verticalCollision && isMoving()) {
                    setMotion((if (isBoxColliding()) 0.2 else 0.4) + getJumpSpeed(), event)
                    speed *= 2.149
                } else if (vStage == 3) {
                    speed = distance - 0.66 * (distance - getSpeed(slow.getValue()))
                } else {
                    if (mc.player!!.isOnGround && vStage > 0) {
                        vStage = if (1.35 * getSpeed(slow.getValue()) - 0.01 > speed) 0 else if (isMoving()) 1 else 0
                    }
                    speed = distance - distance / 159.0
                }
                if (vStage > 8) {
                    speed = getSpeed(slow.getValue())
                }
                speed = minOf(speed, getCap())
                speed = maxOf(speed, getSpeed(slow.getValue()))
                if (vStage > 0) {
                    strafe(event, speed)
                }
                if (isMoving()) {
                    vStage++
                }
            }
            Mode.LowHop -> {
                if (!lagTimer.passedMs(100L)) {
                    return
                }
                if (useTimer.getValue() && lagTimer.passedMs(250L)) {
                    if (!timerActive) {
                        EntityUtil.setTimer(1.0888f)
                        timerActive = true
                    }
                } else if (timerActive) {
                    EntityUtil.resetTimer()
                    timerActive = false
                }
                if (!mc.player!!.horizontalCollision) {
                    val yFrac = MathUtil.round(mc.player!!.y - Math.floor(mc.player!!.y), 3)
                    if (yFrac == MathUtil.round(0.4, 3)) {
                        setMotion(0.31 + getJumpSpeed(), event)
                    } else if (yFrac == MathUtil.round(0.71, 3)) {
                        setMotion(0.04 + getJumpSpeed(), event)
                    } else if (yFrac == MathUtil.round(0.75, 3)) {
                        setMotion(-0.2 - getJumpSpeed(), event)
                    } else if (yFrac == MathUtil.round(0.55, 3)) {
                        setMotion(-0.14 + getJumpSpeed(), event)
                    } else if (yFrac == MathUtil.round(0.41, 3)) {
                        setMotion(-0.2 + getJumpSpeed(), event)
                    }
                }
                if (lowStage == 1 && isMoving()) {
                    speed = 1.35 * getSpeed(slow.getValue()) - 0.01
                } else if (lowStage == 2 && isMoving()) {
                    setMotion((if (isBoxColliding()) 0.2 else 0.3999) + getJumpSpeed(), event)
                    speed *= if (boost) 1.5685 else 1.3445
                } else if (lowStage == 3) {
                    speed = distance - 0.66 * (distance - getSpeed(slow.getValue()))
                    boost = !boost
                } else {
                    if (mc.player!!.isOnGround && lowStage > 0) {
                        lowStage = if (isMoving()) 1 else 0
                    }
                    speed = distance - distance / 159.0
                }
                speed = minOf(speed, getCap())
                speed = maxOf(speed, getSpeed(slow.getValue()))
                strafe(event, speed)
                if (isMoving()) {
                    lowStage++
                }
            }
            Mode.Constantiam -> {
                if (!lagTimer.passedMs(100L)) {
                    constStage = 0
                    return
                }
                if (!isMoving()) {
                    speed = getSpeed(slow.getValue())
                }
                if (constStage == 0 && isMoving() && mc.player!!.isOnGround) {
                    speed = 0.08
                } else if (constStage == 1 && mc.player!!.verticalCollision && isMoving()) {
                    speed = 0.25 + getSpeed(slow.getValue()) - 0.01
                } else if (constStage == 2 && mc.player!!.verticalCollision && isMoving()) {
                    val y = (if (isBoxColliding()) 0.2 else 0.4) + getJumpSpeed()
                    setMotion(y, event)
                    speed *= constFactor.getValue()
                } else if (constStage == 3) {
                    speed = distance - 0.66 * (distance - getSpeed(slow.getValue()))
                } else {
                    if (mc.player!!.isOnGround && constStage > 0) {
                        constStage = 0
                    }
                    if (!mc.player!!.isOnGround && constStage > constOff.getValue() && constStage < constTicks.getValue()) {
                        event.setY(if (mc.player!!.age % 2 == 0) 0.00118212 else -0.00118212)
                    }
                    speed = distance - distance / 159.0
                }
                speed = minOf(speed, getCap())
                if (constStage != 0) {
                    speed = maxOf(speed, getSpeed(slow.getValue()))
                }
                strafe(event, speed)
                if (isMoving()) {
                    constStage++
                }
            }
        }
    }

    private fun strafe(event: MoveEvent, speed: Double) {
        if (isMoving()) {
            val dir = MotionUtil.forward(speed)
            event.setX(dir[0])
            event.setZ(dir[1])
        } else {
            event.setX(0.0)
            event.setZ(0.0)
        }
        event.isCanceled = true
    }

    private fun setMotion(y: Double, event: MoveEvent) {
        event.setY(y)
        mc.player!!.velocity = Vec3d(mc.player!!.velocity.x, y, mc.player!!.velocity.z)
        event.isCanceled = true
    }

    private fun isMoving(): Boolean {
        return mc.player!!.input.movementForward != 0.0f || mc.player!!.input.movementSideways != 0.0f
    }

    private fun noMovementKeys(): Boolean {
        return mc.player!!.input.movementForward == 0.0f && mc.player!!.input.movementSideways == 0.0f
    }

    private fun isJumping(): Boolean {
        return mc.options.jumpKey.isPressed()
    }

    private fun inLiquid(): Boolean {
        return mc.player!!.isTouchingWater || mc.player!!.isInLava
    }

    private fun isBoxColliding(): Boolean {
        return mc.world!!.getEntityCollisions(mc.player!!, mc.player!!.boundingBox.offset(0.0, 0.21, 0.0)).isNotEmpty()
    }

    private fun notColliding(): Boolean {
        val collisions = mc.world!!.getEntityCollisions(mc.player!!, mc.player!!.boundingBox.expand(0.1, 0.0, 0.1))
        val stepping = Moon.moduleManager!!.isModuleEnabled(Step::class.java) && collisions.isNotEmpty()
        return mc.player!!.isOnGround && !stepping && !inLiquid()
    }

    private fun getDistance2D(): Double {
        val dx = mc.player!!.x - mc.player!!.prevX
        val dz = mc.player!!.z - mc.player!!.prevZ
        return sqrt(dx * dx + dz * dz)
    }

    private fun getJumpSpeed(): Double {
        val jump = mc.player!!.getStatusEffect(StatusEffects.JUMP_BOOST)
        return if (jump != null) (jump.amplifier + 1) * 0.1 else 0.0
    }

    private fun getSpeed(slow: Boolean): Double {
        return getSpeed(slow, 0.2873)
    }

    private fun getSpeed(slow: Boolean, default: Double): Double {
        var base = default
        val speedEffect = mc.player!!.getStatusEffect(StatusEffects.SPEED)
        if (speedEffect != null) {
            base *= 1.0 + 0.2 * (speedEffect.amplifier + 1)
        }
        if (slow) {
            val slowness = mc.player!!.getStatusEffect(StatusEffects.SLOWNESS)
            if (slowness != null) {
                base /= 1.0 + 0.2 * (slowness.amplifier + 1)
            }
        }
        return base
    }

    private fun isInMovementDirection(x: Double, y: Double, z: Double): Boolean {
        val v = mc.player!!.velocity
        if (v.x == 0.0 && v.z == 0.0) {
            return true
        }
        val movingX = mc.player!!.x + v.x * 10000.0
        val movingZ = mc.player!!.z + v.z * 10000.0
        val antiX = mc.player!!.x - v.x * 10000.0
        val antiZ = mc.player!!.z - v.y * 10000.0
        val dMoving = (movingX - x) * (movingX - x) + (movingZ - z) * (movingZ - z)
        val dAnti = (antiX - x) * (antiX - x) + (antiZ - z) * (antiZ - z)
        return dMoving < dAnti
    }

    private fun getCap(): Double {
        var ret = cap.getValue()
        if (!scaleCap.getValue()) {
            return ret
        }
        val p = mc.player ?: return ret
        val speedEffect = p.getStatusEffect(StatusEffects.SPEED)
        if (speedEffect != null) {
            ret *= 1.0 + 0.2 * (speedEffect.amplifier + 1)
        }
        if (slow.getValue()) {
            val slowness = p.getStatusEffect(StatusEffects.SLOWNESS)
            if (slowness != null) {
                ret /= 1.0 + 0.2 * (slowness.amplifier + 1)
            }
        }
        return ret
    }

    enum class Mode {
        Instant,
        OldGround,
        OnGround,
        Vanilla,
        NCP,
        Strafe,
        GayHop,
        Bhop,
        VHop,
        LowHop,
        Constantiam,
        None
    }
}
