package me.moon.manager

import me.moon.features.Feature
import me.moon.features.modules.client.HUD
import me.moon.util.Timer
import java.text.DecimalFormat

class ServerManager : Feature() {
    private val tpsCounts = FloatArray(10)
    private val format = DecimalFormat("##.00#")
    private val timer = Timer()
    private var TPS = 20.0f
    private var lastUpdate = -1L
    private var serverBrand = ""

    fun onPacketReceived() {
        timer.reset()
    }

    fun isServerNotResponding(): Boolean {
        return timer.passedMs(HUD.getInstance().lagTime.getValue().toLong())
    }

    fun serverRespondingTime(): Long {
        return timer.getPassedTimeMs()
    }

    fun update() {
        val currentTime = System.currentTimeMillis()
        if (lastUpdate == -1L) {
            lastUpdate = currentTime
        } else {
            val timeDiff = currentTime - lastUpdate
            var tickTime = timeDiff.toFloat() / 20.0f
            if (tickTime == 0.0f) {
                tickTime = 50.0f
            }
            var tps: Float
            tps = 1000.0f / tickTime
            if (tps > 20.0f) {
                tps = 20.0f
            }
            System.arraycopy(tpsCounts, 0, tpsCounts, 1, tpsCounts.size - 1)
            tpsCounts[0] = tps
            var total = 0.0
            for (f in tpsCounts) {
                total += f.toDouble()
            }
            total /= tpsCounts.size.toDouble()
            if (total > 20.0) {
                total = 20.0
            }
            TPS = format.format(total).toFloat()
            lastUpdate = currentTime
        }
    }

    public override fun reset() {
        tpsCounts.fill(20.0f)
        TPS = 20.0f
    }

    fun getTpsFactor(): Float {
        return 20.0f / TPS
    }

    fun getTPS(): Float = TPS

    fun getServerBrand(): String = serverBrand

    fun setServerBrand(brand: String) {
        serverBrand = brand
    }

    fun getPing(): Int {
        if (fullNullCheck()) {
            return 0
        } else {
            try {
                return mc.networkHandler!!.getPlayerListEntry(mc.player!!.gameProfile.id)!!.latency
            } catch (e: Exception) {
                return 0
            }
        }
    }
}
