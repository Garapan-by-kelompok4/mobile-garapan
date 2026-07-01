package com.app.garapan.presentation.screen.add_portfolio

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.ImagePlus
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.app.garapan.presentation.navigation.NavResults
import com.app.garapan.ui.theme.BorderColor
import com.app.garapan.ui.theme.BrandNavy
import com.app.garapan.ui.theme.ErrorRed
import com.app.garapan.ui.theme.LightGray
import com.app.garapan.ui.theme.OnPrimary
import com.app.garapan.ui.theme.PrimaryText
import com.app.garapan.ui.theme.SecondaryText
import com.app.garapan.ui.theme.Surface

@Composable
fun AddPortfolioScreen(
    navController: NavController,
    viewModel: AddPortfolioViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
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
                AddPortfolioEvent.Saved -> {
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set(NavResults.PORTFOLIO_REFRESH, true)
                    navController.navigateUp()
                }
            }
        }
    }

    Scaffold(containerColor = Surface) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(
                        imageVector = Lucide.ArrowLeft,
                        contentDescription = "Back",
                        tint = BrandNavy
                    )
                }
                Text(
                    text = "Tambah Portofolio",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = BrandNavy
                    )
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Unggah item portofolio baru",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = PrimaryText
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Pilih gambar dari galeri. File akan diunggah otomatis ke server.",
                style = MaterialTheme.typography.bodyMedium.copy(color = SecondaryText)
            )
            Spacer(modifier = Modifier.height(20.dp))
            PortfolioImagePicker(
                imageUri = uiState.imageUri,
                onPickImage = {
                    imagePicker.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
                }
            )
            Spacer(modifier = Modifier.height(20.dp))
            PortfolioField("Judul", uiState.title, viewModel::onTitleChanged)
            Spacer(modifier = Modifier.height(14.dp))
            PortfolioField("Deskripsi", uiState.description, viewModel::onDescriptionChanged, singleLine = false)
            Spacer(modifier = Modifier.height(14.dp))
            PortfolioField("URL Proyek (opsional)", uiState.projectUrl, viewModel::onProjectUrlChanged)
            uiState.errorMessage?.let { message ->
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall.copy(color = ErrorRed)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = viewModel::onSave,
                enabled = !uiState.isSaving && !uiState.isProcessingImage,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandNavy,
                    contentColor = OnPrimary
                )
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(22.dp),
                        strokeWidth = 2.dp,
                        color = OnPrimary
                    )
                } else {
                    Text(
                        text = "Simpan Portofolio",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PortfolioImagePicker(
    imageUri: android.net.Uri?,
    onPickImage: () -> Unit
) {
    Column {
        Text(
            text = "Gambar Portofolio",
            style = MaterialTheme.typography.labelMedium.copy(
                color = SecondaryText,
                fontWeight = FontWeight.SemiBold
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(LightGray)
                .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
                .clickable(onClick = onPickImage),
            contentAlignment = Alignment.Center
        ) {
            if (imageUri == null) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = Lucide.ImagePlus,
                        contentDescription = null,
                        tint = BrandNavy,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Ketuk untuk pilih gambar",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = PrimaryText,
                            fontWeight = FontWeight.SemiBold
                        ),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "JPG atau PNG, maks. 5 MB",
                        style = MaterialTheme.typography.bodySmall.copy(color = SecondaryText),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                AsyncImage(
                    model = imageUri,
                    contentDescription = "Pratinjau gambar portofolio",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
        if (imageUri != null) {
            TextButton(
                onClick = onPickImage,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Ganti gambar")
            }
        }
    }
}

@Composable
private fun PortfolioField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    singleLine: Boolean = true
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                color = SecondaryText,
                fontWeight = FontWeight.SemiBold
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = singleLine,
            minLines = if (singleLine) 1 else 3
        )
    }
}
