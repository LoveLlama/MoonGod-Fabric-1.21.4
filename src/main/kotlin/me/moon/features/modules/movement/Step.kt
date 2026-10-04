package me.moon.features.modules.movement

import me.moon.Moon
import me.moon.features.modules.Module
import me.moon.features.setting.Setting
import me.moon.util.MotionUtil
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket

class Step : Module("Step", "Step", Module.Category.MOVEMENT, true, false, false) {
    var height = register(Setting("Height", 2.5, 0.5, 2.5))

    override fun onTick() {
        if (!fullNullCheck()) {
            if (!mc.player!!.isTouchingWater
                && !mc.player!!.isInLava
                && !mc.player!!.isClimbing
                && !mc.options.jumpKey.isPressed()
            ) {
                if (Moon.moduleManager!!.getModuleByName("Speed")?.isOn() != true) {
                    val dir = MotionUtil.forward(0.1)
                    var twofive = false
                    var two = false
                    var onefive = false
                    var one = false
                    if (mc.world!!.getEntityCollisions(mc.player!!, mc.player!!.boundingBox.offset(dir[0], 2.6, dir[1])).isEmpty()
                        && !mc.world!!.getEntityCollisions(mc.player!!, mc.player!!.boundingBox.offset(dir[0], 2.4, dir[1])).isEmpty()
                    ) {
                        twofive = true
                    }

                    if (mc.world!!.getEntityCollisions(mc.player!!, mc.player!!.boundingBox.offset(dir[0], 2.1, dir[1])).isEmpty()
                        && !mc.world!!.getEntityCollisions(mc.player!!, mc.player!!.boundingBox.offset(dir[0], 1.9, dir[1])).isEmpty()
                    ) {
                        two = true
                    }

                    if (mc.world!!.getEntityCollisions(mc.player!!, mc.player!!.boundingBox.offset(dir[0], 1.6, dir[1])).isEmpty()
                        && !mc.world!!.getEntityCollisions(mc.player!!, mc.player!!.boundingBox.offset(dir[0], 1.4, dir[1])).isEmpty()
                    ) {
                        onefive = true
                    }

                    if (mc.world!!.getEntityCollisions(mc.player!!, mc.player!!.boundingBox.offset(dir[0], 1.0, dir[1])).isEmpty()
                        && !mc.world!!.getEntityCollisions(mc.player!!, mc.player!!.boundingBox.offset(dir[0], 0.6, dir[1])).isEmpty()
                    ) {
                        one = true
                    }

                    if (mc.player!!.horizontalCollision
                        && (mc.player!!.input.movementForward != 0.0f || mc.player!!.input.movementSideways != 0.0f)
                        && mc.player!!.isOnGround
                    ) {
                        if (one && this.height.getValue() >= 1.0) {
                            val oneOffset = doubleArrayOf(0.42, 0.753)
                            for (offset in oneOffset) {
                                mc.player!!.networkHandler.sendPacket(
                                    PlayerMoveC2SPacket.PositionAndOnGround(mc.player!!.x, mc.player!!.y + offset, mc.player!!.z, mc.player!!.isOnGround, mc.player!!.horizontalCollision)
                                )
                            }
                            mc.player!!.setPosition(mc.player!!.x, mc.player!!.y + 1.0, mc.player!!.z)
                        }

                        if (onefive && this.height.getValue() >= 1.5) {
                            val oneFiveOffset = doubleArrayOf(0.42, 0.75, 1.0, 1.16, 1.23, 1.2)
                            for (offset in oneFiveOffset) {
                                mc.player!!.networkHandler.sendPacket(
                                    PlayerMoveC2SPacket.PositionAndOnGround(mc.player!!.x, mc.player!!.y + offset, mc.player!!.z, mc.player!!.isOnGround, mc.player!!.horizontalCollision)
                                )
                            }
                            mc.player!!.setPosition(mc.player!!.x, mc.player!!.y + 1.5, mc.player!!.z)
                        }

                        if (two && this.height.getValue() >= 2.0) {
                            val twoOffset = doubleArrayOf(0.42, 0.78, 0.63, 0.51, 0.9, 1.21, 1.45, 1.43)
                            for (offset in twoOffset) {
                                mc.player!!.networkHandler.sendPacket(
                                    PlayerMoveC2SPacket.PositionAndOnGround(mc.player!!.x, mc.player!!.y + offset, mc.player!!.z, mc.player!!.isOnGround, mc.player!!.horizontalCollision)
                                )
                            }
                            mc.player!!.setPosition(mc.player!!.x, mc.player!!.y + 2.0, mc.player!!.z)
                        }

                        if (twofive && this.height.getValue() >= 2.5) {
                            val twoFiveOffset = doubleArrayOf(0.425, 0.821, 0.699, 0.599, 1.022, 1.372, 1.652, 1.869, 2.019, 1.907)
                            for (offset in twoFiveOffset) {
                                mc.player!!.networkHandler.sendPacket(
                                    PlayerMoveC2SPacket.PositionAndOnGround(mc.player!!.x, mc.player!!.y + offset, mc.player!!.z, mc.player!!.isOnGround, mc.player!!.horizontalCollision)
                                )
                            }
                            mc.player!!.setPosition(mc.player!!.x, mc.player!!.y + 2.5, mc.player!!.z)
                        }
                    }
                }
            }
        }
    }

    override fun onDisable() {
        mc.player?.getAttributeInstance(EntityAttributes.STEP_HEIGHT)?.baseValue = 0.6
    }
}
