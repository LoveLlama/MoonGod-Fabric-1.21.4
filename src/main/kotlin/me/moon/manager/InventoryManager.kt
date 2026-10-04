package me.moon.manager

import me.moon.util.Util
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket

class InventoryManager : Util {
    var currentPlayerItem: Int = 0
    private var recoverySlot = -1

    fun update() {
        if (recoverySlot != -1) {
            mc.player!!.networkHandler.sendPacket(UpdateSelectedSlotC2SPacket(if (recoverySlot == 8) 7 else recoverySlot + 1))
            mc.player!!.networkHandler.sendPacket(UpdateSelectedSlotC2SPacket(recoverySlot))
            mc.player!!.inventory.selectedSlot = recoverySlot
            val i = mc.player!!.inventory.selectedSlot
            if (i != currentPlayerItem) {
                currentPlayerItem = i
                mc.player!!.networkHandler.sendPacket(UpdateSelectedSlotC2SPacket(currentPlayerItem))
            }
            recoverySlot = -1
        }
    }

    fun recoverSilent(slot: Int) {
        recoverySlot = slot
    }
}
