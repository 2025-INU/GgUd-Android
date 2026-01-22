package com.capstone.ggud.ui.promise

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.capstone.ggud.R
import com.capstone.ggud.ui.components.TopBar
import com.capstone.ggud.ui.theme.pBlack

@Composable
fun WaitingRoomScreen(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        TopBar(navController, "약속 대기방")
        Spacer(modifier = Modifier.height(11.dp))

        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth().aspectRatio(327f / 165f)
                    .paint(
                        painter = painterResource(R.drawable.bg_promise_waiting),
                        contentScale = ContentScale.Crop
                    )
                    .padding(24.dp)
            ) {
                Row {
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
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "약속이 생성되었습니다!",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = Color(0xFF16A34A),
                        modifier = Modifier.align(Alignment.CenterStart)
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "친구 초대하기",
                fontWeight = Bold,
                fontSize = 18.sp,
                color = pBlack
            )
            Spacer(modifier = Modifier.height(16.dp))

            Image(
                painter = painterResource(R.drawable.btn_kakao_link),
                contentDescription = "링크공유 버튼",
                modifier = Modifier.scale(1.08f)
            )

            Text(
                text = "링크를 통해 친구들이 약속에 참여할 수 있어요",
                fontSize = 14.sp,
                color = Color(0xFF6B7280),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(32.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "참여한 친구",
                    fontWeight = Bold,
                    fontSize = 18.sp,
                    color = pBlack
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "2명",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color(0xFF0284C7)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            PeopleCard("이은우 (나)", true)
            PeopleCard("윤은석", false)
            Spacer(modifier = Modifier.height(20.dp))

            Column (
                modifier = Modifier
                    .fillMaxWidth().aspectRatio(327f / 112f)
                    .background(Color(0xFFF0FDF4))
                    .border(1.dp, Color(0xFFBBF7D0), RoundedCornerShape(12.dp))
                    .padding(17.dp)
            ) {
                Text(
                    text = "모든 참여자가 위치를 입력했습니다!",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color(0xFF166534)
                )
                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF16A34A))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "중간지점 확인하기",
                        fontWeight = Bold,
                        fontSize = 14.sp,
                        color = Color.White,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

@Composable
fun PeopleCard(
    name: String,
    enterLocation: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth().aspectRatio(327f / 80f)
            .clip(RoundedCornerShape(12.dp))
            .padding(bottom = 12.dp)
            .background(Color(0xFFF9FAFB))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(R.drawable.ic_promise_profile),
            contentDescription = null,
            modifier = Modifier.fillMaxHeight().aspectRatio(1f)
        )
        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = name,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = pBlack
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (enterLocation) "위치 입력 완료" else "위치 입력 전",
                fontSize = 14.sp,
                color = Color(0xFF4B5563)
            )
        }
        Spacer(modifier = Modifier.weight(1f))

        if (enterLocation) {
            Image(
                painter = painterResource(R.drawable.ic_check),
                contentDescription = null,
                modifier = Modifier.size(36.dp)
            )
        }
    }
}