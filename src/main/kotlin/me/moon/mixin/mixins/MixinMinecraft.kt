package me.moon.mixin.mixins

import me.moon.features.modules.misc.MultiTask
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.client.network.ClientPlayerInteractionManager
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Redirect

@Mixin(value = [MinecraftClient::class], priority = 234)
abstract class MixinMinecraft {
    @Redirect(method = ["handleBlockBreaking(Z)V"], at = [At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z")])
    private fun isHandActiveWrapper(player: ClientPlayerEntity): Boolean {
        return !MultiTask.getInstance().isOn() && player.isUsingItem
    }

    @Redirect(method = ["doItemUse()V"], at = [At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;isBreakingBlock()Z", ordinal = 0)])
    private fun isHittingBlockHook(manager: ClientPlayerInteractionManager): Boolean {
        return !MultiTask.getInstance().isOn() && manager.isBreakingBlock
    }
}
