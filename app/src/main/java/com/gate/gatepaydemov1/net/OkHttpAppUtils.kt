package com.gate.gatepaydemov1.net

import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

object OkHttpAppUtils {

    @Volatile
    private var singleton: OkHttpClient? = null

    fun getInstance(): OkHttpClient {
        return singleton ?: synchronized(this) {
            singleton ?: buildOkHttpClient().also { singleton = it }
        }
    }

    private fun buildOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder().connectTimeout(15000L, TimeUnit.MILLISECONDS)
            .readTimeout(20000L, TimeUnit.MILLISECONDS).writeTimeout(15000L, TimeUnit.MILLISECONDS)
            .addInterceptor(loggingInterceptor).build()
    }

    /**
     * Form强转json
     */
    fun getRequestBody(params: Any): RequestBody {
        val json = Gson().toJson(params)
        return json.toRequestBody("application/json".toMediaType())
    }
}

