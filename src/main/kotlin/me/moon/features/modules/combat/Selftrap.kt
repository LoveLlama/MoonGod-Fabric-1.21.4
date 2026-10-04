package me.moon.features.modules.combat

import me.moon.event.SubscribeEvent
import me.moon.event.events.UpdateWalkingPlayerEvent
import me.moon.features.modules.Module
import me.moon.features.setting.Setting
import me.moon.util.BlockUtil
import me.moon.util.EntityUtil
import me.moon.util.InventoryUtil
import me.moon.util.Timer
import net.minecraft.block.Blocks
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos
import java.util.Comparator

class Selftrap : Module("Selftrap", "Lure your enemies in!", Module.Category.COMBAT, true, false, true) {
    private val blocksPerTick = register(Setting("BlocksPerTick", 8, 1, 20))
    private val delay = register(Setting("Delay", 50, 0, 250))
    private val rotate = register(Setting("Rotate", true))
    private val disableTime = register(Setting("DisableTime", 200, 50, 1000))
    private val disable = register(Setting("AutoDisable", true))
    private val packet = register(Setting("PacketPlace", false))
    private val offTimer = Timer()
    private val timer = Timer()
    private val retries = HashMap<BlockPos, Int>()
    private val retryTimer = Timer()
    private var blocksThisTick = 0
    private var isSneaking = false
    private var hasOffhand = false

    override fun onEnable() {
        if (fullNullCheck()) {
            this.disable()
        }

        this.offTimer.reset()
    }

    override fun onTick() {
        if (this.isOn() && (this.blocksPerTick.getValue() != 1 || !this.rotate.getValue())) {
            this.doHoleFill()
        }
    }

    @SubscribeEvent
    fun onUpdateWalkingPlayer(event: UpdateWalkingPlayerEvent) {
        if (this.isOn() && event.stage == 0 && this.blocksPerTick.getValue() == 1 && this.rotate.getValue()) {
            this.doHoleFill()
        }
    }

    override fun onDisable() {
        this.isSneaking = EntityUtil.stopSneaking(this.isSneaking)
        this.retries.clear()
        this.hasOffhand = false
    }

    private fun doHoleFill() {
        if (!this.check()) {
            for (position in this.getPositions()) {
                val placeability = BlockUtil.isPositionPlaceable(position, false)
                if (placeability == 1 && (this.retries[position] == null || this.retries[position]!! < 4)) {
                    this.placeBlock(position)
                    this.retries[position] = if (this.retries[position] == null) 1 else this.retries[position]!! + 1
                }

                if (placeability == 3) {
                    this.placeBlock(position)
                }
            }
        }
    }

    private fun getPositions(): List<BlockPos> {
        val positions = ArrayList<BlockPos>()
        positions.add(BlockPos.ofFloored(mc.player!!.x, mc.player!!.y + 2.0, mc.player!!.z))
        val placeability = BlockUtil.isPositionPlaceable(positions[0], false)
        when (placeability) {
            0 -> return ArrayList()
            1 -> {
                if (BlockUtil.isPositionPlaceable(positions[0], false, false) == 3) {
                    return positions
                }
            }
            2 -> {
                positions.add(BlockPos.ofFloored(mc.player!!.x + 1.0, mc.player!!.y + 1.0, mc.player!!.z))
                positions.add(BlockPos.ofFloored(mc.player!!.x + 1.0, mc.player!!.y + 2.0, mc.player!!.z))
            }
            3 -> return positions
        }
        positions.sortWith(Comparator.comparingInt { pos -> pos.y })
        return positions
    }

    private fun placeBlock(pos: BlockPos) {
        if (this.blocksThisTick < this.blocksPerTick.getValue()) {
            val smartRotate = this.blocksPerTick.getValue() == 1 && this.rotate.getValue()
            val originalSlot = mc.player!!.inventory.selectedSlot
            val obbySlot = InventoryUtil.findHotbarBlock(Blocks.OBSIDIAN)
            val eChestSot = InventoryUtil.findHotbarBlock(Blocks.ENDER_CHEST)
            if (obbySlot == -1 && eChestSot == -1) {
                this.toggle()
            }

            mc.player!!.inventory.selectedSlot = if (obbySlot == -1) eChestSot else obbySlot
            mc.player!!.networkHandler.sendPacket(UpdateSelectedSlotC2SPacket(mc.player!!.inventory.selectedSlot))
            this.isSneaking = if (smartRotate) {
                BlockUtil.placeBlockSmartRotate(pos, if (this.hasOffhand) Hand.OFF_HAND else Hand.MAIN_HAND, true, this.packet.getValue(), this.isSneaking)
            } else {
                BlockUtil.placeBlock(pos, if (this.hasOffhand) Hand.OFF_HAND else Hand.MAIN_HAND, this.rotate.getValue(), this.packet.getValue(), this.isSneaking)
            }
            mc.player!!.inventory.selectedSlot = originalSlot
            mc.player!!.networkHandler.sendPacket(UpdateSelectedSlotC2SPacket(originalSlot))
            this.timer.reset()
            ++this.blocksThisTick
        }
    }

    private fun check(): Boolean {
        if (fullNullCheck()) {
            this.disable()
            return true
        } else {
            val obbySlot = InventoryUtil.findHotbarBlock(Blocks.OBSIDIAN)
            val eChestSot = InventoryUtil.findHotbarBlock(Blocks.ENDER_CHEST)
            if (obbySlot == -1 && eChestSot == -1) {
                this.toggle()
            }

            this.blocksThisTick = 0
            this.isSneaking = EntityUtil.stopSneaking(this.isSneaking)
            if (this.retryTimer.passedMs(2000L)) {
                this.retries.clear()
                this.retryTimer.reset()
            }

            if (!EntityUtil.isSafe(mc.player!!, 0, false)) {
                this.offTimer.reset()
                return true
            } else if (this.disable.getValue() && this.offTimer.passedMs(this.disableTime.getValue().toLong())) {
                this.disable()
                return true
            } else {
                return !this.timer.passedMs(this.delay.getValue().toLong())
            }
        }
    }
}
