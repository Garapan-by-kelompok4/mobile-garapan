package com.app.garapan.domain.repository

import com.app.garapan.domain.common.Resource

interface LaporanRepository {
    suspend fun createLaporan(pesananId: String, reason: String): Resource<Unit>
}
