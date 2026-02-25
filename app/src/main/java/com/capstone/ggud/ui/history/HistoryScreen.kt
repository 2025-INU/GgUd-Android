package com.capstone.ggud.ui.history

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.capstone.ggud.R
import com.capstone.ggud.data.PromiseRepository
import com.capstone.ggud.network.ApiClient
import com.capstone.ggud.network.PromiseApi
import com.capstone.ggud.ui.theme.pBlack

@Composable
fun HistoryScreen(navController: NavHostController) {
    val context = LocalContext.current

    val api = remember { ApiClient.getPromiseApi(context) }
    val repository = remember { PromiseRepository(api) }

    val vm: HistoryViewModel = viewModel(
        factory = HistoryViewModelFactory(repository)
    )
    val uiState by vm.uiState.collectAsState()

    var showSearchBar by remember { mutableStateOf(false) }
    val keywordValue = uiState.keyword

    val bottomBarHeight = 91.dp

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            Row( //상단바
                modifier = Modifier
                    .fillMaxWidth()
                    .height(76.dp)
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "약속 히스토리",
                    fontWeight = Bold,
                    fontSize = 24.sp,
                    color = pBlack
                )
                Spacer(modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .size(37.dp, 44.dp)
                        .padding(8.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            val next = !showSearchBar
                            showSearchBar = next
                            if (!next) {
                                vm.clearKeyword()
                            }
                        }
                ) {
                    Image(
                        painter = painterResource(if (showSearchBar) R.drawable.btn_history_cancel else R.drawable.btn_history_search),
                        contentDescription = "히스토리 검색 버튼",
                        modifier = Modifier
                            .size(if (showSearchBar) 24.dp else 20.dp)
                            .align(Alignment.Center)
                    )
                }
            }

            if (showSearchBar) { //검색바
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(62.dp)
                        .padding(start = 24.dp, end = 24.dp, bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF9FAFB))
                            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(12.dp))
                            .padding(17.dp, 13.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_search),
                            contentDescription = null,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(18.dp))
                        BasicTextField(
                            value = keywordValue,
                            onValueChange = { if (it.length <= 50) vm.onKeywordChanged(it) }, //50자 제한
                            singleLine = true,
                            textStyle = TextStyle(
                                fontSize = 14.sp,
                                color = Color(0xFF111827)
                            ),
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Done, //완료버튼 보이게
                                keyboardType = KeyboardType.Text //일반 문자 입력용 키보드
                            ),
                            decorationBox = { inner ->
                                if(keywordValue.isBlank()) {
                                    Text(
                                        text = "약속 이름이나 친구 이름을 검색하세요",
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 14.sp,
                                        color = Color(0xFF9CA3AF)
                                    )
                                }
                                inner()
                            }
                        )
                    }
                }
            }
            Divider(thickness = 1.dp, color = Color(0xFFE5E7EB))
            Spacer(modifier = Modifier.height(11.dp))

            val scrollState = rememberScrollState()
            Column( //카드 리스트
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp)
                    .padding(bottom = bottomBarHeight + 15.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp) //카드 사이 간격 16
            ) {
                when {
                    uiState.loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "불러오는 중...",
                                fontSize = 14.sp,
                                color = Color(0xFF6B7280)
                            )
                        }
                    }

                    uiState.items.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "표시할 히스토리가 없습니다.",
                                fontSize = 14.sp,
                                color = Color(0xFF6B7280)
                            )
                        }
                    }

                    else -> {
                        uiState.items.forEach { p ->
                            val date = HistoryViewModel.formatDate(p.promiseDateTime)
                            val time = HistoryViewModel.formatTime(p.promiseDateTime)
                            val spot = p.confirmedPlaceName ?: "장소 미정"

                            HistoryCard(
                                navController = navController,
                                title = p.title,
                                date = date,
                                time = time,
                                people = p.participantCount,
                                spot = spot
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(15.dp))
        }

        Box( //하단바
            modifier = Modifier
                .zIndex(1f)
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .offset(y=1.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.bottom_bar_his),
                contentDescription = "하단바",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(bottomBarHeight)
            )

            Row(modifier = Modifier.matchParentSize()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            navController.navigate("home")
                        }
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            navController.navigate("history")
                        }
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            navController.navigate("my")
                        }
                )
            }
        }
    }
}