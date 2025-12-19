package com.capstone.ggud.ui.my

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.capstone.ggud.R
import com.capstone.ggud.ui.components.TopBar

@Composable
fun ProfileEditScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TopBar("프로필 수정")
        Spacer(modifier = Modifier.height(11.dp))

        Column(
            modifier = Modifier
                .size(327.dp, 260.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(24.dp)
        ){
            Text(
                text = "프로필 사진",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF111827),
                modifier = Modifier.align(Alignment.Start)
            )

            Box(
                modifier = Modifier
                    .padding(vertical = 24.dp)
                    .size(96.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                Image(
                    painter = painterResource(R.drawable.profile),
                    contentDescription = null,
                    modifier = Modifier.size(96.dp)
                )

                Image( //프로필수정 버튼 (기능X)
                    painter = painterResource(R.drawable.btn_edit),
                    contentDescription = "수정",
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(32.dp)
                        .offset(x = 8.dp, y = 6.dp)
                )
            }

            Text(
                text = "프로필 사진을 변경하려면 카메라 아이콘을\n터치하세요",
                fontSize = 14.sp,
                color = Color(0xFF6B7280),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier
                .size(327.dp, 175.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(24.dp)
        ) {
            Text(
                text = "기본 정보",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF111827),
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "이름",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF374151),
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box( //이름 텍스트필드 (기능X)
                modifier = Modifier
                    .size(279.dp, 47.dp)
                    .border(
                        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(17.dp, 13.dp)
            ) {
                Text(text = "이은우", fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Image( //저장 버튼 (기능X)
            painter = painterResource(R.drawable.btn_profile_save),
            contentDescription = "프로필 수정 저장 버튼",
            modifier = Modifier.width(327.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Image( //취소 버튼 (기능X)
            painter = painterResource(R.drawable.btn_profile_cancel),
            contentDescription = "프로필 수정 취소 버튼",
            modifier = Modifier.width(327.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileEditScreenPreview() {
    ProfileEditScreen()
}