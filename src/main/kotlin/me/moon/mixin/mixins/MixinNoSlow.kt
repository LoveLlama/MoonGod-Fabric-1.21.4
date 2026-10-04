package me.moon.mixin.mixins

import me.moon.features.modules.movement.PlayerTweaks
import net.minecraft.client.network.ClientPlayerEntity
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Redirect

@Mixin(ClientPlayerEntity::class)
abstract class MixinNoSlow {
    @Redirect(method = ["tickMovement()V"], at = [At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z", ordinal = 0)])
    fun noSlow(instance: ClientPlayerEntity): Boolean {
        if (PlayerTweaks.getInstance().isOn() && PlayerTweaks.getInstance().noSlow.getValue() && instance.isUsingItem && !instance.hasVehicle()) {
            return false
        }
        return instance.isUsingItem
    }
}
