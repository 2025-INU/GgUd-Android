package com.capstone.ggud

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.capstone.ggud.ui.calculate.CalculateScreen
import com.capstone.ggud.ui.history.HistoryScreen
import com.capstone.ggud.ui.login.LoginScreen
import com.capstone.ggud.ui.login.StartScreen
import com.capstone.ggud.ui.main.MainScreen
import com.capstone.ggud.ui.map.KakaoMapScreen
import com.capstone.ggud.ui.my.MypageScreen
import com.capstone.ggud.ui.my.NotifySettingScreen
import com.capstone.ggud.ui.my.ProfileEditScreen
import com.capstone.ggud.ui.notification.NotificationScreen
import com.capstone.ggud.ui.promise.PromiseJoinScreen
import com.capstone.ggud.ui.promise.PromiseScreen
import com.capstone.ggud.ui.promise.WaitingRoomScreen
import com.capstone.ggud.ui.theme.GgUdTheme
import com.kakao.vectormap.KakaoMapSdk

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

                        composable("notification") { NotificationScreen(navController = navController) }

                        composable("history") { HistoryScreen(navController = navController) }

                        composable("my") { MypageScreen(navController = navController) }
                        composable("notify_setting") { NotifySettingScreen(navController = navController) }
                        composable("profile") { ProfileEditScreen(navController = navController) }

                        composable("promise") { PromiseScreen(navController = navController) }
                        composable("promise_join") { PromiseJoinScreen(navController = navController) }
                        composable("waiting") { WaitingRoomScreen(navController = navController) }

                        composable("calculate") { CalculateScreen(navController = navController, "약속 이름") }

                        composable("map") { KakaoMapScreen() }
                    }
                }
            }
        }
    }
}