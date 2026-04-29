package com.app.garapan.presentation.screen.edit_profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.app.garapan.ui.theme.BrandNavy
import com.app.garapan.ui.theme.MutedText
import com.app.garapan.ui.theme.PrimaryText
import com.app.garapan.ui.theme.White

@Composable
fun EditProfileScreen(
    navController: NavController,
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(containerColor = White) { innerPadding ->
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
                    .padding(horizontal = 28.dp)
            ) {
                Spacer(modifier = Modifier.height(10.dp))
                EditProfileAvatar()
                Spacer(modifier = Modifier.height(40.dp))

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
                    onValueChange = viewModel::onPhoneNumberChanged
                )
                Spacer(modifier = Modifier.height(16.dp))
                EditProfileField(
                    label = "Status",
                    value = uiState.status,
                    placeholder = "Individu",
                    onValueChange = viewModel::onStatusChanged
                )
                Spacer(modifier = Modifier.height(16.dp))
                EditProfileField(
                    label = "Perusahaan / Organisasi / Proyek",
                    value = uiState.organization,
                    placeholder = "Nama PT / Nama Org / Nama Proyek",
                    onValueChange = viewModel::onOrganizationChanged
                )
                Spacer(modifier = Modifier.height(16.dp))
                EditProfileField(
                    label = "Social accounts",
                    value = uiState.socialAccount,
                    placeholder = "Link LinkedIn",
                    onValueChange = viewModel::onSocialAccountChanged
                )
                Spacer(modifier = Modifier.height(40.dp))
            }

            Button(
                onClick = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp)
                    .padding(bottom = 22.dp)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandNavy,
                    contentColor = White
                ),
                shape = RoundedCornerShape(50.dp)
            ) {
                Text(
                    text = "Simpan",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.ExtraBold)
                )
            }
        }
    }
}

@Composable
private fun EditProfileTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = PrimaryText,
                modifier = Modifier.size(30.dp)
            )
        }
        Text(
            text = "Profile",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                color = PrimaryText
            )
        )
    }
}

@Composable
private fun EditProfileAvatar() {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(112.dp)
                .clip(CircleShape)
                .background(BrandNavy.copy(alpha = 0.12f))
                .border(2.dp, BrandNavy, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "ML",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = BrandNavy
                )
            )
        }
        Box(
            modifier = Modifier
                .padding(top = 78.dp, start = 78.dp)
                .size(30.dp)
                .clip(CircleShape)
                .background(BrandNavy)
                .border(2.dp, White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.CameraAlt,
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
    trailingIcon: (@Composable () -> Unit)? = null
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                color = PrimaryText
            )
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(RoundedCornerShape(5.dp))
                .border(1.dp, PrimaryText, RoundedCornerShape(5.dp))
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = PrimaryText),
                singleLine = true,
                cursorBrush = SolidColor(BrandNavy),
                decorationBox = { inner ->
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyMedium.copy(color = MutedText)
                        )
                    }
                    inner()
                }
            )
            trailingIcon?.invoke()
        }
    }
}
