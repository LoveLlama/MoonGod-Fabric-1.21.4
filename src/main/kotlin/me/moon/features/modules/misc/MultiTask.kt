package me.moon.features.modules.misc

import me.moon.features.modules.Module

class MultiTask : Module("MultiTask", "URBBRGROUG", Module.Category.MISC, true, false, false) {
    init {
        setInstance()
    }

    private fun setInstance() {
        INSTANCE = this
    }

    companion object {
        @JvmField
        var INSTANCE: MultiTask = MultiTask()

        @JvmStatic
        fun getInstance(): MultiTask = INSTANCE
    }
}
