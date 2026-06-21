package com.app.garapan.presentation.screen.auth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.app.garapan.R
import com.app.garapan.data.auth.GoogleAuthClient
import com.app.garapan.presentation.navigation.Routes
import com.app.garapan.ui.theme.AccentBlue
import com.app.garapan.ui.theme.BrandNavy
import com.app.garapan.ui.theme.BorderColor
import com.app.garapan.ui.theme.MutedText
import com.app.garapan.ui.theme.OnPrimary
import com.app.garapan.ui.theme.PrimaryText
import com.app.garapan.ui.theme.SecondaryText
import com.app.garapan.ui.theme.Surface
import com.app.garapan.ui.theme.White
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val googleAuthClient = remember { GoogleAuthClient() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is LoginEvent.Navigate -> navController.navigate(event.route) {
                    popUpTo(Routes.SPLASH) { inclusive = true }
                }
                is LoginEvent.Toast -> Unit
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            LoginHeader()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(White)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = "Welcome Back",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Sign in to access your dashboard.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = SecondaryText)
                )

                Spacer(modifier = Modifier.height(24.dp))

                EmailField(
                    value = uiState.email,
                    onValueChange = viewModel::onEmailChanged,
                    placeholder = "your@email.com"
                )

                Spacer(modifier = Modifier.height(16.dp))

                PasswordField(
                    value = uiState.password,
                    onValueChange = viewModel::onPasswordChanged,
                    isPasswordVisible = uiState.isPasswordVisible,
                    onToggleVisibility = viewModel::onTogglePasswordVisibility,
                    onForgotPassword = { navController.navigate(Routes.FORGOT_PASSWORD) }
                )

                if (uiState.errorMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = uiState.errorMessage.orEmpty(),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = com.app.garapan.ui.theme.ErrorRed,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                if (uiState.infoMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = uiState.infoMessage.orEmpty(),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = AccentBlue,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                if (uiState.canResendVerification) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Resend verification email",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = AccentBlue,
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.clickable(enabled = !uiState.isLoading) {
                            viewModel.onResendVerification()
                        }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = viewModel::onSignIn,
                    enabled = !uiState.isLoading && !uiState.requiresTwoFactor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandNavy,
                        contentColor = OnPrimary
                    )
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = OnPrimary
                        )
                    } else {
                        Text(
                            text = "Sign In",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                OrDivider()

                Spacer(modifier = Modifier.height(16.dp))

                GoogleSignInButton(
                    enabled = !uiState.isLoading,
                    onClick = {
                        coroutineScope.launch {
                            val activity = context.findActivity()
                            if (activity == null) {
                                viewModel.onGoogleSignInError("Unable to start Google sign-in from this screen.")
                                return@launch
                            }

                            try {
                                val idToken = googleAuthClient.getIdToken(activity)
                                viewModel.onGoogleSignIn(idToken)
                            } catch (_: GetCredentialCancellationException) {
                                // User dismissed the Google sheet.
                            } catch (_: NoCredentialException) {
                                // No account selected or available; keep the screen unchanged.
                            } catch (_: Exception) {
                                viewModel.onGoogleSignInError("Unable to continue with Google. Please try again.")
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(32.dp))

                BottomSignUpText(
                    onCreateAccount = { navController.navigate(Routes.REGISTER) }
                )

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun LoginHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "GARAPAN",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                color = BrandNavy,
                letterSpacing = 2.sp
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "The IT Project Marketplace",
            style = MaterialTheme.typography.bodySmall.copy(color = SecondaryText)
        )
    }
}

@Composable
private fun EmailField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "your@email.com"
) {
    Column {
        Text(
            text = "Email Address",
            style = MaterialTheme.typography.labelMedium.copy(
                color = PrimaryText,
                fontWeight = FontWeight.Medium
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
                Text(text = placeholder, color = MutedText)
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Surface,
                unfocusedContainerColor = Surface,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                focusedTextColor = PrimaryText,
                unfocusedTextColor = PrimaryText,
            ),
            shape = RoundedCornerShape(8.dp)
        )
    }
}

@Composable
private fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    isPasswordVisible: Boolean,
    onToggleVisibility: () -> Unit,
    onForgotPassword: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Password",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = PrimaryText,
                    fontWeight = FontWeight.Medium
                )
            )
            Text(
                text = "Forgot?",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = AccentBlue,
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier.clickable { onForgotPassword() }
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp)),
            singleLine = true,
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = onToggleVisibility) {
                    Icon(
                        painter = painterResource(
                            if (isPasswordVisible) R.drawable.ic_visibility_off
                            else R.drawable.ic_visibility
                        ),
                        contentDescription = if (isPasswordVisible) "Hide password" else "Show password",
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
                unfocusedTextColor = PrimaryText,
            ),
            shape = RoundedCornerShape(8.dp)
        )
    }
}

@Composable
private fun OrDivider() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = BorderColor)
        Text(
            text = "  Or continue with  ",
            style = MaterialTheme.typography.bodySmall.copy(color = SecondaryText)
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = BorderColor)
    }
}

@Composable
private fun GoogleSignInButton(
    enabled: Boolean,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryText),
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_google),
            contentDescription = "Google",
            modifier = Modifier.size(20.dp),
            tint = Color.Unspecified
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Google",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium,
                color = PrimaryText
            )
        )
    }
}

private tailrec fun Context.findActivity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }

@Composable
private fun BottomSignUpText(onCreateAccount: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Don't have an account? ",
            style = MaterialTheme.typography.bodyMedium.copy(color = SecondaryText)
        )
        Text(
            text = "Create one now",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = AccentBlue,
                fontWeight = FontWeight.SemiBold
            ),
            modifier = Modifier.clickable { onCreateAccount() }
        )
    }
}
