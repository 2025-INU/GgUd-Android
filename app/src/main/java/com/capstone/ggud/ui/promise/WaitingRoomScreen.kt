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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.capstone.ggud.R
import com.capstone.ggud.ui.components.formatIsoToDotTime
import com.capstone.ggud.ui.theme.pBlack
import com.kakao.sdk.share.ShareClient
import com.kakao.sdk.share.WebSharerClient
import com.kakao.sdk.share.model.SharingResult
import com.kakao.sdk.template.model.Link
import com.kakao.sdk.template.model.TextTemplate
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow

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
        vm.fetchMe()
        vm.fetchInviteCode(promiseId)
    }

    LaunchedEffect(promiseId) {
        while (true) {
            vm.fetchParticipants(promiseId)
            delay(3000)
        }
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

    val myParticipant = uiState.participants.firstOrNull {
        it.userId == uiState.myUserId
    }

    val isHost = myParticipant?.host == true

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        WaitingTopBar(
            navController = navController,
            onHomeClick = {
                navController.navigate("home/$promiseId") {
                    popUpTo("home") {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
            }
        )

        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(11.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(165.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.bg_promise_waiting),
                    contentDescription = null,
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.FillBounds
                )

                Column(
                    modifier = Modifier
                        .matchParentSize()
                        .padding(24.dp)
                ) {
                    Row {
                        Image(
                            painter = painterResource(R.drawable.ic_promise),
                            contentDescription = null,
                            modifier = Modifier.size(48.dp)
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
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
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .heightIn(min = 53.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = "약속이 생성되었습니다!",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = Color(0xFF16A34A)
                        )
                    }
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
                painter = painterResource(R.drawable.btn_kakao_code),
                contentDescription = "코드공유 버튼",
                modifier = Modifier
                    .scale(1.08f)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        uiState.inviteCode?.let { code ->
                            shareWithKakaoTalk(context, code)
                        }
                    }
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFF3B82F6), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ){
                Text(
                    text = "초대 코드: ${uiState.inviteCode}",
                    fontWeight = Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF3B82F6)
                )
            }

            Text(
                text = "코드를 통해 친구들이 약속에 참여할 수 있어요",
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

            val sortedParticipants = uiState.participants.sortedByDescending {
                it.userId == uiState.myUserId
            }

            //참여자 목록
            sortedParticipants.forEachIndexed { idx, p ->

                val isMe = p.userId == uiState.myUserId

                val nameText = buildString {
                    if (p.host) append("(호스트) ")
                    append(p.nickname)
                    if (isMe) {
                        append(" (나)")
                    }
                }

                PeopleCard(
                    name = nameText,
                    enterLocation = p.locationSubmitted,
                    isMe = isMe,
                    profileImageUrl = p.profileImageUrl,
                    onClickEnterLocation = {
                        navController.navigate("promise_join/$promiseId")
                    }
                )

                if (idx != sortedParticipants.lastIndex) {
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
                                if (isHost) {
                                    vm.startMidpointSelection(promiseId)
                                } else {
                                    navController.navigate("middle_point/$promiseId")
                                }
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

                Text(
                    text = "최종 확정은 호스트만 가능해요.",
                    fontSize = 14.sp,
                    color = Color(0xFF6B7280),
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .align(Alignment.CenterHorizontally)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun WaitingTopBar(
    navController: NavHostController,
    onHomeClick: () -> Unit
) {
    Column {
        Row(modifier = Modifier
            .fillMaxWidth()
            .size(375.dp, 69.dp)
            .background(Color.White)
            .padding(24.dp, 17.dp)
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
            Text(
                text = "약속 대기방",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF111827)
            )
            Spacer(modifier = Modifier.weight(1f))
            Image( //홈 버튼
                painter = painterResource(R.drawable.btn_home),
                contentDescription = "메인으로",
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onHomeClick() }
            )
        }
        Divider(thickness = 1.dp, color = Color(0xFFE5E7EB))
    }
}

@Composable
fun PeopleCard(
    name: String,
    enterLocation: Boolean,
    isMe: Boolean,
    profileImageUrl: String?,
    onClickEnterLocation: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 80.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF9FAFB))
            .clickable(
                enabled = isMe,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onClickEnterLocation()
            }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (profileImageUrl.isNullOrBlank()) {
            Image(
                painter = painterResource(R.drawable.ic_promise_profile),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .clip(CircleShape)
                    .size(48.dp)
            )
        } else {
            AsyncImage(
                model = profileImageUrl,
                contentDescription = null,
                placeholder = painterResource(R.drawable.ic_promise_profile),
                error = painterResource(R.drawable.ic_promise_profile),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .clip(CircleShape)
                    .size(48.dp)
            )
        }

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
                text = when {
                    enterLocation -> "위치 입력 완료"
                    isMe -> "클릭해서 위치 입력하기"
                    else -> "위치 입력 대기중"
                },
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

private fun shareWithKakaoTalk(context: Context, inviteCode: String) {
    //카카오 메시지 템플릿
    val template = TextTemplate(
        text = "약속에 초대되었어요!\n초대 코드: $inviteCode\n앱에서 초대 코드를 입력해 참여해 주세요.",
        link = Link(
            webUrl = null,
            mobileWebUrl = null
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