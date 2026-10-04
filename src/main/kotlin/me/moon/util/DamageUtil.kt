package me.moon.util

import me.moon.util.Util
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import net.minecraft.world.RaycastContext
import kotlin.math.sqrt

object DamageUtil : Util {
    @JvmStatic
    fun calculateDamage(posX: Double, posY: Double, posZ: Double, entity: Entity): Float {
        var finalDamage = 1.0f

        try {
            val doubleExplosionSize = 12.0f
            val ddx = entity.x - posX
            val ddy = entity.y - posY
            val ddz = entity.z - posZ
            val distancedSize = sqrt(ddx * ddx + ddy * ddy + ddz * ddz) / doubleExplosionSize.toDouble()
            val blockDensity = getBlockDensity(Vec3d(posX, posY, posZ), entity.boundingBox)
            val v = (1.0 - distancedSize) * blockDensity
            val damage = ((v * v + v) / 2.0 * 7.0 * doubleExplosionSize.toDouble() + 1.0).toInt().toFloat()
            if (entity is LivingEntity) {
                finalDamage = getBlastReduction(entity, getDamageMultiplied(damage))
            }
        } catch (var16: NullPointerException) {
        }

        return finalDamage
    }

    private fun getBlockDensity(center: Vec3d, box: Box): Double {
        val box2 = box.contract(0.25)
        var visible = 0
        var count = 0
        for (ax in 0..1) {
            for (ay in 0..1) {
                for (az in 0..1) {
                    val px = if (ax == 0) box2.minX else box2.maxX
                    val py = if (ay == 0) box2.minY else box2.maxY
                    val pz = if (az == 0) box2.minZ else box2.maxZ
                    count++
                    val hit = mc.world!!.raycast(
                        RaycastContext(
                            center, Vec3d(px, py, pz),
                            RaycastContext.ShapeType.COLLIDER,
                            RaycastContext.FluidHandling.NONE,
                            mc.player ?: centerAsEntity()
                        )
                    )
                    if (hit == null || hit.type == net.minecraft.util.hit.HitResult.Type.MISS) {
                        visible++
                    }
                }
            }
        }
        return visible.toDouble() / count
    }

    private fun centerAsEntity(): Entity {
        return mc.cameraEntity ?: mc.player!!
    }

    @JvmStatic
    fun getBlastReduction(entity: LivingEntity, damage: Float): Float {
        val armor = entity.armor.toFloat()
        val toughness = entity.getAttributeValue(EntityAttributes.ARMOR_TOUGHNESS).toFloat()
        val divisor = 2.0f + toughness / 4.0f
        val absorbed = (armor - damage / divisor).coerceIn(armor * 0.2f, 20.0f)
        var dmg = damage * (1.0f - absorbed / 25.0f)
        if (entity.hasStatusEffect(StatusEffects.RESISTANCE)) {
            dmg -= dmg / 4.0f
        }
        return Math.max(dmg, 0.0f)
    }

    private fun getDamageMultiplied(damage: Float): Float {
        val diff = mc.world!!.difficulty.ordinal
        return damage * (if (diff == 0) 0.0f else if (diff == 2) 1.0f else if (diff == 1) 0.5f else 1.5f)
    }
}
