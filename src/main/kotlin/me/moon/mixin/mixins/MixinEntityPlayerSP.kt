package me.moon.mixin.mixins

import me.moon.event.EventBus
import me.moon.event.events.MoveEvent
import me.moon.event.events.PushEvent
import me.moon.event.events.UpdateWalkingPlayerEvent
import me.moon.features.modules.movement.Sprint
import me.moon.util.Util
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.entity.MovementType
import net.minecraft.util.math.Vec3d
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.ModifyVariable
import org.spongepowered.asm.mixin.injection.Redirect
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

@Mixin(value = [ClientPlayerEntity::class], priority = Int.MAX_VALUE)
abstract class MixinEntityPlayerSP : Util {

    @Inject(method = ["sendMovementPackets()V"], at = [At("HEAD")], cancellable = true)
    private fun preMotion(ci: CallbackInfo) {
        val event = UpdateWalkingPlayerEvent(0)
        EventBus.post(event)
        if (event.isCanceled) {
            ci.cancel()
        }
    }

    @Inject(method = ["sendMovementPackets()V"], at = [At("RETURN")], cancellable = true)
    private fun postMotion(ci: CallbackInfo) {
        val event = UpdateWalkingPlayerEvent(1)
        EventBus.post(event)
        if (event.isCanceled) {
            ci.cancel()
        }
    }

    @Inject(method = ["pushOutOfBlocks(DD)V"], at = [At("HEAD")], cancellable = true)
    private fun pushOutOfBlocksHook(x: Double, z: Double, ci: CallbackInfo) {
        val event = PushEvent(1)
        EventBus.post(event)
        if (event.isCanceled) {
            ci.cancel()
        }
    }

    @Redirect(method = ["tickMovement()V"], at = [At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;setSprinting(Z)V", ordinal = 2)])
    fun onLivingUpdate(instance: ClientPlayerEntity, sprinting: Boolean) {
        val forward = instance.input.movementForward
        val sideways = instance.input.movementSideways
        if (Sprint.getInstance().isOn() && Sprint.getInstance().mode.getValue() == Sprint.Mode.RAGE) {
            if (forward == 0.0f && sideways == 0.0f) {
                instance.setSprinting(sprinting)
            } else {
                instance.setSprinting(true)
            }
        } else {
            if (sideways == 0.0f) {
                instance.setSprinting(sprinting)
            } else {
                instance.setSprinting(true)
            }
        }
    }

    @ModifyVariable(method = ["move(Lnet/minecraft/entity/MovementType;Lnet/minecraft/util/math/Vec3d;)V"], at = [At("HEAD")], argsOnly = true, ordinal = 0)
    fun onMove(movement: Vec3d, type: MovementType): Vec3d {
        val event = MoveEvent(0, type, movement.x, movement.y, movement.z)
        EventBus.post(event)
        return if (event.isCanceled) Vec3d(event.getX(), event.getY(), event.getZ()) else movement
    }
}
