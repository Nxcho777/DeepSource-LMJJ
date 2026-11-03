package cl.duoc.deepsource.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import cl.duoc.deepsource.navigation.AppDestination
import cl.duoc.deepsource.navigation.DeepSourceNavHost
import cl.duoc.deepsource.navigation.navigateTab
import cl.duoc.deepsource.ui.viewmodel.WorkspaceViewModel

data class WidgetNavigationRequest(val sequence: Long, val projectId: String?)

@Composable
fun DeepSourceApp(startDestination: String = "login", widgetRequest: WidgetNavigationRequest? = null) {
    val context = LocalContext.current.applicationContext
    val navController = rememberNavController()
    val workspaceViewModel: WorkspaceViewModel = viewModel(factory = WorkspaceViewModel.factory(context))
    DeepSourceNavHost(navController = navController, startDestination = startDestination, workspaceViewModel = workspaceViewModel)
    LaunchedEffect(widgetRequest?.sequence) {
        widgetRequest?.let { request ->
            request.projectId?.let(workspaceViewModel::selectProject)
            if (navController.currentDestination?.route != AppDestination.RegisterTime.route) {
                navController.navigateTab(AppDestination.RegisterTime.route)
            }
        }
    }
}
