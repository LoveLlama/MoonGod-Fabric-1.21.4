package me.moon.manager

import me.moon.features.Feature
import me.moon.util.BlockUtil
import me.moon.util.EntityUtil
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.util.math.BlockPos

class HoleManager : Feature() {
    private val midSafety: MutableList<BlockPos> = ArrayList()
    private var holes: MutableList<BlockPos> = ArrayList()

    fun update() {
        if (!fullNullCheck()) {
            holes = calcHoles()
        }
    }

    fun getHoles(): List<BlockPos> = holes

    fun getMidSafety(): List<BlockPos> = midSafety

    fun getSortedHoles(): List<BlockPos> {
        holes.sortBy { hole -> mc.player!!.squaredDistanceTo(hole.x + 0.5, hole.y + 0.5, hole.z + 0.5) }
        return getHoles()
    }

    fun calcHoles(): ArrayList<BlockPos> {
        val safeSpots = ArrayList<BlockPos>()
        midSafety.clear()
        for (pos in BlockUtil.getSphere(EntityUtil.getPlayerPos(mc.player!!), 6.0f, 6, false, true, 0)) {
            if (mc.world!!.getBlockState(pos).isOf(Blocks.AIR)
                && mc.world!!.getBlockState(pos.add(0, 1, 0)).isOf(Blocks.AIR)
                && mc.world!!.getBlockState(pos.add(0, 2, 0)).isOf(Blocks.AIR)
            ) {
                var isSafe = true
                var midSafe = true
                for (offset in surroundOffset) {
                    val block = mc.world!!.getBlockState(pos.add(offset)).block
                    if (BlockUtil.isBlockUnSolid(block)) {
                        midSafe = false
                    }
                    if (block !== Blocks.BEDROCK && block !== Blocks.OBSIDIAN && block !== Blocks.ENDER_CHEST && block !== Blocks.ANVIL) {
                        isSafe = false
                    }
                }
                if (isSafe) {
                    safeSpots.add(pos)
                }
                if (midSafe) {
                    midSafety.add(pos)
                }
            }
        }
        return safeSpots
    }

    fun isSafe(pos: BlockPos): Boolean {
        var isSafe = true
        for (offset in surroundOffset) {
            val block = mc.world!!.getBlockState(pos.add(offset)).block
            if (block !== Blocks.BEDROCK) {
                isSafe = false
                break
            }
        }
        return isSafe
    }

    companion object {
        private val surroundOffset: Array<BlockPos> = BlockUtil.toBlockPos(EntityUtil.getOffsets(0, true))
    }
}
