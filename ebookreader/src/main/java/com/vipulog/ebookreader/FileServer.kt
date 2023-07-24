package com.vipulog.ebookreader

import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.core.net.toFile
import fi.iki.elonen.NanoHTTPD
import java.io.FileInputStream
import java.io.IOException
import java.util.Random


internal class FileServer(port: Int = findAvailablePort()) : NanoHTTPD(port) {
    init {
        start(SOCKET_READ_TIMEOUT, false)
    }

    override fun serve(session: IHTTPSession): Response {
        val params = session.parameters
        val fileUrl = params["url"]?.firstOrNull()
        try {
            val file = Uri.parse(fileUrl).toFile()
            val fileInputStream = FileInputStream(file)
            val response = newFixedLengthResponse(
                Response.Status.OK,
                MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension),
                fileInputStream,
                file.length()
            )
            response.addHeader("Access-Control-Allow-Origin", "*")
            return response
        } catch (e: IOException) {
            e.printStackTrace()
            return newFixedLengthResponse(
                Response.Status.INTERNAL_ERROR,
                MIME_PLAINTEXT,
                "Error loading $fileUrl"
            )
        }
    }

    companion object {
        private const val MAX_RETRY = 5

        private fun findAvailablePort(): Int {
            val random = Random()
            var retry = 0
            var port: Int

            do {
                port = random.nextInt(16384) + 49152
                retry++
            } while (!isPortAvailable(port) && retry < MAX_RETRY)

            if (!isPortAvailable(port)) {
                throw RuntimeException("Couldn't find an available port after $MAX_RETRY retries.")
            }

            return port
        }

        private fun isPortAvailable(port: Int): Boolean {
            return try {
                val socket = java.net.ServerSocket(port)
                socket.close()
                true
            } catch (e: Exception) {
                false
            }
        }
    }
}