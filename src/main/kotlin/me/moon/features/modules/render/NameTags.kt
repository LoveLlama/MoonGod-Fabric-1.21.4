package me.moon.features.modules.render

import me.moon.Moon
import me.moon.event.events.Render3DEvent
import me.moon.features.modules.Module
import me.moon.features.setting.Setting
import me.moon.util.EntityUtil
import me.moon.util.RenderUtil
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.Formatting
import net.minecraft.util.math.BlockPos

class NameTags : Module("NameTags", "letter Nametags", Module.Category.RENDER, false, false, false) {
    private val invisibles = register(Setting("Invisibles", true))

    override fun onRender3D(event: Render3DEvent) {
        if (!fullNullCheck()) {
            for (player in mc.world!!.players) {
                if (player.squaredDistanceTo(mc.player!!) <= 900.0
                    && player != mc.player!!
                    && player.isAlive
                    && (!player.isInvisible || this.invisibles.getValue())
                ) {
                    val pos = EntityUtil.getInterpolatedRenderPos(player, event.partialTicks)
                    this.renderNameTag(player, pos.x, pos.y, pos.z)
                }
            }
        }
    }

    private fun renderNameTag(player: PlayerEntity, x: Double, y: Double, z: Double) {
        val displayTag = this.getDisplayTag(player)
        RenderUtil.drawText(BlockPos.ofFloored(x, y + (if (player.isSneaking) 0.5 else 0.7) + 1.4, z), displayTag, this.getDisplayColour(player))
    }

    private fun getDisplayTag(player: PlayerEntity): String {
        var name = player.name.string
        if (name.contains(mc.gameProfile.name)) {
            name = "You"
        }
        val health = player.health + player.absorptionAmount
        val color: Formatting
        if (health > 18.0f) color = Formatting.GREEN
        else if (health > 16.0f) color = Formatting.DARK_GREEN
        else if (health > 12.0f) color = Formatting.YELLOW
        else if (health > 8.0f) color = Formatting.GOLD
        else if (health > 5.0f) color = Formatting.RED
        else color = Formatting.DARK_RED

        var pingStr = ""
        try {
            val info = mc.player!!.networkHandler.getPlayerListEntry(player.uuid)
            if (info != null) {
                pingStr = pingStr + info.latency + "ms "
            }
        } catch (ignored: Exception) {
        }

        name = if (Math.floor(health.toDouble()) == health.toDouble()) {
            name + color.toString() + ' ' + (if (health > 0.0f) Math.floor(health.toDouble()).toInt() else "dead")
        } else {
            name + color.toString() + ' ' + (if (health > 0.0f) health.toInt() else "dead")
        }
        return pingStr + name
    }

    private fun getDisplayColour(player: PlayerEntity): Int {
        if (Moon.friendManager!!.isFriend(player)) {
            return -11157267
        }
        var colour = -1
        if (player.isInvisible) {
            colour = -1113785
        } else if (player.isSneaking) {
            colour = -6481515
        }
        return colour
    }
}
