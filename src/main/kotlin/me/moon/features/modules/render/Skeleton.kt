package me.moon.features.modules.render

import me.moon.Moon
import me.moon.event.events.Render3DEvent
import me.moon.features.modules.Module
import me.moon.features.setting.Setting
import me.moon.util.BlockUtil
import me.moon.util.EntityUtil
import me.moon.util.RenderUtil
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import java.awt.Color

class Skeleton : Module("Skeleton", "Draws a skeleton inside the player.", Module.Category.RENDER, false, false, false) {
    private val entities: MutableMap<PlayerEntity, Array<FloatArray>> = HashMap()
    private val lineWidth = register(Setting("LineWidth", 1.0f, 0.1f, 5.0f))
    private val alpha = register(Setting("Alpha", 255, 0, 255))
    private val invisibles = register(Setting("Invisibles", false))

    fun addEntity(e: PlayerEntity, rot: Array<FloatArray>) {
        entities[e] = rot
    }

    private fun getVec3(event: Render3DEvent, e: PlayerEntity): Vec3d {
        val pt = event.partialTicks
        val x = e.prevX + (e.x - e.prevX) * pt.toDouble()
        val y = e.prevY + (e.y - e.prevY) * pt.toDouble()
        val z = e.prevZ + (e.z - e.prevZ) * pt.toDouble()
        return Vec3d(x, y, z)
    }

    override fun onRender3D(event: Render3DEvent) {
        if (!fullNullCheck()) {
            entities.keys.removeIf { it !in mc.world!!.players }
            for (e in mc.world!!.players) {
                this.drawSkeleton(event, e)
            }
        }
    }

    private fun drawSkeleton(event: Render3DEvent, e: PlayerEntity) {
        if (BlockUtil.isPosInFov(BlockPos.ofFloored(e.pos))) {
            if (!e.isInvisible || this.invisibles.getValue()) {
                val entPos = entities[e]
                if (entPos != null && e.isAlive && e !in listOf(mc.player!!)) {
                    val color = if (Moon.friendManager!!.isFriend(e)) Color(0, 191, 230, this.alpha.getValue())
                    else Color(255, 0, 0, this.alpha.getValue())
                    val vec = this.getVec3(event, e)
                    val yOff = if (e.isSneaking) 0.6f else 0.75f
                    val shoulder = Vec3d(vec.x, vec.y + yOff + 0.55, vec.z)
                    val hips = Vec3d(vec.x, vec.y + yOff, vec.z)
                    RenderUtil.drawColoredBoundingBox(lineBox(hips, shoulder), this.lineWidth.getValue(), color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)
                    RenderUtil.drawColoredBoundingBox(lineBox(Vec3d(vec.x - 0.375, shoulder.y, vec.z), Vec3d(vec.x + 0.375, shoulder.y, vec.z)), this.lineWidth.getValue(), color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)
                    RenderUtil.drawColoredBoundingBox(lineBox(shoulder, Vec3d(shoulder.x, shoulder.y + 0.3, shoulder.z)), this.lineWidth.getValue(), color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)
                    RenderUtil.drawColoredBoundingBox(lineBox(Vec3d(vec.x - 0.125, hips.y, vec.z), Vec3d(vec.x + 0.125, hips.y, vec.z)), this.lineWidth.getValue(), color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)
                    RenderUtil.drawColoredBoundingBox(lineBox(Vec3d(vec.x - 0.125, hips.y, vec.z), Vec3d(vec.x - 0.125, hips.y - yOff, vec.z)), this.lineWidth.getValue(), color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)
                    RenderUtil.drawColoredBoundingBox(lineBox(Vec3d(vec.x + 0.125, hips.y, vec.z), Vec3d(vec.x + 0.125, hips.y - yOff, vec.z)), this.lineWidth.getValue(), color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)
                    RenderUtil.drawColoredBoundingBox(lineBox(Vec3d(vec.x - 0.375, shoulder.y, vec.z), Vec3d(vec.x - 0.375, shoulder.y - 0.5, vec.z)), this.lineWidth.getValue(), color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)
                    RenderUtil.drawColoredBoundingBox(lineBox(Vec3d(vec.x + 0.375, shoulder.y, vec.z), Vec3d(vec.x + 0.375, shoulder.y - 0.5, vec.z)), this.lineWidth.getValue(), color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)
                }
            }
        }
    }

    private fun doesntContain(entityPlayer: PlayerEntity): Boolean {
        return !mc.world!!.players.contains(entityPlayer)
    }

    private fun lineBox(a: Vec3d, b: Vec3d): Box {
        return Box(
            minOf(a.x, b.x) - 0.01, minOf(a.y, b.y) - 0.01, minOf(a.z, b.z) - 0.01,
            maxOf(a.x, b.x) + 0.01, maxOf(a.y, b.y) + 0.01, maxOf(a.z, b.z) + 0.01
        )
    }
}
