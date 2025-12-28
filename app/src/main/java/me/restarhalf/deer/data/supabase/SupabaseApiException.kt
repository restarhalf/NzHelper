package me.restarhalf.deer.data.supabase

class SupabaseApiException(
    val statusCode: Int,
    val errorBody: String?,
    message: String
) : Exception(message)
