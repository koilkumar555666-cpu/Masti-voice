package com.example.mastivoice

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.outlined.Celebration
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mastivoice.ui.screens.LibraryScreen
import com.example.mastivoice.ui.screens.SoundboardScreen
import com.example.mastivoice.ui.screens.VoiceStudioScreen
import com.example.mastivoice.ui.theme.*
import com.example.mastivoice.viewmodel.MastiVoiceViewModel

enum class AppTab(val title: String) {
    STUDIO("Voice Studio"),
    SOUNDBOARD("Soundboard"),
    LIBRARY("Library")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MastiVoiceTheme {
                val viewModel: MastiVoiceViewModel = viewModel()
                var currentTab by remember { mutableStateOf(AppTab.STUDIO) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = BackgroundDark,
                    topBar = {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = SurfaceDark,
                            tonalElevation = 4.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .statusBarsPadding()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_masti_logo),
                                    contentDescription = "Masti Voice Logo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = stringResource(id = R.string.app_name),
                                        style = MaterialTheme.typography.titleLarge,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.5.sp
                                    )
                                    Text(
                                        text = stringResource(id = R.string.tagline),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MastiPurpleLight,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = SurfaceDark,
                            tonalElevation = 8.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .navigationBarsPadding()
                        ) {
                            NavigationBarItem(
                                selected = currentTab == AppTab.STUDIO,
                                onClick = { currentTab = AppTab.STUDIO },
                                icon = {
                                    Icon(
                                        imageVector = if (currentTab == AppTab.STUDIO) Icons.Filled.Mic else Icons.Outlined.Mic,
                                        contentDescription = "Voice Studio"
                                    )
                                },
                                label = { Text("Studio", fontWeight = FontWeight.Bold) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = TextPrimary,
                                    selectedTextColor = MastiCyan,
                                    indicatorColor = MastiPurple,
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary
                                ),
                                modifier = Modifier.testTag("nav_tab_studio")
                            )

                            NavigationBarItem(
                                selected = currentTab == AppTab.SOUNDBOARD,
                                onClick = { currentTab = AppTab.SOUNDBOARD },
                                icon = {
                                    Icon(
                                        imageVector = if (currentTab == AppTab.SOUNDBOARD) Icons.Filled.Celebration else Icons.Outlined.Celebration,
                                        contentDescription = "Soundboard"
                                    )
                                },
                                label = { Text("Soundboard", fontWeight = FontWeight.Bold) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = TextPrimary,
                                    selectedTextColor = MastiYellow,
                                    indicatorColor = MastiPurple,
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary
                                ),
                                modifier = Modifier.testTag("nav_tab_soundboard")
                            )

                            NavigationBarItem(
                                selected = currentTab == AppTab.LIBRARY,
                                onClick = { currentTab = AppTab.LIBRARY },
                                icon = {
                                    Icon(
                                        imageVector = if (currentTab == AppTab.LIBRARY) Icons.Filled.Folder else Icons.Outlined.Folder,
                                        contentDescription = "Library"
                                    )
                                },
                                label = { Text("Library", fontWeight = FontWeight.Bold) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = TextPrimary,
                                    selectedTextColor = MastiPink,
                                    indicatorColor = MastiPurple,
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary
                                ),
                                modifier = Modifier.testTag("nav_tab_library")
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentTab) {
                            AppTab.STUDIO -> VoiceStudioScreen(
                                viewModel = viewModel,
                                onNavigateToLibrary = { currentTab = AppTab.LIBRARY }
                            )
                            AppTab.SOUNDBOARD -> SoundboardScreen(
                                viewModel = viewModel
                            )
                            AppTab.LIBRARY -> LibraryScreen(
                                viewModel = viewModel,
                                onNavigateToStudio = { currentTab = AppTab.STUDIO }
                            )
                        }
                    }
                }
            }
        }
    }
}
