package com.app.garapan.data.remote.dto

import com.google.gson.annotations.SerializedName

data class KategoriListResponseDto(
    val value: List<KategoriDto>,
    @SerializedName("Count")
    val count: Int
)

data class KategoriDto(
    val id: String,
    val name: String,
    val icon: String
)
