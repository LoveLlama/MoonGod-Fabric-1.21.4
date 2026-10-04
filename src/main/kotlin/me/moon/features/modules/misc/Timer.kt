package me.moon.features.modules.misc

import me.moon.features.modules.Module
import me.moon.features.setting.Setting
import me.moon.util.EntityUtil

class Timer : Module("Timer", "Timer", Module.Category.MISC, true, false, false) {
    var timer = register(Setting("Timer", 1.1f, 1.0f, 2.0f))

    override fun onTick() {
        if (!fullNullCheck()) {
            EntityUtil.setTimer(this.timer.getValue())
        }
    }

    override fun onDisable() {
        EntityUtil.resetTimer()
    }

    override fun getDisplayInfo(): String {
        return this.timer.getValue().toString()
    }
}
