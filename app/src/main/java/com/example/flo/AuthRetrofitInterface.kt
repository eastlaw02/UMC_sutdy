package com.example.flo

import android.telecom.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthRetrofitInterface {
    @POST("/users")
    fun signUp(@Body user : User) : retrofit2.Call<AuthResponse>

}