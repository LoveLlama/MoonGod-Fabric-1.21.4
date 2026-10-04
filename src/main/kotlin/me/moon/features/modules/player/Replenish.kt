package me.moon.features.modules.player

import me.moon.features.modules.Module
import me.moon.features.setting.Setting
import me.moon.util.Timer
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.screen.slot.SlotActionType

class Replenish : Module("Replenish", "Replenishes your hotbar", Module.Category.PLAYER, false, false, false) {
    private val delay = register(Setting("Delay", 0, 0, 10))
    private val stack = register(Setting("Stack", 50, 50, 64))
    private val timer = Timer()
    private val hotbar: MutableList<Item> = ArrayList()

    override fun onEnable() {
        if (!fullNullCheck()) {
            this.hotbar.clear()
            for (i in 0..8) {
                val stack = mc.player!!.inventory.getStack(i)
                if (!stack.isEmpty && !this.hotbar.contains(stack.item)) {
                    this.hotbar.add(stack.item)
                } else {
                    this.hotbar.add(Items.AIR)
                }
            }
        }
    }

    override fun onUpdate() {
        if (mc.currentScreen == null) {
            if (this.timer.passedMs((this.delay.getValue() * 1000).toLong())) {
                for (i in 0..8) {
                    if (this.refillSlotIfNeed(i)) {
                        this.timer.reset()
                        return
                    }
                }
            }
        }
    }

    private fun refillSlotIfNeed(pSlot: Int): Boolean {
        val stack = mc.player!!.inventory.getStack(pSlot)
        if (!stack.isEmpty && stack.item != Items.AIR) {
            if (!stack.isStackable) {
                return false
            } else if (stack.count >= stack.maxCount) {
                return false
            } else if (stack.count >= this.stack.getValue()) {
                return false
            } else {
                for (i in 9..35) {
                    val item = mc.player!!.inventory.getStack(i)
                    if (!item.isEmpty && this.canItemBeMergedWith(stack, item)) {
                        mc.interactionManager!!.clickSlot(mc.player!!.currentScreenHandler.syncId, i, 0, SlotActionType.QUICK_MOVE, mc.player!!)
                        return true
                    }
                }
                return false
            }
        }
        return false
    }

    private fun canItemBeMergedWith(pSource: ItemStack, pTarget: ItemStack): Boolean {
        return pSource.item == pTarget.item && pSource.name.string == pTarget.name.string
    }
}
