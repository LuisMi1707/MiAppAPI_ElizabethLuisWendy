package com.example.miappapi_elizabethluiswendy.api

import com.example.miappapi_elizabethluiswendy.model.Producto
import retrofit2.Response
import retrofit2.http.*

interface ProductoApi {

    @GET("mostrar.php")
    suspend fun obtenerTodos(): Response<ProductosResponse>

    @GET("mostrar_por_id.php")
    suspend fun obtenerPorId(@Query("id") id: Int): Response<ProductoResponse>

    @POST("crear.php")
    suspend fun crear(@Body producto: CrearRequest): Response<CrearResponse>

    @PUT("actualizar.php")
    suspend fun actualizar(
        @Query("id") id: Int,
        @Body producto: ActualizarRequest
    ): Response<MensajeResponse>

    @HTTP(method = "DELETE", path = "eliminar.php", hasBody = false)
    suspend fun eliminar(@Query("id") id: Int): Response<MensajeResponse>
}

data class ProductosResponse(
    val success: Boolean,
    val total: Int,
    val data: List<Producto>
)

data class ProductoResponse(
    val success: Boolean,
    val data: Producto
)

data class MensajeResponse(
    val success: Boolean,
    val message: String,
    val error: String? = null
)

data class CrearResponse(
    val success: Boolean,
    val message: String,
    val id: Int,
    val error: String? = null
)

data class CrearRequest(
    val nombre: String,
    val descripcion: String,
    val precio: Double
)

data class ActualizarRequest(
    val nombre: String,
    val descripcion: String,
    val precio: Double
)