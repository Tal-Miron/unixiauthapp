package com.unixi.authapp.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserData(

    @SerialName("user_id")
    val userId: String,

    @SerialName("full_name")
    val fullName: String,

    @SerialName("email")
    val email: String,

    @SerialName("company")
    val company: String,

    @SerialName("account_creation_date")
    val accountCreationDate: String,

    @SerialName("department")
    val department: String,

    @SerialName("account_status")
    val accountStatus: String,

    @SerialName("last_login_time")
    val lastLoginTime: String
)