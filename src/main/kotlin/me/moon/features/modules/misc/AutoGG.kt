package me.moon.features.modules.misc

import net.minecraft.network.packet.Packet

import me.moon.Moon
import me.moon.event.SubscribeEvent
import me.moon.event.events.PacketEvent
import me.moon.features.command.Command
import me.moon.features.modules.Module
import me.moon.features.setting.Setting
import me.moon.util.TextUtil
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket
import java.util.HashMap

class AutoGG : Module("AutoGG", "Sends msg after you kill someone", Module.Category.MISC, true, false, false) {
    private val custom = register(Setting("Custom", "Ezzz."))
    private val totemPopCounter = register(Setting("TotemPop-Counter", true))
    private val totemPop = HashMap<String, Int>()
    @JvmField
    var target: String? = null

    init {
        setInstance()
    }

    private fun setInstance() {
        INSTANCE = this
    }

    @SubscribeEvent
    fun onPacketReceive(event: PacketEvent.Receive) {
        try {
            if (event.getPacket<Packet<*>>() is EntityStatusS2CPacket) {
                val packet = event.getPacket<EntityStatusS2CPacket>()
                if (packet.status.toInt() == 35) {
                    val entity = packet.getEntity(mc.world!!)
                    val name = entity!!.name.string
                    var count = 1
                    if (totemPop.containsKey(name)) {
                        count = totemPop[name]!!
                        totemPop[name] = ++count
                    } else {
                        totemPop[name] = count
                    }

                    if (this.totemPopCounter.getValue()
                        && !name.equals(mc.player!!.name.string)
                        && !Moon.friendManager!!.isFriend(name)
                    ) {
                        Command.sendMessage(
                            TextUtil.coloredString(name, TextUtil.Color.DARK_RED)
                                + ' '
                                + TextUtil.coloredString("had popped", TextUtil.Color.RED)
                                + ' '
                                + TextUtil.coloredString(count.toString(), TextUtil.Color.GREEN)
                                + ' '
                                + TextUtil.coloredString("totems", TextUtil.Color.DARK_RED)
                                + ' '
                                + TextUtil.coloredString("currently.", TextUtil.Color.RED)
                        )
                    }
                }
            }
        } catch (var5: NullPointerException) {
        }
    }

    override fun onUpdate() {
        for (player in mc.world!!.players) {
            val name = player.name.string
            if (totemPop.containsKey(name) && (!player.isAlive || player.health <= 0.0f)) {
                val count = totemPop[name]!!
                totemPop.remove(name)
                if (name == this.target) {
                    mc.player!!.networkHandler.sendChatMessage(this.custom.getValue() + ' ' + this.target + " died after popping " + count + " totems.")
                }
            }
        }
    }

    companion object {
        @JvmField
        var INSTANCE: AutoGG = AutoGG()

        @JvmStatic
        fun getInstance(): AutoGG = INSTANCE
    }
}
