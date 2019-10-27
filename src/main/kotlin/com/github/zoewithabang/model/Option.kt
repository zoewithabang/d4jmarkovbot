package com.github.zoewithabang.model

class Option(key: String, value: String) {
    var key: String? = key
    var value: String? = value

    override fun toString(): String {
        return "Option(key=$key, value=$value)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Option

        if (key != other.key) return false
        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        var result = key?.hashCode() ?: 0
        result = 31 * result + (value?.hashCode() ?: 0)
        return result
    }
}
