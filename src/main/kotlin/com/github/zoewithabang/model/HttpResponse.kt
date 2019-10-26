package com.github.zoewithabang.model

class HttpResponse(response: ByteArray, source: String) {
    var response: ByteArray? = response
    var source: String? = source

    override fun toString(): String {
        return "HttpResponse(response=${response?.contentToString()}, source=$source)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HttpResponse

        if (source != other.source) return false

        return true
    }

    override fun hashCode(): Int {
        var result = response?.contentHashCode() ?: 0
        result = 31 * result + (source?.hashCode() ?: 0)
        return result
    }
}
