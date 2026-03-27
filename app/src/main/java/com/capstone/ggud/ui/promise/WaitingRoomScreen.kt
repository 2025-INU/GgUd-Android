package com.capstone.ggud.ui.promise

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.capstone.ggud.R
import com.capstone.ggud.ui.components.TopBar
import com.capstone.ggud.ui.components.formatIsoToDotTime
import com.capstone.ggud.ui.theme.pBlack
import com.kakao.sdk.share.ShareClient
import com.kakao.sdk.share.WebSharerClient
import com.kakao.sdk.share.model.SharingResult
import com.kakao.sdk.template.model.Link
import com.kakao.sdk.template.model.TextTemplate
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun WaitingRoomScreen(
    navController: NavHostController,
    promiseId: Long,
    vm: WaitingRoomViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by vm.uiState.collectAsState()

    val locationSubmitted by (
            navController.currentBackStackEntry
                ?.savedStateHandle
                ?.getStateFlow("location_submitted", false)
                ?: MutableStateFlow(false)
            ).collectAsState()

    LaunchedEffect(promiseId) {
        vm.fetchSummary(promiseId)
        vm.fetchParticipants(promiseId)
    }

    LaunchedEffect(uiState.inviteLink) {
        val link = uiState.inviteLink ?: return@LaunchedEffect
        shareWithKakaoTalk(context, link)
        vm.clearInviteLink()
    }

    LaunchedEffect(locationSubmitted) {
        if (locationSubmitted) {
            vm.fetchParticipants(promiseId)
            navController.currentBackStackEntry
                ?.savedStateHandle
                ?.set("location_submitted", false)
        }
    }

    LaunchedEffect(uiState.midpointStarted) {
        if (uiState.midpointStarted) {
            navController.navigate("middle_point/$promiseId")
            vm.clearMidpointStarted()
        }
    }

    val allSubmitted = uiState.participants.isNotEmpty() && uiState.participants.all { it.locationSubmitted }

    val titleText = uiState.summary?.title ?: "약속 이름"
    val dateText = uiState.summary?.promiseDateTime?.let { formatIsoToDotTime(it) } ?: "-"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        TopBar(navController, "약속 대기방")

        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(11.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth().aspectRatio(327f / 165f)
                    .paint(
                        painter = painterResource(R.drawable.bg_promise_waiting),
                        contentScale = ContentScale.Crop
                    )
                    .padding(24.dp)
            ) {
                Row {
                    Image(
                        painter = painterResource(R.drawable.ic_promise),
                        contentDescription = null,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))

                    Column { //약속 요약
                        Text(
                            text = titleText,
                            fontWeight = Bold,
                            fontSize = 18.sp,
                            color = pBlack
                        )
                        Text(
                            text = dateText,
                            fontSize = 14.sp,
                            color = Color(0xFF4B5563)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "약속이 생성되었습니다!",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = Color(0xFF16A34A),
                        modifier = Modifier.align(Alignment.CenterStart)
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "친구 초대하기",
                fontWeight = Bold,
                fontSize = 18.sp,
                color = pBlack
            )
            Spacer(modifier = Modifier.height(16.dp))

            Image(
                painter = painterResource(R.drawable.btn_kakao_link),
                contentDescription = "링크공유 버튼",
                modifier = Modifier
                    .scale(1.08f)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        vm.fetchInviteLink(promiseId)
                    }
            )

            Text(
                text = "링크를 통해 친구들이 약속에 참여할 수 있어요",
                fontSize = 14.sp,
                color = Color(0xFF6B7280),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(32.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "참여한 친구",
                    fontWeight = Bold,
                    fontSize = 18.sp,
                    color = pBlack
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "${uiState.participants.size}명",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color(0xFF0284C7)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            //참여자 목록
            uiState.participants.forEachIndexed { idx, p ->
                val nameText = buildString {
                    if (p.host) append("(호스트) ")
                    append(p.nickname)
                    if (p.id == p.userId) append(" (나)")
                }

                PeopleCard(
                    name = nameText,
                    enterLocation = p.locationSubmitted,
                    onClickEnterLocation = {
                        navController.navigate("promise_join/$promiseId")
                    }
                )

                if (idx != uiState.participants.lastIndex) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
            Spacer(modifier = Modifier.height(20.dp))

            if (allSubmitted) {
                Column (
                    modifier = Modifier
                        .fillMaxWidth().aspectRatio(327f / 112f)
                        .background(Color(0xFFF0FDF4), RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFFBBF7D0), RoundedCornerShape(12.dp))
                        .padding(17.dp)
                ) {
                    Text(
                        text = "모든 참여자가 위치를 입력했습니다!",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = Color(0xFF166534)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF16A34A))
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                vm.startMidpointSelection(promiseId)
                            }
                    ) {
                        Text(
                            text = "중간지점 확인하기",
                            fontWeight = Bold,
                            fontSize = 14.sp,
                            color = Color.White,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun PeopleCard(
    name: String,
    enterLocation: Boolean,
    onClickEnterLocation: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 80.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF9FAFB))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onClickEnterLocation()
            }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(R.drawable.ic_promise_profile),
            contentDescription = null,
            modifier = Modifier.size(48.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = name,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = pBlack
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (enterLocation) "위치 입력 완료" else "위치 입력하기",
                fontSize = 14.sp,
                color = Color(0xFF4B5563)
            )
        }

        if (enterLocation) {
            Image(
                painter = painterResource(R.drawable.ic_check),
                contentDescription = null,
                modifier = Modifier.size(36.dp)
            )
        }
    }
}

private fun shareWithKakaoTalk(context: Context, inviteUrl: String) {
    //카카오 메시지 템플릿
    val template = TextTemplate(
        text = "약속에 초대되었어요!\n아래 링크로 참여해 주세요",
        link = Link(
            webUrl = inviteUrl,
            mobileWebUrl = inviteUrl
        )
    )

    if (ShareClient.instance.isKakaoTalkSharingAvailable(context)) {
        ShareClient.instance.shareDefault(context, template) {
                sharingResult: SharingResult?, error: Throwable? ->
            if (error != null) {
                openWebShare(context, template)
            } else if (sharingResult != null) {
                context.startActivity(sharingResult.intent)
            }
        }
    } else {
        openWebShare(context, template)
    }
}

private fun openWebShare(context: Context, template: TextTemplate) {
    val shareUrl = WebSharerClient.instance.makeDefaultUrl(template)
    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(shareUrl.toString())))
}