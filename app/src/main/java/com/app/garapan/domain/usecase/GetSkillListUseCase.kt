package com.app.garapan.domain.usecase

import com.app.garapan.domain.repository.SkillRepository
import javax.inject.Inject

class GetSkillListUseCase @Inject constructor(
    private val skillRepository: SkillRepository
) {
    suspend operator fun invoke() = skillRepository.getSkillList()
}
