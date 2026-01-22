package com.capstone.ggud.ui.history

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.capstone.ggud.R
import com.capstone.ggud.ui.components.CardContent

@Composable
fun HistoryCard(
    navController: NavHostController,
    title: String,
    date: String,
    time: String,
    people: Int,
    spot: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(208.dp)
            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp))
            .padding(25.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(94.dp)
        ) {
            CardContent(title, date, time)
            Spacer(modifier = Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(R.drawable.ic_complete),
                    contentDescription = null,
                    modifier = Modifier.size(50.dp, 28.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Image( //정산하기 버튼
                    painter = painterResource(R.drawable.btn_calculate),
                    contentDescription = "정산하기 이동",
                    modifier = Modifier
                        .size(75.5.dp, 28.dp)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            navController.navigate("calculate")
                        }
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween //남는 간격 자동분배
        ) {
            Image(painter = painterResource(R.drawable.ic_people), contentDescription = null)
            Text(
                text = "$people" + "명 참여",
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = Color(0xFF4B5563)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.ic_spot),
                    contentDescription = null,
                    tint = Color(0xFF4B5563)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = spot,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = Color(0xFF4B5563)
                )
            }
        }
    }
}