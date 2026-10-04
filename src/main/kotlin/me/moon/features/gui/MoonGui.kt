package me.moon.features.gui

import me.moon.Moon
import me.moon.features.Feature
import me.moon.features.gui.components.Component
import me.moon.features.gui.components.items.Item
import me.moon.features.gui.components.items.buttons.ModuleButton
import me.moon.features.modules.Module
import me.moon.features.modules.client.ClickGui
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text

class MoonGui : Screen {
    private val components: MutableList<Component> = ArrayList()

    constructor() : super(Text.literal("")) {
        setInstance()
        this.load()
    }

    private fun setInstance() {
        INSTANCE = this
    }

    private fun load() {
        var x = -84
        for (category: Module.Category in Moon.moduleManager!!.getCategories()) {
            x += 90
            val name = category.getName()
            this.components.add(object : Component(name, x, 4, true) {
                override fun setupItems() {
                    Component.counter1 = intArrayOf(1)
                    for (module: Module in Moon.moduleManager!!.getModulesByCategory(category)) {
                        if (!module.hidden) {
                            this.addButton(ModuleButton(module))
                        }
                    }
                }
            })
        }
        for (component: Component in this.components) {
            component.getItems().sortBy { it.getName() }
        }
    }

    fun updateModule(module: Module?) {
        for (component: Component in this.components) {
            for (item: Item in component.getItems()) {
                if (item is ModuleButton) {
                    val mod = item.getModule()
                    if (module != null && module.equals(mod)) {
                        item.initSettings()
                    }
                }
            }
        }
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        this.renderBackground(context, mouseX, mouseY, delta)
        val scale = ClickGui.getInstance().size.getValue()
        val client = MinecraftClient.getInstance()
        val centerX = client.window.scaledWidth / 2.0f
        val centerY = client.window.scaledHeight / 2.0f
        val modelView = RenderSystem.getModelViewStack()
        modelView.pushMatrix()
        try {
            modelView.translate(centerX, centerY, 0.0f)
            modelView.scale(scale, scale, 1.0f)
            modelView.translate(-centerX, -centerY, 0.0f)
            val guiMouseX = (centerX + (mouseX - centerX) / scale).toInt()
            val guiMouseY = (centerY + (mouseY - centerY) / scale).toInt()
            for (component: Component in this.components) {
                component.drawScreen(guiMouseX, guiMouseY, delta)
            }
        } finally {
            modelView.popMatrix()
        }
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        val guiX = this.toGuiMouseX(mouseX).toInt()
        val guiY = this.toGuiMouseY(mouseY).toInt()
        for (component: Component in this.components) {
            component.mouseClicked(guiX, guiY, button)
        }
        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        val guiX = this.toGuiMouseX(mouseX).toInt()
        val guiY = this.toGuiMouseY(mouseY).toInt()
        for (component: Component in this.components) {
            component.mouseReleased(guiX, guiY, button)
        }
        return super.mouseReleased(mouseX, mouseY, button)
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, horizontalAmount: Double, verticalAmount: Double): Boolean {
        if (verticalAmount < 0) {
            for (component: Component in this.components) {
                component.setY(component.getY() - 10)
            }
        } else if (verticalAmount > 0) {
            for (component: Component in this.components) {
                component.setY(component.getY() + 10)
            }
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        for (component: Component in this.components) {
            component.onKeyTyped(0.toChar(), keyCode)
        }
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun charTyped(chr: Char, modifiers: Int): Boolean {
        for (component: Component in this.components) {
            component.onKeyTyped(chr, 0)
        }
        return super.charTyped(chr, modifiers)
    }

    override fun shouldPause(): Boolean {
        return false
    }

    fun getComponents(): MutableList<Component> {
        return this.components
    }

    fun getTextOffset(): Int {
        return -6
    }

    private fun toGuiMouseX(screenX: Double): Double {
        val scale = ClickGui.getInstance().size.getValue()
        val centerX = MinecraftClient.getInstance().window.scaledWidth / 2.0
        return centerX + (screenX - centerX) / scale
    }

    private fun toGuiMouseY(screenY: Double): Double {
        val scale = ClickGui.getInstance().size.getValue()
        val centerY = MinecraftClient.getInstance().window.scaledHeight / 2.0
        return centerY + (screenY - centerY) / scale
    }

    fun getComponentByName(name: String): Component? {
        for (component: Component in this.components) {
            if (component.getName().equals(name, ignoreCase = true)) {
                return component
            }
        }
        return null
    }

    companion object {
        @JvmField
        var INSTANCE: MoonGui = MoonGui()

        @JvmStatic
        fun getInstance(): MoonGui {
            if (INSTANCE == null) {
                INSTANCE = MoonGui()
            }
            return INSTANCE
        }

        @JvmStatic
        fun getClickGui(): MoonGui {
            return getInstance()
        }
    }
}
