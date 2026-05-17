package com.capstone.ggud.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.capstone.ggud.R

@Composable
fun PromiseProfileStack(
    people: Int,
    profileImageUrls: List<String?> = emptyList(),
    modifier: Modifier = Modifier
) {
    val avatarSize = 32.dp
    val overlapOffset = 22.dp

    val visibleProfileCount = if (people > 4) 4 else people
    val extraCount = people - visibleProfileCount

    val totalCount = visibleProfileCount + if (extraCount > 0) 1 else 0
    val stackWidth = if (totalCount <= 0) {
        avatarSize
    } else {
        avatarSize + overlapOffset * (totalCount - 1)
    }

    Row(
        modifier = modifier.width(stackWidth),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(stackWidth)
                .height(avatarSize)
        ) {
            repeat(visibleProfileCount) { index ->
                val profileImageUrl = profileImageUrls.getOrNull(index)

                AsyncImage(
                    model = profileImageUrl,
                    contentDescription = null,
                    placeholder = painterResource(R.drawable.ic_promise_profile),
                    error = painterResource(R.drawable.ic_promise_profile),
                    fallback = painterResource(R.drawable.ic_promise_profile),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(avatarSize)
                        .offset(x = overlapOffset * index)
                        .zIndex(index.toFloat())
                        .clip(CircleShape)
                        .border(2.dp, Color.White, CircleShape)
                )
            }

            if (extraCount > 0) {
                Box(
                    modifier = Modifier
                        .size(avatarSize)
                        .offset(x = overlapOffset * visibleProfileCount)
                        .zIndex(visibleProfileCount.toFloat())
                        .clip(CircleShape)
                        .background(Color(0xFFD1D5DB))
                        .border(
                            width = 2.dp,
                            color = Color.White,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+$extraCount",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF4B5563)
                    )
                }
            }
        }
    }
}