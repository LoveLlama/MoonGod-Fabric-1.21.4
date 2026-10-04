package me.moon.event

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class SubscribeEvent(val priority: Int = 0, val receiveCanceled: Boolean = false)
