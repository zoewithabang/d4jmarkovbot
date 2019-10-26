package com.github.zoewithabang.model

import java.util.ArrayList

class UserData {
    var id: String? = null
    var tracked: Boolean? = null
    var permissionRank: Int? = null
    var messages: ArrayList<MessageData>? = null

    constructor() {
        messages = ArrayList()
    }

    constructor(id: String, tracked: Boolean, permissionRank: Int) {
        this.id = id
        this.tracked = tracked
        this.permissionRank = permissionRank
        messages = ArrayList()
    }

    fun addMessage(message: MessageData) {
        messages!!.add(message)
    }

    override fun toString(): String {
        return "UserData(id=$id, tracked=$tracked, permissionRank=$permissionRank, messages=$messages)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UserData

        if (id != other.id) return false
        if (tracked != other.tracked) return false
        if (permissionRank != other.permissionRank) return false
        if (messages != other.messages) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + (tracked?.hashCode() ?: 0)
        result = 31 * result + (permissionRank ?: 0)
        result = 31 * result + (messages?.hashCode() ?: 0)
        return result
    }
}
