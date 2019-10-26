package com.github.zoewithabang.model

import java.sql.Timestamp

class MessageData {
    var id: String? = null
    var userId: String? = null
    var content: String? = null
    var timestampLong: Long? = null
        private set

    val timestampTimestamp: Timestamp?
        get() = timestampLong?.let { Timestamp(it) }

    constructor(id: String, userId: String, content: String, timestamp: Long) {
        this.id = id
        this.userId = userId
        this.content = content
        this.timestampLong = timestamp
    }

    constructor(id: String, userId: String, content: String, timestamp: Timestamp) {
        this.id = id
        this.userId = userId
        this.content = content
        this.timestampLong = timestamp.time
    }

    override fun toString(): String {
        return "MessageData(id=$id, userId=$userId, content=$content, timestampLong=$timestampLong)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MessageData

        if (id != other.id) return false
        if (userId != other.userId) return false
        if (content != other.content) return false
        if (timestampLong != other.timestampLong) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + (userId?.hashCode() ?: 0)
        result = 31 * result + (content?.hashCode() ?: 0)
        result = 31 * result + (timestampLong?.hashCode() ?: 0)
        return result
    }
}
