package com.capstone.ggud.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
fun TopBar(title: String) {
    Column {
        Row(modifier = Modifier
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
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF111827)
            )
        }
        Divider(thickness = 1.dp, color = Color(0xFFE5E7EB))
    }
}

@Preview(showBackground = true)
@Composable
fun TopBarPreview() {
    TopBar("상단바")
}