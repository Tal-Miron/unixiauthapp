package com.unixi.authapp.util

import com.unixi.authapp.data.model.AppConfig
import kotlinx.serialization.json.Json
import java.util.Base64

object QrDecryptor {

    private const val secret = "UNIXI_QR_SECRET"

    private val json = Json {
        ignoreUnknownKeys = true
    }

    fun decrypt(rawValue: String): AppConfig {
        require(rawValue.isNotBlank()) {
            "QR value cannot be blank."
        }

        val decryptedJson = decryptPayload(rawValue)
        return json.decodeFromString<AppConfig>(decryptedJson)
    }

    private fun decryptPayload(rawValue: String): String {
        return try {
            val decodedBytes = Base64.getDecoder().decode(rawValue)
            val decodedText = decodedBytes.toString(Charsets.UTF_8)

            validateSecret(decodedText)

            decodedText.removePrefix("$secret:")
        } catch (exception: IllegalArgumentException) {
            throw IllegalArgumentException("Invalid QR payload format.", exception)
        }
    }

    private fun validateSecret(decodedText: String) {
        require(decodedText.startsWith("$secret:")) {
            "QR payload secret is invalid."
        }
    }
}