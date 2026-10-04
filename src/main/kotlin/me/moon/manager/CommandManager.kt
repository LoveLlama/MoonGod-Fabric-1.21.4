package me.moon.manager

import me.moon.features.Feature
import me.moon.features.command.Command
import me.moon.features.command.commands.BindCommand
import me.moon.features.command.commands.ConfigCommand
import me.moon.features.command.commands.FriendCommand
import me.moon.features.command.commands.HelpCommand
import me.moon.features.command.commands.HistoryCommand
import me.moon.features.command.commands.ModuleCommand
import me.moon.features.command.commands.PrefixCommand
import me.moon.features.command.commands.ReloadCommand
import me.moon.features.command.commands.ReloadSoundCommand
import me.moon.features.command.commands.UnloadCommand
import net.minecraft.util.Formatting
import java.util.LinkedList

class CommandManager : Feature("Command") {
    private val commands = ArrayList<Command>()
    private var clientMessage = "[MoonGod]"
    private var prefix = "."

    init {
        commands.add(BindCommand())
        commands.add(ModuleCommand())
        commands.add(PrefixCommand())
        commands.add(ConfigCommand())
        commands.add(FriendCommand())
        commands.add(HelpCommand())
        commands.add(ReloadCommand())
        commands.add(UnloadCommand())
        commands.add(ReloadSoundCommand())
        commands.add(HistoryCommand())
    }

    private fun strip(str: String, key: String): String {
        return if (str.startsWith(key) && str.endsWith(key) && str.length >= key.length * 2) str.substring(key.length, str.length - key.length) else str
    }

    fun executeCommand(command: String) {
        val parts = command.split(" (?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)".toRegex()).toTypedArray()
        val name = parts[0].substring(1)
        val args = removeElement(parts, 0)
        for (i in args.indices) {
            if (args[i] != null) {
                args[i] = strip(args[i], "\"")
            }
        }
        for (c in commands) {
            if (c.getName().equals(name, ignoreCase = true)) {
                c.execute(args)
                return
            }
        }
        Command.sendMessage(Formatting.GRAY.toString() + "Command not found, type 'help' for the commands list.")
    }

    fun getCommandByName(name: String): Command? {
        for (command in commands) {
            if (command.getName() == name) {
                return command
            }
        }
        return null
    }

    fun getCommands(): ArrayList<Command> = commands

    fun getClientMessage(): String = clientMessage

    fun setClientMessage(clientMessage: String) {
        this.clientMessage = clientMessage
    }

    fun getPrefix(): String = prefix

    fun setPrefix(prefix: String) {
        this.prefix = prefix
    }

    companion object {
        @JvmStatic
        fun removeElement(input: Array<String>, indexToDelete: Int): Array<String> {
            val result = LinkedList<String>()
            for (i in input.indices) {
                if (i != indexToDelete) {
                    result.add(input[i])
                }
            }
            return result.toTypedArray()
        }
    }
}
