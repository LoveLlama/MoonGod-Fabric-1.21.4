package me.moon.features.command.commands

import me.moon.Moon
import me.moon.features.command.Command
import me.moon.features.setting.Bind
import net.minecraft.client.util.InputUtil
import net.minecraft.util.Formatting

class BindCommand : Command("bind", arrayOf("<module>", "<bind>")) {
    override fun execute(commands: Array<String>) {
        if (commands.isEmpty()) {
            Command.sendMessage("Please specify a module.")
        } else {
            val rkey = commands[1]
            val moduleName = commands[0]
            val module = Moon.moduleManager!!.getModuleByName(moduleName)
            if (module == null) {
                Command.sendMessage("Unknown module '" + moduleName + "'!")
            } else {
                var key = InputUtil.fromTranslationKey("key.keyboard." + rkey.lowercase())?.code ?: 0
                if (rkey.equals("none", ignoreCase = true)) {
                    key = -1
                }
                if (key == 0) {
                    Command.sendMessage("Unknown key '" + rkey + "'!")
                } else {
                    module.bind.setValue(Bind(key))
                    Command.sendMessage("Bind for " + Formatting.GREEN.toString() + module.getName() + Formatting.WHITE.toString() + " set to " + Formatting.GRAY.toString() + rkey.uppercase())
                }
            }
        }
    }
}
