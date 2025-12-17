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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.capstone.ggud.R

@Composable
fun MainScreen() {
    var promise by remember { mutableStateOf(true) }

    val bottomBarHeight = 86.dp
    val fabGap = 15.dp

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 24.dp, horizontal = 16.dp)
                .padding(bottom = bottomBarHeight + fabGap + 110.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            //상단바
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "GgUd",
                    fontWeight = Bold,
                    fontSize = 24.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                Image(
                    painter = painterResource(R.drawable.btn_notify),
                    contentDescription = "알림페이지",
                    modifier = Modifier.size(24.dp, 23.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Image( //창 변환 버튼
                painter = painterResource(if (promise) R.drawable.main_promise_bar else R.drawable.main_promise_bar_upcoming),
                contentDescription = if (promise) "진행중인 약속" else "예정된 약속",
                modifier = Modifier.clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { promise = !promise }
            )

            Spacer(modifier = Modifier.height(24.dp))

            //약속 목록 (스크롤X)
            if (promise) {
                Column(
                    modifier = Modifier
                        .size(327.dp, 226.dp)
                        .border(
                            width = 1.dp,
                            color = Color(0xFFE5E7EB),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(25.dp)
                ) {
                    Row {
                        Text(text = "약속 이름", fontWeight = Bold, fontSize = 18.sp)
                        Spacer(modifier = Modifier.weight(1f))
                        Image(
                            painter = painterResource(R.drawable.ic_promise_in_progress),
                            contentDescription = null
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row {
                        Icon(painter = painterResource(R.drawable.ic_day), contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "2025-12-17", fontSize = 14.sp, color = Color(0xFF4B5563))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row {
                        Icon(painter = painterResource(R.drawable.ic_time), contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "00:00", fontSize = 14.sp, color = Color(0xFF4B5563))
                    }
                    Spacer(modifier = Modifier.height(28.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(painter = painterResource(R.drawable.ic_people), contentDescription = null)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "4명",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF4B5563)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(R.drawable.ic_spot),
                            contentDescription = null,
                            tint = Color(0xFF4B5563)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "약속 장소",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF4B5563)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .size(327.dp, 226.dp)
                        .border(
                            width = 1.dp,
                            color = Color(0xFFE5E7EB),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(25.dp)
                ) {
                    Row {
                        Text(text = "약속 이름", fontWeight = Bold, fontSize = 18.sp)
                        Spacer(modifier = Modifier.weight(1f))
                        Image(
                            painter = painterResource(R.drawable.ic_promise_in_progress),
                            contentDescription = null
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row {
                        Icon(painter = painterResource(R.drawable.ic_day), contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "2025-12-17", fontSize = 14.sp, color = Color(0xFF4B5563))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row {
                        Icon(painter = painterResource(R.drawable.ic_time), contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "00:00", fontSize = 14.sp, color = Color(0xFF4B5563))
                    }
                    Spacer(modifier = Modifier.height(28.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(painter = painterResource(R.drawable.ic_people), contentDescription = null)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "4명",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF4B5563)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(R.drawable.ic_spot),
                            contentDescription = null,
                            tint = Color(0xFF4B5563)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "약속 장소",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF4B5563)
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .size(327.dp, 305.dp)
                        .border(
                            width = 1.dp,
                            color = Color(0xFFE5E7EB),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(25.dp)
                ) {
                    Row {
                        Text(text = "약속 이름", fontWeight = Bold, fontSize = 18.sp)
                        Spacer(modifier = Modifier.weight(1f))
                        Image(
                            painter = painterResource(R.drawable.ic_promise_confirmed),
                            contentDescription = null
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row {
                        Icon(painter = painterResource(R.drawable.ic_day), contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "2025-12-17", fontSize = 14.sp, color = Color(0xFF4B5563))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row {
                        Icon(painter = painterResource(R.drawable.ic_time), contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "00:00", fontSize = 14.sp, color = Color(0xFF4B5563))
                    }
                    Spacer(modifier = Modifier.height(28.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(painter = painterResource(R.drawable.ic_people), contentDescription = null)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "4명",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF4B5563)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(R.drawable.ic_spot),
                            contentDescription = null,
                            tint = Color(0xFF4B5563)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "약속 장소",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF4B5563)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(
                        modifier = Modifier
                            .size(277.dp, 72.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF3F4F6))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "확정된 장소",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF374151)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF3B82F6))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "식당, 카페, ...",
                                fontSize = 14.sp,
                                color = Color(0xFF4B5563)
                            )
                        }
                    }
                }
            }
        }

        Image(
            painter = painterResource(R.drawable.btn_create_promise),
            contentDescription = "약속 생성",
            modifier = Modifier
                .zIndex(2f)
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 16.dp, bottom = bottomBarHeight + fabGap)
        )

        Box( //하단바 (네비게이션X)
            modifier = Modifier
                .zIndex(1f)
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            Image(
                painter = painterResource(R.drawable.bottom_bar_home),
                contentDescription = "하단바",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(bottomBarHeight)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MainScreen()
}
