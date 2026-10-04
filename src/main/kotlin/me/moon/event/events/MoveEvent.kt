package me.moon.event.events

import me.moon.event.EventStage
import net.minecraft.entity.MovementType

class MoveEvent(stage: Int, type: MovementType, x: Double, y: Double, z: Double) : EventStage(stage) {
    private var type: MovementType = type
    private var x: Double = x
    private var y: Double = y
    private var z: Double = z

    fun getType(): MovementType = type

    fun setType(type: MovementType) {
        this.type = type
    }

    fun getX(): Double = x

    fun setX(x: Double) {
        this.x = x
    }

    fun getY(): Double = y

    fun setY(y: Double) {
        this.y = y
    }

    fun getZ(): Double = z

    fun setZ(z: Double) {
        this.z = z
    }
}
