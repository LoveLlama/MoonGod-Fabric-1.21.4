package me.moon.mixin.mixins

import me.moon.Moon
import me.moon.features.modules.combat.Killaura
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.attribute.EntityAttributes
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

@Mixin(PlayerEntity::class)
abstract class MixinEntityPlayer {
    @Inject(method = ["getAttackCooldownProgressPerTick()F"], at = [At("HEAD")], cancellable = true)
    private fun getCooldownPeriodHook(cir: CallbackInfoReturnable<Float>) {
        if (Killaura.getInstance().isOn() && Killaura.getInstance().tpsSync.getValue()) {
            val self = this as PlayerEntity
            val attr = self.getAttributeValue(EntityAttributes.ATTACK_SPEED)
            if (attr > 0.0) {
                val tpsFactor = Moon.serverManager?.getTpsFactor() ?: 1.0f
                cir.setReturnValue((1.0 / attr * 20.0 * tpsFactor).toFloat())
            }
        }
    }
}
