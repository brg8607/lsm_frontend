package com.example.applsm.data

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.*
import okhttp3.MultipartBody
import okhttp3.RequestBody

// --- CORRECCIÓN CRÍTICA: Tipos Nullables (?) ---

data class User(
    val id: Int,
    val nombre: String,
    val correo: String?,
    @SerializedName("tipo_usuario") val tipoUsuario: String
)

data class AuthResponse(
    val mensaje: String,
    val token: String?,
    val usuario: User?
)

data class Categoria(
    val id: Int,
    val nombre: String,
    @SerializedName("icon_url") val iconUrl: String?
)

data class Sena(
    val id: Int,
    val palabra: String,
    val descripcion: String?,
    // AQUÍ ESTABA EL ERROR: Agregamos '?' para permitir nulos
    @SerializedName("video_url") val videoUrl: String?,
    @SerializedName("imagen_url") val imagenUrl: String?,
    @SerializedName("categoria_nombre") val categoriaNombre: String?
)

data class Quiz(
    val id: Int,
    val titulo: String,
    val preguntas: List<Pregunta>
)

data class Pregunta(
    val id: Int,
    @SerializedName("pregunta_texto") val texto: String,
    @SerializedName("video_asociado_url") val videoUrl: String?,
    @SerializedName("opciones") val opciones: List<String>
)

data class Progreso(
    @SerializedName("categoria_id") val categoriaId: Int,
    val nombre: String,
    val porcentaje: Int
)

// 2. INTERFAZ API
interface ApiService {
    @POST("api/auth/login")
    suspend fun login(@Body creds: Map<String, String>): Response<AuthResponse>

    @POST("api/auth/register")
    suspend fun register(@Body data: Map<String, String>): Response<AuthResponse>

    @POST("api/auth/guest")
    suspend fun guestLogin(): Response<AuthResponse>

    @GET("api/categorias")
    suspend fun getCategorias(): Response<List<Categoria>>

    @GET("api/senas")
    suspend fun getSenas(
        @Query("categoria_id") catId: Int? = null,
        @Query("busqueda") query: String? = null
    ): Response<List<Sena>>

    @GET("api/quiz/hoy")
    suspend fun getQuizDelDia(@Header("Authorization") token: String): Response<Quiz>

    @POST("api/quiz/resultado")
    suspend fun enviarResultado(
        @Header("Authorization") token: String,
        @Body resultado: Map<String, Any>
    ): Response<Map<String, Any>>

    @GET("api/progreso")
    suspend fun getProgreso(@Header("Authorization") token: String): Response<List<Progreso>>

    @Multipart
    @POST("api/admin/senas")
    suspend fun subirSena(
        @Header("Authorization") token: String,
        @Part("palabra") palabra: RequestBody,
        @Part("categoria_id") catId: RequestBody,
        @Part video: MultipartBody.Part
    ): Response<Map<String, Any>>
}

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:3000/"

    val api: ApiService by lazy {
        retrofit2.Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}