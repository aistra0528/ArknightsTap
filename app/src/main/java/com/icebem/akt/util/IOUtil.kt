package com.icebem.akt.util

import android.content.Context
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

object IOUtil {
    private const val LENGTH_KB = 1024
    private const val CONNECT_TIMEOUT = 5000
    private const val METHOD_GET = "GET"

    @Throws(IOException::class)
    fun fromAssets(context: Context, path: String): InputStream = context.assets.open(path)

    @Throws(IOException::class)
    fun fromFile(file: File): InputStream = FileInputStream(file)

    @Throws(IOException::class)
    fun fromWeb(url: String): InputStream {
        (URL(url).openConnection() as HttpURLConnection).run {
            requestMethod = METHOD_GET
            connectTimeout = CONNECT_TIMEOUT
            readTimeout = CONNECT_TIMEOUT
            return inputStream
        }
    }

    @Throws(IOException::class)
    private fun stream2Bytes(stream: InputStream): ByteArrayOutputStream {
        val out = ByteArrayOutputStream()
        val buffer = ByteArray(LENGTH_KB)
        var len: Int
        stream.use { while (stream.read(buffer).also { len = it } != -1) out.write(buffer, 0, len) }
        return out
    }

    @Throws(IOException::class)
    fun stream2String(stream: InputStream): String = stream2Bytes(stream).toString(StandardCharsets.UTF_8.name())

    @Throws(IOException::class)
    fun stream2File(stream: InputStream, path: String): File {
        val file = File(path)
        file.parentFile?.mkdirs()
        val out = FileOutputStream(file)
        stream2Bytes(stream).writeTo(out)
        out.close()
        return file
    }
}