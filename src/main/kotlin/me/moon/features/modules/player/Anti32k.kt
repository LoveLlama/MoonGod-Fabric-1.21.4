package me.moon.features.modules.player

import me.moon.features.modules.Module
import me.moon.features.modules.player.MoonBreak
import me.moon.features.setting.Setting
import me.moon.util.BlockUtil
import me.moon.util.InventoryUtil
import net.minecraft.block.Blocks
import net.minecraft.item.Items
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d

class Anti32k : Module("Anti32k", "Anti32k", Module.Category.PLAYER, true, false, false) {
    private val range = register(Setting("Range", 5, 3, 5))

    init { setInstance() }
    private fun setInstance() { INSTANCE = this }

    override fun onTick() {
        if (!fullNullCheck()) {
            val mainSlot = mc.player!!.inventory.selectedSlot
            if (MoonBreak.getInstance().isOff()) {
                MoonBreak.getInstance().enable()
            }

            for (blockPos in breakPos(this.range.getValue().toFloat())) {
                val slotPick = InventoryUtil.getItemHotbar(Items.DIAMOND_PICKAXE)
                if (slotPick == -1) {
                    return
                }
                if (blockPos != null) {
                    if (mc.world!!.getBlockState(blockPos).block == Blocks.HOPPER
                        && mc.world!!.getBlockState(blockPos.up()).block == Blocks.SHULKER_BOX
                    ) {
                        mc.player!!.inventory.selectedSlot = slotPick
                        mc.player!!.networkHandler.sendPacket(net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket(slotPick))
                        mc.player!!.swingHand(Hand.MAIN_HAND)
                        val above = blockPos.up()
                        mc.interactionManager!!.interactBlock(mc.player!!, Hand.MAIN_HAND, BlockHitResult(Vec3d(above.x.toDouble(), above.y.toDouble(), above.z.toDouble()), Direction.UP, above, false))
                        mc.interactionManager!!.updateBlockBreakingProgress(blockPos, BlockUtil.getRayTraceFacing(blockPos))
                    } else {
                        mc.player!!.inventory.selectedSlot = mainSlot
                        mc.player!!.networkHandler.sendPacket(net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket(mainSlot))
                    }
                }
            }
        }
    }

    private fun breakPos(placeRange: Float): List<BlockPos> {
        val positions: MutableList<BlockPos> = ArrayList()
        positions.addAll(
            BlockUtil.getSphere(
                BlockPos(Math.floor(mc.player!!.x).toInt(), Math.floor(mc.player!!.y).toInt(), Math.floor(mc.player!!.z).toInt()),
                placeRange,
                0,
                false,
                true,
                0
            )
        )
        return positions
    }

    companion object {
        @JvmField
        var INSTANCE: Anti32k = Anti32k()

        @JvmStatic
        fun getInstance(): Anti32k = INSTANCE
    }
}
