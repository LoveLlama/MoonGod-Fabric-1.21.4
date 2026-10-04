package me.moon.features.command.commands

import me.moon.Moon
import me.moon.features.command.Command
import net.minecraft.util.Formatting
import java.io.File

class ConfigCommand : Command("config", arrayOf("<save/load>")) {
    override fun execute(commands: Array<String>) {
        if (commands.isEmpty()) {
            Command.sendMessage(".config save <list> / .config load <list>")
        } else {
            if (commands.size == 1) {
                if ("list" == commands[0]) {
                    var configs = "Configs: "
                    val file = File("moon/")
                    val directories = (file.listFiles() ?: emptyArray()).toList().filter { it.isDirectory }.filter { it.name != "util" }
                    val builder = StringBuilder(configs)
                    for (file1 in directories) {
                        builder.append(file1.name + ", ")
                    }
                    configs = builder.toString()
                    Command.sendMessage(configs)
                } else {
                    Command.sendMessage(".config save <list> / .config load <list>")
                }
            }
            if (commands.size >= 2) {
                when (commands[0]) {
                    "save" -> {
                        Moon.configManager!!.saveConfig(commands[1])
                        Command.sendMessage(Formatting.GREEN.toString() + "Config '" + commands[1] + "' has been saved.")
                        return
                    }
                    "load" -> {
                        if (Moon.configManager!!.configExists(commands[1])) {
                            Moon.configManager!!.loadConfig(commands[1])
                            Command.sendMessage(Formatting.GREEN.toString() + "Config '" + commands[1] + "' has been loaded.")
                        } else {
                            Command.sendMessage(Formatting.RED.toString() + "Config '" + commands[1] + "' does not exist.")
                        }
                        return
                    }
                    else -> Command.sendMessage(".config save <list> / .config load <list>")
                }
            }
        }
    }
}
