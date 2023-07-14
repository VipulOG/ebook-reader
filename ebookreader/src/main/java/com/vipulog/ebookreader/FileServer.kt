package com.vipulog.ebookreader

import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.core.net.toFile
import fi.iki.elonen.NanoHTTPD
import java.io.FileInputStream
import java.io.IOException


internal class FileServer(port: Int = 8080) : NanoHTTPD(port) {
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
}