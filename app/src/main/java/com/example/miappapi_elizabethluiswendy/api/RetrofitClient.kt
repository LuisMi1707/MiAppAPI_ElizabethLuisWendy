package com.example.miappapi_elizabethluiswendy.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // URL BASE CON TU IP Y PUERTO
    private const val BASE_URL = "http://192.168.0.34:8080/proyecto-api/api/"

    val instance: ProductoApi by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(ProductoApi::class.java)
    }
}