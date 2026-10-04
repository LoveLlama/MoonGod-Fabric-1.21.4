package me.moon.features.modules.misc

import me.moon.Moon
import me.moon.features.command.Command
import me.moon.features.modules.Module
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.Formatting
import net.minecraft.util.hit.EntityHitResult
import org.lwjgl.glfw.GLFW

class MCF : Module("MCF", "Middleclick Friends.", Module.Category.MISC, true, false, false) {
    private var clicked = false

    override fun onUpdate() {
        if (GLFW.glfwGetMouseButton(mc.window.handle, 2) == GLFW.GLFW_PRESS) {
            if (!this.clicked && mc.currentScreen == null) {
                this.onClick()
            }

            this.clicked = true
        } else {
            this.clicked = false
        }
    }

    private fun onClick() {
        val result = mc.crosshairTarget
        if (result != null && result.type == net.minecraft.util.hit.HitResult.Type.ENTITY) {
            val entity = (result as EntityHitResult).entity
            if (entity is PlayerEntity) {
                val name = entity.gameProfile.name
                if (Moon.friendManager!!.isFriend(name)) {
                    Moon.friendManager!!.removeFriend(name)
                    Command.sendMessage(Formatting.RED.toString() + name + Formatting.RED + " has been unfriended.")
                } else {
                    Moon.friendManager!!.addFriend(name)
                    Command.sendMessage(Formatting.AQUA.toString() + name + Formatting.AQUA + " has been friended.")
                }
            }
        }

        this.clicked = true
    }
}
