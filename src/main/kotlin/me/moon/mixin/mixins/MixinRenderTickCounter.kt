package me.moon.mixin.mixins

import it.unimi.dsi.fastutil.floats.FloatUnaryOperator
import me.moon.mixin.interfaces.IRenderTickCounter
import net.minecraft.client.render.RenderTickCounter
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Unique
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Redirect

@Mixin(RenderTickCounter.Dynamic::class)
abstract class MixinRenderTickCounter : IRenderTickCounter {
    @JvmField
    @Unique
    var moonTickSpeed: Float = 1.0f

    override fun getTickSpeed(): Float {
        return moonTickSpeed
    }

    override fun setTickSpeed(speed: Float) {
        moonTickSpeed = speed
    }

    @Redirect(method = ["beginRenderTick(J)I"], at = [At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/floats/FloatUnaryOperator;apply(F)F")])
    fun moonRedirectTickLength(op: FloatUnaryOperator, tickTime: Float): Float {
        val speed = if (moonTickSpeed.isNaN() || moonTickSpeed <= 0f) 1.0f else moonTickSpeed
        return op.apply(tickTime) / speed
    }
}
