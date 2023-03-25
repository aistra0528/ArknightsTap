package com.icebem.akt.util

import android.os.Build
import com.icebem.akt.ArkApp.Companion.app
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import kotlin.io.path.*

object ArkIO {
    @Throws(IOException::class)
    private fun stream2String(stream: InputStream): String = stream.bufferedReader().use { it.readText() }

    @Throws(IOException::class)
    fun fromAssets(path: String): String = stream2String(app.assets.open(path))

    /**
     * This method is not recommended on huge files. It has an internal limitation of 2 GB file size.
     */
    @Throws(IOException::class)
    fun fromFile(path: String): String = File(path).readText()

    @Throws(IOException::class)
    suspend fun fromWeb(url: String): String = withContext(Dispatchers.IO) {
        (URL(url).openConnection() as HttpURLConnection).run {
            if (url.startsWith("https://gitee.com")) addRequestProperty("User-Agent", "Mozilla/5.0")
            stream2String(inputStream)
        }
    }

    @Throws(IOException::class)
    fun exists(path: String): Boolean = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> Files.exists(Paths.get(path))
        else -> File(path).exists()
    }

    @Throws(IOException::class)
    private fun createDirectories(dir: String): Boolean = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> Files.createDirectories(Paths.get(dir)).exists()
        else -> File(dir).mkdirs()
    }

    @Throws(IOException::class)
    fun clearDirectory(path: String) {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> Files.list(Paths.get(path)).forEach {
                if (it.isDirectory()) clearDirectory(it.pathString)
                it.deleteExisting()
            }
            else -> File(path).listFiles()?.forEach {
                if (it.isDirectory) clearDirectory(it.path)
                it.delete()
            }
        }
    }

    @Throws(IOException::class)
    fun delete(path: String): Boolean = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> Files.deleteIfExists(Paths.get(path))
        else -> File(path).delete()
    }

    @Throws(IOException::class)
    fun copy(source: String, target: String): File {
        val file = File(target)
        file.parent?.let { createDirectories(it) }
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> Files.copy(Paths.get(source), Paths.get(target), StandardCopyOption.REPLACE_EXISTING)
            else -> FileInputStream(source).channel.use {
                FileOutputStream(target).channel.use { out ->
                    out.transferFrom(it, 0, it.size())
                }
            }
        }
        return file
    }

    @Throws(IOException::class)
    fun writeText(path: String, text: String) {
        val file = File(path)
        file.parent?.let { createDirectories(it) }
        file.writeText(text)
    }
}