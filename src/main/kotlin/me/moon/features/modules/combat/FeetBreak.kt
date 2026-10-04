package me.moon.features.modules.combat

import me.moon.features.modules.Module
import me.moon.features.modules.player.MoonBreak
import me.moon.features.setting.Bind
import me.moon.features.setting.Setting
import me.moon.util.BlockUtil
import me.moon.util.EntityUtil
import me.moon.util.InventoryUtil
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Items
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d

class FeetBreak : Module("FeetBreak", "FeetBreak", Module.Category.COMBAT, true, false, false) {
    @JvmField
    var target: PlayerEntity? = null
    private val enableBind = register(Setting("Enable", Bind(-1)))

    override fun onTick() {
        if (!fullNullCheck()) {
            if (InventoryUtil.getItemHotbar(Items.DIAMOND_PICKAXE) != -1) {
                this.target = this.getTarget(5.0)
                this.surroundMine()
            }
        }
    }

    override fun getDisplayInfo(): String? {
        return this.target?.name?.string
    }

    private fun surroundMine() {
        val target = this.target
        if (target != null && this.enableBind.getValue().isDown()) {
            val a = target.pos
            if (EntityUtil.getSurroundWeakness(a, 1, -1)) {
                this.surroundMine(a, -1.0, 0.0, 0.0)
            } else if (EntityUtil.getSurroundWeakness(a, 2, -1)) {
                this.surroundMine(a, 1.0, 0.0, 0.0)
            } else if (EntityUtil.getSurroundWeakness(a, 3, -1)) {
                this.surroundMine(a, 0.0, 0.0, -1.0)
            } else if (EntityUtil.getSurroundWeakness(a, 4, -1)) {
                this.surroundMine(a, 0.0, 0.0, 1.0)
            } else if (EntityUtil.getSurroundWeakness(a, 5, -1)) {
                this.surroundMine(a, -1.0, 0.0, 0.0)
            } else if (EntityUtil.getSurroundWeakness(a, 6, -1)) {
                this.surroundMine(a, 1.0, 0.0, 0.0)
            } else if (EntityUtil.getSurroundWeakness(a, 7, -1)) {
                this.surroundMine(a, 0.0, 0.0, -1.0)
            } else if (EntityUtil.getSurroundWeakness(a, 8, -1)) {
                this.surroundMine(a, 0.0, 0.0, 1.0)
            }
        }
    }

    private fun surroundMine(pos: Vec3d, x: Double, y: Double, z: Double) {
        val position = BlockPos.ofFloored(pos).add(x.toInt(), y.toInt(), z.toInt())
        if (MoonBreak.getInstance().isOff()) {
            MoonBreak.getInstance().enable()
        } else if (MoonBreak.getInstance().isOn()) {
            mc.interactionManager!!.attackBlock(position, BlockUtil.getRayTraceFacing(position))
        }
    }

    private fun getTarget(range: Double): PlayerEntity? {
        var target: PlayerEntity? = null
        var distance = range

        for (player in mc.world!!.players) {
            if (!EntityUtil.isntValid(player, range) && EntityUtil.isInHole(player)) {
                if (target == null) {
                    target = player
                    distance = mc.player!!.squaredDistanceTo(player)
                } else if (mc.player!!.squaredDistanceTo(player) < distance) {
                    target = player
                    distance = mc.player!!.squaredDistanceTo(player)
                }
            }
        }

        return target
    }
}
