package com.app.garapan.presentation.screen.security

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.app.garapan.presentation.navigation.Routes
import com.app.garapan.ui.theme.AccentBlue
import com.app.garapan.ui.theme.PrimaryText
import com.app.garapan.ui.theme.SecondaryText
import com.app.garapan.ui.theme.White

@Composable
fun SecurityScreen(
    navController: NavController
) {
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
                    text = "Kelola kredensial akun Anda untuk menjaga keamanan ruang profesional Anda.",
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
                        icon = Icons.Filled.Lock,
                        title = "Ubah Kata Sandi",
                        subtitle = "Perbarui kata sandi akun Anda secara berkala untuk menjaga keamanan.",
                        action = "Perbarui",
                        onAction = { navController.navigate(Routes.CHANGE_PASSWORD) }
                    )
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
    onAction: () -> Unit
) {
    Row(verticalAlignment = Alignment.Top) {
        IconBadge(icon = icon)
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
                onClick = onAction,
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
private fun IconBadge(icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(Color(0xFFE7ECFF)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AccentBlue,
            modifier = Modifier.size(22.dp)
        )
    }
}
