package com.github.zoewithabang.model

class CommandInfo(command: String, active: Boolean, permissionRank: Int) {
    var command: String? = command
    var active: Boolean? = active
    var permissionRank: Int? = permissionRank

    override fun toString(): String {
        return "CommandInfo(command=$command, active=$active, permissionRank=$permissionRank)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CommandInfo

        if (command != other.command) return false
        if (active != other.active) return false
        if (permissionRank != other.permissionRank) return false

        return true
    }

    override fun hashCode(): Int {
        var result = command?.hashCode() ?: 0
        result = 31 * result + (active?.hashCode() ?: 0)
        result = 31 * result + (permissionRank ?: 0)
        return result
    }
}
