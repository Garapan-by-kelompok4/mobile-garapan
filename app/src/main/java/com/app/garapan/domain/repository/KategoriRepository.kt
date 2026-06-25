package com.app.garapan.domain.repository

import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.Kategori

interface KategoriRepository {
    suspend fun getKategoriList(): Resource<List<Kategori>>
}
