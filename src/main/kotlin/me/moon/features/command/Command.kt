package me.moon.features.command

import me.moon.Moon
import me.moon.features.Feature
import net.minecraft.client.MinecraftClient
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.util.regex.Matcher
import java.util.regex.Pattern

abstract class Command : Feature {
    @JvmField
    protected var name: String? = null
    @JvmField
    protected var commands: Array<String> = arrayOf("")

    constructor(name: String) : super(name) {
        this.name = name
        this.commands = arrayOf("")
    }

    constructor(name: String, commands: Array<String>) : super(name) {
        this.name = name
        this.commands = commands
    }

    abstract fun execute(commands: Array<String>)

    override fun getName(): String? = name

    fun getCommands(): Array<String> = commands

    companion object {
        private val pattern: Pattern = Pattern.compile("&[0123456789abcdefrlosmk]")

        @JvmStatic
        fun chatMessage(text: String): MutableText {
            val matcher: Matcher = pattern.matcher(text)
            val stringBuffer = StringBuffer()
            while (matcher.find()) {
                val replacement: String = matcher.group().substring(1)
                matcher.appendReplacement(stringBuffer, replacement)
            }
            matcher.appendTail(stringBuffer)
            return Text.literal(stringBuffer.toString())
        }

        @JvmStatic
        fun sendMessage(message: String) {
            sendSilentMessage(Moon.commandManager!!.getClientMessage() + " " + Formatting.GRAY.toString() + message)
        }

        @JvmStatic
        fun sendSilentMessage(message: String) {
            if (!nullCheck()) {
                MinecraftClient.getInstance().inGameHud.chatHud.addMessage(chatMessage(message))
            }
        }

        @JvmStatic
        fun getCommandPrefix(): String = Moon.commandManager!!.getPrefix()
    }
}
