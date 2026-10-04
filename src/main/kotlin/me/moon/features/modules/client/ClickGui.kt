package me.moon.features.modules.client

import me.moon.Moon
import me.moon.event.SubscribeEvent
import me.moon.event.events.ClientEvent
import me.moon.features.command.Command
import me.moon.features.gui.MoonGui
import me.moon.features.modules.Module
import me.moon.features.setting.Setting
import net.minecraft.util.Formatting

class ClickGui : Module {
    @JvmField var prefix: Setting<String> = register(Setting("Prefix", "."))
    @JvmField var customFov: Setting<Boolean> = register(Setting("CustomFov", false))
    @JvmField var fov: Setting<Float> = register(Setting("Fov", 90.0f, -180.0f, 180.0f))
    @JvmField var size: Setting<Float> = register(Setting("Size", 1.0f, 0.5f, 2.0f))
    @JvmField var red: Setting<Int> = register(Setting("Red", 255, 0, 255))
    @JvmField var green: Setting<Int> = register(Setting("Green", 255, 0, 255))
    @JvmField var blue: Setting<Int> = register(Setting("Blue", 255, 0, 255))
    @JvmField var hoverAlpha: Setting<Int> = register(Setting("Alpha", 100, 0, 255))
    @JvmField var topRed: Setting<Int> = register(Setting("SecondRed", 255, 0, 255))
    @JvmField var topGreen: Setting<Int> = register(Setting("SecondGreen", 255, 0, 255))
    @JvmField var topBlue: Setting<Int> = register(Setting("SecondBlue", 255, 0, 255))
    @JvmField var alpha: Setting<Int> = register(Setting("HoverAlpha", 50, 0, 255))
    @JvmField var rainbow: Setting<Boolean> = register(Setting("Rainbow", false))
    @JvmField var rainbowModeHud: Setting<rainbowMode> = register(Setting("HRainbowMode", rainbowMode.Static) { this.rainbow.getValue() })
    @JvmField var rainbowModeA: Setting<rainbowModeArray> = register(Setting("ARainbowMode", rainbowModeArray.Static) { this.rainbow.getValue() })
    @JvmField var rainbowHue: Setting<Int> = register(Setting("Delay", 240, 0, 600) { this.rainbow.getValue() })
    @JvmField var rainbowBrightness: Setting<Float> = register(Setting("Brightness ", 150.0f, 1.0f, 255.0f) { this.rainbow.getValue() })
    @JvmField var rainbowSaturation: Setting<Float> = register(Setting("Saturation", 150.0f, 1.0f, 255.0f) { this.rainbow.getValue() })

    constructor() : super("ClickGui", "Opens the ClickGui", Module.Category.CLIENT, true, false, false) {
        setInstance()
    }

    override fun onUpdate() {
        if (this.customFov.getValue()) {
            mc.options.getFov().setValue(this.fov.getValue().toInt())
        }
    }

    @SubscribeEvent
    fun onSettingChange(event: ClientEvent) {
        val setting = event.getSetting()
        if (event.stage == 2 && setting != null && setting.getFeature() == this) {
            if (setting == this.prefix) {
                Moon.commandManager!!.setPrefix(this.prefix.getPlannedValue())
                Command.sendMessage("Prefix set to " + Formatting.DARK_GRAY + Moon.commandManager!!.getPrefix())
            }
            Moon.colorManager!!.setColor(this.red.getPlannedValue(), this.green.getPlannedValue(), this.blue.getPlannedValue(), this.hoverAlpha.getPlannedValue())
        }
    }

    override fun onEnable() {
        mc.setScreen(MoonGui.getClickGui())
    }

    override fun onLoad() {
        Moon.colorManager!!.setColor(this.red.getValue(), this.green.getValue(), this.blue.getValue(), this.hoverAlpha.getValue())
        Moon.commandManager!!.setPrefix(this.prefix.getValue())
    }

    override fun onTick() {
        if (mc.currentScreen !is MoonGui) {
            this.disable()
        }
    }

    enum class rainbowMode {
        Static,
        Sideway
    }

    enum class rainbowModeArray {
        Static,
        Up
    }

    companion object {
        @JvmField
        var INSTANCE: ClickGui = ClickGui()

        @JvmStatic
        fun getInstance(): ClickGui {
            if (INSTANCE == null) {
                INSTANCE = ClickGui()
            }
            return INSTANCE
        }
    }

    private fun setInstance() {
        INSTANCE = this
    }
}
