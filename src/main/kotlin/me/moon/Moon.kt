package me.moon

import me.moon.manager.ColorManager
import me.moon.manager.CommandManager
import me.moon.manager.ConfigManager
import me.moon.manager.EventManager
import me.moon.manager.FileManager
import me.moon.manager.FriendManager
import me.moon.manager.HoleManager
import me.moon.manager.InventoryManager
import me.moon.manager.ModuleManager
import me.moon.manager.PacketManager
import me.moon.manager.PositionManager
import me.moon.manager.PotionManager
import me.moon.manager.ReloadManager
import me.moon.manager.RotationManager
import me.moon.manager.ServerManager
import me.moon.manager.SpeedManager
import me.moon.manager.TextManager
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.minecraft.client.MinecraftClient
import org.slf4j.LoggerFactory

object Moon : ClientModInitializer {
    const val MODID = "moon"
    const val MODNAME = "MoonGod"
    const val MODVER = "777"
    val LOGGER = LoggerFactory.getLogger("MoonGod")

    var commandManager: CommandManager? = null
    var friendManager: FriendManager? = null
    var moduleManager: ModuleManager? = null
    var packetManager: PacketManager? = null
    var colorManager: ColorManager? = null
    var holeManager: HoleManager? = null
    var inventoryManager: InventoryManager? = null
    var potionManager: PotionManager? = null
    var rotationManager: RotationManager? = null
    var positionManager: PositionManager? = null
    var speedManager: SpeedManager? = null
    var reloadManager: ReloadManager? = null
    var fileManager: FileManager? = null
    var configManager: ConfigManager? = null
    var serverManager: ServerManager? = null
    var eventManager: EventManager? = null
    var textManager: TextManager? = null

    private var unloaded = false

    fun load() {
        unloaded = false
        if (reloadManager != null) {
            reloadManager!!.unload()
            reloadManager = null
        }

        textManager = TextManager()
        commandManager = CommandManager()
        friendManager = FriendManager()
        moduleManager = ModuleManager()
        rotationManager = RotationManager()
        packetManager = PacketManager()
        eventManager = EventManager()
        speedManager = SpeedManager()
        potionManager = PotionManager()
        inventoryManager = InventoryManager()
        serverManager = ServerManager()
        fileManager = FileManager()
        colorManager = ColorManager()
        positionManager = PositionManager()
        configManager = ConfigManager()
        holeManager = HoleManager()
        LOGGER.info("Managers loaded")
        moduleManager!!.init()
        LOGGER.info("Modules loaded")
        configManager!!.init()
        eventManager!!.init()
        LOGGER.info("EventManager loaded")
        textManager!!.init(true)
        moduleManager!!.onLoad()
        LOGGER.info("Moon successfully loaded!")
    }

    fun unload(unload: Boolean) {
        if (unload) {
            reloadManager = ReloadManager()
            reloadManager!!.init(if (commandManager != null) commandManager!!.getPrefix() else ".")
        }

        onUnload()
        eventManager = null
        friendManager = null
        speedManager = null
        holeManager = null
        positionManager = null
        rotationManager = null
        configManager = null
        commandManager = null
        colorManager = null
        serverManager = null
        fileManager = null
        potionManager = null
        inventoryManager = null
        moduleManager = null
        textManager = null
        LOGGER.info("\nMoon unloaded!\n")
    }

    fun reload() {
        unload(false)
        load()
    }

    fun onUnload() {
        if (!unloaded) {
            eventManager!!.onUnload()
            moduleManager!!.onUnload()
            configManager!!.saveConfig(configManager!!.config.replaceFirst("moon/", ""))
            moduleManager!!.onUnloadPost()
            unloaded = true
        }
    }

    override fun onInitializeClient() {
        
        
        ClientLifecycleEvents.CLIENT_STARTED.register {
            MinecraftClient.getInstance().window.setTitle("MoonGod")
            load()
        }
    }
}
