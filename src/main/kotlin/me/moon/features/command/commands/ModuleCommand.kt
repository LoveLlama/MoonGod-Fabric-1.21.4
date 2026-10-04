package me.moon.features.command.commands

import com.google.gson.JsonParser
import me.moon.Moon
import me.moon.features.command.Command
import me.moon.features.setting.Setting
import me.moon.manager.ConfigManager
import net.minecraft.util.Formatting

class ModuleCommand : Command("module", arrayOf("<module>", "<set/reset>", "<setting>", "<value>")) {
    override fun execute(commands: Array<String>) {
        if (commands.isEmpty()) {
            Command.sendMessage("Modules: ")
            for (category in Moon.moduleManager!!.getCategories()) {
                var modules = category.getName() + ": "
                for (module1 in Moon.moduleManager!!.getModulesByCategory(category)) {
                    modules = modules + (if (module1.isEnabled()) Formatting.GREEN else Formatting.RED) + module1.getName() + Formatting.WHITE.toString() + ", "
                }
                Command.sendMessage(modules)
            }
        } else {
            var module = Moon.moduleManager!!.getModuleByDisplayName(commands[0])
            if (module == null) {
                module = Moon.moduleManager!!.getModuleByName(commands[0])
                if (module == null) {
                    Command.sendMessage("This module doesnt exist.")
                } else {
                    Command.sendMessage(" This is the original name of the module. Its current name is: " + module.getDisplayName())
                }
            } else if (commands.size == 1) {
                Command.sendMessage(module.getDisplayName() + " : " + module.getDescription())
                for (setting2 in module.getSettings()) {
                    Command.sendMessage(setting2.getName() + " : " + setting2.getValue() + ", " + setting2.getDescription())
                }
            } else if (commands.size == 2) {
                if (commands[1].equals("set", ignoreCase = true)) {
                    Command.sendMessage("Please specify a setting.")
                } else if (commands[1].equals("reset", ignoreCase = true)) {
                    for (setting3 in module.getSettings()) {
                        setting3.setValue(setting3.getDefaultValue())
                    }
                } else {
                    Command.sendMessage("This command doesnt exist.")
                }
            } else if (commands.size == 3) {
                Command.sendMessage("Please specify a value.")
            } else {
                if (commands.size == 4) {
                    val setting = module.getSettingByName(commands[2])
                    if (setting != null) {
                        val jp = JsonParser()
                        if (setting.getType().equals("String", ignoreCase = true)) {
                            (setting as Setting<Any?>).setValue(commands[3])
                            Command.sendMessage(Formatting.DARK_GRAY.toString() + module.getName() + " " + setting.getName() + " has been set to " + commands[3] + ".")
                            return
                        }
                        try {
                            if (setting.getName().equals("Enabled", ignoreCase = true)) {
                                if (commands[3].equals("true", ignoreCase = true)) {
                                    module.enable()
                                }
                                if (commands[3].equals("false", ignoreCase = true)) {
                                    module.disable()
                                }
                            }
                            ConfigManager.setValueFromJson(module, setting, jp.parse(commands[3]))
                        } catch (e: Exception) {
                            Command.sendMessage("Bad Value! This setting requires a: " + setting.getType() + " value.")
                            return
                        }
                        Command.sendMessage(Formatting.GRAY.toString() + module.getName() + " " + setting.getName() + " has been set to " + commands[3] + ".")
                    }
                }
            }
        }
    }
}
