package me.moon.manager

import me.moon.features.Feature
import me.moon.util.MathUtil
import me.moon.util.RotationUtil
import net.minecraft.entity.Entity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d

class RotationManager : Feature() {
    private var yaw = 0.0f
    private var pitch = 0.0f

    fun updateRotations() {
        yaw = mc.player!!.yaw
        pitch = mc.player!!.pitch
    }

    fun restoreRotations() {
        mc.player!!.yaw = yaw
        mc.player!!.headYaw = yaw
        mc.player!!.pitch = pitch
    }

    fun setPlayerRotations(yaw: Float, pitch: Float) {
        mc.player!!.yaw = yaw
        mc.player!!.headYaw = yaw
        mc.player!!.pitch = pitch
    }

    fun setPlayerYaw(yaw: Float) {
        mc.player!!.yaw = yaw
        mc.player!!.headYaw = yaw
    }

    fun lookAtPos(pos: BlockPos) {
        val angle = MathUtil.calcAngle(
            mc.player!!.getCameraPosVec(mc.renderTickCounter.getTickDelta(false)),
            Vec3d(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5)
        )
        setPlayerRotations(angle[0], angle[1])
    }

    fun lookAtVec3d(vec3d: Vec3d) {
        val angle = MathUtil.calcAngle(
            mc.player!!.getCameraPosVec(mc.renderTickCounter.getTickDelta(false)),
            Vec3d(vec3d.x, vec3d.y, vec3d.z)
        )
        setPlayerRotations(angle[0], angle[1])
    }

    fun lookAtVec3d(x: Double, y: Double, z: Double) {
        val vec3d = Vec3d(x, y, z)
        lookAtVec3d(vec3d)
    }

    fun lookAtEntity(entity: Entity) {
        val angle = MathUtil.calcAngle(mc.player!!.getCameraPosVec(mc.renderTickCounter.getTickDelta(false)), entity.getCameraPosVec(mc.renderTickCounter.getTickDelta(false)))
        setPlayerRotations(angle[0], angle[1])
    }

    fun setPlayerPitch(pitch: Float) {
        mc.player!!.pitch = pitch
    }

    fun getYaw(): Float = yaw

    fun setYaw(yaw: Float) {
        this.yaw = yaw
    }

    fun getPitch(): Float = pitch

    fun setPitch(pitch: Float) {
        this.pitch = pitch
    }

    fun getDirection4D(): Int {
        return RotationUtil.getDirection4D()
    }

    fun getDirection4D(northRed: Boolean): String {
        return RotationUtil.getDirection4D(northRed)
    }
}
