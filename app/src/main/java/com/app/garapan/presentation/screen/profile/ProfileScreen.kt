package com.app.garapan.presentation.screen.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.app.garapan.R
import com.app.garapan.domain.model.Role
import com.app.garapan.presentation.navigation.Routes
import com.app.garapan.ui.theme.AccentBlue
import com.app.garapan.ui.theme.BorderColor
import com.app.garapan.ui.theme.BrandNavy
import com.app.garapan.ui.theme.ErrorRed
import com.app.garapan.ui.theme.MutedText
import com.app.garapan.ui.theme.PrimaryText
import com.app.garapan.ui.theme.SecondaryText
import com.app.garapan.ui.theme.Surface
import com.app.garapan.ui.theme.White
import kotlinx.coroutines.flow.collectLatest

private data class ProfileMenuItem(
    val label: String,
    val icon: ImageVector,
    val destructive: Boolean = false,
    val onClick: () -> Unit = {}
)

@Composable
fun ProfileScreen(
    navController: NavController,
    showBackButton: Boolean = true,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is ProfileEvent.Navigate -> navController.navigate(event.route) {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }

    val initials = uiState.name
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
        .ifBlank { "?" }

    Scaffold(containerColor = Surface) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                ProfileHeader(
                    showBackButton = showBackButton,
                    onBack = { navController.navigateUp() }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                ProfileHeaderCard(
                    name = uiState.name,
                    email = uiState.email,
                    initials = initials,
                    avatarUrl = uiState.avatarUrl,
                    onClick = { navController.navigate(Routes.EDIT_PROFILE) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                ProfileSectionLabel(
                    title = "Preferensi",
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            item {
                ProfileMenuCard(
                    items = buildList {
                        add(
                            ProfileMenuItem("Keamanan Akun", Icons.Filled.Security) {
                                navController.navigate(Routes.SECURITY)
                            }
                        )
                        if (uiState.role == Role.MAHASISWA) {
                            add(
                                ProfileMenuItem("Layanan Saya", Icons.Filled.Store) {
                                    navController.navigate(Routes.PROFILE_SERVICES)
                                }
                            )
                            add(
                                ProfileMenuItem("Portofolio", Icons.Filled.Work) {
                                    navController.navigate(Routes.PROFILE_PORTFOLIO)
                                }
                            )
                            add(
                                ProfileMenuItem("Keahlian", Icons.Filled.Computer) {
                                    navController.navigate(Routes.SKILLS)
                                }
                            )
                        }
                        if (uiState.role == Role.KLIEN || uiState.role == Role.MAHASISWA || uiState.role == Role.ADMIN) {
                            add(
                                ProfileMenuItem(
                                    label = if (uiState.role == Role.MAHASISWA) {
                                        "Proyek Diambil"
                                    } else {
                                        "Proyek Saya"
                                    },
                                    icon = Icons.Filled.Assignment
                                ) {
                                    navController.navigate(Routes.MY_PROJECTS)
                                }
                            )
                        }
                    },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
                ProfileSectionLabel(
                    title = "Lainnya",
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            item {
                ProfileMenuCard(
                    items = listOf(
                        ProfileMenuItem("Riwayat Pesanan", Icons.Filled.ShoppingCart) {
                            navController.navigate(Routes.ORDER_HISTORY)
                        },
                        ProfileMenuItem("Pusat Bantuan", Icons.AutoMirrored.Filled.Help),
                        ProfileMenuItem("Syarat & Ketentuan", Icons.AutoMirrored.Filled.Article),
                        ProfileMenuItem("Kebijakan Privasi", Icons.Filled.PrivacyTip),
                        ProfileMenuItem(
                            label = "Keluar (Log Out)",
                            icon = Icons.AutoMirrored.Filled.Logout,
                            destructive = true,
                            onClick = { viewModel.onLogout() }
                        )
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun ProfileHeader(
    showBackButton: Boolean,
    onBack: () -> Unit
) {
    if (showBackButton) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(White)
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
                text = "Profile",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = BrandNavy
                )
            )
        }
    } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(White)
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.logo_garapan),
                contentDescription = "Garapan Logo",
                modifier = Modifier.size(34.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Profile",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = BrandNavy
                )
            )
        }
    }
}

@Composable
private fun ProfileHeaderCard(
    name: String,
    email: String,
    initials: String,
    avatarUrl: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProfileAvatar(initials = initials, avatarUrl = avatarUrl)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 14.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText
                    )
                )
                Text(
                    text = email,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = SecondaryText
                    )
                )
            }
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = "Open profile detail",
                tint = MutedText,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun ProfileAvatar(initials: String, avatarUrl: String?) {
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(BrandNavy.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center
    ) {
        if (avatarUrl != null) {
            AsyncImage(
                model = avatarUrl,
                contentDescription = "Profile photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
            )
        } else {
            Text(
                text = initials,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = BrandNavy
                )
            )
        }
    }
}

@Composable
private fun ProfileSectionLabel(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge.copy(
            fontWeight = FontWeight.ExtraBold,
            color = SecondaryText,
            letterSpacing = 0.sp
        ),
        modifier = modifier
    )
}

@Composable
private fun ProfileMenuCard(
    items: List<ProfileMenuItem>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            items.forEachIndexed { index, item ->
                ProfileMenuRow(item = item)
                if (index != items.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 62.dp),
                        thickness = 1.dp,
                        color = BorderColor
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileMenuRow(item: ProfileMenuItem) {
    val contentColor = when {
        item.destructive -> ErrorRed
        else -> PrimaryText
    }
    val iconTint = when {
        item.destructive -> ErrorRed
        else -> AccentBlue
    }
    val iconBackground = when {
        item.destructive -> ErrorRed.copy(alpha = 0.1f)
        else -> AccentBlue.copy(alpha = 0.1f)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = item.onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(iconBackground),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
        }
        Text(
            text = item.label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium,
                color = contentColor
            ),
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 14.dp)
        )
        if (!item.destructive) {
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MutedText,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
