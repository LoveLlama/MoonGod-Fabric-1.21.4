package me.moon.features.modules.movement

import me.moon.features.modules.Module
import me.moon.features.setting.Setting
import net.minecraft.block.Blocks
import net.minecraft.util.math.BlockPos

class ReverseStep : Module("ReverseStep", "ReverseStep", Module.Category.MOVEMENT, true, false, false) {
    private val twoBlocks = register(Setting("2Blocks", false))

    override fun onUpdate() {
        if (!fullNullCheck()) {
            val center = mc.player!!.blockPos
            val touchingState = mc.world!!.getBlockState(center.down(2)).block
            val touchingState2 = mc.world!!.getBlockState(center.down(3)).block
            if (!mc.player!!.isInLava && !mc.player!!.isTouchingWater) {
                if (touchingState != Blocks.BEDROCK && touchingState != Blocks.OBSIDIAN) {
                    if ((this.twoBlocks.getValue() && touchingState2 == Blocks.BEDROCK
                            || this.twoBlocks.getValue() && touchingState2 == Blocks.OBSIDIAN)
                        && mc.player!!.isOnGround
                    ) {
                        mc.player!!.velocity = mc.player!!.velocity.subtract(0.0, 1.0, 0.0)
                    }
                } else if (mc.player!!.isOnGround) {
                    mc.player!!.velocity = mc.player!!.velocity.subtract(0.0, 1.0, 0.0)
                }
            }
        }
    }
}
