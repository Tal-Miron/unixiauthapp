package com.unixi.authapp.data.model

data class DeviceInfo(
    val model: String,
    val manufacturer: String,
    val osVersion: String,
    val sdkVersion: Int,
    val language: String,
    val os: String,
    val appVersion: String
)