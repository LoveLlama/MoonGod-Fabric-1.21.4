package me.moon.features.modules.combat

import me.moon.Moon
import me.moon.event.SubscribeEvent
import me.moon.event.events.UpdateWalkingPlayerEvent
import me.moon.features.modules.Module
import me.moon.features.setting.Setting
import me.moon.util.EntityUtil
import me.moon.util.MathUtil
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.Hand

class Killaura : Module("Killaura", "Kills aura.", Module.Category.COMBAT, true, false, false) {
    @JvmField
    var target: Entity? = null
    private val range = register(Setting("Range", 6.0f, 0.1f, 6.0f))
    private val hitDelay = register(Setting("HitDelay", 1.0, 0.0, 1.0))
    private val rotate = register(Setting("Rotate", true))
    private val onlySharp = register(Setting("SwordOnly", true))
    var tpsSync = register(Setting("TpsSync", true))
    private val raytrace = register(Setting("Raytrace", 3.0, 0.0, 3.0))
    private val players = register(Setting("Players", true))
    private val mobs = register(Setting("Mobs", false))
    private val animals = register(Setting("Animals", false))
    private val vehicles = register(Setting("Entities", false))
    private val projectiles = register(Setting("Projectiles", false))

    init {
        setInstance()
    }

    private fun setInstance() {
        INSTANCE = this
    }

    override fun onTick() {
        if (!this.rotate.getValue()) {
            this.doKillaura()
        }
    }

    @SubscribeEvent
    fun onUpdateWalkingPlayerEvent(event: UpdateWalkingPlayerEvent) {
        if (event.stage == 0 && this.rotate.getValue()) {
            this.doKillaura()
        }
    }

    private fun doKillaura() {
        this.target = this.getTarget()
        if (!this.onlySharp.getValue() || EntityUtil.holdingWeapon(mc.player!!)) {
            val target = this.target
            if (target != null) {
                if (this.rotate.getValue()) {
                    Moon.rotationManager!!.lookAtEntity(target)
                }

                if (Auto32k.getInstance().isOn() && Auto32k.getInstance().smartHitXIN.getValue() && target is PlayerEntity) {
                    val player = target
                    if (player.health + player.absorptionAmount >= 28.0 && mc.player!!.getAttackCooldownProgress(0.0f) < 0.3f
                        || player.health + player.absorptionAmount <= 28.0 && mc.player!!.getAttackCooldownProgress(0.0f) < this.hitDelay.getValue().toFloat()
                    ) {
                        return
                    }
                } else if (mc.player!!.getAttackCooldownProgress(0.0f) < this.hitDelay.getValue().toFloat()) {
                    return
                }

                if (AsyncHit.getInstance().isOn()) {
                    AsyncHit.getInstance().enqueueAttack(target)
                } else {
                    EntityUtil.attackEntity(target, false, Hand.MAIN_HAND)
                }
            }
        }
    }

    private fun getTarget(): Entity? {
        var closestEntity: Entity? = null

        for (entity in mc.world!!.entities) {
            if ((this.players.getValue() && entity is PlayerEntity
                    || this.mobs.getValue() && EntityUtil.isMobAggressive(entity)
                    || this.animals.getValue() && EntityUtil.isPassive(entity)
                    || this.vehicles.getValue() && EntityUtil.isVehicle(entity)
                    || this.projectiles.getValue() && EntityUtil.isProjectile(entity))
                && (entity !is LivingEntity || !EntityUtil.isntValid(entity, this.range.getValue().toDouble()))
                && (mc.player!!.canSee(entity) || mc.player!!.squaredDistanceTo(entity) <= MathUtil.square(this.raytrace.getValue().toDouble()))
            ) {
                if (closestEntity == null) {
                    closestEntity = entity
                } else if (closestEntity!!.distanceTo(mc.player!!) > entity.distanceTo(mc.player!!)) {
                    closestEntity = entity
                }
            }
        }

        return closestEntity
    }

    override fun getDisplayInfo(): String? {
        return if (this.target is PlayerEntity) this.target!!.name.string else null
    }

    companion object {
        @JvmField
        var INSTANCE: Killaura = Killaura()

        @JvmStatic
        fun getInstance(): Killaura = INSTANCE
    }
}
