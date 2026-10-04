package me.moon.util

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import me.moon.features.command.Command
import net.minecraft.client.network.PlayerListEntry
import net.minecraft.client.MinecraftClient
import java.io.BufferedInputStream
import java.io.DataOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.ArrayList
import java.util.Arrays
import java.util.Collections
import java.util.Date
import java.util.Scanner
import java.util.UUID

object PlayerUtil : Util {
    private val PARSER = JsonParser()
    private val GSON = com.google.gson.Gson()

    @JvmStatic
    fun getNameFromUUID(uuid: UUID): String? {
        return try {
            val process = lookUpName(uuid)
            val thread = Thread(process)
            thread.start()
            thread.join()
            process.getName()
        } catch (var3: Exception) {
            null
        }
    }

    @JvmStatic
    fun getNameFromUUID(uuid: String): String? {
        return try {
            val process = lookUpName(uuid)
            val thread = Thread(process)
            thread.start()
            thread.join()
            process.getName()
        } catch (var3: Exception) {
            null
        }
    }

    @JvmStatic
    fun getUUIDFromName(name: String): UUID? {
        return try {
            val process = lookUpUUID(name)
            val thread = Thread(process)
            thread.start()
            thread.join()
            process.getUUID()
        } catch (var3: Exception) {
            null
        }
    }

    @JvmStatic
    fun requestIDs(data: String): String? {
        return try {
            val query = "https://api.mojang.com/profiles/minecraft"
            val url = URL(query)
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 5000
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            conn.doOutput = true
            conn.doInput = true
            conn.requestMethod = "POST"
            val os: OutputStream = conn.outputStream
            os.write(data.toByteArray(StandardCharsets.UTF_8))
            os.close()
            val inn = BufferedInputStream(conn.inputStream)
            val res = convertStreamToString(inn)
            inn.close()
            conn.disconnect()
            res
        } catch (var7: Exception) {
            null
        }
    }

    @JvmStatic
    fun convertStreamToString(input: InputStream): String {
        val s = Scanner(input).useDelimiter("\\A")
        return if (s.hasNext()) s.next() else "/"
    }

    @JvmStatic
    fun getHistoryOfNames(id: UUID): List<String>? {
        return try {
            val array = getResources(URL("https://api.mojang.com/user/profiles/" + getIdNoHyphens(id) + "/names"), "GET")!!.asJsonArray
            val temp = ArrayList<String>()

            for (e: JsonElement in array) {
                val node = e.asJsonObject
                val name = node.get("name").asString
                val changedAt = if (node.has("changedToAt")) node.get("changedToAt").asLong else 0L
                temp.add(name + "§8" + Date(changedAt).toString())
            }

            Collections.sort(temp)
            temp
        } catch (var9: Exception) {
            null
        }
    }

    @JvmStatic
    fun getIdNoHyphens(uuid: UUID): String {
        return uuid.toString().replace("-".toRegex(), "")
    }

    @Throws(Exception::class)
    private fun getResources(url: URL, request: String): JsonElement? {
        return getResources(url, request, null)
    }

    @Throws(Exception::class)
    private fun getResources(url: URL, request: String, element: JsonElement?): JsonElement {
        var connection: javax.net.ssl.HttpsURLConnection? = null

        try {
            connection = url.openConnection() as javax.net.ssl.HttpsURLConnection
            connection.doOutput = true
            connection.requestMethod = request
            connection.setRequestProperty("Content-Type", "application/json")
            if (element != null) {
                val output = DataOutputStream(connection.outputStream)
                output.writeBytes(GSON.toJson(element))
                output.close()
            }

            val scanner = Scanner(connection.inputStream)
            val builder = StringBuilder()

            while (scanner.hasNextLine()) {
                builder.append(scanner.nextLine())
                builder.append('\n')
            }

            scanner.close()
            val json = builder.toString()
            return PARSER.parse(json)
        } finally {
            connection?.disconnect()
        }
    }

    class lookUpName(input: String) : Runnable {
        private val uuid: String
        private val uuidID: UUID
        @Volatile
        @JvmField
        var name: String? = null

        constructor(input: UUID) : this(input.toString()) {
        }

        init {
            this.uuid = input
            this.uuidID = UUID.fromString(input)
        }

        override fun run() {
            name = lookUpName()
        }

        fun lookUpName(): String? {
            var player: net.minecraft.entity.player.PlayerEntity? = null
            if (net.minecraft.client.MinecraftClient.getInstance().world != null) {
                player = net.minecraft.client.MinecraftClient.getInstance().world!!.getPlayerByUuid(uuidID)
            }

            if (player == null) {
                val url = "https://api.mojang.com/user/profiles/" + uuid.replace("-", "") + "/names"

                try {
                    val nameJson = URL(url).readText()
                    if (nameJson.contains(",")) {
                        val names: List<String> = Arrays.asList(*nameJson.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
                        Collections.reverse(names)
                        return names[1].replace("{\"name\":\"", "").replace("\"", "")
                    } else {
                        return nameJson.replace("[{\"name\":\"", "").replace("\"}]", "")
                    }
                } catch (var5: java.io.IOException) {
                    var5.printStackTrace()
                    return null
                }
            } else {
                return player.gameProfile.name
            }
        }

        fun getName(): String? {
            return name
        }
    }

    class lookUpUUID(private val name: String) : Runnable {
        @Volatile
        var uuid: UUID? = null
            private set

        override fun run() {
            var profile: PlayerListEntry?
            try {
                val infoMap = ArrayList<PlayerListEntry>(net.minecraft.client.MinecraftClient.getInstance().networkHandler!!.playerList)
                profile = infoMap.firstOrNull { networkPlayerInfo: PlayerListEntry -> networkPlayerInfo.profile.name.equals(name, ignoreCase = true) }

                uuid = profile!!.profile.id
            } catch (var6: Exception) {
                profile = null
            }

            if (profile == null) {
                Command.sendMessage("Player isn't online. Looking up UUID..")
                val s = requestIDs("[\"$name\"]")
                if (s != null && s.isNotEmpty()) {
                    val element = JsonParser().parse(s)
                    if (element.asJsonArray.size() == 0) {
                        Command.sendMessage("Couldn't find player ID. (1)")
                    } else {
                        try {
                            val id = element.asJsonArray.get(0).asJsonObject.get("id").asString
                            uuid = UUID.fromString(id)
                        } catch (var5: Exception) {
                            var5.printStackTrace()
                            Command.sendMessage("Couldn't find player ID. (2)")
                        }
                    }
                } else {
                    Command.sendMessage("Couldn't find player ID. Are you connected to the internet? (0)")
                }
            }
        }

        fun getUUID(): UUID? {
            return uuid
        }

        fun getName(): String {
            return name
        }
    }
}
