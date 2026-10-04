package me.moon.features.modules.movement

import me.moon.features.modules.Module
import me.moon.features.setting.Setting

class Sprint : Module("Sprint", "Modifies sprinting", Module.Category.MOVEMENT, true, false, false) {
    var mode = register(Setting("Mode", Mode.LEGIT))

    init {
        setInstance()
    }

    private fun setInstance() {
        INSTANCE = this
    }

    override fun onUpdate() {
        when (this.mode.getValue()) {
            Mode.RAGE -> {
                if ((mc.options.forwardKey.isPressed()
                        || mc.options.backKey.isPressed()
                        || mc.options.leftKey.isPressed()
                        || mc.options.rightKey.isPressed())
                    && !mc.player!!.isSneaking
                    && !mc.player!!.horizontalCollision
                    && !(mc.player!!.hungerManager.foodLevel.toFloat() <= 6.0f)
                ) {
                    mc.player!!.isSprinting = true
                }
            }
            Mode.LEGIT -> {
                if (mc.options.forwardKey.isPressed()
                    && !mc.player!!.isSneaking
                    && !mc.player!!.isUsingItem
                    && !mc.player!!.horizontalCollision
                    && !(mc.player!!.hungerManager.foodLevel.toFloat() <= 6.0f)
                    && mc.currentScreen == null
                ) {
                    mc.player!!.isSprinting = true
                }
            }
        }
    }

    override fun onDisable() {
        if (!nullCheck()) {
            mc.player!!.isSprinting = false
        }
    }

    override fun getDisplayInfo(): String {
        return this.mode.getValue().name
    }

    enum class Mode {
        LEGIT,
        RAGE
    }

    companion object {
        @JvmField
        var INSTANCE: Sprint = Sprint()

        @JvmStatic
        fun getInstance(): Sprint = INSTANCE
    }
}
