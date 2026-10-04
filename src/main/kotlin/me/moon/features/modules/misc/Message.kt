package me.moon.features.modules.misc

import me.moon.features.modules.Module
import me.moon.features.setting.Setting
import me.moon.util.Timer
import kotlin.random.Random

class Message : Module("Message", "Message", Module.Category.MISC, true, false, false) {
    private val timer = Timer()
    private val custom = register(Setting("Custom", "/kit create "))
    private val random = register(Setting("Random", 1, 1, 20))
    private val delay = register(Setting("Delay", 100, 0, 10000))

    override fun onTick() {
        if (!fullNullCheck()) {
            if (this.timer.passedMs(this.delay.getValue().toLong())) {
                val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
                val sb = StringBuilder()
                repeat(this.random.getValue()) { sb.append(alphabet[Random.nextInt(alphabet.length)]) }
                mc.player!!.networkHandler.sendChatMessage(this.custom.getValue() + sb.toString())
                this.timer.reset()
            }
        }
    }
}
