package me.moon.manager

import me.moon.Moon
import me.moon.features.Feature
import me.moon.features.gui.font.CustomFont
import me.moon.features.modules.client.FontMod
import me.moon.util.Timer
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.util.math.MathHelper
import org.joml.Matrix4f
import java.awt.Font

class TextManager : Feature() {
    private val idleTimer = Timer()
    var scaledWidth = 0
    var scaledHeight = 0
    var scaleFactor = 0
    var customFont: CustomFont = CustomFont(Font("Verdana", 0, 17), true, false)
    private var idling = false

    init {
        updateResolution()
    }

    fun init(startup: Boolean) {
        val cFont = Moon.moduleManager!!.getModuleByClass(FontMod::class.java)
        try {
            setFontRenderer(
                Font(cFont!!.fontName.getValue(), cFont.fontStyle.getValue(), cFont.fontSize.getValue()),
                cFont.antiAlias.getValue(),
                cFont.fractionalMetrics.getValue()
            )
        } catch (e: Exception) {
        }
    }

    fun drawStringWithShadow(text: String, x: Float, y: Float, color: Int) {
        drawString(text, x, y, color, true)
    }

    fun drawString(text: String, x: Float, y: Float, color: Int, shadow: Boolean) {
        if (Moon.moduleManager!!.isModuleEnabled(FontMod.getInstance().getName()!!)) {
            if (shadow) {
                customFont.drawStringWithShadow(text, x.toDouble(), y.toDouble(), color)
            } else {
                customFont.drawString(text, x, y, color)
            }
        } else {
            val matrix = Matrix4f()
            val providers = mc.bufferBuilders.entityVertexConsumers
            mc.textRenderer.draw(text, x, y, color, shadow, matrix, providers, TextRenderer.TextLayerType.NORMAL, 0, 15728880)
            providers.draw()
        }
    }

    fun getStringWidth(text: String): Int {
        return if (Moon.moduleManager!!.isModuleEnabled(FontMod.getInstance().getName()!!)) customFont.getStringWidth(text) else mc.textRenderer.getWidth(text)
    }

    fun getFontHeight(): Int {
        if (Moon.moduleManager!!.isModuleEnabled(FontMod.getInstance().getName()!!)) {
            val text = "A"
            return customFont.getStringHeight(text)
        } else {
            return mc.textRenderer.fontHeight
        }
    }

    fun setFontRenderer(font: Font, antiAlias: Boolean, fractionalMetrics: Boolean) {
        customFont = CustomFont(font, antiAlias, fractionalMetrics)
    }

    fun getCurrentFont(): Font = customFont.getFont()

    fun updateResolution() {
        scaledWidth = mc.window.width
        scaledHeight = mc.window.height
        scaleFactor = 1
        val flag = false
        var i = mc.options.guiScale.value
        if (i == 0) {
            i = 1000
        }
        while (scaleFactor < i && scaledWidth / (scaleFactor + 1) >= 320 && scaledHeight / (scaleFactor + 1) >= 240) {
            scaleFactor++
        }
        if (flag && scaleFactor % 2 != 0 && scaleFactor != 1) {
            scaleFactor--
        }
        val scaledWidthD = (scaledWidth / scaleFactor).toDouble()
        val scaledHeightD = (scaledHeight / scaleFactor).toDouble()
        scaledWidth = MathHelper.ceil(scaledWidthD)
        scaledHeight = MathHelper.ceil(scaledHeightD)
    }

    fun getIdleSign(): String {
        if (idleTimer.passedMs(500L)) {
            idling = !idling
            idleTimer.reset()
        }
        return if (idling) "_" else ""
    }
}
