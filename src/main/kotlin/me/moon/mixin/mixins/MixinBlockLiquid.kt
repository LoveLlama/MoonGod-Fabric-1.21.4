package me.moon.mixin.mixins

import me.moon.features.modules.misc.Interact
import net.minecraft.block.FluidBlock
import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.world.BlockView
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

@Mixin(FluidBlock::class, priority = 1782)
class MixinBlockLiquid {
    @Inject(method = ["getRaycastShape(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/util/shape/VoxelShape;"], at = [At("HEAD")], cancellable = true)
    fun getRaycastShape(state: BlockState, world: BlockView, pos: BlockPos, cir: CallbackInfoReturnable<VoxelShape>) {
        if (Interact.getInstance().isOn() && Interact.getInstance().liquid.getValue()) {
            cir.setReturnValue(VoxelShapes.fullCube())
        }
    }
}
