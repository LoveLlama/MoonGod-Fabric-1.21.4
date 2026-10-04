package me.moon.features.modules.player

import me.moon.Moon
import me.moon.features.command.Command
import me.moon.features.modules.Module
import me.moon.features.modules.movement.NoFall
import me.moon.features.setting.Setting
import me.moon.util.BlockUtil
import me.moon.util.EntityUtil
import me.moon.util.InventoryUtil
import net.minecraft.block.Blocks
import net.minecraft.item.Items
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket
import net.minecraft.util.Formatting
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos

class Burrow : Module("Burrow", "XD", Module.Category.PLAYER, true, false, false) {
    private val center = register(Setting("TPCenter", false))
    private val rotate = register(Setting("Rotate", true))
    private var isSneaking = false
    private var startPos: BlockPos? = null
    private var noFall = false

    override fun onDisable() {
        this.isSneaking = EntityUtil.stopSneaking(this.isSneaking)
        if (this.noFall) {
            NoFall.getInstance().enable()
            this.noFall = false
        }
    }

    override fun onEnable() {
        if (NoFall.getInstance().isOn()) {
            NoFall.getInstance().disable()
            this.noFall = true
        }

        this.isSneaking = EntityUtil.stopSneaking(this.isSneaking)
        if (mc.player == null || mc.world == null) {
            this.disable()
            return
        }
        this.startPos = EntityUtil.getRoundedBlockPos(mc.player!!)
        if (this.center.getValue()) {
            Moon.positionManager!!.setPositionPacket(
                this.startPos!!.x + 0.5,
                this.startPos!!.y.toDouble(),
                this.startPos!!.z + 0.5,
                true,
                true,
                true
            )
        }

        if (InventoryUtil.findHotbarBlock(Blocks.OBSIDIAN) == -1) {
            Command.sendMessage("<" + this.getDisplayName() + "> " + Formatting.RED.toString() + "Obsidian ?")
            this.disable()
        } else if (mc.world!!.getBlockState(BlockPos(mc.player!!.blockPos.add(0, 3, 0))).block == Blocks.AIR
            && mc.world!!.getBlockState(BlockPos(mc.player!!.blockPos.add(0, 2, 0))).block == Blocks.AIR
        ) {
            val a = mc.player!!.inventory.selectedSlot
            mc.player!!.inventory.selectedSlot = InventoryUtil.findHotbarBlock(Blocks.OBSIDIAN)
            mc.player!!.networkHandler.sendPacket(UpdateSelectedSlotC2SPacket(mc.player!!.inventory.selectedSlot))
            mc.player!!.networkHandler.sendPacket(PlayerMoveC2SPacket.PositionAndOnGround(mc.player!!.x, mc.player!!.y + 0.41999998688698, mc.player!!.z, true, mc.player!!.horizontalCollision))
            mc.player!!.networkHandler.sendPacket(PlayerMoveC2SPacket.PositionAndOnGround(mc.player!!.x, mc.player!!.y + 0.7531999805211997, mc.player!!.z, true, mc.player!!.horizontalCollision))
            mc.player!!.networkHandler.sendPacket(PlayerMoveC2SPacket.PositionAndOnGround(mc.player!!.x, mc.player!!.y + 1.00133597911214, mc.player!!.z, true, mc.player!!.horizontalCollision))
            mc.player!!.networkHandler.sendPacket(PlayerMoveC2SPacket.PositionAndOnGround(mc.player!!.x, mc.player!!.y + 1.16610926093821, mc.player!!.z, true, mc.player!!.horizontalCollision))
            this.isSneaking = BlockUtil.placeBlock(
                BlockPos(EntityUtil.getPlayerPos(mc.player!!)), Hand.MAIN_HAND, this.rotate.getValue(), true, this.isSneaking
            )
            mc.player!!.inventory.selectedSlot = a
            mc.player!!.networkHandler.sendPacket(UpdateSelectedSlotC2SPacket(a))
            mc.player!!.networkHandler.sendPacket(PlayerMoveC2SPacket.PositionAndOnGround(mc.player!!.x, mc.player!!.y + 3.0, mc.player!!.z, false, mc.player!!.horizontalCollision))
            this.disable()
        } else {
            Command.sendMessage("<" + this.getDisplayName() + "> " + Formatting.RED.toString() + "From above stop you !")
            this.disable()
        }
    }
}
