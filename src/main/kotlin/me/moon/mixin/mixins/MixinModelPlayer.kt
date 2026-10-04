package me.moon.mixin.mixins

import me.moon.Moon
import me.moon.features.modules.render.Skeleton
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.AbstractClientPlayerEntity
import net.minecraft.client.render.entity.PlayerEntityRenderer
import net.minecraft.client.render.entity.state.PlayerEntityRenderState
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

@Mixin(value = [PlayerEntityRenderer::class], priority = 134)
abstract class MixinModelPlayer {
    @Inject(method = ["updateRenderState(Lnet/minecraft/client/network/AbstractClientPlayerEntity;Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;F)V"], at = [At("RETURN")])
    fun updateRenderState(player: AbstractClientPlayerEntity, state: PlayerEntityRenderState, tickDelta: Float, ci: CallbackInfo) {
        val mc = MinecraftClient.getInstance()
        if (mc.world != null && mc.player != null) {
            val skeleton = Moon.moduleManager?.getModuleByClass(Skeleton::class.java) as? Skeleton
            if (skeleton != null) {
                skeleton.addEntity(player, arrayOf())
            }
        }
    }
}
