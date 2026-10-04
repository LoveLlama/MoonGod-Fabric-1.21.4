package me.moon.features.modules.render

import net.minecraft.network.packet.Packet

import me.moon.event.SubscribeEvent
import me.moon.event.events.PacketEvent
import me.moon.features.modules.Module
import me.moon.features.setting.Setting
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.network.packet.s2c.play.EntityStatusEffectS2CPacket

class Fullbright : Module("Fullbright", "Makes your game brighter.", Module.Category.RENDER, true, false, false) {
    var mode = register(Setting("Mode", Mode.GAMMA))
    var effects = register(Setting("Effects", false))
    private var previousSetting = 1.0f

    override fun onEnable() {
        this.previousSetting = mc.options.gamma.value.toFloat()
    }

    override fun onUpdate() {
        if (this.mode.getValue() == Mode.GAMMA) {
            mc.options.gamma.value = 1.0
        }
        if (this.mode.getValue() == Mode.POTION) {
            mc.player!!.addStatusEffect(StatusEffectInstance(StatusEffects.NIGHT_VISION, 5210))
        }
    }

    override fun onDisable() {
        if (this.mode.getValue() == Mode.POTION && mc.player != null) {
            mc.player!!.removeStatusEffect(StatusEffects.NIGHT_VISION)
        }
        mc.options.gamma.value = this.previousSetting.toDouble()
    }

    @SubscribeEvent
    fun onPacketReceive(event: PacketEvent.Receive) {
        if (event.getPacket<Packet<*>>() is EntityStatusEffectS2CPacket && this.effects.getValue()) {
            val packet = event.getPacket<EntityStatusEffectS2CPacket>()
            if (mc.player != null && packet.getEntityId() == mc.player!!.id) {
                event.isCanceled = true
            }
        }
    }

    enum class Mode {
        GAMMA,
        POTION
    }
}
