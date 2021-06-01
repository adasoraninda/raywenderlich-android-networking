package com.raywenderlich.android.taskie.networking

import okhttp3.OkHttpClient
import retrofit2.Retrofit

fun buildClient(): OkHttpClient {
    return OkHttpClient.Builder()
        .build()
}

fun buildRetrofit(): Retrofit {
    return Retrofit.Builder()
        .client(buildClient())
        .baseUrl(BASE_URL)
        .build()
}

fun buildApiService(): RemoteApiService {
    return buildRetrofit().create(RemoteApiService::class.java)
}