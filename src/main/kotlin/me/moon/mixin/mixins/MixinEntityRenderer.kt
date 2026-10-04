package me.moon.mixin.mixins

import me.moon.event.EventBus
import me.moon.event.events.NoRenderEvent
import me.moon.features.modules.misc.NoHitBox
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.GameRenderer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.Entity
import net.minecraft.entity.projectile.ProjectileUtil
import net.minecraft.item.BlockItem
import net.minecraft.item.Items
import net.minecraft.item.PickaxeItem
import net.minecraft.item.SwordItem
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.Redirect
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import java.util.function.Predicate

@Mixin(value = [GameRenderer::class], priority = 1998)
class MixinEntityRenderer {
    @Redirect(method = ["findCrosshairTarget(Lnet/minecraft/entity/Entity;DDF)Lnet/minecraft/util/hit/HitResult;"], at = [At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/ProjectileUtil;raycast(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Box;Ljava/util/function/Predicate;D)Lnet/minecraft/util/hit/EntityHitResult;")])
    fun raycast(entity: Entity, min: Vec3d, max: Vec3d, box: Box, predicate: Predicate<Entity>, maxDistance: Double): EntityHitResult? {
        val mc = MinecraftClient.getInstance()
        val held = mc.player?.mainHandStack?.item
        val shouldFilter = held == null || !NoHitBox.getINSTANCE().isOn() ||
            !((held is PickaxeItem && NoHitBox.getINSTANCE().pickaxe.getValue())
                || (held == Items.END_CRYSTAL && NoHitBox.getINSTANCE().crystal.getValue())
                || (held == Items.GOLDEN_APPLE && NoHitBox.getINSTANCE().gapple.getValue())
                || (held is BlockItem && NoHitBox.getINSTANCE().block.getValue())
                || (held is SwordItem && NoHitBox.getINSTANCE().sword.getValue()))
        return if (shouldFilter) ProjectileUtil.raycast(entity, min, max, box, predicate, maxDistance) else null
    }

    @Inject(method = ["tiltViewWhenHurt(Lnet/minecraft/client/util/math/MatrixStack;F)V"], at = [At("HEAD")], cancellable = true)
    fun hurtCameraEffect(matrixStack: MatrixStack, ticks: Float, ci: CallbackInfo) {
        val event = NoRenderEvent(1)
        EventBus.post(event)
        if (event.isCanceled) {
            ci.cancel()
        }
    }
}
