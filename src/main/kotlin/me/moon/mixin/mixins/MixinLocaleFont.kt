package me.moon.mixin.mixins

import net.minecraft.client.font.FontFilterType
import net.minecraft.client.font.FontStorage
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.ModifyVariable

@Mixin(value = [FontStorage::class], priority = 100)
class MixinLocaleFont {
    @ModifyVariable(method = ["setActiveFilters(Ljava/util/Set;)V"], at = [At("HEAD")], argsOnly = true)
    fun stripCjkFilters(activeFilters: Set<FontFilterType>): Set<FontFilterType> {
        return activeFilters.filter { it != FontFilterType.JAPANESE_VARIANTS }.toSet()
    }
}
