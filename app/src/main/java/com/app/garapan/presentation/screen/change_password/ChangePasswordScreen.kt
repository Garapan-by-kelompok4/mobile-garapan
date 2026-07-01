package com.app.garapan.presentation.screen.change_password

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Eye
import com.composables.icons.lucide.EyeOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.app.garapan.presentation.components.AppCard
import com.app.garapan.presentation.components.AppPrimaryButton
import com.app.garapan.presentation.components.AppTopBar
import com.app.garapan.presentation.navigation.Routes
import com.app.garapan.ui.theme.ErrorRed
import com.app.garapan.ui.theme.MutedText
import com.app.garapan.ui.theme.PrimaryText
import com.app.garapan.ui.theme.SecondaryText
import com.app.garapan.ui.theme.Surface

@Composable
fun ChangePasswordScreen(
    navController: NavController,
    viewModel: ChangePasswordViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ChangePasswordEvent.ShowMessage ->
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                ChangePasswordEvent.Success -> {
                    Toast.makeText(
                        context,
                        "Kata sandi berhasil diubah. Silakan masuk kembali.",
                        Toast.LENGTH_LONG
                    ).show()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
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
            ChangePasswordTopBar(onBack = { navController.navigateUp() })
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Ubah Kata Sandi",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryText
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Gunakan minimal 8 karakter dengan kombinasi huruf besar, huruf kecil, angka, dan simbol.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = SecondaryText)
                )
                Spacer(modifier = Modifier.height(20.dp))

                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 18.dp)
                    ) {
                        PasswordField(
                            label = "Kata Sandi Saat Ini",
                            value = uiState.currentPassword,
                            visible = uiState.isCurrentVisible,
                            onValueChange = viewModel::onCurrentPasswordChanged,
                            onToggleVisibility = viewModel::onToggleCurrentVisibility
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        PasswordField(
                            label = "Kata Sandi Baru",
                            value = uiState.newPassword,
                            visible = uiState.isNewVisible,
                            onValueChange = viewModel::onNewPasswordChanged,
                            onToggleVisibility = viewModel::onToggleNewVisibility
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        PasswordField(
                            label = "Konfirmasi Kata Sandi Baru",
                            value = uiState.confirmPassword,
                            visible = uiState.isConfirmVisible,
                            onValueChange = viewModel::onConfirmPasswordChanged,
                            onToggleVisibility = viewModel::onToggleConfirmVisibility
                        )
                    }
                }

                if (uiState.errorMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = uiState.errorMessage!!,
                        style = MaterialTheme.typography.bodySmall.copy(color = ErrorRed)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            AppPrimaryButton(
                text = "Perbarui",
                onClick = viewModel::onSubmit,
                enabled = !uiState.isSubmitting,
                isLoading = uiState.isSubmitting,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp)
            )
        }
    }
}

@Composable
private fun ChangePasswordTopBar(onBack: () -> Unit) {
    AppTopBar(title = "Keamanan Akun", onBack = onBack)
}

@Composable
private fun PasswordField(
    label: String,
    value: String,
    visible: Boolean,
    onValueChange: (String) -> Unit,
    onToggleVisibility: () -> Unit
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
                    text = "••••••••",
                    color = MutedText,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            singleLine = true,
            visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = onToggleVisibility) {
                    Icon(
                        imageVector = if (visible) Lucide.EyeOff else Lucide.Eye,
                        contentDescription = if (visible) "Sembunyikan kata sandi" else "Tampilkan kata sandi",
                        tint = MutedText
                    )
                }
            },
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
