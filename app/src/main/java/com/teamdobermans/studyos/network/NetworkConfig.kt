package com.teamdobermans.studyos.network

import android.os.Build

object NetworkConfig {

    private const val PORT = "8000"

                private const val LAN_IP = "192.168.1.143"


    private val isEmulator: Boolean
        get() = Build.FINGERPRINT.startsWith("generic")
            || Build.FINGERPRINT.startsWith("unknown")
            || Build.MODEL.contains("google_sdk", ignoreCase = true)
            || Build.MODEL.contains("Emulator", ignoreCase = true)
            || Build.MODEL.contains("Android SDK built for x86", ignoreCase = true)
            || Build.MANUFACTURER.contains("Genymotion", ignoreCase = true)
            || Build.HARDWARE.contains("ranchu")               || Build.HARDWARE.contains("goldfish")
            val BASE_URL: String
        get() = if (isEmulator) {
            "http://10.0.2.2:$PORT/"
        } else {
            "http://$LAN_IP:$PORT/"
        }
}
