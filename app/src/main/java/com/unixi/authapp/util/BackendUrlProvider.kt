package com.unixi.authapp.util

class BackendUrlProvider {

    fun buildResolveQrUrl(endpoint: String): String {
        val validatedEndpoint = validateEndpoint(endpoint)
        return "$validatedEndpoint/qr/resolve"
    }

    fun buildValidatePasswordUrl(endpoint: String): String {
        val validatedEndpoint = validateEndpoint(endpoint)
        return "$validatedEndpoint/auth/validate"
    }

    fun buildHealthUrl(endpoint: String): String {
        val validatedEndpoint = validateEndpoint(endpoint)
        return "$validatedEndpoint/health"
    }

    private fun validateEndpoint(endpoint: String): String {
        val normalizedEndpoint = endpoint.trim().trimEnd('/')

        require(normalizedEndpoint.isNotBlank()) {
            "Backend endpoint cannot be blank."
        }

        require(normalizedEndpoint.startsWith("https://")) {
            "Backend endpoint must use HTTPS."
        }

        return normalizedEndpoint
    }
}