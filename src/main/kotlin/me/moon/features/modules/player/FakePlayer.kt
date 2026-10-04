package me.moon.features.modules.player

import com.mojang.authlib.GameProfile
import me.moon.features.modules.Module
import me.moon.features.setting.Setting
import net.minecraft.client.network.OtherClientPlayerEntity
import net.minecraft.entity.Entity
import net.minecraft.world.GameMode
import java.util.UUID

class FakePlayer : Module("FakePlayer", "Spawns a FakePlayer for testing", Module.Category.PLAYER, true, false, false) {
    private val setHealth = register(Setting("SetHealth", 20, 1, 20))
    private var clonedPlayer: OtherClientPlayerEntity? = null

    override fun onEnable() {
        val player = mc.player
        val world = mc.world
        if (player != null && world != null && !player.isDead) {
            val fake = OtherClientPlayerEntity(world, GameProfile(UUID.fromString("a3ca166d-c5f1-3d5a-baac-b18a5b38d4cd"), "404"))
            fake.refreshPositionAndAngles(player.x, player.y, player.z, player.yaw, player.pitch)
            fake.setId(-404)
            fake.setHealth(this.setHealth.getValue().toFloat())
            this.clonedPlayer = fake
            world.addEntity(fake)
            fake.tick()
        } else {
            this.disable()
        }
    }

    override fun onDisable() {
        val world = mc.world
        if (world != null) {
            world.removeEntity(-404, Entity.RemovalReason.DISCARDED)
        }
    }
}
