package me.moon.features.gui.components.items.buttons

import me.moon.features.gui.components.Component
import me.moon.features.gui.components.items.Item
import me.moon.features.modules.Module
import me.moon.features.setting.Bind
import me.moon.features.setting.Setting
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.sound.SoundEvents

class ModuleButton(module: Module) : Button(module.getName() ?: "") {
    private val module: Module = module
    private var items: MutableList<Item> = ArrayList()
    private var subOpen = false

    init {
        this.initSettings()
    }

    fun initSettings() {
        val newItems: MutableList<Item> = ArrayList()
        if (!this.module.getSettings().isEmpty()) {
            for (setting: Setting<*> in this.module.getSettings()) {
                if (setting.getValue() is Boolean && !setting.getName().equals("Enabled")) {
                    newItems.add(BooleanButton(setting))
                }
                if (setting.getValue() is Bind && !setting.getName().equals("Keybind", ignoreCase = true) && !this.module.getName().equals("Hud", ignoreCase = true)) {
                    newItems.add(BindButton(setting))
                }
                if ((setting.getValue() is String || setting.getValue() is Char) && !setting.getName().equals("displayName", ignoreCase = true)) {
                    newItems.add(StringButton(setting))
                }
                if (setting.isNumberSetting() && setting.hasRestriction()) {
                    newItems.add(Slider(setting))
                } else if (setting.isEnumSetting()) {
                    newItems.add(EnumButton(setting))
                }
            }
        }
        newItems.add(BindButton(this.module.getSettingByName("Keybind")!!))
        this.items = newItems
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        super.drawScreen(mouseX, mouseY, partialTicks)
        if (!this.items.isEmpty() && this.subOpen) {
            var height = 1.0f
            for (item: Item in this.items) {
                Component.counter1[0]++
                if (!item.isHidden()) {
                    height += 15.0f
                    item.setLocation(this.x + 1.0f, this.y + height)
                    item.setHeight(15)
                    item.setWidth(this.width - 9)
                    item.drawScreen(mouseX, mouseY, partialTicks)
                }
                item.update()
            }
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        super.mouseClicked(mouseX, mouseY, mouseButton)
        if (!this.items.isEmpty()) {
            if (mouseButton == 1 && this.isHovering(mouseX, mouseY)) {
                this.subOpen = !this.subOpen
                mc.soundManager.play(PositionedSoundInstance.master(net.minecraft.sound.SoundEvents.UI_BUTTON_CLICK, 1.0f))
            }
            if (this.subOpen) {
                for (item: Item in this.items) {
                    if (!item.isHidden()) {
                        item.mouseClicked(mouseX, mouseY, mouseButton)
                    }
                }
            }
        }
    }

    override fun onKeyTyped(typedChar: Char, keyCode: Int) {
        super.onKeyTyped(typedChar, keyCode)
        if (!this.items.isEmpty() && this.subOpen) {
            for (item: Item in this.items) {
                if (!item.isHidden()) {
                    item.onKeyTyped(typedChar, keyCode)
                }
            }
        }
    }

    override fun getHeight(): Int {
        if (this.subOpen) {
            var height = 14
            for (item: Item in this.items) {
                if (!item.isHidden()) {
                    height += item.getHeight() + 1
                }
            }
            return height + 2
        } else {
            return 14
        }
    }

    fun getModule(): Module {
        return this.module
    }

    override fun toggle() {
        this.module.toggle()
    }

    override fun getState(): Boolean {
        return this.module.isEnabled()
    }
}
