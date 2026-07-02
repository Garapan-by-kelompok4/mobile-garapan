package com.app.garapan.presentation.screen.privacy_policy

import androidx.compose.foundation.layout.consumeWindowInsets
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
import com.composables.icons.lucide.ShieldCheck

private val privacySections = listOf(
    LegalSection(
        "Informasi yang Kami Kumpulkan",
        "Data akun: nama, email, nomor telepon, foto profil. Data verifikasi: status mahasiswa, keahlian, portofolio (untuk Mahasiswa). Data transaksi: riwayat pesanan, informasi pembayaran (diproses oleh Midtrans, GARAPAN tidak menyimpan data kartu pembayaran). Data komunikasi: pesan dalam fitur chat antara Mahasiswa dan Klien. Data perangkat: untuk notifikasi push (Firebase Cloud Messaging)."
    ),
    LegalSection(
        "Dasar dan Tujuan Pemrosesan Data",
        "Data diproses berdasarkan persetujuan pengguna dan kebutuhan pelaksanaan layanan, sesuai dengan UU No. 27 Tahun 2022 tentang Pelindungan Data Pribadi (UU PDP), untuk tujuan: memfasilitasi transaksi, verifikasi identitas, komunikasi, serta peningkatan layanan."
    ),
    LegalSection(
        "Berbagi Data dengan Pihak Ketiga",
        "Data dapat dibagikan kepada Midtrans untuk pemrosesan pembayaran, dan Firebase (Google) untuk notifikasi push. Kami tidak menjual data pribadi pengguna kepada pihak ketiga untuk kepentingan pemasaran."
    ),
    LegalSection(
        "Penyimpanan dan Keamanan Data",
        "Data disimpan selama akun aktif atau selama diperlukan untuk memenuhi kewajiban hukum. Kami menerapkan langkah keamanan teknis dan organisasi yang wajar untuk melindungi data pengguna."
    ),
    LegalSection(
        "Hak Pengguna",
        "Sesuai UU PDP, pengguna berhak untuk mengakses, memperbaiki, membatasi pemrosesan, atau meminta penghapusan data pribadinya, dengan menghubungi tim dukungan GARAPAN."
    ),
    LegalSection(
        "Perubahan Kebijakan",
        "Kebijakan Privasi ini dapat diperbarui sewaktu-waktu. Perubahan material akan diberitahukan melalui Aplikasi."
    ),
    LegalSection(
        "Kontak",
        "Pertanyaan terkait privasi dapat diajukan ke email/kontak resmi GARAPAN."
    )
)

@Composable
fun PrivacyPolicyScreen(
    navController: NavController
) {
    Scaffold(containerColor = Surface) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            AppTopBar(title = "Kebijakan Privasi", onBack = { navController.navigateUp() })
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                LegalDocumentHeader(
                    icon = Lucide.ShieldCheck,
                    description = "Bagaimana GARAPAN mengumpulkan, menggunakan, dan melindungi data pribadi Anda.",
                    lastUpdated = "1 Juli 2026"
                )
                privacySections.forEachIndexed { index, section ->
                    LegalSectionCard(index = index + 1, section = section)
                }
            }
        }
    }
}
