package com.gate.gatepaydemov1.net

object HttpServiceAppClient {

    private var interfaceAppService: InterfaceAppService? = null

    fun getInstance(baseUrl: String): InterfaceAppService {
        RetrofitAppUtils.setUrlRoot(baseUrl)
        interfaceAppService = RetrofitAppUtils.createApiForGson(InterfaceAppService::class.java)
        return interfaceAppService ?: throw IllegalStateException("Failed to create InterfaceAppService")
    }
}

