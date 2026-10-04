package me.moon.features.command.commands

import me.moon.Moon
import me.moon.features.command.Command
import net.minecraft.util.Formatting

class HelpCommand : Command("help") {
    override fun execute(commands: Array<String>) {
        Command.sendMessage("Commands: ")
        for (command in Moon.commandManager!!.getCommands()) {
            Command.sendMessage(Formatting.GRAY.toString() + Moon.commandManager!!.getPrefix() + command.getName())
        }
    }
}
