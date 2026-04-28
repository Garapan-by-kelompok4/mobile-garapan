package com.app.garapan.presentation.screen.security

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.app.garapan.ui.theme.AccentBlue
import com.app.garapan.ui.theme.PrimaryText
import com.app.garapan.ui.theme.SecondaryText
import com.app.garapan.ui.theme.White

@Composable
fun SecurityScreen(
    navController: NavController,
    viewModel: SecurityViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(containerColor = Color(0xFFFAF8FF)) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            SecurityTopBar(onBack = { navController.navigateUp() })
            Column(
                modifier = Modifier.padding(horizontal = 12.dp)
            ) {
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = "Keamanan Akun",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryText
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Kelola kredensial dan metode autentikasi Anda. Kami menyarankan untuk mengaktifkan lapisan keamanan ganda untuk melindungi ruang profesional Anda.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = SecondaryText,
                        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                    )
                )

                Spacer(modifier = Modifier.height(32.dp))
                SecuritySectionTitle("AKSES & AUTENTIKASI")
                Spacer(modifier = Modifier.height(10.dp))
                SecurityCard {
                    SecurityActionRow(
                        icon = Icons.Filled.MoreHoriz,
                        title = "Ubah Kata Sandi",
                        subtitle = "Diperbarui 3 bulan yang lalu",
                        action = "Perbarui"
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    SecurityActionRow(
                        icon = Icons.Filled.Security,
                        title = "Verifikasi Dua Langkah",
                        subtitle = "Aktif\nMelindungi akun Anda dengan mewajibkan kode tambahan saat login dari perangkat tak dikenal.",
                        action = "Kelola",
                        active = true
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))
                SecuritySectionTitle("PEMULIHAN AKUN")
                Spacer(modifier = Modifier.height(10.dp))
                SecurityCard {
                    SecurityActionRow(
                        icon = Icons.Filled.Email,
                        title = "Email Pemulihan",
                        subtitle = "${uiState.recoveryEmail}\nDigunakan untuk mereset kata sandi jika Anda kehilangan akses ke akun.",
                        action = "Ubah"
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))
                SecuritySectionTitle("AKTIVITAS & PERANGKAT")
                Spacer(modifier = Modifier.height(10.dp))
                SecurityCard {
                    LoginHistoryHeader()
                    Spacer(modifier = Modifier.height(18.dp))
                    DeviceItem("Google Pixel 2XL", "Sesi Saat Ini", "Jakarta, Indonesia", active = true)
                    Spacer(modifier = Modifier.height(16.dp))
                    DeviceItem("Samsung A15", "Aktif 2 hari yang lalu", "Bandung, Indonesia")
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun SecurityTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = AccentBlue
            )
        }
        Text(
            text = "Edit Keamanan Akun",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = AccentBlue
            )
        )
    }
}

@Composable
private fun SecuritySectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.ExtraBold,
            color = SecondaryText
        )
    )
}

@Composable
private fun SecurityCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(White)
            .padding(18.dp),
        content = content
    )
}

@Composable
private fun SecurityActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    action: String,
    active: Boolean = false
) {
    Row(verticalAlignment = Alignment.Top) {
        IconBadge(icon = icon, active = active)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 14.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = PrimaryText
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = SecondaryText,
                    lineHeight = MaterialTheme.typography.bodySmall.lineHeight
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE6E5F1),
                    contentColor = AccentBlue
                ),
                shape = RoundedCornerShape(50.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
            ) {
                Text(
                    text = action,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold)
                )
            }
        }
    }
}

@Composable
private fun IconBadge(icon: ImageVector, active: Boolean = false) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(if (active) AccentBlue else Color(0xFFE7ECFF)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (active) White else AccentBlue,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun LoginHistoryHeader() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconBadge(icon = Icons.Filled.Computer)
        Text(
            text = "Riwayat\nLogin",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                color = PrimaryText
            ),
            modifier = Modifier
                .weight(1f)
                .padding(start = 14.dp)
        )
        Text(
            text = "Lihat\nSemua",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                color = AccentBlue
            )
        )
    }
}

@Composable
private fun DeviceItem(
    name: String,
    status: String,
    location: String,
    active: Boolean = false
) {
    Column {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                color = PrimaryText
            )
        )
        Text(
            text = status,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                color = if (active) AccentBlue else SecondaryText
            )
        )
        Text(
            text = location,
            style = MaterialTheme.typography.bodySmall.copy(color = SecondaryText)
        )
    }
}
