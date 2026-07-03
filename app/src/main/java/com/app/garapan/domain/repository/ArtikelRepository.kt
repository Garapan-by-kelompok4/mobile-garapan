package com.app.garapan.domain.repository

import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.Artikel
import com.app.garapan.domain.model.ArtikelRecommendation

interface ArtikelRepository {
    suspend fun getArtikelList(page: Int = 1, limit: Int = 20): Resource<List<Artikel>>
    suspend fun getArtikelDetail(id: String): Resource<Artikel>
    suspend fun getArtikelRecommendations(id: String, limit: Int = 2): Resource<List<ArtikelRecommendation>>
}
