package me.moon.util

import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Calendar
import java.util.Comparator
import java.util.LinkedHashMap
import java.util.LinkedList
import java.util.Random

object MathUtil : Util {
    private val random = Random()

    @JvmStatic
    fun getRandom(min: Int, max: Int): Int {
        return min + random.nextInt(max - min + 1)
    }

    @JvmStatic
    fun getRandom(min: Double, max: Double): Double {
        return MathHelper.clamp(min + random.nextDouble() * max, min, max)
    }

    @JvmStatic
    fun getRandom(min: Float, max: Float): Float {
        return MathHelper.clamp(min + random.nextFloat() * max, min, max)
    }

    @JvmStatic
    fun clamp(num: Int, min: Int, max: Int): Int {
        return if (num < min) min else Math.min(num, max)
    }

    @JvmStatic
    fun clamp(num: Float, min: Float, max: Float): Float {
        return if (num < min) min else Math.min(num, max)
    }

    @JvmStatic
    fun clamp(num: Double, min: Double, max: Double): Double {
        return if (num < min) min else Math.min(num, max)
    }

    @JvmStatic
    fun sin(value: Float): Float {
        return MathHelper.sin(value)
    }

    @JvmStatic
    fun cos(value: Float): Float {
        return MathHelper.cos(value)
    }

    @JvmStatic
    fun wrapDegrees(value: Float): Float {
        return MathHelper.wrapDegrees(value)
    }

    @JvmStatic
    fun wrapDegrees(value: Double): Double {
        return MathHelper.wrapDegrees(value)
    }

    @JvmStatic
    fun roundVec(vec3d: Vec3d, places: Int): Vec3d {
        return Vec3d(round(vec3d.x, places), round(vec3d.y, places), round(vec3d.z, places))
    }

    @JvmStatic
    fun square(input: Double): Double {
        return input * input
    }

    @JvmStatic
    fun round(value: Double, places: Int): Double {
        if (places < 0) {
            throw IllegalArgumentException()
        } else {
            var bd = BigDecimal.valueOf(value)
            bd = bd.setScale(places, RoundingMode.FLOOR)
            return bd.toDouble()
        }
    }

    @JvmStatic
    fun wrap(valI: Float): Float {
        var v = valI.toDouble() % 360.0
        if (v >= 180.0) {
            v -= 360.0
        }

        if (v < -180.0) {
            v += 360.0
        }

        return v.toFloat()
    }

    @JvmStatic
    fun direction(yaw: Float): Vec3d {
        return Vec3d(
            Math.cos(degToRad((yaw + 90.0f).toDouble())),
            0.0,
            Math.sin(degToRad((yaw + 90.0f).toDouble()))
        )
    }

    @JvmStatic
    fun round(value: Float, places: Int): Float {
        if (places < 0) {
            throw IllegalArgumentException()
        } else {
            var bd = BigDecimal.valueOf(value.toDouble())
            bd = bd.setScale(places, RoundingMode.FLOOR)
            return bd.toFloat()
        }
    }

    @JvmStatic
    fun <K, V : Comparable<V>> sortByValue(map: Map<K, V>, descending: Boolean): Map<K, V> {
        val list = LinkedList(map.entries.toList())
        if (descending) {
            list.sortWith(Comparator { a, b -> b.value.compareTo(a.value) })
        } else {
            list.sortWith(Comparator { a, b -> a.value.compareTo(b.value) })
        }

        val result = LinkedHashMap<K, V>()

        for (entry in list) {
            result[entry.key] = entry.value
        }

        return result
    }

    @JvmStatic
    fun getTimeOfDay(): String {
        val c = Calendar.getInstance()
        val timeOfDay = c[11]
        if (timeOfDay < 12) {
            return "Good Morning "
        } else if (timeOfDay < 16) {
            return "Good Afternoon "
        } else {
            return if (timeOfDay < 21) "Good Evening " else "Good Night "
        }
    }

    @JvmStatic
    fun radToDeg(rad: Double): Double {
        return rad * (180.0 / Math.PI)
    }

    @JvmStatic
    fun degToRad(deg: Double): Double {
        return deg * (Math.PI / 180.0)
    }

    @JvmStatic
    fun getIncremental(value: Double, inc: Double): Double {
        val one = 1.0 / inc
        return Math.round(value * one) / one
    }

    @JvmStatic
    fun directionSpeed(speed: Double): DoubleArray {
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

    @JvmStatic
    fun getBlockBlocks(entity: Entity): MutableList<Vec3d> {
        val vec3ds = ArrayList<Vec3d>()
        val bb = entity.boundingBox
        val y = entity.y
        val minX = round(bb.minX, 0)
        val minZ = round(bb.minZ, 0)
        val maxX = round(bb.maxX, 0)
        val maxZ = round(bb.maxZ, 0)
        if (minX != maxX) {
            vec3ds.add(Vec3d(minX, y, minZ))
            vec3ds.add(Vec3d(maxX, y, minZ))
            if (minZ != maxZ) {
                vec3ds.add(Vec3d(minX, y, maxZ))
                vec3ds.add(Vec3d(maxX, y, maxZ))
                return vec3ds
            }
        } else if (minZ != maxZ) {
            vec3ds.add(Vec3d(minX, y, minZ))
            vec3ds.add(Vec3d(minX, y, maxZ))
            return vec3ds
        }

        vec3ds.add(entity.pos)
        return vec3ds
    }

    @JvmStatic
    fun calculateLine(x1: Vec3d, x2: Vec3d, distance: Double): Vec3d {
        val dx = x2.x - x1.x
        val dy = x2.y - x1.y
        val dz = x2.z - x1.z
        val length = Math.hypot(Math.hypot(dx, dy), dz)
        if (length == 0.0) return x1
        val unitSlopeX = dx / length
        val unitSlopeY = dy / length
        val unitSlopeZ = dz / length
        val x = x1.x + unitSlopeX * distance
        val y = x1.y + unitSlopeY * distance
        val z = x1.z + unitSlopeZ * distance
        return Vec3d(x, y, z)
    }

    @JvmStatic
    fun multiply(one: Double): Double {
        return one * one
    }

    @JvmStatic
    fun extrapolatePlayerPosition(player: PlayerEntity, ticks: Int): Vec3d {
        val lastPos = Vec3d(player.prevX, player.prevY, player.prevZ)
        val currentPos = Vec3d(player.x, player.y, player.z)
        val velocity = player.velocity
        val distance = multiply(velocity.x) + multiply(velocity.y) + multiply(velocity.z)
        val tempVec = calculateLine(lastPos, currentPos, distance * ticks.toDouble())
        return Vec3d(tempVec.x, player.y, tempVec.z)
    }

    @JvmStatic
    fun areVec3dsAligned(vec3d1: Vec3d, vec3d2: Vec3d): Boolean {
        return areVec3dsAlignedRetarded(vec3d1, vec3d2)
    }

    @JvmStatic
    fun areVec3dsAlignedRetarded(vec3d1: Vec3d, vec3d2: Vec3d): Boolean {
        val pos1 = BlockPos(Math.floor(vec3d1.x).toInt(), Math.floor(vec3d1.y).toInt(), Math.floor(vec3d1.z).toInt())
        val pos2 = BlockPos(vec3d2.x.toInt(), vec3d1.y.toInt(), vec3d2.z.toInt())
        return pos1 == pos2
    }

    @JvmStatic
    fun calcAngle(from: Vec3d, to: Vec3d): FloatArray {
        val difX = to.x - from.x
        val difY = (to.y - from.y) * -1.0
        val difZ = to.z - from.z
        val dist = Math.hypot(difX, difZ)
        return floatArrayOf(
            MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(difZ, difX)) - 90.0).toFloat(),
            MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(difY, dist))).toFloat()
        )
    }

    @JvmStatic
    fun interpolateEntity(entity: Entity, time: Float): Vec3d {
        return Vec3d(
            entity.prevX + (entity.x - entity.prevX) * time.toDouble(),
            entity.prevY + (entity.y - entity.prevY) * time.toDouble(),
            entity.prevZ + (entity.z - entity.prevZ) * time.toDouble()
        )
    }

    @JvmStatic
    fun kahanSum(values: DoubleArray): Double {
        var sum = 0.0
        var c = 0.0
        for (v in values) {
            val y = v - c
            val t = sum + y
            c = (t - sum) - y
            sum = t
        }
        return sum
    }

    @JvmStatic
    fun hypot(x: Double, y: Double, z: Double): Double {
        return Math.hypot(Math.hypot(x, y), z)
    }

    @JvmStatic
    fun lerpAngle(from: Float, to: Float, t: Float): Float {
        val delta = MathHelper.wrapDegrees(to - from)
        return MathHelper.wrapDegrees(from + delta * t)
    }

    @JvmStatic
    fun smoothstep(edge0: Float, edge1: Float, x: Float): Float {
        val t = MathHelper.clamp((x - edge0) / (edge1 - edge0), 0.0f, 1.0f)
        return t * t * (3.0f - 2.0f * t)
    }
}
