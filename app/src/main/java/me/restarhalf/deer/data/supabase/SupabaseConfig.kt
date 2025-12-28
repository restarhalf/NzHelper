package me.restarhalf.deer.data.supabase

import me.restarhalf.deer.BuildConfig

object SupabaseConfig {
    val url: String = BuildConfig.SUPABASE_URL
    val anonKey: String = BuildConfig.SUPABASE_ANON_KEY

    val authBaseUrl: String = "${url.trimEnd('/')}/auth/v1"
    val restBaseUrl: String = "${url.trimEnd('/')}/rest/v1"
}
