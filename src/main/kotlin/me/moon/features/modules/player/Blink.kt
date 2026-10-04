package me.moon.features.modules.player

import me.moon.event.SubscribeEvent
import me.moon.event.events.PacketEvent
import me.moon.features.modules.Module
import me.moon.features.setting.Setting
import me.moon.util.MathUtil
import me.moon.util.Timer
import net.minecraft.client.network.OtherClientPlayerEntity
import net.minecraft.entity.Entity
import net.minecraft.network.packet.Packet
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket
import net.minecraft.network.packet.c2s.common.KeepAliveC2SPacket
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket
import net.minecraft.network.packet.c2s.play.RequestCommandCompletionsC2SPacket
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket
import net.minecraft.util.math.BlockPos
import java.util.Queue
import java.util.concurrent.ConcurrentLinkedQueue

class Blink : Module("Blink", "Fakelag.", Module.Category.PLAYER, true, false, false) {
    private val cPacketPlayer = register(Setting("CPacketPlayer", true))
    private val autoOff = register(Setting("AutoOff", Mode.MANUAL))
    private val timeLimit = register(Setting("Time", 20, 1, 500, java.util.function.Predicate { autoOff.getValue() == Mode.TIME }))
    private val packetLimit = register(Setting("Packets", 20, 1, 500, java.util.function.Predicate { autoOff.getValue() == Mode.PACKETS }))
    private val distance = register(Setting("Distance", 10.0f, 1.0f, 100.0f, java.util.function.Predicate { autoOff.getValue() == Mode.DISTANCE }))
    private val timer = Timer()
    private val packets: Queue<Packet<*>> = ConcurrentLinkedQueue()
    private var entity: OtherClientPlayerEntity? = null
    private var packetsCanceled = 0
    private var startPos: BlockPos? = null

    init { setInstance() }
    private fun setInstance() { INSTANCE = this }

    override fun onEnable() {
        if (!fullNullCheck()) {
            val fake = OtherClientPlayerEntity(mc.world!!, mc.gameProfile)
            fake.refreshPositionAndAngles(mc.player!!.x, mc.player!!.y, mc.player!!.z, mc.player!!.yaw, mc.player!!.pitch)
            fake.setId(6942069)
            this.entity = fake
            mc.world!!.addEntity(fake)
            this.startPos = mc.player!!.blockPos
        } else {
            disable()
        }
        this.packetsCanceled = 0
        this.timer.reset()
    }

    override fun onUpdate() {
        if (nullCheck()
            || (this.autoOff.getValue() == Mode.TIME && this.timer.passedS(this.timeLimit.getValue().toDouble()))
            || (this.autoOff.getValue() == Mode.DISTANCE
                && this.startPos != null
                && mc.player!!.squaredDistanceTo(this.startPos!!.x.toDouble(), this.startPos!!.y.toDouble(), this.startPos!!.z.toDouble()) >= MathUtil.square(this.distance.getValue().toDouble()))
            || (this.autoOff.getValue() == Mode.PACKETS && this.packetsCanceled >= this.packetLimit.getValue())
        ) {
            this.disable()
        }
    }

    @SubscribeEvent
    fun onSendPacket(event: PacketEvent.Send) {
        if (mc.world != null && !mc.isInSingleplayer) {
            val packet = event.getPacket<Packet<*>>()
            if (this.cPacketPlayer.getValue() && packet is PlayerMoveC2SPacket) {
                event.isCanceled = true
                this.packets.add(packet)
                ++this.packetsCanceled
            }
            if (!this.cPacketPlayer.getValue()) {
                if (packet is ChatMessageC2SPacket
                    || packet is TeleportConfirmC2SPacket
                    || packet is KeepAliveC2SPacket
                    || packet is RequestCommandCompletionsC2SPacket
                ) {
                    return
                }
                this.packets.add(packet)
                event.isCanceled = true
                ++this.packetsCanceled
            }
        }
    }

    override fun onDisable() {
        if (!fullNullCheck()) {
            this.entity?.let { mc.world!!.removeEntity(it.id, Entity.RemovalReason.DISCARDED) }
            while (!this.packets.isEmpty()) {
                mc.player!!.networkHandler.sendPacket(this.packets.poll())
            }
        }
        this.startPos = null
    }

    override fun getDisplayInfo(): String? = if (this.packets.isNotEmpty()) this.packets.size.toString() else null

    enum class Mode {
        MANUAL,
        TIME,
        DISTANCE,
        PACKETS
    }

    companion object {
        @JvmField
        var INSTANCE: Blink = Blink()

        @JvmStatic
        fun getInstance(): Blink = INSTANCE
    }
}
