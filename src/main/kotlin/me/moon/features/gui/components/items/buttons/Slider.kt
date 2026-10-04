package me.moon.features.gui.components.items.buttons

import me.moon.Moon
import me.moon.features.gui.MoonGui
import me.moon.features.gui.components.Component
import me.moon.features.modules.client.ClickGui
import me.moon.features.setting.Setting
import me.moon.util.RenderUtil
import net.minecraft.util.Formatting
import org.lwjgl.glfw.GLFW

class Slider(setting: Setting<*>) : Button(setting.getName()) {
    private val min: Number = setting.getMin() as Number
    private val max: Number = setting.getMax() as Number
    private val difference: Int = this.max.toInt() - this.min.toInt()
    @JvmField
    var setting: Setting<*> = setting

    init {
        this.width = 15
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        this.dragSetting(mouseX, mouseY)
        val clickGui = Moon.moduleManager!!.getModuleByClass(ClickGui::class.java) as ClickGui
        RenderUtil.drawRect(
            this.x, this.y, this.x + this.width.toFloat() + 7.4f, this.y + this.height.toFloat() - 0.5f,
            if (!this.isHovering(mouseX, mouseY)) 290805077 else -2007673515
        )
        val value = this.setting.getValue() as Number
        RenderUtil.drawRect(
            this.x,
            this.y,
            if (value.toFloat() <= this.min.toFloat()) this.x else this.x + (this.width.toFloat() + 7.4f) * this.partialMultiplier(),
            this.y + this.height.toFloat() - 0.5f,
            if (!this.isHovering(mouseX, mouseY)) Moon.colorManager!!.getColorWithAlpha(clickGui.hoverAlpha.getValue()) else Moon.colorManager!!.getColorWithAlpha(clickGui.alpha.getValue())
        )
        Moon.textManager!!.drawStringWithShadow(
            this.getName() + " " + Formatting.GRAY + (if (value is Float) value else value.toDouble()),
            this.x + 2.3f,
            this.y - 1.7f - MoonGui.getClickGui().getTextOffset().toFloat(),
            -1
        )
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        super.mouseClicked(mouseX, mouseY, mouseButton)
        if (this.isHovering(mouseX, mouseY)) {
            this.setSettingFromX(mouseX)
        }
    }

    override fun isHovering(mouseX: Int, mouseY: Int): Boolean {
        for (component: Component in MoonGui.getClickGui().getComponents()) {
            if (component.drag) {
                return false
            }
        }
        return mouseX >= this.getX() && mouseX <= this.getX() + this.getWidth() + 8.0f && mouseY >= this.getY() && mouseY <= this.getY() + this.height
    }

    override fun update() {
        this.setHidden(!this.setting.isVisible())
    }

    private fun dragSetting(mouseX: Int, mouseY: Int) {
        if (this.isHovering(mouseX, mouseY) && GLFW.glfwGetMouseButton(mc.window.handle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS) {
            this.setSettingFromX(mouseX)
        }
    }

    override fun getHeight(): Int {
        return 14
    }

    private fun setSettingFromX(mouseX: Int) {
        val percent = (mouseX - this.x) / (this.width.toFloat() + 7.4f)
        when (this.setting.getValue()) {
            is Double -> {
                val result = (this.min.toDouble()) + (this.difference.toFloat() * percent).toDouble()
                (this.setting as Setting<Any>).setValue(Math.round(10.0 * result) / 10.0)
            }
            is Float -> {
                val result = this.min.toFloat() + this.difference.toFloat() * percent
                (this.setting as Setting<Any>).setValue(Math.round(10.0f * result) / 10.0f)
            }
            is Int -> {
                (this.setting as Setting<Any>).setValue(this.min.toInt() + (this.difference.toFloat() * percent).toInt())
            }
        }
    }

    private fun middle(): Float {
        return this.max.toFloat() - this.min.toFloat()
    }

    private fun part(): Float {
        return (this.setting.getValue() as Number).toFloat() - this.min.toFloat()
    }

    private fun partialMultiplier(): Float {
        return this.part() / this.middle()
    }
}
