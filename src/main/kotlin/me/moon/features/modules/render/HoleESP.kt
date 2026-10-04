package me.moon.features.modules.render

import me.moon.event.events.Render3DEvent
import me.moon.features.modules.Module
import me.moon.features.setting.Setting
import me.moon.util.BlockUtil
import me.moon.util.RenderUtil
import net.minecraft.block.Blocks
import net.minecraft.util.math.BlockPos
import java.awt.Color
import java.util.function.Predicate

class HoleESP : Module("HoleESP", "Shows safe spots.", Module.Category.RENDER, false, false, false) {
    private val range = register(Setting("RangeX", 10, 0, 10))
    private val rangeY = register(Setting("RangeY", 10, 0, 10))
    private val red = register(Setting("Red", 200, 0, 255))
    private val green = register(Setting("Green", 50, 0, 255))
    private val blue = register(Setting("Blue", 200, 0, 255))
    private val alpha = register(Setting("Alpha", 200, 0, 255))
    private val boxAlpha = register(Setting("BoxAlpha", 100, 0, 255))
    private val lineWidth = register(Setting("LineWidth", 1.0f, 0.1f, 5.0f))
    private val safeRed = register(Setting("BedrockRed", 255, 0, 255))
    private val safeGreen = register(Setting("BedrockGreen", 255, 0, 255))
    private val safeBlue = register(Setting("BedrockBlue", 255, 0, 255))
    private val safeAlpha = register(Setting("BedrockAlpha", 200, 0, 255))
    var future = register(Setting("FutureRender", true))
    var fov = register(Setting("InFov", true))
    var renderOwn = register(Setting("RenderOwn", true))
    var box = register(Setting("Box", true))
    var outline = register(Setting("Outline", true))
    private val cRed = register(Setting("OL-Red", 200, 0, 255, Predicate { outline.getValue() }))
    private val cGreen = register(Setting("OL-Green", 50, 0, 255, Predicate { outline.getValue() }))
    private val cBlue = register(Setting("OL-Blue", 200, 0, 255, Predicate { outline.getValue() }))
    private val cAlpha = register(Setting("OL-Alpha", 200, 0, 255, Predicate { outline.getValue() }))
    private val safecRed = register(Setting("OL-BedrockRed", 255, 0, 255, Predicate { outline.getValue() }))
    private val safecGreen = register(Setting("OL-BedrockGreen", 255, 0, 255, Predicate { outline.getValue() }))
    private val safecBlue = register(Setting("OL-BedrockBlue", 255, 0, 255, Predicate { outline.getValue() }))
    private val safecAlpha = register(Setting("OL-BedrockAlpha", 200, 0, 255, Predicate { outline.getValue() }))

    init { setInstance() }
    private fun setInstance() { INSTANCE = this }

    override fun onRender3D(event: Render3DEvent) {
        if (fullNullCheck()) return
        val playerPos = mc.player!!.blockPos

        var x = playerPos.x - this.range.getValue()
        while (x < playerPos.x + this.range.getValue()) {
            var z = playerPos.z - this.range.getValue()
            while (z < playerPos.z + this.range.getValue()) {
                var y = playerPos.y + this.rangeY.getValue()
                while (y > playerPos.y - this.rangeY.getValue()) {
                    val pos = BlockPos.ofFloored(x.toDouble(), y.toDouble(), z.toDouble())
                    if (mc.world!!.getBlockState(pos).block == Blocks.AIR
                        && mc.world!!.getBlockState(pos.up()).block == Blocks.AIR
                        && mc.world!!.getBlockState(pos.up(2)).block == Blocks.AIR
                        && (pos != mc.player!!.blockPos || this.renderOwn.getValue())
                        && (BlockUtil.isPosInFov(pos) || !this.fov.getValue())
                    ) {
                        if (mc.world!!.getBlockState(pos.north()).block == Blocks.BEDROCK
                            && mc.world!!.getBlockState(pos.east()).block == Blocks.BEDROCK
                            && mc.world!!.getBlockState(pos.west()).block == Blocks.BEDROCK
                            && mc.world!!.getBlockState(pos.south()).block == Blocks.BEDROCK
                            && mc.world!!.getBlockState(pos.down()).block == Blocks.BEDROCK
                        ) {
                            RenderUtil.drawBoxESP(
                                if (this.future.getValue()) pos.down() else pos,
                                Color(this.safeRed.getValue(), this.safeGreen.getValue(), this.safeBlue.getValue(), this.safeAlpha.getValue()),
                                this.outline.getValue(),
                                Color(this.safecRed.getValue(), this.safecGreen.getValue(), this.safecBlue.getValue(), this.safecAlpha.getValue()),
                                this.lineWidth.getValue(),
                                this.outline.getValue(),
                                this.box.getValue(),
                                this.boxAlpha.getValue(),
                                true
                            )
                        } else if (BlockUtil.isBlockUnSafe(mc.world!!.getBlockState(pos.down()).block)
                            && BlockUtil.isBlockUnSafe(mc.world!!.getBlockState(pos.east()).block)
                            && BlockUtil.isBlockUnSafe(mc.world!!.getBlockState(pos.west()).block)
                            && BlockUtil.isBlockUnSafe(mc.world!!.getBlockState(pos.south()).block)
                            && BlockUtil.isBlockUnSafe(mc.world!!.getBlockState(pos.north()).block)
                        ) {
                            RenderUtil.drawBoxESP(
                                if (this.future.getValue()) pos.down() else pos,
                                Color(this.red.getValue(), this.green.getValue(), this.blue.getValue(), this.alpha.getValue()),
                                this.outline.getValue(),
                                Color(this.cRed.getValue(), this.cGreen.getValue(), this.cBlue.getValue(), this.cAlpha.getValue()),
                                this.lineWidth.getValue(),
                                this.outline.getValue(),
                                this.box.getValue(),
                                this.boxAlpha.getValue(),
                                true
                            )
                        }
                    }
                    --y
                }
                ++z
            }
            ++x
        }
    }

    companion object {
        @JvmField
        var INSTANCE: HoleESP = HoleESP()

        @JvmStatic
        fun getInstance(): HoleESP = INSTANCE
    }
}
