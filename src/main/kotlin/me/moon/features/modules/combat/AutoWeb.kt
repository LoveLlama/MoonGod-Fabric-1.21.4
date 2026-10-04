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

class AutoWeb : Module("AutoWeb", "Traps other players in webs", Module.Category.COMBAT, true, false, false) {
    private val delay = register(Setting("Delay", 50, 0, 250))
    private val blocksPerPlace = register(Setting("BlocksPerTick", 8, 1, 30))
    private val packet = register(Setting("PacketPlace", false))
    private val disable = register(Setting("AutoDisable", false))
    private val toggle = register(Setting("AutoToggle", false))
    private val rotate = register(Setting("Rotate", true))
    private val raytrace = register(Setting("Raytrace", false))
    private val lowerbody = register(Setting("Feet", true))
    private val upperBody = register(Setting("Face", false))
    private val timer = Timer()
    @JvmField
    var target: PlayerEntity? = null
    private var didPlace = false
    private var isSneaking = false
    private var placements = 0
    private var smartRotate = false
    private var startPos: BlockPos? = null

    override fun onEnable() {
        if (!fullNullCheck()) {
            this.startPos = EntityUtil.getRoundedBlockPos(mc.player!!)
        }
    }

    override fun onTick() {
        if (InventoryUtil.findHotbarBlock(Blocks.COBWEB) == -1) {
            Command.sendMessage("<" + this.getDisplayName() + "> " + Formatting.RED + "Web ?")
            this.disable()
        } else {
            this.smartRotate = false
            this.doTrap()
            if (this.toggle.getValue()) {
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
            this.doWebTrap()
            if (this.didPlace) {
                this.timer.reset()
            }
        }
    }

    private fun doWebTrap() {
        val placeTargets = this.getPlacements()
        val a = mc.player!!.inventory.selectedSlot
        mc.player!!.inventory.selectedSlot = InventoryUtil.findHotbarBlock(Blocks.COBWEB)
        mc.player!!.networkHandler.sendPacket(UpdateSelectedSlotC2SPacket(mc.player!!.inventory.selectedSlot))
        this.placeList(placeTargets)
        mc.player!!.inventory.selectedSlot = a
        mc.player!!.networkHandler.sendPacket(UpdateSelectedSlotC2SPacket(a))
    }

    private fun getPlacements(): MutableList<Vec3d> {
        val list = ArrayList<Vec3d>()
        val baseVec = this.target!!.pos
        if (this.lowerbody.getValue()) {
            list.add(baseVec)
        }

        if (this.upperBody.getValue()) {
            list.add(baseVec.add(0.0, 1.0, 0.0))
        }

        return list
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
            if (placeability == 3 || placeability == 1) {
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

        if (this.isOff()) {
            return true
        } else if (this.disable.getValue() && !this.startPos!!.equals(EntityUtil.getRoundedBlockPos(mc.player!!))) {
            this.disable()
            return true
        } else {
            this.isSneaking = EntityUtil.stopSneaking(this.isSneaking)
            this.target = this.getTarget(10.0)
            return this.target == null || !this.timer.passedMs(this.delay.getValue().toLong())
        }
    }

    private fun getTarget(range: Double): PlayerEntity? {
        var target: PlayerEntity? = null
        var distance = Math.pow(range, 2.0) + 1.0

        for (player in mc.world!!.players) {
            val inWeb = mc.world!!.getBlockState(BlockPos.ofFloored(player.pos)).block == Blocks.COBWEB
            if (!EntityUtil.isntValid(player, range) && !inWeb && !(Moon.speedManager!!.getPlayerSpeed(player) > 30.0)) {
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
        if (this.placements < this.blocksPerPlace.getValue() && mc.player!!.squaredDistanceTo(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble()) <= MathUtil.square(6.0)) {
            this.isSneaking = if (this.smartRotate) {
                BlockUtil.placeBlockSmartRotate(pos, Hand.MAIN_HAND, true, this.packet.getValue(), this.isSneaking)
            } else {
                BlockUtil.placeBlock(pos, Hand.MAIN_HAND, this.rotate.getValue(), this.packet.getValue(), this.isSneaking)
            }
            this.didPlace = true
            ++this.placements
        }
    }
}
