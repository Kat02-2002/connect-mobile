package com.jnarvaez.aplicacionupc3042
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.jnarvaez.aplicacionupc3042.presentation.navigation.AppNavigation
import com.jnarvaez.aplicacionupc3042.ui.theme.AplicacionUPC3042Theme
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { AplicacionUPC3042Theme { AppNavigation() } }
    }
}
