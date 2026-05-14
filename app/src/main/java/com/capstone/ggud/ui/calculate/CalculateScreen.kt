package com.capstone.ggud.ui.calculate

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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.capstone.ggud.R
import com.capstone.ggud.data.PromiseRepository
import com.capstone.ggud.network.ApiClient
import com.capstone.ggud.ui.theme.pBlack
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

private fun formatWon(value: Long): String {
    val nf = NumberFormat.getNumberInstance(Locale.KOREA)
    return nf.format(value) + "원"
}

@Composable
fun CalculateScreen(
    navController: NavHostController,
    promiseId: Long
) {
    val context = LocalContext.current

    val vm: CalculateViewModel = viewModel(
        factory = CalculateViewModelFactory(
            repo = PromiseRepository(ApiClient.getPromiseApi(context))
        )
    )
    val uiState by vm.uiState.collectAsState()

    LaunchedEffect(promiseId) {
        vm.fetchMe()
        vm.loadExpenses(promiseId)
    }

    LaunchedEffect(uiState.myAmountText) {
        if (uiState.myAmountText.isBlank()) return@LaunchedEffect

        kotlinx.coroutines.delay(500)

        vm.submitMyExpense(promiseId)
    }

    val settlement = uiState.settlement ?: return
    val expenses = settlement?.expenses.orEmpty()
    val transfers = settlement?.transfers.orEmpty()

    val totalAmount = settlement?.totalAmount ?: 0L
    val perPersonText = "1인당 ${formatWon(settlement?.perPersonAmount ?: 0L)}"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column { //상단바
            Row(modifier = Modifier
                .fillMaxWidth()
                .size(375.dp, 80.dp)
                .background(Color.White)
                .padding(24.dp, 16.dp)
                .zIndex(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image( //뒤로가기 버튼
                    painter = painterResource(R.drawable.btn_back),
                    contentDescription = "뒤로가기",
                    modifier = Modifier
                        .padding(start = (7.7).dp)
                        .size(21.dp, 20.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            val popped = navController.popBackStack()
                            //혹시 pop이 안 되면 navigateUp 시도
                            if (!popped) navController.navigateUp()
                        }
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "정산하기",
                        fontSize = 18.sp,
                        fontWeight = Bold,
                        color = pBlack
                    )
                    Text(
                        text = settlement.promiseName,
                        fontSize = 14.sp,
                        color = Color(0xFF4B5563)
                    )
                }
            }
            Divider(thickness = 1.dp, color = Color(0xFFE5E7EB))
        }

        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(11.dp))

            Column( //총 결제 금액, 인당 금액 박스
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(327f / 148f)
                    .paint(
                        painter = painterResource(R.drawable.bg_promise_waiting),
                        contentScale = ContentScale.FillBounds
                    )
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "총 결제 금액", fontWeight = Bold, fontSize = 18.sp, color = pBlack)
                Text(text = formatWon(totalAmount), fontWeight = Bold, fontSize = 30.sp, color = Color(0xFF0284C7))
                Text(text = perPersonText, fontSize = 14.sp, color = Color(0xFF4B5563))
            }
            Spacer(modifier = Modifier.height(24.dp))

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(text = "각자 결제한 금액", fontWeight = Bold, fontSize = 18.sp, color = pBlack)

                val sortedExpenses = expenses.sortedByDescending {
                    it.userId == uiState.myUserId
                }

                sortedExpenses.forEach { expense ->
                    val diff = expense.balanceAmount

                    val roleText = when {
                        diff > 0L -> "받을 사람"
                        diff < 0L -> "보낼 사람"
                        else -> ""
                    }

                    val isEditable = expense.userId == uiState.myUserId

                    val displayName = if (isEditable) {
                        "${expense.nickname} (나)"
                    } else expense.nickname

                    PayAmountCard(
                        name = displayName,
                        roleText = roleText,
                        value = if (isEditable) {
                            uiState.myAmountText
                        } else {
                            expense.paidAmount.toString()
                        },
                        enabled = isEditable,
                        onValueChange = { newText ->
                            vm.changeAmount(newText)
                        }
                    )
                }
            }

            //하나라도 입력되면 결과 보여주기
            val hasAnyAmount = expenses.any { it.paidAmount > 0L } || uiState.myAmountText.isNotBlank()

            if (hasAnyAmount) {
                Spacer(modifier = Modifier.height(24.dp))

                Text(text = "정산 결과", fontWeight = Bold, fontSize = 18.sp, color = pBlack)
                Spacer(modifier = Modifier.height(16.dp))

                //각 사람별로 (보낼/받을) + 차액을 계산해서 ResultCard로 표시
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                    expenses.forEach { expense ->
                        val diff = expense.balanceAmount
                        val isReceiver = diff > 0L
                        val roleText = if (isReceiver) "받을 사람" else "보낼 사람"

                        val isMe = expense.userId == uiState.myUserId

                        val displayName = if (isMe) {
                            "${expense.nickname} (나)"
                        } else {
                            expense.nickname
                        }

                        ResultCard(
                            name = displayName,
                            value = formatWon(abs(diff)),
                            isReceiver = isReceiver, //색/문구 바꾸기 위해 추가 파라미터
                            roleText = roleText //"받을 사람/보낼 사람" 텍스트
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(text = "상세 정산 내역", fontWeight = Bold, fontSize = 14.sp, color = pBlack)
                Spacer(modifier = Modifier.height(12.dp))

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    transfers.forEach { transfer ->

                        val fromName =
                            if (transfer.fromUserId == uiState.myUserId) {
                                "${transfer.fromNickname} (나)"
                            } else {
                                transfer.fromNickname
                            }

                        val toName =
                            if (transfer.toUserId == uiState.myUserId) {
                                "${transfer.toNickname} (나)"
                            } else {
                                transfer.toNickname
                            }

                        RemittanceCard(
                            rename = fromName,
                            giname = toName,
                            value = formatWon(transfer.amount)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Image(
                    painter = painterResource(R.drawable.btn_total_calculate),
                    contentDescription = "총 정산 버튼",
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .scale(1.08f)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            vm.settleExpenses(promiseId) {
                                val popped = navController.popBackStack()
                                if (!popped) navController.navigateUp()
                            }
                        }
                )
            }
        }
    }
}

//결제한 금액 카드
@Composable
fun PayAmountCard(
    name: String,
    roleText: String,
    value: String,
    enabled: Boolean,
    onValueChange: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(327f / 72f)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF9FAFB))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(R.drawable.ic_promise_profile),
            contentDescription = null,
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = name,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = pBlack
            )

            if(roleText.isNotBlank()) {
                Text(
                    text = roleText,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = Color(0xFF0284C7)
                )
            }
        }
        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .size(96.dp, 39.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
                .padding(13.dp, 9.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                enabled = enabled,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(
                    fontSize = 14.sp,
                    color = Color(0xFF111827),
                    textAlign = TextAlign.End
                ),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done, //완료버튼 보이게
                    keyboardType = KeyboardType.Number
                ),
                decorationBox = { inner ->
                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (value.isBlank()) {
                            Text(
                                text = "0",
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                                color = Color(0xFF9CA3AF),
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        inner()
                    }
                }
            )
        }
        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "원",
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = Color(0xFF4B5563)
        )
    }
}

//정산 결과 카드
@Composable
fun ResultCard(
    name: String,
    value: String,
    isReceiver: Boolean, //true면 "받을 사람", false면 "보낼 사람"
    roleText: String
) {
    val bgColor = if (isReceiver) Color(0xFFEFF6FF) else Color(0xFFFFF7ED)
    val borderColor = if (isReceiver) Color(0xFFBFDBFE) else Color(0xFFFED7AA)
    val accentColor = if (isReceiver) Color(0xFF2563EB) else Color(0xFFEA580C)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .heightIn(min = 74.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .padding(17.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(R.drawable.ic_promise_profile),
            contentDescription = null,
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(text = name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = pBlack)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = roleText, fontWeight = FontWeight.Medium, fontSize = 12.sp, color = accentColor)
        }
        Spacer(modifier = Modifier.weight(1f))

        Text(text = if (isReceiver) "+" + value else value, fontWeight = Bold, fontSize = 18.sp, color = accentColor)
    }
}

//상세 정산 내역 카드
@Composable
fun RemittanceCard(
    rename: String,
    giname: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(12.dp))
            .padding(17.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(R.drawable.ic_promise_profile),
            contentDescription = null,
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = rename, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = pBlack)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "보내는 사람", fontWeight = FontWeight.Medium, fontSize = 12.sp, color = Color(0xFF6B7280))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Image(
            painter = painterResource(R.drawable.ic_send),
            contentDescription = null
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = giname, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = pBlack)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "받는 사람", fontWeight = FontWeight.Medium, fontSize = 12.sp, color = Color(0xFF6B7280))
        }
        Spacer(modifier = Modifier.weight(1f))

        Text(text = value, fontWeight = Bold, fontSize = 18.sp, color = Color(0xFF0284C7))
    }
}