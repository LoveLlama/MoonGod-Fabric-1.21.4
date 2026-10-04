package me.moon.features.modules.combat

import me.moon.Moon
import me.moon.features.command.Command
import me.moon.features.modules.Module
import me.moon.features.setting.Setting
import me.moon.util.BlockUtil
import me.moon.util.EntityUtil
import me.moon.util.InventoryUtil
import me.moon.util.MathUtil
import me.moon.util.Timer
import net.minecraft.block.Blocks
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket
import net.minecraft.util.Formatting
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import java.util.Comparator

class AutoTrap : Module("AutoTrap", "Traps other players", Module.Category.COMBAT, true, false, false) {
    private val delay = register(Setting("Delay", 50, 0, 250))
    private val blocksPerPlace = register(Setting("BlocksPerTick", 8, 1, 30))
    private val togg1e = register(Setting("AutoToggle", false))
    private val rotate = register(Setting("Rotate", true))
    private val smartrotate = register(Setting("SmartRotate", true))
    private val packet = register(Setting("Packet", true))
    private val raytrace = register(Setting("Raytrace", false))
    private val antiScaffold = register(Setting("AntiScaffold", false))
    private val antiStep = register(Setting("AntiStep", false))
    private val timer = Timer()
    private val retries = HashMap<BlockPos, Int>()
    private val retryTimer = Timer()

    @JvmField
    var target: PlayerEntity? = null
    private var didPlace = false
    private var isSneaking = false
    private var lastHotbarSlot = 0
    private var placements = 0
    private var smartRotate = false
    private var startPos: BlockPos? = null

    private fun setInstance() {
        INSTANCE = this
    }

    init {
        setInstance()
    }

    companion object {
        @JvmField
        var INSTANCE: AutoTrap = AutoTrap()

        @JvmStatic
        fun getInstance(): AutoTrap = INSTANCE
    }

    override fun onEnable() {
        if (!fullNullCheck()) {
            this.startPos = EntityUtil.getRoundedBlockPos(mc.player!!)
            this.lastHotbarSlot = mc.player!!.inventory.selectedSlot
            this.retries.clear()
        }
    }

    override fun onTick() {
        if (!fullNullCheck()) {
            this.smartRotate = this.smartrotate.getValue()
            this.doTrap()
            if (this.togg1e.getValue()) {
                this.toggle()
            }
        }
    }

    override fun getDisplayInfo(): String? {
        return this.target?.name?.string
    }

    override fun onDisable() {
        this.isSneaking = EntityUtil.stopSneaking(this.isSneaking)
    }

    private fun doTrap() {
        if (!this.check()) {
            this.doStaticTrap()
            if (this.didPlace) {
                this.timer.reset()
            }
        }
    }

    private fun doStaticTrap() {
        val placeTargets = EntityUtil.targets(
            this.target!!.pos, this.antiScaffold.getValue(), this.antiStep.getValue(), false, false, false, this.raytrace.getValue()
        )
        this.placeList(placeTargets)
    }

    private fun placeList(list: MutableList<Vec3d>) {
        list.sortWith { a, b ->
            java.lang.Double.compare(
                mc.player!!.squaredDistanceTo(b.x, b.y, b.z),
                mc.player!!.squaredDistanceTo(a.x, a.y, a.z)
            )
        }
        list.sortWith(Comparator.comparingDouble { vec3d -> vec3d.y })

        for (vec3d in list) {
            val position = BlockPos.ofFloored(vec3d)
            val placeability = BlockUtil.isPositionPlaceable(position, this.raytrace.getValue())
            if (placeability == 1 && (this.retries[position] == null || this.retries[position]!! < 4)) {
                this.placeBlock(position)
                this.retries[position] = if (this.retries[position] == null) 1 else this.retries[position]!! + 1
                this.retryTimer.reset()
            } else if (placeability == 3) {
                this.placeBlock(position)
            }
        }
    }

    private fun check(): Boolean {
        this.didPlace = false
        this.placements = 0
        if (this.startPos == null) {
            this.startPos = EntityUtil.getRoundedBlockPos(mc.player!!)
        }

        val obbySlot2 = InventoryUtil.findHotbarBlock(Blocks.OBSIDIAN)
        if (obbySlot2 == -1) {
            this.toggle()
        }

        val obbySlot = InventoryUtil.findHotbarBlock(Blocks.OBSIDIAN)
        if (this.isOff()) {
            return true
        } else if (!this.startPos!!.equals(EntityUtil.getRoundedBlockPos(mc.player!!))) {
            this.disable()
            return true
        } else {
            if (this.retryTimer.passedMs(2000L)) {
                this.retries.clear()
                this.retryTimer.reset()
            }

            if (obbySlot == -1) {
                Command.sendMessage("<" + this.getDisplayName() + "> " + Formatting.RED + "No Obsidian in hotbar disabling...")
                this.disable()
                return true
            } else {
                if (mc.player!!.inventory.selectedSlot != this.lastHotbarSlot && mc.player!!.inventory.selectedSlot != obbySlot) {
                    this.lastHotbarSlot = mc.player!!.inventory.selectedSlot
                }

                this.isSneaking = EntityUtil.stopSneaking(this.isSneaking)
                this.target = this.getTarget(10.0, true)
                return this.target == null || !this.timer.passedMs(this.delay.getValue().toLong())
            }
        }
    }

    private fun getTarget(range: Double, trapped: Boolean): PlayerEntity? {
        var target: PlayerEntity? = null
        var distance = Math.pow(range, 2.0) + 1.0

        for (player in mc.world!!.players) {
            if (!EntityUtil.isntValid(player, range)
                && (!trapped || !EntityUtil.isTrapped(player, this.antiScaffold.getValue(), this.antiStep.getValue(), false, false, false))
                && !(Moon.speedManager!!.getPlayerSpeed(player) > 10.0)
            ) {
                if (target == null) {
                    target = player
                    distance = mc.player!!.squaredDistanceTo(player).toDouble()
                } else if (mc.player!!.squaredDistanceTo(player).toDouble() < distance) {
                    target = player
                    distance = mc.player!!.squaredDistanceTo(player).toDouble()
                }
            }
        }

        return target
    }

    private fun placeBlock(pos: BlockPos) {
        if (this.placements < this.blocksPerPlace.getValue() && mc.player!!.squaredDistanceTo(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble()) <= MathUtil.square(5.0)) {
            val originalSlot = mc.player!!.inventory.selectedSlot
            val obbySlot = InventoryUtil.findHotbarBlock(Blocks.OBSIDIAN)
            val eChestSot = InventoryUtil.findHotbarBlock(Blocks.ENDER_CHEST)
            if (obbySlot == -1 && eChestSot == -1) {
                this.toggle()
            }

            if (this.smartRotate) {
                mc.player!!.inventory.selectedSlot = if (obbySlot == -1) eChestSot else obbySlot
                mc.player!!.networkHandler.sendPacket(UpdateSelectedSlotC2SPacket(mc.player!!.inventory.selectedSlot))
                this.isSneaking = BlockUtil.placeBlockSmartRotate(pos, Hand.MAIN_HAND, true, this.packet.getValue(), this.isSneaking)
                mc.player!!.inventory.selectedSlot = originalSlot
                mc.player!!.networkHandler.sendPacket(UpdateSelectedSlotC2SPacket(originalSlot))
            } else {
                mc.player!!.inventory.selectedSlot = if (obbySlot == -1) eChestSot else obbySlot
                mc.player!!.networkHandler.sendPacket(UpdateSelectedSlotC2SPacket(mc.player!!.inventory.selectedSlot))
                this.isSneaking = BlockUtil.placeBlock(pos, Hand.MAIN_HAND, this.rotate.getValue(), this.packet.getValue(), this.isSneaking)
                mc.player!!.inventory.selectedSlot = originalSlot
                mc.player!!.networkHandler.sendPacket(UpdateSelectedSlotC2SPacket(originalSlot))
            }

            this.didPlace = true
            ++this.placements
        }
    }
}
