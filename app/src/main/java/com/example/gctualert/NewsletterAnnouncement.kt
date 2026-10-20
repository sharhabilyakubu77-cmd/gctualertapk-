
package com.example.gctualert

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NewsletterAnnouncement(
    val id: Long = 0,
    val title: String = "",
    val content: String = "",
    val category: String? = null,
    val department: String? = null,
    val author: String? = null,
    @SerialName("image_url")
    val imageUrl: String? = null,
    @SerialName("is_pinned")
    val isPinned: Boolean? = false,
    @SerialName("created_at")
    val createdAt: String? = null
) {
    val activeImageUrl: String?
        get() {
            if (imageUrl.isNullOrBlank()) return null
            return if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
                imageUrl
            } else {
                "https://gctualert.infinityfree.io/" + imageUrl.removePrefix("/")
            }
        }
}
