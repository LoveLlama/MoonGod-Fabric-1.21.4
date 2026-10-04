package me.moon.manager

import me.moon.features.Feature
import me.moon.features.setting.Setting
import me.moon.util.PlayerUtil
import net.minecraft.entity.player.PlayerEntity
import java.util.UUID

class FriendManager : Feature("Friends") {
    private var friends: MutableList<Friend> = ArrayList()

    fun isFriend(name: String): Boolean {
        cleanFriends()
        return friends.stream().anyMatch { friend -> friend.getUsername().equals(name, ignoreCase = true) }
    }

    fun isFriend(player: PlayerEntity): Boolean {
        return isFriend(player.name.string)
    }

    fun addFriend(name: String) {
        val friend = getFriendByName(name)
        if (friend != null) {
            friends.add(friend)
        }
        cleanFriends()
    }

    fun removeFriend(name: String) {
        cleanFriends()
        for (friend in friends) {
            if (friend.getUsername().equals(name, ignoreCase = true)) {
                friends.remove(friend)
                break
            }
        }
    }

    fun onLoad() {
        friends = ArrayList()
        clearSettings()
    }

    fun saveFriends() {
        clearSettings()
        cleanFriends()
        for (friend in friends) {
            register(Setting(friend.getUuid().toString(), friend.getUsername()))
        }
    }

    fun cleanFriends() {
        friends.stream().filter { it != null }.filter { it.getUsername() != null }
    }

    fun getFriends(): MutableList<Friend> {
        cleanFriends()
        return friends
    }

    fun getFriendByName(input: String): Friend? {
        val uuid = PlayerUtil.getUUIDFromName(input)
        return if (uuid != null) Friend(input, uuid) else null
    }

    fun addFriend(friend: Friend) {
        friends.add(friend)
    }

    class Friend(username: String, uuid: UUID) {
        private val username: String = username
        private val uuid: UUID = uuid

        fun getUsername(): String = username

        fun getUuid(): UUID = uuid
    }
}
