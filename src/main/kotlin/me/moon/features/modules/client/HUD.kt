package me.moon.features.modules.client

import me.moon.Moon
import me.moon.event.SubscribeEvent
import me.moon.event.events.ClientEvent
import me.moon.event.events.Render2DEvent
import me.moon.features.modules.Module
import me.moon.features.setting.Setting
import me.moon.util.ColorUtil
import me.moon.util.EntityUtil
import me.moon.util.MathUtil
import me.moon.util.RenderUtil
import me.moon.util.TextUtil
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ChatScreen
import net.minecraft.client.MinecraftClient
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.util.Formatting
import net.minecraft.world.World
import java.text.SimpleDateFormat
import java.util.Date

class HUD : Module {
    private val totem: ItemStack = ItemStack(Items.TOTEM_OF_UNDYING)
    private val renderingUp: Setting<Boolean> = register(Setting("RenderingUp", false))
    private val waterMark: Setting<Boolean> = register(Setting("Watermark", false))
    private val arrayList: Setting<Boolean> = register(Setting("ActiveModules", false))
    private val coords: Setting<Boolean> = register(Setting("Coords", false))
    private val direction: Setting<Boolean> = register(Setting("Direction", false))
    private val armor: Setting<Boolean> = register(Setting("Armor", false))
    private val totems: Setting<Boolean> = register(Setting("Totems", false))
    private val greeter: Setting<Boolean> = register(Setting("Welcomer", false))
    private val speed: Setting<Boolean> = register(Setting("Speed", false))
    private val potions: Setting<Boolean> = register(Setting("Potions", false))
    private val ping: Setting<Boolean> = register(Setting("Ping", false))
    private val tps: Setting<Boolean> = register(Setting("TPS", false))
    private val fps: Setting<Boolean> = register(Setting("FPS", false))
    private val lag: Setting<Boolean> = register(Setting("LagNotifier", false))
    private val hitMarkers: Setting<Boolean> = register(Setting("HitMarkers", true))
    private val players: MutableMap<String, Int> = HashMap()
    @JvmField var bracketColor: Setting<TextUtil.Color> = register(Setting("BracketColor", TextUtil.Color.WHITE))
    @JvmField var commandColor: Setting<TextUtil.Color> = register(Setting("NameColor", TextUtil.Color.WHITE))
    @JvmField var commandBracket: Setting<String> = register(Setting("Bracket", "<"))
    @JvmField var commandBracket2: Setting<String> = register(Setting("Bracket2", ">"))
    @JvmField var notifyToggles: Setting<Boolean> = register(Setting("ChatNotify", false))
    @JvmField var animationHorizontalTime: Setting<Int> = register(Setting("AnimationHTime", 500, 1, 1000) { this.arrayList.getValue() })
    @JvmField var animationVerticalTime: Setting<Int> = register(Setting("AnimationVTime", 50, 1, 500) { this.arrayList.getValue() })
    @JvmField var renderingMode: Setting<RenderingMode> = register(Setting("Ordering", RenderingMode.ABC))
    @JvmField var waterMarkX: Setting<Int> = register(Setting("WatermarkPosX", 2, 0, 100) { this.waterMark.getValue() })
    @JvmField var waterMarkY: Setting<Int> = register(Setting("WatermarkPosY", 2, 0, 100) { this.waterMark.getValue() })
    private val time: Setting<Boolean> = register(Setting("Time", false, "The time"))
    @JvmField var lagTime: Setting<Int> = register(Setting("LagTime", 200, 0, 1000))
    private var color = 0
    private var shouldIncrement = false
    private var hitMarkerTimer = 0

    constructor() : super("HUD", "HUD Elements rendered on your screen", Module.Category.CLIENT, true, false, false) {
        setInstance()
    }

    companion object {
        @JvmField
        var INSTANCE: HUD = HUD()

        @JvmStatic
        fun getInstance(): HUD {
            if (INSTANCE == null) {
                INSTANCE = HUD()
            }
            return INSTANCE
        }
    }

    private fun setInstance() {
        INSTANCE = this
    }

    override fun onUpdate() {
        if (this.shouldIncrement) {
            ++this.hitMarkerTimer
        }
        if (this.hitMarkerTimer == 10) {
            this.hitMarkerTimer = 0
            this.shouldIncrement = false
        }
    }

    private fun rainbowColor(var46: IntArray): Int {
        return if (ClickGui.getInstance().rainbowModeA.getValue() == ClickGui.rainbowModeArray.Up) {
            ColorUtil.rainbow(var46[0] * ClickGui.getInstance().rainbowHue.getValue()).rgb
        } else {
            ColorUtil.rainbow(ClickGui.getInstance().rainbowHue.getValue()).rgb
        }
    }

    override fun onRender2D(event: Render2DEvent) {
        if (!fullNullCheck()) {
            val width = this.renderer.scaledWidth
            val height = this.renderer.scaledHeight
            this.color = ColorUtil.toRGBA(ClickGui.getInstance().red.getValue(), ClickGui.getInstance().green.getValue(), ClickGui.getInstance().blue.getValue())
            if (this.waterMark.getValue()) {
                val string = "MoonGod | 777"
                if (ClickGui.getInstance().rainbow.getValue()) {
                    if (ClickGui.getInstance().rainbowModeHud.getValue() == ClickGui.rainbowMode.Static) {
                        this.renderer.drawString(string, this.waterMarkX.getValue().toFloat(), this.waterMarkY.getValue().toFloat(), ColorUtil.rainbow(ClickGui.getInstance().rainbowHue.getValue()).rgb, true)
                    } else {
                        val var5 = intArrayOf(1)
                        var var7 = 0.0f
                        for (c: Char in string.toCharArray()) {
                            this.renderer.drawString(c.toString(), this.waterMarkX.getValue().toFloat() + var7, this.waterMarkY.getValue().toFloat(), ColorUtil.rainbow(var5[0] * ClickGui.getInstance().rainbowHue.getValue()).rgb, true)
                            var7 += this.renderer.getStringWidth(c.toString()).toFloat()
                            var5[0]++
                        }
                    }
                } else {
                    this.renderer.drawString(string, this.waterMarkX.getValue().toFloat(), this.waterMarkY.getValue().toFloat(), this.color, true)
                }
            }

            val var46 = intArrayOf(1)
            var var47 = if (mc.currentScreen is ChatScreen && !this.renderingUp.getValue()) 14 else 0
            if (this.arrayList.getValue()) {
                if (this.renderingUp.getValue()) {
                    if (this.renderingMode.getValue() == RenderingMode.ABC) {
                        for (var6 in 0 until Moon.moduleManager!!.sortedModulesABC.size) {
                            val var58 = Moon.moduleManager!!.sortedModulesABC[var6]
                            this.renderer.drawString(var58, (width - 2 - this.renderer.getStringWidth(var58)).toFloat(), (2 + var47 * 10).toFloat(), if (ClickGui.getInstance().rainbow.getValue()) this.rainbowColor(var46) else this.color, true)
                            ++var47
                            var46[0]++
                        }
                    } else {
                        for (var48 in 0 until Moon.moduleManager!!.sortedModules.size) {
                            val var59 = Moon.moduleManager!!.sortedModules[var48]
                            val str = var59.getDisplayName() + Formatting.GRAY + (if (var59.getDisplayInfo() != null) " [" + Formatting.WHITE + var59.getDisplayInfo() + Formatting.GRAY + "]" else "")
                            this.renderer.drawString(str, (width - 2 - this.renderer.getStringWidth(str)).toFloat(), (2 + var47 * 10).toFloat(), if (ClickGui.getInstance().rainbow.getValue()) this.rainbowColor(var46) else this.color, true)
                            ++var47
                            var46[0]++
                        }
                    }
                } else if (this.renderingMode.getValue() == RenderingMode.ABC) {
                    for (var49 in 0 until Moon.moduleManager!!.sortedModulesABC.size) {
                        val var60 = Moon.moduleManager!!.sortedModulesABC[var49]
                        var47 += 10
                        this.renderer.drawString(var60, (width - 2 - this.renderer.getStringWidth(var60)).toFloat(), (height - var47).toFloat(), if (ClickGui.getInstance().rainbow.getValue()) this.rainbowColor(var46) else this.color, true)
                        var46[0]++
                    }
                } else {
                    for (var50 in 0 until Moon.moduleManager!!.sortedModules.size) {
                        val var61 = Moon.moduleManager!!.sortedModules[var50]
                        val var88 = var61.getDisplayName() + Formatting.GRAY + (if (var61.getDisplayInfo() != null) " [" + Formatting.WHITE + var61.getDisplayInfo() + Formatting.GRAY + "]" else "")
                        var47 += 10
                        this.renderer.drawString(var88, (width - 2 - this.renderer.getStringWidth(var88)).toFloat(), (height - var47).toFloat(), if (ClickGui.getInstance().rainbow.getValue()) this.rainbowColor(var46) else this.color, true)
                        var46[0]++
                    }
                }
            }

            var var51 = if (mc.currentScreen is ChatScreen && this.renderingUp.getValue()) 13 else if (this.renderingUp.getValue()) -2 else 0
            if (this.renderingUp.getValue()) {
                if (this.potions.getValue()) {
                    for (var78: StatusEffectInstance in ArrayList(mc.player!!.statusEffects)) {
                        val var89 = Moon.potionManager!!.getColoredPotionString(var78)
                        var51 += 10
                        this.renderer.drawString(var89, (width - this.renderer.getStringWidth(var89) - 2).toFloat(), (height - 2 - var51).toFloat(), var78.getEffectType().value().color, true)
                    }
                }
                if (this.speed.getValue()) {
                    val var63 = Formatting.GRAY.toString() + "Speed " + Formatting.WHITE + Moon.speedManager!!.getSpeedKpH() + " km/h"
                    var51 += 10
                    this.renderer.drawString(var63, (width - this.renderer.getStringWidth(var63) - 2).toFloat(), (height - 2 - var51).toFloat(), if (ClickGui.getInstance().rainbow.getValue()) this.rainbowColor(var46) else this.color, true)
                    var46[0]++
                }
                if (this.time.getValue()) {
                    val var64 = Formatting.GRAY.toString() + "Time " + Formatting.WHITE + SimpleDateFormat("h:mm a").format(Date())
                    var51 += 10
                    this.renderer.drawString(var64, (width - this.renderer.getStringWidth(var64) - 2).toFloat(), (height - 2 - var51).toFloat(), if (ClickGui.getInstance().rainbow.getValue()) this.rainbowColor(var46) else this.color, true)
                    var46[0]++
                }
                if (this.tps.getValue()) {
                    val var65 = Formatting.GRAY.toString() + "TPS " + Formatting.WHITE + Moon.serverManager!!.getTPS()
                    var51 += 10
                    this.renderer.drawString(var65, (width - this.renderer.getStringWidth(var65) - 2).toFloat(), (height - 2 - var51).toFloat(), if (ClickGui.getInstance().rainbow.getValue()) this.rainbowColor(var46) else this.color, true)
                    var46[0]++
                }
                val var66 = Formatting.GRAY.toString() + "FPS " + Formatting.WHITE + mc.currentFps
                val var74 = Formatting.GRAY.toString() + "Ping " + Formatting.WHITE + Moon.serverManager!!.getPing()
                if (this.renderer.getStringWidth(var74) > this.renderer.getStringWidth(var66)) {
                    if (this.ping.getValue()) {
                        var51 += 10
                        this.renderer.drawString(var74, (width - this.renderer.getStringWidth(var74) - 2).toFloat(), (height - 2 - var51).toFloat(), if (ClickGui.getInstance().rainbow.getValue()) this.rainbowColor(var46) else this.color, true)
                        var46[0]++
                    }
                    if (this.fps.getValue()) {
                        var51 += 10
                        this.renderer.drawString(var66, (width - this.renderer.getStringWidth(var66) - 2).toFloat(), (height - 2 - var51).toFloat(), if (ClickGui.getInstance().rainbow.getValue()) this.rainbowColor(var46) else this.color, true)
                        var46[0]++
                    }
                } else {
                    if (this.fps.getValue()) {
                        var51 += 10
                        this.renderer.drawString(var66, (width - this.renderer.getStringWidth(var66) - 2).toFloat(), (height - 2 - var51).toFloat(), if (ClickGui.getInstance().rainbow.getValue()) this.rainbowColor(var46) else this.color, true)
                        var46[0]++
                    }
                    if (this.ping.getValue()) {
                        var51 += 10
                        this.renderer.drawString(var74, (width - this.renderer.getStringWidth(var74) - 2).toFloat(), (height - 2 - var51).toFloat(), if (ClickGui.getInstance().rainbow.getValue()) this.rainbowColor(var46) else this.color, true)
                        var46[0]++
                    }
                }
            } else {
                if (this.potions.getValue()) {
                    for (var79: StatusEffectInstance in ArrayList(mc.player!!.statusEffects)) {
                        val var90 = Moon.potionManager!!.getColoredPotionString(var79)
                        this.renderer.drawString(var90, (width - this.renderer.getStringWidth(var90) - 2).toFloat(), (2 + var51++ * 10).toFloat(), var79.getEffectType().value().color, true)
                    }
                }
                if (this.speed.getValue()) {
                    val var68 = Formatting.GRAY.toString() + "Speed " + Formatting.WHITE + Moon.speedManager!!.getSpeedKpH() + " km/h"
                    this.renderer.drawString(var68, (width - this.renderer.getStringWidth(var68) - 2).toFloat(), (2 + var51++ * 10).toFloat(), if (ClickGui.getInstance().rainbow.getValue()) this.rainbowColor(var46) else this.color, true)
                    var46[0]++
                }
                if (this.time.getValue()) {
                    val var69 = Formatting.GRAY.toString() + "Time " + Formatting.WHITE + SimpleDateFormat("h:mm a").format(Date())
                    this.renderer.drawString(var69, (width - this.renderer.getStringWidth(var69) - 2).toFloat(), (2 + var51++ * 10).toFloat(), if (ClickGui.getInstance().rainbow.getValue()) this.rainbowColor(var46) else this.color, true)
                    var46[0]++
                }
                if (this.tps.getValue()) {
                    val var70 = Formatting.GRAY.toString() + "TPS " + Formatting.WHITE + Moon.serverManager!!.getTPS()
                    this.renderer.drawString(var70, (width - this.renderer.getStringWidth(var70) - 2).toFloat(), (2 + var51++ * 10).toFloat(), if (ClickGui.getInstance().rainbow.getValue()) this.rainbowColor(var46) else this.color, true)
                    var46[0]++
                }
                val var71 = Formatting.GRAY.toString() + "FPS " + Formatting.WHITE + mc.currentFps
                val var76 = Formatting.GRAY.toString() + "Ping " + Formatting.WHITE + Moon.serverManager!!.getPing()
                if (this.renderer.getStringWidth(var76) > this.renderer.getStringWidth(var71)) {
                    if (this.ping.getValue()) {
                        this.renderer.drawString(var76, (width - this.renderer.getStringWidth(var76) - 2).toFloat(), (2 + var51++ * 10).toFloat(), if (ClickGui.getInstance().rainbow.getValue()) this.rainbowColor(var46) else this.color, true)
                        var46[0]++
                    }
                    if (this.fps.getValue()) {
                        this.renderer.drawString(var71, (width - this.renderer.getStringWidth(var71) - 2).toFloat(), (2 + var51++ * 10).toFloat(), if (ClickGui.getInstance().rainbow.getValue()) this.rainbowColor(var46) else this.color, true)
                        var46[0]++
                    }
                } else {
                    if (this.fps.getValue()) {
                        this.renderer.drawString(var71, (width - this.renderer.getStringWidth(var71) - 2).toFloat(), (2 + var51++ * 10).toFloat(), if (ClickGui.getInstance().rainbow.getValue()) this.rainbowColor(var46) else this.color, true)
                        var46[0]++
                    }
                    if (this.ping.getValue()) {
                        this.renderer.drawString(var76, (width - this.renderer.getStringWidth(var76) - 2).toFloat(), (2 + var51++ * 10).toFloat(), if (ClickGui.getInstance().rainbow.getValue()) this.rainbowColor(var46) else this.color, true)
                        var46[0]++
                    }
                }
            }

            val var72 = mc.world!!.registryKey == World.NETHER
            val var77 = mc.player!!.x.toInt()
            val var80 = mc.player!!.y.toInt()
            val var81 = mc.player!!.z.toInt()
            val var11 = if (!var72) 0.125f else 8.0f
            val hposX = (mc.player!!.x * var11).toInt()
            val hposZ = (mc.player!!.z * var11).toInt()
            var51 = if (mc.currentScreen is ChatScreen) 14 else 0
            val coordinates = Formatting.WHITE.toString() + "XYZ " + Formatting.RESET.toString() + var77 + ", " + var80 + ", " + var81 + Formatting.WHITE + " [" + Formatting.RESET + hposX + ", " + hposZ + Formatting.WHITE + "]"
            val direction = if (this.direction.getValue()) Moon.rotationManager!!.getDirection4D(false) else ""
            val coords = if (this.coords.getValue()) coordinates else ""
            var51 += 10
            if (ClickGui.getInstance().rainbow.getValue()) {
                val rainbowCoords = if (this.coords.getValue()) "XYZ " + var77 + ", " + var80 + ", " + var81 + " [" + hposX + ", " + hposZ + "]" else ""
                if (ClickGui.getInstance().rainbowModeHud.getValue() == ClickGui.rainbowMode.Static) {
                    this.renderer.drawString(direction, 2.0f, (height - var51 - 11).toFloat(), ColorUtil.rainbow(ClickGui.getInstance().rainbowHue.getValue()).rgb, true)
                    this.renderer.drawString(rainbowCoords, 2.0f, (height - var51).toFloat(), ColorUtil.rainbow(ClickGui.getInstance().rainbowHue.getValue()).rgb, true)
                } else {
                    var counter2 = 1
                    var s = 0.0f
                    for (c: Char in direction.toCharArray()) {
                        this.renderer.drawString(c.toString(), 2.0f + s, (height - var51 - 11).toFloat(), ColorUtil.rainbow(counter2 * ClickGui.getInstance().rainbowHue.getValue()).rgb, true)
                        s += this.renderer.getStringWidth(c.toString()).toFloat()
                        counter2++
                    }
                    var var83 = 1
                    var var85 = 0.0f
                    for (c: Char in rainbowCoords.toCharArray()) {
                        this.renderer.drawString(c.toString(), 2.0f + var85, (height - var51).toFloat(), ColorUtil.rainbow(var83 * ClickGui.getInstance().rainbowHue.getValue()).rgb, true)
                        var85 += this.renderer.getStringWidth(c.toString()).toFloat()
                        var83++
                    }
                }
            } else {
                this.renderer.drawString(direction, 2.0f, (height - var51 - 11).toFloat(), this.color, true)
                this.renderer.drawString(coords, 2.0f, (height - var51).toFloat(), this.color, true)
            }

            if (this.armor.getValue()) this.renderArmorHUD()
            if (this.totems.getValue()) this.renderTotemHUD()
            if (this.greeter.getValue()) this.renderGreeter()
            if (this.lag.getValue()) this.renderLag()
            if (this.hitMarkers.getValue() && this.hitMarkerTimer > 0) this.drawHitMarkers()
        }
    }

    fun getTextRadarPlayers(): MutableMap<String, Int> {
        return EntityUtil.getTextRadarPlayers()
    }

    fun renderGreeter() {
        val width = this.renderer.scaledWidth
        var text = ""
        if (this.greeter.getValue()) {
            text = text + MathUtil.getTimeOfDay() + (mc.player!!.displayName?.string ?: "")
        }
        if (ClickGui.getInstance().rainbow.getValue()) {
            if (ClickGui.getInstance().rainbowModeHud.getValue() == ClickGui.rainbowMode.Static) {
                this.renderer.drawString(text, width / 2.0f - this.renderer.getStringWidth(text) / 2.0f + 2.0f, 2.0f, ColorUtil.rainbow(ClickGui.getInstance().rainbowHue.getValue()).rgb, true)
            } else {
                var counter1 = 1
                var i = 0.0f
                for (c: Char in text.toCharArray()) {
                    this.renderer.drawString(c.toString(), width / 2.0f - this.renderer.getStringWidth(text) / 2.0f + 2.0f + i, 2.0f, ColorUtil.rainbow(counter1 * ClickGui.getInstance().rainbowHue.getValue()).rgb, true)
                    i += this.renderer.getStringWidth(c.toString()).toFloat()
                    counter1++
                }
            }
        } else {
            this.renderer.drawString(text, width / 2.0f - this.renderer.getStringWidth(text) / 2.0f + 2.0f, 2.0f, this.color, true)
        }
    }

    fun renderLag() {
        val width = this.renderer.scaledWidth
        if (Moon.serverManager!!.isServerNotResponding()) {
            val text = Formatting.RED.toString() + "Server not responding " + MathUtil.round(Moon.serverManager!!.serverRespondingTime() / 1000.0f, 1) + "s."
            this.renderer.drawString(text, width / 2.0f - this.renderer.getStringWidth(text) / 2.0f + 2.0f, 20.0f, this.color, true)
        }
    }

    private fun hudContext(): DrawContext {
        return DrawContext(mc, mc.bufferBuilders.entityVertexConsumers)
    }

    fun renderTotemHUD() {
        val width = this.renderer.scaledWidth
        val height = this.renderer.scaledHeight
        var totems = mc.player!!.inventory.main.filter { it.isOf(Items.TOTEM_OF_UNDYING) }.sumOf { it.count }
        if (mc.player!!.offHandStack.isOf(Items.TOTEM_OF_UNDYING)) {
            totems += mc.player!!.offHandStack.count
        }
        if (totems > 0) {
            val i = width / 2
            val y = height - 55 - (if (mc.player!!.isTouchingWater() && mc.interactionManager!!.currentGameMode.isSurvivalLike()) 10 else 0)
            val x = i - 189 + 180 + 2
            val ctx = hudContext()
            ctx.drawItem(this.totem, x, y)
            ctx.drawStackOverlay(mc.textRenderer, this.totem, x, y, "")
            ctx.draw()
            this.renderer.drawStringWithShadow(totems.toString(), (x + 19 - 2 - this.renderer.getStringWidth(totems.toString())).toFloat(), (y + 9).toFloat(), 16777215)
        }
    }

    fun renderArmorHUD() {
        val width = this.renderer.scaledWidth
        val height = this.renderer.scaledHeight
        val i = width / 2
        var iteration = 0
        val y = height - 55 - (if (mc.player!!.isTouchingWater() && mc.interactionManager!!.currentGameMode.isSurvivalLike()) 10 else 0)
        val ctx = hudContext()
        for (`is`: ItemStack in mc.player!!.inventory.armor) {
            ++iteration
            if (!`is`.isEmpty) {
                val x = i - 90 + (9 - iteration) * 20 + 2
                ctx.drawItem(`is`, x, y)
                ctx.drawStackOverlay(mc.textRenderer, `is`, x, y, "")
                val s = if (`is`.count > 1) `is`.count.toString() else ""
                this.renderer.drawStringWithShadow(s, (x + 19 - 2 - this.renderer.getStringWidth(s)).toFloat(), (y + 9).toFloat(), 16777215)
                var dmg = 0
                val green = (`is`.maxDamage - `is`.damage).toFloat() / `is`.maxDamage.toFloat()
                val red = 1.0f - green
                dmg = 100 - (red * 100.0f).toInt()
                this.renderer.drawStringWithShadow(dmg.toString(), (x + 8 - this.renderer.getStringWidth(dmg.toString()) / 2).toFloat(), (y - 11).toFloat(), ColorUtil.toRGBA((red * 255.0f).toInt(), (green * 255.0f).toInt(), 0))
            }
        }
        ctx.draw()
    }

    fun drawHitMarkers() {
        val width = this.renderer.scaledWidth
        val height = this.renderer.scaledHeight
        RenderUtil.drawLine(width / 2.0f - 4.0f, height / 2.0f - 4.0f, width / 2.0f - 8.0f, height / 2.0f - 8.0f, 1.0f, ColorUtil.toRGBA(255, 255, 255, 255))
        RenderUtil.drawLine(width / 2.0f + 4.0f, height / 2.0f - 4.0f, width / 2.0f + 8.0f, height / 2.0f - 8.0f, 1.0f, ColorUtil.toRGBA(255, 255, 255, 255))
        RenderUtil.drawLine(width / 2.0f - 4.0f, height / 2.0f + 4.0f, width / 2.0f - 8.0f, height / 2.0f + 8.0f, 1.0f, ColorUtil.toRGBA(255, 255, 255, 255))
        RenderUtil.drawLine(width / 2.0f + 4.0f, height / 2.0f + 4.0f, width / 2.0f + 8.0f, height / 2.0f + 8.0f, 1.0f, ColorUtil.toRGBA(255, 255, 255, 255))
    }

    fun onAttack() {
        this.shouldIncrement = true
    }

    override fun onLoad() {
        Moon.commandManager!!.setClientMessage(this.getCommandMessage())
    }

    @SubscribeEvent
    fun onSettingChange(event: ClientEvent) {
        val setting = event.getSetting()
        if (event.stage == 2 && setting != null && this.equals(setting.getFeature())) {
            Moon.commandManager!!.setClientMessage(this.getCommandMessage())
        }
    }

    fun getCommandMessage(): String {
        return TextUtil.coloredString(this.commandBracket.getPlannedValue(), this.bracketColor.getPlannedValue()) + TextUtil.coloredString("MoonGod", this.commandColor.getPlannedValue()) + TextUtil.coloredString(this.commandBracket2.getPlannedValue(), this.bracketColor.getPlannedValue())
    }

    fun drawTextRadar(yOffset: Int) {
        if (!this.players.isEmpty()) {
            var y = this.renderer.getFontHeight() + 7 + yOffset
            for (player: Map.Entry<String, Int> in this.players.entries) {
                val text = player.key + " "
                val textheight = this.renderer.getFontHeight() + 1
                this.renderer.drawString(text, 2.0f, y.toFloat(), this.color, true)
                y += textheight
            }
        }
    }

    enum class RenderingMode {
        Length,
        ABC
    }

    enum class Sound {
        NONE,
        COD,
        CSGO
    }
}
