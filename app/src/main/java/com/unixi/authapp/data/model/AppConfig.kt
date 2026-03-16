package com.unixi.authapp.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AppConfig(
    @SerialName("endpoint_url")
    val endpointUrl: String,
    @SerialName("qr_token")
    val qrToken: String
)