package com.newstudio.ujkhataapp

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.newstudio.ujkhataapp.ui.screen.CustomerScreen
import com.newstudio.ujkhataapp.ui.theme.UJKhataAppTheme
import com.newstudio.ujkhataapp.viewModel.KhataViewModel


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val khataViewModel = KhataViewModel()
        setContent {
            UJKhataAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) {  innerPadding ->
                    val navController = rememberNavController()
                    Navigation(navController, khataViewModel)
                }
            }
        }
    }
}

