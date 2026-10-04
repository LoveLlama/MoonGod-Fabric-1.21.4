package me.moon.features.modules.misc

import me.moon.event.SubscribeEvent
import me.moon.event.events.Render2DEvent
import me.moon.features.modules.Module
import me.moon.features.modules.client.ClickGui
import me.moon.util.ColorUtil
import me.moon.util.RenderUtil
import me.moon.util.Timer
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.ContainerComponent
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier
import java.awt.Color
import java.util.concurrent.ConcurrentHashMap

class ToolTips : Module("ShulkerViewer", "Several tweaks for tooltips.", Module.Category.MISC, true, false, false) {
    private val SHULKER_GUI_TEXTURE = Identifier.of("textures/gui/container/shulker_box.png")
    var spiedPlayers: MutableMap<PlayerEntity, ItemStack> = ConcurrentHashMap()
    var playerTimers: MutableMap<PlayerEntity, Timer> = ConcurrentHashMap()

    init {
        setInstance()
    }

    private fun setInstance() {
        INSTANCE = this
    }

    override fun onUpdate() {
        if (!fullNullCheck()) {
            for (player in mc.world!!.players) {
                if (player != null && player.mainHandStack.contains(DataComponentTypes.CONTAINER) && mc.player!! != player) {
                    val stack = player.mainHandStack
                    this.spiedPlayers[player] = stack
                }
            }
        }
    }

    override fun onRender2D(event: Render2DEvent) {
        if (!fullNullCheck()) {
            val x = -3
            var y = 124

            for (player in mc.world!!.players) {
                if (this.spiedPlayers[player] != null) {
                    var playerTimer: Timer?
                    if (player.mainHandStack.isEmpty || !player.mainHandStack.contains(DataComponentTypes.CONTAINER)) {
                        playerTimer = this.playerTimers[player]
                        if (playerTimer == null) {
                            val timer = Timer()
                            timer.reset()
                            this.playerTimers[player] = timer
                        } else if (playerTimer.passedS(3.0)) {
                            continue
                        }
                    } else if (player.mainHandStack.contains(DataComponentTypes.CONTAINER)) {
                        val pt = this.playerTimers[player]
                        if (pt != null) {
                            pt.reset()
                            this.playerTimers[player] = pt
                        }
                    }

                    val stack = this.spiedPlayers[player]!!
                    this.renderShulkerToolTip(stack, x, y, player.name.string)
                    y += 90
                }
            }
        }
    }

    fun renderShulkerToolTip(stack: ItemStack, x: Int, y: Int, name: String?) {
        val container = stack.get(DataComponentTypes.CONTAINER)
        if (container != null) {
            RenderUtil.drawTexturedRect(x, y, 0, 0, 176, 16, 500)
            RenderUtil.drawTexturedRect(x, y + 16, 0, 16, 176, 57, 500)
            RenderUtil.drawTexturedRect(x, y + 16 + 54, 0, 160, 176, 8, 500)
            val color = Color(ClickGui.getInstance().red.getValue(), ClickGui.getInstance().green.getValue(), ClickGui.getInstance().blue.getValue(), 200)
            val stacks: List<ItemStack> = container.stream().toList()
            for (i in stacks.indices) {
                val iX = x + i % 9 * 18 + 8
                val iY = y + i / 9 * 18 + 18
                val itemStack = stacks[i]
                if (!itemStack.isEmpty) {
                    RenderUtil.itemRender
                }
            }
            ColorUtil.toRGBA(color)
        }
    }

    companion object {
        @JvmField
        var INSTANCE: ToolTips = ToolTips()

        @JvmStatic
        fun getInstance(): ToolTips = INSTANCE
    }
}
