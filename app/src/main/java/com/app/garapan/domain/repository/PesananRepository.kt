package com.app.garapan.domain.repository

import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.CreatePesananParams
import com.app.garapan.domain.model.Pesanan

interface PesananRepository {
    suspend fun createPesanan(params: CreatePesananParams): Resource<Pesanan>
    suspend fun getMyPesananList(page: Int = 1, limit: Int = 20): Resource<List<Pesanan>>
    suspend fun getPesananDetail(id: String): Resource<Pesanan>
    suspend fun deliverPesanan(id: String): Resource<Pesanan>
    suspend fun completePesanan(id: String): Resource<Pesanan>
    suspend fun cancelPesanan(id: String): Resource<Pesanan>
}
