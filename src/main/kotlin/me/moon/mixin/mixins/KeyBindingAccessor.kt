package me.moon.mixin.mixins

import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.gen.Accessor

@Mixin(KeyBinding::class)
interface KeyBindingAccessor {
    @Accessor("boundKey")
    fun moonGetBoundKey(): InputUtil.Key
}
