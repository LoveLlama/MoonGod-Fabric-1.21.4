package me.moon.features.modules.combat

import me.moon.Moon
import me.moon.features.command.Command
import me.moon.features.modules.Module
import me.moon.features.setting.Setting
import me.moon.util.BlockUtil
import me.moon.util.EntityUtil
import me.moon.util.InventoryUtil
import me.moon.util.Timer
import net.minecraft.block.Blocks
import net.minecraft.item.BlockItem
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket
import net.minecraft.util.Formatting
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d

class Surround : Module("Surround", "Surrounds you with Obsidian", Module.Category.COMBAT, true, false, false) {
    private val blocksPerTick = register(Setting("BlocksPerTick", 12, 1, 20))
    private val delay = register(Setting("Delay", 0, 0, 250))
    private val toggle = register(Setting("AutoToggle", false))
    private val noGhost = register(Setting("PacketPlace", false))
    private val center = register(Setting("TPCenter", false))
    private val rotate = register(Setting("Rotate", true))
    private val timer = Timer()
    private val retryTimer = Timer()
    private val extendingBlocks = HashSet<Vec3d>()
    private val retries = HashMap<BlockPos, Int>()
    private var isSafe = false
    private var startPos: BlockPos? = null
    private var didPlace = false
    private var lastHotbarSlot = 0
    private var isSneaking = false
    private var placements = 0
    private var extenders = 1
    private var obbySlot = -1
    private var offHand = false

    override fun onEnable() {
        if (!fullNullCheck()) {
            this.lastHotbarSlot = mc.player!!.inventory.selectedSlot
            this.startPos = EntityUtil.getRoundedBlockPos(mc.player!!)
            if (this.center.getValue()) {
                Moon.positionManager!!.setPositionPacket(
                    this.startPos!!.x + 0.5, this.startPos!!.y.toDouble(), this.startPos!!.z + 0.5, true, true, true
                )
            }

            this.retries.clear()
            this.retryTimer.reset()
        }
    }

    override fun onTick() {
        if (!fullNullCheck()) {
            this.doFeetPlace()
        }
    }

    override fun onDisable() {
        this.isSneaking = EntityUtil.stopSneaking(this.isSneaking)
    }

    override fun getDisplayInfo(): String {
        return if (this.isSafe) Formatting.RED.toString() + "Unsafe" else Formatting.GREEN.toString() + "Safe"
    }

    private fun doFeetPlace() {
        if (!this.check()) {
            if (!EntityUtil.isSafe(mc.player!!, 0, true)) {
                this.isSafe = true
                this.placeBlocks(mc.player!!.pos, EntityUtil.getUnsafeBlockArray(mc.player!!, 0, true), true, false, false)
            } else {
                this.isSafe = false
            }

            this.processExtendingBlocks()
            if (this.didPlace) {
                this.timer.reset()
            }

            if (this.toggle.getValue()) {
                this.toggle()
            }
        }
    }

    private fun processExtendingBlocks() {
        if (this.extendingBlocks.size == 2 && this.extenders < 1) {
            val array = arrayOfNulls<Vec3d>(2)
            var i = 0
            for (vec3d in this.extendingBlocks) {
                array[i] = vec3d
                ++i
            }

            val placementslefore = this.placements
            val close = this.areClose(array)
            if (close != null) {
                this.placeBlocks(close, EntityUtil.getUnsafeBlockArrayFromVec3d(close, 0, true), true, false, true)
            }

            if (placementslefore < this.placements) {
                this.extendingBlocks.clear()
            }
        } else if (this.extendingBlocks.size > 2 || this.extenders >= 1) {
            this.extendingBlocks.clear()
        }
    }

    private fun areClose(vec3ds: Array<Vec3d?>): Vec3d? {
        var matches = 0
        for (vec3d in vec3ds) {
            for (pos in EntityUtil.getUnsafeBlockArray(mc.player!!, 0, true)) {
                if (vec3d == pos) {
                    ++matches
                }
            }
        }

        return if (matches == 2) mc.player!!.pos.add(vec3ds[0]!!.add(vec3ds[1]!!)) else null
    }

    private fun placeBlocks(pos: Vec3d, vec3ds: Array<Vec3d>, hasHelpingBlocks: Boolean, isHelping: Boolean, isExtending: Boolean): Boolean {
        var gotHelp = true

        for (vec3d in vec3ds) {
            gotHelp = true
            val position = BlockPos.ofFloored(pos).add(vec3d.x.toInt(), vec3d.y.toInt(), vec3d.z.toInt())
            when (BlockUtil.isPositionPlaceable(position, false)) {
                1 -> {
                    if (this.retries[position] == null || this.retries[position]!! < 4) {
                        this.placeBlock(position)
                        this.retries[position] = if (this.retries[position] == null) 1 else this.retries[position]!! + 1
                        this.retryTimer.reset()
                    } else if (Moon.speedManager!!.getSpeedKpH() == 0.0 && !isExtending && this.extenders < 1) {
                        this.placeBlocks(
                            mc.player!!.pos.add(vec3d), EntityUtil.getUnsafeBlockArrayFromVec3d(mc.player!!.pos.add(vec3d), 0, true),
                            hasHelpingBlocks, false, true
                        )
                        this.extendingBlocks.add(vec3d)
                        ++this.extenders
                    }
                }
                2 -> {
                    if (!hasHelpingBlocks) {
                        continue
                    }

                    gotHelp = this.placeBlocks(pos, BlockUtil.getHelpingBlocks(vec3d), false, true, true)
                }
            }
            if (gotHelp) {
                this.placeBlock(position)
            }

            if (isHelping) {
                return true
            }
        }

        return false
    }

    private fun check(): Boolean {
        if (fullNullCheck()) {
            if (this.isOn()) {
                this.disable()
            }

            return true
        } else {
            if (this.startPos == null) {
                this.startPos = EntityUtil.getRoundedBlockPos(mc.player!!)
            }

            val obbySlot = InventoryUtil.findHotbarBlock(Blocks.OBSIDIAN)
            val eChestSot = InventoryUtil.findHotbarBlock(Blocks.ENDER_CHEST)
            if (obbySlot == -1 && eChestSot == -1) {
                this.toggle()
            }

            this.offHand = mc.player!!.offHandStack.item is BlockItem && (mc.player!!.offHandStack.item as BlockItem).block == Blocks.OBSIDIAN
            this.didPlace = false
            this.extenders = 1
            this.placements = 0
            this.obbySlot = InventoryUtil.findHotbarBlock(Blocks.OBSIDIAN)
            val echestSlot = InventoryUtil.findHotbarBlock(Blocks.ENDER_CHEST)
            if (this.isOff()) {
                return true
            } else {
                if (this.retryTimer.passedMs(2500L)) {
                    this.retries.clear()
                    this.retryTimer.reset()
                }

                if (this.obbySlot == -1 && !this.offHand && echestSlot == -1) {
                    Command.sendMessage("<" + this.getDisplayName() + "> " + Formatting.RED + "No Obsidian in hotbar disabling...")
                    this.disable()
                    return true
                } else {
                    this.isSneaking = EntityUtil.stopSneaking(this.isSneaking)
                    if (mc.player!!.inventory.selectedSlot != this.lastHotbarSlot
                        && mc.player!!.inventory.selectedSlot != this.obbySlot
                        && mc.player!!.inventory.selectedSlot != echestSlot
                    ) {
                        this.lastHotbarSlot = mc.player!!.inventory.selectedSlot
                    }

                    if (!this.startPos!!.equals(EntityUtil.getRoundedBlockPos(mc.player!!))) {
                        this.disable()
                        return true
                    } else {
                        return !this.timer.passedMs(this.delay.getValue().toLong())
                    }
                }
            }
        }
    }

    private fun placeBlock(pos: BlockPos) {
        if (this.placements < this.blocksPerTick.getValue()) {
            val originalSlot = mc.player!!.inventory.selectedSlot
            val obbySlot = InventoryUtil.findHotbarBlock(Blocks.OBSIDIAN)
            val eChestSot = InventoryUtil.findHotbarBlock(Blocks.ENDER_CHEST)
            if (obbySlot == -1 && eChestSot == -1) {
                this.toggle()
            }

            mc.player!!.inventory.selectedSlot = if (obbySlot == -1) eChestSot else obbySlot
            mc.player!!.networkHandler.sendPacket(UpdateSelectedSlotC2SPacket(mc.player!!.inventory.selectedSlot))

            for (blockPos in arrayOf(pos.north(), pos.south(), pos.east(), pos.west(), pos.down(), pos.up())) {
                val block = mc.world!!.getBlockState(blockPos)
                if (block.isAir
                    || !block.fluidState.isEmpty
                    || block.block == Blocks.SHORT_GRASS
                    || block.block == Blocks.FIRE
                    || block.block == Blocks.DEAD_BUSH
                    || block.block == Blocks.SNOW
                ) {
                    this.isSneaking = BlockUtil.placeBlock(
                        pos.down(), if (this.offHand) Hand.OFF_HAND else Hand.MAIN_HAND, this.rotate.getValue(), this.noGhost.getValue(), this.isSneaking
                    )
                }
            }

            this.isSneaking = BlockUtil.placeBlock(
                pos, if (this.offHand) Hand.OFF_HAND else Hand.MAIN_HAND, this.rotate.getValue(), this.noGhost.getValue(), this.isSneaking
            )
            mc.player!!.inventory.selectedSlot = originalSlot
            mc.player!!.networkHandler.sendPacket(UpdateSelectedSlotC2SPacket(originalSlot))
            this.didPlace = true
            ++this.placements
        }
    }
}
