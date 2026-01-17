package com.canh0chua.downloadsorganizer

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.canh0chua.downloadsorganizer.ui.DashboardScreen
import com.canh0chua.downloadsorganizer.ui.DetailScreen
import com.canh0chua.downloadsorganizer.ui.Screen
import com.canh0chua.downloadsorganizer.ui.theme.DownloadsOrganizerTheme
import com.canh0chua.downloadsorganizer.viewmodel.FileViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: FileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DownloadsOrganizerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainApp(viewModel)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val hasPermission = hasManageExternalStoragePermission()
        viewModel.updatePermissionStatus(hasPermission)
        if (hasPermission) {
            viewModel.scanFiles()
        }
    }

    override fun onStop() {
        super.onStop()
        finish()
    }

    private fun hasManageExternalStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            true // For older versions, standard permissions are handled differently but MANAGE_EXTERNAL_STORAGE is R+
        }
    }
}

@Composable
fun MainApp(viewModel: FileViewModel) {
    val navController = rememberNavController()
    val hasPermission by viewModel.hasPermission.collectAsState()

    if (!hasPermission) {
        val context = androidx.compose.ui.platform.LocalContext.current
        PermissionRequiredScreen {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                    data = Uri.parse("package:com.canh0chua.downloadsorganizer")
                }
                context.startActivity(intent)
            }
        }
    } else {
        NavHost(navController = navController, startDestination = Screen.Dashboard.route) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    viewModel = viewModel,
                    onCategoryClick = { type ->
                        navController.navigate(Screen.Detail.createRoute(type.name))
                    }
                )
            }
            composable(Screen.Detail.route) { backStackEntry ->
                val fileType = backStackEntry.arguments?.getString("fileType") ?: ""
                DetailScreen(
                    viewModel = viewModel,
                    fileTypeName = fileType,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
fun PermissionRequiredScreen(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Permission Required",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "This app needs access to all files in your Downloads folder to organize them.",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRequestPermission) {
            Text("Grant Permission")
        }
    }
}
