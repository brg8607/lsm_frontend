package com.example.applsm.data

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.*
import okhttp3.MultipartBody
import okhttp3.RequestBody

// --- MODELOS DE DATOS ---

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
    @SerializedName("video_url") val videoUrl: String?,
    @SerializedName("imagen_url") val imagenUrl: String?,
    @SerializedName("categoria_nombre") val categoriaNombre: String?
)

data class Quiz(
    val id: Long?, // Puede ser nulo si es generado al vuelo
    val titulo: String,
    val preguntas: List<Pregunta>
)

// CORRECCIÓN AQUÍ: Agregamos el campo imagenUrl para recibir "imagen_asociada_url" del backend
data class Pregunta(
    val id: Int,
    @SerializedName("pregunta_texto") val texto: String,
    @SerializedName("video_asociado_url") val videoUrl: String?,
    @SerializedName("imagen_asociada_url") val imagenUrl: String?, // <--- ¡ESTE FALTABA!
    @SerializedName("opciones") val opciones: List<String>,
    @SerializedName("respuesta_correcta") val respuestaCorrecta: String? = null
)

// Datos de Racha
data class RachaResponse(
    @SerializedName("dias_racha") val dias: Int,
    @SerializedName("mensaje") val mensaje: String
)

// Datos de Progreso Detallado
data class EstadoProgreso(
    @SerializedName("categoria_id") val categoriaId: Int,
    @SerializedName("nivel_actual") val nivel: Int,
    @SerializedName("indice_pregunta") val indice: Int,
    @SerializedName("categoria_nombre") val categoriaNombre: String? = null
)

// Modelo para el Mapa de Progreso
data class ProgresoCategoria(
    @SerializedName("categoria_id") val categoriaId: Int,
    @SerializedName("nivel_actual") val nivelActual: Int,
    @SerializedName("preguntas_completadas") val preguntasCompletadas: Int,
    @SerializedName("total_preguntas") val totalPreguntas: Int,
    @SerializedName("completado") val completado: Boolean,
    @SerializedName("bloqueado") val bloqueado: Boolean
)

data class UltimoProgreso(
    @SerializedName("categoria_id") val categoriaId: Int,
    @SerializedName("categoria_nombre") val categoriaNombre: String,
    @SerializedName("nivel") val nivel: Int,
    @SerializedName("progreso_percent") val progresoPercent: Float
)

// Modelo antiguo de Progreso (Legacy)
data class Progreso(
    @SerializedName("categoria_id") val categoriaId: Int,
    val nombre: String,
    val porcentaje: Int
)

// --- API SERVICE ---

interface ApiService {
    @POST("api/auth/login")
    suspend fun login(@Body creds: Map<String, String>): Response<AuthResponse>

    @POST("api/auth/register")
    suspend fun register(@Body data: Map<String, String>): Response<AuthResponse>

    @POST("api/auth/google")
    suspend fun googleLogin(@Body data: Map<String, String>): Response<AuthResponse>

    @POST("api/auth/guest")
    suspend fun guestLogin(): Response<AuthResponse>

    @GET("api/categorias")
    suspend fun getCategorias(): Response<List<Categoria>>

    @GET("api/senas")
    suspend fun getSenas(
        @Query("categoria_id") catId: Int? = null,
        @Query("busqueda") query: String? = null
    ): Response<List<Sena>>

    // --- NUEVOS ENDPOINTS DINÁMICOS ---

    @GET("api/quiz/generarDinamico")
    suspend fun generarQuizDinamico(
        @Header("Authorization") token: String,
        @Query("categoria_id") catId: Int? = null,
        @Query("nivel") nivel: Int = 1
    ): Response<Quiz>

    @GET("api/progreso/mapa")
    suspend fun getProgresoMapa(@Header("Authorization") token: String): Response<List<ProgresoCategoria>>

    @POST("api/progreso/guardar")
    suspend fun guardarProgreso(
        @Header("Authorization") token: String,
        @Body datos: ProgresoRequest // Asegúrate de tener esta clase definida o usar Map si prefieres
    ): Response<Map<String, String>>

    @GET("api/progreso/actual")
    suspend fun getProgresoActual(@Header("Authorization") token: String): Response<UltimoProgreso>

    @GET("api/usuario/racha")
    suspend fun getRacha(@Header("Authorization") token: String): Response<RachaResponse>

    // --- ENDPOINTS LEGACY ---
    @GET("api/quiz/hoy")
    suspend fun getQuizDelDia(@Header("Authorization") token: String): Response<Quiz>

    @POST("api/quiz/resultado")
    suspend fun enviarResultado(
        @Header("Authorization") token: String,
        @Body resultado: ResultadoQuizRequest // Asegúrate de tener esta clase definida
    ): Response<Map<String, Any>>

    @GET("api/progreso")
    suspend fun getProgreso(@Header("Authorization") token: String): Response<List<Progreso>>

    // --- ADMIN ---
    @Multipart
    @POST("api/admin/senas")
    suspend fun subirSena(
        @Header("Authorization") token: String,
        @Part("palabra") palabra: RequestBody,
        @Part("categoria_id") catId: RequestBody,
        @Part video: MultipartBody.Part
    ): Response<Map<String, Any>>
}

// CLASES DE REQUEST NECESARIAS QUE NO ESTABAN EN EL SNIPPET ANTERIOR PERO SE USAN
data class ProgresoRequest(
    @SerializedName("categoria_id") val categoriaId: Int,
    @SerializedName("nivel") val nivel: Int,
    @SerializedName("indice") val indice: Int
)

data class ResultadoQuizRequest(
    @SerializedName("quiz_id") val quizId: Int,
    @SerializedName("puntaje") val puntaje: Int
)


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