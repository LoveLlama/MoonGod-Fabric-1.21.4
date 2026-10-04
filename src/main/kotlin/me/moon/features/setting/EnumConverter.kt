package me.moon.features.setting

import com.google.common.base.Converter
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive

class EnumConverter(private val clazz: Class<out Enum<*>>) : Converter<Enum<*>, JsonElement>() {

    public override fun doForward(anEnum: Enum<*>): JsonElement {
        return JsonPrimitive(anEnum.toString())
    }

    public override fun doBackward(jsonElement: JsonElement): Enum<*>? {
        return try {
            java.lang.Enum.valueOf(clazz, jsonElement.asString)
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    companion object {
        @JvmStatic
        fun currentEnum(clazz: Enum<*>): Int {
            val constants = clazz.javaClass.enumConstants ?: return -1
            for (i in constants.indices) {
                val e = constants[i] as Enum<*>
                if (e.name.equals(clazz.name, ignoreCase = true)) {
                    return i
                }
            }
            return -1
        }

        @JvmStatic
        fun increaseEnum(clazz: Enum<*>): Enum<*> {
            val index = currentEnum(clazz)
            val constants = clazz.javaClass.enumConstants!!
            for (i in constants.indices) {
                val e = constants[i] as Enum<*>
                if (i == index + 1) {
                    return e
                }
            }
            return constants[0] as Enum<*>
        }

        @JvmStatic
        fun getProperName(clazz: Enum<*>): String {
            return Character.toUpperCase(clazz.name[0]) + clazz.name.lowercase().substring(1)
        }
    }
}
