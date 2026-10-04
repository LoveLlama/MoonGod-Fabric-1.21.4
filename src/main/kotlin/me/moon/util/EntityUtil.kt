package me.moon.util

import me.moon.Moon
import me.moon.mixin.interfaces.IRenderTickCounter
import me.moon.util.Util
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.ExperienceOrbEntity
import net.minecraft.entity.mob.EndermanEntity
import net.minecraft.entity.mob.EndermiteEntity
import net.minecraft.entity.mob.MobEntity
import net.minecraft.entity.mob.SlimeEntity
import net.minecraft.entity.passive.IronGolemEntity
import net.minecraft.entity.passive.PassiveEntity
import net.minecraft.entity.passive.SquidEntity
import net.minecraft.entity.passive.VillagerEntity
import net.minecraft.entity.passive.WolfEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.projectile.FireballEntity
import net.minecraft.entity.projectile.ShulkerBulletEntity
import net.minecraft.entity.vehicle.BoatEntity
import net.minecraft.entity.vehicle.AbstractMinecartEntity
import net.minecraft.item.MiningToolItem
import net.minecraft.item.SwordItem
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket
import net.minecraft.util.Hand
import net.minecraft.util.Formatting
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import java.math.RoundingMode
import java.text.DecimalFormat

object EntityUtil : Util {
    @JvmField
    val antiDropOffsetList = arrayOf(Vec3d(0.0, -2.0, 0.0))

    @JvmField
    val platformOffsetList = arrayOf(
        Vec3d(0.0, -1.0, 0.0), Vec3d(0.0, -1.0, -1.0), Vec3d(0.0, -1.0, 1.0), Vec3d(-1.0, -1.0, 0.0), Vec3d(1.0, -1.0, 0.0)
    )

    @JvmField
    val legOffsetList = arrayOf(
        Vec3d(-1.0, 0.0, 0.0), Vec3d(1.0, 0.0, 0.0), Vec3d(0.0, 0.0, -1.0), Vec3d(0.0, 0.0, 1.0)
    )

    @JvmField
    val OffsetList = arrayOf(
        Vec3d(1.0, 1.0, 0.0), Vec3d(-1.0, 1.0, 0.0), Vec3d(0.0, 1.0, 1.0), Vec3d(0.0, 1.0, -1.0), Vec3d(0.0, 2.0, 0.0)
    )

    @JvmField
    val antiStepOffsetList = arrayOf(
        Vec3d(-1.0, 2.0, 0.0), Vec3d(1.0, 2.0, 0.0), Vec3d(0.0, 2.0, 1.0), Vec3d(0.0, 2.0, -1.0)
    )

    @JvmField
    val antiScaffoldOffsetList = arrayOf(Vec3d(0.0, 3.0, 0.0))

    @JvmStatic
    fun attackEntity(entity: Entity, packet: Boolean, hand: Hand) {
        val p = mc.player!!
        if (packet) {
            p.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.attack(entity, p.isSneaking))
        } else {
            mc.interactionManager?.attackEntity(p, entity)
        }

        p.swingHand(hand)
    }

    @JvmStatic
    fun interpolateEntity(entity: Entity, time: Float): Vec3d {
        return Vec3d(
            entity.prevX + (entity.x - entity.prevX) * time.toDouble(),
            entity.prevY + (entity.y - entity.prevY) * time.toDouble(),
            entity.prevZ + (entity.z - entity.prevZ) * time.toDouble()
        )
    }

    @JvmStatic
    fun getInterpolatedPos(entity: Entity, partialTicks: Float): Vec3d {
        return Vec3d(entity.prevX, entity.prevY, entity.prevZ).add(getInterpolatedAmount(entity, partialTicks))
    }

    @JvmStatic
    fun getInterpolatedRenderPos(entity: Entity, partialTicks: Float): Vec3d {
        return getInterpolatedPos(entity, partialTicks)
            .subtract(mc.gameRenderer.camera.pos.x, mc.gameRenderer.camera.pos.y, mc.gameRenderer.camera.pos.z)
    }

    @JvmStatic
    fun getInterpolatedRenderPos(vec: Vec3d): Vec3d {
        return Vec3d(vec.x, vec.y, vec.z)
            .subtract(mc.gameRenderer.camera.pos.x, mc.gameRenderer.camera.pos.y, mc.gameRenderer.camera.pos.z)
    }

    @JvmStatic
    fun getInterpolatedAmount(entity: Entity, x: Double, y: Double, z: Double): Vec3d {
        return Vec3d(
            (entity.x - entity.prevX) * x,
            (entity.y - entity.prevY) * y,
            (entity.z - entity.prevZ) * z
        )
    }

    @JvmStatic
    fun getInterpolatedAmount(entity: Entity, vec: Vec3d): Vec3d {
        return getInterpolatedAmount(entity, vec.x, vec.y, vec.z)
    }

    @JvmStatic
    fun getInterpolatedAmount(entity: Entity, partialTicks: Float): Vec3d {
        return getInterpolatedAmount(entity, partialTicks.toDouble(), partialTicks.toDouble(), partialTicks.toDouble())
    }

    @JvmStatic
    fun isPassive(entity: Entity): Boolean {
        if (entity is WolfEntity && entity.hasAngerTime()) {
            return false
        } else if (entity !is PassiveEntity && entity !is net.minecraft.entity.mob.AmbientEntity && entity !is SquidEntity) {
            return entity is IronGolemEntity && entity.attacker == null
        } else {
            return true
        }
    }

    @JvmStatic
    fun isSafe(entity: Entity, height: Int, floor: Boolean): Boolean {
        return getUnsafeBlocks(entity, height, floor).size == 0
    }

    @JvmStatic
    fun stopSneaking(isSneaking: Boolean): Boolean {
        val p = mc.player
        if (isSneaking && p != null) {
            p.networkHandler.sendPacket(
                ClientCommandC2SPacket(p, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY)
            )
        }

        return false
    }

    @JvmStatic
    fun getPlayerPos(player: PlayerEntity): BlockPos {
        return BlockPos(Math.floor(player.x).toInt(), Math.floor(player.y).toInt(), Math.floor(player.z).toInt())
    }

    @JvmStatic
    fun getUnsafeBlocks(entity: Entity, height: Int, floor: Boolean): MutableList<Vec3d> {
        return getUnsafeBlocksFromVec3d(entity.pos, height, floor)
    }

    @JvmStatic
    fun isMobAggressive(entity: Entity): Boolean {
        if (entity is net.minecraft.entity.mob.ZombifiedPiglinEntity) {
            if (entity.angerTime > 0) {
                return true
            }
        } else {
            if (entity is WolfEntity) {
                return entity.hasAngerTime() && mc.player != entity.owner
            }

            if (entity is EndermanEntity) {
                return entity.isAngry()
            }
        }

        return isHostileMob(entity)
    }

    @JvmStatic
    fun isNeutralMob(entity: Entity): Boolean {
        return entity is net.minecraft.entity.mob.ZombifiedPiglinEntity || entity is WolfEntity || entity is EndermanEntity
    }

    @JvmStatic
    fun isProjectile(entity: Entity): Boolean {
        return entity is ShulkerBulletEntity || entity is FireballEntity
    }

    @JvmStatic
    fun isVehicle(entity: Entity): Boolean {
        return entity is BoatEntity || entity is AbstractMinecartEntity
    }

    @JvmStatic
    fun isFriendlyMob(entity: Entity): Boolean {
        return (entity is PassiveEntity && !isNeutralMob(entity))
            || entity is net.minecraft.entity.mob.AmbientEntity
            || entity is VillagerEntity
            || entity is IronGolemEntity
            || isNeutralMob(entity) && !isMobAggressive(entity)
    }

    @JvmStatic
    fun isHostileMob(entity: Entity): Boolean {
        return entity.type.isIn(net.minecraft.registry.tag.EntityTypeTags.SKELETONS) && !isNeutralMob(entity)
    }

    private fun posOf(v: Vec3d): BlockPos {
        return BlockPos(Math.floor(v.x).toInt(), Math.floor(v.y).toInt(), Math.floor(v.z).toInt())
    }

    @JvmStatic
    fun getUnsafeBlocksFromVec3d(pos: Vec3d, height: Int, floor: Boolean): MutableList<Vec3d> {
        val vec3d = ArrayList<Vec3d>()

        for (vector in getOffsets(height, floor)) {
            val targetPos = posOf(pos.add(vector))
            val block = mc.world!!.getBlockState(targetPos).block
            if (block == Blocks.AIR
                || !mc.world!!.getBlockState(targetPos).fluidState.isEmpty
                || block == Blocks.SHORT_GRASS
                || block == Blocks.FIRE
                || block == Blocks.DEAD_BUSH
                || block == Blocks.SNOW
            ) {
                vec3d.add(vector)
            }
        }

        return vec3d
    }

    @JvmStatic
    fun getSurroundWeakness(pos: Vec3d, feetMine: Int, render: Int): Boolean {
        when (feetMine) {
            1 -> {
                val raytrace = posOf(pos)
                if (!BlockUtil.canBlockBeSeen((raytrace.x - 2).toDouble(), raytrace.y.toDouble(), raytrace.z.toDouble()) &&
                    Math.sqrt(mc.player!!.squaredDistanceTo((raytrace.x - 2).toDouble(), raytrace.y.toDouble(), raytrace.z.toDouble())) > 3.0
                ) {
                    return false
                }
                val block = mc.world!!.getBlockState(posOf(pos).add(-2, 1, 0)).block
                if (block == Blocks.AIR || block == Blocks.FIRE) {
                    val blocka = mc.world!!.getBlockState(posOf(pos).add(-2, 0, 0)).block
                    if (blocka == Blocks.AIR || blocka == Blocks.FIRE) {
                        val blockb = mc.world!!.getBlockState(posOf(pos).add(-2, -1, 0)).block
                        if ((blockb == Blocks.OBSIDIAN || blockb == Blocks.BEDROCK) && mc.world!!.getBlockState(posOf(pos).add(-1, 0, 0)).block != Blocks.BEDROCK) {
                            return true
                        }
                    }
                }
            }
            2 -> {
                val raytrace = posOf(pos)
                if (!BlockUtil.canBlockBeSeen((raytrace.x + 2).toDouble(), raytrace.y.toDouble(), raytrace.z.toDouble()) &&
                    Math.sqrt(mc.player!!.squaredDistanceTo((raytrace.x + 2).toDouble(), raytrace.y.toDouble(), raytrace.z.toDouble())) > 3.0
                ) {
                    return false
                }
                val block = mc.world!!.getBlockState(posOf(pos).add(2, 1, 0)).block
                if (block == Blocks.AIR || block == Blocks.FIRE) {
                    val blocka = mc.world!!.getBlockState(posOf(pos).add(2, 0, 0)).block
                    if (blocka == Blocks.AIR || blocka == Blocks.FIRE) {
                        val blockb = mc.world!!.getBlockState(posOf(pos).add(2, -1, 0)).block
                        if ((blockb == Blocks.OBSIDIAN || blockb == Blocks.BEDROCK) && mc.world!!.getBlockState(posOf(pos).add(1, 0, 0)).block != Blocks.BEDROCK) {
                            return true
                        }
                    }
                }
            }
            3 -> {
                val raytrace = posOf(pos)
                if (!BlockUtil.canBlockBeSeen(raytrace.x.toDouble(), raytrace.y.toDouble(), (raytrace.z - 2).toDouble()) &&
                    Math.sqrt(mc.player!!.squaredDistanceTo(raytrace.x.toDouble(), raytrace.y.toDouble(), (raytrace.z - 2).toDouble())) > 3.0
                ) {
                    return false
                }
                val block = mc.world!!.getBlockState(posOf(pos).add(0, 1, -2)).block
                if (block == Blocks.AIR || block == Blocks.FIRE) {
                    val blocka = mc.world!!.getBlockState(posOf(pos).add(0, 0, -2)).block
                    if (blocka == Blocks.AIR || blocka == Blocks.FIRE) {
                        val blockb = mc.world!!.getBlockState(posOf(pos).add(0, -1, -2)).block
                        if ((blockb == Blocks.OBSIDIAN || blockb == Blocks.BEDROCK) && mc.world!!.getBlockState(posOf(pos).add(0, 0, -1)).block != Blocks.BEDROCK) {
                            return true
                        }
                    }
                }
            }
            4 -> {
                val raytrace = posOf(pos)
                if (!BlockUtil.canBlockBeSeen(raytrace.x.toDouble(), raytrace.y.toDouble(), (raytrace.z + 2).toDouble()) &&
                    Math.sqrt(mc.player!!.squaredDistanceTo(raytrace.x.toDouble(), raytrace.y.toDouble(), (raytrace.z + 2).toDouble())) > 3.0
                ) {
                    return false
                }
                val block = mc.world!!.getBlockState(posOf(pos).add(0, 1, 2)).block
                if (block == Blocks.AIR || block == Blocks.FIRE) {
                    val blocka = mc.world!!.getBlockState(posOf(pos).add(0, 0, 2)).block
                    if (blocka == Blocks.AIR || blocka == Blocks.FIRE) {
                        val blockb = mc.world!!.getBlockState(posOf(pos).add(0, -1, 2)).block
                        if ((blockb == Blocks.OBSIDIAN || blockb == Blocks.BEDROCK) && mc.world!!.getBlockState(posOf(pos).add(0, 0, 1)).block != Blocks.BEDROCK) {
                            return true
                        }
                    }
                }
            }
            5 -> {
                val raytrace = posOf(pos)
                if (!BlockUtil.canBlockBeSeen((raytrace.x - 1).toDouble(), raytrace.y.toDouble(), raytrace.z.toDouble()) &&
                    Math.sqrt(mc.player!!.squaredDistanceTo((raytrace.x - 1).toDouble(), raytrace.y.toDouble(), raytrace.z.toDouble())) > 3.0
                ) {
                    return false
                }
                val block = mc.world!!.getBlockState(posOf(pos).add(-1, 1, 0)).block
                if ((block == Blocks.AIR || block == Blocks.FIRE) && mc.world!!.getBlockState(posOf(pos).add(-1, 0, 0)).block != Blocks.BEDROCK) {
                    return true
                }
            }
            6 -> {
                val raytrace = posOf(pos)
                if (!BlockUtil.canBlockBeSeen((raytrace.x + 1).toDouble(), raytrace.y.toDouble(), raytrace.z.toDouble()) &&
                    Math.sqrt(mc.player!!.squaredDistanceTo((raytrace.x + 1).toDouble(), raytrace.y.toDouble(), raytrace.z.toDouble())) > 3.0
                ) {
                    return false
                }
                val block = mc.world!!.getBlockState(posOf(pos).add(1, 1, 0)).block
                if ((block == Blocks.AIR || block == Blocks.FIRE) && mc.world!!.getBlockState(posOf(pos).add(1, 0, 0)).block != Blocks.BEDROCK) {
                    return true
                }
            }
            7 -> {
                val raytrace = posOf(pos)
                if (!BlockUtil.canBlockBeSeen(raytrace.x.toDouble(), raytrace.y.toDouble(), (raytrace.z - 1).toDouble()) &&
                    Math.sqrt(mc.player!!.squaredDistanceTo(raytrace.x.toDouble(), raytrace.y.toDouble(), (raytrace.z - 1).toDouble())) > 3.0
                ) {
                    return false
                }
                val block = mc.world!!.getBlockState(posOf(pos).add(0, 1, -1)).block
                if ((block == Blocks.AIR || block == Blocks.FIRE) && mc.world!!.getBlockState(posOf(pos).add(0, 0, -1)).block != Blocks.BEDROCK) {
                    return true
                }
            }
            8 -> {
                val raytrace = posOf(pos)
                if (!BlockUtil.canBlockBeSeen(raytrace.x.toDouble(), raytrace.y.toDouble(), (raytrace.z + 1).toDouble()) &&
                    Math.sqrt(mc.player!!.squaredDistanceTo(raytrace.x.toDouble(), raytrace.y.toDouble(), (raytrace.z + 1).toDouble())) > 3.0
                ) {
                    return false
                }
                val block = mc.world!!.getBlockState(posOf(pos).add(0, 1, 1)).block
                if ((block == Blocks.AIR || block == Blocks.FIRE) && mc.world!!.getBlockState(posOf(pos).add(0, 0, 1)).block != Blocks.BEDROCK) {
                    return true
                }
            }
        }
        when (render) {
            1 -> {
                val block2 = mc.world!!.getBlockState(posOf(pos).add(-2, 1, 0)).block
                if (block2 == Blocks.AIR || block2 == Blocks.FIRE) {
                    val blocka2 = mc.world!!.getBlockState(posOf(pos).add(-2, 0, 0)).block
                    if (blocka2 == Blocks.AIR || blocka2 == Blocks.FIRE) {
                        val blockb2 = mc.world!!.getBlockState(posOf(pos).add(-2, -1, 0)).block
                        if ((blockb2 == Blocks.OBSIDIAN || blockb2 == Blocks.BEDROCK) && mc.world!!.getBlockState(posOf(pos).add(-1, 0, 0)).block != Blocks.BEDROCK) {
                            return true
                        }
                    }
                }
            }
            2 -> {
                val block2 = mc.world!!.getBlockState(posOf(pos).add(2, 1, 0)).block
                if (block2 == Blocks.AIR || block2 == Blocks.FIRE) {
                    val blocka2 = mc.world!!.getBlockState(posOf(pos).add(2, 0, 0)).block
                    if (blocka2 == Blocks.AIR || blocka2 == Blocks.FIRE) {
                        val blockb2 = mc.world!!.getBlockState(posOf(pos).add(2, -1, 0)).block
                        if ((blockb2 == Blocks.OBSIDIAN || blockb2 == Blocks.BEDROCK) && mc.world!!.getBlockState(posOf(pos).add(1, 0, 0)).block != Blocks.BEDROCK) {
                            return true
                        }
                    }
                }
            }
            3 -> {
                val block2 = mc.world!!.getBlockState(posOf(pos).add(0, 1, -2)).block
                if (block2 == Blocks.AIR || block2 == Blocks.FIRE) {
                    val blocka2 = mc.world!!.getBlockState(posOf(pos).add(0, 0, -2)).block
                    if (blocka2 == Blocks.AIR || blocka2 == Blocks.FIRE) {
                        val blockb2 = mc.world!!.getBlockState(posOf(pos).add(0, -1, -2)).block
                        if ((blockb2 == Blocks.OBSIDIAN || blockb2 == Blocks.BEDROCK) && mc.world!!.getBlockState(posOf(pos).add(0, 0, -1)).block != Blocks.BEDROCK) {
                            return true
                        }
                    }
                }
            }
            4 -> {
                val block2 = mc.world!!.getBlockState(posOf(pos).add(0, 1, 2)).block
                if (block2 == Blocks.AIR || block2 == Blocks.FIRE) {
                    val blocka2 = mc.world!!.getBlockState(posOf(pos).add(0, 0, 2)).block
                    if (blocka2 == Blocks.AIR || blocka2 == Blocks.FIRE) {
                        val blockb2 = mc.world!!.getBlockState(posOf(pos).add(0, -1, 2)).block
                        if ((blockb2 == Blocks.OBSIDIAN || blockb2 == Blocks.BEDROCK) && mc.world!!.getBlockState(posOf(pos).add(0, 0, 1)).block != Blocks.BEDROCK) {
                            return true
                        }
                    }
                }
            }
            5 -> {
                val block2 = mc.world!!.getBlockState(posOf(pos).add(-1, 1, 0)).block
                if ((block2 == Blocks.AIR || block2 == Blocks.FIRE) && mc.world!!.getBlockState(posOf(pos).add(-1, 0, 0)).block != Blocks.BEDROCK) {
                    return true
                }
            }
            6 -> {
                val block2 = mc.world!!.getBlockState(posOf(pos).add(1, 1, 0)).block
                if ((block2 == Blocks.AIR || block2 == Blocks.FIRE) && mc.world!!.getBlockState(posOf(pos).add(1, 0, 0)).block != Blocks.BEDROCK) {
                    return true
                }
            }
            7 -> {
                val block2 = mc.world!!.getBlockState(posOf(pos).add(0, 1, -1)).block
                if ((block2 == Blocks.AIR || block2 == Blocks.FIRE) && mc.world!!.getBlockState(posOf(pos).add(0, 0, -1)).block != Blocks.BEDROCK) {
                    return true
                }
            }
            8 -> {
                val block2 = mc.world!!.getBlockState(posOf(pos).add(0, 1, 1)).block
                if ((block2 == Blocks.AIR || block2 == Blocks.FIRE) && mc.world!!.getBlockState(posOf(pos).add(0, 0, 1)).block != Blocks.BEDROCK) {
                    return true
                }
            }
        }
        return false
    }

    @JvmStatic
    fun isInHole(entity: Entity): Boolean {
        return isBedrockHole(posOf(entity.pos))
            || isObbyHole(posOf(entity.pos))
            || isBothHole(posOf(entity.pos))
    }

    @JvmStatic
    fun isObbyHole(blockPos: BlockPos): Boolean {
        for (pos in arrayOf(
            blockPos.north(), blockPos.south(), blockPos.east(), blockPos.west(), blockPos.down()
        )) {
            val touchingState = mc.world!!.getBlockState(pos)
            if (touchingState.block == Blocks.AIR || touchingState.block != Blocks.OBSIDIAN) {
                return false
            }
        }

        return true
    }

    @JvmStatic
    fun isBedrockHole(blockPos: BlockPos): Boolean {
        for (pos in arrayOf(
            blockPos.north(), blockPos.south(), blockPos.east(), blockPos.west(), blockPos.down()
        )) {
            val touchingState = mc.world!!.getBlockState(pos)
            if (touchingState.block == Blocks.AIR || touchingState.block != Blocks.BEDROCK) {
                return false
            }
        }

        return true
    }

    @JvmStatic
    fun isBothHole(blockPos: BlockPos): Boolean {
        for (pos in arrayOf(
            blockPos.north(), blockPos.south(), blockPos.east(), blockPos.west(), blockPos.down()
        )) {
            val touchingState = mc.world!!.getBlockState(pos)
            if (touchingState.block == Blocks.AIR
                || touchingState.block != Blocks.BEDROCK && touchingState.block != Blocks.OBSIDIAN
            ) {
                return false
            }
        }

        return true
    }

    @JvmStatic
    fun getUnsafeBlockArray(entity: Entity, height: Int, floor: Boolean): Array<Vec3d> {
        val list = getUnsafeBlocks(entity, height, floor)
        return list.toTypedArray()
    }

    @JvmStatic
    fun getUnsafeBlockArrayFromVec3d(pos: Vec3d, height: Int, floor: Boolean): Array<Vec3d> {
        val list = getUnsafeBlocksFromVec3d(pos, height, floor)
        return list.toTypedArray()
    }

    @JvmStatic
    fun getDst(vec: Vec3d): Double {
        return mc.player!!.pos.distanceTo(vec)
    }

    @JvmStatic
    fun isTrapped(player: PlayerEntity, antiScaffold: Boolean, antiStep: Boolean, legs: Boolean, platform: Boolean, antiDrop: Boolean): Boolean {
        return getUntrappedBlocks(player, antiScaffold, antiStep, legs, platform, antiDrop).size == 0
    }

    @JvmStatic
    fun isTrappedExtended(
        extension: Int, player: PlayerEntity, antiScaffold: Boolean, antiStep: Boolean, legs: Boolean, platform: Boolean, antiDrop: Boolean, raytrace: Boolean
    ): Boolean {
        return getUntrappedBlocksExtended(extension, player, antiScaffold, antiStep, legs, platform, antiDrop, raytrace).size == 0
    }

    @JvmStatic
    fun getUntrappedBlocks(player: PlayerEntity, antiScaffold: Boolean, antiStep: Boolean, legs: Boolean, platform: Boolean, antiDrop: Boolean): MutableList<Vec3d> {
        val vec3ds = ArrayList<Vec3d>()
        if (!antiStep && getUnsafeBlocks(player, 2, false).size == 4) {
            vec3ds.addAll(getUnsafeBlocks(player, 2, false))
        }

        for (vector in getTrapOffsets(antiScaffold, antiStep, legs, platform, antiDrop)) {
            val vv = player.pos.add(vector)
            val targetPos = BlockPos(Math.floor(vv.x).toInt(), Math.floor(vv.y).toInt(), Math.floor(vv.z).toInt())
            val block = mc.world!!.getBlockState(targetPos).block
            if (block == Blocks.AIR
                || !mc.world!!.getBlockState(targetPos).fluidState.isEmpty
                || block == Blocks.SHORT_GRASS
                || block == Blocks.FIRE
                || block == Blocks.DEAD_BUSH
                || block == Blocks.SNOW
            ) {
                vec3ds.add(vector)
            }
        }

        return vec3ds
    }

    @JvmStatic
    fun isInWater(entity: Entity?): Boolean {
        if (entity == null) {
            return false
        } else {
            val y = entity.y + 0.01

            var x = MathHelper.floor(entity.x)
            while (x < MathHelper.ceil(entity.x)) {
                var z = MathHelper.floor(entity.z)
                while (z < MathHelper.ceil(entity.z)) {
                    val pos = BlockPos(x, y.toInt(), z)
                    if (!mc.world!!.getBlockState(pos).fluidState.isEmpty) {
                        return true
                    }
                    z++
                }
                x++
            }

            return false
        }
    }

    @JvmStatic
    fun isDrivenByPlayer(entityIn: Entity?): Boolean {
        val p = mc.player
        return p != null && entityIn != null && entityIn == p.vehicle
    }

    @JvmStatic
    fun isPlayer(entity: Entity?): Boolean {
        return entity is PlayerEntity
    }

    @JvmStatic
    fun isAboveWater(entity: Entity?): Boolean {
        return isAboveWater(entity, false)
    }

    @JvmStatic
    fun isAboveWater(entity: Entity?, packet: Boolean): Boolean {
        if (entity == null) {
            return false
        } else {
            val y = entity.y - (if (packet) 0.03 else if (isPlayer(entity)) 0.2 else 0.5)

            var x = MathHelper.floor(entity.x)
            while (x < MathHelper.ceil(entity.x)) {
                var z = MathHelper.floor(entity.z)
                while (z < MathHelper.ceil(entity.z)) {
                    val pos = BlockPos(x, MathHelper.floor(y).toInt(), z)
                    if (!mc.world!!.getBlockState(pos).fluidState.isEmpty) {
                        return true
                    }
                    z++
                }
                x++
            }

            return false
        }
    }

    @JvmStatic
    fun getUntrappedBlocksExtended(
        extension: Int, player: PlayerEntity, antiScaffold: Boolean, antiStep: Boolean, legs: Boolean, platform: Boolean, antiDrop: Boolean, raytrace: Boolean
    ): MutableList<Vec3d> {
        val placeTargets = ArrayList<Vec3d>()
        if (extension == 1) {
            placeTargets.addAll(targets(player.pos, antiScaffold, antiStep, legs, platform, antiDrop, raytrace))
        } else {
            var extend = 1

            for (vec3d in MathUtil.getBlockBlocks(player)) {
                if (extend > extension) {
                    break
                }

                placeTargets.addAll(targets(vec3d, antiScaffold, antiStep, legs, platform, antiDrop, raytrace))
                extend++
            }
        }

        val removeList = ArrayList<Vec3d>()

        for (vec3d in placeTargets) {
            val pos = BlockPos(Math.floor(vec3d.x).toInt(), Math.floor(vec3d.y).toInt(), Math.floor(vec3d.z).toInt())
            if (BlockUtil.isPositionPlaceable(pos, raytrace) == -1) {
                removeList.add(vec3d)
            }
        }

        for (vec3d in removeList) {
            placeTargets.remove(vec3d)
        }

        return placeTargets
    }

    @JvmStatic
    fun targets(vec3d: Vec3d, antiScaffold: Boolean, antiStep: Boolean, legs: Boolean, platform: Boolean, antiDrop: Boolean, raytrace: Boolean): MutableList<Vec3d> {
        val placeTargets = ArrayList<Vec3d>()
        if (antiDrop) {
            placeTargets.addAll(BlockUtil.convertVec3ds(vec3d, antiDropOffsetList))
        }

        if (platform) {
            placeTargets.addAll(BlockUtil.convertVec3ds(vec3d, platformOffsetList))
        }

        if (legs) {
            placeTargets.addAll(BlockUtil.convertVec3ds(vec3d, legOffsetList))
        }

        placeTargets.addAll(BlockUtil.convertVec3ds(vec3d, OffsetList))
        if (antiStep) {
            placeTargets.addAll(BlockUtil.convertVec3ds(vec3d, antiStepOffsetList))
        } else {
            val vec3ds = getUnsafeBlocksFromVec3d(vec3d, 2, false)
            if (vec3ds.size == 4) {
                for (vector in vec3ds) {
                    val vv = vec3d.add(vector)
                    val position = BlockPos(Math.floor(vv.x).toInt(), Math.floor(vv.y).toInt(), Math.floor(vv.z).toInt())
                    when (BlockUtil.isPositionPlaceable(position, raytrace)) {
                        -1, 1, 2 -> {}
                        3 -> placeTargets.add(vec3d.add(vector))
                        0 -> {
                            if (antiScaffold) {
                                placeTargets.addAll(BlockUtil.convertVec3ds(vec3d, antiScaffoldOffsetList))
                            }

                            return placeTargets
                        }
                        else -> {
                            if (antiScaffold) {
                                placeTargets.addAll(BlockUtil.convertVec3ds(vec3d, antiScaffoldOffsetList))
                            }

                            return placeTargets
                        }
                    }
                }
            }
        }

        if (antiScaffold) {
            placeTargets.addAll(BlockUtil.convertVec3ds(vec3d, antiScaffoldOffsetList))
        }

        return placeTargets
    }

    @JvmStatic
    fun getOffsetList(y: Int, floor: Boolean): MutableList<Vec3d> {
        val offsets = ArrayList<Vec3d>()
        offsets.add(Vec3d(-1.0, y.toDouble(), 0.0))
        offsets.add(Vec3d(1.0, y.toDouble(), 0.0))
        offsets.add(Vec3d(0.0, y.toDouble(), -1.0))
        offsets.add(Vec3d(0.0, y.toDouble(), 1.0))
        if (floor) {
            offsets.add(Vec3d(0.0, (y - 1).toDouble(), 0.0))
        }

        return offsets
    }

    @JvmStatic
    fun getOffsets(y: Int, floor: Boolean): Array<Vec3d> {
        val offsets = getOffsetList(y, floor)
        return offsets.toTypedArray()
    }

    @JvmStatic
    fun getTrapOffsets(antiScaffold: Boolean, antiStep: Boolean, legs: Boolean, platform: Boolean, antiDrop: Boolean): Array<Vec3d> {
        val offsets = getTrapOffsetsList(antiScaffold, antiStep, legs, platform, antiDrop)
        return offsets.toTypedArray()
    }

    @JvmStatic
    fun getTrapOffsetsList(antiScaffold: Boolean, antiStep: Boolean, legs: Boolean, platform: Boolean, antiDrop: Boolean): MutableList<Vec3d> {
        val offsets = ArrayList(getOffsetList(1, false))
        offsets.add(Vec3d(0.0, 2.0, 0.0))
        if (antiScaffold) {
            offsets.add(Vec3d(0.0, 3.0, 0.0))
        }

        if (antiStep) {
            offsets.addAll(getOffsetList(2, false))
        }

        if (legs) {
            offsets.addAll(getOffsetList(0, false))
        }

        if (platform) {
            offsets.addAll(getOffsetList(-1, false))
            offsets.add(Vec3d(0.0, -1.0, 0.0))
        }

        if (antiDrop) {
            offsets.add(Vec3d(0.0, -2.0, 0.0))
        }

        return offsets
    }

    @JvmStatic
    fun getHeightOffsets(min: Int, max: Int): Array<Vec3d> {
        val offsets = ArrayList<Vec3d>()

        for (i in min..max) {
            offsets.add(Vec3d(0.0, i.toDouble(), 0.0))
        }

        return offsets.toTypedArray()
    }

    @JvmStatic
    fun getRoundedBlockPos(entity: Entity): BlockPos {
        val v = MathUtil.roundVec(entity.pos, 0)
        return BlockPos(Math.floor(v.x).toInt(), Math.floor(v.y).toInt(), Math.floor(v.z).toInt())
    }

    @JvmStatic
    fun isLiving(entity: Entity?): Boolean {
        return entity is LivingEntity
    }

    @JvmStatic
    fun isAlive(entity: Entity): Boolean {
        return isLiving(entity) && !entity.isRemoved && (entity as LivingEntity).health > 0.0f
    }

    @JvmStatic
    fun isDead(entity: Entity): Boolean {
        return !isAlive(entity)
    }

    @JvmStatic
    fun isntValid(entity: Entity?, range: Double): Boolean {
        val p = mc.player!!
        return entity == null
            || isDead(entity)
            || entity == p
            || entity is PlayerEntity && Moon.friendManager!!.isFriend(entity.gameProfile.name)
            || p.squaredDistanceTo(entity) > MathUtil.square(range)
    }

    @JvmStatic
    fun holdingWeapon(player: PlayerEntity): Boolean {
        return player.mainHandStack.item is SwordItem || player.mainHandStack.item is MiningToolItem
    }

    @JvmStatic
    fun rayTraceHitCheck(entity: Entity, shouldCheck: Boolean): Boolean {
        return !shouldCheck || mc.player!!.canSee(entity)
    }

    @JvmStatic
    fun isMoving(): Boolean {
        val p = mc.player!!
        return p.input.movementForward.toDouble() != 0.0 || p.input.movementSideways.toDouble() != 0.0
    }

    @JvmStatic
    fun getPlayerPosWithEntity(): BlockPos {
        val p = mc.player!!
        val vehicle = p.vehicle
        return BlockPos(
            (vehicle?.x ?: p.x).toInt(),
            (vehicle?.y ?: p.y).toInt(),
            (vehicle?.z ?: p.z).toInt()
        )
    }

    @JvmStatic
    fun getTextRadarPlayers(): MutableMap<String, Int> {
        val output = HashMap<String, Int>()
        val dfHealth = DecimalFormat("#.#")
        dfHealth.roundingMode = RoundingMode.CEILING
        val dfDistance = DecimalFormat("#.#")
        dfDistance.roundingMode = RoundingMode.CEILING
        val healthSB = StringBuilder()
        val distanceSB = StringBuilder()

        for (player in mc.world!!.players) {
            val p = mc.player!!
            if (!player.isInvisible && !player.gameProfile.name.equals(p.gameProfile.name, ignoreCase = true)) {
                val hpRaw = ((player.health.toInt()) + player.absorptionAmount).toInt()
                val hp = dfHealth.format(hpRaw.toLong())
                healthSB.append("§")
                if (hpRaw >= 20) {
                    healthSB.append("a")
                } else if (hpRaw >= 10) {
                    healthSB.append("e")
                } else if (hpRaw >= 5) {
                    healthSB.append("6")
                } else {
                    healthSB.append("c")
                }

                healthSB.append(hp)
                val distanceInt = p.distanceTo(player).toInt()
                val distance = dfDistance.format(distanceInt.toLong())
                distanceSB.append("§")
                if (distanceInt >= 25) {
                    distanceSB.append("a")
                } else if (distanceInt > 10) {
                    distanceSB.append("6")
                } else {
                    distanceSB.append("c")
                }

                distanceSB.append(distance)
                output[
                    healthSB.toString()
                        + " "
                        + (if (Moon.friendManager!!.isFriend(player.gameProfile.name)) Formatting.AQUA else Formatting.RED)
                        + player.gameProfile.name
                        + " "
                        + distanceSB.toString()
                        + " §f0"
                ] = mc.player!!.distanceTo(player).toInt()
                healthSB.setLength(0)
                distanceSB.setLength(0)
            }
        }

        var result: Map<String, Int> = output
        if (!result.isEmpty()) {
            result = MathUtil.sortByValue(result, false)
        }

        return HashMap(result)
    }

    @JvmStatic
    fun isAboveBlock(entity: Entity, blockPos: BlockPos): Boolean {
        return entity.y >= blockPos.y.toDouble()
    }

    @JvmStatic
    fun setTimer(speed: Float) {
        (mc.renderTickCounter as IRenderTickCounter).setTickSpeed(speed)
    }

    @JvmStatic
    fun resetTimer() {
        (mc.renderTickCounter as IRenderTickCounter).setTickSpeed(1.0f)
    }
}
