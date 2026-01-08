package com.capstone.ggud.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.capstone.ggud.R
import com.capstone.ggud.ui.theme.pBlack

@Composable
fun CardContent(
    title: String,
    date: String,
    time: String,
){
    Column {
        Text(text = title, fontWeight = Bold, fontSize = 18.sp, color = pBlack)
        Spacer(modifier = Modifier.height(8.dp))
        Row {
            Icon(painter = painterResource(R.drawable.ic_day), contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = date, fontSize = 14.sp, color = Color(0xFF4B5563))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row {
            Icon(painter = painterResource(R.drawable.ic_time), contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = time, fontSize = 14.sp, color = Color(0xFF4B5563))
        }
    }
}