package me.moon.features.gui.components.items.buttons

import me.moon.Moon
import me.moon.features.gui.MoonGui
import me.moon.features.modules.client.ClickGui
import me.moon.features.setting.Setting
import me.moon.util.RenderUtil
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.sound.SoundEvents
import net.minecraft.util.Formatting

class EnumButton(setting: Setting<*>) : Button(setting.getName()) {
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
            if (this.getState()) {
                if (!this.isHovering(mouseX, mouseY)) Moon.colorManager!!.getColorWithAlpha(clickGui.hoverAlpha.getValue()) else Moon.colorManager!!.getColorWithAlpha(clickGui.alpha.getValue())
            } else {
                if (!this.isHovering(mouseX, mouseY)) 290805077 else -2007673515
            }
        )
        val enumName = this.setting.currentEnumName()
        Moon.textManager!!.drawStringWithShadow(
            this.setting.getName() + " " + Formatting.GRAY + (if (enumName.equals("ABC", ignoreCase = true)) "ABC" else enumName),
            this.x + 2.3f,
            this.y - 1.7f - MoonGui.getClickGui().getTextOffset().toFloat(),
            if (this.getState()) -1 else -5592406
        )
    }

    override fun update() {
        this.setHidden(!this.setting.isVisible())
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        super.mouseClicked(mouseX, mouseY, mouseButton)
        if (this.isHovering(mouseX, mouseY)) {
            mc.soundManager.play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f))
        }
    }

    override fun getHeight(): Int {
        return 14
    }

    override fun toggle() {
        this.setting.increaseEnum()
    }

    override fun getState(): Boolean {
        return true
    }
}
