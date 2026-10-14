package com.jnarvaez.aplicacionupc3042.presentation.common

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController

@Composable
fun AppScaffold(navHostController: NavHostController, content: @Composable (PaddingValues) -> Unit){
    Scaffold(
        topBar = { AppTopBar(navHostController) },
        bottomBar = { AppBottomBar(navHostController) }
    ) {paddingValues ->
        content(paddingValues)
    }
}