package com.github.zoewithabang.model

class Alias {
    var alias: String? = null
    var command: String? = null
    var description: String? = null

    constructor()

    constructor(alias: String, command: String, description: String) {
        this.alias = alias
        this.command = command
        this.description = description
    }

    override fun toString(): String {
        return "Alias(alias=$alias, command=$command, description=$description)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Alias

        if (alias != other.alias) return false
        if (command != other.command) return false
        if (description != other.description) return false

        return true
    }

    override fun hashCode(): Int {
        var result = alias?.hashCode() ?: 0
        result = 31 * result + (command?.hashCode() ?: 0)
        result = 31 * result + (description?.hashCode() ?: 0)
        return result
    }
}
