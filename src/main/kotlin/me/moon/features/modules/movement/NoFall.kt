package me.moon.features.modules.movement

import net.minecraft.network.packet.Packet

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import me.moon.event.SubscribeEvent
import me.moon.event.events.PacketEvent
import me.moon.features.command.Command
import me.moon.features.modules.Module
import me.moon.features.setting.Setting
import me.moon.util.EntityUtil
import me.moon.util.InventoryUtil
import me.moon.util.Timer
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.EquipmentSlot
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.util.Hand
import net.minecraft.util.hit.HitResult
import net.minecraft.world.RaycastContext
import java.util.function.Predicate

class NoFall : Module("NoFall", "Prevents fall damage.", Module.Category.MOVEMENT, true, false, false) {
    private val mode = register(Setting("Mode", Mode.PACKET))
    private val distance = register(Setting("Distance", 15, 0, 50, Predicate { mode.getValue() == Mode.BUCKET || mode.getValue() == Mode.PACKET }))
    private val glide = register(Setting("Glide", false, Predicate { mode.getValue() == Mode.ELYTRA }))
    private val silent = register(Setting("Silent", true, Predicate { mode.getValue() == Mode.ELYTRA }))
    private val bypass = register(Setting("Bypass", false, Predicate { mode.getValue() == Mode.ELYTRA }))
    private val timer = Timer()
    private var equipped = false
    private var gotElytra = false
    private var currentState = State.FALL_CHECK

    init { setInstance() }
    private fun setInstance() { INSTANCE = this }

    override fun onEnable() {
        ogslot = -1
        this.currentState = State.FALL_CHECK
    }

    private fun rayTrace(dy: Double): Boolean {
        val start = mc.player!!.pos
        val result = mc.world!!.raycast(RaycastContext(start, start.add(0.0, dy, 0.0), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.ANY, mc.player!!))
        return result != null && result.type == HitResult.Type.BLOCK
    }

    @SubscribeEvent
    fun onPacketSend(event: PacketEvent.Send) {
        if (!fullNullCheck()) {
            if (this.mode.getValue() == Mode.ELYTRA) {
                if (this.bypass.getValue()) {
                    this.currentState = this.currentState.onSend(event)
                } else if (!this.equipped && event.getPacket<Packet<*>>() is PlayerMoveC2SPacket && mc.player!!.fallDistance >= 3.0f) {
                    if (this.glide.getValue() || rayTrace(-3.0)) {
                        if (mc.player!!.getEquippedStack(EquipmentSlot.CHEST).item == Items.ELYTRA) {
                            mc.player!!.networkHandler.sendPacket(ClientCommandC2SPacket(mc.player!!, ClientCommandC2SPacket.Mode.START_FALL_FLYING))
                        } else if (this.silent.getValue()) {
                            val slot = InventoryUtil.getItemHotbar(Items.ELYTRA)
                            if (slot != -1) {
                                mc.interactionManager!!.clickSlot(mc.player!!.currentScreenHandler.syncId, 6, slot, SlotActionType.SWAP, mc.player!!)
                                mc.player!!.networkHandler.sendPacket(ClientCommandC2SPacket(mc.player!!, ClientCommandC2SPacket.Mode.START_FALL_FLYING))
                            }
                            ogslot = slot
                            this.equipped = true
                        }
                    }
                }
            }

            if (this.mode.getValue() == Mode.PACKET && event.getPacket<Packet<*>>() is PlayerMoveC2SPacket && !mc.player!!.isOnGround) {
                if (mc.player!!.getEquippedStack(EquipmentSlot.CHEST).item == Items.ELYTRA) {
                    return
                }
                val packet = event.getPacket<PlayerMoveC2SPacket>()
                if (mc.player!!.fallDistance >= this.distance.getValue().toFloat() && !packet.isOnGround()) {
                    event.isCanceled = true
                    when (packet) {
                        is PlayerMoveC2SPacket.PositionAndOnGround -> mc.player!!.networkHandler.sendPacket(PlayerMoveC2SPacket.PositionAndOnGround(mc.player!!.x, mc.player!!.y, mc.player!!.z, true, packet.horizontalCollision()))
                        is PlayerMoveC2SPacket.Full -> mc.player!!.networkHandler.sendPacket(PlayerMoveC2SPacket.Full(mc.player!!.x, mc.player!!.y, mc.player!!.z, mc.player!!.yaw, mc.player!!.pitch, true, packet.horizontalCollision()))
                        is PlayerMoveC2SPacket.LookAndOnGround -> mc.player!!.networkHandler.sendPacket(PlayerMoveC2SPacket.LookAndOnGround(mc.player!!.yaw, mc.player!!.pitch, true, packet.horizontalCollision()))
                        is PlayerMoveC2SPacket.OnGroundOnly -> mc.player!!.networkHandler.sendPacket(PlayerMoveC2SPacket.OnGroundOnly(true, packet.horizontalCollision()))
                        else -> {}
                    }
                }
            }
        }
    }

    @SubscribeEvent
    fun onPacketReceive(event: PacketEvent.Receive) {
        if (!fullNullCheck()) {
            if ((this.equipped || this.bypass.getValue())
                && this.mode.getValue() == Mode.ELYTRA
                && (event.getPacket<Packet<*>>() is InventoryS2CPacket || event.getPacket<Packet<*>>() is ScreenHandlerSlotUpdateS2CPacket)
            ) {
                if (this.bypass.getValue()) {
                    this.currentState = this.currentState.onReceive(event)
                } else {
                    this.gotElytra = true
                }
            }
        }
    }

    override fun onUpdate() {
        if (!fullNullCheck()) {
            if (this.mode.getValue() == Mode.ELYTRA) {
                if (this.bypass.getValue()) {
                    this.currentState = this.currentState.onUpdate()
                } else if (this.silent.getValue() && this.equipped && this.gotElytra) {
                    mc.interactionManager!!.clickSlot(mc.player!!.currentScreenHandler.syncId, 6, ogslot, SlotActionType.SWAP, mc.player!!)
                    this.equipped = false
                    this.gotElytra = false
                } else {
                    val found = InventoryUtil.findStackInventory(Items.ELYTRA)
                    if (this.silent.getValue()
                        && InventoryUtil.getItemHotbar(Items.ELYTRA) == -1
                        && found != -1
                        && ogslot != -1
                    ) {
                        mc.interactionManager!!.clickSlot(mc.player!!.currentScreenHandler.syncId, found, ogslot, SlotActionType.SWAP, mc.player!!)
                    }
                }
            }
        }
    }

    override fun onTick() {
        if (!fullNullCheck()) {
            if (this.mode.getValue() == Mode.BUCKET
                && mc.player!!.fallDistance >= this.distance.getValue().toFloat()
                && !EntityUtil.isAboveWater(mc.player!!)
                && this.timer.passedMs(100L)
                && rayTrace(-5.33)
            ) {
                var hand = Hand.MAIN_HAND
                if (mc.player!!.offHandStack.item == Items.WATER_BUCKET) {
                    hand = Hand.OFF_HAND
                } else if (mc.player!!.mainHandStack.item != Items.WATER_BUCKET) {
                    for (i in 0..8) {
                        if (mc.player!!.inventory.getStack(i).item == Items.WATER_BUCKET) {
                            mc.player!!.inventory.selectedSlot = i
                            mc.player!!.networkHandler.sendPacket(UpdateSelectedSlotC2SPacket(i))
                            mc.player!!.setPitch(90.0f)
                            this.timer.reset()
                            return
                        }
                    }
                    return
                }
                mc.player!!.setPitch(90.0f)
                mc.interactionManager!!.interactItem(mc.player!!, hand)
                this.timer.reset()
            }
        }
    }

    override fun getDisplayInfo(): String = this.mode.getValue().name

    enum class Mode { PACKET, BUCKET, ELYTRA }

    enum class State {
        FALL_CHECK {
            override fun onSend(event: PacketEvent.Send): State {
                val mc = MinecraftClient.getInstance()
                val start = mc.player!!.pos
                val result = mc.world!!.raycast(RaycastContext(start, start.add(0.0, -3.0, 0.0), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.ANY, mc.player!!))
                if (event.getPacket<Packet<*>>() is PlayerMoveC2SPacket
                    && mc.player!!.fallDistance >= 3.0f
                    && result != null
                    && result.type == HitResult.Type.BLOCK
                ) {
                    val slot = InventoryUtil.getItemHotbar(Items.ELYTRA)
                    if (slot != -1) {
                        mc.interactionManager!!.clickSlot(mc.player!!.currentScreenHandler.syncId, 6, slot, SlotActionType.SWAP, mc.player!!)
                        NoFall.ogslot = slot
                        mc.player!!.networkHandler.sendPacket(ClientCommandC2SPacket(mc.player!!, ClientCommandC2SPacket.Mode.START_FALL_FLYING))
                        return WAIT_FOR_ELYTRA_DEQUIP
                    }
                    return this
                }
                return this
            }
        },
        WAIT_FOR_ELYTRA_DEQUIP {
            override fun onReceive(event: PacketEvent.Receive): State {
                return if (event.getPacket<Packet<*>>() is InventoryS2CPacket || event.getPacket<Packet<*>>() is ScreenHandlerSlotUpdateS2CPacket) REEQUIP_ELYTRA else this
            }
        },
        REEQUIP_ELYTRA {
            override fun onUpdate(): State {
                val mc = MinecraftClient.getInstance()
                mc.interactionManager!!.clickSlot(mc.player!!.currentScreenHandler.syncId, 6, NoFall.ogslot, SlotActionType.SWAP, mc.player!!)
                val slot = InventoryUtil.findStackInventory(Items.ELYTRA, true)
                if (slot == -1) {
                    Command.sendMessage("§cElytra not found after regain?")
                    return WAIT_FOR_NEXT_REQUIP
                }
                mc.interactionManager!!.clickSlot(mc.player!!.currentScreenHandler.syncId, slot, NoFall.ogslot, SlotActionType.SWAP, mc.player!!)
                NoFall.bypassTimer.reset()
                return RESET_TIME
            }
        },
        WAIT_FOR_NEXT_REQUIP {
            override fun onUpdate(): State = if (NoFall.bypassTimer.passedMs(250L)) REEQUIP_ELYTRA else this
        },
        RESET_TIME {
            override fun onUpdate(): State {
                val mc = MinecraftClient.getInstance()
                if (!mc.player!!.isOnGround && !NoFall.bypassTimer.passedMs(250L)) {
                    return this
                }
                mc.player!!.networkHandler.sendPacket(ClickSlotC2SPacket(0, 0, 0, 0, SlotActionType.PICKUP, ItemStack(Items.BEDROCK), Int2ObjectOpenHashMap()))
                return FALL_CHECK
            }
        };

        open fun onSend(event: PacketEvent.Send): State = this
        open fun onReceive(event: PacketEvent.Receive): State = this
        open fun onUpdate(): State = this
    }

    companion object {
        private val bypassTimer = Timer()
        private var ogslot = -1

        @JvmField
        var INSTANCE: NoFall = NoFall()

        @JvmStatic
        fun getInstance(): NoFall = INSTANCE
    }
}
