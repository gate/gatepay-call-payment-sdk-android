package com.gate.gatepaydemov1

import android.app.Application
import com.gateio.sdk.gatepay.GatePaySDK

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        //Gate提供的clientId
        GatePaySDK.init(true, this, DemoConfig.MERCHANT_CLIENT_ID)
    }
}