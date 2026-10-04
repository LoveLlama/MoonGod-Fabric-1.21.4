package me.moon.event.events

import me.moon.event.EventStage
import net.minecraft.entity.Entity

class PushEvent : EventStage {
    @JvmField var entity: Entity? = null
    @JvmField var x: Double = 0.0
    @JvmField var y: Double = 0.0
    @JvmField var z: Double = 0.0
    @JvmField var airbone: Boolean = false

    constructor(stage: Int, entity: Entity, x: Double, y: Double, z: Double, airbone: Boolean) : super(stage) {
        this.entity = entity
        this.x = x
        this.y = y
        this.z = z
        this.airbone = airbone
    }

    constructor(stage: Int) : super(stage)

    constructor(stage: Int, entity: Entity) : super(stage) {
        this.entity = entity
    }
}
