package me.moon.util

class Timer {
    private var time: Long = -1L

    fun passedS(s: Double): Boolean {
        return getMs(System.nanoTime() - time) >= (s * 1000.0).toLong()
    }

    fun passedM(m: Double): Boolean {
        return getMs(System.nanoTime() - time) >= (m * 1000.0 * 60.0).toLong()
    }

    fun passedDms(dms: Double): Boolean {
        return getMs(System.nanoTime() - time) >= (dms * 10.0).toLong()
    }

    fun passedDs(ds: Double): Boolean {
        return getMs(System.nanoTime() - time) >= (ds * 100.0).toLong()
    }

    fun passedMs(ms: Long): Boolean {
        return getMs(System.nanoTime() - time) >= ms
    }

    fun passedNS(ns: Long): Boolean {
        return System.nanoTime() - time >= ns
    }

    fun setMs(ms: Long) {
        time = System.nanoTime() - ms * 1000000L
    }

    fun getPassedTimeMs(): Long {
        return getMs(System.nanoTime() - time)
    }

    fun reset(): Timer {
        time = System.nanoTime()
        return this
    }

    fun getMs(time: Long): Long {
        return time / 1000000L
    }

    fun convertToNS(time: Long): Long {
        return time * 1000000L
    }
}
