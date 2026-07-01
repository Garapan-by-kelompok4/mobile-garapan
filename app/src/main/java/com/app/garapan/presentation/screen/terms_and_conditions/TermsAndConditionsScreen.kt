package com.app.garapan.presentation.screen.terms_and_conditions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.app.garapan.presentation.components.AppTopBar
import com.app.garapan.presentation.components.LegalDocumentHeader
import com.app.garapan.presentation.components.LegalSection
import com.app.garapan.presentation.components.LegalSectionCard
import com.app.garapan.ui.theme.Surface
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.ScrollText

private val termsSections = listOf(
    LegalSection(
        "Penerimaan Ketentuan",
        "Dengan mengakses dan menggunakan aplikasi GARAPAN (\"Aplikasi\"), Anda menyetujui untuk terikat oleh Syarat & Ketentuan ini. Jika Anda tidak setuju, mohon untuk tidak menggunakan Aplikasi."
    ),
    LegalSection(
        "Deskripsi Layanan",
        "GARAPAN adalah platform yang mempertemukan mahasiswa penyedia jasa IT freelance (\"Mahasiswa\") dengan pengguna yang membutuhkan jasa tersebut (\"Klien\"). GARAPAN bertindak sebagai perantara dan bukan pihak dalam perjanjian kerja antara Mahasiswa dan Klien."
    ),
    LegalSection(
        "Akun Pengguna",
        "Pengguna wajib memberikan informasi yang akurat saat mendaftar, termasuk verifikasi status sebagai mahasiswa aktif bagi peran Mahasiswa. Pengguna bertanggung jawab menjaga kerahasiaan kredensial akun. GARAPAN berhak menangguhkan atau menghapus akun yang melanggar ketentuan ini."
    ),
    LegalSection(
        "Pemesanan dan Pembayaran",
        "Transaksi pembayaran diproses melalui mitra payment gateway pihak ketiga (Midtrans). Dana pembayaran akan ditahan (escrow) hingga pekerjaan dikonfirmasi selesai oleh Klien. Kebijakan pembatalan dan pengembalian dana mengikuti ketentuan yang berlaku pada masing-masing pesanan."
    ),
    LegalSection(
        "Perilaku Pengguna",
        "Pengguna dilarang menyalahgunakan Aplikasi untuk aktivitas ilegal, penipuan, pelecehan, atau pelanggaran hak kekayaan intelektual pihak lain."
    ),
    LegalSection(
        "Penyelesaian Sengketa",
        "Sengketa antara Mahasiswa dan Klien akan difasilitasi melalui fitur dispute internal Aplikasi. GARAPAN dapat menjadi mediator namun tidak menjamin hasil akhir sengketa."
    ),
    LegalSection(
        "Batasan Tanggung Jawab",
        "GARAPAN tidak bertanggung jawab atas kualitas pekerjaan, keterlambatan, atau kerugian yang timbul dari transaksi antara Mahasiswa dan Klien, sepanjang diizinkan oleh hukum yang berlaku."
    ),
    LegalSection(
        "Perubahan Ketentuan",
        "GARAPAN dapat mengubah Syarat & Ketentuan ini sewaktu-waktu. Perubahan akan diberitahukan melalui Aplikasi."
    ),
    LegalSection(
        "Hukum yang Berlaku",
        "Syarat & Ketentuan ini tunduk pada hukum Republik Indonesia."
    )
)

@Composable
fun TermsAndConditionsScreen(
    navController: NavController
) {
    Scaffold(containerColor = Surface) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            AppTopBar(title = "Syarat & Ketentuan", onBack = { navController.navigateUp() })
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                LegalDocumentHeader(
                    icon = Lucide.ScrollText,
                    description = "Ketentuan penggunaan Aplikasi GARAPAN bagi Mahasiswa dan Klien.",
                    lastUpdated = "1 Juli 2026"
                )
                termsSections.forEachIndexed { index, section ->
                    LegalSectionCard(index = index + 1, section = section)
                }
            }
        }
    }
}
