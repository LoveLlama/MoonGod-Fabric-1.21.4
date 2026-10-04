package me.moon.manager

import me.moon.features.Feature
import net.minecraft.client.resource.language.I18n
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.Formatting

class PotionManager : Feature() {
    private val potions: MutableMap<PlayerEntity, PotionList> = java.util.concurrent.ConcurrentHashMap()

    fun getOwnPotions(): List<StatusEffectInstance> {
        return getPlayerPotions(mc.player!!)
    }

    fun getPlayerPotions(player: PlayerEntity): List<StatusEffectInstance> {
        val list = potions[player]
        var potions: MutableList<StatusEffectInstance> = ArrayList()
        if (list != null) {
            potions = list.getEffects()
        }
        return potions
    }

    fun getImportantPotions(player: PlayerEntity): Array<StatusEffectInstance?> {
        val array = arrayOfNulls<StatusEffectInstance>(3)
        for (effect in getPlayerPotions(player)) {
            val potion = effect.effectType.value()
            val name = I18n.translate(potion.translationKey).lowercase()
            if (name == "strength") {
                array[0] = effect
            }
            if (name == "strength" || name == "weakness") {
                array[1] = effect
            }
            if (name == "strength" || name == "weakness" || name == "speed") {
                array[2] = effect
            }
        }
        return array
    }

    fun getPotionString(effect: StatusEffectInstance): String {
        val potion = effect.effectType.value()
        return I18n.translate(potion.translationKey) + " " + (effect.amplifier + 1) + " " + Formatting.WHITE.toString() + getPotionDurationString(effect)
    }

    private fun getPotionDurationString(effect: StatusEffectInstance): String {
        if (effect.isInfinite()) {
            return "**:**"
        }
        val i = effect.duration
        return String.format("%d:%02d", i / 1200, (i % 1200) / 20)
    }

    fun getColoredPotionString(effect: StatusEffectInstance): String {
        return getPotionString(effect)
    }

    class PotionList {
        private val effects: MutableList<StatusEffectInstance> = ArrayList()

        fun addEffect(effect: StatusEffectInstance?) {
            if (effect != null) {
                effects.add(effect)
            }
        }

        fun getEffects(): MutableList<StatusEffectInstance> = effects
    }
}
