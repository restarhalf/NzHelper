package me.restarhalf.deer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import me.restarhalf.deer.ui.MD3.theme.NzHelperTheme
import me.restarhalf.deer.ui.MD3.screens.MainScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NzHelperTheme {
                MainScreen()
            }
        }
    }
}