package me.moon.util

import me.moon.util.Util
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d

object TestUtil : Util {
    @JvmField
    var emptyBlocks: List<Block> = listOf(
        Blocks.AIR,
        Blocks.LAVA,
        Blocks.LAVA,
        Blocks.WATER,
        Blocks.WATER,
        Blocks.VINE,
        Blocks.SNOW,
        Blocks.SHORT_GRASS,
        Blocks.FIRE
    )

    @JvmField
    var rightclickableBlocks: List<Block> = listOf(
        Blocks.CHEST,
        Blocks.TRAPPED_CHEST,
        Blocks.ENDER_CHEST,
        Blocks.WHITE_SHULKER_BOX,
        Blocks.ORANGE_SHULKER_BOX,
        Blocks.MAGENTA_SHULKER_BOX,
        Blocks.LIGHT_BLUE_SHULKER_BOX,
        Blocks.YELLOW_SHULKER_BOX,
        Blocks.LIME_SHULKER_BOX,
        Blocks.PINK_SHULKER_BOX,
        Blocks.GRAY_SHULKER_BOX,
        Blocks.LIGHT_GRAY_SHULKER_BOX,
        Blocks.CYAN_SHULKER_BOX,
        Blocks.PURPLE_SHULKER_BOX,
        Blocks.BLUE_SHULKER_BOX,
        Blocks.BROWN_SHULKER_BOX,
        Blocks.GREEN_SHULKER_BOX,
        Blocks.RED_SHULKER_BOX,
        Blocks.BLACK_SHULKER_BOX,
        Blocks.ANVIL,
        Blocks.OAK_BUTTON,
        Blocks.STONE_BUTTON,
        Blocks.COMPARATOR,
        Blocks.REPEATER,
        Blocks.REPEATER,
        Blocks.COMPARATOR,
        Blocks.OAK_FENCE_GATE,
        Blocks.SPRUCE_FENCE_GATE,
        Blocks.BIRCH_FENCE_GATE,
        Blocks.JUNGLE_FENCE_GATE,
        Blocks.DARK_OAK_FENCE_GATE,
        Blocks.ACACIA_FENCE_GATE,
        Blocks.BREWING_STAND,
        Blocks.DISPENSER,
        Blocks.DROPPER,
        Blocks.LEVER,
        Blocks.NOTE_BLOCK,
        Blocks.JUKEBOX,
        Blocks.BEACON,
        Blocks.RED_BED,
        Blocks.FURNACE,
        Blocks.OAK_DOOR,
        Blocks.SPRUCE_DOOR,
        Blocks.BIRCH_DOOR,
        Blocks.JUNGLE_DOOR,
        Blocks.ACACIA_DOOR,
        Blocks.DARK_OAK_DOOR,
        Blocks.CAKE,
        Blocks.ENCHANTING_TABLE,
        Blocks.DRAGON_EGG,
        Blocks.HOPPER,
        Blocks.REPEATING_COMMAND_BLOCK,
        Blocks.COMMAND_BLOCK,
        Blocks.CHAIN_COMMAND_BLOCK,
        Blocks.CRAFTING_TABLE
    )

    @JvmStatic
    fun placeBlock(pos: BlockPos): Boolean {
        val p = mc.player!!
        if (isBlockEmpty(pos)) {
            for (f in Direction.values()) {
                val neighborBlock = mc.world!!.getBlockState(pos.offset(f)).block
                val vec = Vec3d(
                    pos.x + 0.5 + f.offsetX * 0.5,
                    pos.y + 0.5 + f.offsetY * 0.5,
                    pos.z + 0.5 + f.offsetZ * 0.5
                )
                if (!emptyBlocks.contains(neighborBlock) &&
                    p.getCameraPosVec(mc.renderTickCounter.getTickDelta(true)).distanceTo(vec) <= 4.25
                ) {
                    if (rightclickableBlocks.contains(neighborBlock)) {
                        p.networkHandler.sendPacket(
                            ClientCommandC2SPacket(p, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY)
                        )
                    }

                    p.networkHandler.sendPacket(
                        PlayerInteractBlockC2SPacket(
                            Hand.MAIN_HAND,
                            BlockHitResult(
                                Vec3d(pos.offset(f).x + 0.5, pos.offset(f).y + 0.5, pos.offset(f).z + 0.5),
                                f.opposite,
                                pos.offset(f),
                                false
                            ),
                            0
                        )
                    )
                    if (rightclickableBlocks.contains(neighborBlock)) {
                        p.networkHandler.sendPacket(
                            ClientCommandC2SPacket(p, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY)
                        )
                    }

                    p.swingHand(Hand.MAIN_HAND)
                    return true
                }
            }
        }

        return false
    }

    @JvmStatic
    fun isBlockEmpty(pos: BlockPos): Boolean {
        try {
            if (emptyBlocks.contains(mc.world!!.getBlockState(pos).block)) {
                val box = Box(pos)
                val entityIter = mc.world!!.entities.iterator()

                var e: Entity
                do {
                    if (!entityIter.hasNext()) {
                        return true
                    }
                    e = entityIter.next()
                } while (!(e is LivingEntity) || !box.intersects(e.boundingBox))
            }
        } catch (var4: Exception) {
        }

        return false
    }
}
