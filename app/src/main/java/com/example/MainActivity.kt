package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.ToneFormAppRoot
import com.example.ui.ToneFormViewModel
import com.example.ui.theme.ToneFormTheme

class MainActivity : ComponentActivity() {

    private val viewModel: ToneFormViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            ToneFormTheme(
                adaptationMode = uiState.customization.colorMode,
                studioPreset = uiState.customization.selectedPreset,
                albumBitmap = uiState.albumBitmap
            ) {
                ToneFormAppRoot(
                    uiState = uiState,
                    viewModel = viewModel
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshSystemStatus()
    }
}
