package com.github.zoewithabang.model

class CyTubeMedia {
    var title: String? = null
    var service: String? = null
    var url: String? = null

    val fullServiceName: String
        get() {
            return when (service) {
                "yt" -> "YouTube"
                "sc" -> "SoundCloud"
                else -> ""
            }
        }

    val fullUrl: String?
        get() {
            return when (service) {
                "yt" -> "https://www.youtube.com/watch?v=" + url!!
                "sc" -> url
                else -> url
            }
        }

    override fun toString(): String {
        return "CyTubeMedia(title=$title, service=$service, url=$url)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CyTubeMedia

        if (title != other.title) return false
        if (service != other.service) return false
        if (url != other.url) return false

        return true
    }

    override fun hashCode(): Int {
        var result = title?.hashCode() ?: 0
        result = 31 * result + (service?.hashCode() ?: 0)
        result = 31 * result + (url?.hashCode() ?: 0)
        return result
    }
}
