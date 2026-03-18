package com.capstone.ggud.ui.recommendation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import com.capstone.ggud.R
import com.capstone.ggud.ui.map.KakaoMapScreen
import com.capstone.ggud.ui.theme.pBlack

data class RecommendedPlaceUiModel(
    val id: Int,
    val name: String,
    val description: String,
    val rating: Double,
    val walkingMinutes: Int,
    val priceText: String,
    val category: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendPlaceScreen(navController: NavHostController) {

    val peekHeight = 250.dp
    val scaffoldState = rememberBottomSheetScaffoldState()

    var selectedPlaceIds by remember { mutableStateOf(setOf<Int>()) }
    val selectedCount = selectedPlaceIds.size

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetPeekHeight = peekHeight,
            sheetContainerColor = Color.White,
            sheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            sheetShadowElevation = 12.dp,
            sheetDragHandle = null,
            sheetContent = {
                BottomSheetContent(
                    modifier = Modifier.fillMaxWidth(),
                    selectedPlaceIds = selectedPlaceIds,
                    onTogglePlace = { placeId ->
                        selectedPlaceIds =
                            if (placeId in selectedPlaceIds) {
                                selectedPlaceIds - placeId
                            } else {
                                selectedPlaceIds + placeId
                            }
                    }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                KakaoMapScreen(modifier = Modifier.matchParentSize())

                RecommendTopBar(navController)
            }
        }

        if (selectedCount > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(93.dp)
                    .offset(y = 25.dp)
                    .background(Color.White)
            ) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                        .fillMaxWidth()
                        .height(60.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF0EA5E9),
                                    Color(0xFF2563EB)
                                )
                            )
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            navController.navigate("home")
                        }
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = "${selectedCount}개 장소로 약속 확정하기",
                            fontSize = 14.sp,
                            fontWeight = Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecommendTopBar(navController: NavHostController) {
    val types = listOf(
        "전체" to R.drawable.ic_all,
        "식당" to R.drawable.ic_restaurant,
        "카페" to R.drawable.ic_cafe,
        "술집" to R.drawable.ic_bar
    )

    var selectedType by remember { mutableStateOf("전체") }
    var showDialog by remember { mutableStateOf(false) }

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
                    text = "장소 추천",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF111827)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "강남역 4번 출구 근처",
                        fontSize = 14.sp,
                        color = Color(0xFF4B5563)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "변경",
                        fontSize = 13.sp,
                        color = Color(0xFF0284C7)
                    )
                }
            }
            Spacer(modifier = Modifier.weight(1f))

            Image(
                painter = painterResource(R.drawable.btn_ai),
                contentDescription = "AI 버튼",
                modifier = Modifier
                    .size(43.dp)
                    .clip(CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { showDialog = true }
            )
            AiRecommendDialog(
                showDialog = showDialog,
                onDismiss = { showDialog = false }
            )
        }
        Divider(thickness = 1.dp, color = Color(0xFFE5E7EB))

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .width(375.dp)
                .wrapContentHeight()
                .heightIn(min = 69.dp)
                .background(Color.White)
                .padding(24.dp, 16.dp)
                .zIndex(1f),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(types) { (text, icon) ->
                PlaceTypeButton(
                    icon = painterResource(icon),
                    type = text,
                    selected = selectedType == text,
                    onClick = {
                        selectedType = text
                    }
                )
            }
        }
    }
}

@Composable
private fun PlaceTypeButton(
    icon: Painter,
    type: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .wrapContentWidth()
            .widthIn(min = 80.dp)
            .height(37.dp)
            .clip(shape = RoundedCornerShape(999.dp))
            .background(if (selected) Color(0xFF0EA5E9) else Color(0xFFF3F4F6))
            .padding(16.dp, 8.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = icon,
            contentDescription =  null,
            modifier = Modifier.size(14.dp, 15.dp),
            tint = if (selected) Color.White else Color(0xFF4B5563)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = type,
            fontSize = 14.sp,
            lineHeight = 14.sp,
            fontWeight = FontWeight.Medium,
            color = if (selected) Color.White else Color(0xFF4B5563),
            modifier = Modifier.offset(y=-(0.9).dp)
        )
    }
}

@Composable
private fun BottomSheetContent(
    modifier: Modifier = Modifier,
    selectedPlaceIds: Set<Int>,
    onTogglePlace: (Int) -> Unit
) {
    val dummyPlaces = remember {
        listOf(
            RecommendedPlaceUiModel(
                id = 1,
                name = "모던 이탈리안 키친",
                description = "정통 이탈리안 요리와 모던한 분위기",
                rating = 4.8,
                walkingMinutes = 3,
                priceText = "2-3만원",
                category = "식당"
            ),
            RecommendedPlaceUiModel(
                id = 2,
                name = "블루보틀 커피",
                description = "스페셜티 커피와 편안한 공간",
                rating = 4.6,
                walkingMinutes = 2,
                priceText = "5천-1만원",
                category = "카페"
            ),
            RecommendedPlaceUiModel(
                id = 3,
                name = "루프탑 바 스카이",
                description = "도시 전망과 함께하는 칵테일",
                rating = 4.7,
                walkingMinutes = 4,
                priceText = "1-2만원",
                category = "술집"
            )
        )
    }

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
            text = "추천 장소",
            fontSize = 18.sp,
            fontWeight = Bold,
            color = pBlack
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = dummyPlaces,
                key = { it.id }
            ) { place ->
                RecommendPlaceCard(
                    place = place,
                    isSelected = place.id in selectedPlaceIds,
                    onCardClick = {
                        onTogglePlace(place.id)
                    },
                    onPinClick = {
                        //TODO
                    }
                )
            }

            if (selectedPlaceIds.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(50.dp))
                }
            }
        }
    }
}

@Composable
private fun RecommendPlaceCard(
    place: RecommendedPlaceUiModel,
    isSelected: Boolean,
    onCardClick: () -> Unit,
    onPinClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        Color(0xFFF0F9FF)
    } else {
        Color(0xFFF9FAFB)
    }

    val borderColor = if (isSelected) {
        Color(0xFF0EA5E9)
    } else {
        Color(0xFFE5E7EB)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .heightIn(min = 90.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .padding(13.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onCardClick
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
        ) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color(0xFF0EA5E9).copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(24.dp)
                        .background(Color(0xFF0EA5E9), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(verticalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = place.name,
                fontSize = 14.sp,
                fontWeight = Bold,
                color = pBlack,
                maxLines = 1
            )

            Text(
                text = place.description,
                fontSize = 12.sp,
                color = Color(0xFF4B5563),
                maxLines = 1
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = Color(0xFFFACC15),
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${place.rating}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = pBlack
                )
                Spacer(modifier = Modifier.width(11.3.dp))

                Icon(
                    imageVector = Icons.Filled.DirectionsWalk,
                    contentDescription = null,
                    tint = Color(0xFF0284C7),
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "도보 ${place.walkingMinutes}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF0284C7)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Column(
            modifier = Modifier.height(64.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(Color(0xFF22C55E), RoundedCornerShape(4.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onPinClick
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Place,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
            }

            Text(
                text = place.priceText,
                fontSize = 12.sp,
                color = Color(0xFF4B5563)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AiRecommendDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit
) {
    if (!showDialog) return

    var text by remember { mutableStateOf("") }

    val exampleKeywoeds = listOf(
        "로맨틱한 분위기",
        "조용한 카페",
        "활기찬 펍",
        "고급 레스토랑"
    )

    Dialog(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .width(327.dp)
                .wrapContentHeight()
                .heightIn(min = 437.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(R.drawable.btn_ai),
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "AI 장소 추천",
                    fontWeight = Bold,
                    fontSize = 18.sp,
                    color = pBlack
                )
                Spacer(modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF3F4F6))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ){ onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        tint = Color(0xFF4B5563),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "원하는 분위기나 장소 스타일을 자세히 설명해주세요. AI가 맞춤형 장소를 추천해드립니다.",
                fontSize = 14.sp,
                color = Color(0xFF4B5563)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(128.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(12.dp))
                    .padding(17.dp, 13.dp)
            ) {
                BasicTextField(
                    value = text,
                    onValueChange = { if (it.length <= 200) text = it },
                    modifier = Modifier.fillMaxSize(),
                    textStyle = TextStyle(
                        fontSize = 14.sp,
                        color = pBlack,
                        lineHeight = 20.sp
                    ),
                    singleLine = false,
                    maxLines = Int.MAX_VALUE,
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.TopStart
                        ) {
                            if (text.isEmpty()) {
                                Text(
                                    text = "예: 로맨틱하고 조용한 분위기의 카페나 레스토랑을 찾고 있어요.",
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp,
                                    color = Color(0xFF6B7280)
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 64.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    exampleKeywoeds.forEach { keyword ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(Color(0xFFECFDF5))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    text = keyword
                                }
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = keyword,
                                fontSize = 12.sp,
                                color = Color(0xFF059669)
                            )
                        }
                    }
                }

                Text(
                    text = "${text.length}/200",
                    fontSize = 12.sp,
                    color = Color(0xFF9CA3AF),
                    modifier = Modifier.align(Alignment.TopEnd)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(134.5.dp, 47.dp)
                        .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(12.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onDismiss()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "취소",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF374151)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(134.5.dp, 47.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (text.isBlank()) {
                                Color(0xFF10B981).copy(alpha = 0.5f)
                            } else {
                                Color(0xFF10B981)
                            }
                        )
                        .clickable(
                            enabled = text.isNotBlank(),
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onDismiss()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "AI 추천 받기",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}