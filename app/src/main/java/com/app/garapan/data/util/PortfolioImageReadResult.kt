package com.app.garapan.data.util

import com.app.garapan.domain.model.PortofolioImage

sealed interface PortfolioImageReadResult {
    data class Success(val image: PortofolioImage) : PortfolioImageReadResult
    data class Failure(val stage: String, val detail: String) : PortfolioImageReadResult
}
