package com.capstone.ggud.ui.main

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.capstone.ggud.R
import com.capstone.ggud.data.PromiseRepository
import com.capstone.ggud.network.ApiClient
import com.capstone.ggud.network.dto.PromiseStatus
import com.capstone.ggud.ui.components.CardContent
import com.capstone.ggud.ui.components.PromiseProfileStack
import com.capstone.ggud.ui.theme.pBlack
import com.capstone.ggud.ui.theme.pBlue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavHostController,
    focusPromiseId: Long? = null
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val api = remember { ApiClient.getPromiseApi(context) }
    val repository = remember { PromiseRepository(api) }

    val vm: MainViewModel = viewModel(
        factory = MainViewModelFactory(repository)
    )
    val uiState by vm.uiState.collectAsState()

    var promise by remember { mutableStateOf(true) }
    val focusedCardRequester = remember { BringIntoViewRequester() }
    var focusTargetId by remember(focusPromiseId) { mutableStateOf(focusPromiseId) }

    var showPromiseDialog by remember { mutableStateOf(false) }
    var showJoinDialog by remember { mutableStateOf(false) }
    var joinCode by remember { mutableStateOf("") }

    var searchedPromiseTitle by remember { mutableStateOf("") }
    var searchedPromiseDate by remember { mutableStateOf("") }
    var searchedPromiseTime by remember { mutableStateOf("") }
    var searchedPromiseHost by remember { mutableStateOf("") }
    var searchedPromiseId by remember { mutableStateOf<Long?>(null) }
    var isCheckingCode by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    fun resetSearchedPromise() {
        searchedPromiseTitle = ""
        searchedPromiseDate = ""
        searchedPromiseTime = ""
        searchedPromiseHost = ""
        searchedPromiseId = null
    }

    fun checkInviteCode(code: String) {
        resetSearchedPromise()

        if (code.length != 6) return

        isCheckingCode = true

        CoroutineScope(Dispatchers.Main).launch {
            runCatching {
                repository.getPromiseByInviteCode(code)
            }.onSuccess { promise ->
                if (promise.status == PromiseStatus.RECRUITING) {
                    searchedPromiseTitle = promise.title
                    searchedPromiseDate = MainViewModel.formatDate(promise.promiseDateTime)
                    searchedPromiseTime = MainViewModel.formatTime(promise.promiseDateTime)
                    searchedPromiseHost = promise.hostNickname ?: ""
                    searchedPromiseId = promise.id
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
                    "존재하지 않는 약속입니다. 코드를 다시 확인해주세요.",
                    Toast.LENGTH_SHORT
                ).show()
            }

            isCheckingCode = false
        }
    }

    val bottomBarHeight = 91.dp
    val fabGap = 80.dp

    val scrollState = rememberScrollState()
    val density = LocalDensity.current

    LaunchedEffect(
        focusTargetId,
        uiState.loading,
        uiState.upcoming
    ) {
        val targetId = focusTargetId ?: return@LaunchedEffect

        if (!uiState.loading) {
            val existsInUpcoming = uiState.upcoming.any { it.id == targetId }

            if (existsInUpcoming) {
                promise = false
                delay(300)

                focusedCardRequester.bringIntoView()

                val extraScroll = with(density) { 350.dp.roundToPx() }
                scrollState.animateScrollTo(
                    (scrollState.value + extraScroll).coerceAtMost(scrollState.maxValue)
                )

                focusTargetId = null
            }
        }
    }

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
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 76.dp)
                .background(Color.White)
                .verticalScroll(scrollState)
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
                ) {
                    focusTargetId = null
                    promise = !promise
                    vm.load()
                }
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
                        profileImageUrls = uiState.profileImageUrlsByPromiseId[p.id].orEmpty(),
                        spot = p.confirmedPlaceName ?: "장소 미정",
                        onClick = { navController.navigate("ongoing/${p.id}/${Uri.encode(p.title)}") },
                        onEndClick = {
                            vm.completePromise(
                                promiseId = p.id,
                                onSuccess = {
                                    Toast.makeText(
                                        context,
                                        "약속이 종료되었습니다.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                onFailure = {
                                    Toast.makeText(
                                        context,
                                        "호스트가 약속을 종료할 수 있습니다.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            )
                        }
                    )
                }
            } else {
                list.forEach { p ->
                    val focusModifier =
                        if (p.id == focusTargetId) {
                            Modifier.bringIntoViewRequester(focusedCardRequester)
                        } else {
                            Modifier
                        }

                    when (p.status) {
                        PromiseStatus.RECRUITING,
                        PromiseStatus.SELECTING_MIDPOINT,
                        PromiseStatus.MIDPOINT_CONFIRMED -> {
                            SimpleUpcomingCard(
                                modifier = focusModifier,
                                name = p.title,
                                date = MainViewModel.formatDate(p.promiseDateTime),
                                time = MainViewModel.formatTime(p.promiseDateTime),
                                onClick = {
                                    when (p.status) {
                                        PromiseStatus.RECRUITING -> { navController.navigate("waiting/${p.id}") }
                                        PromiseStatus.SELECTING_MIDPOINT -> { navController.navigate("middle_point/${p.id}") }
                                        PromiseStatus.MIDPOINT_CONFIRMED -> { navController.navigate("recommend_place/${p.id}/${Uri.encode(p.midpointStationName.orEmpty())}") }
                                        else -> Unit
                                    }
                                },
                                onCancelClick = {
                                    vm.cancelPromise(
                                        promiseId = p.id,
                                        onSuccess = {
                                            Toast.makeText(
                                                context,
                                                "약속이 취소되었습니다.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        },
                                        onFailure = {
                                            Toast.makeText(
                                                context,
                                                "호스트가 약속을 취소할 수 있습니다.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    )
                                }
                            )
                        }

                        PromiseStatus.PLACE_CONFIRMED -> {
                            ConfirmedCard(
                                modifier = focusModifier,
                                name = p.title,
                                date = MainViewModel.formatDate(p.promiseDateTime),
                                time = MainViewModel.formatTime(p.promiseDateTime),
                                people = p.participantCount,
                                profileImageUrls = uiState.profileImageUrlsByPromiseId[p.id].orEmpty(),
                                spot = p.confirmedPlaceName ?: "장소 미정",
                                onCancelClick = {
                                    vm.cancelPromise(
                                        promiseId = p.id,
                                        onSuccess = {
                                            Toast.makeText(
                                                context,
                                                "약속이 취소되었습니다.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        },
                                        onFailure = {
                                            Toast.makeText(
                                                context,
                                                "호스트가 약속을 취소할 수 있습니다.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    )
                                }
                            )
                        }

                        else -> Unit
                    }
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
            Dialog(
                onDismissRequest = { showPromiseDialog = false }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(28.dp)
                        )
                        .padding(
                            start = 28.dp,
                            end = 28.dp,
                            top = 24.dp,
                            bottom = 24.dp
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "약속",
                        fontSize = 20.sp,
                        fontWeight = Bold,
                        color = Color(0xFF111827)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "원하시는 기능을 선택해주세요.",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF9CA3AF),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                showPromiseDialog = false
                                showJoinDialog = true
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFF0F9FF),
                                contentColor = pBlue
                            ),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.5.dp, Color(0xFFE0F2FE)),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 0.dp,
                                pressedElevation = 0.dp
                            ),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = "약속 참여",
                                fontSize = 16.sp,
                                fontWeight = Bold
                            )
                        }

                        Button(
                            onClick = {
                                showPromiseDialog = false
                                navController.navigate("promise")
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFF0F9FF),
                                contentColor = pBlue
                            ),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.5.dp, Color(0xFFE0F2FE)),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 0.dp,
                                pressedElevation = 0.dp
                            ),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = "약속 생성",
                                fontSize = 16.sp,
                                fontWeight = Bold
                            )
                        }
                    }
                }
            }
        }

        if (showJoinDialog) { //약속참여 바텀시트
            ModalBottomSheet(
                onDismissRequest = {
                    showJoinDialog = false
                    joinCode = ""
                    resetSearchedPromise()
                },
                sheetState = sheetState,
                containerColor = Color(0xFFF9FAFB),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                dragHandle = {
                    Box(
                        modifier = Modifier
                            .padding(top = 10.dp, bottom = 8.dp)
                            .width(42.dp)
                            .height(5.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color(0xFFD1D5DB))
                    )
                }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 28.dp)
                        .padding(bottom = 36.dp)
                ) {
                    Text(
                        text = "초대 코드로 참여",
                        fontSize = 22.sp,
                        fontWeight = Bold,
                        color = pBlack
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "친구가 공유한 초대 코드를 붙여넣고 약속에 참여해보세요.",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF4B5563)
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    BasicTextField(
                        value = joinCode,
                        onValueChange = { input ->
                            val newValue = input
                                .uppercase()
                                .filter { it.isDigit() || it in 'A'..'Z' }
                                .take(6)

                            joinCode = newValue
                            checkInviteCode(newValue)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Ascii
                        ),
                        textStyle = TextStyle(
                            fontSize = 18.sp,
                            fontWeight = Bold,
                            color = pBlack
                        ),
                        cursorBrush = SolidColor(pBlue),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp)
                            .background(Color.White, RoundedCornerShape(14.dp))
                            .padding(horizontal = 20.dp),
                        decorationBox = { innerTextField ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.weight(1f)) {
                                    if (joinCode.isBlank()) {
                                        Text(
                                            text = "초대 코드를 입력하세요",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFFD1D5DB)
                                        )
                                    }

                                    innerTextField()
                                }

                                if (isCheckingCode) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                        color = Color(0xFF3B62F6)
                                    )
                                }
                            }
                        }
                    )

                    if (searchedPromiseId != null) {
                        Spacer(modifier = Modifier.height(20.dp))

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White, RoundedCornerShape(14.dp))
                                .padding(horizontal = 20.dp, vertical = 18.dp)
                        ) {
                            Text(
                                text = searchedPromiseTitle,
                                fontSize = 18.sp,
                                fontWeight = Bold,
                                color = Color(0xFF111827)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_day),
                                    contentDescription = null,
                                    tint = Color(0xFF9CA3AF),
                                    modifier = Modifier.size(16.dp)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = searchedPromiseDate,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF9CA3AF)
                                )

                                Spacer(modifier = Modifier.width(16.dp))

                                Icon(
                                    painter = painterResource(R.drawable.ic_time),
                                    contentDescription = null,
                                    tint = Color(0xFF9CA3AF),
                                    modifier = Modifier.size(16.dp)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = searchedPromiseTime,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF9CA3AF)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "주최자: $searchedPromiseHost",
                                fontSize = 14.sp,
                                fontWeight = Bold,
                                color = Color(0xFF60A5FA)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Button(
                            onClick = {
                                val pasted = clipboardManager.getText()?.text.orEmpty()
                                val newValue = pasted
                                    .uppercase()
                                    .filter { it.isDigit() || it in 'A'..'Z' }
                                    .take(6)

                                joinCode = newValue
                                checkInviteCode(newValue)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(55.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = pBlue
                            ),
                            shape = RoundedCornerShape(14.dp),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 0.dp,
                                pressedElevation = 0.dp
                            ),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = "붙여넣기",
                                fontSize = 16.sp,
                                fontWeight = Bold
                            )
                        }

                        Button(
                            onClick = {
                                val code = joinCode.trim()

                                if (code.length != 6) {
                                    Toast.makeText(
                                        context,
                                        "초대 코드 6자리를 입력해주세요.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    return@Button
                                }

                                CoroutineScope(Dispatchers.Main).launch {
                                    runCatching {
                                        repository.joinPromiseByInviteCode(code)
                                    }.onSuccess { joinedPromise ->
                                        showJoinDialog = false
                                        joinCode = ""
                                        searchedPromiseTitle = ""
                                        searchedPromiseDate = ""
                                        searchedPromiseTime = ""
                                        searchedPromiseHost = ""
                                        searchedPromiseId = null

                                        navController.navigate("waiting/${joinedPromise.id}")
                                    }.onFailure {
                                        Toast.makeText(
                                            context,
                                            "약속 참여에 실패했습니다.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            },
                            enabled = searchedPromiseId != null && !isCheckingCode,
                            modifier = Modifier
                                .weight(1f)
                                .height(55.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2563EB),
                                contentColor = Color.White,
                                disabledContainerColor = Color(0xFFBFDBFE),
                                disabledContentColor = Color.White
                            ),
                            shape = RoundedCornerShape(14.dp),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 0.dp,
                                pressedElevation = 0.dp
                            ),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = "참여하기",
                                fontSize = 16.sp,
                                fontWeight = Bold
                            )
                        }
                    }
                }
            }
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
    profileImageUrls: List<String?>,
    spot: String,
    onClick: () -> Unit,
    onEndClick: () -> Unit
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
            Column {
                Image(
                    painter = painterResource(R.drawable.ic_promise_in_progress),
                    contentDescription = null
                )

                Spacer(modifier = Modifier.height(10.dp))

                Image(
                    painter = painterResource(R.drawable.btn_end),
                    contentDescription = "약속종료 버튼",
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onEndClick() }
                )
            }
        }
        Spacer(modifier = Modifier.height(28.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            PromiseProfileStack(people = people, profileImageUrls = profileImageUrls)
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
    modifier: Modifier = Modifier,
    name: String,
    date: String,
    time: String,
    people: Int,
    profileImageUrls: List<String?>,
    spot: String,
    onCancelClick: () -> Unit
){
    Column(
        modifier = modifier
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
            Column {
                Image(
                    painter = painterResource(R.drawable.ic_promise_confirmed),
                    contentDescription = null
                )

                Spacer(modifier = Modifier.height(10.dp))

                Image(
                    painter = painterResource(R.drawable.btn_cancel),
                    contentDescription = "약속취소 버튼",
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onCancelClick() }
                )
            }
        }
        Spacer(modifier = Modifier.height(28.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            PromiseProfileStack(people = people, profileImageUrls = profileImageUrls)
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
                        .background(pBlue)
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

    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
fun SimpleUpcomingCard(
    modifier: Modifier = Modifier,
    name: String,
    date: String,
    time: String,
    onClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    Column(
        modifier = modifier
            .width(327.dp)
            .wrapContentHeight()
            .heightIn(min = 150.dp)
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
            Column {
                Image(
                    painter = painterResource(R.drawable.ic_promise_upcoming),
                    contentDescription = null
                )

                Spacer(modifier = Modifier.height(10.dp))

                Image(
                    painter = painterResource(R.drawable.btn_cancel),
                    contentDescription = "약속취소 버튼",
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onCancelClick() }
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))
}