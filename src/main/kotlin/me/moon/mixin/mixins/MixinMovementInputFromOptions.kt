package me.moon.mixin.mixins

import me.moon.features.modules.movement.PlayerTweaks
import net.minecraft.client.MinecraftClient
import net.minecraft.client.input.KeyboardInput
import net.minecraft.client.gui.screen.ChatScreen
import net.minecraft.client.option.KeyBinding
import org.lwjgl.glfw.GLFW
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Redirect

@Mixin(value = [KeyboardInput::class], priority = 11010)
abstract class MixinMovementInputFromOptions {
    @Redirect(method = ["tick()V"], at = [At(value = "INVOKE", target = "Lnet/minecraft/client/option/KeyBinding;isPressed()Z")])
    fun isKeyPressed(keyBinding: KeyBinding): Boolean {
        val keyCode = (keyBinding as KeyBindingAccessor).moonGetBoundKey().code
        val mc = MinecraftClient.getInstance()
        val windowHandle = mc.window?.handle
        val guiMove = PlayerTweaks.getInstance().getSettingByName("Gui Move")?.getValue() == true
        return if (windowHandle != null && keyCode > 0 && keyCode < 256 && PlayerTweaks.getInstance().isOn() && guiMove && mc.currentScreen != null && mc.currentScreen !is ChatScreen) {
            GLFW.glfwGetKey(windowHandle, keyCode) == GLFW.GLFW_PRESS
        } else {
            keyBinding.isPressed
        }
    }
}
