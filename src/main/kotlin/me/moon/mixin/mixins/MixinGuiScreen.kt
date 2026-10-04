package me.moon.mixin.mixins

import me.moon.features.modules.misc.ToolTips
import net.minecraft.block.ShulkerBoxBlock
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.item.BlockItem
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

@Mixin(value = [Screen::class], priority = 1998)
class MixinGuiScreen {
    @Inject(method = ["renderBackground(Lnet/minecraft/client/gui/DrawContext;IIF)V"], at = [At("HEAD")], cancellable = true)
    fun drawWorldBackground(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float, ci: CallbackInfo) {
        if (MinecraftClient.getInstance().player != null) {
            ci.cancel()
        }
    }

    @Inject(method = ["renderWithTooltip(Lnet/minecraft/client/gui/DrawContext;IIF)V"], at = [At("HEAD")], cancellable = true)
    fun renderToolTipHook(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float, ci: CallbackInfo) {
        val mc = MinecraftClient.getInstance()
        if (ToolTips.getInstance().isOn() && mc.currentScreen is HandledScreen<*>) {
            val slot = (this as Any as HandledScreenAccessor).moonGetFocusedSlot()
            if (slot != null) {
                val stack = slot.stack
                val item = stack.item
                if (!stack.isEmpty && item is BlockItem && item.block is ShulkerBoxBlock) {
                    ToolTips.getInstance().renderShulkerToolTip(stack, mouseX, mouseY, null)
                    ci.cancel()
                }
            }
        }
    }
}
