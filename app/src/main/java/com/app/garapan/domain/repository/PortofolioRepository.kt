package com.app.garapan.domain.repository

import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.CreatePortofolioParams
import com.app.garapan.domain.model.Portofolio

interface PortofolioRepository {
    suspend fun getPortofolioList(mahasiswaId: String): Resource<List<Portofolio>>
    suspend fun addPortofolio(params: CreatePortofolioParams): Resource<Portofolio>
    suspend fun deletePortofolio(id: String): Resource<Unit>
}
