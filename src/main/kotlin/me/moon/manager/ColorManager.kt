package me.moon.manager

import me.moon.features.gui.components.Component
import me.moon.features.modules.client.ClickGui
import me.moon.util.ColorUtil
import java.awt.Color

class ColorManager {
    private var red = 1.0F
    private var green = 1.0F
    private var blue = 1.0F
    private var alpha = 1.0F
    private var color = Color(red, green, blue, alpha)

    fun getColor(): Color = color

    fun setColor(color: Color) {
        this.color = color
    }

    fun getColorAsInt(): Int = ColorUtil.toRGBA(color)

    fun getColorAsIntFullAlpha(): Int {
        return ColorUtil.toRGBA(Color(color.red, color.green, color.blue, 255))
    }

    fun getColorWithAlpha(alpha: Int): Int {
        return if (ClickGui.getInstance().rainbow.getValue()) {
            ColorUtil.rainbow(Component.counter1[0] * ClickGui.getInstance().rainbowHue.getValue()).rgb
        } else {
            ColorUtil.toRGBA(Color(red, green, blue, alpha.toFloat() / 255.0F))
        }
    }

    fun setColor(red: Float, green: Float, blue: Float, alpha: Float) {
        this.red = red
        this.green = green
        this.blue = blue
        this.alpha = alpha
        this.updateColor()
    }

    fun updateColor() {
        setColor(Color(red, green, blue, alpha))
    }

    fun setColor(red: Int, green: Int, blue: Int, alpha: Int) {
        this.red = red.toFloat() / 255.0F
        this.green = green.toFloat() / 255.0F
        this.blue = blue.toFloat() / 255.0F
        this.alpha = alpha.toFloat() / 255.0F
        this.updateColor()
    }

    fun setRed(red: Float) {
        this.red = red
        updateColor()
    }

    fun setGreen(green: Float) {
        this.green = green
        updateColor()
    }

    fun setBlue(blue: Float) {
        this.blue = blue
        updateColor()
    }

    fun setAlpha(alpha: Float) {
        this.alpha = alpha
        updateColor()
    }
}
