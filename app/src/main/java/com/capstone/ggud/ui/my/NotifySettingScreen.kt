package com.capstone.ggud.ui.my

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.capstone.ggud.R
import com.capstone.ggud.ui.components.Section
import com.capstone.ggud.ui.components.TopBar

@Composable
fun NotifySettingScreen(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB))
    ) {
        TopBar(navController, "알림 설정")
        Spacer(modifier = Modifier.height(11.dp))

        Section("약속 알림")
        Box(modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .wrapContentHeight()
                .heightIn(min = 155.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
        ) {
            Column {
                ToggleContent("약속 초대", "새로운 약속 초대를 받을 때 알림")
                Divider(thickness = 1.dp, color = Color(0xFFF9FAFB))
                ToggleContent("약속 알림", "예정된 약속 알림 전 미리 알림")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Section("알림 방식")
        Box(modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .wrapContentHeight()
                .heightIn(min = 77.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
        ) {
            ToggleContent("푸시 알림", "앱 푸시 알림 받기")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Image( //저장 버튼 (기능X)
            painter = painterResource(R.drawable.btn_notify_save),
            contentDescription = "알림 설정 저장 버튼",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .align(Alignment.CenterHorizontally)
        )
    }
}

//메뉴 내용
@Composable
fun ToggleContent(
    title: String,
    description: String
){
    //토글 색만 변경 (기능X)
    var checked by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp, 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.width(251.dp)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF111827)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                fontSize = 14.sp,
                color = Color(0xFF6B7280)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        ToggleSwitch(checked = checked, onCheckedChange = { checked = it })
    }
}

//토글 스위치 설정
@Composable
fun ToggleSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        colors = SwitchDefaults.colors(
            checkedTrackColor = Color(0xFF3B82F6), //ON
            uncheckedTrackColor = Color(0xFFD1D5DB), //OFF

            checkedThumbColor = Color.White, //thumb 항상 흰색
            uncheckedThumbColor = Color.White,

            checkedBorderColor = Color.Transparent, //테두리 없애기
            uncheckedBorderColor = Color.Transparent
        )
    )
}