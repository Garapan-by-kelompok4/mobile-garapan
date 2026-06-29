package com.app.garapan.presentation.screen.my_projects

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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.app.garapan.presentation.navigation.NavResults
import com.app.garapan.presentation.navigation.Routes
import com.app.garapan.ui.theme.AccentBlue
import com.app.garapan.ui.theme.BrandNavy
import com.app.garapan.ui.theme.ErrorRed
import com.app.garapan.ui.theme.MutedText
import com.app.garapan.ui.theme.PrimaryText
import com.app.garapan.ui.theme.SecondaryText
import com.app.garapan.ui.theme.Surface
import com.app.garapan.ui.theme.White

@Composable
fun MyProjectsScreen(
    navController: NavController,
    showBackButton: Boolean = true,
    viewModel: MyProjectsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
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
        handle.getStateFlow(NavResults.PROJECT_REFRESH, false).collect { shouldRefresh ->
            if (!shouldRefresh) return@collect
            NavResults.clearProjectRefresh(handle)
            viewModel.loadProjects(refresh = true)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is MyProjectsEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
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
                MyProjectsTopBar(
                    title = uiState.screenTitle,
                    onBack = { navController.navigateUp() },
                    showBackButton = showBackButton
                )
            }

            when {
                uiState.isLoading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = BrandNavy)
                        }
                    }
                }
                uiState.loadErrorMessage != null -> {
                    item {
                        ErrorState(
                            message = uiState.loadErrorMessage.orEmpty(),
                            onRetry = { viewModel.loadProjects() }
                        )
                    }
                }
                uiState.projects.isEmpty() -> {
                    item {
                        EmptyState(title = uiState.screenTitle)
                    }
                }
                else -> {
                    items(uiState.projects, key = { it.id }) { project ->
                        MyProjectCard(
                            project = project,
                            canDelete = uiState.canDelete,
                            isDeleting = uiState.isDeleting,
                            onClick = { navController.navigate(Routes.projectDetailRoute(project.id)) },
                            onEdit = { navController.navigate(Routes.editProjectRoute(project.id)) },
                            onDelete = { viewModel.onDeleteProject(project.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MyProjectsTopBar(
    title: String,
    onBack: () -> Unit,
    showBackButton: Boolean
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
            text = title,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                color = BrandNavy
            )
        )
    }
}

@Composable
private fun MyProjectCard(
    project: MyProjectItem,
    canDelete: Boolean,
    isDeleting: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = project.category,
                        style = MaterialTheme.typography.labelMedium.copy(color = AccentBlue)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = project.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = PrimaryText
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (canDelete) {
                    Row {
                        if (project.isEditable) {
                            IconButton(
                                onClick = onEdit,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Edit,
                                    contentDescription = "Edit project",
                                    tint = AccentBlue
                                )
                            }
                        }
                        IconButton(
                            onClick = onDelete,
                            enabled = !isDeleting,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Delete project",
                                tint = ErrorRed
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            ProjectMetaRow(icon = Icons.Outlined.Paid, text = project.budget)
            Spacer(modifier = Modifier.height(6.dp))
            ProjectMetaRow(icon = Icons.Filled.Schedule, text = project.deadline)
            if (project.assigneeName.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Freelancer: ${project.assigneeName}",
                    style = MaterialTheme.typography.bodySmall.copy(color = SecondaryText)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = project.status,
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(AccentBlue.copy(alpha = 0.12f))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall.copy(
                    color = AccentBlue,
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    }
}

@Composable
private fun ProjectMetaRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MutedText,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(color = SecondaryText)
        )
    }
}

@Composable
private fun EmptyState(title: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (title.contains("Diambil", ignoreCase = true)) {
                "Belum ada proyek yang diambil."
            } else {
                "Belum ada proyek yang diposting."
            },
            style = MaterialTheme.typography.bodyMedium.copy(color = SecondaryText)
        )
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium.copy(color = SecondaryText)
        )
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = BrandNavy)
        ) {
            Text("Coba Lagi")
        }
    }
}
