package me.moon.mixin.mixins

import me.moon.event.EventBus
import me.moon.event.events.PushEvent
import me.moon.features.modules.misc.NoHitBox
import net.minecraft.entity.Entity
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Redirect

@Mixin(Entity::class)
class MixinEntity {
    @Redirect(method = ["pushAwayFrom(Lnet/minecraft/entity/Entity;)V"], at = [At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;addVelocity(DDD)V")])
    fun pushOutOfBlocksNoHitBox(instance: Entity, x: Double, y: Double, z: Double) {
        if (!NoHitBox.getINSTANCE().isOn()) {
            instance.addVelocity(x, y, z)
        }
    }

    @Redirect(method = ["updateMovementInFluid(Lnet/minecraft/registry/tag/TagKey;D)Z"], at = [At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;isPushedByFluids()Z")])
    fun isPushedByWaterHook(entity: Entity): Boolean {
        val event = PushEvent(2, entity)
        EventBus.post(event)
        return entity.isPushedByFluids() && !event.isCanceled
    }
}
