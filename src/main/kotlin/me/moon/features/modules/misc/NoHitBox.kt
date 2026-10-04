package me.moon.features.modules.misc

import me.moon.features.modules.Module
import me.moon.features.setting.Setting

class NoHitBox : Module("NoHitBox", "NoHitBox.", Module.Category.MISC, false, false, false) {
    var pickaxe = register(Setting("Pickaxe", true))
    var crystal = register(Setting("Crystal", true))
    var gapple = register(Setting("Gapple", true))
    var block = register(Setting("Block", true))
    var sword = register(Setting("Sword", true))

    init {
        setInstance()
    }

    private fun setInstance() {
        INSTANCE = this
    }

    companion object {
        @JvmField
        var INSTANCE: NoHitBox = NoHitBox()

        @JvmStatic
        fun getINSTANCE(): NoHitBox = INSTANCE
    }
}
