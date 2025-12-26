package me.restarhalf.deer.ui.md3.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import me.restarhalf.deer.data.ThemeRepository
import me.restarhalf.deer.data.UiStyle

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val prefs by ThemeRepository.themePreferences.collectAsState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

     val md3Selected = prefs.uiStyle == UiStyle.MD3
     val miuixSelected = prefs.uiStyle == UiStyle.MIUIX

     val md3ContainerColor by animateColorAsState(
         targetValue = if (md3Selected) {
             MaterialTheme.colorScheme.primaryContainer
         } else {
             MaterialTheme.colorScheme.surfaceVariant
         }
     )
     val miuixContainerColor by animateColorAsState(
         targetValue = if (miuixSelected) {
             MaterialTheme.colorScheme.primaryContainer
         } else {
             MaterialTheme.colorScheme.surfaceVariant
         }
     )

     val md3DotColor by animateColorAsState(
         targetValue = if (md3Selected) MaterialTheme.colorScheme.primary else Color.Transparent
     )
     val miuixDotColor by animateColorAsState(
         targetValue = if (miuixSelected) MaterialTheme.colorScheme.primary else Color.Transparent
     )

    Scaffold(
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text("设置") },
                scrollBehavior = scrollBehavior
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
        ) {
            Card(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = md3ContainerColor
                ),
                onClick = { ThemeRepository.setUiStyle(context, UiStyle.MD3) }
            ) {
                ListItem(
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Outlined.Palette,
                            contentDescription = null,
                        )
                    },
                    headlineContent = {
                        Text(text = "Material Design 3", style = MaterialTheme.typography.titleMedium)
                    },
                    supportingContent = {
                        Text(text = "Material Design 3 界面")
                    },
                    trailingContent = {
                        Spacer(
                            modifier = Modifier
                                .size(10.dp)
                                .background(md3DotColor, CircleShape)
                        )
                    }
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = miuixContainerColor
                ),
                onClick = { ThemeRepository.setUiStyle(context, UiStyle.MIUIX) }
            ) {
                ListItem(
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Outlined.Palette,
                            contentDescription = null,
                        )
                    },
                    headlineContent = {
                        Text(text = "MIUIX", style = MaterialTheme.typography.titleMedium)
                    },
                    supportingContent = {
                        Text(text = "miuix 风格界面")
                    },
                    trailingContent = {
                        Spacer(
                            modifier = Modifier
                                .size(10.dp)
                                .background(miuixDotColor, CircleShape)
                        )
                    }
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            ListItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                headlineContent = {
                    Text(text = "深色模式", style = MaterialTheme.typography.titleMedium)
                },
                supportingContent = {
                    Text(text = "仅对 Material Design 3 界面生效")
                },
                trailingContent = {
                    Switch(
                        checked = prefs.md3DarkTheme,
                        onCheckedChange = { ThemeRepository.setMd3DarkTheme(context, it) }
                    )
                }
            )
            ListItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        navController.navigate("about")
                    },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                    )
                },
                headlineContent = {
                    Text(text = "关于", style = MaterialTheme.typography.titleMedium)
                }
            )
            Spacer(modifier = Modifier.weight(1f))
        }

    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    SettingsScreen(
        navController = rememberNavController()
    )
}
