package com.app.garapan.presentation.navigation

import androidx.lifecycle.SavedStateHandle
import com.app.garapan.domain.model.Jasa
import com.app.garapan.domain.model.JasaStatus

object NavResults {
    const val PORTFOLIO_REFRESH = "portfolio_refresh"
    const val JASA_REFRESH = "jasa_refresh"
    const val PROJECT_REFRESH = "project_refresh"

    private const val JASA_SAVED_ID = "jasa_saved_id"
    private const val JASA_SAVED_TITLE = "jasa_saved_title"
    private const val JASA_SAVED_PRICE = "jasa_saved_price"
    private const val JASA_SAVED_IMAGE_URL = "jasa_saved_image_url"
    private const val JASA_SAVED_KATEGORI_NAME = "jasa_saved_kategori_name"
    private const val JASA_SAVED_STATUS = "jasa_saved_status"

    fun publishJasaSaved(handle: SavedStateHandle?, jasa: Jasa) {
        if (handle == null) return
        handle[JASA_REFRESH] = true
        handle[JASA_SAVED_ID] = jasa.id
        handle[JASA_SAVED_TITLE] = jasa.title
        handle[JASA_SAVED_PRICE] = jasa.price
        handle[JASA_SAVED_IMAGE_URL] = jasa.imageUrl
        handle[JASA_SAVED_KATEGORI_NAME] = jasa.kategoriName
        handle[JASA_SAVED_STATUS] = jasa.status.name
    }

    fun readJasaSaved(handle: SavedStateHandle): Jasa? {
        val id = handle.get<String>(JASA_SAVED_ID) ?: return null
        val statusName = handle.get<String>(JASA_SAVED_STATUS) ?: JasaStatus.ACTIVE.name
        val status = runCatching { JasaStatus.valueOf(statusName) }.getOrDefault(JasaStatus.ACTIVE)
        return Jasa(
            id = id,
            mahasiswaId = "",
            kategoriId = "",
            title = handle.get<String>(JASA_SAVED_TITLE).orEmpty(),
            description = "",
            price = handle.get<Double>(JASA_SAVED_PRICE)
                ?: handle.get<Float>(JASA_SAVED_PRICE)?.toDouble()
                ?: 0.0,
            imageUrl = handle.get<String>(JASA_SAVED_IMAGE_URL).orEmpty(),
            status = status,
            kategoriName = handle.get<String>(JASA_SAVED_KATEGORI_NAME).orEmpty()
        )
    }

    fun clearJasaSaved(handle: SavedStateHandle) {
        handle[JASA_REFRESH] = false
        handle.remove<String>(JASA_SAVED_ID)
        handle.remove<String>(JASA_SAVED_TITLE)
        handle.remove<Double>(JASA_SAVED_PRICE)
        handle.remove<Float>(JASA_SAVED_PRICE)
        handle.remove<String>(JASA_SAVED_IMAGE_URL)
        handle.remove<String>(JASA_SAVED_KATEGORI_NAME)
        handle.remove<String>(JASA_SAVED_STATUS)
    }

    fun publishProjectRefresh(handle: SavedStateHandle?) {
        handle?.set(PROJECT_REFRESH, true)
    }

    fun clearProjectRefresh(handle: SavedStateHandle) {
        handle[PROJECT_REFRESH] = false
    }
}
