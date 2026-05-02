package com.capstone.ggud.ui.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import com.capstone.ggud.R
import com.capstone.ggud.ui.map.KakaoMapScreen
import com.capstone.ggud.ui.theme.pBlack

//진행중인 약속 화면
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OngoingPromiseScreen(navController: NavHostController) {
    val scaffoldState = rememberBottomSheetScaffoldState()

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 320.dp,
        sheetContainerColor = Color.White,
        sheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        sheetShadowElevation = 12.dp,
        sheetDragHandle = null,
        sheetContent = {
            OngoingBottomSheetContent()
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            KakaoMapScreen(modifier = Modifier.matchParentSize())

            OngoingTopBar(navController)
        }
    }
}

@Composable
private fun OngoingTopBar(navController: NavHostController) {
    Column { //상단바
        Row(modifier = Modifier
            .fillMaxWidth()
            .width(375.dp)
            .wrapContentHeight()
            .heightIn(min = 69.dp)
            .background(Color.White)
            .padding(24.dp, 16.dp)
            .zIndex(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image( //뒤로가기 버튼
                painter = painterResource(R.drawable.btn_back),
                contentDescription = "뒤로가기",
                modifier = Modifier
                    .padding(start = (7.7).dp)
                    .size(21.dp, 20.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        val popped = navController.popBackStack()
                        if (!popped) navController.navigateUp()
                    }
            )
            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = "실시간 위치",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF111827)
                )
                Text(
                    text = "회사 동료 점심 모임",
                    fontSize = 14.sp,
                    color = Color(0xFF4B5563)
                )
            }
            Spacer(modifier = Modifier.weight(1f))

            Box(
                modifier = Modifier
                    .wrapContentWidth()
                    .widthIn(min = 75.dp)
                    .height(28.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE0F2FE))
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ){
                Text("1/4 도착", color = Color(0xFF0369A1), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            }
        }
        Divider(thickness = 1.dp, color = Color(0xFFE5E7EB))
    }
}

@Composable
private fun OngoingBottomSheetContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(48.dp, 4.dp)
                .background(Color(0xFFD1D5DB), CircleShape)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "길찾기",
            fontSize = 18.sp,
            fontWeight = Bold,
            color = pBlack
        )

        Spacer(modifier = Modifier.height(16.dp))

        RouteInfoCard()
    }
}

@Composable
private fun RouteInfoCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF9FAFB))
            .padding(16.dp)
    ) {
        // 참가자 정보
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(R.drawable.ic_promise_profile),
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "이은우",
                fontSize = 16.sp,
                fontWeight = Bold,
                color = pBlack
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 경로 정보 박스
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(R.drawable.ic_path),
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "경로 정보",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF374151)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "[도보] 5분 · 300m",
                fontSize = 15.sp,
                lineHeight = 24.sp,
                color = Color(0xFF4B5563)
            )
            Text(
                text = "[지하철] 2호선 강남역 방면 · 20분",
                fontSize = 15.sp,
                lineHeight = 24.sp,
                color = Color(0xFF4B5563)
            )
            Text(
                text = "[환승]",
                fontSize = 15.sp,
                lineHeight = 24.sp,
                color = Color(0xFF4B5563)
            )
            Text(
                text = "[지하철] 신분당선 판교역 방면 · 15분",
                fontSize = 15.sp,
                lineHeight = 24.sp,
                color = Color(0xFF4B5563)
            )
            Text(
                text = "[도보] 5분 · 200m",
                fontSize = 15.sp,
                lineHeight = 24.sp,
                color = Color(0xFF4B5563)
            )
        }
    }
}