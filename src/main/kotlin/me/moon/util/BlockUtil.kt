package me.moon.util

import me.moon.Moon
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.entity.Entity
import net.minecraft.entity.decoration.EndCrystalEntity
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.ExperienceOrbEntity
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d

object BlockUtil : Util {
    @JvmField
    var armorTarget = false

    @JvmField
    val blackList: List<Block> = listOf(
        Blocks.ENDER_CHEST,
        Blocks.CHEST,
        Blocks.TRAPPED_CHEST,
        Blocks.CRAFTING_TABLE,
        Blocks.ANVIL,
        Blocks.BREWING_STAND,
        Blocks.HOPPER,
        Blocks.DROPPER,
        Blocks.DISPENSER,
        Blocks.OAK_TRAPDOOR,
        Blocks.ENCHANTING_TABLE
    )

    @JvmField
    val shulkerList: List<Block> = listOf(
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
        Blocks.BLACK_SHULKER_BOX
    )

    @JvmField
    val unSafeBlocks: List<Block> = listOf(Blocks.OBSIDIAN, Blocks.BEDROCK, Blocks.ENDER_CHEST, Blocks.ANVIL)

    @JvmField
    var unSolidBlocks: List<Block> = listOf(
        Blocks.LAVA,
        Blocks.FLOWER_POT,
        Blocks.SNOW,
        Blocks.WHITE_CARPET,
        Blocks.END_ROD,
        Blocks.SKELETON_SKULL,
        Blocks.FLOWER_POT,
        Blocks.TRIPWIRE,
        Blocks.TRIPWIRE_HOOK,
        Blocks.OAK_BUTTON,
        Blocks.LEVER,
        Blocks.STONE_BUTTON,
        Blocks.LADDER,
        Blocks.COMPARATOR,
        Blocks.COMPARATOR,
        Blocks.REPEATER,
        Blocks.REPEATER,
        Blocks.REDSTONE_TORCH,
        Blocks.REDSTONE_TORCH,
        Blocks.REDSTONE_WIRE,
        Blocks.AIR,
        Blocks.NETHER_PORTAL,
        Blocks.END_PORTAL,
        Blocks.WATER,
        Blocks.WATER,
        Blocks.LAVA,
        Blocks.LAVA,
        Blocks.OAK_SAPLING,
        Blocks.POPPY,
        Blocks.DANDELION,
        Blocks.BROWN_MUSHROOM,
        Blocks.RED_MUSHROOM,
        Blocks.WHEAT,
        Blocks.CARROTS,
        Blocks.POTATOES,
        Blocks.BEETROOTS,
        Blocks.SUGAR_CANE,
        Blocks.PUMPKIN_STEM,
        Blocks.MELON_STEM,
        Blocks.LILY_PAD,
        Blocks.NETHER_WART,
        Blocks.COCOA,
        Blocks.CHORUS_FLOWER,
        Blocks.CHORUS_PLANT,
        Blocks.SHORT_GRASS,
        Blocks.DEAD_BUSH,
        Blocks.VINE,
        Blocks.FIRE,
        Blocks.RAIL,
        Blocks.ACTIVATOR_RAIL,
        Blocks.DETECTOR_RAIL,
        Blocks.POWERED_RAIL,
        Blocks.TORCH
    )

    @JvmStatic
    fun getPossibleSides(pos: BlockPos): MutableList<Direction> {
        val facings = ArrayList<Direction>()

        for (side in Direction.values()) {
            val neighbour = pos.offset(side)
            val state = mc.world!!.getBlockState(neighbour)
            if (!state.isReplaceable() && !state.isAir) {
                facings.add(side)
            }
        }

        return facings
    }

    @JvmStatic
    fun getFirstFacing(pos: BlockPos): Direction? {
        val iterator = getPossibleSides(pos).iterator()
        return if (iterator.hasNext()) iterator.next() else null
    }

    @JvmStatic
    fun getRayTraceFacing(pos: BlockPos): Direction {
        val result = mc.world!!.raycast(
            net.minecraft.world.RaycastContext(
                Vec3d(mc.player!!.x, mc.player!!.eyeY, mc.player!!.z),
                Vec3d(pos.x + 0.5, pos.y - 0.5, pos.z + 0.5),
                net.minecraft.world.RaycastContext.ShapeType.COLLIDER,
                net.minecraft.world.RaycastContext.FluidHandling.NONE,
                mc.player!!
            )
        )
        return if (result != null && result.type == net.minecraft.util.hit.HitResult.Type.BLOCK) result.side else Direction.UP
    }

    @JvmStatic
    fun isPositionPlaceable(pos: BlockPos, rayTrace: Boolean): Int {
        return isPositionPlaceable(pos, rayTrace, true)
    }

    @JvmStatic
    fun isPositionPlaceable(pos: BlockPos, rayTrace: Boolean, entityCheck: Boolean): Int {
        val state = mc.world!!.getBlockState(pos)
        val block = state.block
        if (!state.isAir
            && !state.fluidState.isEmpty
            && block != Blocks.SHORT_GRASS
            && block != Blocks.FIRE
            && block != Blocks.DEAD_BUSH
            && block != Blocks.SNOW
        ) {
            return 0
        } else if (!rayTracePlaceCheck(pos, rayTrace, 0.0f)) {
            return -1
        } else {
            if (entityCheck) {
                for (entity in mc.world!!.getEntitiesByClass(Entity::class.java, Box(pos)) { true }) {
                    if (entity !is ItemEntity && entity !is ExperienceOrbEntity) {
                        return 1
                    }
                }
            }

            for (side in getPossibleSides(pos)) {
                if (canBeClicked(pos.offset(side))) {
                    return 3
                }
            }

            return 2
        }
    }

    @JvmStatic
    fun rightClickBlock(pos: BlockPos, vec: Vec3d, hand: Hand, direction: Direction, packet: Boolean) {
        if (packet) {
            mc.player!!.networkHandler.sendPacket(
                PlayerInteractBlockC2SPacket(
                    hand,
                    BlockHitResult(vec, direction, pos, false),
                    0
                )
            )
        } else {
            mc.interactionManager?.interactBlock(mc.player!!, hand, BlockHitResult(vec, direction, pos, false))
        }

        mc.player!!.swingHand(Hand.MAIN_HAND)
        mc.itemUseCooldown = 4
    }

    @JvmStatic
    fun placeBlock(pos: BlockPos, hand: Hand, rotate: Boolean, packet: Boolean, isSneaking: Boolean): Boolean {
        var sneaking = false
        val side = getFirstFacing(pos)
        if (side == null) {
            return isSneaking
        } else {
            val neighbour = pos.offset(side)
            val opposite = side.opposite
            val hitVec = Vec3d(neighbour).add(0.5, 0.5, 0.5).add(Vec3d(opposite.vector).multiply(0.5))
            val neighbourBlock = mc.world!!.getBlockState(neighbour).block
            if (!mc.player!!.isSneaking && (blackList.contains(neighbourBlock) || shulkerList.contains(neighbourBlock))) {
                mc.player!!.networkHandler.sendPacket(
                    ClientCommandC2SPacket(mc.player!!, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY)
                )
                mc.player!!.isSneaking = true
                sneaking = true
            }

            if (rotate) {
                RotationUtil.faceVector(hitVec, true)
            }

            rightClickBlock(neighbour, hitVec, hand, opposite, packet)
            mc.player!!.swingHand(Hand.MAIN_HAND)
            mc.itemUseCooldown = 4
            return sneaking || isSneaking
        }
    }

    @JvmStatic
    fun placeBlockSmartRotate(pos: BlockPos, hand: Hand, rotate: Boolean, packet: Boolean, isSneaking: Boolean): Boolean {
        var sneaking = false
        val side = getFirstFacing(pos)
        if (side == null) {
            return isSneaking
        } else {
            val neighbour = pos.offset(side)
            val opposite = side.opposite
            val hitVec = Vec3d(neighbour).add(0.5, 0.5, 0.5).add(Vec3d(opposite.vector).multiply(0.5))
            val neighbourBlock = mc.world!!.getBlockState(neighbour).block
            if (!mc.player!!.isSneaking && (blackList.contains(neighbourBlock) || shulkerList.contains(neighbourBlock))) {
                mc.player!!.networkHandler.sendPacket(
                    ClientCommandC2SPacket(mc.player!!, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY)
                )
                sneaking = true
            }

            if (rotate) {
                Moon.rotationManager!!.lookAtVec3d(hitVec)
            }

            rightClickBlock(neighbour, hitVec, hand, opposite, packet)
            return sneaking || isSneaking
        }
    }

    @JvmStatic
    fun placeBlockStopSneaking(pos: BlockPos, hand: Hand, rotate: Boolean, packet: Boolean, isSneaking: Boolean) {
        val sneaking = placeBlockSmartRotate(pos, hand, rotate, packet, isSneaking)
        if (!isSneaking && sneaking) {
            mc.player!!.networkHandler.sendPacket(
                ClientCommandC2SPacket(mc.player!!, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY)
            )
        }
    }

    @JvmStatic
    fun getHelpingBlocks(vec3d: Vec3d): Array<Vec3d> {
        return arrayOf(
            Vec3d(vec3d.x, vec3d.y - 1.0, vec3d.z),
            Vec3d(
                if (vec3d.x != 0.0) vec3d.x * 2.0 else vec3d.x,
                vec3d.y,
                if (vec3d.x != 0.0) vec3d.z else vec3d.z * 2.0
            ),
            Vec3d(
                if (vec3d.x == 0.0) vec3d.x + 1.0 else vec3d.x,
                vec3d.y,
                if (vec3d.x == 0.0) vec3d.z else vec3d.z + 1.0
            ),
            Vec3d(
                if (vec3d.x == 0.0) vec3d.x - 1.0 else vec3d.x,
                vec3d.y,
                if (vec3d.x == 0.0) vec3d.z else vec3d.z - 1.0
            ),
            Vec3d(vec3d.x, vec3d.y + 1.0, vec3d.z)
        )
    }

    @JvmStatic
    fun getSphere(loc: BlockPos, r: Float, h: Int, hollow: Boolean, sphere: Boolean, plus_y: Int): MutableList<BlockPos> {
        val circleBlocks = ArrayList<BlockPos>()
        val cx = loc.x
        val cy = loc.y
        val cz = loc.z

        var x = cx - r.toInt()
        while (x <= (cx + r).toInt()) {
            var z = cz - r.toInt()
            while (z <= (cz + r).toInt()) {
                var y = if (sphere) cy - r.toInt() else cy
                while (y < (if (sphere) (cy + r).toInt() else cy + h)) {
                    val dist = (cx - x) * (cx - x) + (cz - z) * (cz - z) + (if (sphere) (cy - y) * (cy - y) else 0)
                    if (dist < (r * r).toDouble() && (!hollow || !(dist < ((r - 1.0f) * (r - 1.0f)).toDouble()))) {
                        val l = BlockPos(x, y + plus_y, z)
                        circleBlocks.add(l)
                    }
                    y++
                }
                z++
            }
            x++
        }

        return circleBlocks
    }

    @JvmStatic
    fun canPlaceCrystal(blockPos: BlockPos): Boolean {
        val boost = blockPos.add(0, 1, 0)
        val boost2 = blockPos.add(0, 2, 0)

        try {
            if (mc.world!!.getBlockState(blockPos).block != Blocks.BEDROCK
                && mc.world!!.getBlockState(blockPos).block != Blocks.OBSIDIAN
            ) {
                return false
            } else if (mc.world!!.getBlockState(boost).block == Blocks.AIR
                && mc.world!!.getBlockState(boost2).block == Blocks.AIR
            ) {
                for (entity in mc.world!!.getEntitiesByClass(Entity::class.java, Box(boost)) { true }) {
                    if (entity !is EndCrystalEntity) {
                        return false
                    }
                }

                for (entity in mc.world!!.getEntitiesByClass(Entity::class.java, Box(boost2)) { true }) {
                    if (entity !is EndCrystalEntity) {
                        return false
                    }
                }

                return true
            } else {
                return false
            }
        } catch (var5: Exception) {
            return false
        }
    }

    @JvmStatic
    fun getDistance32k(player: Entity, pos: BlockPos): Double {
        val d0 = player.x - pos.x
        val d1 = player.y + 1.0 - pos.y
        val d2 = player.z - pos.z
        return Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2)
    }

    @JvmStatic
    fun canBlockBeSeen(x: Double, y: Double, z: Double): Boolean {
        return mc.world!!.raycast(
            net.minecraft.world.RaycastContext(
                Vec3d(mc.player!!.x, mc.player!!.eyeY, mc.player!!.z),
                Vec3d(x, y + 1.7, z),
                net.minecraft.world.RaycastContext.ShapeType.COLLIDER,
                net.minecraft.world.RaycastContext.FluidHandling.NONE,
                mc.player!!
            )
        ) == null
    }

    @JvmStatic
    fun canBeClicked(pos: BlockPos): Boolean {
        return getState(pos).isFullCube(mc.world!!, pos)
    }

    private fun getBlock(pos: BlockPos): Block {
        return getState(pos).block
    }

    private fun getState(pos: BlockPos): net.minecraft.block.BlockState {
        return mc.world!!.getBlockState(pos)
    }

    @JvmStatic
    fun isBlockAboveEntitySolid(entity: Entity?): Boolean {
        if (entity != null) {
            val pos = BlockPos(Math.floor(entity.x).toInt(), Math.floor(entity.y + 2.0).toInt(), Math.floor(entity.z).toInt())
            return isBlockSolid(pos)
        } else {
            return false
        }
    }

    @JvmStatic
    fun placeCrystalOnBlock(pos: BlockPos, hand: Hand) {
        val hitResult = BlockHitResult(Vec3d(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble()), Direction.UP, pos, false)
        mc.interactionManager?.interactBlock(mc.player!!, hand, hitResult)
        mc.player!!.swingHand(hand)
    }

    @JvmStatic
    fun toBlockPos(vec3ds: Array<Vec3d>): Array<BlockPos> {
        val list = arrayOfNulls<BlockPos>(vec3ds.size)

        for (i in vec3ds.indices) {
            list[i] = BlockPos(Math.floor(vec3ds[i].x).toInt(), Math.floor(vec3ds[i].y).toInt(), Math.floor(vec3ds[i].z).toInt())
        }

        return list.requireNoNulls()
    }

    @JvmStatic
    fun posToVec3d(pos: BlockPos): Vec3d {
        return Vec3d(pos)
    }

    @JvmStatic
    fun vec3dToPos(vec3d: Vec3d): BlockPos {
        return BlockPos(Math.floor(vec3d.x).toInt(), Math.floor(vec3d.y).toInt(), Math.floor(vec3d.z).toInt())
    }

    @JvmStatic
    fun isBlockBelowEntitySolid(entity: Entity?): Boolean {
        if (entity != null) {
            val pos = BlockPos(Math.floor(entity.x).toInt(), Math.floor(entity.y - 1.0).toInt(), Math.floor(entity.z).toInt())
            return isBlockSolid(pos)
        } else {
            return false
        }
    }

    @JvmStatic
    fun isBlockSolid(pos: BlockPos): Boolean {
        return !isBlockUnSolid(pos)
    }

    @JvmStatic
    fun isBlockUnSolid(pos: BlockPos): Boolean {
        return isBlockUnSolid(mc.world!!.getBlockState(pos).block)
    }

    @JvmStatic
    fun isBlockUnSolid(block: Block): Boolean {
        return unSolidBlocks.contains(block)
    }

    @JvmStatic
    fun isBlockUnSafe(block: Block): Boolean {
        return unSafeBlocks.contains(block)
    }

    @JvmStatic
    fun convertVec3ds(vec3d: Vec3d, input: Array<Vec3d>): Array<Vec3d> {
        val output = arrayOfNulls<Vec3d>(input.size)

        for (i in input.indices) {
            output[i] = vec3d.add(input[i])
        }

        return output.requireNoNulls()
    }

    @JvmStatic
    fun isPosInFov(pos: BlockPos): Boolean {
        val dirnumber = RotationUtil.getDirection4D()
        if (dirnumber == 0 && pos.z - mc.player!!.pos.z < 0.0) {
            return false
        } else if (dirnumber == 1 && pos.x - mc.player!!.pos.x > 0.0) {
            return false
        } else {
            return if (dirnumber == 2 && pos.z - mc.player!!.pos.z > 0.0) false else dirnumber != 3 || pos.x - mc.player!!.pos.x >= 0.0
        }
    }

    @JvmStatic
    fun canBreak(pos: BlockPos): Boolean {
        val blockState = mc.world!!.getBlockState(pos)
        return blockState.getHardness(mc.world!!, pos) != -1.0f
    }

    @JvmStatic
    fun isValidBlock(pos: BlockPos): Boolean {
        val block = mc.world!!.getBlockState(pos).block
        return block.defaultState.fluidState.isEmpty && block != Blocks.AIR
    }

    @JvmStatic
    fun isScaffoldPos(pos: BlockPos): Boolean {
        return mc.world!!.getBlockState(pos).isAir
            || mc.world!!.getBlockState(pos).block == Blocks.SNOW
            || mc.world!!.getBlockState(pos).block == Blocks.SHORT_GRASS
            || !mc.world!!.getBlockState(pos).fluidState.isEmpty
    }

    @JvmStatic
    fun rayTracePlaceCheck(pos: BlockPos, shouldCheck: Boolean, height: Float): Boolean {
        return !shouldCheck
            || mc.world!!.raycast(
                net.minecraft.world.RaycastContext(
                    Vec3d(mc.player!!.x, mc.player!!.eyeY, mc.player!!.z),
                    Vec3d(pos.x.toDouble(), pos.y + height.toDouble(), pos.z.toDouble()),
                    net.minecraft.world.RaycastContext.ShapeType.COLLIDER,
                    net.minecraft.world.RaycastContext.FluidHandling.NONE,
                    mc.player!!
                )
            ) == null
    }

    @JvmStatic
    fun rayTracePlaceCheck(pos: BlockPos, shouldCheck: Boolean): Boolean {
        return rayTracePlaceCheck(pos, shouldCheck, 1.0f)
    }
}
