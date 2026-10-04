package me.moon.features.modules.render

import me.moon.event.events.Render3DEvent
import me.moon.features.modules.Module
import me.moon.features.modules.client.ClickGui
import me.moon.features.setting.Setting
import me.moon.util.ColorUtil
import me.moon.util.RenderUtil
import net.minecraft.util.hit.HitResult
import java.awt.Color

class BlockHighlight : Module("BlockHighlight", "Highlights the block u look at.", Module.Category.RENDER, false, false, false) {
    private val lineWidth = register(Setting("LineWidth", 1.0f, 0.1f, 5.0f))
    private val cAlpha = register(Setting("Alpha", 255, 0, 255))

    override fun onRender3D(event: Render3DEvent) {
        val ray = mc.crosshairTarget
        if (ray != null && ray.type == HitResult.Type.BLOCK) {
            val blockpos = (ray as net.minecraft.util.hit.BlockHitResult).blockPos
            RenderUtil.drawBlockOutline(
                blockpos,
                if (ClickGui.getInstance().rainbow.getValue()) ColorUtil.rainbow(ClickGui.getInstance().rainbowHue.getValue())
                else Color(
                    ClickGui.getInstance().red.getValue(),
                    ClickGui.getInstance().green.getValue(),
                    ClickGui.getInstance().blue.getValue(),
                    this.cAlpha.getValue()
                ),
                this.lineWidth.getValue(),
                false
            )
        }
    }
}
