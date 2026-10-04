package me.moon.features.gui.components.items.buttons

import me.moon.Moon
import me.moon.features.gui.MoonGui
import me.moon.features.gui.components.Component
import me.moon.features.gui.components.items.Item
import me.moon.features.modules.client.ClickGui
import me.moon.util.RenderUtil
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.sound.SoundEvents

open class Button(name: String) : Item(name) {
    private var state = false

    init {
        this.height = 15
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val clickGui = Moon.moduleManager!!.getModuleByClass(ClickGui::class.java) as ClickGui
        RenderUtil.drawRect(
            this.x,
            this.y,
            this.x + this.width.toFloat(),
            this.y + this.height.toFloat() - 0.5f,
            if (this.getState()) {
                if (!this.isHovering(mouseX, mouseY)) Moon.colorManager!!.getColorWithAlpha(clickGui.hoverAlpha.getValue()) else Moon.colorManager!!.getColorWithAlpha(clickGui.alpha.getValue())
            } else {
                if (!this.isHovering(mouseX, mouseY)) 290805077 else -2007673515
            }
        )
        Moon.textManager!!.drawStringWithShadow(this.getName() ?: "", this.x + 2.3f, this.y - 2.0f - MoonGui.getClickGui().getTextOffset().toFloat(), if (this.getState()) -1 else -5592406)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (mouseButton == 0 && this.isHovering(mouseX, mouseY)) {
            this.onMouseClick()
        }
    }

    open fun onMouseClick() {
        this.state = !this.state
        this.toggle()
        mc.soundManager.play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f))
    }

    open fun toggle() {
    }

    open fun getState(): Boolean {
        return this.state
    }

    open fun setState(state: Boolean) {
        this.state = state
    }

    override fun getHeight(): Int {
        return 14
    }

    open fun isHovering(mouseX: Int, mouseY: Int): Boolean {
        for (component: Component in MoonGui.getClickGui().getComponents()) {
            if (component.drag) {
                return false
            }
        }
        return mouseX >= this.getX() && mouseX <= this.getX() + this.getWidth() && mouseY >= this.getY() && mouseY <= this.getY() + this.height
    }
}
