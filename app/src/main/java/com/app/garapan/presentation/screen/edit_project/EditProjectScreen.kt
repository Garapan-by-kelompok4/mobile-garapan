package com.app.garapan.presentation.screen.edit_project

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Calendar
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.app.garapan.presentation.components.AppPrimaryButton
import com.app.garapan.presentation.navigation.NavResults
import com.app.garapan.presentation.navigation.Routes
import com.app.garapan.presentation.screen.post_project.BudgetRangeFields
import com.app.garapan.presentation.screen.post_project.CategoryChips
import com.app.garapan.presentation.screen.post_project.DeadlineDatePickerDialog
import com.app.garapan.presentation.screen.post_project.PostProjectField
import com.app.garapan.presentation.screen.post_project.PostProjectTopBar
import com.app.garapan.presentation.screen.post_project.ProjectFormCard
import com.app.garapan.presentation.screen.post_project.ProjectImagePicker
import com.app.garapan.presentation.screen.post_project.ReadOnlyPostProjectField
import com.app.garapan.ui.theme.BrandNavy
import com.app.garapan.ui.theme.MutedText
import com.app.garapan.ui.theme.SecondaryText
import com.app.garapan.ui.theme.Surface

@Composable
fun EditProjectScreen(
    navController: NavController,
    viewModel: EditProjectViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var showDeadlinePicker by remember { mutableStateOf(false) }
    val imagePicker = rememberLauncherForActivityResult(
        contract = PickVisualMedia()
    ) { uri ->
        uri?.let { pickedUri ->
            viewModel.onImageSelected(pickedUri, context)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is EditProjectEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
                EditProjectEvent.Saved -> {
                    Toast.makeText(context, "Perubahan proyek berhasil disimpan.", Toast.LENGTH_SHORT).show()
                    runCatching {
                        navController.getBackStackEntry(Routes.MAIN).savedStateHandle
                    }.getOrNull()?.let(NavResults::publishProjectRefresh)
                    navController.previousBackStackEntry?.savedStateHandle?.let(NavResults::publishProjectRefresh)
                    navController.navigateUp()
                }
            }
        }
    }

    if (showDeadlinePicker) {
        DeadlineDatePickerDialog(
            onDismiss = { showDeadlinePicker = false },
            onDateSelected = {
                viewModel.onDeadlineChanged(it)
                showDeadlinePicker = false
            }
        )
    }

    Scaffold(
        containerColor = Surface,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            PostProjectTopBar(
                title = "Edit Proyek",
                onBack = { navController.navigateUp() }
            )

            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = BrandNavy)
                    }
                }
                uiState.errorMessage != null && uiState.title.isBlank() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = uiState.errorMessage.orEmpty(),
                            style = MaterialTheme.typography.bodyMedium.copy(color = SecondaryText)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = viewModel::retry) {
                            Text("Coba Lagi")
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 20.dp, top = 22.dp, end = 20.dp, bottom = 18.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            ProjectFormCard(title = "Informasi Dasar Proyek") {
                                PostProjectField(
                                    label = "Judul Proyek",
                                    value = uiState.title,
                                    placeholder = "Judul proyek",
                                    onValueChange = viewModel::onTitleChanged
                                )
                                Spacer(modifier = Modifier.height(18.dp))
                                Text(
                                    text = "Kategori Keahlian",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = SecondaryText,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                when {
                                    uiState.isCategoryLoading -> {
                                        Text(
                                            text = "Memuat kategori...",
                                            style = MaterialTheme.typography.bodySmall.copy(color = MutedText)
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                    }
                                    uiState.categoryErrorMessage != null -> {
                                        Text(
                                            text = "Gagal memuat kategori.",
                                            style = MaterialTheme.typography.bodySmall.copy(color = MutedText)
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                    }
                                }
                                CategoryChips(
                                    categories = uiState.categories,
                                    selectedCategory = uiState.selectedCategory,
                                    onCategorySelected = viewModel::onCategorySelected
                                )
                                Spacer(modifier = Modifier.height(18.dp))
                                PostProjectField(
                                    label = "Deskripsi Detail",
                                    value = uiState.description,
                                    placeholder = "Deskripsi proyek",
                                    onValueChange = viewModel::onDescriptionChanged,
                                    minHeight = 108.dp,
                                    singleLine = false
                                )
                                Spacer(modifier = Modifier.height(18.dp))
                                Text(
                                    text = "Gambar Proyek (Opsional)",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = SecondaryText,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                ProjectImagePicker(
                                    imageUri = uiState.imageUri,
                                    existingImageUrl = uiState.existingImageUrl,
                                    isProcessing = uiState.isProcessingImage,
                                    onPickImage = {
                                        imagePicker.launch(
                                            PickVisualMediaRequest(PickVisualMedia.ImageOnly)
                                        )
                                    }
                                )
                            }
                        }
                        item {
                            ProjectFormCard(title = "Ruang Lingkup Pekerjaan") {
                                Text(
                                    text = "Anggaran (Range)",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = SecondaryText,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                BudgetRangeFields(
                                    minimumBudget = uiState.minimumBudget,
                                    maximumBudget = uiState.maximumBudget,
                                    onMinimumBudgetChanged = viewModel::onMinimumBudgetChanged,
                                    onMaximumBudgetChanged = viewModel::onMaximumBudgetChanged
                                )
                                Spacer(modifier = Modifier.height(18.dp))
                                ReadOnlyPostProjectField(
                                    label = "Tenggat Waktu (Deadline)",
                                    value = uiState.deadline,
                                    placeholder = "mm/dd/yyyy",
                                    leadingIcon = Lucide.Calendar,
                                    onClick = { showDeadlinePicker = true }
                                )
                            }
                        }
                        item {
                            AppPrimaryButton(
                                text = "Simpan Perubahan",
                                onClick = viewModel::onSave,
                                enabled = !uiState.isSaving && !uiState.isProcessingImage,
                                isLoading = uiState.isSaving
                            )
                        }
                    }
                }
            }
        }
    }
}
