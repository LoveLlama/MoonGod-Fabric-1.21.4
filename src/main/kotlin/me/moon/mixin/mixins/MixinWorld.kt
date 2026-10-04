package me.moon.mixin.mixins

import me.moon.features.modules.render.NoRender
import net.minecraft.util.math.BlockPos
import net.minecraft.world.chunk.light.ChunkSkyLightProvider
import net.minecraft.world.chunk.light.LightingProvider
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Redirect

@Mixin(value = [LightingProvider::class], priority = 2147483524)
class MixinWorld {
    @Redirect(method = ["checkBlock(Lnet/minecraft/util/math/BlockPos;)V"], at = [At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/light/ChunkSkyLightProvider;checkBlock(Lnet/minecraft/util/math/BlockPos;)V")])
    fun redirectSkyLightCheck(skyLightProvider: ChunkSkyLightProvider, pos: BlockPos) {
        if (NoRender.getInstance().isOn() && NoRender.getInstance().skyLightUpdate.getValue()) {
            return
        }
        skyLightProvider.checkBlock(pos)
    }
}
