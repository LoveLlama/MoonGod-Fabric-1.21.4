package me.moon.features.command.commands

import me.moon.Moon
import me.moon.features.command.Command

class UnloadCommand : Command("unload", emptyArray()) {
    override fun execute(commands: Array<String>) {
        Moon.unload(true)
    }
}
