package me.moon.mixin.mixins

import io.netty.channel.ChannelHandlerContext
import me.moon.event.EventBus
import me.moon.event.events.PacketEvent
import net.minecraft.network.ClientConnection
import net.minecraft.network.packet.Packet
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

@Mixin(value = [ClientConnection::class], priority = Int.MAX_VALUE)
class MixinNetworkManager {
    @Inject(method = ["send(Lnet/minecraft/network/packet/Packet;)V"], at = [At("HEAD")], cancellable = true)
    private fun onSendPacketPre(packet: Packet<*>, ci: CallbackInfo) {
        val event = PacketEvent.Send(packet)
        EventBus.post(event)
        if (event.isCanceled) {
            ci.cancel()
        }
    }

    @Inject(method = ["channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/packet/Packet;)V"], at = [At("HEAD")], cancellable = true)
    private fun onChannelReadPre(context: ChannelHandlerContext, packet: Packet<*>, ci: CallbackInfo) {
        val event = PacketEvent.Receive(packet)
        EventBus.post(event)
        if (event.isCanceled) {
            ci.cancel()
        }
    }
}
