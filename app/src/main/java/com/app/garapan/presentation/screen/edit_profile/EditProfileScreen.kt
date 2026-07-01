package com.app.garapan.presentation.screen.edit_profile

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Camera
import com.composables.icons.lucide.ChevronDown
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.app.garapan.domain.model.ProfileStatus
import com.app.garapan.presentation.components.AppCard
import com.app.garapan.presentation.components.AppPrimaryButton
import com.app.garapan.presentation.components.AppTopBar
import com.app.garapan.ui.theme.BrandNavy
import com.app.garapan.ui.theme.MutedText
import com.app.garapan.ui.theme.PrimaryText
import com.app.garapan.ui.theme.SecondaryText
import com.app.garapan.ui.theme.Surface
import com.app.garapan.ui.theme.White

@Composable
fun EditProfileScreen(
    navController: NavController,
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val avatarPicker = rememberLauncherForActivityResult(PickVisualMedia()) { uri ->
        if (uri != null) viewModel.onAvatarSelected(uri)
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is EditProfileEvent.ShowMessage ->
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                EditProfileEvent.Saved -> {
                    Toast.makeText(context, "Profil berhasil disimpan.", Toast.LENGTH_SHORT).show()
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
        ) {
            EditProfileTopBar(onBack = { navController.navigateUp() })
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                EditProfileAvatar(
                    avatarUrl = uiState.avatarUrl,
                    initials = uiState.initials,
                    isUploading = uiState.isUploadingAvatar,
                    onPickImage = {
                        avatarPicker.launch(
                            PickVisualMediaRequest(PickVisualMedia.ImageOnly)
                        )
                    }
                )
                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = "Informasi Profil",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = SecondaryText,
                        letterSpacing = 0.sp
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))
                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 18.dp)
                    ) {
                        EditProfileField(
                            label = "Nama Lengkap",
                            value = uiState.fullName,
                            placeholder = "Nama Lengkap",
                            onValueChange = viewModel::onFullNameChanged
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        EditProfileField(
                            label = "Nomor Telepon",
                            value = uiState.phoneNumber,
                            placeholder = "+6212345678",
                            onValueChange = viewModel::onPhoneNumberChanged,
                            keyboardType = KeyboardType.Phone
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        EditProfileStatusField(
                            status = uiState.status,
                            expanded = uiState.isStatusDropdownExpanded,
                            onToggle = viewModel::onStatusDropdownToggle,
                            onDismiss = viewModel::onStatusDropdownDismiss,
                            onSelect = viewModel::onStatusSelected
                        )
                        if (uiState.isKlien) {
                            Spacer(modifier = Modifier.height(16.dp))
                            EditProfileField(
                                label = "Perusahaan / Organisasi / Proyek",
                                value = uiState.organization,
                                placeholder = "Nama PT / Nama Org / Nama Proyek",
                                onValueChange = viewModel::onOrganizationChanged
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        EditProfileField(
                            label = "Social accounts",
                            value = uiState.linkedinUrl,
                            placeholder = "Link LinkedIn",
                            onValueChange = viewModel::onLinkedinUrlChanged,
                            keyboardType = KeyboardType.Uri
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            AppPrimaryButton(
                text = "Simpan",
                onClick = viewModel::onSave,
                enabled = !uiState.isSaving && !uiState.isLoading,
                isLoading = uiState.isSaving,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp)
            )
        }
    }
}

@Composable
private fun EditProfileTopBar(onBack: () -> Unit) {
    AppTopBar(title = "Edit Profile", onBack = onBack)
}

@Composable
private fun EditProfileAvatar(
    avatarUrl: String?,
    initials: String,
    isUploading: Boolean,
    onPickImage: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(112.dp)
                .clip(CircleShape)
                .clickable(enabled = !isUploading, onClick = onPickImage)
                .background(BrandNavy.copy(alpha = 0.12f))
                .border(2.dp, BrandNavy, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (avatarUrl != null) {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = "Profile photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(112.dp)
                        .clip(CircleShape)
                )
            } else {
                Text(
                    text = initials,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = BrandNavy
                    )
                )
            }
            if (isUploading) {
                Box(
                    modifier = Modifier
                        .size(112.dp)
                        .clip(CircleShape)
                        .background(BrandNavy.copy(alpha = 0.35f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = White, strokeWidth = 2.dp)
                }
            }
        }
        Box(
            modifier = Modifier
                .padding(top = 78.dp, start = 78.dp)
                .size(30.dp)
                .clip(CircleShape)
                .background(BrandNavy)
                .border(2.dp, White, CircleShape)
                .clickable(enabled = !isUploading, onClick = onPickImage),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Lucide.Camera,
                contentDescription = "Change photo",
                tint = White,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun EditProfileField(
    label: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Medium,
                color = PrimaryText
            )
        )
        Spacer(modifier = Modifier.height(6.dp))
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp)),
            placeholder = {
                Text(
                    text = placeholder,
                    color = MutedText,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Surface,
                unfocusedContainerColor = Surface,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                focusedTextColor = PrimaryText,
                unfocusedTextColor = PrimaryText
            ),
            shape = RoundedCornerShape(8.dp),
            textStyle = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun EditProfileStatusField(
    status: ProfileStatus?,
    expanded: Boolean,
    onToggle: () -> Unit,
    onDismiss: () -> Unit,
    onSelect: (ProfileStatus) -> Unit
) {
    Column {
        Text(
            text = "Status",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Medium,
                color = PrimaryText
            )
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Surface)
                    .clickable(onClick = onToggle)
                    .padding(horizontal = 16.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = status?.label ?: "Individu",
                    color = if (status != null) PrimaryText else MutedText,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Lucide.ChevronDown,
                    contentDescription = null,
                    tint = MutedText
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = onDismiss
            ) {
                profileStatusOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.label) },
                        onClick = { onSelect(option) }
                    )
                }
            }
        }
    }
}
