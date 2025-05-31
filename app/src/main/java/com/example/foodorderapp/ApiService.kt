package com.example.foodorderapp

import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    @GET("menu")
    fun getMenu(): Call<List<Menu>>

    @POST("menu")
    fun addMenu(@Body menu: Menu): Call<Menu>

    @DELETE("menu/{id}")
    fun deleteMenu(@Path("id") id: Int): Call<Void>

    @PUT("menu/{id}")
    fun updateMenu(@Path("id") id: Int, @Body menu: Menu): Call<Menu>
}
