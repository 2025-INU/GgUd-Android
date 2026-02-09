package com.capstone.ggud.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.capstone.ggud.R
import com.capstone.ggud.data.AuthRepository
import com.capstone.ggud.data.TokenStore
import com.capstone.ggud.network.ApiClient

@androidx.compose.runtime.Composable
fun StartScreen(navController: NavHostController) {
    val context = LocalContext.current

    val tokenStore = remember { TokenStore(context.applicationContext) }
    val authRepo = remember { AuthRepository(ApiClient.getAuthApi(context), tokenStore) }

    val vm: StartViewModel = viewModel(
        factory = StartViewModelFactory(authRepo)
    )

    val uiState by vm.uiState.collectAsState()

    LaunchedEffect(Unit) { vm.decideNext() } //화면 진입 시 1회 실행 (다음 화면 결정)

    //next가 바뀌면 해당 화면으로 네비게이션
    LaunchedEffect(uiState.next) {
        when (val next = uiState.next) {
            StartRoute.ToHome -> {
                navController.navigate("home") {
                    popUpTo("start") { inclusive = true }
                    launchSingleTop = true
                }
            }
            StartRoute.ToLogin -> {
                navController.navigate("login") {
                    popUpTo("start") { inclusive = true }
                    launchSingleTop = true
                }
            }
            is StartRoute.Error -> {
                navController.navigate("login") {
                    popUpTo("start") { inclusive = true }
                    launchSingleTop = true
                }
            }
            else -> Unit
        }
    }

    Column( //로딩 중에 보여줄 로고 + 로딩
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.drawable.login_mark),
            contentDescription = null,
            modifier = Modifier.size(100.dp)
        )

        CircularProgressIndicator(modifier = Modifier.size(32.dp))
    }
}