package com.capstone.ggud.ui.main

import android.widget.Toast
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.capstone.ggud.R
import com.capstone.ggud.data.PromiseRepository
import com.capstone.ggud.network.ApiClient
import com.capstone.ggud.network.dto.PromiseStatus
import com.capstone.ggud.ui.components.CardContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun MainScreen(navController: NavHostController) {
    val context = LocalContext.current

    val api = remember { ApiClient.getPromiseApi(context) }
    val repository = remember { PromiseRepository(api) }

    val vm: MainViewModel = viewModel(
        factory = MainViewModelFactory(repository)
    )
    val uiState by vm.uiState.collectAsState()

    var promise by remember { mutableStateOf(true) }

    var showPromiseDialog by remember { mutableStateOf(false) }
    var showJoinDialog by remember { mutableStateOf(false) }
    var joinCode by remember { mutableStateOf("") }

    val bottomBarHeight = 91.dp
    val fabGap = 80.dp

    Box(modifier = Modifier.fillMaxSize()) {
        //상단바
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .height(76.dp)
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "GgUd",
                fontWeight = Bold,
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.weight(1f))
            Image(
                painter = painterResource(R.drawable.btn_notify),
                contentDescription = "알림페이지",
                modifier = Modifier
                    .size(22.dp, 21.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        navController.navigate("notification")
                    }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 76.dp)
                .background(Color.White)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = bottomBarHeight + fabGap),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(4.dp))

            Image( //창 변환 버튼
                painter = painterResource(if (promise) R.drawable.main_promise_bar else R.drawable.main_promise_bar_upcoming),
                contentDescription = if (promise) "진행중인 약속" else "예정된 약속",
                modifier = Modifier.clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { promise = !promise }
            )

            Spacer(modifier = Modifier.height(24.dp))

            val list = if (promise) uiState.inProgress else uiState.upcoming

            //약속 목록
            if (promise) {
                list.forEach { p ->
                    InProgressCard(
                        name = p.title,
                        date = MainViewModel.formatDate(p.promiseDateTime),
                        time = MainViewModel.formatTime(p.promiseDateTime),
                        people = p.participantCount,
                        spot = p.confirmedPlaceName ?: "장소 미정",
                        onClick = { navController.navigate("ongoing") }
                    )
                }
            } else {
                list.forEach { p ->
                    ConfirmedCard(
                        name = p.title,
                        date = MainViewModel.formatDate(p.promiseDateTime),
                        time = MainViewModel.formatTime(p.promiseDateTime),
                        people = p.participantCount,
                        spot = p.confirmedPlaceName ?: "장소 미정"
                    )
                }
            }
        }

        Image(
            painter = painterResource(R.drawable.btn_create_promise),
            contentDescription = "약속 생성",
            modifier = Modifier
                .zIndex(2f)
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 16.dp, bottom = fabGap)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    showPromiseDialog = true
                }
        )

        if (showPromiseDialog) { //약속 다이얼로그
            AlertDialog(
                onDismissRequest = { showPromiseDialog = false }, //바깥영역 눌렀을때
                containerColor = Color.White, //배경
                title = { Text("약속") },
                text = { Text("원하시는 기능을 선택해주세요.")},
                confirmButton = { //오른쪽 버튼
                    TextButton(
                        onClick = {
                            showPromiseDialog = false
                            navController.navigate("promise")
                        }
                    ) { Text("약속 생성") }
                },
                dismissButton = { //왼쪽 버튼
                    TextButton(
                        onClick = {
                            showPromiseDialog = false
                            showJoinDialog = true
                        }
                    ) { Text("약속 참여") }
                }
            )
        }

        if (showJoinDialog) { //약속참여 다이얼로그 (기능X)
            AlertDialog(
                onDismissRequest = {
                    showJoinDialog = false
                    joinCode = "" //참여코드 초기화
                },
                containerColor = Color.White,
                title = { Text("약속 참여") },
                text = {
                    Column {
                        Text("카카오톡으로 받은 참여 코드를 입력해주세요.")
                        Spacer(modifier = Modifier.height(12.dp))

                        Box( //참여코드 입력칸
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            BasicTextField( //실제로는 텍스트필드 하나지만 6칸처럼 보이도록
                                value = joinCode, //입력값
                                onValueChange = { input ->
                                    val newValue = input
                                        .uppercase() //대문자 변환
                                        .filter { it.isDigit() || it in 'A'..'Z' } //대문자+숫자만
                                        .take(6) //6자리 제한

                                    if (joinCode != newValue) {
                                        joinCode = newValue
                                    }
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Ascii
                                ),
                                textStyle = TextStyle( //실제 텍스트는 투명처리, 직접 6칸을 그려야하기 때문에
                                    color = Color.Transparent
                                ),
                                cursorBrush = SolidColor(Color.Transparent), //커서도 투명처리
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                decorationBox = { //UI 직접 그려주는 영역
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        //가로 정렬 칸 간격 12, 가운데 정렬
                                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                                    ) {
                                        repeat(6) { index -> //6칸 반복 생성
                                            //현재 칸에 들어갈 문자
                                            //joinCode가 12면, 0번칸에 1, 1번칸에 2, 나머지는 빈문자열
                                            val char = joinCode.getOrNull(index)?.toString() ?: ""
                                            val isFocused = joinCode.length == index //현재 칸 위치 표시, joinCode 길이로

                                            Box(
                                                modifier = Modifier
                                                    .size(width = 36.dp, height = 52.dp)
                                                    .border(
                                                        width = 1.5.dp,
                                                        color = if (isFocused) Color.Black else Color(0xFFE5E7EB),
                                                        shape = RoundedCornerShape(12.dp)
                                                    )
                                                    .background(
                                                        Color.White,
                                                        RoundedCornerShape(12.dp)
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = char,
                                                    fontSize = 20.sp,
                                                    color = Color(0xFF111827),
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val code = joinCode.trim()

                            CoroutineScope(Dispatchers.Main).launch {
                                runCatching {
                                    repository.getPromiseByInviteCode(code)
                                }.onSuccess { promise ->
                                    if (promise.status == PromiseStatus.RECRUITING) {
                                        showJoinDialog = false
                                        joinCode = ""

                                        navController.navigate("waiting/${promise.id}")
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "이미 진행 중이거나 참여할 수 없는 약속입니다.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }.onFailure {
                                    Toast.makeText(
                                        context,
                                        "존재하지 않는 약속입니다.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    ) { Text("확인") }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showJoinDialog = false
                            joinCode = ""
                        }
                    ) { Text("취소") }
                }
            )
        }

        Box( //하단바
            modifier = Modifier
                .zIndex(1f)
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .offset(y=1.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.bottom_bar_home),
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

//진행중인 약속 카드
@Composable
fun InProgressCard(
    name: String,
    date: String,
    time: String,
    people: Int,
    spot: String,
    onClick: () -> Unit
){
    Column(
        modifier = Modifier
            .width(327.dp)
            .wrapContentHeight()
            .heightIn(min = 226.dp)
            .border(
                width = 1.dp,
                color = Color(0xFFE5E7EB),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .padding(25.dp)
    ) {
        Row {
            CardContent(name, date, time)
            Spacer(modifier = Modifier.weight(1f))
            Image(
                painter = painterResource(R.drawable.ic_promise_in_progress),
                contentDescription = null
            )
        }
        Spacer(modifier = Modifier.height(28.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(painter = painterResource(R.drawable.ic_people), contentDescription = null)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "$people"+"명",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF4B5563)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(R.drawable.ic_spot),
                contentDescription = null,
                tint = Color(0xFF4B5563)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = spot,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF4B5563)
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))
}

//예정된 약속 카드
@Composable
fun ConfirmedCard(
    name: String,
    date: String,
    time: String,
    people: Int,
    spot: String
){
    Column(
        modifier = Modifier
            .width(327.dp)
            .wrapContentHeight()
            .heightIn(min = 305.dp)
            .border(
                width = 1.dp,
                color = Color(0xFFE5E7EB),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(25.dp)
    ) {
        Row {
            CardContent(name, date, time)
            Spacer(modifier = Modifier.weight(1f))
            Image(
                painter = painterResource(R.drawable.ic_promise_confirmed),
                contentDescription = null
            )
        }
        Spacer(modifier = Modifier.height(28.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(painter = painterResource(R.drawable.ic_people), contentDescription = null)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "$people"+"명",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF4B5563)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(R.drawable.ic_spot),
                contentDescription = null,
                tint = Color(0xFF4B5563)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = spot,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF4B5563)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Column(
            modifier = Modifier
                .width(277.dp)
                .wrapContentHeight()
                .heightIn(min = 72.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFF3F4F6))
                .padding(12.dp)
        ) {
            Text(
                text = "확정된 장소",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF374151)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF3B82F6))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = spot,
                    fontSize = 14.sp,
                    color = Color(0xFF4B5563)
                )
            }
        }
    }
}