package com.github.zoewithabang.model

class TaskInfo(task: String, active: Boolean, initialDelay: Int, period: Int) {
    var task: String? = task
    var active: Boolean? = active
    var initialDelay: Int? = initialDelay
    var period: Int? = period

    override fun toString(): String {
        return "TaskInfo(task=$task, active=$active, initialDelay=$initialDelay, period=$period)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TaskInfo

        if (task != other.task) return false
        if (active != other.active) return false
        if (initialDelay != other.initialDelay) return false
        if (period != other.period) return false

        return true
    }

    override fun hashCode(): Int {
        var result = task?.hashCode() ?: 0
        result = 31 * result + (active?.hashCode() ?: 0)
        result = 31 * result + (initialDelay ?: 0)
        result = 31 * result + (period ?: 0)
        return result
    }
}
