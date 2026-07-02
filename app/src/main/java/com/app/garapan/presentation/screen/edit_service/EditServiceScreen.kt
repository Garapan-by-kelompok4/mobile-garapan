package com.app.garapan.presentation.screen.edit_service

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.ImagePlus
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.app.garapan.presentation.components.AppCard
import com.app.garapan.presentation.components.AppPrimaryButton
import com.app.garapan.presentation.components.AppTopBar
import com.app.garapan.presentation.navigation.NavResults
import com.app.garapan.presentation.navigation.Routes
import com.app.garapan.ui.theme.AccentBlue
import com.app.garapan.ui.theme.BorderColor
import com.app.garapan.ui.theme.BrandNavy
import com.app.garapan.ui.theme.ErrorRed
import com.app.garapan.ui.theme.LightGray
import com.app.garapan.ui.theme.MutedText
import com.app.garapan.ui.theme.PrimaryText
import com.app.garapan.ui.theme.SecondaryText
import com.app.garapan.ui.theme.Surface

@Composable
fun EditServiceScreen(
    navController: NavController,
    viewModel: EditServiceViewModel = hiltViewModel()
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
                is EditServiceEvent.Saved -> {
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.let { NavResults.publishJasaSaved(it, event.jasa) }
                    runCatching {
                        navController.getBackStackEntry(Routes.MAIN).savedStateHandle
                    }.getOrNull()?.let { NavResults.publishJasaSaved(it, event.jasa) }
                    navController.navigateUp()
                }
            }
        }
    }

    Scaffold(containerColor = Surface) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = BrandNavy)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        ) {
            EditServiceTopBar(onBack = { navController.navigateUp() })
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, top = 22.dp, end = 20.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    EditServiceHero()
                }
                item {
                    FormCard(title = "Gambar Layanan") {
                        JasaImagePicker(
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
                    FormCard(title = "Informasi Dasar Layanan") {
                        EditTextField(
                            label = "Judul Layanan",
                            value = uiState.title,
                            placeholder = "Misal: Pembuatan Landing Page Company Profile",
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
                                    text = "Gagal memuat kategori. Periksa koneksi lalu buka ulang halaman.",
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
                        EditTextField(
                            label = "Deskripsi Detail",
                            value = uiState.description,
                            placeholder = "Jelaskan layanan, cakupan pekerjaan, dan deliverable yang ditawarkan...",
                            onValueChange = viewModel::onDescriptionChanged,
                            minHeight = 108.dp,
                            singleLine = false
                        )
                    }
                }
                item {
                    FormCard(title = "Harga Layanan") {
                        Text(
                            text = "Harga",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = SecondaryText,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        PriceField(
                            value = uiState.price,
                            onValueChange = viewModel::onPriceChanged,
                            placeholder = "500.000",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                if (uiState.errorMessage != null) {
                    item {
                        Text(
                            text = uiState.errorMessage.orEmpty(),
                            style = MaterialTheme.typography.bodySmall.copy(color = ErrorRed),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
                item {
                    AppPrimaryButton(
                        text = if (viewModel.isNewJasa) "Buat Layanan" else "Simpan Perubahan",
                        onClick = viewModel::onSave,
                        enabled = !uiState.isSaving,
                        isLoading = uiState.isSaving
                    )
                }
            }
        }
    }
}

@Composable
private fun EditServiceTopBar(onBack: () -> Unit) {
    AppTopBar(title = "Edit Layanan", onBack = onBack)
}

@Composable
private fun EditServiceHero() {
    Column {
        Row {
            Text(
                text = "Kelola ",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = PrimaryText,
                    lineHeight = 30.sp
                )
            )
            Text(
                text = "Layanan",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = AccentBlue,
                    lineHeight = 30.sp
                )
            )
            Text(
                text = " mu",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = PrimaryText,
                    lineHeight = 30.sp
                )
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Perbarui kebutuhan proyek dan layanan yang tampil untuk freelancer.",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = SecondaryText,
                lineHeight = 20.sp
            )
        )
    }
}

@Composable
private fun FormCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(22.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryText
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(BorderColor.copy(alpha = 0.7f))
            )
            Spacer(modifier = Modifier.height(18.dp))
            content()
        }
    }
}

@Composable
private fun CategoryChips(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { category ->
            val selected = category == selectedCategory
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .clickable { onCategorySelected(category) }
                    .background(if (selected) AccentBlue.copy(alpha = 0.14f) else Surface)
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = category,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = if (selected) AccentBlue else SecondaryText
                    )
                )
            }
        }
    }
}

@Composable
private fun PriceField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Surface)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Rp",
            style = MaterialTheme.typography.bodySmall.copy(
                color = SecondaryText,
                fontWeight = FontWeight.Medium
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        BasicTextField(
            value = value,
            onValueChange = { raw -> onValueChange(raw.filter(Char::isDigit).take(9)) },
            modifier = Modifier.weight(1f),
            textStyle = MaterialTheme.typography.bodySmall.copy(
                color = PrimaryText,
                fontWeight = FontWeight.SemiBold
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            visualTransformation = ThousandSeparatorTransformation,
            cursorBrush = SolidColor(BrandNavy),
            decorationBox = { inner ->
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodySmall.copy(color = MutedText),
                        maxLines = 1
                    )
                }
                inner()
            }
        )
    }
}

@Composable
private fun EditTextField(
    label: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    minHeight: androidx.compose.ui.unit.Dp = 52.dp,
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(minHeight)
                .clip(RoundedCornerShape(8.dp))
                .background(Surface)
                .padding(horizontal = 12.dp, vertical = if (singleLine) 0.dp else 12.dp),
            verticalAlignment = if (singleLine) Alignment.CenterVertically else Alignment.Top
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = PrimaryText),
                singleLine = singleLine,
                cursorBrush = SolidColor(BrandNavy),
                decorationBox = { inner ->
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MutedText,
                                lineHeight = 20.sp
                            ),
                            maxLines = if (singleLine) 1 else 4,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    inner()
                }
            )
        }
    }
}

@Composable
private fun JasaImagePicker(
    imageUri: android.net.Uri?,
    existingImageUrl: String,
    isProcessing: Boolean,
    onPickImage: () -> Unit
) {
    val previewModel = imageUri ?: existingImageUrl.takeIf { it.isNotBlank() }

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(LightGray)
                .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
                .clickable(enabled = !isProcessing, onClick = onPickImage),
            contentAlignment = Alignment.Center
        ) {
            when {
                isProcessing -> CircularProgressIndicator(color = BrandNavy)
                previewModel == null -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
                    }
                }
                else -> {
                    AsyncImage(
                        model = previewModel,
                        contentDescription = "Pratinjau gambar jasa",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
        if (previewModel != null && !isProcessing) {
            TextButton(
                onClick = onPickImage,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Ganti gambar")
            }
        }
    }
}

private object ThousandSeparatorTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text
        val formatted = digits.reversed().chunked(3).joinToString(".").reversed()
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val dotsAdded = ((offset - 1) / 3).coerceAtLeast(0)
                return (offset + dotsAdded).coerceAtMost(formatted.length)
            }

            override fun transformedToOriginal(offset: Int): Int =
                formatted.substring(0, offset.coerceAtMost(formatted.length))
                    .count(Char::isDigit)
                    .coerceAtMost(digits.length)
        }
        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}
