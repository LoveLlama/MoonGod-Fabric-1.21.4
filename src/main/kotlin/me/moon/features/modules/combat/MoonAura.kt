package me.moon.features.modules.combat

import net.minecraft.network.packet.Packet

import me.moon.Moon
import me.moon.event.SubscribeEvent
import me.moon.event.events.PacketEvent
import me.moon.event.events.Render3DEvent
import me.moon.features.modules.Module
import me.moon.features.modules.client.ClickGui
import me.moon.features.modules.misc.AutoGG
import me.moon.features.setting.Setting
import me.moon.util.BlockUtil
import me.moon.util.ColorUtil
import me.moon.util.DamageUtil
import me.moon.util.EntityUtil
import me.moon.util.MathUtil
import me.moon.util.RenderUtil
import me.moon.util.Timer
import net.minecraft.block.Blocks
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen
import net.minecraft.entity.Entity
import net.minecraft.entity.decoration.EndCrystalEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Items
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import java.awt.Color
import java.util.function.Predicate
import kotlin.Comparator

class MoonAura : Module("AutoCrystal", "Version: 0.5.3", Module.Category.COMBAT, true, false, false) {
    private val placeTimer = Timer()
    private val breakTimer = Timer()
    private val setting = register(Setting("Settings", Settings.PLACE))
    private val place = register(Setting("Place", true, Predicate { setting.getValue() == Settings.PLACE }))
    private val placeDelay = register(Setting("PlaceDelay", 0.0, 0.0, 400.0, Predicate { place.getValue() && setting.getValue() == Settings.PLACE }))
    private val placeRange = register(Setting("PlaceRange", 5.0, 0.0, 6.0, Predicate { place.getValue() && setting.getValue() == Settings.PLACE }))
    private val explode = register(Setting("Break", true, Predicate { setting.getValue() == Settings.BREAK }))
    private val packetBreak = register(Setting("PacketBreak", true, Predicate { explode.getValue() && setting.getValue() == Settings.BREAK }))
    private val predictsBreak = register(Setting("PredictsBreak", true, Predicate { setting.getValue() == Settings.BREAK }))
    private val fastUpdate = register(Setting("FastUpDate", true, Predicate { setting.getValue() == Settings.BREAK }))
    private val breakDelay = register(Setting("BreakDelay", 0.0, 0.0, 400.0, Predicate { explode.getValue() && setting.getValue() == Settings.BREAK }))
    private val breakRange = register(Setting("BreakRange", 6.0, 0.0, 6.0, Predicate { explode.getValue() && setting.getValue() == Settings.BREAK }))
    private val rotate = register(Setting("Rotate", false, Predicate { setting.getValue() == Settings.MISC }))
    private val antiSuicide = register(Setting("AntiSuicide", true, Predicate { setting.getValue() == Settings.MISC }))
    private val raytrace = register(Setting("Raytrace", 3.0, 0.0, 3.0, Predicate { setting.getValue() == Settings.MISC }))
    private val targetRange = register(Setting("TargetRange", 12.0, 0.0, 12.0, Predicate { setting.getValue() == Settings.MISC }))
    private val minPlaceDmg = register(Setting("MinPlaceDmg", 6.0, 0.0, 24.0, Predicate { setting.getValue() == Settings.MISC }))
    private val minBreakDmg = register(Setting("MinBreakDmg", 6.0, 0.0, 24.0, Predicate { setting.getValue() == Settings.MISC }))
    private val maxSelfDmg = register(Setting("MaxSelfDmg", 9.5, 0.0, 12.0, Predicate { setting.getValue() == Settings.MISC }))
    private val breakSwing = register(Setting("BreakSwing", SwingMode.MainHand, Predicate { setting.getValue() == Settings.MISC }))
    private val render = register(Setting("Render", true, Predicate { setting.getValue() == Settings.RENDER }))
    private val renderDmg = register(Setting("RenderDmg", true, Predicate { render.getValue() && setting.getValue() == Settings.RENDER }))
    private val box = register(Setting("Box", true, Predicate { render.getValue() && setting.getValue() == Settings.RENDER }))
    private val outline = register(Setting("Outline", true, Predicate { render.getValue() && setting.getValue() == Settings.RENDER }))
    private val red = register(Setting("Red", 255, 0, 255, Predicate { render.getValue() && box.getValue() && setting.getValue() == Settings.RENDER }))
    private val green = register(Setting("Green", 255, 0, 255, Predicate { render.getValue() && box.getValue() && setting.getValue() == Settings.RENDER }))
    private val blue = register(Setting("Blue", 255, 0, 255, Predicate { render.getValue() && box.getValue() && setting.getValue() == Settings.RENDER }))
    private val alpha = register(Setting("Alpha", 200, 0, 255, Predicate { render.getValue() && box.getValue() && setting.getValue() == Settings.RENDER }))
    private val boxAlpha = register(Setting("BoxAlpha", 100, 0, 255, Predicate { render.getValue() && box.getValue() && setting.getValue() == Settings.RENDER }))
    private val lineWidth = register(Setting("LineWidth", 1.0f, 0.1f, 5.0f, Predicate { render.getValue() && box.getValue() && setting.getValue() == Settings.RENDER }))
    private val cRed = register(Setting("OL-Red", 255, 0, 255, Predicate { render.getValue() && outline.getValue() && setting.getValue() == Settings.RENDER }))
    private val cGreen = register(Setting("OL-Green", 255, 0, 255, Predicate { render.getValue() && outline.getValue() && setting.getValue() == Settings.RENDER }))
    private val cBlue = register(Setting("OL-Blue", 255, 0, 255, Predicate { render.getValue() && outline.getValue() && setting.getValue() == Settings.RENDER }))
    private val cAlpha = register(Setting("OL-Alpha", 200, 0, 255, Predicate { render.getValue() && outline.getValue() && setting.getValue() == Settings.RENDER }))
    private var crystal: EndCrystalEntity? = null
    private var rotating = false
    private var displaytarget: String? = null
    private var target: PlayerEntity? = null
    private var pitch = 0.0f
    private var yaw = 0.0f
    private var pos: BlockPos? = null

    override fun onEnable() {
        this.placeTimer.reset()
        this.breakTimer.reset()
        this.target = null
        this.displaytarget = null
        this.crystal = null
        this.pos = null
    }

    override fun onDisable() {
        this.rotating = false
    }

    override fun onTick() {
        if (!fullNullCheck()) {
            if (mc.currentScreen is GenericContainerScreen) {
                val cross = mc.crosshairTarget
                if (cross is BlockHitResult && mc.world!!.getBlockState(cross.blockPos).block == Blocks.ENDER_CHEST) {
                    mc.setScreen(null)
                }
            }

            if (mc.player!!.offHandStack.item != Items.END_CRYSTAL && mc.player!!.mainHandStack.item != Items.END_CRYSTAL) {
                this.target = null
                this.pos = null
            } else {
                if (this.target == null) {
                    this.target = this.getTarget()
                }

                if (this.target == null) {
                    this.crystal = null
                } else {
                    this.crystal = mc.world!!.entities.filter { this.hit1(it) }.map { it as EndCrystalEntity }.minWithOrNull(Comparator.comparing { this.target!!.distanceTo(it) })
                    if (this.crystal != null && this.explode.getValue() && this.breakTimer.passedMs(this.breakDelay.getValue().toLong())) {
                        this.breakTimer.reset()
                        val hand = if (this.breakSwing.getValue() == SwingMode.MainHand) Hand.MAIN_HAND else Hand.OFF_HAND
                        if (AsyncHit.getInstance().isOn()) {
                            this.rotateTo(this.crystal!!)
                            AsyncHit.getInstance().enqueueAttack(this.crystal!!)
                        } else if (this.packetBreak.getValue()) {
                            this.rotateTo(this.crystal!!)
                            EntityUtil.attackEntity(this.crystal!!, true, hand)
                        } else {
                            this.rotateTo(this.crystal!!)
                            EntityUtil.attackEntity(this.crystal!!, false, hand)
                            mc.player!!.resetLastAttackedTicks()
                        }
                    }

                    if (this.place.getValue() && this.placeTimer.passedMs(this.placeDelay.getValue().toLong())) {
                        this.placeTimer.reset()
                        var damage = 1.5f

                        for (blockPos in this.possiblePlacePositions(this.placeRange.getValue().toFloat())) {
                            if (this.target != null
                                && blockPos != null
                                && this.target!!.isAlive
                                && !(this.target!!.health + this.target!!.absorptionAmount <= 0.0f)
                            ) {
                                val targetDamage = DamageUtil.calculateDamage(blockPos.x + 0.5, blockPos.y + 1.0, blockPos.z + 0.5, this.target!!)
                                if (!(targetDamage < this.minPlaceDmg.getValue()) && !(damage >= targetDamage)) {
                                    this.pos = blockPos
                                    damage = targetDamage
                                }
                            }
                        }

                        if (damage == 1.5f) {
                            this.target = null
                            this.pos = null
                            return
                        }

                        if (this.pos != null) {
                            this.rotateToPos(this.pos!!)
                            BlockUtil.placeCrystalOnBlock(this.pos!!, if (mc.player!!.offHandStack.item == Items.END_CRYSTAL) Hand.OFF_HAND else Hand.MAIN_HAND)
                        }
                    }
                }
            }
        }
    }

    private fun rotateTo(entity: Entity) {
        if (this.rotate.getValue()) {
            val angle = MathUtil.calcAngle(mc.player!!.getCameraPosVec(mc.renderTickCounter.getTickDelta(true)), entity.pos)
            this.yaw = angle[0]
            this.pitch = angle[1]
            this.rotating = true
        }
    }

    private fun rotateToPos(pos: BlockPos) {
        if (this.rotate.getValue()) {
            val angle = MathUtil.calcAngle(
                mc.player!!.getCameraPosVec(mc.renderTickCounter.getTickDelta(true)),
                Vec3d(pos.x + 0.5, (pos.y - 0.5).toDouble(), pos.z + 0.5)
            )
            this.yaw = angle[0]
            this.pitch = angle[1]
            this.rotating = true
        }
    }

    @SubscribeEvent
    fun onPacketSend(event: PacketEvent.Send) {
        if (fullNullCheck()) {
            return
        }
        if (this.rotate.getValue() && this.rotating && event.getPacket<Packet<*>>() is PlayerMoveC2SPacket) {
            val packet = event.getPacket<PlayerMoveC2SPacket>()
            this.rotating = false
            event.isCanceled = true
            when (packet) {
                is PlayerMoveC2SPacket.LookAndOnGround -> mc.player!!.networkHandler.sendPacket(PlayerMoveC2SPacket.LookAndOnGround(this.yaw, this.pitch, packet.isOnGround(), packet.horizontalCollision()))
                is PlayerMoveC2SPacket.Full -> mc.player!!.networkHandler.sendPacket(PlayerMoveC2SPacket.Full(mc.player!!.x, mc.player!!.y, mc.player!!.z, this.yaw, this.pitch, packet.isOnGround(), packet.horizontalCollision()))
                else -> {}
            }
        }
    }

    @SubscribeEvent
    fun onPacketReceive(event: PacketEvent.Receive) {
        if (fullNullCheck()) {
            return
        }
        try {
            if (this.predictsBreak.getValue() && event.getPacket<Packet<*>>() is EntitySpawnS2CPacket) {
                val packet = event.getPacket<EntitySpawnS2CPacket>()
                if (packet.entityType == net.minecraft.entity.EntityType.END_CRYSTAL && this.hit2(BlockPos.ofFloored(packet.x, packet.y, packet.z))) {
                    val ent = mc.world!!.getEntityById(packet.entityId)
                    if (ent != null) {
                        mc.player!!.networkHandler.sendPacket(net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket.attack(ent, mc.player!!.isSneaking))
                    }
                }
            }

            if (event.getPacket<Packet<*>>() is ExplosionS2CPacket) {
                val packet = event.getPacket<ExplosionS2CPacket>()
                if (this.fastUpdate.getValue() && this.crystal != null && this.hit2(BlockPos.ofFloored(packet.center.x, packet.center.y, packet.center.z))) {
                    this.crystal = null
                }
            }
        } catch (var4: NullPointerException) {
        }
    }

    override fun onRender3D(event: Render3DEvent) {
        if (this.pos != null && this.target != null) {
            RenderUtil.drawBoxESP(
                this.pos!!,
                if (ClickGui.getInstance().rainbow.getValue()) ColorUtil.rainbow(ClickGui.getInstance().rainbowHue.getValue())
                else Color(this.red.getValue(), this.green.getValue(), this.blue.getValue(), this.alpha.getValue()),
                this.outline.getValue(),
                if (ClickGui.getInstance().rainbow.getValue()) ColorUtil.rainbow(ClickGui.getInstance().rainbowHue.getValue())
                else Color(this.cRed.getValue(), this.cGreen.getValue(), this.cBlue.getValue(), this.cAlpha.getValue()),
                this.lineWidth.getValue(),
                this.outline.getValue(),
                this.box.getValue(),
                this.boxAlpha.getValue(),
                true
            )
            val renderDamage = DamageUtil.calculateDamage(this.pos!!.x + 0.5, this.pos!!.y + 1.0, this.pos!!.z + 0.5, this.target!!)
            val color = if (renderDamage > 18.0) 16711680
            else if (renderDamage > 16.0) 16720896
            else if (renderDamage > 12.0) 16744192
            else if (renderDamage > 8.0) 16776960
            else if (renderDamage > 5.0) '\uffff'.code
            else '\uff00'.code
            if (this.renderDmg.getValue()) {
                RenderUtil.drawText(this.pos!!, (if (Math.floor(renderDamage.toDouble()) == renderDamage.toDouble()) renderDamage.toInt().toString() else String.format("%.1f", renderDamage)) + "", color)
            }
        }
    }

    override fun getDisplayInfo(): String? {
        if (this.target != null) {
            this.displaytarget = this.target!!.name.string
        }

        if (this.displaytarget != null) {
            if (AutoGG.getInstance().isOn()) {
                val duel = Moon.moduleManager!!.getModuleByName("AutoGG") as AutoGG
                duel.target = this.displaytarget
            }

            return this.displaytarget
        } else {
            return null
        }
    }

    private fun getTarget(): PlayerEntity? {
        val player = mc.player
        val world = mc.world
        if (player == null || world == null) {
            return null
        }
        var closestPlayer: PlayerEntity? = null

        for (entity in world.players) {
            if (player.isAlive
                && entity.isAlive
                && !(entity.health + entity.absorptionAmount <= 0.0f)
                && !entity.name.string.equals(player.name.string)
                && !Moon.friendManager!!.isFriend(entity.name.string)
                && !entity.isCreative
                && !EntityUtil.isInHole(entity)
                && !(player.distanceTo(entity) > this.targetRange.getValue().toFloat())
            ) {
                if (closestPlayer == null) {
                    closestPlayer = entity
                } else if (closestPlayer!!.distanceTo(player) > entity.distanceTo(player)) {
                    closestPlayer = entity
                }
            }
        }

        return closestPlayer
    }

    private fun hit1(p_Entity: Entity?): Boolean {
        if (p_Entity == null) {
            return false
        } else if (p_Entity !is EndCrystalEntity) {
            return false
        } else if (p_Entity.isRemoved) {
            return false
        } else if (this.target == null) {
            return false
        } else if (Math.sqrt(mc.player!!.squaredDistanceTo(p_Entity.x, p_Entity.y, p_Entity.z).toDouble()) > this.breakRange.getValue()) {
            return false
        } else if (!mc.player!!.canSee(p_Entity)
            && Math.sqrt(mc.player!!.squaredDistanceTo(p_Entity.x, p_Entity.y, p_Entity.z).toDouble()) > this.raytrace.getValue()
        ) {
            return false
        } else if (!this.target!!.isAlive && !(this.target!!.health + this.target!!.absorptionAmount <= 0.0f)) {
            return false
        } else {
            val selfDamage = DamageUtil.calculateDamage(p_Entity.x, p_Entity.y, p_Entity.z, mc.player!!)
            val targetDamage = DamageUtil.calculateDamage(p_Entity.x, p_Entity.y, p_Entity.z, this.target!!)
            return if (!(selfDamage + (if (this.antiSuicide.getValue()) 2.0 else 0.5) >= (mc.player!!.health + mc.player!!.absorptionAmount).toDouble())
                && !(selfDamage - 0.5 > this.maxSelfDmg.getValue())
            ) {
                targetDamage >= (this.target!!.health + this.target!!.absorptionAmount).toDouble() || targetDamage >= this.minBreakDmg.getValue()
            } else {
                false
            }
        }
    }

    private fun hit2(packet: BlockPos): Boolean {
        if (packet == null) {
            return false
        } else if (this.target == null) {
            return false
        } else if (Math.sqrt(mc.player!!.squaredDistanceTo(packet.x.toDouble(), packet.y.toDouble(), packet.z.toDouble()).toDouble()) > this.breakRange.getValue()) {
            return false
        } else if (!BlockUtil.canBlockBeSeen(packet.x.toDouble(), packet.y.toDouble(), packet.z.toDouble())
            && Math.sqrt(mc.player!!.squaredDistanceTo(packet.x.toDouble(), packet.y.toDouble(), packet.z.toDouble()).toDouble()) > this.raytrace.getValue()
        ) {
            return false
        } else if (!this.target!!.isAlive && !(this.target!!.health + this.target!!.absorptionAmount <= 0.0f)) {
            return false
        } else {
            val selfDamage = DamageUtil.calculateDamage(packet.x.toDouble(), packet.y.toDouble(), packet.z.toDouble(), mc.player!!)
            val targetDamage = DamageUtil.calculateDamage(packet.x.toDouble(), packet.y.toDouble(), packet.z.toDouble(), this.target!!)
            return if (!(selfDamage + (if (this.antiSuicide.getValue()) 2.0 else 0.5) >= (mc.player!!.health + mc.player!!.absorptionAmount).toDouble())
                && !(selfDamage - 0.5 > this.maxSelfDmg.getValue())
            ) {
                targetDamage >= (this.target!!.health + this.target!!.absorptionAmount).toDouble() || targetDamage >= this.minBreakDmg.getValue()
            } else {
                false
            }
        }
    }

    private fun possiblePlacePositions(placeRange: Float): List<BlockPos> {
        val positions = ArrayList<BlockPos>()
        positions.addAll(
            BlockUtil.getSphere(
                EntityUtil.getRoundedBlockPos(mc.player!!),
                placeRange,
                placeRange.toInt(),
                false,
                true,
                0
            ).filter { BlockUtil.canPlaceCrystal(it) }
        )
        positions.removeIf { blockPos ->
            if (!BlockUtil.canBlockBeSeen(blockPos.x + 0.5, (blockPos.y + 1).toDouble(), blockPos.z + 0.5)
                && Math.sqrt(mc.player!!.squaredDistanceTo(blockPos.x + 0.5, (blockPos.y + 1).toDouble(), blockPos.z + 0.5).toDouble()) > this.raytrace.getValue()
            ) {
                true
            } else {
                val selfDamage = DamageUtil.calculateDamage(blockPos.x + 0.5, blockPos.y + 1.0, blockPos.z + 0.5, mc.player!!)
                selfDamage + (if (this.antiSuicide.getValue()) 2.0 else 0.5) >= (mc.player!!.health + mc.player!!.absorptionAmount).toDouble()
                    || selfDamage - 0.5 > this.maxSelfDmg.getValue()
            }
        }
        return positions
    }

    enum class Settings {
        PLACE,
        BREAK,
        MISC,
        RENDER
    }

    enum class SwingMode {
        MainHand,
        OffHand
    }
}
