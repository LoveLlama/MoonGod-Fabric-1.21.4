package me.moon.features.gui.components

import me.moon.Moon
import me.moon.features.Feature
import me.moon.features.gui.MoonGui
import me.moon.features.gui.components.items.Item
import me.moon.features.gui.components.items.buttons.Button
import me.moon.features.modules.client.ClickGui
import me.moon.util.ColorUtil
import me.moon.util.RenderUtil
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.sound.SoundEvents

open class Component(name: String, x: Int, y: Int, open: Boolean) : Feature(name) {
    @JvmField
    var drag = false
    private val items: MutableList<Item> = ArrayList()
    private var x: Int = x
    private var y: Int = y
    private var x2 = 0
    private var y2 = 0
    private var width = 88
    private var height = 18
    private var open: Boolean = open
    private var hidden = false

    init {
        this.setupItems()
    }

    open fun setupItems() {
    }

    private fun drag(mouseX: Int, mouseY: Int) {
        if (this.drag) {
            this.x = this.x2 + mouseX
            this.y = this.y2 + mouseY
        }
    }

    open fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        this.drag(mouseX, mouseY)
        counter1 = intArrayOf(1)
        val totalItemHeight = if (this.open) this.getTotalItemHeight() - 2.0f else 0.0f
        val color = ColorUtil.toARGB(
            ClickGui.getInstance().topRed.getValue(), ClickGui.getInstance().topGreen.getValue(), ClickGui.getInstance().topBlue.getValue(), 255
        )
        RenderUtil.drawRect(
            this.x.toFloat(),
            (this.y - 1).toFloat(),
            (this.x + this.width).toFloat(),
            (this.y + this.height - 6).toFloat(),
            if (ClickGui.getInstance().rainbow.getValue()) ColorUtil.rainbow(ClickGui.getInstance().rainbowHue.getValue()).rgb else color
        )
        if (this.open) {
            RenderUtil.drawRect(this.x.toFloat(), this.y + 12.5f, (this.x + this.width).toFloat(), this.y + this.height.toFloat() + totalItemHeight, 1996488704)
        }
        Moon.textManager!!.drawStringWithShadow(this.getName() ?: "", this.x + 3.0f, this.y - 4.0f - MoonGui.getClickGui().getTextOffset().toFloat(), -1)
        if (this.open) {
            var y = (this.getY() + this.getHeight()) - 3.0f
            for (item: Item in this.getItems()) {
                counter1[0]++
                if (!item.isHidden()) {
                    item.setLocation(this.x + 2.0f, y)
                    item.setWidth(this.getWidth() - 4)
                    item.drawScreen(mouseX, mouseY, partialTicks)
                    y += item.getHeight() + 1.5f
                }
            }
        }
    }

    open fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (mouseButton == 0 && this.isHovering(mouseX, mouseY)) {
            this.x2 = this.x - mouseX
            this.y2 = this.y - mouseY
            for (component: Component in MoonGui.getClickGui().getComponents()) {
                if (component.drag) {
                    component.drag = false
                }
            }
            this.drag = true
        } else if (mouseButton == 1 && this.isHovering(mouseX, mouseY)) {
            this.open = !this.open
            mc.soundManager.play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f))
        } else if (this.open) {
            for (item: Item in this.getItems()) {
                item.mouseClicked(mouseX, mouseY, mouseButton)
            }
        }
    }

    open fun mouseReleased(mouseX: Int, mouseY: Int, releaseButton: Int) {
        if (releaseButton == 0) {
            this.drag = false
        }
        if (this.open) {
            for (item: Item in this.getItems()) {
                item.mouseReleased(mouseX, mouseY, releaseButton)
            }
        }
    }

    open fun onKeyTyped(typedChar: Char, keyCode: Int) {
        if (this.open) {
            for (item: Item in this.getItems()) {
                item.onKeyTyped(typedChar, keyCode)
            }
        }
    }

    open fun addButton(button: Button) {
        this.items.add(button)
    }

    open fun getX(): Int {
        return this.x
    }

    open fun setX(x: Int) {
        this.x = x
    }

    open fun getY(): Int {
        return this.y
    }

    open fun setY(y: Int) {
        this.y = y
    }

    open fun getWidth(): Int {
        return this.width
    }

    open fun setWidth(width: Int) {
        this.width = width
    }

    open fun getHeight(): Int {
        return this.height
    }

    open fun setHeight(height: Int) {
        this.height = height
    }

    open fun isHidden(): Boolean {
        return this.hidden
    }

    open fun setHidden(hidden: Boolean) {
        this.hidden = hidden
    }

    open fun isOpen(): Boolean {
        return this.open
    }

    open fun getItems(): MutableList<Item> {
        return this.items
    }

    private fun isHovering(mouseX: Int, mouseY: Int): Boolean {
        return mouseX >= this.getX() && mouseX <= this.getX() + this.getWidth() && mouseY >= this.getY() && mouseY <= this.getY() + this.getHeight() - (if (this.open) 2 else 0)
    }

    private fun getTotalItemHeight(): Float {
        var height = 0.0f
        for (item: Item in this.getItems()) {
            height += item.getHeight() + 1.5f
        }
        return height
    }

    companion object {
        @JvmField
        var counter1 = intArrayOf(1)
    }
}
