package com.capstone.ggud.ui.notification

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsEndWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.capstone.ggud.R

@Composable
fun NotificationCard(
    title: String,
    descript: String,
    time: String,
    read: Boolean
) {
    if (read) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .size(343.dp, 102.dp)
                .background(Color(0xFF3B82F6)),
            horizontalArrangement = Arrangement.End
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .fillMaxHeight()
                    .size(336.dp)
                    .background(Color.White)
                    .padding(17.dp)

            ) {
                Content(title, descript, time, read)
            }
        }
    }
    else {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .size(343.dp, 102.dp)
                .background(Color.White)
                .padding(17.dp)
        ) {
            Content(title, descript, time, read)
        }
    }
}

@Composable
fun Content(
    title: String,
    descript: String,
    time: String,
    read: Boolean
) {
    Row {
        Image(
            painter = painterResource(R.drawable.ic_alarm),
            contentDescription = null
        )
        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF374151)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = descript,
                fontSize = 14.sp,
                color = Color(0xFF4B5563)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = time,
                fontSize = 12.sp,
                color = Color(0xFF6B7280)
            )
        }

        if (read) {
            Spacer(modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF3B82F6))
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NotificationCardPreview(){
    NotificationCard("제목", "내용", "30분 전", true)
}