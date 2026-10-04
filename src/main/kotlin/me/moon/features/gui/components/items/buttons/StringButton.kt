package me.moon.features.gui.components.items.buttons

import me.moon.Moon
import me.moon.features.gui.MoonGui
import me.moon.features.modules.client.ClickGui
import me.moon.features.setting.Setting
import me.moon.util.RenderUtil
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.sound.SoundEvents
import net.minecraft.util.Formatting
import org.lwjgl.glfw.GLFW

class StringButton(setting: Setting<*>) : Button(setting.getName()) {
    private val setting: Setting<*> = setting
    var isListening = false
    private var currentString = CurrentString("")

    init {
        this.width = 15
    }

    class CurrentString(string: String) {
        private val string: String = string
        fun getString(): String {
            return this.string
        }
    }

    companion object {
        @JvmStatic
        fun removeLastChar(str: String?): String {
            var output = ""
            if (str != null && str.length > 0) {
                output = str.substring(0, str.length - 1)
            }
            return output
        }
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val clickGui = Moon.moduleManager!!.getModuleByClass(ClickGui::class.java) as ClickGui
        RenderUtil.drawRect(
            this.x,
            this.y,
            this.x + this.width.toFloat() + 7.4f,
            this.y + this.height.toFloat() - 0.5f,
            if (this.getState()) {
                if (!this.isHovering(mouseX, mouseY)) Moon.colorManager!!.getColorWithAlpha(clickGui.hoverAlpha.getValue()) else Moon.colorManager!!.getColorWithAlpha(clickGui.alpha.getValue())
            } else {
                if (!this.isHovering(mouseX, mouseY)) 290805077 else -2007673515
            }
        )
        if (this.isListening) {
            Moon.textManager!!.drawStringWithShadow(
                this.currentString.getString() + Moon.textManager!!.getIdleSign(),
                this.x + 2.3f,
                this.y - 1.7f - MoonGui.getClickGui().getTextOffset().toFloat(),
                if (this.getState()) -1 else -5592406
            )
        } else {
            Moon.textManager!!.drawStringWithShadow(
                (if (this.setting.getName().equals("Buttons")) "Buttons " else if (this.setting.getName().equals("Prefix")) "Prefix  " + Formatting.GRAY else "") + this.setting.getValue().toString(),
                this.x + 2.3f,
                this.y - 1.7f - MoonGui.getClickGui().getTextOffset().toFloat(),
                if (this.getState()) -1 else -5592406
            )
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        super.mouseClicked(mouseX, mouseY, mouseButton)
        if (this.isHovering(mouseX, mouseY)) {
            mc.soundManager.play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f))
        }
    }

    override fun onKeyTyped(typedChar: Char, keyCode: Int) {
        if (this.isListening) {
            when (keyCode) {
                GLFW.GLFW_KEY_ESCAPE -> return
                GLFW.GLFW_KEY_ENTER -> {
                    this.enterString()
                    this.setString(removeLastChar(this.currentString.getString()))
                }
                GLFW.GLFW_KEY_BACKSPACE -> this.setString(removeLastChar(this.currentString.getString()))
                else -> if (typedChar.code >= 32 && typedChar != '§') {
                    this.setString(this.currentString.getString() + typedChar)
                }
            }
        }
    }

    override fun update() {
        this.setHidden(!this.setting.isVisible())
    }

    private fun enterString() {
        if (this.currentString.getString().isEmpty()) {
            (this.setting as Setting<Any>).setValue(this.setting.getDefaultValue())
        } else {
            (this.setting as Setting<Any>).setValue(this.currentString.getString())
        }
        this.setString("")
        this.onMouseClick()
    }

    override fun getHeight(): Int {
        return 14
    }

    override fun toggle() {
        this.isListening = !this.isListening
    }

    override fun getState(): Boolean {
        return !this.isListening
    }

    fun setString(newString: String) {
        this.currentString = CurrentString(newString)
    }
}
