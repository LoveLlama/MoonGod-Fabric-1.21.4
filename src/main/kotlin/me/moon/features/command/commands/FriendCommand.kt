package me.moon.features.command.commands

import me.moon.Moon
import me.moon.features.command.Command
import me.moon.manager.FriendManager
import net.minecraft.util.Formatting

class FriendCommand : Command("friend", arrayOf("<add/del/name/clear>", "<name>")) {
    override fun execute(commands: Array<String>) {
        if (!commands.isEmpty()) {
            if (commands.size == 1) {
                if ("reset" == commands[0]) {
                    Moon.friendManager!!.onLoad()
                    Command.sendMessage("Friends got reset.")
                } else {
                    Command.sendMessage(commands[0] + (if (Moon.friendManager!!.isFriend(commands[0])) " is friended." else " isn't friended."))
                }
            } else {
                if (commands.size >= 1) {
                    when (commands[0]) {
                        "add" -> {
                            Moon.friendManager!!.addFriend(commands[1])
                            Command.sendMessage(Formatting.GREEN.toString() + commands[1] + " has been friended")
                            return
                        }
                        "del" -> {
                            Moon.friendManager!!.removeFriend(commands[1])
                            Command.sendMessage(Formatting.RED.toString() + commands[1] + " has been unfriended")
                            return
                        }
                        else -> Command.sendMessage(".friend add <name> / .friend del <name>)")
                    }
                }
            }
        } else {
            if (Moon.friendManager!!.getFriends().isEmpty()) {
                Command.sendMessage(".friend add <name> / .friend del <name>")
            } else {
                var f = "Friends: "
                for (friend in Moon.friendManager!!.getFriends()) {
                    try {
                        f = f + friend.getUsername() + ", "
                    } catch (e: Exception) {
                    }
                }
                Command.sendMessage(f)
            }
        }
    }
}
