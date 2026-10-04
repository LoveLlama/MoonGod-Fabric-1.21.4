package me.moon.mixin.mixins

import me.moon.event.EventBus
import me.moon.event.events.NoRenderEvent
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer
import net.minecraft.client.render.entity.model.BipedEntityModel
import net.minecraft.entity.EquipmentSlot
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

@Mixin(value = [ArmorFeatureRenderer::class], priority = 1898)
class MixinLayerBipedArmor {
    @Inject(method = ["setVisible(Lnet/minecraft/client/render/entity/model/BipedEntityModel;Lnet/minecraft/entity/EquipmentSlot;)V"], at = [At("HEAD")], cancellable = true)
    protected fun setModelSlotVisible(model: BipedEntityModel<*>, slot: EquipmentSlot, ci: CallbackInfo) {
        val event = NoRenderEvent(0)
        EventBus.post(event)
        if (event.isCanceled) {
            ci.cancel()
            when (slot) {
                EquipmentSlot.HEAD -> {
                    model.head.visible = false
                    model.hat.visible = false
                }
                EquipmentSlot.CHEST -> {
                    model.body.visible = false
                    model.rightArm.visible = false
                    model.leftArm.visible = false
                }
                EquipmentSlot.LEGS -> {
                    model.body.visible = false
                    model.rightLeg.visible = false
                    model.leftLeg.visible = false
                }
                EquipmentSlot.FEET -> {
                    model.rightLeg.visible = false
                    model.leftLeg.visible = false
                }
                else -> {}
            }
        }
    }
}
