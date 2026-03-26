package com.capstone.ggud.ui.promise

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.platform.LocalContext
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
import com.capstone.ggud.ui.components.formatIsoToDotTime
import com.capstone.ggud.ui.theme.pBlack
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Build
import androidx.compose.runtime.rememberCoroutineScope
import androidx.core.content.ContextCompat
import com.capstone.ggud.data.KakaoLocalRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

data class PlaceSearchItem(
    val name: String,
    val address: String,
    val roadAddress: String,
    val longitude: String,
    val latitude: String
)

@Composable
fun PromiseJoinScreen(
    navController: NavHostController,
    promiseId: Long
) {

    //위치 입력값
    var location by remember { mutableStateOf("") }
    var showLocation by remember { mutableStateOf(false) }

    var selectedLatitude by remember { mutableStateOf<Double?>(null) }
    var selectedLongitude by remember { mutableStateOf<Double?>(null) }

    var isLoadingLocation by remember { mutableStateOf(false) }

    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<PlaceSearchItem>>(emptyList()) }
    var searchJob by remember { mutableStateOf<Job?>(null) }

    val context = LocalContext.current
    val repository = remember {
        PromiseRepository(ApiClient.getPromiseApi(context))
    }
    val vm: PromiseJoinViewModel = viewModel(
        factory = PromiseJoinViewModelFactory(repository)
    )
    val uiState by vm.uiState.collectAsState()

    val kakaoRepository = remember { KakaoLocalRepository(context) }
    val coroutineScope = rememberCoroutineScope()

    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    var currentAddress by remember { mutableStateOf("") }

    val canJoin = selectedLatitude != null && selectedLongitude != null && !uiState.isSubmitting

    LaunchedEffect(promiseId) {
        vm.loadPromiseSummary(promiseId)
    }

    LaunchedEffect(uiState.submitSuccess) {
        if (uiState.submitSuccess) {
            navController.previousBackStackEntry
                ?.savedStateHandle
                ?.set("location_submitted", true)

            navController.popBackStack()
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        @SuppressLint("MissingPermission")
        if (fineGranted || coarseGranted) {
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                CancellationTokenSource().token
            ).addOnSuccessListener { loc ->
                if (loc == null) {
                    Toast.makeText(context, "현재 위치를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val geocoder = Geocoder(context, Locale.KOREA)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    geocoder.getFromLocation(loc.latitude, loc.longitude, 1) { addresses ->
                        val address = addresses.firstOrNull()?.getAddressLine(0).orEmpty()
                        if (address.isNotBlank()) {
                            location = address
                            currentAddress = address
                            selectedLatitude = loc.latitude
                            selectedLongitude = loc.longitude
                            searchQuery = address
                            searchResults = emptyList()
                            showLocation = true
                        } else {
                            Toast.makeText(context, "주소 변환에 실패했습니다.", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(loc.latitude, loc.longitude, 1)
                    val address = addresses?.firstOrNull()?.getAddressLine(0).orEmpty()

                    if (address.isNotBlank()) {
                        location = address
                        currentAddress = address
                        selectedLatitude = loc.latitude
                        selectedLongitude = loc.longitude
                        searchQuery = address
                        searchResults = emptyList()
                        showLocation = true
                    } else {
                        Toast.makeText(context, "주소 변환에 실패했습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            }.addOnFailureListener {
                Toast.makeText(context, "위치 정보를 가져오지 못했습니다.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    fun searchPlaces(query: String) {
        searchJob?.cancel()

        if (query.isBlank()) {
            searchResults = emptyList()
            return
        }

        searchJob = coroutineScope.launch {
            delay(300) // 타이핑 debounce

            val result = kakaoRepository.searchPlaces(query)
            searchResults = result.map {
                PlaceSearchItem(
                    name = it.place_name.orEmpty(),
                    address = it.address_name.orEmpty(),
                    roadAddress = it.road_address_name.orEmpty(),
                    longitude = it.x.orEmpty(),
                    latitude = it.y.orEmpty()
                )
            }
        }
    }

    val titleText = uiState.summary?.title ?: "약속 이름"
    val dateText = uiState.summary?.promiseDateTime?.let { formatIsoToDotTime(it) } ?: "-"
    val hostName = uiState.summary?.hostNickname ?: "주최자"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        TopBar(navController, "약속 참여하기")

        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(11.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(327f / 185f)
                    .paint(
                        painter = painterResource(R.drawable.bg_promise_waiting),
                        contentScale = ContentScale.FillBounds
                    )
                    .padding(24.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
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
                        Text(
                            text = "주최자: ${hostName}",
                            fontSize = 12.sp,
                            color = Color(0xFF6B7280)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_important),
                        contentDescription = null,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "출발 위치를 입력해주세요",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = Color(0xFF0284C7)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "출발 위치",
                fontWeight = Bold,
                fontSize = 18.sp,
                color = pBlack
            )
            Spacer(modifier = Modifier.height(16.dp))

            Image(
                painter = painterResource(R.drawable.btn_location),
                contentDescription = "위치 수집 버튼",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(327f / 62f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        val finePermission = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED

                        val coarsePermission = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED

                        if (finePermission || coarsePermission) {
                            isLoadingLocation = true
                            fusedLocationClient.getCurrentLocation(
                                Priority.PRIORITY_HIGH_ACCURACY,
                                CancellationTokenSource().token
                            ).addOnSuccessListener { loc ->
                                if (loc == null) {
                                    Toast.makeText(context, "현재 위치를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show()
                                    return@addOnSuccessListener
                                }

                                val geocoder = Geocoder(context, Locale.KOREA)

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    geocoder.getFromLocation(loc.latitude, loc.longitude, 1) { addresses ->
                                        val address = addresses.firstOrNull()?.getAddressLine(0).orEmpty()
                                        if (address.isNotBlank()) {
                                            location = address
                                            currentAddress = address
                                            selectedLatitude = loc.latitude
                                            selectedLongitude = loc.longitude
                                            searchQuery = address
                                            searchResults = emptyList()
                                            showLocation = true
                                            isLoadingLocation = false
                                        } else {
                                            isLoadingLocation = false
                                            Toast.makeText(context, "주소 변환에 실패했습니다.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } else {
                                    @Suppress("DEPRECATION")
                                    val addresses = geocoder.getFromLocation(loc.latitude, loc.longitude, 1)
                                    val address = addresses?.firstOrNull()?.getAddressLine(0).orEmpty()

                                    if (address.isNotBlank()) {
                                        location = address
                                        currentAddress = address
                                        selectedLatitude = loc.latitude
                                        selectedLongitude = loc.longitude
                                        searchQuery = address
                                        searchResults = emptyList()
                                        showLocation = true
                                        isLoadingLocation = false
                                    } else {
                                        isLoadingLocation = false
                                        Toast.makeText(context, "주소 변환에 실패했습니다.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }.addOnFailureListener {
                                isLoadingLocation = false
                                Toast.makeText(context, "위치 정보를 가져오지 못했습니다.", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    }
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(327f / 47f)
                    .border(
                        width = 1.dp,
                        color = Color(0xFFE5E7EB),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 17.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_search),
                    contentDescription = null,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(18.dp))
                BasicTextField(
                    value = searchQuery,
                    onValueChange = {
                        if (it.length <= 50) {
                            searchQuery = it
                            searchPlaces(it)
                        }
                    },
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
                        if (searchQuery.isBlank()) {
                            Text(
                                text = "주소를 검색하세요",
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                                color = Color(0xFF6B7280)
                            )
                        }
                        inner()
                    }
                )
            }

            if (searchResults.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = Color(0xFFE5E7EB),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .background(Color.White, RoundedCornerShape(12.dp))
                ) {
                    searchResults.forEachIndexed { index, item ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    val selectedAddress =
                                        if (item.roadAddress.isNotBlank()) item.roadAddress else item.address

                                    location = selectedAddress
                                    currentAddress = selectedAddress
                                    selectedLatitude = item.latitude.toDoubleOrNull()
                                    selectedLongitude = item.longitude.toDoubleOrNull()
                                    searchQuery = selectedAddress
                                    showLocation = true
                                    searchResults = emptyList()
                                }
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                        ) {
                            Text(
                                text = if (item.name.isNotBlank()) item.name else "검색 결과",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF111827)
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = if (item.roadAddress.isNotBlank()) item.roadAddress else item.address,
                                fontSize = 13.sp,
                                color = Color(0xFF6B7280)
                            )
                        }

                        if (index != searchResults.lastIndex) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(Color(0xFFF3F4F6))
                            )
                        }
                    }
                }
            }

            if (isLoadingLocation || showLocation) {
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(327f / 54f)
                        .background(Color(0xFFF0FDF4), RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFFBBF7D0), RoundedCornerShape(12.dp))
                        .padding(17.dp)
                ) {
                    Text(
                        text = if (isLoadingLocation) "현재 위치 불러오는 중..."
                                else currentAddress.ifBlank { location },
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = Color(0xFF166534)
                    )
                }
            }

            Spacer(modifier = Modifier.height(64.dp))

            Image(
                painter = painterResource(if (canJoin) R.drawable.btn_join else R.drawable.btn_join_disabled),
                contentDescription = "약속 참여",
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .scale(1.08f)
                    .clickable(
                        enabled = canJoin,
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        val lat = selectedLatitude
                        val lng = selectedLongitude

                        if (lat == null || lng == null) {
                            Toast.makeText(context, "출발 위치를 먼저 선택해주세요.", Toast.LENGTH_SHORT).show()
                            return@clickable
                        }

                        vm.updateDeparture(
                            promiseId = promiseId,
                            latitude = lat,
                            longitude = lng,
                            address = currentAddress.ifBlank { location }
                        )
                    }
            )
            Text(
                text = "참여하면 약속 대기방으로 이동합니다",
                fontSize = 14.sp,
                color = Color(0xFF6B7280),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .offset(y=-(8).dp)
            )
        }
    }
}