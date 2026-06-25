package com.app.garapan.domain.repository

import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.Skill

interface SkillRepository {
    suspend fun getSkillList(): Resource<List<Skill>>
}
