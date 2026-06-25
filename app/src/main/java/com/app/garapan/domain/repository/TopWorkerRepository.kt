package com.app.garapan.domain.repository

import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.TopWorker

interface TopWorkerRepository {
    suspend fun getTopWorkers(): Resource<List<TopWorker>>
}
