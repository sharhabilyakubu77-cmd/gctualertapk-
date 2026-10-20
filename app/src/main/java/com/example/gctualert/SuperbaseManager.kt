package com.example.gctualert

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.serializer.KotlinXSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonPrimitive

object SupabaseManager {
    val client = createSupabaseClient(
        supabaseUrl = "https://qfnoddaomhycqxvksloo.supabase.co",
        supabaseKey = "sb_publishable_H8HkjvzDRVYFjyBoqByENg_Ooi4MR4m"
    ) {
        defaultSerializer = KotlinXSerializer(
            Json {
                ignoreUnknownKeys = true
                isLenient = true
                coerceInputValues = true
            }
        )
        install(Postgrest)
        install(Realtime)
    }
}

@Serializable
data class StudentRecord(
    @SerialName("Full Name") val name: String,
    @SerialName("Email") val email: String,
    @SerialName("Course") val course: String,
    @SerialName("department") val department: String,
    @SerialName("Index Number") val indexNumber: Long,
    @SerialName("Password") val password: String
)

@Serializable
data class StudentProfile(
    @SerialName("Full Name") val name: String? = null,
    @SerialName("Course") val course: String? = null,
    @SerialName("Index Number") val indexNumber: Long? = null
)


