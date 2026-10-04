package me.moon.features.modules.render

import me.moon.event.SubscribeEvent
import me.moon.event.events.NoRenderEvent
import me.moon.features.modules.Module
import me.moon.features.setting.Setting
import net.minecraft.entity.effect.StatusEffects

class NoRender : Module("NoRender", "No Render", Module.Category.RENDER, true, false, false) {
    var armor = register(Setting("Armor", true))
    var fire = register(Setting("Frie", true))
    var blind = register(Setting("Blind", true))
    var nausea = register(Setting("Nausea", true))
    var hurtCam = register(Setting("HurtCam", true))
    var skyLightUpdate = register(Setting("SkyLightUpdate", true))

    init { setInstance() }
    private fun setInstance() { INSTANCE = this }

    override fun onUpdate() {
        if (this.blind.getValue() && mc.player!!.hasStatusEffect(StatusEffects.BLINDNESS)) {
            mc.player!!.removeStatusEffect(StatusEffects.BLINDNESS)
        }
        if (this.nausea.getValue() && mc.player!!.hasStatusEffect(StatusEffects.NAUSEA)) {
            mc.player!!.removeStatusEffect(StatusEffects.NAUSEA)
        }
    }

    @SubscribeEvent
    fun NoRenderEventListener(event: NoRenderEvent) {
        if (event.stage == 0 && this.armor.getValue()) {
            event.isCanceled = true
        } else if (event.stage == 1 && this.hurtCam.getValue()) {
            event.isCanceled = true
        }
    }

    companion object {
        @JvmField
        var INSTANCE: NoRender = NoRender()

        @JvmStatic
        fun getInstance(): NoRender = INSTANCE
    }
}
