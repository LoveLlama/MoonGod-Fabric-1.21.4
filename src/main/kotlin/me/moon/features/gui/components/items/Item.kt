package me.moon.features.gui.components.items

import me.moon.features.Feature

open class Item(name: String) : Feature(name) {
    @JvmField protected var x = 0f
    @JvmField protected var y = 0f
    @JvmField protected var width = 0
    @JvmField protected var height = 0
    private var hidden = false

    open fun setLocation(x: Float, y: Float) {
        this.x = x
        this.y = y
    }

    open fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
    }

    open fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
    }

    open fun mouseReleased(mouseX: Int, mouseY: Int, releaseButton: Int) {
    }

    open fun update() {
    }

    open fun onKeyTyped(typedChar: Char, keyCode: Int) {
    }

    open fun getX(): Float {
        return this.x
    }

    open fun getY(): Float {
        return this.y
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

    open fun setHidden(hidden: Boolean): Boolean {
        this.hidden = hidden
        return this.hidden
    }
}
