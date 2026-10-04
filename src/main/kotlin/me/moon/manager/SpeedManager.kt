package me.moon.manager

import me.moon.features.Feature
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.MathHelper

class SpeedManager : Feature() {
    private val distancer = 20
    var firstJumpSpeed = 0.0
    var lastJumpSpeed = 0.0
    var percentJumpSpeedChanged = 0.0
    var jumpSpeedChanged = 0.0
    var didJumpLastTick = false
    var jumpInfoStartTime = 0L
    var wasFirstJump = true
    var speedometerCurrentSpeed = 0.0
    var playerSpeeds: HashMap<PlayerEntity, Double> = HashMap()

    fun lastJumpInfoTimeRemaining(): Float {
        return ((System.currentTimeMillis() - jumpInfoStartTime) / 1000.0).toFloat()
    }

    fun updateValues() {
        val distTraveledLastTickX = mc.player!!.x - mc.player!!.prevX
        val distTraveledLastTickZ = mc.player!!.z - mc.player!!.prevZ
        speedometerCurrentSpeed = distTraveledLastTickX * distTraveledLastTickX + distTraveledLastTickZ * distTraveledLastTickZ
        if (didJumpThisTick && (!mc.player!!.isOnGround || isJumping)) {
            if (didJumpThisTick && !didJumpLastTick) {
                wasFirstJump = lastJumpSpeed == 0.0
                percentJumpSpeedChanged = if (speedometerCurrentSpeed != 0.0) speedometerCurrentSpeed / lastJumpSpeed - 1.0 else -1.0
                jumpSpeedChanged = speedometerCurrentSpeed - lastJumpSpeed
                jumpInfoStartTime = System.currentTimeMillis()
                lastJumpSpeed = speedometerCurrentSpeed
                firstJumpSpeed = if (wasFirstJump) lastJumpSpeed else 0.0
            }
            didJumpLastTick = didJumpThisTick
        } else {
            didJumpLastTick = false
            lastJumpSpeed = 0.0
        }
        updatePlayers()
    }

    fun updatePlayers() {
        for (player in mc.world!!.players) {
            if (mc.player!!.squaredDistanceTo(player) < (20 * 20).toDouble()) {
                val distTraveledLastTickX = player.x - player.prevX
                val distTraveledLastTickZ = player.z - player.prevZ
                val playerSpeed = distTraveledLastTickX * distTraveledLastTickX + distTraveledLastTickZ * distTraveledLastTickZ
                playerSpeeds.put(player, playerSpeed)
            }
        }
    }

    fun getPlayerSpeed(player: PlayerEntity): Double {
        return if (playerSpeeds[player] == null) 0.0 else turnIntoKpH(playerSpeeds[player]!!)
    }

    fun turnIntoKpH(input: Double): Double {
        return MathHelper.sqrt(input.toFloat()).toDouble() * 71.2729367892
    }

    fun getSpeedKpH(): Double {
        val speedometerkphdouble = turnIntoKpH(speedometerCurrentSpeed)
        return Math.round(10.0 * speedometerkphdouble) / 10.0
    }

    fun getSpeedMpS(): Double {
        val speedometerMpsdouble = turnIntoKpH(speedometerCurrentSpeed) / 3.6
        return Math.round(10.0 * speedometerMpsdouble) / 10.0
    }

    companion object {
        const val LAST_JUMP_INFO_DURATION_DEFAULT = 3.0
        @JvmField
        var didJumpThisTick = false
        @JvmField
        var isJumping = false

        @JvmStatic
        fun setDidJumpThisTick(value: Boolean) {
            didJumpThisTick = value
        }

        @JvmStatic
        fun setIsJumping(value: Boolean) {
            isJumping = value
        }
    }
}
