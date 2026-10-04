package me.moon.features.modules.combat

import me.moon.Moon
import me.moon.features.modules.Module
import me.moon.features.setting.Setting
import me.moon.util.InventoryUtil
import me.moon.util.Timer
import net.minecraft.client.gui.screen.ingame.InventoryScreen
import net.minecraft.entity.EquipmentSlot
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import java.util.HashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.function.Predicate

class AutoArmor : Module("AutoArmor", "Puts Armor on for you.", Module.Category.COMBAT, true, false, false) {
    private val delay = register(Setting("Delay", 50, 0, 500))
    private val curse = register(Setting("Vanishing", false))
    private val mendingTakeOff = register(Setting("AutoMend", false))
    private val closestEnemy = register(Setting("Enemy", 8, 1, 20, Predicate { mendingTakeOff.getValue() }))
    private val repair = register(Setting("Repair%", 80, 1, 100, Predicate { mendingTakeOff.getValue() }))
    private val actions = register(Setting("Packets", 3, 1, 12))
    private val timer = Timer()
    private val taskList = ConcurrentLinkedQueue<InventoryUtil.Task>()
    private val doneSlots = ArrayList<Int>()
    @JvmField
    var flag = false

    override fun onLogin() {
        this.timer.reset()
    }

    override fun onDisable() {
        this.taskList.clear()
        this.doneSlots.clear()
        this.flag = false
    }

    override fun onLogout() {
        this.taskList.clear()
        this.doneSlots.clear()
    }

    override fun onTick() {
        if (!fullNullCheck() && (mc.currentScreen == null || mc.currentScreen is InventoryScreen)) {
            if (this.taskList.isEmpty()) {
                if (this.mendingTakeOff.getValue()
                    && mc.player!!.mainHandStack.item == Items.EXPERIENCE_BOTTLE
                    && mc.options.useKey.isPressed()
                    && mc.world!!.players.none { e ->
                        e != mc.player!!
                            && !Moon.friendManager!!.isFriend(e.name.string)
                            && mc.player!!.distanceTo(e) <= this.closestEnemy.getValue().toFloat()
                    }
                    && !this.flag
                ) {
                    var takeOff = 0

                    for (armorSlot in this.getArmor().entries) {
                        val stack = armorSlot.value
                        val percent = this.repair.getValue() / 100.0f
                        val dam = Math.round(stack.maxDamage * percent)
                        if (dam < stack.maxDamage - stack.damage) {
                            ++takeOff
                        }
                    }

                    if (takeOff == 4) {
                        this.flag = true
                    }

                    if (!this.flag) {
                        val itemStack1 = mc.player!!.currentScreenHandler.slots[5].stack
                        if (!itemStack1.isEmpty) {
                            val percent = this.repair.getValue() / 100.0f
                            val dam2 = Math.round(itemStack1.maxDamage * percent)
                            if (dam2 < itemStack1.maxDamage - itemStack1.damage) {
                                this.takeOffSlot(5)
                            }
                        }

                        val itemStack2 = mc.player!!.currentScreenHandler.slots[6].stack
                        if (!itemStack2.isEmpty) {
                            val percent = this.repair.getValue() / 100.0f
                            val dam3 = Math.round(itemStack2.maxDamage * percent)
                            if (dam3 < itemStack2.maxDamage - itemStack2.damage) {
                                this.takeOffSlot(6)
                            }
                        }

                        val itemStack3 = mc.player!!.currentScreenHandler.slots[7].stack
                        if (!itemStack3.isEmpty) {
                            val percent = this.repair.getValue() / 100.0f
                            val dam = Math.round(itemStack3.maxDamage * percent)
                            if (dam < itemStack3.maxDamage - itemStack3.damage) {
                                this.takeOffSlot(7)
                            }
                        }

                        val itemStack4 = mc.player!!.currentScreenHandler.slots[8].stack
                        if (!itemStack4.isEmpty) {
                            val percent = this.repair.getValue() / 100.0f
                            val dam4 = Math.round(itemStack4.maxDamage * percent)
                            if (dam4 < itemStack4.maxDamage - itemStack4.damage) {
                                this.takeOffSlot(8)
                            }
                        }
                    }

                    return
                }

                this.flag = false
                val helm = mc.player!!.currentScreenHandler.slots[5].stack
                val slot4 = InventoryUtil.findArmorSlot(EquipmentSlot.HEAD, this.curse.getValue(), true)
                if (helm.item == Items.AIR && slot4 != -1) {
                    this.getSlotOn(5, slot4)
                }

                val slot3 = InventoryUtil.findArmorSlot(EquipmentSlot.CHEST, this.curse.getValue(), true)
                if (mc.player!!.currentScreenHandler.slots[6].stack.item == Items.AIR && slot3 != -1) {
                    this.getSlotOn(6, slot3)
                }

                val slot2 = InventoryUtil.findArmorSlot(EquipmentSlot.LEGS, this.curse.getValue(), true)
                if (mc.player!!.currentScreenHandler.slots[7].stack.item == Items.AIR && slot2 != -1) {
                    this.getSlotOn(7, slot2)
                }

                val slot = InventoryUtil.findArmorSlot(EquipmentSlot.FEET, this.curse.getValue(), true)
                if (mc.player!!.currentScreenHandler.slots[8].stack.item == Items.AIR && slot != -1) {
                    this.getSlotOn(8, slot)
                }
            }

            if (this.timer.passedMs((this.delay.getValue() * Moon.serverManager!!.getTpsFactor()).toLong())) {
                if (!this.taskList.isEmpty()) {
                    for (i in 0 until this.actions.getValue()) {
                        val task = this.taskList.poll()
                        if (task != null) {
                            task.run()
                        }
                    }
                }

                this.timer.reset()
            }
        }
    }

    private fun takeOffSlot(slot: Int) {
        if (this.taskList.isEmpty()) {
            var target = -1

            for (i in InventoryUtil.findEmptySlots(true)) {
                if (!this.doneSlots.contains(target)) {
                    target = i
                    this.doneSlots.add(i)
                }
            }

            if (target != -1) {
                this.taskList.add(InventoryUtil.Task(slot))
                this.taskList.add(InventoryUtil.Task(target))
                this.taskList.add(InventoryUtil.Task())
            }
        }
    }

    private fun getSlotOn(slot: Int, target: Int) {
        if (this.taskList.isEmpty()) {
            this.doneSlots.remove(Integer.valueOf(target))
            this.taskList.add(InventoryUtil.Task(target))
            this.taskList.add(InventoryUtil.Task(slot))
            this.taskList.add(InventoryUtil.Task())
        }
    }

    private fun getArmor(): Map<Int, ItemStack> {
        return this.getInventorySlots(5, 8)
    }

    private fun getInventorySlots(currentIn: Int, last: Int): HashMap<Int, ItemStack> {
        val fullInventorySlots = HashMap<Int, ItemStack>()
        var current = currentIn
        while (current <= last) {
            fullInventorySlots[current] = mc.player!!.currentScreenHandler.slots[current].stack
            current++
        }

        return fullInventorySlots
    }
}
