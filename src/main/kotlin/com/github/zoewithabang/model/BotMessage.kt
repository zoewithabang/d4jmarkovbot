package com.github.zoewithabang.model

class BotMessage {
    var name: String? = null
    var message: String? = null
    var description: String? = null

    constructor()

    constructor(name: String, message: String, description: String) {
        this.name = name
        this.message = message
        this.description = description
    }

    override fun toString(): String {
        return "BotMessage(name=$name, message=$message, description=$description)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BotMessage

        if (name != other.name) return false
        if (message != other.message) return false
        if (description != other.description) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name?.hashCode() ?: 0
        result = 31 * result + (message?.hashCode() ?: 0)
        result = 31 * result + (description?.hashCode() ?: 0)
        return result
    }
}
