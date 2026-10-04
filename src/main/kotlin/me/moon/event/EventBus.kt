package me.moon.event

import me.moon.Moon
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap

object EventBus {
    private class Handler(
        val target: Any,
        val method: Method,
        val eventType: Class<*>,
        val priority: Int,
        val receiveCanceled: Boolean
    )

    private val listeners = ConcurrentHashMap<Any, List<Handler>>()

    fun register(listener: Any) {
        val handlers = ArrayList<Handler>()
        var clazz: Class<*>? = listener.javaClass
        while (clazz != null && clazz != Any::class.java) {
            for (method in clazz.declaredMethods) {
                val ann = method.getAnnotation(SubscribeEvent::class.java) ?: continue
                if (method.parameterTypes.size != 1) continue
                method.isAccessible = true
                handlers.add(Handler(listener, method, method.parameterTypes[0], ann.priority, ann.receiveCanceled))
            }
            clazz = clazz.superclass
        }
        listeners[listener] = handlers
    }

    fun unregister(listener: Any) {
        listeners.remove(listener)
    }

    fun post(event: Any): Boolean {
        val all = ArrayList<Handler>()
        synchronized(listeners) {
            for (handlers in listeners.values) {
                all.addAll(handlers)
            }
        }
        val sorted = all.filter { it.eventType.isAssignableFrom(event.javaClass) }
            .sortedBy { it.priority }
        for (handler in sorted) {
            val stage = event as? EventStage
            if (stage != null && stage.isCanceled && !handler.receiveCanceled) continue
            try {
                handler.method.invoke(handler.target, event)
            } catch (e: java.lang.reflect.InvocationTargetException) {
                Moon.LOGGER.error("Event listener ${handler.method.name} failed for ${event.javaClass.simpleName}", e.targetException)
            } catch (e: Throwable) {
                Moon.LOGGER.error("Event listener dispatch failed for ${event.javaClass.simpleName}", e)
            }
        }
        val stage = event as? EventStage
        if (stage != null) {
            return !stage.isCanceled
        }
        return true
    }
}
