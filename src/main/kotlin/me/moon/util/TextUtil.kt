package me.moon.util

import net.minecraft.util.Formatting

object TextUtil {
    @JvmStatic
    fun coloredString(string: String, color: Color): String {
        val formatting = when (color) {
            Color.AQUA -> Formatting.AQUA
            Color.WHITE -> Formatting.WHITE
            Color.BLACK -> Formatting.BLACK
            Color.DARK_BLUE -> Formatting.DARK_BLUE
            Color.DARK_GREEN -> Formatting.DARK_GREEN
            Color.DARK_AQUA -> Formatting.DARK_AQUA
            Color.DARK_RED -> Formatting.DARK_RED
            Color.DARK_PURPLE -> Formatting.DARK_PURPLE
            Color.GOLD -> Formatting.GOLD
            Color.DARK_GRAY -> Formatting.DARK_GRAY
            Color.GRAY -> Formatting.GRAY
            Color.BLUE -> Formatting.BLUE
            Color.RED -> Formatting.RED
            Color.GREEN -> Formatting.GREEN
            Color.LIGHT_PURPLE -> Formatting.LIGHT_PURPLE
            Color.YELLOW -> Formatting.YELLOW
            Color.NONE -> return string
        }
        return formatting.toString() + string + Formatting.RESET
    }

    enum class Color {
        NONE,
        WHITE,
        BLACK,
        DARK_BLUE,
        DARK_GREEN,
        DARK_AQUA,
        DARK_RED,
        DARK_PURPLE,
        GOLD,
        GRAY,
        DARK_GRAY,
        BLUE,
        GREEN,
        AQUA,
        RED,
        LIGHT_PURPLE,
        YELLOW
    }
}
