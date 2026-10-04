package me.moon.manager

import me.moon.Moon
import me.moon.features.Feature
import me.moon.features.modules.Module
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

class FileManager : Feature() {
    private val base: Path = getMkDirectory(getRoot(), "moon")
    private val config: Path = getMkDirectory(base, "config")

    init {
        getMkDirectory(base, "pvp")
        for (category in Moon.moduleManager!!.getCategories()) {
            getMkDirectory(config, category.getName())
        }
    }

    private fun expandPath(fullPath: String): Array<String> {
        return fullPath.split(":?\\\\|/".toRegex()).toTypedArray()
    }

    private fun expandPaths(vararg paths: String): List<String> {
        return paths.flatMap { expandPath(it).toList() }
    }

    private fun lookupPath(root: Path, vararg paths: String): Path {
        return Paths.get(root.toString(), *paths)
    }

    private fun getRoot(): Path = Paths.get("")

    private fun createDirectory(dir: Path) {
        try {
            if (!Files.isDirectory(dir)) {
                if (Files.exists(dir)) {
                    Files.delete(dir)
                }
                Files.createDirectories(dir)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getMkDirectory(parent: Path, vararg paths: String): Path {
        if (paths.size < 1) {
            return parent
        }
        val dir = lookupPath(parent, *paths)
        createDirectory(dir)
        return dir
    }

    fun getBasePath(): Path = base

    fun getBaseResolve(vararg paths: String): Path {
        val names = expandPaths(*paths).toTypedArray()
        if (names.size < 1) {
            throw IllegalArgumentException("missing path")
        }
        return lookupPath(getBasePath(), *names)
    }

    fun getMkBaseResolve(vararg paths: String): Path {
        val path = getBaseResolve(*paths)
        createDirectory(path.parent)
        return path
    }

    fun getConfig(): Path = getBasePath().resolve("config")

    fun getCache(): Path = getBasePath().resolve("cache")

    fun getMkBaseDirectory(vararg names: String): Path {
        return getMkDirectory(getBasePath(), expandPaths(*names).joinToString(File.separator))
    }

    fun getMkConfigDirectory(vararg names: String): Path {
        return getMkDirectory(getConfig(), expandPaths(*names).joinToString(File.separator))
    }

    companion object {
        @JvmStatic
        fun appendTextFile(data: String, file: String): Boolean {
            try {
                val path = Paths.get(file)
                Files.write(
                    path,
                    listOf(data),
                    StandardCharsets.UTF_8,
                    if (Files.exists(path)) StandardOpenOption.APPEND else StandardOpenOption.CREATE
                )
                return true
            } catch (e: Exception) {
                println("WARNING: Unable to write file: " + file)
                return false
            }
        }

        @JvmStatic
        fun readTextFileAllLines(file: String): List<String> {
            try {
                val path = Paths.get(file)
                return Files.readAllLines(path, StandardCharsets.UTF_8)
            } catch (e: Exception) {
                println("WARNING: Unable to read file, creating new file: " + file)
                appendTextFile("", file)
                return emptyList()
            }
        }
    }
}
