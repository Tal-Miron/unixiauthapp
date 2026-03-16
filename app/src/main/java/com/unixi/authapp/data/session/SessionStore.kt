package com.unixi.authapp.data.session

import com.unixi.authapp.data.model.UserData

class SessionStore {

    var endpoint: String? = null
        private set

    var userData: UserData? = null
        private set

    fun saveSession(endpoint: String, userData: UserData) {
        this.endpoint = endpoint
        this.userData = userData
    }

    fun clear() {
        endpoint = null
        userData = null
    }

    fun isActive(): Boolean {
        return endpoint != null && userData != null
    }
}