package com.example.eyesight.data.response

import android.adservices.ondevicepersonalization.UserData

data class TokenRequest(
    val idToken: String,
)

// ApiResponse.kt
data class ApiResponse(
    val success: Boolean,
    val message: String
)