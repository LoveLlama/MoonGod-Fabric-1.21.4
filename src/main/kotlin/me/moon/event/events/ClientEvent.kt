package me.moon.event.events

import me.moon.event.EventStage
import me.moon.features.Feature
import me.moon.features.setting.Setting

class ClientEvent : EventStage {
    private var feature: Feature? = null
    private var setting: Setting<*>? = null

    constructor(stage: Int, feature: Feature) : super(stage) {
        this.feature = feature
    }

    constructor(setting: Setting<*>) : super(2) {
        this.setting = setting
    }

    fun getFeature(): Feature? = feature

    fun getSetting(): Setting<*>? = setting
}
