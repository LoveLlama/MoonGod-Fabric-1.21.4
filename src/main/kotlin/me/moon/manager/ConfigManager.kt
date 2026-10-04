package me.moon.manager

import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import me.moon.Moon
import me.moon.features.Feature
import me.moon.features.modules.Module
import me.moon.features.setting.Bind
import me.moon.features.setting.EnumConverter
import me.moon.features.setting.Setting
import me.moon.util.Util
import java.io.BufferedWriter
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.nio.file.Files
import java.nio.file.Paths
import java.util.Scanner
import java.util.UUID

class ConfigManager : Util {
    val features: ArrayList<Feature> = ArrayList()
    var config: String = "moon/config/"

    private fun loadFile(input: JsonObject, feature: Feature) {
        for (entry in input.entrySet()) {
            val settingName = entry.key
            val element = entry.value
            if (feature is FriendManager) {
                try {
                    Moon.friendManager!!.addFriend(FriendManager.Friend(element.asString, UUID.fromString(settingName)))
                } catch (e: Exception) {
                }
            } else {
                var settingFound = false
                for (setting in feature.getSettings()) {
                    if (settingName == setting.getName()) {
                        try {
                            setValueFromJson(feature, setting, element)
                        } catch (e: Exception) {
                        }
                        settingFound = true
                    }
                }
            }
        }
    }

    fun loadConfig(name: String) {
        val files = (File("moon").listFiles() ?: emptyArray()).toList().filter { it.isDirectory }
        if (files.contains(File("moon/" + name + "/"))) {
            config = "moon/" + name + "/"
        } else {
            config = "moon/config/"
        }
        Moon.friendManager!!.onLoad()
        for (feature in features) {
            try {
                loadSettings(feature)
            } catch (e: Exception) {
            }
        }
        saveCurrentConfig()
    }

    fun configExists(name: String): Boolean {
        val files = (File("moon").listFiles() ?: emptyArray()).toList().filter { it.isDirectory }
        return files.contains(File("moon/" + name + "/"))
    }

    fun saveConfig(name: String) {
        config = "moon/" + name + "/"
        val path = File(config)
        if (!path.exists()) {
            path.mkdirs()
        }
        Moon.friendManager!!.saveFriends()
        for (feature in features) {
            try {
                saveSettings(feature)
            } catch (e: Exception) {
            }
        }
        saveCurrentConfig()
    }

    fun saveCurrentConfig() {
        val currentConfig = File("moon/currentconfig.txt")
        try {
            if (currentConfig.exists()) {
                val writer = java.io.FileWriter(currentConfig)
                var tempConfig = config.replace("/", "")
                writer.write(tempConfig.replace("moon", ""))
                writer.close()
            } else {
                currentConfig.createNewFile()
                val writer = java.io.FileWriter(currentConfig)
                var tempConfig = config.replace("/", "")
                writer.write(tempConfig.replace("moon", ""))
                writer.close()
            }
        } catch (e: Exception) {
        }
    }

    fun loadCurrentConfig(): String {
        val currentConfig = File("moon/currentconfig.txt")
        var name = "config"
        try {
            if (currentConfig.exists()) {
                val reader = Scanner(currentConfig)
                while (reader.hasNextLine()) {
                    name = reader.nextLine()
                }
                reader.close()
            }
        } catch (e: Exception) {
        }
        return name
    }

    fun resetConfig(saveConfig: Boolean, name: String) {
        for (feature in features) {
            feature.reset()
        }
        if (saveConfig) {
            saveConfig(name)
        }
    }

    fun saveSettings(feature: Feature) {
        JsonObject()
        val directory = File(config + getDirectory(feature))
        if (!directory.exists()) {
            directory.mkdirs()
        }
        val featureName = config + getDirectory(feature) + feature.getName() + ".json"
        val outputFile = Paths.get(featureName)
        if (!Files.exists(outputFile)) {
            Files.createFile(outputFile)
        }
        val gson = GsonBuilder().setPrettyPrinting().create()
        val json = gson.toJson(writeSettings(feature))
        val writer: BufferedWriter = BufferedWriter(OutputStreamWriter(Files.newOutputStream(outputFile)))
        writer.write(json)
        writer.close()
    }

    fun init() {
        features.addAll(Moon.moduleManager!!.modules)
        features.add(Moon.friendManager!!)
        val name = loadCurrentConfig()
        loadConfig(name)
        Moon.LOGGER.info("Config loaded.")
    }

    private fun loadSettings(feature: Feature) {
        val featureName = config + getDirectory(feature) + feature.getName() + ".json"
        val featurePath = Paths.get(featureName)
        if (Files.exists(featurePath)) {
            loadPath(featurePath, feature)
        }
    }

    private fun loadPath(path: java.nio.file.Path, feature: Feature) {
        val stream = Files.newInputStream(path)
        try {
            loadFile(JsonParser().parse(InputStreamReader(stream)).asJsonObject, feature)
        } catch (e: IllegalStateException) {
            Moon.LOGGER.error("Bad Config File for: " + feature.getName() + ". Resetting...")
            loadFile(JsonObject(), feature)
        } finally {
            stream.close()
        }
    }

    fun writeSettings(feature: Feature): JsonObject {
        val obj = JsonObject()
        val jp = JsonParser()
        for (setting in feature.getSettings()) {
            if (setting.isEnumSetting()) {
                val converter = EnumConverter((setting.getValue() as Enum<*>).javaClass)
                obj.add(setting.getName(), converter.doForward(setting.getValue() as Enum<*>))
            } else if (setting.getValue() is Bind) {
                obj.add(setting.getName(), Bind.BindConverter().doForward(setting.getValue() as Bind))
            } else if (setting.isStringSetting()) {
                obj.add(setting.getName(), JsonPrimitive(setting.getValueAsString().replace(" ", "_")))
            } else {
                try {
                    obj.add(setting.getName(), jp.parse(setting.getValueAsString()))
                } catch (e: Exception) {
                }
            }
        }
        return obj
    }

    fun getDirectory(feature: Feature): String {
        var directory = ""
        if (feature is Module) {
            directory = directory + feature.getCategory().getName() + "/"
        }
        return directory
    }

    companion object {
        @JvmStatic
        @Suppress("UNCHECKED_CAST")
        fun setValueFromJson(feature: Feature, setting: Setting<*>, element: JsonElement) {
            val s = setting as Setting<Any?>
            when (setting.getType()) {
                "Boolean" -> s.setValue(element.asBoolean)
                "Double" -> s.setValue(element.asDouble)
                "Float" -> s.setValue(element.asFloat)
                "Integer" -> s.setValue(element.asInt)
                "String" -> {
                    val str = element.asString
                    s.setValue(str.replace("_", " "))
                }
                "Bind" -> s.setValue(Bind.BindConverter().doBackward(element))
                "Enum" -> {
                    try {
                        val converter = EnumConverter((setting.getValue() as Enum<*>).javaClass)
                        val value = converter.doBackward(element)
                        s.setValue(if (value == null) setting.getDefaultValue() else value)
                    } catch (e: Exception) {
                    }
                }
                else -> Moon.LOGGER.error("Unknown Setting type for: " + feature.getName() + " : " + setting.getName())
            }
        }
    }
}
