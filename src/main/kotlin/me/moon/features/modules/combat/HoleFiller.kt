package me.moon.features.modules.combat

import me.moon.features.command.Command
import me.moon.features.modules.Module
import me.moon.features.setting.Setting
import me.moon.util.EntityUtil
import me.moon.util.InventoryUtil
import me.moon.util.TestUtil
import me.moon.util.Timer
import net.minecraft.block.Blocks
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.util.Formatting
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import java.util.function.Predicate

class HoleFiller : Module("HoleFiller", "Fills holes around you.", Module.Category.COMBAT, true, false, true) {
    private val range = register(Setting("PlaceRange", 8, 0, 10))
    private val delay = register(Setting("Delay", 50, 0, 250))
    private val blocksPerTick = register(Setting("BlocksPerTick", 20, 8, 30))
    private val offTimer = Timer()
    private val timer = Timer()
    private val retries = HashMap<BlockPos, Int>()
    private val retryTimer = Timer()
    private var blocksThisTick = 0
    private var holes = ArrayList<BlockPos>()

    init {
        setInstance()
    }

    private fun setInstance() {
        INSTANCE = this
    }

    override fun onEnable() {
        if (fullNullCheck()) {
            this.disable()
        }

        this.offTimer.reset()
    }

    override fun onTick() {
        if (this.isOn()) {
            this.doHoleFill()
        }
    }

    override fun onDisable() {
        this.retries.clear()
    }

    private fun doHoleFill() {
        if (!this.check()) {
            this.holes = ArrayList()
            val center = EntityUtil.getRoundedBlockPos(mc.player!!)
            for (pos in BlockPos.iterate(
                center.add(-this.range.getValue(), -this.range.getValue(), -this.range.getValue()),
                center.add(this.range.getValue(), this.range.getValue(), this.range.getValue())
            )) {
                val state = mc.world!!.getBlockState(pos)
                val stateUp = mc.world!!.getBlockState(pos.up())
                if (!state.isSolidBlock(mc.world!!, pos) && !stateUp.isSolidBlock(mc.world!!, pos.up())) {
                    val solidNeighbours = (mc.world!!.getBlockState(pos.east()).block == Blocks.BEDROCK
                        || mc.world!!.getBlockState(pos.east()).block == Blocks.OBSIDIAN)
                        && (mc.world!!.getBlockState(pos.north()).block == Blocks.BEDROCK
                        || mc.world!!.getBlockState(pos.north()).block == Blocks.OBSIDIAN)
                        && (mc.world!!.getBlockState(pos.west()).block == Blocks.BEDROCK
                        || mc.world!!.getBlockState(pos.west()).block == Blocks.OBSIDIAN)
                        && (mc.world!!.getBlockState(pos.south()).block == Blocks.BEDROCK
                        || mc.world!!.getBlockState(pos.south()).block == Blocks.OBSIDIAN)
                        && mc.world!!.getBlockState(pos).isAir
                        && mc.world!!.getBlockState(pos.up()).isAir
                        && mc.world!!.getBlockState(pos.up(2)).isAir
                    if (solidNeighbours) {
                        this.holes.add(pos)
                    }
                }
            }

            this.holes.forEach { this.placeBlock(it) }
            this.toggle()
        }
    }

    private fun placeBlock(pos: BlockPos) {
        for (entity in mc.world!!.getEntitiesByClass(Entity::class.java, Box(pos)) { true }) {
            if (entity is LivingEntity) {
                return
            }
        }

        if (this.blocksThisTick < this.blocksPerTick.getValue()) {
            val obbySlot = InventoryUtil.findHotbarBlock(Blocks.OBSIDIAN)
            val eChestSot = InventoryUtil.findHotbarBlock(Blocks.ENDER_CHEST)
            if (obbySlot == -1 && eChestSot == -1) {
                this.toggle()
            }

            val originalSlot = mc.player!!.inventory.selectedSlot
            mc.player!!.inventory.selectedSlot = if (obbySlot == -1) eChestSot else obbySlot
            mc.player!!.networkHandler.sendPacket(net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket(mc.player!!.inventory.selectedSlot))
            TestUtil.placeBlock(pos)
            if (mc.player!!.inventory.selectedSlot != originalSlot) {
                mc.player!!.inventory.selectedSlot = originalSlot
                mc.player!!.networkHandler.sendPacket(net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket(originalSlot))
            }

            this.timer.reset()
            ++this.blocksThisTick
        }
    }

    private fun check(): Boolean {
        if (fullNullCheck()) {
            this.disable()
            return true
        } else if (InventoryUtil.findHotbarBlock(Blocks.OBSIDIAN) == -1 && InventoryUtil.findHotbarBlock(Blocks.ENDER_CHEST) == -1) {
            Command.sendMessage("<" + this.getDisplayName() + "> " + Formatting.RED + "Obsidian ? EnderChest ?")
            this.disable()
            return true
        } else {
            this.blocksThisTick = 0
            if (this.retryTimer.passedMs(2000L)) {
                this.retries.clear()
                this.retryTimer.reset()
            }

            return !this.timer.passedMs(this.delay.getValue().toLong())
        }
    }

    companion object {
        @JvmField
        var INSTANCE: HoleFiller = HoleFiller()

        @JvmStatic
        fun getInstance(): HoleFiller = INSTANCE
    }
}
