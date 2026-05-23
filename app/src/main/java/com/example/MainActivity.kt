package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.ui.DugamaApp
import com.example.ui.DugamaViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Support edge-to-edge full screen drawing
        enableEdgeToEdge()
        
        // Instantiate the Dugama AndroidViewModel
        val viewModel = ViewModelProvider(this)[DugamaViewModel::class.java]
        
        setContent {
            MyApplicationTheme {
                DugamaApp(viewModel = viewModel)
            }
        }
    }
}
