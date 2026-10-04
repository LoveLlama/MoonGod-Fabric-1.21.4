package me.moon.features.command.commands

import me.moon.Moon
import me.moon.features.command.Command

class ReloadCommand : Command("reload", emptyArray()) {
    override fun execute(commands: Array<String>) {
        Moon.reload()
    }
}
