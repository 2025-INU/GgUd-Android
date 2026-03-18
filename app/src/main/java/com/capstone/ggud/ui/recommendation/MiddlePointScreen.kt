package com.capstone.ggud.ui.recommendation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.capstone.ggud.R
import com.capstone.ggud.ui.components.TopBar
import com.capstone.ggud.ui.map.KakaoMapScreen
import com.capstone.ggud.ui.theme.pBlack

data class MiddlePointCardUi(
    val title: String,
    val address: String,
    val avgMinutes: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiddlePointScreen(navController: NavHostController) {

    val peekHeight = 300.dp

    val scaffoldState = rememberBottomSheetScaffoldState()

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = peekHeight,
        sheetContainerColor = Color.White,
        sheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        sheetShadowElevation = 12.dp,
        sheetDragHandle = null,
        sheetContent = {
            BottomSheetContent(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                items = listOf(
                    MiddlePointCardUi(
                        title = "강남역 4번 출구 근처",
                        address = "서울특별시 강남구 강남대로 396",
                        avgMinutes = 12
                    ),
                    MiddlePointCardUi(
                        title = "역삼역 2번 출구 근처",
                        address = "서울특별시 강남구 테헤란로 123",
                        avgMinutes = 15
                    )
                ),
                onClickItem = { navController.navigate("recommend_place") }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            KakaoMapScreen(modifier = Modifier.matchParentSize())

            TopOverlayBar(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth(),
                navController = navController
            )

            GuideBanner(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 90.dp)
                    .width(327.dp)
                    .wrapContentHeight()
                    .heightIn(min = 106.dp)
            )
        }
    }
}

@Composable
private fun TopOverlayBar(
    modifier: Modifier = Modifier,
    navController: NavHostController
) {
    Box(modifier = modifier) {
        TopBar(navController, "중간지점 결과")
    }
}

@Composable
private fun GuideBanner(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 2.dp,
        shadowElevation = 6.dp,
        color = Color.White
    ) {
        Column(
            modifier = Modifier.padding(17.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "모이기 좋은 장소를 선택하세요",
                fontSize = 18.sp,
                fontWeight = Bold,
                color = pBlack
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "모든 참여자의 위치를 고려한 최적의 중간지점이에요",
                fontSize = 14.sp,
                color = Color(0xFF4B5563),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun BottomSheetContent(
    modifier: Modifier = Modifier,
    items: List<MiddlePointCardUi>,
    onClickItem: (MiddlePointCardUi) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(48.dp, 4.dp)
                .background(Color(0xFFD1D5DB), shape = CircleShape)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "추천 중간지점",
            fontSize = 18.sp,
            fontWeight = Bold,
            color = pBlack
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items) { item ->
                MiddlePointCard(
                    item = item,
                    onClick = { onClickItem(item) }
                )
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }
        }
    }
}

@Composable
private fun MiddlePointCard(
    item: MiddlePointCardUi,
    onClick: () -> Unit
) {

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 105.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF9FAFB)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = pBlack
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.address,
                    fontSize = 14.sp,
                    color = Color(0xFF4B5563)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.ic_time),
                        contentDescription = null,
                        tint = Color(0xFF0284C7),
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "평균 ${item.avgMinutes}분",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF0284C7)
                    )
                }
            }
            Image(
                painter = painterResource(R.drawable.btn_recommend),
                contentDescription = "추천장소로 이동",
                modifier = Modifier.size(43.dp)
            )
        }
    }
}