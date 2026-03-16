package com.unixi.authapp.util

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.unixi.authapp.data.model.AppConfig
import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

object QrDecryptor {

    // ── Hardcoded secrets (dev only — replace with secure storage before production) ──
    // These must match the values in your Python .env file exactly.
    private const val HMAC_SECRET = "BgcH7pnhkQcXC6i-yeHy08oK_aF-P0Rki3nFGAYGYMPP6RAHjEDeDthUgi96cgzOcyMuOxpavEmjtU9KGmNKSA"
    private const val AES_KEY_HEX = "5e813bf93174d9b60263d3687825d218816f941e3b331e364ec8444f442a9ec3"

    private val aesKey: SecretKeySpec by lazy {
        val keyBytes = hexToBytes(AES_KEY_HEX)
        require(keyBytes.size == 32) { "AES_KEY must be 32 bytes (64 hex chars)." }
        SecretKeySpec(keyBytes, "AES")
    }

    // ── Public entry point ────────────────────────────────────────────────────

    /**
     * Verifies the JWT signature, then returns an AppConfig with:
     *   - qrToken : read directly from the JWT claim
     *   - proxy   : decrypted from the AES-256-GCM encrypted claim
     *
     * Throws [IllegalArgumentException] if the JWT is invalid or tampered.
     * Throws [JWTVerificationException] if the signature does not match.
     */
    fun decrypt(rawValue: String): AppConfig {
        require(rawValue.isNotBlank()) { "QR value cannot be blank." }

        val decodedJwt = verifyJwt(rawValue)

        val qrToken = decodedJwt.getClaim("qrToken").asString()
            ?: throw IllegalArgumentException("Missing qrToken claim.")

        val proxyEncrypted = decodedJwt.getClaim("proxy").asString()
            ?: throw IllegalArgumentException("Missing proxy claim.")

        val proxyAddress = decryptProxy(proxyEncrypted)

        return AppConfig(proxyAddress, qrToken)
    }

    // ── JWT verification ──────────────────────────────────────────────────────

    private fun verifyJwt(token: String) = try {
        val algorithm = Algorithm.HMAC256(HMAC_SECRET)
        val verifier = JWT.require(algorithm)
            .build()
        verifier.verify(token)
    } catch (e: JWTVerificationException) {
        throw JWTVerificationException("JWT signature verification failed: ${e.message}", e)
    }

    // ── AES-256-GCM decryption ────────────────────────────────────────────────

    /**
     * Decrypts a Base64url-encoded proxy address.
     *
     * Expected byte layout (mirrors Python's encrypt_proxy):
     *   [ 12-byte IV ][ ciphertext + 16-byte GCM auth tag ]
     *
     * GCM auth tag is verified automatically by the Cipher — if the
     * ciphertext was tampered with, decryption throws AEADBadTagException.
     */
    private fun decryptProxy(encoded: String): String {
        val raw = base64UrlDecode(encoded)

        require(raw.size > 12 + 16) {
            "Proxy payload too short to be valid AES-256-GCM ciphertext."
        }

        val iv             = raw.copyOfRange(0, 12)
        val ciphertextTag  = raw.copyOfRange(12, raw.size)  // ciphertext + 16-byte tag

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec   = GCMParameterSpec(128, iv)  // 128-bit auth tag
        cipher.init(Cipher.DECRYPT_MODE, aesKey, spec)

        return cipher.doFinal(ciphertextTag).toString(Charsets.UTF_8)
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Decodes a Base64url string that may or may not have padding.
     * Python strips '=' padding — we restore it before decoding.
     */
    private fun base64UrlDecode(input: String): ByteArray {
        val padded = when (input.length % 4) {
            2    -> "$input=="
            3    -> "$input="
            else -> input
        }
        return Base64.decode(padded, Base64.URL_SAFE)
    }

    private fun hexToBytes(hex: String): ByteArray {
        require(hex.length % 2 == 0) { "Hex string must have even length." }
        return ByteArray(hex.length / 2) { i ->
            hex.substring(i * 2, i * 2 + 2).toInt(16).toByte()
        }
    }
}