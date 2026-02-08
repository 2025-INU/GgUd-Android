package com.capstone.ggud.ui.promise

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.capstone.ggud.R
import com.capstone.ggud.data.PromiseRepository
import com.capstone.ggud.network.ApiClient
import com.capstone.ggud.ui.components.TopBar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromiseScreen(navController: NavHostController) {

    val context = LocalContext.current

    val promiseRepo = remember {
        PromiseRepository(ApiClient.getPromiseApi(context))
    }
    val vm: PromiseViewModel = viewModel(
        factory = PromiseViewModelFactory(promiseRepo)
    )

    val state = vm.uiState

    var isNameFocused by remember { mutableStateOf(false) }
    var isDateFocused by remember { mutableStateOf(false) }
    var isTimeFocused by remember { mutableStateOf(false) }

    //포커스, 키보드 제어
    val focusManager = LocalFocusManager.current
    val nameFocusRequester = remember { FocusRequester() }

    fun clearFocusStates() { //포커스 상태 초기화
        isDateFocused = false
        isTimeFocused = false
    }

    //이름 입력 값
    var name by remember { mutableStateOf("") }

    //날짜,시간 선택 값
    var showDateDialog by remember { mutableStateOf(false) }
    var showTimeDialog by remember { mutableStateOf(false) }

    //DatePicker 상태
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = state.selectedDateMillis
    )

    //TimePicker 상태
    val timePickerState = rememberTimePickerState(
        initialHour = state.selectedHour ?: 12,
        initialMinute = state.selecteMinute ?: 0,
        is24Hour = true
    )

    fun formatDate(millis: Long): String {
        val sdf = SimpleDateFormat("yyyy.MM.dd", Locale.KOREA)
        return sdf.format(Date(millis))
    }

    fun formatTime(h: Int, m: Int): String = String.format("%02d:%02d", h, m)

    LaunchedEffect(state.createdPromiseId) {
        if (state.createdPromiseId != null) {
            navController.navigate("waiting")
            vm.consumeCreated()
        }
    }

    //날짜 다이얼로그
    if (showDateDialog) {
        DatePickerDialog(
            onDismissRequest = { //다이얼로그 밖을 누르거나 back으로 닫을때
                showDateDialog = false
                isDateFocused = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        //선택된 날짜 저장
                        vm.onDateSelected(datePickerState.selectedDateMillis)
                        //다이얼로그 닫기 + 테두리 포커스 해제
                        showDateDialog = false
                        isDateFocused = false
                    }
                ) { Text("확인") }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        //취소 시 다이얼로그만 닫기
                        showDateDialog = false
                        isDateFocused = false
                    }
                ) { Text("취소") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    //시간 다이얼로그
    if (showTimeDialog) {
        AlertDialog(
            onDismissRequest = {
                showTimeDialog = false
                isTimeFocused = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        //선택된 시간 저장
                        vm.onTimeSelected(timePickerState.hour, timePickerState.minute)
                        //다이얼로그 닫기 + 테두리 포커스 해제
                        showTimeDialog = false
                        isTimeFocused = false
                    }
                ) { Text("확인") }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showTimeDialog = false
                        isTimeFocused = false
                    }
                ) { Text("취소") }
            },
            title = { Text("시간 선택") },
            text = { TimePicker(state = timePickerState) }
        )
    }

    val isFormValid by remember(state) {
        derivedStateOf { state.isFormValid && !state.isLoading }
    }

    Box( //화면
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                focusManager.clearFocus()
                clearFocusStates()
            }
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                TopBar(navController, "약속 만들기")
                Spacer(modifier = Modifier.height(11.dp))

                Image(
                    painter = painterResource(R.drawable.ic_promise),
                    contentDescription = null,
                    modifier = Modifier.size(80.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "새로운 약속을 만들어보세요",
                    fontWeight = Bold,
                    fontSize = 24.sp,
                    color = Color(0xFF111827)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "친구들과의 만남을 위한 정보를 입력해주세요",
                    fontSize = 14.sp,
                    color = Color(0xFF4B5563)
                )
                Spacer(modifier = Modifier.height(32.dp))
            }

            Column(
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                Text(
                    text = "약속 이름 *",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color(0xFF374151)
                )
                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .size(327.dp, 58.dp)
                        .clickable {
                            clearFocusStates()
                            nameFocusRequester.requestFocus()
                        }
                        .border(
                            width = 1.dp,
                            color = if(isNameFocused) Color(0xFF3B82F6) else Color(0xFFE5E7EB),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(17.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    BasicTextField(
                        value = state.title,
                        onValueChange = { vm.onTitleChange(it) },
                        singleLine = true,
                        textStyle = TextStyle(
                            fontSize = 16.sp,
                            color = Color(0xFF111827)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(nameFocusRequester)
                            .onFocusChanged {focusState ->
                                isNameFocused = focusState.isFocused
                            },
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Done, //완료버튼 보이게
                            keyboardType = KeyboardType.Text //일반 문자 입력용 키보드
                        ),
                        keyboardActions = KeyboardActions(
                            //완료 버튼 누르면 포커스 해제, 테두리 회색
                            onDone = { focusManager.clearFocus() }
                        ),
                        decorationBox = { inner ->
                            if(state.title.isBlank()) {
                                Text(
                                    text = "약속 이름을 입력하세요",
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 16.sp,
                                    color = Color(0xFF6B7280)
                                )
                            }
                            inner()
                        }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                Row {
                    Text(
                        text = "예: 대학 동기 모임, 회사 회식, 생일 파티",
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "${state.title.length}/50",
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "만날 날짜 *",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color(0xFF374151)
                )
                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .size(327.dp, 58.dp)
                        .clickable {
                            focusManager.clearFocus()
                            isTimeFocused = false
                            isDateFocused = true
                            showDateDialog = true
                        }
                        .border(
                            width = 1.dp,
                            color = if(isDateFocused) Color(0xFF3B82F6) else Color(0xFFE5E7EB),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(17.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val dateText = state.selectedDateMillis?.let { formatDate(it) } ?: "-/-/-"

                        Text(
                            text = dateText,
                            fontSize = 14.sp,
                            color = Color(0xFF111827)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            imageVector = Icons.Outlined.CalendarToday,
                            contentDescription = "날짜 선택",
                            tint = Color(0xFF111827),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "만날 시간 *",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color(0xFF374151)
                )
                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .size(327.dp, 58.dp)
                        .clickable {
                            focusManager.clearFocus()
                            isDateFocused = false
                            isTimeFocused = true
                            showTimeDialog = true
                        }
                        .border(
                            width = 1.dp,
                            color = if(isTimeFocused) Color(0xFF3B82F6) else Color(0xFFE5E7EB),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(17.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val timeText = if (state.selectedHour != null && state.selecteMinute != null) {
                            formatTime(state.selectedHour!!, state.selecteMinute!!)
                        } else {
                            "--:--"
                        }

                        Text(
                            text = timeText,
                            fontSize = 14.sp,
                            color = Color(0xFF111827)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            imageVector = Icons.Outlined.AccessTime,
                            contentDescription = "시간 선택",
                            tint = Color(0xFF111827),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(48.dp))
            }

            Image(
                painter = painterResource(if (isFormValid) R.drawable.btn_create else R.drawable.btn_create_disabled),
                contentDescription = "약속 생성",
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .scale(0.97f)
                    .clickable(
                        enabled = isFormValid,
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        vm.createPromise()
                    }
            )
            Text(
                text = "모든 필수 정보를 입력해주세요",
                fontSize = 14.sp,
                color = Color(0xFF6B7280),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .offset(y=-(8).dp)
            )
        }
    }
}