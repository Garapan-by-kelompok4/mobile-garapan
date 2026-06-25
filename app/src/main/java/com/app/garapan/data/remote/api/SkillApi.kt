package com.app.garapan.data.remote.api

import com.app.garapan.data.remote.dto.SkillDto
import retrofit2.http.GET

interface SkillApi {
    @GET("skills")
    suspend fun getSkillList(): List<SkillDto>
}
