package com.app.garapan.presentation.screen.profile_services

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.Paid
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.app.garapan.presentation.navigation.NavResults
import com.app.garapan.presentation.navigation.Routes
import com.app.garapan.ui.theme.AccentBlue
import com.app.garapan.ui.theme.LightGray
import com.app.garapan.ui.theme.MutedText
import com.app.garapan.ui.theme.PrimaryText
import com.app.garapan.ui.theme.SecondaryText
import com.app.garapan.ui.theme.Surface
import com.app.garapan.ui.theme.White

@Composable
fun ProfileServicesScreen(
    navController: NavController,
    showBackButton: Boolean = true,
    viewModel: ProfileServicesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val refreshStateHandle = remember(navController, showBackButton) {
        if (!showBackButton) {
            runCatching { navController.getBackStackEntry(Routes.MAIN).savedStateHandle }.getOrNull()
        } else {
            navController.currentBackStackEntry?.savedStateHandle
        }
    }

    LaunchedEffect(refreshStateHandle) {
        val handle = refreshStateHandle ?: return@LaunchedEffect
        handle.getStateFlow(NavResults.JASA_REFRESH, false).collect { shouldRefresh ->
            if (!shouldRefresh) return@collect
            NavResults.readJasaSaved(handle)?.let(viewModel::applySavedJasa)
            NavResults.clearJasaSaved(handle)
            viewModel.loadMyJasa(refresh = true)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ProfileServicesEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        containerColor = Surface,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 20.dp, top = 8.dp, end = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                ProfileServicesTopBar(
                    onBack = { navController.navigateUp() },
                    showBackButton = showBackButton
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Layanan Saya",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryText
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Kelola layanan yang Anda tawarkan di marketplace.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = SecondaryText,
                        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                    )
                )
            }

            item {
                Button(
                    onClick = { navController.navigate(Routes.editServiceRoute("new")) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                ) {
                    Text(
                        text = "Buat Layanan",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = White
                        )
                    )
                }
            }

            if (uiState.isRefreshing) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = AccentBlue,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Memperbarui layanan...",
                            style = MaterialTheme.typography.bodySmall.copy(color = SecondaryText)
                        )
                    }
                }
            }

            when {
                uiState.isLoading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = AccentBlue)
                        }
                    }
                }
                uiState.loadErrorMessage != null && uiState.services.isEmpty() -> {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = uiState.loadErrorMessage.orEmpty(),
                                style = MaterialTheme.typography.bodyMedium.copy(color = SecondaryText)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { viewModel.loadMyJasa() },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                            ) {
                                Text(text = "Coba Lagi")
                            }
                        }
                    }
                }
                else -> {
                    if (uiState.services.isEmpty()) {
                        item {
                            Text(
                                text = "Belum ada layanan. Ketuk Buat Layanan untuk mulai.",
                                style = MaterialTheme.typography.bodyMedium.copy(color = SecondaryText),
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    } else {
                        items(uiState.services, key = { it.id }) { service ->
                            ProfileServiceCard(
                                service = service,
                                isDeleting = uiState.isDeleting,
                                onClick = { navController.navigate(Routes.editServiceRoute(service.id)) },
                                onEditClick = { navController.navigate(Routes.editServiceRoute(service.id)) },
                                onDeleteClick = { viewModel.onDeleteService(service.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileServicesTopBar(
    onBack: () -> Unit,
    showBackButton: Boolean = true
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showBackButton) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = AccentBlue
                )
            }
        }
        Text(
            text = "Layanan Saya",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                color = AccentBlue
            )
        )
    }
}

@Composable
private fun SectionHeader(
    title: String,
    showEditAction: Boolean = false,
    onEditClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                color = PrimaryText
            ),
            modifier = Modifier.weight(1f)
        )
        if (showEditAction) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .clickable(onClick = onEditClick)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = AccentBlue,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Edit",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = AccentBlue,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@Composable
private fun SkillPanel(skills: List<String>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(AccentBlue.copy(alpha = 0.05f))
            .padding(18.dp)
    ) {
        androidx.compose.foundation.layout.FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            skills.forEach { skill ->
                Text(
                    text = skill,
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = SecondaryText,
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(Color(0xFFE4E7EA))
                        .padding(horizontal = 16.dp, vertical = 9.dp)
                )
            }
        }
    }
}

@Composable
private fun ProfileServiceCard(
    service: ProfileServiceItem,
    isDeleting: Boolean,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(118.dp)
                    .background(LightGray),
                contentAlignment = Alignment.TopEnd
            ) {
                if (service.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = service.imageUrl,
                        contentDescription = service.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Row(
                    modifier = Modifier.padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ServiceActionButton(
                        icon = Icons.Default.Edit,
                        contentDescription = "Edit layanan",
                        onClick = onEditClick
                    )
                    ServiceActionButton(
                        icon = Icons.Default.Delete,
                        contentDescription = "Hapus layanan",
                        enabled = !isDeleting,
                        onClick = onDeleteClick
                    )
                }
            }
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = service.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryText
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(10.dp))
                ServiceMetaRow(icon = Icons.Default.Schedule, text = service.deadline)
                Spacer(modifier = Modifier.height(5.dp))
                ServiceMetaRow(icon = Icons.Outlined.Paid, text = service.budget)
                Spacer(modifier = Modifier.height(5.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = null,
                        tint = MutedText,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = service.teamSize,
                        style = MaterialTheme.typography.bodySmall.copy(color = SecondaryText),
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    StatusChip(status = service.status)
                }
            }
        }
    }
}

@Composable
private fun ServiceActionButton(
    icon: ImageVector,
    contentDescription: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(50.dp))
            .background(White.copy(alpha = 0.92f)),
        contentAlignment = Alignment.Center
    ) {
        IconButton(onClick = onClick, enabled = enabled) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = SecondaryText,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun ServiceMetaRow(
    icon: ImageVector,
    text: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MutedText,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(color = SecondaryText),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun StatusChip(status: String) {
    val isDone = status.equals("Selesai", ignoreCase = true)
    Text(
        text = status,
        style = MaterialTheme.typography.labelSmall.copy(
            color = White,
            fontWeight = FontWeight.ExtraBold
        ),
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(if (isDone) Color(0xFF22C55E) else AccentBlue)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}
