package me.moon.features.setting

import me.moon.event.EventBus
import me.moon.event.events.ClientEvent
import me.moon.features.Feature
import java.util.function.Predicate

class Setting<T> {
    private val name: String
    private val defaultValue: T
    private var value: T
    private var plannedValue: T
    private var min: T? = null
    private var max: T? = null
    private var hasRestriction = false
    private var visibility: Predicate<T>? = null
    private var description: String? = null
    private var feature: Feature? = null

    constructor(name: String, defaultValue: T) {
        this.name = name
        this.defaultValue = defaultValue
        this.value = defaultValue
        this.plannedValue = defaultValue
        this.description = ""
    }

    constructor(name: String, defaultValue: T, description: String) : this(name, defaultValue) {
        this.description = description
    }

    constructor(name: String, defaultValue: T, min: T, max: T, description: String) : this(name, defaultValue, min, max) {
        this.description = description
    }

    constructor(name: String, defaultValue: T, min: T, max: T) : this(name, defaultValue) {
        this.min = min
        this.max = max
        this.description = ""
        this.hasRestriction = true
    }

    constructor(name: String, defaultValue: T, min: T, max: T, visibility: Predicate<T>, description: String) : this(name, defaultValue, min, max, visibility) {
        this.description = description
    }

    constructor(name: String, defaultValue: T, min: T, max: T, visibility: Predicate<T>) : this(name, defaultValue, min, max) {
        this.visibility = visibility
    }

    constructor(name: String, defaultValue: T, visibility: Predicate<T>) : this(name, defaultValue) {
        this.visibility = visibility
    }

    fun getName(): String = name

    fun getValue(): T = value

    fun setValue(value: T) {
        setPlannedValue(value)
        if (hasRestriction) {
            if ((min as Number).toFloat() > (value as Number).toFloat()) {
                setPlannedValue(min!!)
            }
            if ((max as Number).toFloat() < (value as Number).toFloat()) {
                setPlannedValue(max!!)
            }
        }
        val event = ClientEvent(this)
        EventBus.post(event)
        if (!event.isCanceled) {
            this.value = plannedValue
        } else {
            this.plannedValue = this.value
        }
    }

    fun getPlannedValue(): T = plannedValue

    fun setPlannedValue(value: T) {
        plannedValue = value
    }

    fun getMin(): T? = min

    fun setMin(min: T) {
        this.min = min
    }

    fun getMax(): T? = max

    fun setMax(max: T) {
        this.max = max
    }

    fun setValueNoEvent(value: T) {
        setPlannedValue(value)
        if (hasRestriction) {
            if ((min as Number).toFloat() > (value as Number).toFloat()) {
                setPlannedValue(min!!)
            }
            if ((max as Number).toFloat() < (value as Number).toFloat()) {
                setPlannedValue(max!!)
            }
        }
        this.value = plannedValue
    }

    fun getFeature(): Feature? = feature

    fun setFeature(feature: Feature) {
        this.feature = feature
    }

    fun getEnum(input: String): Int {
        val constants = (value as Any).javaClass.enumConstants ?: return -1
        for (i in constants.indices) {
            val e = constants[i] as Enum<*>
            if (e.name.equals(input, ignoreCase = true)) {
                return i
            }
        }
        return -1
    }

    fun setEnumValue(value: String) {
        val constants = (this.value as Enum<*>).javaClass.enumConstants ?: return
        for (e in constants) {
            e as Enum<*>
            if (e.name.equals(value, ignoreCase = true)) {
                this.value = e as T
            }
        }
    }

    fun currentEnumName(): String = EnumConverter.getProperName(value as Enum<*>)

    fun currentEnum(): Int = EnumConverter.currentEnum(value as Enum<*>)

    fun increaseEnum() {
        plannedValue = EnumConverter.increaseEnum(value as Enum<*>) as T
        val event = ClientEvent(this)
        EventBus.post(event)
        if (!event.isCanceled) {
            value = plannedValue
        } else {
            plannedValue = value
        }
    }

    fun increaseEnumNoEvent() {
        value = EnumConverter.increaseEnum(value as Enum<*>) as T
    }

    fun getType(): String = if (isEnumSetting()) "Enum" else getClassName(defaultValue)

    fun getClassName(value: Any?): String = (value as Any).javaClass.simpleName

    fun getDescription(): String = description ?: ""

    fun isNumberSetting(): Boolean = value is Double || value is Int || value is Short || value is Long || value is Float

    fun isEnumSetting(): Boolean = !isNumberSetting() && value !is String && value !is Bind && value !is Char && value !is Boolean

    fun isStringSetting(): Boolean = value is String

    fun getDefaultValue(): T = defaultValue

    fun getValueAsString(): String = value.toString()

    fun hasRestriction(): Boolean = hasRestriction

    fun setVisibility(visibility: Predicate<T>) {
        this.visibility = visibility
    }

    fun isVisible(): Boolean = visibility?.test(value) ?: true
}
