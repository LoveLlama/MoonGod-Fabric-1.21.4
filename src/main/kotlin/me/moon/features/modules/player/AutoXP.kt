package me.moon.features.modules.player

import me.moon.features.modules.Module
import me.moon.features.setting.Setting
import me.moon.util.InventoryUtil
import me.moon.util.Timer
import net.minecraft.entity.ExperienceOrbEntity
import net.minecraft.item.Items
import net.minecraft.util.Hand

class AutoXP : Module("AutoXP", "AutoXP", Module.Category.PLAYER, true, false, false) {
    private var toMend: Char = 0.toChar()
    private val timer = Timer()
    private val delay = register(Setting("XP Delay", 4, 1, 4))
    private val minDamage = register(Setting("Min Damage", 50, 0, 100))
    private val maxHeal = register(Setting("Repair To", 90, 0, 100))
    private val sneakOnly = register(Setting("Sneak Only", true))
    private val predict = register(Setting("Predict", true))

    override fun onUpdate() {
        if (!fullNullCheck()) {
            var sumOfDamage = 0
            val armor = mc.player!!.inventory.armor

            for (i in 0 until armor.size) {
                val itemStack = armor[i]
                if (!itemStack.isEmpty) {
                    val damageOnArmor = (itemStack.maxDamage - itemStack.damage).toFloat()
                    val damagePercent = 100.0f - 100.0f * (1.0f - damageOnArmor / itemStack.maxDamage.toFloat())
                    if (damagePercent <= this.maxHeal.getValue().toFloat()) {
                        if (damagePercent <= this.minDamage.getValue().toFloat()) {
                            this.toMend = (this.toMend.toInt() or (1 shl i)).toChar()
                        }
                        if (this.predict.getValue()) {
                            sumOfDamage += ((itemStack.maxDamage * this.maxHeal.getValue()) / 100.0 - (itemStack.maxDamage - itemStack.damage)).toInt()
                        }
                    } else {
                        this.toMend = (this.toMend.toInt() and (1 shl i).inv()).toChar()
                    }
                }
            }

            if (this.toMend.code > 0 && this.timer.passedMs((this.delay.getValue() * 45).toLong())) {
                this.timer.reset()
                if (this.predict.getValue()) {
                    val totalXp = mc.world!!.entities
                        .filterIsInstance<ExperienceOrbEntity>()
                        .filter { it.squaredDistanceTo(mc.player!!) <= 1.0 }
                        .sumOf { it.experienceAmount }
                    if (totalXp * 2 < sumOfDamage) {
                        this.mendArmor()
                    }
                } else {
                    this.mendArmor()
                }
            }
        }
    }

    private fun mendArmor() {
        if (!this.sneakOnly.getValue() || mc.player!!.isSneaking) {
            val a = InventoryUtil.getItemHotbar(Items.EXPERIENCE_BOTTLE)
            val b = mc.player!!.inventory.selectedSlot
            if (a != -1) {
                mc.player!!.inventory.selectedSlot = a
                mc.player!!.networkHandler.sendPacket(net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket(a))
                mc.interactionManager!!.interactItem(mc.player!!, Hand.MAIN_HAND)
                mc.player!!.inventory.selectedSlot = b
                mc.player!!.networkHandler.sendPacket(net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket(b))
            }
        }
    }
}
