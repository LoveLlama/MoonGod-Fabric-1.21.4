package me.moon.features.command.commands

import me.moon.features.command.Command
import me.moon.util.PlayerUtil
import net.minecraft.util.Formatting
import java.util.UUID

class HistoryCommand : Command("history", arrayOf("<player>")) {
    override fun execute(commands: Array<String>) {
        if (commands.isEmpty()) {
            Command.sendMessage(Formatting.RED.toString() + "Please specify a player.")
            return
        }
        val uuid: UUID
        try {
            uuid = PlayerUtil.getUUIDFromName(commands[0])!!
        } catch (e: Exception) {
            Command.sendMessage("An error occured.")
            return
        }
        val names: List<String>?
        try {
            names = PlayerUtil.getHistoryOfNames(uuid)
        } catch (e: Exception) {
            Command.sendMessage("An error occured.")
            return
        }
        if (names != null) {
            Command.sendMessage(commands[0] + "Â´s name history:")
            for (name in names) {
                Command.sendMessage(name)
            }
        } else {
            Command.sendMessage("No names found.")
        }
    }
}
