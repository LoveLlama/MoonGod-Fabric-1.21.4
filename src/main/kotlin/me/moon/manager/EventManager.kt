package me.moon.manager

import com.google.common.base.Strings
import com.mojang.blaze3d.systems.RenderSystem
import me.moon.Moon
import me.moon.event.EventBus
import me.moon.event.SubscribeEvent
import me.moon.event.events.ConnectionEvent
import me.moon.event.events.DeathEvent
import me.moon.event.events.PacketEvent
import me.moon.event.events.Render2DEvent
import me.moon.event.events.Render3DEvent
import me.moon.event.events.UpdateWalkingPlayerEvent
import me.moon.features.Feature
import me.moon.features.command.Command
import me.moon.features.modules.client.HUD
import me.moon.util.RenderUtil
import me.moon.util.Timer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.RenderTickCounter
import net.minecraft.client.util.InputUtil
import net.minecraft.network.packet.Packet
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket
import net.minecraft.network.packet.s2c.play.PlayerRemoveS2CPacket
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket
import net.minecraft.util.Formatting
import org.lwjgl.opengl.GL13
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean

class EventManager : Feature() {
    private val tickOngoing = AtomicBoolean(false)
    private val logoutTimer = Timer()
    private val keyStates = HashMap<Int, Boolean>()

    fun init() {
        if (!registered) {
            ClientTickEvents.START_CLIENT_TICK.register { tickOngoing.set(true) }
            ClientTickEvents.END_CLIENT_TICK.register { onEndTick() }
            ClientPlayConnectionEvents.JOIN.register { _, _, _ -> onClientConnect() }
            ClientPlayConnectionEvents.DISCONNECT.register { _, _ -> onClientDisconnect() }
            WorldRenderEvents.LAST.register { onWorldRender(it) }
            HudRenderCallback.EVENT.register { ctx, tickCounter -> onHudRender(ctx, tickCounter) }
            ClientSendMessageEvents.ALLOW_CHAT.register { message -> onChatSent(message) }
            registered = true
        }
        active = true
        EventBus.register(this)
    }

    fun onUnload() {
        active = false
        EventBus.unregister(this)
    }

    fun ticksOngoing(): Boolean = tickOngoing.get()

    private fun onEndTick() {
        if (!active) return
        if (!fullNullCheck()) {
            Moon.inventoryManager!!.update()
            Moon.moduleManager!!.onUpdate()
            if (HUD.getInstance().renderingMode.getValue() == HUD.RenderingMode.Length) {
                Moon.moduleManager!!.sortModules(true)
            } else {
                Moon.moduleManager!!.sortModulesABC()
            }
            Moon.moduleManager!!.onTick()
            for (player in mc.world!!.players) {
                if (player != null && !(player.health > 0.0F)) {
                    EventBus.post(DeathEvent(player))
                }
            }
        }
        pollKeys()
    }

    private fun pollKeys() {
        val window = mc.window ?: return
        for (module in Moon.moduleManager!!.modules) {
            val k = module.bind.getValue().getKey()
            if (k < 0 || k >= 256) {
                keyStates[k] = false
                continue
            }
            val pressed = InputUtil.isKeyPressed(window.handle, k)
            val wasPressed = keyStates[k] ?: false
            if (pressed && !wasPressed) {
                Moon.moduleManager!!.onKeyPressed(k)
            }
            keyStates[k] = pressed
        }
    }

    private fun onClientConnect() {
        if (!active) return
        logoutTimer.reset()
        Moon.moduleManager!!.onLogin()
    }

    private fun onClientDisconnect() {
        if (!active) return
        Moon.moduleManager!!.onLogout()
    }

    private fun onWorldRender(context: WorldRenderContext) {
        if (!active) return
        context.frustum()?.let { RenderUtil.camera = it }
        RenderSystem.enableBlend()
        RenderSystem.blendFuncSeparate(
            com.mojang.blaze3d.platform.GlStateManager.SrcFactor.SRC_ALPHA,
            com.mojang.blaze3d.platform.GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA,
            com.mojang.blaze3d.platform.GlStateManager.SrcFactor.ONE,
            com.mojang.blaze3d.platform.GlStateManager.DstFactor.ZERO
        )
        RenderSystem.disableDepthTest()
        RenderSystem.lineWidth(1.0f)
        val render3dEvent = Render3DEvent(context.tickCounter().getTickDelta(true))
        Moon.moduleManager!!.onRender3D(render3dEvent)
        RenderSystem.lineWidth(1.0f)
        RenderSystem.disableBlend()
        RenderSystem.enableDepthTest()
        RenderSystem.enableCull()
        RenderSystem.depthMask(true)
        RenderSystem.enableBlend()
        RenderSystem.enableDepthTest()
    }

    @SubscribeEvent
    fun onUpdateWalkingPlayer(event: UpdateWalkingPlayerEvent) {
        if (!fullNullCheck()) {
            if (event.stage == 0) {
                Moon.speedManager!!.updateValues()
                Moon.rotationManager!!.updateRotations()
                Moon.positionManager!!.updatePosition()
            }
            if (event.stage == 1) {
                Moon.rotationManager!!.restoreRotations()
                Moon.positionManager!!.restorePosition()
            }
        }
    }

    @SubscribeEvent
    fun onPacketReceive(event: PacketEvent.Receive) {
        if (event.stage == 0) {
            Moon.serverManager!!.onPacketReceived()
            val packet: Packet<*>? = event.getPacket<Packet<*>>()
            if (packet is PlayerListS2CPacket && !fullNullCheck() && logoutTimer.passedS(1.0)) {
                val actions = packet.actions
                if (!actions.contains(PlayerListS2CPacket.Action.ADD_PLAYER)) {
                    return
                }
                packet.entries.stream()
                    .filter { data ->
                        val profile = data.profile
                        profile != null && (!Strings.isNullOrEmpty(profile.name) || profile.id != null)
                    }
                    .forEach { data ->
                        val profile = data.profile
                        val id: UUID = profile!!.id
                        val name = profile.name
                        EventBus.post(ConnectionEvent(0, id, name))
                    }
            }
            if (packet is PlayerRemoveS2CPacket && !fullNullCheck() && logoutTimer.passedS(1.0)) {
                for (id in packet.profileIds) {
                    val entity = mc.world!!.getPlayerByUuid(id)
                    if (entity != null) {
                        val logoutName = entity.name.string
                        EventBus.post(ConnectionEvent(1, entity, id, logoutName))
                    } else {
                        EventBus.post(ConnectionEvent(2, id, null))
                    }
                }
            }
            if (packet is WorldTimeUpdateS2CPacket) {
                Moon.serverManager?.update()
            }
        }
    }

    private fun onHudRender(ctx: DrawContext, tickCounter: RenderTickCounter) {
        if (!active) return
        Moon.textManager?.updateResolution()
        val render2dEvent = Render2DEvent(tickCounter.getTickDelta(true))
        Moon.moduleManager!!.onRender2D(render2dEvent)
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)
    }

    private fun onChatSent(message: String): Boolean {
        if (!active) return true
        try {
            if (message.startsWith(Command.getCommandPrefix())) {
                mc.inGameHud.chatHud.addToMessageHistory(message)
                if (message.length > 1) {
                    Moon.commandManager!!.executeCommand(message.substring(Command.getCommandPrefix().length - 1))
                } else {
                    Command.sendMessage("Please enter a command.")
                }
                return false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Command.sendMessage(Formatting.RED.toString() + "An error occurred while running this command. Check the log!")
        }
        return true
    }

    companion object {
        private var registered = false
        private var active = false
    }
}
