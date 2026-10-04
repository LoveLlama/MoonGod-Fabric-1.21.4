package me.moon.mixin.mixins

import me.moon.event.EventBus
import me.moon.event.events.PlayerDamageBlockEvent
import me.moon.event.events.ProcessRightClickBlockEvent
import me.moon.features.modules.misc.PacketEat
import me.moon.features.modules.client.HUD
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.client.network.ClientPlayerInteractionManager
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.Hand
import net.minecraft.util.ActionResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.component.DataComponentTypes
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Shadow
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

@Mixin(value = [ClientPlayerInteractionManager::class], priority = Int.MAX_VALUE)
abstract class MixinPlayerControllerMP {
    @Shadow
    protected abstract fun syncSelectedSlot()

    @Inject(method = ["updateBlockBreakingProgress(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;)Z"], at = [At("HEAD")], cancellable = true)
    private fun onPlayerDamageBlockHook(pos: BlockPos, face: Direction, cir: CallbackInfoReturnable<Boolean>) {
        val event = PlayerDamageBlockEvent(0, pos, face)
        EventBus.post(event)
    }

    @Inject(method = ["interactBlock(Lnet/minecraft/client/network/ClientPlayerEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ActionResult;"], at = [At("HEAD")], cancellable = true)
    fun processRightClickBlock(player: ClientPlayerEntity, hand: Hand, hitResult: BlockHitResult, cir: CallbackInfoReturnable<ActionResult>) {
        val event = ProcessRightClickBlockEvent(hitResult.blockPos, hand, player.getStackInHand(hand))
        EventBus.post(event)
        if (event.isCanceled) {
            cir.setReturnValue(ActionResult.FAIL)
        }
    }

    @Inject(method = ["stopUsingItem(Lnet/minecraft/entity/player/PlayerEntity;)V"], at = [At("HEAD")], cancellable = true)
    private fun onStoppedUsingItem(playerIn: PlayerEntity, ci: CallbackInfo) {
        val stack = playerIn.getStackInHand(playerIn.activeHand)
        if (PacketEat.getInstance().isOn() && stack.contains(DataComponentTypes.FOOD)) {
            this.syncSelectedSlot()
            playerIn.stopUsingItem()
            ci.cancel()
        }
    }

    @Inject(method = ["attackEntity(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/entity/Entity;)V"], at = [At("HEAD")])
    private fun onAttackEntity(player: PlayerEntity, target: Entity, ci: CallbackInfo) {
        HUD.getInstance().onAttack()
    }
}
