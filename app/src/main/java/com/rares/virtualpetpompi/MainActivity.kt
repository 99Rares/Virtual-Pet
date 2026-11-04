package com.rares.virtualpetpompi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.rares.virtualpetpompi.ui.AppRoot

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<PetViewModel> {
        PetViewModel.factory((application as PetApp).container)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { AppRoot((application as PetApp).container, viewModel) }
    }
}
