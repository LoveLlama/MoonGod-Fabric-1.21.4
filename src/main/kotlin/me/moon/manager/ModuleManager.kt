package me.moon.manager

import me.moon.Moon
import me.moon.event.EventBus
import net.minecraft.client.MinecraftClient
import me.moon.event.events.Render2DEvent
import me.moon.event.events.Render3DEvent
import me.moon.features.Feature
import me.moon.features.gui.MoonGui
import me.moon.features.modules.Module
import me.moon.features.modules.client.ClickGui
import me.moon.features.modules.client.FontMod
import me.moon.features.modules.client.HUD
import me.moon.features.modules.combat.AsyncHit
import me.moon.features.modules.combat.Auto32k
import me.moon.features.modules.combat.AutoArmor
import me.moon.features.modules.combat.AutoTrap
import me.moon.features.modules.combat.AutoWeb
import me.moon.features.modules.combat.Criticals
import me.moon.features.modules.combat.FeetBreak
import me.moon.features.modules.combat.HoleFiller
import me.moon.features.modules.combat.Killaura
import me.moon.features.modules.combat.MoonAura
import me.moon.features.modules.combat.Offhand
import me.moon.features.modules.combat.Selftrap
import me.moon.features.modules.combat.Surround
import me.moon.features.modules.misc.AutoGG
import me.moon.features.modules.misc.ChatSuffix
import me.moon.features.modules.misc.Interact
import me.moon.features.modules.misc.MCF
import me.moon.features.modules.misc.Message
import me.moon.features.modules.misc.MultiTask
import me.moon.features.modules.misc.NoHitBox
import me.moon.features.modules.misc.PacketEat
import me.moon.features.modules.misc.Timer
import me.moon.features.modules.misc.ToolTips
import me.moon.features.modules.movement.ElytraFlight
import me.moon.features.modules.movement.NoFall
import me.moon.features.modules.movement.PlayerTweaks
import me.moon.features.modules.movement.ReverseStep
import me.moon.features.modules.movement.Speed
import me.moon.features.modules.movement.Sprint
import me.moon.features.modules.movement.Step
import me.moon.features.modules.movement.Strafe
import me.moon.features.modules.player.Anti32k
import me.moon.features.modules.player.AutoXP
import me.moon.features.modules.player.Blink
import me.moon.features.modules.player.Burrow
import me.moon.features.modules.player.FakePlayer
import me.moon.features.modules.player.MoonBreak
import me.moon.features.modules.player.Replenish
import me.moon.features.modules.render.BlockHighlight
import me.moon.features.modules.render.ESP
import me.moon.features.modules.render.Fullbright
import me.moon.features.modules.render.HoleESP
import me.moon.features.modules.render.NameTags
import me.moon.features.modules.render.NoRender
import me.moon.features.modules.render.Skeleton
import me.moon.features.modules.render.SurroundRender
import me.moon.util.Util
import net.minecraft.client.util.InputUtil
import net.minecraft.util.Formatting
import java.util.Comparator
import java.util.concurrent.Executors

class ModuleManager : Feature() {
    val modules: ArrayList<Module> = ArrayList()
    var sortedModules: MutableList<Module> = ArrayList()
    var sortedModulesABC: MutableList<String> = ArrayList()
    var animationThread: Animation? = null

    fun init() {
        modules.add(ClickGui())
        modules.add(FontMod())
        modules.add(HUD())
        modules.add(BlockHighlight())
        modules.add(HoleESP())
        modules.add(Skeleton())
        modules.add(Replenish())
        modules.add(FakePlayer())
        modules.add(MoonBreak())
        modules.add(Step())
        modules.add(Interact())
        modules.add(MCF())
        modules.add(AutoGG())
        modules.add(ToolTips())
        modules.add(Offhand())
        modules.add(Burrow())
        modules.add(Surround())
        modules.add(AutoTrap())
        modules.add(AutoWeb())
        modules.add(Fullbright())
        modules.add(NoRender())
        modules.add(MoonAura())
        modules.add(AsyncHit())
        modules.add(Killaura())
        modules.add(HoleFiller())
        modules.add(AutoArmor())
        modules.add(Sprint())
        modules.add(ElytraFlight())
        modules.add(MultiTask())
        modules.add(ESP())
        modules.add(Selftrap())
        modules.add(NoHitBox())
        modules.add(PlayerTweaks())
        modules.add(NoFall())
        modules.add(Criticals())
        modules.add(AutoXP())
        modules.add(Blink())
        modules.add(Anti32k())
        modules.add(ReverseStep())
        modules.add(SurroundRender())
        modules.add(FeetBreak())
        modules.add(Message())
        modules.add(PacketEat())
        modules.add(Timer())
        modules.add(ChatSuffix())
        modules.add(Strafe())
        modules.add(Speed())
        modules.add(Auto32k())
        modules.add(NameTags())
    }

    fun getModuleByName(name: String): Module? {
        for (module in modules) {
            if (module.getName().equals(name, ignoreCase = true)) {
                return module
            }
        }
        return null
    }

    fun <T : Module> getModuleByClass(clazz: Class<T>): T? {
        for (module in modules) {
            if (clazz.isInstance(module)) {
                return clazz.cast(module)
            }
        }
        return null
    }

    fun enableModule(clazz: Class<out Module>) {
        val module = getModuleByClass(clazz)
        if (module != null) {
            module.enable()
        }
    }

    fun disableModule(clazz: Class<out Module>) {
        val module = getModuleByClass(clazz)
        if (module != null) {
            module.disable()
        }
    }

    fun enableModule(name: String) {
        val module = getModuleByName(name)
        if (module != null) {
            module.enable()
        }
    }

    fun disableModule(name: String) {
        val module = getModuleByName(name)
        if (module != null) {
            module.disable()
        }
    }

    fun isModuleEnabled(name: String): Boolean {
        val module = getModuleByName(name)
        return module != null && module.isOn()
    }

    fun isModuleEnabled(clazz: Class<out Module>): Boolean {
        val module = getModuleByClass(clazz)
        return module != null && module.isOn()
    }

    fun getModuleByDisplayName(displayName: String): Module? {
        for (module in modules) {
            if (module.getDisplayName().equals(displayName, ignoreCase = true)) {
                return module
            }
        }
        return null
    }

    fun getEnabledModules(): ArrayList<Module> {
        val enabledModules = ArrayList<Module>()
        for (module in modules) {
            if (module.isEnabled()) {
                enabledModules.add(module)
            }
        }
        return enabledModules
    }

    fun getEnabledModulesName(): ArrayList<String> {
        val enabledModules = ArrayList<String>()
        for (module in modules) {
            if (module.isEnabled() && module.isDrawn()) {
                enabledModules.add(module.getFullArrayString())
            }
        }
        return enabledModules
    }

    fun getModulesByCategory(category: Module.Category): ArrayList<Module> {
        val modulesCategory = ArrayList<Module>()
        modules.forEach { module ->
            if (module.getCategory() == category) {
                modulesCategory.add(module)
            }
        }
        return modulesCategory
    }

    fun getCategories(): List<Module.Category> {
        return Module.Category.values().toList()
    }

    fun onLoad() {
        modules.stream().filter(Module::listening).forEach { EventBus.register(it) }
        modules.forEach { it.onLoad() }
    }

    fun onUpdate() {
        modules.stream().filter(Feature::isEnabled).forEach { it.onUpdate() }
    }

    fun onTick() {
        modules.stream().filter(Feature::isEnabled).forEach { it.onTick() }
    }

    fun onRender2D(event: Render2DEvent) {
        modules.stream().filter(Feature::isEnabled).forEach { it.onRender2D(event) }
    }

    fun onRender3D(event: Render3DEvent) {
        modules.stream().filter(Feature::isEnabled).forEach { it.onRender3D(event) }
    }

    fun sortModules(reverse: Boolean) {
        sortedModules = getEnabledModules()
            .stream()
            .filter { it.isDrawn() }
            .sorted(Comparator.comparingDouble { module -> renderer.getStringWidth(module.getFullArrayString()) * (if (reverse) -1.0 else 1.0) })
            .toList() as ArrayList<Module>
    }

    fun sortModulesABC() {
        sortedModulesABC = ArrayList(getEnabledModulesName())
        sortedModulesABC.sortWith(String.CASE_INSENSITIVE_ORDER)
    }

    fun onLogout() {
        modules.forEach { it.onLogout() }
    }

    fun onLogin() {
        modules.forEach { it.onLogin() }
    }

    fun onUnload() {
        modules.forEach { EventBus.unregister(it) }
        modules.forEach { it.onUnload() }
    }

    fun onUnloadPost() {
        for (module in modules) {
            module.enabled.setValue(false)
        }
    }

    fun onKeyPressed(eventKey: Int) {
        if (eventKey != 0 && mc.currentScreen !is MoonGui) {
            modules.forEach { module ->
                if (module.getBind().getKey() == eventKey) {
                    module.toggle()
                }
            }
        }
    }

    inner class Animation : Thread {
        private val service = Executors.newSingleThreadScheduledExecutor()

        constructor() : super("Animation")

        override fun run() {
            if (HUD.getInstance().renderingMode.getValue() == HUD.RenderingMode.Length) {
                for (module in sortedModules) {
                    val text = module.getDisplayName() + Formatting.GRAY.toString() + (if (module.getDisplayInfo() != null) " [" + Formatting.WHITE.toString() + module.getDisplayInfo() + Formatting.GRAY.toString() + "]" else "")
                    module.offset = (renderer.getStringWidth(text).toFloat()) / HUD.getInstance().animationHorizontalTime.getValue().toFloat()
                    module.vOffset = (renderer.getFontHeight().toFloat()) / HUD.getInstance().animationVerticalTime.getValue().toFloat()
                    if (module.isEnabled() && HUD.getInstance().animationHorizontalTime.getValue().toDouble() != 1.0) {
                        if (module.arrayListOffset > module.offset && MinecraftClient.getInstance().world != null) {
                            module.arrayListOffset -= module.offset
                            module.sliding = true
                        }
                    } else if (module.isDisabled() && HUD.getInstance().animationHorizontalTime.getValue().toDouble() != 1.0) {
                        if (module.arrayListOffset < renderer.getStringWidth(text).toFloat() && MinecraftClient.getInstance().world != null) {
                            module.arrayListOffset += module.offset
                            module.sliding = true
                        } else {
                            module.sliding = false
                        }
                    }
                }
            } else {
                for (e in sortedModulesABC) {
                    val module = Moon.moduleManager!!.getModuleByName(e)!!
                    val text = module.getDisplayName() + Formatting.GRAY.toString() + (if (module.getDisplayInfo() != null) " [" + Formatting.WHITE.toString() + module.getDisplayInfo() + Formatting.GRAY.toString() + "]" else "")
                    module.offset = (renderer.getStringWidth(text).toFloat()) / HUD.getInstance().animationHorizontalTime.getValue().toFloat()
                    module.vOffset = (renderer.getFontHeight().toFloat()) / HUD.getInstance().animationVerticalTime.getValue().toFloat()
                    if (module.isEnabled() && HUD.getInstance().animationHorizontalTime.getValue().toDouble() != 1.0) {
                        if (module.arrayListOffset > module.offset && MinecraftClient.getInstance().world != null) {
                            module.arrayListOffset -= module.offset
                            module.sliding = true
                        }
                    } else if (module.isDisabled() && HUD.getInstance().animationHorizontalTime.getValue().toDouble() != 1.0) {
                        if (module.arrayListOffset < renderer.getStringWidth(text).toFloat() && MinecraftClient.getInstance().world != null) {
                            module.arrayListOffset += module.offset
                            module.sliding = true
                        } else {
                            module.sliding = false
                        }
                    }
                }
            }
        }

        override fun start() {
            service.scheduleAtFixedRate(this, 0L, 1L, java.util.concurrent.TimeUnit.MILLISECONDS)
        }
    }
}
