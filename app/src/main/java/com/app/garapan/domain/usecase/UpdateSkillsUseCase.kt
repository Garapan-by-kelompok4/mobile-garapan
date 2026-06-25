package com.app.garapan.domain.usecase

import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.UpdateProfileParams
import com.app.garapan.domain.model.User
import javax.inject.Inject

class UpdateSkillsUseCase @Inject constructor(
    private val updateProfileUseCase: UpdateProfileUseCase
) {
    suspend operator fun invoke(skills: List<String>): Resource<User> =
        updateProfileUseCase(UpdateProfileParams(skills = skills))
}
