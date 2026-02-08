package io.github.dovecoteescapee.byedpi.data

import com.google.gson.annotations.SerializedName

data class Command(
    @SerializedName("text") var text: String,
    @SerializedName("pinned") var pinned: Boolean = false,
    @SerializedName("name") var name: String = ""
)