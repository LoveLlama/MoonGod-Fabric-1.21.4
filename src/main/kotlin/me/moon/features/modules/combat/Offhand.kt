package me.moon.features.modules.combat

import net.minecraft.network.packet.Packet

import me.moon.event.SubscribeEvent
import me.moon.event.events.PacketEvent
import me.moon.event.events.ProcessRightClickBlockEvent
import me.moon.features.modules.Module
import me.moon.features.setting.Setting
import me.moon.util.DamageUtil
import me.moon.util.EntityUtil
import me.moon.util.InventoryUtil
import net.minecraft.block.Blocks
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.decoration.EndCrystalEntity
import net.minecraft.item.BlockItem
import net.minecraft.item.Items
import net.minecraft.item.SwordItem
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import org.lwjgl.glfw.GLFW
import java.util.Queue
import java.util.concurrent.ConcurrentLinkedQueue

class Offhand : Module("Offhand", "Auto switch totem and crystal.", Module.Category.COMBAT, true, false, false) {
    private val crystal = register(Setting("Crystal", true))
    private val crystalHealth = register(Setting("CrystalHealth", 13.0f, 0.1f, 36.0f))
    private val crystalHoleHealth = register(Setting("CrystalHoleHealth", 3.5f, 0.1f, 36.0f))
    private val swordGapple = register(Setting("SwordGapple", true))
    private val crystalCheck = register(Setting("CrystalCheck", true))
    private val armorCheck = register(Setting("ArmorCheck", true))
    private val actions = register(Setting("Packets", 4, 1, 4))

    @JvmField
    var currentMode = Mode2.TOTEMS
    @JvmField
    var totems = 0
    @JvmField
    var crystals = 0
    @JvmField
    var gapples = 0
    private var lastTotemSlot = -1
    private var lastCrystalSlot = -1
    private var lastGappleSlot = -1
    private var lastObbySlot = -1
    private var lastWebSlot = -1
    @JvmField
    var holdingCrystal = false
    @JvmField
    var holdingGapple = false
    @JvmField
    var didSwitchThisTick = false
    private var second = false
    private val taskList: Queue<InventoryUtil.Task> = ConcurrentLinkedQueue()

    private fun setInstance() {
        INSTANCE = this
    }

    init {
        setInstance()
    }

    @SubscribeEvent
    fun onUpdateWalkingPlayer(event: ProcessRightClickBlockEvent) {
        if (event.hand == Hand.MAIN_HAND && event.stack.item == Items.END_CRYSTAL && this.currentMode == Mode2.CRYSTALS) {
            this.putItemInOffhand(this.lastCrystalSlot)
        }
    }

    override fun onUpdate() {
        if (!fullNullCheck()) {
            this.totems = 0
            this.crystals = 0
            this.gapples = 0
            this.lastTotemSlot = -1
            this.lastCrystalSlot = -1
            this.lastGappleSlot = -1
            this.lastObbySlot = -1
            this.lastWebSlot = -1
            this.didSwitchThisTick = false

            for (i in mc.player!!.inventory.main.indices) {
                this.getSlot(i, mc.player!!.inventory.main[i])
            }

            for (i in mc.player!!.inventory.offHand.indices) {
                this.getSlot(i, mc.player!!.inventory.offHand[i])
            }

            this.holdingCrystal = mc.player!!.offHandStack.item == Items.END_CRYSTAL
            this.holdingGapple = mc.player!!.offHandStack.item == Items.ENCHANTED_GOLDEN_APPLE
            if (this.second) {
                this.second = false
            } else {
                if (this.currentMode == Mode2.TOTEMS) {
                    if (this.holdingCrystal) {
                        this.putItemInOffhand(this.lastTotemSlot)
                        this.second = true
                    }

                    if (this.holdingGapple) {
                        this.putItemInOffhand(this.lastTotemSlot)
                        this.second = true
                    }
                }

                if (this.currentMode == Mode2.CRYSTALS) {
                    if (this.holdingGapple) {
                        this.putItemInOffhand(this.lastCrystalSlot)
                        this.second = true
                    }

                    if (mc.player!!.offHandStack.item == Items.TOTEM_OF_UNDYING) {
                        if (this.crystalCheck.getValue() && !EntityUtil.isSafe(mc.player!!, 0, false)) {
                            var health = this.crystalHealth.getValue()
                            if (EntityUtil.isInHole(mc.player!!)) {
                                health = this.crystalHoleHealth.getValue()
                            }

                            if (mc.player!!.health + mc.player!!.absorptionAmount <= health) {
                                return
                            }
                        }

                        this.putItemInOffhand(this.lastCrystalSlot)
                        this.second = true
                    }
                }

                if (this.currentMode == Mode2.GAPPLES && !this.holdingGapple) {
                    this.putItemInOffhand(this.lastGappleSlot)
                    this.second = true
                }
            }

            if (this.currentMode == Mode2.TOTEMS
                && this.holdingCrystal
                && mc.player!!.offHandStack.item == Items.END_CRYSTAL
                && this.armorCheck.getValue()
                && mc.player!!.getEquippedStack(EquipmentSlot.CHEST).item == Items.AIR
            ) {
                this.putItemInOffhand(this.lastTotemSlot)
            }

            if (this.swordGapple.getValue()
                && this.currentMode == Mode2.TOTEMS
                && this.holdingCrystal
                && mc.player!!.offHandStack.item == Items.END_CRYSTAL
                && mc.player!!.mainHandStack.item is SwordItem
            ) {
                this.putItemInOffhand(this.lastGappleSlot)
            }

            mc.options.useKey.setPressed(GLFW.glfwGetMouseButton(mc.window.handle, 1) == GLFW.GLFW_PRESS)
        }
    }

    @SubscribeEvent
    fun onPacketSend(event: PacketEvent.Send) {
        if (fullNullCheck()) {
            return
        }
        if (event.getPacket<Packet<*>>() is PlayerInteractItemC2SPacket) {
            val packet = event.getPacket<PlayerInteractItemC2SPacket>()
            if (packet.hand == Hand.MAIN_HAND && this.currentMode == Mode2.CRYSTALS && this.holdingCrystal) {
                this.putItemInOffhand(this.lastCrystalSlot)
            }
        }
    }

    private fun getSlot(i: Int, stack: net.minecraft.item.ItemStack) {
        if (!stack.isEmpty) {
            val item = stack.item
            if (item == Items.TOTEM_OF_UNDYING) {
                ++this.totems
                this.lastTotemSlot = i
            }

            if (item == Items.END_CRYSTAL) {
                ++this.crystals
                this.lastCrystalSlot = i
            }

            if (item == Items.ENCHANTED_GOLDEN_APPLE) {
                ++this.gapples
                this.lastGappleSlot = i
            }

            if (item is BlockItem && item.block == Blocks.OBSIDIAN) {
                this.lastObbySlot = i
            }

            if (item is BlockItem && item.block == Blocks.COBWEB) {
                this.lastWebSlot = i
            }
        }
    }

    private fun putItemInOffhand(slot: Int) {
        if (slot != -1 && mc.player!!.offHandStack.item != Items.TOTEM_OF_UNDYING) {
            if (this.totems > 0) {
                val totemSlot = InventoryUtil.findItemInventorySlot(Items.TOTEM_OF_UNDYING, false)
                if (totemSlot != -1) {
                    this.taskList.add(InventoryUtil.Task(slot))
                    this.taskList.add(InventoryUtil.Task(45))
                    this.taskList.add(InventoryUtil.Task())
                    this.didSwitchThisTick = true
                }
            }
        }
    }

    override fun getDisplayInfo(): String {
        return if (this.currentMode == Mode2.TOTEMS) {
            "" + this.totems
        } else if (this.currentMode == Mode2.CRYSTALS) {
            "" + this.crystals
        } else {
            "" + this.gapples
        }
    }

    enum class Mode2 {
        TOTEMS,
        CRYSTALS,
        GAPPLES
    }

    companion object {
        @JvmField
        var INSTANCE: Offhand = Offhand()

        @JvmStatic
        fun getInstance(): Offhand = INSTANCE
    }
}
