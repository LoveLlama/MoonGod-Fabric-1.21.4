package me.moon.features.gui.components.items.buttons

import me.moon.Moon
import me.moon.features.gui.MoonGui
import me.moon.features.modules.client.ClickGui
import me.moon.features.setting.Setting
import me.moon.util.RenderUtil
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.sound.SoundEvents
import net.minecraft.util.Formatting

class UnlimitedSlider(setting: Setting<*>) : Button(setting.getName()) {
    @JvmField
    var setting: Setting<*> = setting

    init {
        this.width = 15
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val clickGui = Moon.moduleManager!!.getModuleByClass(ClickGui::class.java) as ClickGui
        RenderUtil.drawRect(
            this.x,
            this.y,
            this.x + this.width.toFloat() + 7.4f,
            this.y + this.height.toFloat() - 0.5f,
            if (!this.isHovering(mouseX, mouseY)) Moon.colorManager!!.getColorWithAlpha(clickGui.hoverAlpha.getValue()) else Moon.colorManager!!.getColorWithAlpha(clickGui.alpha.getValue())
        )
        Moon.textManager!!.drawStringWithShadow(
            " - " + this.setting.getName() + " " + Formatting.GRAY + this.setting.getValue() + Formatting.WHITE + " +",
            this.x + 2.3f,
            this.y - 1.7f - MoonGui.getClickGui().getTextOffset().toFloat(),
            if (this.getState()) -1 else -5592406
        )
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        super.mouseClicked(mouseX, mouseY, mouseButton)
        if (this.isHovering(mouseX, mouseY)) {
            mc.soundManager.play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f))
            if (this.isRight(mouseX)) {
                when (this.setting.getValue()) {
                    is Double -> (this.setting as Setting<Any>).setValue((this.setting.getValue() as Double) + 1.0)
                    is Float -> (this.setting as Setting<Any>).setValue((this.setting.getValue() as Float) + 1.0f)
                    is Int -> (this.setting as Setting<Any>).setValue((this.setting.getValue() as Int) + 1)
                }
            } else {
                when (this.setting.getValue()) {
                    is Double -> (this.setting as Setting<Any>).setValue((this.setting.getValue() as Double) - 1.0)
                    is Float -> (this.setting as Setting<Any>).setValue((this.setting.getValue() as Float) - 1.0f)
                    is Int -> (this.setting as Setting<Any>).setValue((this.setting.getValue() as Int) - 1)
                }
            }
        }
    }

    override fun update() {
        this.setHidden(!this.setting.isVisible())
    }

    override fun getHeight(): Int {
        return 14
    }

    override fun toggle() {
    }

    override fun getState(): Boolean {
        return true
    }

    fun isRight(x: Int): Boolean {
        return x > this.x + (this.width.toFloat() + 7.4f) / 2.0f
    }
}
