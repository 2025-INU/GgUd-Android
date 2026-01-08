package com.capstone.ggud.ui.promise

import androidx.annotation.DrawableRes
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.capstone.ggud.R
import com.capstone.ggud.ui.components.TopBar
import com.capstone.ggud.ui.theme.pBlack

data class TransportItem(
    val id: Int,
    val name: String,
    @DrawableRes val iconRes: Int
)

val transportItems = listOf(
    TransportItem(1, "도보", R.drawable.ic_walk),
    TransportItem(2, "버스", R.drawable.ic_bus),
    TransportItem(3, "지하철", R.drawable.ic_subway),
    TransportItem(4, "자동차", R.drawable.ic_car),
    TransportItem(5, "택시", R.drawable.ic_taxi),
    TransportItem(6, "자전거", R.drawable.ic_bike),
)

@Composable
fun PromiseJoinScreen() {

    //위치 입력값
    var location by remember { mutableStateOf("") }
    var showLocation by remember { mutableStateOf(false) }

    //교통수단 선택 상태
    var selectedTransportId by remember { mutableIntStateOf(-1) } //선택 없음 = -1

    //버튼 활성 조건
    val isJoinEnabled = showLocation && selectedTransportId != -1

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        TopBar("약속 참여하기")

        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(11.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(327f / 185f)
                    .paint(
                        painter = painterResource(R.drawable.bg_promise_waiting),
                        contentScale = ContentScale.FillBounds
                    )
                    .padding(24.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(R.drawable.ic_promise),
                        contentDescription = null,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = "약속 이름",
                            fontWeight = Bold,
                            fontSize = 18.sp,
                            color = pBlack
                        )
                        Spacer(modifier = Modifier.height(5.dp))
                        Text(
                            text = "2026-01-01 • 00:00", //•
                            fontSize = 14.sp,
                            color = Color(0xFF4B5563)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "주최자: 이은우",
                            fontSize = 12.sp,
                            color = Color(0xFF6B7280)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_important),
                        contentDescription = null,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "출발 위치와 교통수단을 선택해주세요",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = Color(0xFF0284C7)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "출발 위치",
                fontWeight = Bold,
                fontSize = 18.sp,
                color = pBlack
            )
            Spacer(modifier = Modifier.height(16.dp))

            Image(
                painter = painterResource(R.drawable.btn_location),
                contentDescription = "위치 수집 버튼",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(327f / 62f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        showLocation = true
                    }
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(327f / 47f)
                    .border(
                        width = 1.dp,
                        color = Color(0xFFE5E7EB),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 17.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_search),
                    contentDescription = null,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(18.dp))
                BasicTextField(
                    value = location,
                    onValueChange = { if (it.length <= 50) location = it }, //50자 제한
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 14.sp,
                        color = Color(0xFF111827)
                    ),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done, //완료버튼 보이게
                        keyboardType = KeyboardType.Text //일반 문자 입력용 키보드
                    ),
                    decorationBox = { inner ->
                        if(location.isBlank()) {
                            Text(
                                text = "주소를 검색하세요",
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                                color = Color(0xFF6B7280)
                            )
                        }
                        inner()
                    }
                )
            }

            if (showLocation) {
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(327f / 54f)
                        .background(Color(0xFFF0FDF4))
                        .border(1.dp, Color(0xFFBBF7D0), RoundedCornerShape(12.dp))
                        .padding(17.dp)
                ) {
                    Text(
                        text = "경기도 부천시 소사동",
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = Color(0xFF166534)
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "교통수단",
                fontWeight = Bold,
                fontSize = 18.sp,
                color = pBlack
            )
            Spacer(modifier = Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(3), //한 행에 3개 버튼
                modifier = Modifier
                    .fillMaxWidth()
                    .height(236.dp),
                userScrollEnabled = false,
                horizontalArrangement = Arrangement.spacedBy(12.dp), //가로간격
                verticalArrangement = Arrangement.spacedBy(12.dp) //세로간격
            ) {
                items(
                    items = transportItems,
                    key = { it.id }
                ) { item ->
                    TransportCard(
                        item = item,
                        selected = (item.id == selectedTransportId),
                        onClick = {
                            selectedTransportId = if (selectedTransportId == item.id) -1 else item.id
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(64.dp))

            Image(
                painter = painterResource(if (isJoinEnabled) R.drawable.btn_join else R.drawable.btn_join_disabled),
                contentDescription = "약속 참여",
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .scale(1.08f)
                    .clickable(
                        enabled = isJoinEnabled,
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        // TODO: 약속 참여
                    }
            )
            Text(
                text = "참여하면 약속 대기방으로 이동합니다",
                fontSize = 14.sp,
                color = Color(0xFF6B7280),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .offset(y=-(8).dp)
            )
        }
    }
}

@Composable
private fun TransportCard(
    item: TransportItem,
    selected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (selected) Color(0xFF0EA5E9) else Color(0xFFE5E7EB)
    val bgColor = if (selected) Color(0xFFF0F9FF) else Color.White

    Column(
        modifier = Modifier
            .size(101.dp, 112.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(item.iconRes),
            contentDescription = "교통수단 아이콘",
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = item.name,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = pBlack
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PromiseJoinScreenPreview() {
    PromiseJoinScreen()
}