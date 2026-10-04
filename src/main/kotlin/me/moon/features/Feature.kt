package me.moon.features

import me.moon.Moon
import me.moon.features.gui.MoonGui
import me.moon.features.modules.Module
import me.moon.features.setting.Setting
import me.moon.manager.TextManager
import me.moon.util.Util
import net.minecraft.client.MinecraftClient

open class Feature : Util {
    @JvmField
    var settings: MutableList<Setting<*>> = ArrayList()
    val renderer: TextManager get() = Moon.textManager!!
    private var name: String? = null

    constructor()

    constructor(name: String?) {
        this.name = name
    }

    open fun getName(): String? = name

    fun getSettings(): MutableList<Setting<*>> = settings

    fun hasSettings(): Boolean = settings.isNotEmpty()

    open fun isEnabled(): Boolean = if (this is Module) this.isOn() else false

    fun isDisabled(): Boolean = !isEnabled()

    fun <T> register(setting: Setting<T>): Setting<T> {
        setting.setFeature(this)
        settings.add(setting)
        if (this is Module && mc.currentScreen is MoonGui) {
            MoonGui.getInstance().updateModule(this)
        }
        return setting
    }

    fun unregister(settingIn: Setting<*>) {
        val removeList = ArrayList<Setting<*>>()
        for (setting in settings) {
            if (setting.equals(settingIn)) {
                removeList.add(setting)
            }
        }
        if (removeList.isNotEmpty()) {
            settings.removeAll(removeList)
        }
        if (this is Module && mc.currentScreen is MoonGui) {
            MoonGui.getInstance().updateModule(this)
        }
    }

    fun getSettingByName(name: String): Setting<*>? {
        for (setting in settings) {
            if (setting.getName().equals(name, ignoreCase = true)) {
                return setting
            }
        }
        return null
    }

    open fun reset() {
        for (setting in settings) {
            setting.setValue(setting.getDefaultValue())
        }
    }

    fun clearSettings() {
        settings = ArrayList()
    }

    companion object {
        @JvmStatic
        fun nullCheck(): Boolean = MinecraftClient.getInstance().player == null

        @JvmStatic
        fun fullNullCheck(): Boolean = MinecraftClient.getInstance().player == null || MinecraftClient.getInstance().world == null
    }
}
