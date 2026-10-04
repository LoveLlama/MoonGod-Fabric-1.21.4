package me.moon.features.modules.render

import me.moon.event.events.Render3DEvent
import me.moon.features.modules.Module
import me.moon.features.setting.Setting
import me.moon.util.EntityUtil
import me.moon.util.RenderUtil
import net.minecraft.entity.projectile.thrown.EnderPearlEntity
import net.minecraft.item.ItemStack
import net.minecraft.entity.ItemEntity
import net.minecraft.util.math.Box
import java.awt.Color

class ESP : Module("ESP", "Renders a nice ESP.", Module.Category.RENDER, false, false, false) {
    private val items = register(Setting("Items", false))
    private val pearl = register(Setting("Pearls", false))
    private val red = register(Setting("Red", 255, 0, 255))
    private val green = register(Setting("Green", 255, 0, 255))
    private val blue = register(Setting("Blue", 255, 0, 255))
    private val boxAlpha = register(Setting("BoxAlpha", 120, 0, 255))
    private val alpha = register(Setting("Alpha", 255, 0, 255))

    init { setInstance() }
    private fun setInstance() { INSTANCE = this }

    override fun onRender3D(event: Render3DEvent) {
        if (fullNullCheck()) return
        if (this.items.getValue()) {
            var i = 0
            for (entity in mc.world!!.entities) {
                if (entity is ItemEntity && mc.player!!.squaredDistanceTo(entity) < 2500.0) {
                    val interp = EntityUtil.getInterpolatedRenderPos(entity, event.partialTicks)
                    val bb = Box(
                        entity.boundingBox.minX - 0.05 - entity.x + interp.x,
                        entity.boundingBox.minY - 0.0 - entity.y + interp.y,
                        entity.boundingBox.minZ - 0.05 - entity.z + interp.z,
                        entity.boundingBox.maxX + 0.05 - entity.x + interp.x,
                        entity.boundingBox.maxY + 0.1 - entity.y + interp.y,
                        entity.boundingBox.maxZ + 0.05 - entity.z + interp.z
                    )
                    RenderUtil.drawColorBox(bb, this.red.getValue() / 255.0f, this.green.getValue() / 255.0f, this.blue.getValue() / 255.0f, this.alpha.getValue() / 255.0f)
                    if (++i >= 50) break
                }
            }
        }

        if (this.pearl.getValue()) {
            var i = 0
            for (entity in mc.world!!.entities) {
                if (entity is EnderPearlEntity && mc.player!!.squaredDistanceTo(entity) < 2500.0) {
                    val interp = EntityUtil.getInterpolatedRenderPos(entity, event.partialTicks)
                    val bb = Box(
                        entity.boundingBox.minX - 0.05 - entity.x + interp.x,
                        entity.boundingBox.minY - 0.0 - entity.y + interp.y,
                        entity.boundingBox.minZ - 0.05 - entity.z + interp.z,
                        entity.boundingBox.maxX + 0.05 - entity.x + interp.x,
                        entity.boundingBox.maxY + 0.1 - entity.y + interp.y,
                        entity.boundingBox.maxZ + 0.05 - entity.z + interp.z
                    )
                    RenderUtil.drawColorBox(bb, this.red.getValue() / 255.0f, this.green.getValue() / 255.0f, this.blue.getValue() / 255.0f, this.alpha.getValue() / 255.0f)
                    if (++i >= 50) break
                }
            }
        }
    }

    companion object {
        @JvmField
        var INSTANCE: ESP = ESP()

        @JvmStatic
        fun getInstance(): ESP = INSTANCE
    }
}
