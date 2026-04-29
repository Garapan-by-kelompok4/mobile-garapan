package com.app.garapan.presentation.screen.profile

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.app.garapan.presentation.navigation.Routes
import com.app.garapan.ui.theme.BorderColor
import com.app.garapan.ui.theme.BrandNavy
import com.app.garapan.ui.theme.MutedText
import com.app.garapan.ui.theme.PrimaryText
import com.app.garapan.ui.theme.SecondaryText
import com.app.garapan.ui.theme.White

private data class ProfileMenuItem(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit = {}
)

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        bottomBar = {
            ProfileBottomNav(navController = navController)
        },
        containerColor = White
    ) { innerPadding ->
        val initials = uiState.name
            .split(" ")
            .take(2)
            .joinToString("") { it.first().uppercase() }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp)
        ) {
            ProfileTopBar(onBack = { navController.navigateUp() })
            Spacer(modifier = Modifier.height(18.dp))

            ProfileHeaderCard(
                name = uiState.name,
                email = uiState.email,
                initials = initials,
                onClick = { navController.navigate(Routes.EDIT_PROFILE) }
            )

            Spacer(modifier = Modifier.height(26.dp))
            ProfileSectionTitle("Preferensi")
            Spacer(modifier = Modifier.height(12.dp))
            ProfileMenuGroup(
                items = listOf(
                    ProfileMenuItem("Keamanan Akun", Icons.Filled.Security) {
                        navController.navigate(Routes.SECURITY)
                    },
                    ProfileMenuItem("Portofolio", Icons.Filled.Work),
                    ProfileMenuItem("Keahlian & Layanan", Icons.Filled.Computer) {
                        navController.navigate(Routes.PROFILE_SERVICES)
                    }
                )
            )

            Spacer(modifier = Modifier.height(14.dp))
            ProfileSectionTitle("Lainnya")
            Spacer(modifier = Modifier.height(12.dp))
            ProfileMenuGroup(
                items = listOf(
                    ProfileMenuItem("Riwayat Pesanan", Icons.Filled.ShoppingCart) {
                        navController.navigate(Routes.ORDER_HISTORY)
                    },
                    ProfileMenuItem("Pusat Bantuan", Icons.AutoMirrored.Filled.Help),
                    ProfileMenuItem("Syarat & Ketentuan", Icons.AutoMirrored.Filled.Article),
                    ProfileMenuItem("Kebijakan Privasi", Icons.Filled.PrivacyTip)
                )
            )

            Spacer(modifier = Modifier.height(14.dp))
            LogoutCard()
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ProfileTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = PrimaryText,
                modifier = Modifier.size(30.dp)
            )
        }
        Text(
            text = "Profile",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                color = PrimaryText
            ),
            modifier = Modifier.padding(start = 2.dp)
        )
    }
}

@Composable
private fun ProfileHeaderCard(
    name: String,
    email: String,
    initials: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, Color(0xFFB7B7B7), RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 22.dp, vertical = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ProfileAvatar(initials = initials)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = PrimaryText
                )
            )
            Text(
                text = email,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = PrimaryText
                )
            )
        }
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = "Open profile detail",
            tint = PrimaryText,
            modifier = Modifier.size(34.dp)
        )
    }
}

@Composable
private fun ProfileAvatar(initials: String) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(BrandNavy.copy(alpha = 0.12f))
            .border(2.dp, BrandNavy, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                color = BrandNavy
            )
        )
    }
}

@Composable
private fun ProfileSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.ExtraBold,
            color = PrimaryText
        )
    )
}

@Composable
private fun ProfileMenuGroup(
    items: List<ProfileMenuItem>,
    muted: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, Color(0xFFB7B7B7), RoundedCornerShape(10.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        items.forEachIndexed { index, item ->
            ProfileMenuRow(
                item = item,
                muted = muted
            )
            if (index != items.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.padding(start = 36.dp, end = 40.dp),
                    thickness = 1.dp,
                    color = PrimaryText.copy(alpha = 0.72f)
                )
            }
        }
    }
}

@Composable
private fun ProfileMenuRow(
    item: ProfileMenuItem,
    muted: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = item.onClick)
            .height(52.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = null,
            tint = PrimaryText,
            modifier = Modifier.size(25.dp)
        )
        Text(
            text = item.label,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                color = if (muted) SecondaryText else PrimaryText
            ),
            modifier = Modifier.padding(start = 18.dp)
        )
    }
}

@Composable
private fun LogoutCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, Color(0xFFB7B7B7), RoundedCornerShape(10.dp))
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Logout,
            contentDescription = null,
            tint = SecondaryText,
            modifier = Modifier.size(25.dp)
        )
        Text(
            text = "Keluar (Log Out)",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                color = SecondaryText
            ),
            modifier = Modifier.padding(start = 18.dp)
        )
    }
}

private data class NavItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@Composable
private fun ProfileBottomNav(navController: NavController) {
    val navItems = listOf(
        NavItem("Home", Icons.Filled.Home, Icons.Outlined.Home),
        NavItem("Search", Icons.Filled.Search, Icons.Outlined.Search),
        NavItem("New", Icons.Default.Add, Icons.Default.Add),
        NavItem("Pesan", Icons.Outlined.ChatBubbleOutline, Icons.Outlined.ChatBubbleOutline),
        NavItem("Profile", Icons.Filled.Person, Icons.Outlined.Person),
    )
    val selectedIndex = 4

    NavigationBar(
        containerColor = White,
        tonalElevation = 0.dp,
        modifier = Modifier.border(width = 1.dp, color = BorderColor, shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp))
    ) {
        navItems.forEachIndexed { index, item ->
            if (index == 2) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .clickable { navController.navigate(Routes.POST_PROJECT) }
                            .background(BrandNavy),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = item.label,
                            tint = White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            } else {
                NavigationBarItem(
                    selected = selectedIndex == index,
                    onClick = {
                        when (index) {
                            0 -> navController.navigate(Routes.HOME) {
                                popUpTo(Routes.HOME) { inclusive = true }
                            }
                            1 -> navController.navigate(Routes.SEARCH)
                            3 -> navController.navigate(Routes.PESAN)
                            else -> {}
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = if (selectedIndex == index) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.label,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BrandNavy,
                        selectedTextColor = BrandNavy,
                        unselectedIconColor = MutedText,
                        unselectedTextColor = MutedText,
                        indicatorColor = BrandNavy.copy(alpha = 0.1f)
                    )
                )
            }
        }
    }
}
