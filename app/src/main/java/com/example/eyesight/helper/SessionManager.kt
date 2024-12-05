package com.example.eyesight.helper

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val preferences: SharedPreferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_LOGGED_IN = "is_loged_in"
        private const val KEY_TOKEN = "auth_token"
        private const val KEY_USER_NAME = "username"
    }

    fun saveLoginSession(token: String, userName: String) {
        preferences.edit().apply{
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_TOKEN, token)
            putString(KEY_USER_NAME, userName)
            apply()
        }
    }

    fun clearSession() {
        preferences.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, false)
            remove(KEY_TOKEN)
            remove(KEY_USER_NAME)
            apply()
        }
    }

    fun isLoggedIn(): Boolean {
        return preferences.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun getToken(): String? {
        return preferences.getString(KEY_TOKEN, null)
    }

    fun getUserName(): String? {
        return preferences.getString(KEY_USER_NAME, null)
    }
}