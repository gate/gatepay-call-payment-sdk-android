package com.gate.gatepaydemov1.net

import io.reactivex.rxjava3.core.Observable
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface InterfaceAppService {

    @Headers("Content-Type:application/json")
    @POST("xxxxxx")
    fun merchantSignature(@Body body: RequestBody): Observable<SigDataBean>
}

