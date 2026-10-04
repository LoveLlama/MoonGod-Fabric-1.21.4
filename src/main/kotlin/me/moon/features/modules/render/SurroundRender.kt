package me.moon.features.modules.render

import me.moon.event.events.Render3DEvent
import me.moon.features.modules.Module
import me.moon.features.setting.Setting
import me.moon.util.EntityUtil
import me.moon.util.RenderUtil
import net.minecraft.block.Blocks
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import java.awt.Color

class SurroundRender : Module("SurroundRender", "SurroundRender", Module.Category.RENDER, true, false, false) {
    var target: PlayerEntity? = null
    private val range = register(Setting("Range", 5, 1, 10))

    override fun onRender3D(event: Render3DEvent) {
        if (!fullNullCheck()) {
            this.target = this.getTarget(this.range.getValue().toDouble())
            this.surroundRender()
        }
    }

    private fun surroundRender() {
        if (this.target != null) {
            val a = this.target!!.pos
            val base = BlockPos.ofFloored(a)
            if (mc.world!!.getBlockState(base).block == Blocks.OBSIDIAN || mc.world!!.getBlockState(base).block == Blocks.ENDER_CHEST) {
                RenderUtil.drawBoxESP(base, Color(255, 255, 0), false, Color(255, 255, 0), 1.0f, false, true, 42, true)
            }
            if (EntityUtil.getSurroundWeakness(a, -1, 1)) this.surroundRender(a, -1.0, 0.0, 0.0, true)
            if (EntityUtil.getSurroundWeakness(a, -1, 2)) this.surroundRender(a, 1.0, 0.0, 0.0, true)
            if (EntityUtil.getSurroundWeakness(a, -1, 3)) this.surroundRender(a, 0.0, 0.0, -1.0, true)
            if (EntityUtil.getSurroundWeakness(a, -1, 4)) this.surroundRender(a, 0.0, 0.0, 1.0, true)
            if (EntityUtil.getSurroundWeakness(a, -1, 5)) this.surroundRender(a, -1.0, 0.0, 0.0, false)
            if (EntityUtil.getSurroundWeakness(a, -1, 6)) this.surroundRender(a, 1.0, 0.0, 0.0, false)
            if (EntityUtil.getSurroundWeakness(a, -1, 7)) this.surroundRender(a, 0.0, 0.0, -1.0, false)
            if (EntityUtil.getSurroundWeakness(a, -1, 8)) this.surroundRender(a, 0.0, 0.0, 1.0, false)
        }
    }

    private fun surroundRender(pos: Vec3d, x: Double, y: Double, z: Double, red: Boolean) {
        val position = BlockPos.ofFloored(pos).add(x.toInt(), y.toInt(), z.toInt())
        if (mc.world!!.getBlockState(position).block != Blocks.AIR && mc.world!!.getBlockState(position).block != Blocks.FIRE) {
            if (red) {
                RenderUtil.drawBoxESP(position, Color(255, 0, 0), false, Color(255, 0, 0), 1.0f, false, true, 42, true)
            } else {
                RenderUtil.drawBoxESP(position, Color(0, 0, 255), false, Color(0, 0, 255), 1.0f, false, true, 42, true)
            }
        }
    }

    private fun getTarget(range: Double): PlayerEntity? {
        var target: PlayerEntity? = null
        var distance = range
        for (player in mc.world!!.players) {
            if (!EntityUtil.isntValid(player, range) && EntityUtil.isInHole(player)) {
                if (target == null) {
                    target = player
                    distance = mc.player!!.squaredDistanceTo(player)
                } else if (mc.player!!.squaredDistanceTo(player) < distance) {
                    target = player
                    distance = mc.player!!.squaredDistanceTo(player)
                }
            }
        }
        return target
    }
}
