package com.example.rolecall.data.remote

import com.google.gson.annotations.SerializedName

data class ProfileRead(
    val id: String,
    @SerializedName("first_name") val firstName: String? = null,
    @SerializedName("last_name") val lastName: String? = null,
    val title: String? = null,
    val email: String? = null,
    @SerializedName("avatar_url") val avatarUrl: String? = null
) {
    val fullName: String
        get() = listOfNotNull(firstName, lastName).joinToString(" ").ifBlank { "User" }
}

data class ProfilePatch(
    @SerializedName("first_name") val firstName: String? = null,
    @SerializedName("last_name") val lastName: String? = null,
    val title: String? = null
)

