package me.moon.features.command.commands

import me.moon.features.command.Command
import net.minecraft.util.Formatting

class ReloadSoundCommand : Command("sound", emptyArray()) {
    override fun execute(commands: Array<String>) {
        try {
            mc.soundManager.reloadSounds()
            Command.sendMessage(Formatting.GREEN.toString() + "Reloaded Sound System.")
        } catch (e: Exception) {
            Command.sendMessage(Formatting.RED.toString() + "Couldnt Reload Sound System!")
        }
    }
}
