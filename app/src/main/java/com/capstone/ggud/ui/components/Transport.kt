package com.capstone.ggud.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.capstone.ggud.R
import com.capstone.ggud.ui.theme.pBlack

data class TransportItem(
    val id: Int,
    val name: String,
    @DrawableRes val iconRes: Int
)

val transportItems = listOf(
    TransportItem(1, "도보", R.drawable.ic_walk),
    TransportItem(2, "버스", R.drawable.ic_bus),
    TransportItem(3, "지하철", R.drawable.ic_subway),
    TransportItem(4, "자동차", R.drawable.ic_car),
    TransportItem(5, "택시", R.drawable.ic_taxi),
    TransportItem(6, "자전거", R.drawable.ic_bike),
)

@Composable
fun Transport() {

    //교통수단 선택 상태
    var selectedTransportId by remember { mutableIntStateOf(-1) } //선택 없음 = -1

    Text(
        text = "교통수단",
        fontWeight = Bold,
        fontSize = 18.sp,
        color = pBlack
    )
    Spacer(modifier = Modifier.height(16.dp))

    LazyVerticalGrid(
        columns = GridCells.Fixed(3), //한 행에 3개 버튼
        modifier = Modifier
            .fillMaxWidth()
            .height(236.dp),
        userScrollEnabled = false,
        horizontalArrangement = Arrangement.spacedBy(12.dp), //가로간격
        verticalArrangement = Arrangement.spacedBy(12.dp) //세로간격
    ) {
        items(
            items = transportItems,
            key = { it.id }
        ) { item ->
            TransportCard(
                item = item,
                selected = (item.id == selectedTransportId),
                onClick = {
                    selectedTransportId = if (selectedTransportId == item.id) -1 else item.id
                }
            )
        }
    }
}

@Composable
private fun TransportCard(
    item: TransportItem,
    selected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (selected) Color(0xFF0EA5E9) else Color(0xFFE5E7EB)
    val bgColor = if (selected) Color(0xFFF0F9FF) else Color.White

    Column(
        modifier = Modifier
            .size(101.dp, 112.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(item.iconRes),
            contentDescription = "교통수단 아이콘",
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = item.name,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = pBlack
        )
    }
}