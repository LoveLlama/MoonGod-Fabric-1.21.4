package me.moon.features.modules.combat

import me.moon.features.modules.Module
import me.moon.features.setting.Setting
import me.moon.util.MathUtil
import net.minecraft.entity.Entity
import net.minecraft.entity.decoration.EndCrystalEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.Hand
import net.minecraft.util.math.Vec3d
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class AsyncHit : Module("AsyncHit", "Asynchronously attacks targets and crystals", Module.Category.COMBAT, true, false, false) {
    private val delay = register(Setting("Delay", 300, 0, 1000))
    private val range = register(Setting("Range", 5.5f, 0.0f, 6.0f))
    private val crystals = register(Setting("Crystals", true))
    private val players = register(Setting("Players", true))
    private val predictTicks = register(Setting("PredictTicks", 2, 0, 10))
    private val cooldown = register(Setting("Cooldown", 0.9f, 0.0f, 1.0f))

    @Volatile
    private var snapshot: Array<Snap> = emptyArray()
    @Volatile
    private var eye: Vec3d? = null
    private val queue = ConcurrentLinkedQueue<Int>()
    private var executor: ScheduledExecutorService? = null

    init {
        setInstance()
    }

    private fun setInstance() {
        INSTANCE = this
    }

    override fun onEnable() {
        val exec = Executors.newSingleThreadScheduledExecutor { r ->
            Thread(r, "Moon-AsyncHit").apply { isDaemon = true }
        }
        executor = exec
        exec.scheduleAtFixedRate({ scan() }, 0L, this.delay.getValue().toLong().coerceAtLeast(1L), TimeUnit.MILLISECONDS)
    }

    override fun onDisable() {
        executor?.shutdownNow()
        executor = null
        queue.clear()
        snapshot = emptyArray()
        eye = null
    }

    override fun onTick() {
        if (mc.world == null || mc.player == null) {
            snapshot = emptyArray()
            eye = null
            return
        }
        eye = mc.player!!.getCameraPosVec(mc.renderTickCounter.getTickDelta(true))
        val list = ArrayList<Snap>()
        for (e in mc.world!!.entities) {
            if (e === mc.player || e.isRemoved) {
                continue
            }
            if (this.crystals.getValue() && e is EndCrystalEntity) {
                list.add(Snap(e.id, e.x, e.y, e.z))
            } else if (this.players.getValue() && e is PlayerEntity && !e.isCreative && e.isAlive) {
                list.add(Snap(e.id, e.x, e.y, e.z))
            }
        }
        snapshot = list.toTypedArray()
    }

    fun enqueueAttack(entity: Entity) {
        queue.add(entity.id)
    }

    private fun scan() {
        try {
            var id = queue.poll()
            if (id == null) {
                id = pickBest() ?: return
            }
            val target = id
            mc.execute { attack(target) }
        } catch (ignored: Exception) {
        }
    }

    private fun pickBest(): Int? {
        val snap = snapshot
        val eyePos = eye
        if (snap.isEmpty() || eyePos == null) {
            return null
        }
        val maxDist = MathUtil.square(this.range.getValue().toDouble())
        var best: Snap? = null
        var bestScore = Double.MAX_VALUE
        for (s in snap) {
            val dist = eyePos.squaredDistanceTo(s.x, s.y, s.z)
            if (dist > maxDist) {
                continue
            }
            val angle = MathUtil.calcAngle(eyePos, Vec3d(s.x, s.y, s.z))
            val score = Math.abs(angle[0].toDouble()) + Math.abs(angle[1].toDouble()) * 2.0 + Math.sqrt(dist)
            if (score < bestScore) {
                bestScore = score
                best = s
            }
        }
        return best?.id
    }

    private fun attack(id: Int) {
        try {
            if (mc.world == null || mc.player == null || mc.interactionManager == null) {
                return
            }
            val entity = mc.world!!.getEntityById(id) ?: return
            if (entity.isRemoved) {
                return
            }
            val eye = mc.player!!.getCameraPosVec(1.0f)
            if (eye.squaredDistanceTo(entity.x, entity.y, entity.z) > MathUtil.square(this.range.getValue().toDouble())) {
                return
            }
            if (mc.player!!.getAttackCooldownProgress(0.0f) < this.cooldown.getValue()) {
                return
            }
            mc.interactionManager!!.attackEntity(mc.player!!, entity)
            mc.player!!.swingHand(Hand.MAIN_HAND)
        } catch (ignored: Exception) {
        }
    }

    private class Snap(val id: Int, val x: Double, val y: Double, val z: Double)

    companion object {
        @JvmField
        var INSTANCE: AsyncHit = AsyncHit()

        @JvmStatic
        fun getInstance(): AsyncHit = INSTANCE
    }
}
