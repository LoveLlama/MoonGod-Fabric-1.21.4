package me.moon.features.modules.combat

import me.moon.features.command.Command
import me.moon.features.modules.Module
import me.moon.features.modules.player.Anti32k
import me.moon.features.setting.Setting
import me.moon.util.BlockUtil
import me.moon.util.EntityUtil
import me.moon.util.InventoryUtil
import me.moon.util.TestUtil
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.client.gui.screen.ingame.HopperScreen
import net.minecraft.entity.Entity
import net.minecraft.item.Items
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket
import net.minecraft.util.Hand
import net.minecraft.util.Formatting
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import java.util.function.Predicate

class Auto32k : Module("Auto32k", "404 is God", Module.Category.COMBAT, true, false, false) {
    private val itemSlot = register(Setting("ItemSlot", 8, 0, 8))
    var lookMode = register(Setting("LookMode", false))
    var smartHitXIN = register(Setting("SmartHit-XIN", false))
    @JvmField
    var placePos: BlockPos? = null

    init {
        setInstance()
    }

    private fun setInstance() {
        INSTANCE = this
    }

    override fun onUpdate() {
        if (!fullNullCheck()) {
            if (Anti32k.getInstance().isOn()) {
                Command.sendMessage("<" + this.getDisplayName() + "> " + Formatting.RED + "Oh no Anti32k")
                this.disable()
            } else {
                if (Killaura.getInstance().isOff()) {
                    Killaura.getInstance().enable()
                }

                if (!this.lookMode.getValue() && Killaura.getInstance().isOn() && Killaura.getInstance().target != null) {
                    for (x in -5..5) {
                        for (y in -3 until 0) {
                            for (z in -5..5) {
                                val autoPos = EntityUtil.getPlayerPos(mc.player!!).add(x, y, z)
                                if (!TestUtil.emptyBlocks.contains(mc.world!!.getBlockState(autoPos).block)
                                    && TestUtil.emptyBlocks.contains(mc.world!!.getBlockState(autoPos.add(0, 1, 0)).block)
                                    && TestUtil.emptyBlocks.contains(mc.world!!.getBlockState(autoPos.add(0, 2, 0)).block)
                                    && mc.world!!.getEntitiesByClass(Entity::class.java, Box(autoPos.add(0, 1, 0))) { true }.isEmpty()
                                    && mc.world!!.getEntitiesByClass(Entity::class.java, Box(autoPos.add(0, 2, 0))) { true }.isEmpty()
                                    && !(BlockUtil.getDistance32k(Killaura.getInstance().target!!, autoPos.add(0, 1, 0)) <= 6.0)
                                    && !(BlockUtil.getDistance32k(Killaura.getInstance().target!!, autoPos.add(0, 2, 0)) <= 6.0)
                                    && !(BlockUtil.getDistance32k(mc.player!!, autoPos) > 4.5)
                                    && !(BlockUtil.getDistance32k(mc.player!!, autoPos.add(0, 1, 0)) > 4.5)
                                    && !(BlockUtil.getDistance32k(mc.player!!, autoPos.add(0, 2, 0)) > 4.5)
                                ) {
                                    this.placePos = autoPos
                                }
                            }
                        }
                    }
                }

                if (mc.currentScreen is HopperScreen) {
                    var swordIndex = -1

                    for (i in 0..4) {
                        if (mc.player!!.currentScreenHandler.slots[i].stack.item == Items.DIAMOND_SWORD) {
                            swordIndex = i
                            break
                        }
                    }

                    if (swordIndex == -1) {
                        return
                    }

                    if (mc.player!!.mainHandStack.item != Items.DIAMOND_SWORD) {
                        mc.interactionManager!!?.clickSlot(mc.player!!.currentScreenHandler.syncId, swordIndex, this.itemSlot.getValue(), SlotActionType.SWAP, mc.player!!)
                        mc.player!!.inventory.selectedSlot = this.itemSlot.getValue()
                    }
                }
            }
        }
    }

    override fun onEnable() {
        if (!fullNullCheck()) {
            if (InventoryUtil.findHotbarBlock(Blocks.HOPPER) != -1 && InventoryUtil.findHotbarBlock(Blocks.SHULKER_BOX) != -1) {
                if (this.lookMode.getValue()) {
                    val cross = mc.crosshairTarget
                    val rayPos: BlockPos = if (cross is net.minecraft.util.hit.BlockHitResult) cross.blockPos else EntityUtil.getPlayerPos(mc.player!!)
                    if (TestUtil.emptyBlocks.contains(mc.world!!.getBlockState(rayPos).block)
                        || !TestUtil.emptyBlocks.contains(mc.world!!.getBlockState(rayPos.add(0, 1, 0)).block)
                        || !TestUtil.emptyBlocks.contains(mc.world!!.getBlockState(rayPos.add(0, 2, 0)).block)
                        || !mc.world!!.getEntitiesByClass(Entity::class.java, Box(rayPos.add(0, 1, 0))) { true }.isEmpty()
                        || !mc.world!!.getEntitiesByClass(Entity::class.java, Box(rayPos.add(0, 2, 0))) { true }.isEmpty()
                    ) {
                        return
                    }

                    this.placePos = rayPos
                }

                if (this.placePos != null) {
                    this.placeBlock(this.placePos!!.add(0, 1, 0), InventoryUtil.findHotbarBlock(Blocks.HOPPER))
                    this.placeBlock(this.placePos!!.add(0, 2, 0), InventoryUtil.findHotbarBlock(Blocks.SHULKER_BOX))
                    this.openBlock(this.placePos!!.add(0, 1, 0))
                    this.placePos = null
                }
            }
        }
    }

    private fun placeBlock(pos: BlockPos, slot: Int) {
        if (TestUtil.emptyBlocks.contains(mc.world!!.getBlockState(pos).block)) {
            if (slot != mc.player!!.inventory.selectedSlot) {
                mc.player!!.inventory.selectedSlot = slot
            }

            for (f in Direction.values()) {
                val neighborBlock = mc.world!!.getBlockState(pos.offset(f)).block
                if (!TestUtil.emptyBlocks.contains(neighborBlock)) {
                    mc.player!!.networkHandler.sendPacket(ClientCommandC2SPacket(mc.player!!, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY))
                    mc.player!!.networkHandler.sendPacket(
                        PlayerInteractBlockC2SPacket(
                            Hand.MAIN_HAND,
                            BlockHitResult(Vec3d(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble()), f.opposite, pos.offset(f), false),
                            0
                        )
                    )
                    mc.player!!.networkHandler.sendPacket(ClientCommandC2SPacket(mc.player!!, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY))
                }
            }
        }
    }

    private fun openBlock(pos: BlockPos) {
        for (f in Direction.values()) {
            val neighborBlock = mc.world!!.getBlockState(pos.offset(f)).block
            if (TestUtil.emptyBlocks.contains(neighborBlock)) {
                mc.player!!.networkHandler.sendPacket(
                    PlayerInteractBlockC2SPacket(
                        Hand.MAIN_HAND,
                        BlockHitResult(Vec3d(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble()), f.opposite, pos, false),
                        0
                    )
                )
            }
        }
    }

    companion object {
        @JvmField
        var INSTANCE: Auto32k = Auto32k()

        @JvmStatic
        fun getInstance(): Auto32k = INSTANCE
    }
}
