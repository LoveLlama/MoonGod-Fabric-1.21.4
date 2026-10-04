package me.moon.features.modules

import me.moon.Moon
import me.moon.event.EventBus
import me.moon.event.events.ClientEvent
import me.moon.event.events.Render2DEvent
import me.moon.event.events.Render3DEvent
import me.moon.features.Feature
import me.moon.features.command.Command
import me.moon.features.modules.client.HUD
import me.moon.features.setting.Bind
import me.moon.features.setting.Setting
import net.minecraft.text.Text
import net.minecraft.util.Formatting

open class Module(
    name: String,
    private val description: String,
    private val category: Category,
    hasListener: Boolean,
    hidden: Boolean,
    alwaysListening: Boolean
) : Feature(name) {
    @JvmField
    var enabled: Setting<Boolean> = register(Setting("Enabled", false))
    @JvmField
    var drawn: Setting<Boolean> = register(Setting("Drawn", true))
    @JvmField
    var bind: Setting<Bind> = register(Setting("Keybind", Bind(-1)))
    lateinit var displayName: Setting<String>
    @JvmField
    var hasListener: Boolean
    @JvmField
    var alwaysListening: Boolean
    @JvmField
    var hidden: Boolean
    @JvmField
    var arrayListOffset: Float = 0.0F
    @JvmField
    var arrayListVOffset: Float = 0.0F
    @JvmField
    var offset: Float = 0.0F
    @JvmField
    var vOffset: Float = 0.0F
    @JvmField
    var sliding: Boolean = false

    init {
        displayName = register(Setting("DisplayName", name))
        this.hasListener = hasListener
        this.hidden = hidden
        this.alwaysListening = alwaysListening
    }

    fun isSliding(): Boolean = sliding

    open fun onEnable() {
    }

    open fun onDisable() {
    }

    open fun onToggle() {
    }

    open fun onLoad() {
    }

    open fun onTick() {
    }

    open fun onLogin() {
    }

    open fun onLogout() {
    }

    open fun onUpdate() {
    }

    open fun onRender2D(event: Render2DEvent) {
    }

    open fun onRender3D(event: Render3DEvent) {
    }

    open fun onUnload() {
    }

    open fun getDisplayInfo(): String? = null

    fun isOn(): Boolean = enabled.getValue()

    fun isOff(): Boolean = !enabled.getValue()

    fun setEnabled(enabled: Boolean) {
        if (enabled) {
            enable()
        } else {
            disable()
        }
    }

    open fun enable() {
        enabled.setValue(java.lang.Boolean.TRUE)
        onToggle()
        onEnable()
        if (HUD.getInstance().notifyToggles.getValue()) {
            val text = Text.literal(Moon.commandManager!!.getClientMessage() + " " + Formatting.GREEN.toString() + displayName.getValue() + " toggled on.")
            mc.inGameHud?.chatHud?.addMessage(text)
        }
        if (isOn() && hasListener && !alwaysListening) {
            EventBus.register(this)
        }
    }

    open fun disable() {
        if (hasListener && !alwaysListening) {
            EventBus.unregister(this)
        }
        enabled.setValue(false)
        if (HUD.getInstance().notifyToggles.getValue()) {
            val text = Text.literal(Moon.commandManager!!.getClientMessage() + " " + Formatting.RED.toString() + displayName.getValue() + " toggled off.")
            mc.inGameHud?.chatHud?.addMessage(text)
        }
        onToggle()
        onDisable()
    }

    open fun toggle() {
        val event = ClientEvent(if (!isEnabled()) 1 else 0, this)
        EventBus.post(event)
        if (!event.isCanceled) {
            setEnabled(!isEnabled())
        }
    }

    fun getDisplayName(): String = displayName.getValue()

    fun setDisplayName(name: String) {
        val module = Moon.moduleManager!!.getModuleByDisplayName(name)
        val originalModule = Moon.moduleManager!!.getModuleByName(name)
        if (module == null && originalModule == null) {
            Command.sendMessage(getDisplayName() + ", name: " + getName() + ", has been renamed to: " + name)
            displayName.setValue(name)
        } else {
            Command.sendMessage(Formatting.RED.toString() + "A module of this name already exists.")
        }
    }

    fun getDescription(): String = description

    fun isDrawn(): Boolean = drawn.getValue()

    fun setDrawn(drawn: Boolean) {
        this.drawn.setValue(drawn)
    }

    fun getCategory(): Category = category

    open fun getInfo(): String? = null

    fun getBind(): Bind = bind.getValue()

    fun setBind(key: Int) {
        bind.setValue(Bind(key))
    }

    fun listening(): Boolean = hasListener && isOn() || alwaysListening

    fun getFullArrayString(): String {
        return getDisplayName() + Formatting.GRAY.toString() + (if (getDisplayInfo() != null) " [" + Formatting.WHITE.toString() + getDisplayInfo() + Formatting.GRAY.toString() + "]" else "")
    }

    enum class Category(private val label: String) {
        COMBAT("Combat"),
        MISC("Misc"),
        RENDER("Render"),
        MOVEMENT("Movement"),
        PLAYER("Player"),
        CLIENT("Client");

        fun getName(): String = label
    }
}
