package me.moon.util

import me.moon.features.modules.client.ClickGui
import java.awt.Color

object ColorUtil {
    @JvmStatic
    fun toARGB(r: Int, g: Int, b: Int, a: Int): Int {
        return Color(r, g, b, a).rgb
    }

    @JvmStatic
    fun toRGBA(r: Int, g: Int, b: Int): Int {
        return toRGBA(r, g, b, 255)
    }

    @JvmStatic
    fun toRGBA(r: Int, g: Int, b: Int, a: Int): Int {
        return (r shl 16) + (g shl 8) + b + (a shl 24)
    }

    @JvmStatic
    fun toRGBA(r: Float, g: Float, b: Float, a: Float): Int {
        return toRGBA((r * 255.0f).toInt(), (g * 255.0f).toInt(), (b * 255.0f).toInt(), (a * 255.0f).toInt())
    }

    @JvmStatic
    fun rainbow(delay: Int): Color {
        val rainbowState = Math.ceil((System.currentTimeMillis() + delay.toLong()) / 20.0)
        val var3 = rainbowState % 360.0
        return Color.getHSBColor(
            (var3 / 360.0).toFloat(),
            ClickGui.getInstance().rainbowSaturation.getValue() / 255.0f,
            ClickGui.getInstance().rainbowBrightness.getValue() / 255.0f
        )
    }

    @JvmStatic
    fun toRGBA(colors: FloatArray): Int {
        if (colors.size != 4) {
            throw IllegalArgumentException("colors[] must have a length of 4!")
        } else {
            return toRGBA(colors[0], colors[1], colors[2], colors[3])
        }
    }

    @JvmStatic
    fun toRGBA(colors: DoubleArray): Int {
        if (colors.size != 4) {
            throw IllegalArgumentException("colors[] must have a length of 4!")
        } else {
            return toRGBA(colors[0].toFloat(), colors[1].toFloat(), colors[2].toFloat(), colors[3].toFloat())
        }
    }

    @JvmStatic
    fun toRGBA(color: Color): Int {
        return toRGBA(color.red, color.green, color.blue, color.alpha)
    }
}
