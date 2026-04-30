package com.capstone.ggud

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.capstone.ggud.ui.calculate.CalculateScreen
import com.capstone.ggud.ui.history.HistoryScreen
import com.capstone.ggud.ui.login.LoginScreen
import com.capstone.ggud.ui.login.StartScreen
import com.capstone.ggud.ui.main.MainScreen
import com.capstone.ggud.ui.main.OngoingPromiseScreen
import com.capstone.ggud.ui.map.KakaoMapScreen
import com.capstone.ggud.ui.my.MypageScreen
import com.capstone.ggud.ui.my.NotifySettingScreen
import com.capstone.ggud.ui.my.ProfileEditScreen
import com.capstone.ggud.ui.notification.NotificationScreen
import com.capstone.ggud.ui.promise.PromiseJoinScreen
import com.capstone.ggud.ui.promise.PromiseScreen
import com.capstone.ggud.ui.promise.WaitingRoomScreen
import com.capstone.ggud.ui.recommendation.MiddlePointScreen
import com.capstone.ggud.ui.recommendation.RecommendPlaceScreen
import com.capstone.ggud.ui.theme.GgUdTheme
import kotlin.getValue

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            GgUdTheme {
                val navController = rememberNavController()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "start",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("start") { StartScreen(navController = navController) }

                        composable("login") { LoginScreen(navController = navController) }

                        composable("home") { MainScreen(navController = navController) }
                        composable("ongoing") { OngoingPromiseScreen(navController = navController) }

                        composable("notification") { NotificationScreen(navController = navController) }

                        composable("history") { HistoryScreen(navController = navController) }

                        composable("my") { MypageScreen(navController = navController) }
                        composable("notify_setting") { NotifySettingScreen(navController = navController) }
                        composable("profile") { ProfileEditScreen(navController = navController) }

                        composable("promise") { PromiseScreen(navController = navController) }
                        composable(
                            route = "promise_join/{promiseId}",
                            arguments = listOf(navArgument("promiseId") { type = NavType.LongType })
                        ) { backStackEntry ->
                            val promiseId = backStackEntry.arguments?.getLong("promiseId") ?: 0L
                            PromiseJoinScreen(
                                navController = navController,
                                promiseId = promiseId
                            )
                        }
                        composable(
                            route = "waiting/{promiseId}",
                            arguments = listOf(navArgument("promiseId") { type = NavType.LongType })
                        ) { backStackEntry ->
                            val promiseId = backStackEntry.arguments?.getLong("promiseId") ?: 0L
                            WaitingRoomScreen(navController = navController, promiseId = promiseId)
                        }

                        composable("calculate") { CalculateScreen(navController = navController, "약속 이름") }

                        composable("map") { KakaoMapScreen() }
                        composable("middle_point/{promiseId}",
                            arguments = listOf(navArgument("promiseId") { type = NavType.LongType })
                        ) { backStackEntry ->
                            val promiseId = backStackEntry.arguments?.getLong("promiseId") ?: 0L
                            MiddlePointScreen(navController = navController, promiseId = promiseId)
                        }
                        composable(
                            route = "recommend_place/{promiseId}/{stationName}",
                            arguments = listOf(
                                navArgument("promiseId") { type = NavType.LongType },
                                navArgument("stationName") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val promiseId = backStackEntry.arguments?.getLong("promiseId") ?: 0L
                            val stationName = backStackEntry.arguments?.getString("stationName") ?: ""

                            RecommendPlaceScreen(
                                navController = navController,
                                promiseId = promiseId,
                                stationName = stationName
                            )
                        }
                    }
                }
            }
        }
    }
}