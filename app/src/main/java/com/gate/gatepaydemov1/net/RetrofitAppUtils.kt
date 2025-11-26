package com.gate.gatepaydemov1.net

import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitAppUtils {

    private var urlRoot: String = ""

    @Volatile
    private var singleton: Retrofit? = null

    fun setUrlRoot(url: String) {
        urlRoot = url
    }

    fun <T> createApiForGson(clazz: Class<T>): T {
        val retrofit = singleton ?: synchronized(this) {
            singleton ?: buildRetrofit().also { singleton = it }
        }
        return retrofit.create(clazz)
    }

    private fun buildRetrofit(): Retrofit {
        return Retrofit.Builder().baseUrl(urlRoot).addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create()).client(OkHttpAppUtils.getInstance()).build()
    }
}

