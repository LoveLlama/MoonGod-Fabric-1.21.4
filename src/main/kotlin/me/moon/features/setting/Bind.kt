package me.moon.features.setting

import com.google.common.base.Converter
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.InputUtil

class Bind(key: Int) {
    private var key: Int = key

    fun getKey(): Int = key

    fun setKey(key: Int) {
        this.key = key
    }

    fun isEmpty(): Boolean = key < 0

    override fun toString(): String {
        return if (isEmpty()) "None" else if (key < 0) "None" else capitalise(InputUtil.Type.KEYSYM.createFromCode(key).localizedText.string)
    }

    fun isDown(): Boolean {
        return !isEmpty() && InputUtil.isKeyPressed(MinecraftClient.getInstance().window.handle, getKey())
    }

    private fun capitalise(str: String): String {
        return if (str.isEmpty()) "" else Character.toUpperCase(str[0]) + (if (str.length != 1) str.substring(1).lowercase() else "")
    }

    class BindConverter : Converter<Bind, JsonElement>() {
        public override fun doForward(bind: Bind): JsonElement {
            return JsonPrimitive(bind.toString())
        }

        public override fun doBackward(jsonElement: JsonElement): Bind {
            val s = jsonElement.asString
            if (s.equals("None", ignoreCase = true)) {
                return Bind.none()
            }
            var key = -1
            try {
                key = InputUtil.fromTranslationKey("key.keyboard." + s.lowercase())?.code ?: 0
            } catch (e: Exception) {
            }
            return if (key == 0) Bind.none() else Bind(key)
        }
    }

    companion object {
        @JvmStatic
        fun none(): Bind = Bind(-1)
    }
}
