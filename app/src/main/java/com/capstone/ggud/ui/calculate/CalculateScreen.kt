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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
import androidx.navigation.NavHostController
import com.capstone.ggud.R
import com.capstone.ggud.ui.theme.pBlack
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs
import kotlin.math.min

private data class Payer(
    val name: String,
    val amountText: String //숫자 문자열(빈값이면 0으로 취급)
)

private data class Transfer(
    val from: String,
    val to: String,
    val amount: Long
)

private data class SettlementResult(
    val total: Long,
    val shares: List<Long>, //각 사람의 정산 기준 금액
    val diffs: List<Long>, //paid - share ( +면 받을, -면 보낼 )
    val transfers: List<Transfer>
)

private fun formatWon(value: Long): String {
    val nf = NumberFormat.getNumberInstance(Locale.KOREA)
    return nf.format(value) + "원"
}

//총액을 n명에게 나눌 때
private fun computeShares(total: Long, n: Int): List<Long> {
    if (n <= 0) return emptyList()
    val base = total / n
    val rem = (total % n).toInt()
    return List(n) { idx -> base + if (idx < rem) 1L else 0L }
}

//정산 결과 계산
private fun buildSettlement(payers: List<Payer>): SettlementResult {
    val paidList = payers.map { it.amountText.toLongOrNull() ?: 0L } //각 사람이 입력한 금액 문자열로
    val total = paidList.sum() //총 결제 금액

    val shares = computeShares(total, payers.size) //각자의 정산 기준 금액
    val diffs = payers.indices.map { i -> paidList[i] - shares[i] } //각 사람의 차액 계산 (+면 받을 금액, -면 보낼 금액)

    //받을 사람 목록
    val creditors = payers.indices
        .map { i -> payers[i].name to diffs[i] }
        .filter { it.second > 0L }
        .map { it.first to it.second } //(name, amountToReceive)

    //보낼 사람 목록
    val debtors = payers.indices
        .map { i -> payers[i].name to diffs[i] }
        .filter { it.second < 0L }
        .map { it.first to (-it.second) } //(name, amountToPay)

    //그리디 매칭 (누가 누구에게 얼마)
    val transfers = mutableListOf<Transfer>()
    var ci = 0
    var di = 0

    val c = creditors.toMutableList()
    val d = debtors.toMutableList()

    while (di < d.size && ci < c.size) {
        val (debtorName, owe) = d[di]
        val (creditorName, claim) = c[ci]

        val pay = min(owe, claim)
        if (pay > 0L) {
            transfers.add(Transfer(from = debtorName, to = creditorName, amount = pay))
        }

        val newOwe = owe - pay
        val newClaim = claim - pay

        d[di] = debtorName to newOwe
        c[ci] = creditorName to newClaim

        if (d[di].second == 0L) di++
        if (c[ci].second == 0L) ci++
    }

    return SettlementResult( //최종 결과 반환
        total = total, //총액
        shares = shares, //각자 기준 금액
        diffs = diffs, //정산 차액
        transfers = transfers //상세 송금 내역
    )
}

@Composable
fun CalculateScreen(
    navController: NavHostController,
    title: String
) {
    //사람 목록
    var payers by remember {
        mutableStateOf(
            listOf(
                Payer("이은우", ""),
                Payer("윤은석", ""),
                Payer("안혜림", ""),
                Payer("정철웅", "")
            )
        )
    }

    //정산 결과
    val settlement by remember(payers) {
        derivedStateOf { buildSettlement(payers) }
    }

    //총액
    val totalAmount = settlement.total

    //1인당 표시
    val perPersonText by remember(settlement) {
        derivedStateOf {
            if (settlement.shares.isEmpty()) {
                "1인당 0원"
            } else {
                val minShare = settlement.shares.minOrNull() ?: 0L
                val maxShare = settlement.shares.maxOrNull() ?: 0L
                if (minShare == maxShare) "1인당 ${formatWon(minShare)}"
                else "1인당 ${formatWon(minShare)} ~ ${formatWon(maxShare)}"
            }
        }
    }

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
                Image( //뒤로가기 버튼 (기능X)
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
                        text = title,
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

                payers.forEachIndexed { index, person ->
                    val diff = settlement.diffs.getOrNull(index) ?: 0L

                    val roleText = when {
                        diff > 0L -> "받을 사람"
                        diff < 0L -> "보낼 사람"
                        else -> ""
                    }

                    PayAmountCard(
                        name = person.name,
                        roleText = roleText,
                        value = person.amountText,
                        onValueChange = { newText ->
                            val digitsOnly = newText.filter { it.isDigit() }.take(12)
                            payers = payers.toMutableList().also { list ->
                                list[index] = list[index].copy(amountText = digitsOnly)
                            }
                        }
                    )
                }
            }

            //하나라도 입력되면 결과 보여주기
            val hasAnyAmount by remember(payers) {
                derivedStateOf { payers.any { (it.amountText.toLongOrNull() ?: 0L) > 0L } }
            }

            if (hasAnyAmount) {
                Spacer(modifier = Modifier.height(24.dp))

                Text(text = "정산 결과", fontWeight = Bold, fontSize = 18.sp, color = pBlack)
                Spacer(modifier = Modifier.height(16.dp))

                //각 사람별로 (보낼/받을) + 차액을 계산해서 ResultCard로 표시
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                    payers.forEachIndexed { index, person ->
                        val diff = settlement.diffs.getOrNull(index) ?: 0L

                        val isReceiver = diff > 0L
                        val roleText = if (isReceiver) "받을 사람" else "보낼 사람"

                        //화면에 찍을 값은 항상 양수로 보이게 (받는 사람도 보낼 사람도 '차액'만)
                        val displayAmount = formatWon(abs(diff))

                        ResultCard(
                            name = person.name,
                            value = displayAmount,
                            isReceiver = isReceiver, //색/문구 바꾸기 위해 추가 파라미터
                            roleText = roleText //"받을 사람/보낼 사람" 텍스트
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(text = "상세 정산 내역", fontWeight = Bold, fontSize = 14.sp, color = pBlack)
                Spacer(modifier = Modifier.height(12.dp))

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (settlement.transfers.isEmpty()) {
                        //필요 시 문구 노출
                        //"정산할 내역이 없습니다."
                    } else {
                        settlement.transfers.forEach { t ->
                            RemittanceCard(
                                rename = t.from,
                                giname = t.to,
                                value = formatWon(t.amount)
                            )
                        }
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
                            // TODO: 정산하기
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
            .height(74.dp)
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