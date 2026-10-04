package me.moon.features.modules.client

import me.moon.Moon
import me.moon.event.SubscribeEvent
import me.moon.event.events.ClientEvent
import me.moon.features.command.Command
import me.moon.features.modules.Module
import me.moon.features.setting.Setting
import net.minecraft.util.Formatting
import java.awt.GraphicsEnvironment

class FontMod : Module {
    @JvmField var fontName: Setting<String> = register(Setting("FontName", "Arial", "Name of the font."))
    @JvmField var antiAlias: Setting<Boolean> = register(Setting("AntiAlias", true, "Smoother font."))
    @JvmField var fractionalMetrics: Setting<Boolean> = register(Setting("Metrics", true, "Thinner font."))
    @JvmField var fontSize: Setting<Int> = register(Setting("Size", 18, 12, 30, "Size of the font."))
    @JvmField var fontStyle: Setting<Int> = register(Setting("Style", 0, 0, 3, "Style of the font."))
    private var reloadFont = false

    constructor() : super("CustomFont", "CustomFont for all of the clients text. Use the font command.", Module.Category.CLIENT, true, false, false) {
        setInstance()
    }

    companion object {
        @JvmField
        var INSTANCE: FontMod = FontMod()

        @JvmStatic
        fun getInstance(): FontMod {
            if (INSTANCE == null) {
                INSTANCE = FontMod()
            }
            return INSTANCE
        }

        @JvmStatic
        fun checkFont(font: String, message: Boolean): Boolean {
            for (s: String in GraphicsEnvironment.getLocalGraphicsEnvironment().availableFontFamilyNames) {
                if (!message && s == font) {
                    return true
                }
                if (message) {
                    Command.sendMessage(s)
                }
            }
            return false
        }
    }

    private fun setInstance() {
        INSTANCE = this
    }

    @SubscribeEvent
    fun onSettingChange(event: ClientEvent) {
        val setting = event.getSetting()
        if (event.stage == 2 && setting != null && setting.getFeature() == this) {
            if (setting.getName().equals("FontName") && !checkFont(setting.getPlannedValue().toString(), false)) {
                Command.sendMessage(Formatting.RED.toString() + "That font doesnt exist.")
                event.isCanceled = true
                return
            }
            this.reloadFont = true
        }
    }

    override fun onTick() {
        if (this.reloadFont) {
            Moon.textManager!!.init(false)
            this.reloadFont = false
        }
    }
}
