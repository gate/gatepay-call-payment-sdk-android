package com.gate.gatepaydemov1

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.gate.gatepaydemov1.net.HttpServiceAppClient
import com.gate.gatepaydemov1.net.OkHttpAppUtils
import com.gate.gatepaydemov1.net.SigDataBean
import com.gateio.sdk.gatepay.GatePayConstant
import com.gateio.sdk.gatepay.GatePaySDK
import com.gateio.sdk.gatepay.NavigationGatePayListener
import com.google.gson.JsonObject
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers

class PayPageActivity : AppCompatActivity() {
    private var etPrepayId: EditText? = null
    private var tvLog: TextView? = null
    private var btOpenGate: Button? = null
    private var lastClickTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pay_page)
        tvLog = findViewById(R.id.tv_input_log)
        etPrepayId = findViewById(R.id.et_prepay_id)
        btOpenGate = findViewById(R.id.bt_open_gate)

        btOpenGate?.setOnClickListener {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastClickTime < 1500) {
                return@setOnClickListener
            }
            lastClickTime = currentTime
            val prepayId = etPrepayId?.text?.toString()
            if (!prepayId.isNullOrEmpty()) {
                getSign(prepayId, "GatePay")
            } else {
                Toast.makeText(this, "enter prepay id", Toast.LENGTH_SHORT).show()
            }
        }

        // 处理初次启动时的Deep Link
        handleDeepLink(intent)

        // 获取当前Scheme
        inputLog("Scheme:\n${GatePaySDK.getSchemeByClientId(DemoConfig.MERCHANT_CLIENT_ID)}")
    }

    private fun inputLog(log: String?) {
        if (!log.isNullOrEmpty()) {
            Log.i("gate_pay_callback", log)
            tvLog?.text = log
        }
    }

    @SuppressLint("CheckResult")
    private fun getSign(prepayId: String?, packageExt: String?) {
        val jsonObject = JsonObject().apply {
            addProperty("prepayid", prepayId)
            addProperty("package_ext", packageExt)
        }

        HttpServiceAppClient.getInstance(DemoConfig.MERCHANT_BASE_URL)
            .merchantSignature(OkHttpAppUtils.getRequestBody(jsonObject)).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread()).subscribe({ dataBean ->
                dataBean.data?.bizData?.let { bizData ->
                    startGatePay(bizData)
                }
            }, { throwable ->
                inputLog("throwable:: ${throwable.message}")
            })
    }

    private fun startGatePay(bizData: SigDataBean.DataBean.BizDataBean) {
        val signature = bizData.signature ?: return
        val ts = bizData.ts ?: return
        val nonce = bizData.nonce ?: return
        val prepayId = bizData.prepayId ?: return

        GatePaySDK.startGatePay(
            this@PayPageActivity, signature, ts, nonce, prepayId, "GatePay", object : NavigationGatePayListener {
                override fun onGateOpenSuccess() {
                    Log.i("gate_pay_sdk", "onGateOpenSuccess()")
                }

                override fun onGateOpenFailed(code: Int, errorMessage: String?) {
                    Log.i("gate_pay_sdk", "onGateOpenFailed()  code:$code   errorMessage:$errorMessage")
                    inputLog("onGateOpenFailed()  code:$code   errorMessage:$errorMessage")
                }
            })
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        // 处理从外部应用返回的Deep Link
        handleDeepLink(intent)
    }

    /**
     * 处理支付回调的Deep Link
     * 格式：gatepay*******://payment?isSuccess=1&source=gatePay&prepayId=123435567
     */
    private fun handleDeepLink(intent: Intent?) {
        intent?.data?.let { uri ->
            inputLog("Deep Link Callback: $uri")

            // 验证host
            if (uri.host == "payment") {
                // 获取回调参数
                val isSuccess = uri.getQueryParameter("isSuccess")
                val source = uri.getQueryParameter("source")
                val prepayId = uri.getQueryParameter("prepayId")

                inputLog("isSuccess: $isSuccess, source: $source, prepayId: $prepayId")

                // 处理支付结果
                when (isSuccess) {
                    GatePayConstant.PAYMENT_STATE_SUCCESS_CODE -> {
                        // Payment Success
                        inputLog("Payment Success\nPrepayId: $prepayId\nSource: $source")
                        Toast.makeText(this, "Payment Success", Toast.LENGTH_SHORT).show()
                    }

                    GatePayConstant.PAYMENT_STATE_FAILED_CODE -> {
                        // Payment Failed
                        inputLog("Payment Failed\nPrepayId: $prepayId\nSource: $source")
                        Toast.makeText(this, "Payment Failed", Toast.LENGTH_SHORT).show()
                    }

                    GatePayConstant.PAYMENT_STATE_CANCEL_CODE -> {
                        // Payment Cancelled
                        inputLog("Payment Cancelled\nPrepayId: $prepayId\nSource: $source")
                        Toast.makeText(this, "Payment Cancelled", Toast.LENGTH_SHORT).show()
                    }

                    else -> {
                        inputLog("Unknown Payment Status: $isSuccess\nPrepayId: $prepayId\nSource: $source")
                    }
                }
            }
        }
    }
}