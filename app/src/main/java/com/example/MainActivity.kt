package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.BusViewModel
import com.example.ui.*

class MainActivity : ComponentActivity() {
    private val viewModel: BusViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val currentScreen by viewModel.currentScreen.collectAsState()
                val currentUser by viewModel.currentUser.collectAsState()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    when (currentScreen) {
                        "welcome" -> WelcomeScreen(
                            viewModel = viewModel,
                            modifier = Modifier.padding(innerPadding)
                        )
                        "login" -> LoginScreen(
                            viewModel = viewModel,
                            modifier = Modifier.padding(innerPadding)
                        )
                        "signup" -> SignUpScreen(
                            viewModel = viewModel,
                            modifier = Modifier.padding(innerPadding)
                        )
                        "dashboard" -> {
                            when (currentUser?.role) {
                                "driver" -> DriverDashboardScreen(
                                    viewModel = viewModel,
                                    modifier = Modifier.padding(innerPadding)
                                )
                                "admin" -> AdminDashboardScreen(
                                    viewModel = viewModel,
                                    modifier = Modifier.padding(innerPadding)
                                )
                                else -> StudentDashboardScreen(
                                    viewModel = viewModel,
                                    modifier = Modifier.padding(innerPadding)
                                )
                            }
                        }
                        else -> WelcomeScreen(
                            viewModel = viewModel,
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}
