package me.moon.features.command.commands

import me.moon.Moon
import me.moon.features.command.Command
import net.minecraft.util.Formatting

class PrefixCommand : Command("prefix", arrayOf("<char>")) {
    override fun execute(commands: Array<String>) {
        if (commands.isEmpty()) {
            Command.sendMessage(Formatting.GREEN.toString() + "Current prefix is " + Moon.commandManager!!.getPrefix())
        } else {
            Moon.commandManager!!.setPrefix(commands[0])
            Command.sendMessage("Prefix changed to " + Formatting.GRAY.toString() + commands[0])
        }
    }
}
