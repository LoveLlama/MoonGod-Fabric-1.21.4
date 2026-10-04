package me.moon.features.modules.misc

import me.moon.features.modules.Module

class PacketEat : Module("PacketEat", "PacketEat", Module.Category.MISC, true, false, false) {
    init {
        setInstance()
    }

    private fun setInstance() {
        INSTANCE = this
    }

    companion object {
        @JvmField
        var INSTANCE: PacketEat = PacketEat()

        @JvmStatic
        fun getInstance(): PacketEat = INSTANCE
    }
}
