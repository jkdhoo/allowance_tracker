package com.hooware.allowancetracker.notifications

import android.os.Handler
import android.os.HandlerThread
import com.hooware.allowancetracker.AllowanceApp
import com.hooware.allowancetracker.utils.FirebaseConfigRetriever
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

object SendFCMNotification {

    fun execute(application: AllowanceApp, token: String, body: String, title: String) {
        SaveNotificationIfNecessary.execute(application, to = token, body = body, title = title)
        val handler = HandlerThread("URLConnection")
        handler.start()
        val mainHandler = Handler(handler.looper)

        val myRunnable = Runnable {
            try {
                val apiKey = FirebaseConfigRetriever.execute("firebase_fcm_key")
                val url = URL("https://fcm.googleapis.com/fcm/send")
                val conn: HttpURLConnection = url.openConnection() as HttpURLConnection
                conn.doOutput = true
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.setRequestProperty("Authorization", "key=$apiKey")
                val message = JSONObject()
                message.put("to", token)
                message.put("priority", "high")
                val notification = JSONObject()
                notification.put("title", "\"$title\"")
                notification.put("body", "\"$body\"")
                message.put("data", notification)
                val os = conn.outputStream
                os.write(message.toString().toByteArray())
                os.flush()
                os.close()
                val responseCode: Int = conn.responseCode
                println("\nSending 'POST' request to URL : $url")
                println("Post parameters : $message")
                println("Response Code : $responseCode")
                println("Response Code : " + conn.responseMessage)
                val `in` = BufferedReader(InputStreamReader(conn.inputStream))
                var inputLine: String?
                val response = StringBuffer()
                while (`in`.readLine().also { inputLine = it } != null) {
                    response.append(inputLine)
                }
                `in`.close()

                // print result
                println(response.toString())
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
        mainHandler.post(myRunnable)
    }
}