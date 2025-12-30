package com.capstone.ggud.ui.notification

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.capstone.ggud.R

@Composable
fun NotificationScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row( //상단바
            modifier = Modifier
            .fillMaxWidth()
            .size(375.dp, 69.dp)
            .background(Color.White)
            .padding(24.dp, 17.dp)
            .zIndex(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image( //뒤로가기 버튼 (기능X)
                painter = painterResource(R.drawable.btn_back),
                contentDescription = "뒤로가기",
                modifier = Modifier
                    .padding(start = (7.7).dp)
                    .size(21.dp, 20.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "알림",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF111827)
            )
            Spacer(modifier = Modifier.weight(1f))
            Text( //기능X
                text = "모두 읽음",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF2563EB)
            )
        }
        Divider(thickness = 1.dp, color = Color(0xFFE5E7EB))

        Spacer(modifier = Modifier.height(11.dp))

        NotificationCard(
            title = "점심 모임이 1일 남았어요",
            descript = "내일 12:30에 강남역에서 만나요",
            time = "30분 전",
            read = true
        )
        Spacer(modifier = Modifier.height(8.dp))
        NotificationCard(
            title = "생일 파티가 3일 남았어요",
            descript = "12월 20일 18:00에 역삼역에서 만나요",
            time = "2시간 전",
            read = false
        )
    }
}

@Preview(showBackground = true)
@Composable
fun NotificationScreenPreview(){
    NotificationScreen()
}