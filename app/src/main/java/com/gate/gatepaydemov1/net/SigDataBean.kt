package com.gate.gatepaydemov1.net

data class SigDataBean(
    val code: Int = 0,
    val label: String? = null,
    val message: String? = null,
    val page: Int = 0,
    val pageSize: Int = 0,
    val pageCount: Int = 0,
    val totalCount: Int = 0,
    val data: DataBean? = null,
) {

    data class DataBean(
        val bizCode: String? = null,
        val bizData: BizDataBean? = null,
        val bizMessage: String? = null,
    ) {

        data class BizDataBean(
            val prepayId: String? = null,
            val ts: String? = null,
            val nonce: String? = null,
            val signature: String? = null,
        )
    }
}

