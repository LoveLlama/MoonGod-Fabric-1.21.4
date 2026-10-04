package me.moon.event

open class EventStage {
    var stage: Int = 0
    var isCanceled: Boolean = false

    constructor()

    constructor(stage: Int) {
        this.stage = stage
    }
}
