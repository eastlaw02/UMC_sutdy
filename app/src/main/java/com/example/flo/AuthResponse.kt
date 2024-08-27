package com.example.flo

import androidx.core.app.NotificationCompat.MessagingStyle.Message

data class AuthResponse(val isSuccess:Boolean, val code:Int, val message: String)
