package com.example.aufa.appsales.data

/**
 * Created by aufa on 23/11/17.
 */
data class Sales (
        val username: String = "",
        val email: String = "",
        val password: String = ""

)

data class Kerjaan (
        var nama: String = "",
        var durasi: String? = "",
        var startTime: Long? = null,
        var endTime: Long? = null,
        var lokasi: String = "",
        var key: String? = null,
        var alasan: String = ""
)

open class Kredit {
    var nama: String? = ""
    var alamat: String? = ""
    var idsales: String? = ""
    var photoUrl: String? = ""
    var dokumenLengkap: Int = 0
    var siapSurvey: Int = 0
    var key: String? = null
    var createdAt: Long = System.currentTimeMillis()
    var kerjaanID: String? = null
}
