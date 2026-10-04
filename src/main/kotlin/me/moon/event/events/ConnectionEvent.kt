package me.moon.event.events

import me.moon.event.EventStage
import net.minecraft.entity.player.PlayerEntity
import java.util.UUID

class ConnectionEvent : EventStage {
    private val uuid: UUID
    private val entity: PlayerEntity?
    private val name: String?

    constructor(stage: Int, uuid: UUID, name: String?) : super(stage) {
        this.uuid = uuid
        this.name = name
        this.entity = null
    }

    constructor(stage: Int, entity: PlayerEntity, uuid: UUID, name: String?) : super(stage) {
        this.entity = entity
        this.uuid = uuid
        this.name = name
    }

    fun getName(): String? = name

    fun getUuid(): UUID = uuid

    fun getEntity(): PlayerEntity? = entity
}
