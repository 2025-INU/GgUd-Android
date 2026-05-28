package com.capstone.ggud.ui.my

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.capstone.ggud.R
import com.capstone.ggud.data.AuthRepository
import com.capstone.ggud.data.TokenStore
import com.capstone.ggud.data.UserRepository
import com.capstone.ggud.network.ApiClient
import com.capstone.ggud.ui.components.TopBar

@Composable
fun ProfileEditScreen(navController: NavHostController) {

    val context = LocalContext.current

    val userApi = remember { ApiClient.getUserApi(context) }
    val authApi = remember { ApiClient.getAuthApi(context) }
    val tokenStore = remember { TokenStore(context.applicationContext) }

    val userRepository = remember { UserRepository(userApi) }
    val authRepository = remember { AuthRepository(authApi, tokenStore) }

    val vm: MyViewModel = viewModel(
        factory = MyViewModelFactory(userRepository, authRepository)
    )

    val uiState by vm.uiState.collectAsState()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            vm.onProfileImageSelected(uri)
        }
    }

    LaunchedEffect(Unit) {
        vm.getMyPage()
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            vm.clearErrorMessage()
        }
    }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            Toast.makeText(context, "프로필이 수정되었습니다", Toast.LENGTH_SHORT).show()
            vm.clearSaveSuccess()
            navController.popBackStack()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TopBar(navController, "프로필 수정")
        Spacer(modifier = Modifier.height(11.dp))

        Column(
            modifier = Modifier
                .width(327.dp)
                .wrapContentHeight()
                .heightIn(min = 260.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(24.dp)
        ){
            Text(
                text = "프로필 사진",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF111827),
                modifier = Modifier.align(Alignment.Start)
            )

            Box(
                modifier = Modifier
                    .padding(vertical = 24.dp)
                    .size(96.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                when {
                    uiState.selectedImageUri != null -> {
                        AsyncImage(
                            model = uiState.selectedImageUri,
                            contentDescription = null,
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop,
                            placeholder = painterResource(R.drawable.profile),
                            error = painterResource(R.drawable.profile)
                        )
                    }

                    uiState.profileImageUrlInput.isNotBlank() -> {
                        AsyncImage(
                            model = uiState.profileImageUrlInput,
                            contentDescription = null,
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop,
                            placeholder = painterResource(R.drawable.profile),
                            error = painterResource(R.drawable.profile)
                        )
                    }

                    else -> {
                        Image(
                            painter = painterResource(R.drawable.profile),
                            contentDescription = null,
                            modifier = Modifier.size(96.dp)
                        )
                    }
                }

                Image( //프로필수정 버튼
                    painter = painterResource(R.drawable.btn_edit),
                    contentDescription = "프로필사진 수정",
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(32.dp)
                        .offset(x = 8.dp, y = 6.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { imagePickerLauncher.launch("image/*") }
                )
            }

            Text(
                text = "프로필 사진을 변경하려면 카메라 아이콘을\n터치하세요",
                fontSize = 14.sp,
                color = Color(0xFF6B7280),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier
                .width(327.dp)
                .wrapContentHeight()
                .heightIn(min = 175.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(24.dp)
        ) {
            Text(
                text = "기본 정보",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF111827),
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "이름",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF374151),
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box( //이름
                modifier = Modifier
                    .width(279.dp)
                    .wrapContentHeight()
                    .heightIn(min = 47.dp)
                    .border(
                        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(17.dp, 13.dp)
            ) {
                BasicTextField(
                    value = uiState.nicknameInput,
                    onValueChange = { vm.onNicknameChanged(it) },
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = 14.sp,
                        color = Color(0xFF111827)
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { innerTextField ->
                        if (uiState.nicknameInput.isBlank()) {
                            Text(
                                text = "이름을 입력하세요",
                                fontSize = 14.sp,
                                color = Color(0xFF9CA3AF)
                            )
                        }
                        innerTextField()
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Image( //저장 버튼
            painter = painterResource(R.drawable.btn_profile_save),
            contentDescription = "프로필 수정 저장 버튼",
            modifier = Modifier
                .width(327.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    vm.updateMyProfile(context)
                }
        )

        Spacer(modifier = Modifier.height(12.dp))

        Image( //취소 버튼
            painter = painterResource(R.drawable.btn_profile_cancel),
            contentDescription = "프로필 수정 취소 버튼",
            modifier = Modifier
                .width(327.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    navController.popBackStack()
                }
        )
    }
}