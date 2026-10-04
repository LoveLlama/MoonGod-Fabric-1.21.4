package me.moon.util

import me.moon.Moon
import me.moon.util.Util
import net.minecraft.block.Block
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.enchantment.Enchantments
import net.minecraft.screen.slot.Slot
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket
import net.minecraft.entity.EquipmentSlot
import java.util.HashMap

object InventoryUtil : Util {
    @JvmStatic
    fun switchToHotbarSlot(slot: Int, silent: Boolean) {
        if (mc.player!!.inventory.selectedSlot != slot && slot >= 0) {
            if (silent) {
                mc.player!!.networkHandler.sendPacket(UpdateSelectedSlotC2SPacket(slot))
            } else {
                mc.player!!.networkHandler.sendPacket(UpdateSelectedSlotC2SPacket(slot))
                mc.player!!.inventory.selectedSlot = slot
            }
        }
    }

    @JvmStatic
    fun switchToHotbarSlot(clazz: Class<*>, silent: Boolean) {
        val slot = findHotbarBlock(clazz)
        if (slot > -1) {
            switchToHotbarSlot(slot, silent)
        }
    }

    @JvmStatic
    fun isNull(stack: ItemStack?): Boolean {
        return stack == null || stack.isEmpty
    }

    @JvmStatic
    fun findHotbarBlock(clazz: Class<*>): Int {
        for (i in 0..8) {
            val stack = mc.player!!.inventory.getStack(i)
            if (!stack.isEmpty) {
                if (clazz.isInstance(stack.item)) {
                    return i
                }

                if (stack.item is BlockItem && clazz.isInstance((stack.item as BlockItem).block)) {
                    return i
                }
            }
        }

        return -1
    }

    @JvmStatic
    fun findHotbarBlock(blockIn: Block): Int {
        for (i in 0..8) {
            val stack = mc.player!!.inventory.getStack(i)
            if (!stack.isEmpty && stack.item is BlockItem && (stack.item as BlockItem).block == blockIn) {
                return i
            }
        }

        return -1
    }

    @JvmStatic
    fun getItemHotbar(input: Item): Int {
        for (i in 0..8) {
            val item = mc.player!!.inventory.getStack(i).item
            if (item == input) {
                return i
            }
        }

        return -1
    }

    @JvmStatic
    fun findStackInventory(input: Item): Int {
        return findStackInventory(input, false)
    }

    @JvmStatic
    fun findStackInventory(input: Item, withHotbar: Boolean): Int {
        var i = if (withHotbar) 0 else 9
        while (i < 36) {
            val item = mc.player!!.inventory.getStack(i).item
            if (input == item) {
                return i + if (i < 9) 36 else 0
            }
            i++
        }

        return -1
    }

    @JvmStatic
    fun findItemInventorySlot(item: Item, offHand: Boolean): Int {
        var slot = -1

        for (entry in getInventoryAndHotbarSlots().entries) {
            if (entry.value.item == item && (entry.key != 45 || offHand)) {
                slot = entry.key
                return slot
            }
        }

        return slot
    }

    @JvmStatic
    fun findEmptySlots(withXCarry: Boolean): MutableList<Int> {
        val outPut = ArrayList<Int>()

        for (entry in getInventoryAndHotbarSlots().entries) {
            if (entry.value.isEmpty || entry.value.item == Items.AIR) {
                outPut.add(entry.key)
            }
        }

        if (withXCarry) {
            for (i in 1..4) {
                val craftingSlot = mc.player!!.currentScreenHandler.slots[i]
                val craftingStack = craftingSlot.stack
                if (craftingStack.isEmpty || craftingStack.item == Items.AIR) {
                    outPut.add(i)
                }
            }
        }

        return outPut
    }

    @JvmStatic
    fun findInventoryBlock(clazz: Class<*>, offHand: Boolean): Int {
        var slot = -1

        for (entry in getInventoryAndHotbarSlots().entries) {
            if (isBlock(entry.value.item, clazz) && (entry.key != 45 || offHand)) {
                slot = entry.key
                return slot
            }
        }

        return slot
    }

    @JvmStatic
    fun isBlock(item: Item, clazz: Class<*>): Boolean {
        if (item is BlockItem) {
            val block = item.block
            return clazz.isInstance(block)
        } else {
            return false
        }
    }

    @JvmStatic
    fun confirmSlot(slot: Int) {
        mc.player!!.networkHandler.sendPacket(UpdateSelectedSlotC2SPacket(slot))
        mc.player!!.inventory.selectedSlot = slot
    }

    @JvmStatic
    fun getInventoryAndHotbarSlots(): Map<Int, ItemStack> {
        return getInventorySlots(9, 44)
    }

    private fun getInventorySlots(currentI: Int, last: Int): HashMap<Int, ItemStack> {
        val fullInventorySlots = HashMap<Int, ItemStack>()

        for (current in currentI..last) {
            fullInventorySlots[current] = mc.player!!.currentScreenHandler.slots[current].stack
        }

        return fullInventorySlots
    }

    @JvmStatic
    fun switchItemToItem(back: Boolean, lastHotbarSlot: Int, switchedItem: Boolean, mode: Switch, item: Item): BooleanArray {
        val switchedItemSwitched = booleanArrayOf(switchedItem, false)
        when (mode) {
            Switch.NORMAL -> {
                if (!back && !switchedItem) {
                    switchToHotbarSlot(getItemHotbar(item), false)
                    switchedItemSwitched[0] = true
                } else if (back && switchedItem) {
                    switchToHotbarSlot(lastHotbarSlot, false)
                    switchedItemSwitched[0] = false
                }

                switchedItemSwitched[1] = true
            }
            Switch.SILENT -> {
                if (!back && !switchedItem) {
                    switchToHotbarSlot(getItemHotbar(item), true)
                    switchedItemSwitched[0] = true
                } else if (back && switchedItem) {
                    switchedItemSwitched[0] = false
                    Moon.inventoryManager!!.recoverSilent(lastHotbarSlot)
                }

                switchedItemSwitched[1] = true
            }
            Switch.NONE -> switchedItemSwitched[1] = back || mc.player!!.inventory.selectedSlot == getItemHotbar(item)
        }

        return switchedItemSwitched
    }

    @JvmStatic
    fun holdingItem(clazz: Class<*>): Boolean {
        var result: Boolean
        val stack = mc.player!!.mainHandStack
        result = isInstanceOf(stack, clazz)
        if (!result) {
            result = isInstanceOf(stack, clazz)
        }

        return result
    }

    @JvmStatic
    fun isInstanceOf(stack: ItemStack?, clazz: Class<*>): Boolean {
        if (stack == null) {
            return false
        } else {
            val item = stack.item
            if (clazz.isInstance(item)) {
                return true
            } else if (item is BlockItem) {
                val block = item.block
                return clazz.isInstance(block)
            } else {
                return false
            }
        }
    }

    @JvmStatic
    fun getEmptyXCarry(): Int {
        for (i in 1..4) {
            val craftingSlot = mc.player!!.currentScreenHandler.slots[i]
            val craftingStack = craftingSlot.stack
            if (craftingStack.isEmpty || craftingStack.item == Items.AIR) {
                return i
            }
        }

        return -1
    }

    @JvmStatic
    fun isSlotEmpty(i: Int): Boolean {
        val slot = mc.player!!.currentScreenHandler.slots[i]
        val stack = slot.stack
        return stack.isEmpty
    }

    @JvmStatic
    fun convertHotbarToInv(input: Int): Int {
        return 36 + input
    }

    @JvmStatic
    fun areStacksCompatible(stack1: ItemStack, stack2: ItemStack): Boolean {
        if (stack1.item != stack2.item) {
            return false
        } else {
            if (stack1.item is BlockItem && stack2.item is BlockItem) {
                val block1 = (stack1.item as BlockItem).block
                val block2 = (stack2.item as BlockItem).block
                if (block1 != block2) {
                    return false
                }
            }

            if (stack1.name.string != stack2.name.string) {
                return false
            } else {
                return stack1.damage == stack2.damage
            }
        }
    }

    @JvmStatic
    fun getEquipmentFromSlot(slot: Int): EquipmentSlot {
        if (slot == 5) {
            return EquipmentSlot.HEAD
        } else if (slot == 6) {
            return EquipmentSlot.CHEST
        } else {
            return if (slot == 7) EquipmentSlot.LEGS else EquipmentSlot.FEET
        }
    }

    @JvmStatic
    fun findArmorSlot(type: EquipmentSlot, binding: Boolean): Int {
        var slot = -1
        var damage = 0.0f

        for (i in 9..44) {
            val s = mc.player!!.currentScreenHandler.slots[i].stack
            if (s.item != Items.AIR && s.item is net.minecraft.item.ArmorItem) {
                if (mc.player!!.getPreferredEquipmentSlot(s) == type) {
                    val currentDamage = armorScore(s, type)
                    val cursed = binding && enchantLevel(Enchantments.BINDING_CURSE, s) > 0
                    if (currentDamage > damage && !cursed) {
                        damage = currentDamage
                        slot = i
                    }
                }
            }
        }

        return slot
    }

    @JvmStatic
    fun findArmorSlot(type: EquipmentSlot, binding: Boolean, withXCarry: Boolean): Int {
        var slot = findArmorSlot(type, binding)
        if (slot == -1 && withXCarry) {
            var damage = 0.0f

            for (i in 1..4) {
                val craftingSlot: Slot = mc.player!!.currentScreenHandler.slots[i]
                val craftingStack = craftingSlot.stack
                if (craftingStack.item != Items.AIR && craftingStack.item is net.minecraft.item.ArmorItem) {
                    if (mc.player!!.getPreferredEquipmentSlot(craftingStack) == type) {
                        val currentDamage = armorScore(craftingStack, type)
                        val cursed = binding && enchantLevel(Enchantments.BINDING_CURSE, craftingStack) > 0
                        if (currentDamage > damage && !cursed) {
                            damage = currentDamage
                            slot = i
                        }
                    }
                }
            }
        }

        return slot
    }

    @JvmStatic
    fun findItemInventorySlot(item: Item, offHand: Boolean, withXCarry: Boolean): Int {
        var slot = findItemInventorySlot(item, offHand)
        if (slot == -1 && withXCarry) {
            for (i in 1..4) {
                val craftingSlot: Slot = mc.player!!.currentScreenHandler.slots[i]
                val craftingStack = craftingSlot.stack
                if (craftingStack.item != Items.AIR && craftingStack.item == item) {
                    slot = i
                }
            }
        }

        return slot
    }

    @JvmStatic
    fun findBlockSlotInventory(clazz: Class<*>, offHand: Boolean, withXCarry: Boolean): Int {
        var slot = findInventoryBlock(clazz, offHand)
        if (slot == -1 && withXCarry) {
            for (i in 1..4) {
                val craftingSlot: Slot = mc.player!!.currentScreenHandler.slots[i]
                val craftingStack = craftingSlot.stack
                if (craftingStack.item != Items.AIR) {
                    val craftingStackItem = craftingStack.item
                    if (clazz.isInstance(craftingStackItem)) {
                        slot = i
                    } else if (craftingStackItem is BlockItem && clazz.isInstance(craftingStackItem.block)) {
                        slot = i
                    }
                }
            }
        }

        return slot
    }

    private fun enchantLevel(key: net.minecraft.registry.RegistryKey<net.minecraft.enchantment.Enchantment>, stack: ItemStack): Int {
        val entry = mc.world!!.registryManager.getOrThrow(net.minecraft.registry.RegistryKeys.ENCHANTMENT).getOrThrow(key)
        return EnchantmentHelper.getLevel(entry, stack)
    }

    private fun armorScore(stack: ItemStack, slot: EquipmentSlot): Float {
        var score = enchantLevel(Enchantments.PROTECTION, stack).toFloat()
        val comp = stack.getOrDefault(
            net.minecraft.component.DataComponentTypes.ATTRIBUTE_MODIFIERS,
            net.minecraft.component.type.AttributeModifiersComponent.DEFAULT
        )
        for (e in comp.modifiers) {
            if (e.attribute == net.minecraft.entity.attribute.EntityAttributes.ARMOR) {
                score += e.modifier.value.toFloat()
            }
        }
        return score
    }

    enum class Switch {
        NORMAL,
        SILENT,
        NONE
    }

    class Task {
        private val slot: Int
        private val update: Boolean
        private val quickClick: Boolean

        constructor() {
            this.update = true
            this.slot = -1
            this.quickClick = false
        }

        constructor(slot: Int) {
            this.slot = slot
            this.quickClick = false
            this.update = false
        }

        constructor(slot: Int, quickClick: Boolean) {
            this.slot = slot
            this.quickClick = quickClick
            this.update = false
        }

        fun run() {
            if (this.slot != -1) {
                val m = net.minecraft.client.MinecraftClient.getInstance()
                m.interactionManager?.clickSlot(
                    m.player!!.currentScreenHandler.syncId, this.slot, 0,
                    if (this.quickClick) SlotActionType.QUICK_MOVE else SlotActionType.PICKUP, m.player!!
                )
            }
        }

        fun isSwitching(): Boolean {
            return !this.update
        }
    }
}
