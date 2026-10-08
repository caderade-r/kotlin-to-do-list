package edu.uark.cm288.to_dolist.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainToDoListActivity: ComponentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d("MainToDoListActivity", "Notification permission granted")
        } else {
            Log.d("MainToDoListActivity", "Notification permission denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainToDoListActivity", "onCreate")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        val targetTaskId = intent.getIntExtra("target_task_id", -1)

        setContent {
            MaterialTheme() {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ToDoListAppNavigation(initialTaskId = targetTaskId)
                }
            }
        }
    }
}

@Composable
fun ToDoListAppNavigation(initialTaskId: Int = -1) {
    val navController = rememberNavController()

    LaunchedEffect(initialTaskId) {
        if (initialTaskId > 0) {
            navController.navigate("todo_item/$initialTaskId")
        }
    }

    NavHost(navController, startDestination = "todo_list") {
        composable("todo_list") {
            val viewModel: ToDoListViewModel = hiltViewModel()
            ToDoScreenRoot(
                viewModel = viewModel,
                onItemClicked = { id ->
                    navController.navigate("todo_item/$id")
                }
            )
        }
        composable(
            "todo_item/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) {
            ToDoListScreenRoot(
                viewModel = hiltViewModel(),
                onNavigateBack = {navController.popBackStack()}
            )
        }
    }
}
