package com.app.garapan.domain.repository

import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.CreateJasaParams
import com.app.garapan.domain.model.Jasa
import com.app.garapan.domain.model.JasaListFilters
import com.app.garapan.domain.model.UpdateJasaParams

interface JasaRepository {
    suspend fun getJasaList(filters: JasaListFilters = JasaListFilters()): Resource<List<Jasa>>
    suspend fun getMyJasaList(filters: JasaListFilters = JasaListFilters()): Resource<List<Jasa>>
    suspend fun getJasaDetail(id: String): Resource<Jasa>
    suspend fun createJasa(params: CreateJasaParams): Resource<Jasa>
    suspend fun updateJasa(id: String, params: UpdateJasaParams): Resource<Jasa>
    suspend fun deleteJasa(id: String): Resource<Unit>
}
