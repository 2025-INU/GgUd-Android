package com.capstone.ggud.ui.my

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.capstone.ggud.R
import com.capstone.ggud.ui.components.Section

@Composable
fun MypageScreen() {
    val bottomBarHeight = 86.dp
    
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.background(Color(0xFFF9FAFB))) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "마이페이지",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )
            }
            Divider(thickness = 1.dp, color = Color(0xFFF3F4F6))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(R.drawable.profile),
                    contentDescription = "프로필이미지",
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "이은우",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF111827)
                )
            }
            Divider(thickness = 1.dp, color = Color(0xFFF3F4F6))

            Section("계정")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(182.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
            ) {
                Column {
                    Content( R.drawable.ic_profile, "프로필 수정")
                    Divider(thickness = 1.dp, color = Color(0xFFF9FAFB))
                    Content(R.drawable.ic_notify, "알림 설정")
                    Divider(thickness = 1.dp, color = Color(0xFFF9FAFB))
                    Content(R.drawable.ic_logout, "로그아웃")
                }
            }

            Section("이용 안내")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(121.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
            ) {
                Column {
                    Content(R.drawable.ic_support, "문의하기")
                    Divider(thickness = 1.dp, color = Color(0xFFF9FAFB))
                    Content(R.drawable.ic_terms, "이용약관")
                }
            }
        }
        Box( //하단바 (네비게이션X)
            modifier = Modifier
                .zIndex(1f)
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            Image(
                painter = painterResource(R.drawable.bottom_bar_my),
                contentDescription = "하단바",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(bottomBarHeight)
            )
        }
    }
}

@Composable
fun Content(
    @DrawableRes imageRes: Int,
    text: String
){
    Row(
        modifier = Modifier
            .padding(16.dp)
            .height(28.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.width(19.dp),
            contentAlignment = Alignment.Center
        ){
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = null
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF111827)
        )
        Spacer(modifier = Modifier.weight(1f))
        Image(
            painter = painterResource(R.drawable.btn_navigate),
            contentDescription = "다음페이지로 이동"
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MypageScreenPreview(){
    MypageScreen()
}