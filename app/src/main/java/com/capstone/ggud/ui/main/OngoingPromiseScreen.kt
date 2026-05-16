package com.capstone.ggud.ui.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.capstone.ggud.R
import com.capstone.ggud.data.TokenStore
import com.capstone.ggud.network.dto.RouteOption
import com.capstone.ggud.network.dto.RouteStep
import com.capstone.ggud.ui.map.OngoingMapScreen
import com.capstone.ggud.ui.theme.pBlack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

//진행중인 약속 화면
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OngoingPromiseScreen(
    navController: NavHostController,
    promiseId: Long,
    promiseTitle: String
) {
    val scaffoldState = rememberBottomSheetScaffoldState()

    val context = LocalContext.current
    val viewModel: OngoingPromiseViewModel = viewModel(
        factory = OngoingPromiseViewModelFactory(
            context = context,
            promiseId = promiseId
        )
    )

    val uiState = viewModel.uiState
    val selectedRouteIndex = viewModel.selectedRouteIndex
    val selectedRouteOption = uiState.routeOptions.getOrNull(selectedRouteIndex)

    LaunchedEffect(Unit) {

        val token = withContext(Dispatchers.IO) {
            TokenStore(context).getAccessToken()
        }

        if (!token.isNullOrBlank()) {
            viewModel.connectLocationSocket(token)
        }
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 360.dp,
        sheetContainerColor = Color.White,
        sheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        sheetShadowElevation = 12.dp,
        sheetDragHandle = null,
        sheetContent = {
            OngoingBottomSheetContent(
                routeOptions = uiState.routeOptions,
                selectedRouteIndex = selectedRouteIndex,
                destinationName = uiState.destinationName,
                onRouteClick = { index ->
                    viewModel.selectRoute(index)
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            OngoingMapScreen(
                modifier = Modifier.matchParentSize(),
                selectedRouteOption = selectedRouteOption,
                destinationLat = uiState.destinationLat,
                destinationLon = uiState.destinationLon,
                participantLocations = uiState.participantLocations,
                onCurrentLocationLoaded = { lat, lon ->
                    viewModel.load(
                        originLat = lat,
                        originLon = lon
                    )

                    viewModel.sendMyLocation(
                        latitude = lat,
                        longitude = lon
                    )
                }
            )

            OngoingTopBar(
                navController = navController,
                promiseTitle = promiseTitle,
                totalCount = uiState.totalCount,
                arrivedCount = uiState.arrivedCount
            )
        }
    }
}

@Composable
private fun OngoingTopBar(
    navController: NavHostController,
    promiseTitle: String,
    totalCount: Int,
    arrivedCount: Int
) {
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
                    text = promiseTitle,
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
                Text(
                    text = "${arrivedCount}/${totalCount} 도착",
                    color = Color(0xFF0369A1),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }
        }
        Divider(thickness = 1.dp, color = Color(0xFFE5E7EB))
    }
}

@Composable
private fun OngoingBottomSheetContent(
    routeOptions: List<RouteOption>,
    selectedRouteIndex: Int,
    destinationName: String,
    onRouteClick: (Int) -> Unit
) {
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

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "길찾기",
                fontSize = 18.sp,
                fontWeight = Bold,
                color = pBlack
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "도착지: ${destinationName}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = pBlack
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (routeOptions.isEmpty()) {
            EmptyRouteCard()
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.heightIn(max = 430.dp)
            ) {
                itemsIndexed(routeOptions) { index, routeOption ->
                    RouteOptionCard(
                        index = index,
                        routeOption = routeOption,
                        isSelected = selectedRouteIndex == index,
                        onClick = { onRouteClick(index) }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyRouteCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF9FAFB))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "경로 정보를 불러오는 중입니다.",
            fontSize = 15.sp,
            color = Color(0xFF6B7280)
        )
    }
}

@Composable
private fun RouteOptionCard(
    index: Int,
    routeOption: RouteOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {

    val borderColor =
        if (isSelected) Color(0xFF0EA5E9)
        else Color(0xFFE5E7EB)

    val backgroundColor =
        if (isSelected) Color(0xFFF0F9FF)
        else Color(0xFFF9FAFB)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(
                width = 1.5.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(
                interactionSource = remember {
                    MutableInteractionSource()
                },
                indication = null
            ) {
                onClick()
            }
            .padding(16.dp)
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {

                Text(
                    text = "경로 ${index + 1}",
                    fontSize = 16.sp,
                    fontWeight = Bold,
                    color = pBlack
                )

                Text(
                    text =
                        "${routeOption.totalDuration}분 · " +
                                "${formatDistance(routeOption.totalDistance)} · " +
                                "${routeOption.transferCount}회 환승",

                    fontSize = 13.sp,
                    color = Color(0xFF6B7280)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            if (isSelected) {
                Text(
                    text = "선택됨",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF0284C7)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .padding(16.dp)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

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

            routeOption.routes.forEach { routeStep ->

                RouteStepText(
                    routeStep = routeStep
                )
            }
        }
    }
}

@Composable
private fun RouteStepText(
    routeStep: RouteStep
) {

    Text(
        text = buildRouteStepText(routeStep),
        fontSize = 15.sp,
        lineHeight = 24.sp,
        color = Color(0xFF4B5563)
    )
}

private fun buildRouteStepText(
    routeStep: RouteStep
): String {

    val typeText = when (routeStep.type.name) {

        "SUBWAY" -> "지하철"
        "BUS" -> "버스"
        "WALK" -> "도보"
        "TRANSFER" -> "환승"

        else -> routeStep.type.name
    }

    return when {

        routeStep.type.name == "TRANSFER" -> {
            "[환승] ${routeStep.instruction}"
        }

        !routeStep.lineName.isNullOrBlank() -> {

            "[$typeText] " +
                    "${routeStep.lineName} · " +
                    "${routeStep.instruction} · " +
                    "${routeStep.duration}분"
        }

        else -> {

            "[$typeText] " +
                    "${routeStep.instruction} · " +
                    "${routeStep.duration}분 · " +
                    formatDistance(routeStep.distance)
        }
    }
}

private fun formatDistance(
    distanceMeter: Int
): String {

    return if (distanceMeter >= 1000) {

        String.format(
            "%.1fkm",
            distanceMeter / 1000.0
        )

    } else {

        "${distanceMeter}m"
    }
}