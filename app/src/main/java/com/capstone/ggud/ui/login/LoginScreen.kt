package com.capstone.ggud.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.capstone.ggud.R

@Composable
fun LoginScreen() {
    Column(
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 63.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.drawable.login_mark),
            contentDescription = null,
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "GgUd",
            fontWeight = Bold,
            fontSize = 36.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "친구들과의 완벽한 만남을 위한\n중간지점 찾기 서비스",
            fontSize = 16.sp,
            color = Color(0xFF4B5563),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(48.dp))
        Image(
            painter = painterResource(R.drawable.introduce),
            contentDescription = null,
            modifier = Modifier.size(327.dp, 278.dp)
        )
        Spacer(modifier = Modifier.height(48.dp))
        Box(
            modifier = Modifier.size(350.dp, 80.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.btn_kakao),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
        }
        Text(
            text = "카카오톡 계정으로 간편하게 시작하세요",
            fontSize = 14.sp,
            color = Color(0xFF9CA3AF),
            textAlign = TextAlign.Center
        )
    }
}


@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen()
}