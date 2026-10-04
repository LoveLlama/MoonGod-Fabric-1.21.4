package me.moon.features.modules.player

import net.minecraft.network.packet.Packet

import me.moon.event.SubscribeEvent
import me.moon.event.events.PacketEvent
import me.moon.event.events.PlayerDamageBlockEvent
import me.moon.event.events.Render3DEvent
import me.moon.features.modules.Module
import me.moon.features.setting.Setting
import me.moon.util.BlockUtil
import me.moon.util.InventoryUtil
import me.moon.util.RenderUtil
import me.moon.util.Timer
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.item.Items
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import java.awt.Color
import java.util.function.Predicate

class MoonBreak : Module("SpeedMine", "MoonBreak", Module.Category.PLAYER, true, false, false) {
    private val breakSuccess = Timer()
    private val creativeMode = register(Setting("CreativeMode", true))
    private val ghostHand = register(Setting("GhostHand", true, Predicate { creativeMode.getValue() }))
    private val render = register(Setting("Render", true))
    private val godBlocks: List<Block> = listOf(Blocks.AIR, Blocks.LAVA, Blocks.WATER, Blocks.BEDROCK)
    private var cancelStart = false
    private var empty = false
    private var facing: Direction = Direction.UP
    private var breakPos: BlockPos? = null

    init { setInstance() }
    private fun setInstance() { INSTANCE = this }

    override fun onUpdate() {
        if (!fullNullCheck()) {
            if (this.creativeMode.getValue() && this.cancelStart && this.breakPos != null && !this.godBlocks.contains(mc.world!!.getBlockState(this.breakPos).block)) {
                if (this.ghostHand.getValue() && InventoryUtil.getItemHotbar(Items.DIAMOND_PICKAXE) != -1) {
                    val slotMain = mc.player!!.inventory.selectedSlot
                    if (mc.world!!.getBlockState(this.breakPos).block == Blocks.OBSIDIAN) {
                        if (this.breakSuccess.passedMs(1234L)) {
                            mc.player!!.inventory.selectedSlot = InventoryUtil.getItemHotbar(Items.DIAMOND_PICKAXE)
                            mc.player!!.networkHandler.sendPacket(net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket(mc.player!!.inventory.selectedSlot))
                            mc.player!!.networkHandler.sendPacket(PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, this.breakPos, this.facing))
                            mc.player!!.inventory.selectedSlot = slotMain
                            mc.player!!.networkHandler.sendPacket(net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket(slotMain))
                        }
                    } else {
                        mc.player!!.inventory.selectedSlot = InventoryUtil.getItemHotbar(Items.DIAMOND_PICKAXE)
                        mc.player!!.networkHandler.sendPacket(net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket(mc.player!!.inventory.selectedSlot))
                        mc.player!!.networkHandler.sendPacket(PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, this.breakPos, this.facing))
                        mc.player!!.inventory.selectedSlot = slotMain
                        mc.player!!.networkHandler.sendPacket(net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket(slotMain))
                    }
                } else {
                    mc.player!!.networkHandler.sendPacket(PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, this.breakPos, this.facing))
                }
            }
        }
    }

    override fun onRender3D(event: Render3DEvent) {
        if (!fullNullCheck()) {
            if (this.render.getValue() && this.creativeMode.getValue() && this.cancelStart && this.breakPos != null) {
                if (this.godBlocks.contains(mc.world!!.getBlockState(this.breakPos).block)) {
                    this.empty = true
                }
                val color = Color(if (this.empty) 0 else 255, if (this.empty) 255 else 0, 0, 255)
                RenderUtil.drawBoxESP(this.breakPos!!, color, false, color, 1.0f, true, true, 84, false)
            } else if (this.cancelStart && this.render.getValue() && this.breakPos != null) {
                RenderUtil.drawBoxESP(this.breakPos!!, Color(255, 255, 255), false, Color(255, 255, 255), 1.0f, true, true, 84, false)
            }
        }
    }

    @SubscribeEvent
    fun onPacketSend(event: PacketEvent.Send) {
        if (!fullNullCheck()) {
            if (event.getPacket<Packet<*>>() is PlayerActionC2SPacket) {
                val packet = event.getPacket<PlayerActionC2SPacket>()
                if (packet.action == PlayerActionC2SPacket.Action.START_DESTROY_BLOCK) {
                    event.isCanceled = this.cancelStart
                }
            }
        }
    }

    @SubscribeEvent
    fun onBlockEvent(event: PlayerDamageBlockEvent) {
        if (!fullNullCheck()) {
            if (BlockUtil.canBreak(event.pos)) {
                this.empty = false
                this.cancelStart = false
                this.breakPos = event.pos
                this.breakSuccess.reset()
                this.facing = event.facing
                if (this.breakPos != null) {
                    mc.player!!.swingHand(Hand.MAIN_HAND)
                    mc.player!!.networkHandler.sendPacket(PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, this.breakPos, this.facing))
                    this.cancelStart = true
                    mc.player!!.networkHandler.sendPacket(PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, this.breakPos, this.facing))
                    event.isCanceled = true
                }
            }
        }
    }

    override fun getDisplayInfo(): String = if (this.ghostHand.getValue()) "Ghost" else "Normal"

    companion object {
        @JvmField
        var INSTANCE: MoonBreak = MoonBreak()

        @JvmStatic
        fun getInstance(): MoonBreak = INSTANCE
    }
}
