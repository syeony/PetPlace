package com.example.petplace.presentation.feature.join

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.canhub.cropper.*
import com.example.petplace.R
import com.example.petplace.presentation.common.theme.AppTypography
import com.example.petplace.presentation.common.theme.BackgroundSoft
import com.example.petplace.presentation.common.theme.PrimaryColor
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.LocationServices
import com.kakao.vectormap.*
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.LabelOptions

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun JoinScreen(navController: NavController, viewModel: JoinViewModel) {
    val context = LocalContext.current

    // ViewModel + 동네 이름 상태
    val viewModel: JoinViewModel = hiltViewModel()
    val rawRegionName by viewModel.regionName.collectAsState(initial = null)
    val regionNameDisplay = rawRegionName ?: "불러오는 중…"

    // 위치 권한
    val locationPerm = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )
    LaunchedEffect(Unit) { locationPerm.launchMultiplePermissionRequest() }

    // 프로필 & 폼 상태
    var croppedUri by remember { mutableStateOf<Uri?>(null) }
    var agreeAll by remember { mutableStateOf(false) }
    var agreeService by remember { mutableStateOf(false) }
    var agreePrivacy by remember { mutableStateOf(false) }
    var agreeMarketing by remember { mutableStateOf(false) }

    // 지도 다이얼로그 / 인증 완료된 동네
    var showMapDialog by remember { mutableStateOf(false) }
    var currentLat by remember { mutableStateOf(0.0) }
    var currentLng by remember { mutableStateOf(0.0) }
    var confirmedRegion by remember { mutableStateOf<String?>(null) }

    // 이미지 런처
    val cropLauncher = rememberLauncherForActivityResult(CropImageContract()) { res ->
        if (res.isSuccessful) croppedUri = res.uriContent
    }
    val pickLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val opts = CropImageContractOptions(it, CropImageOptions()).apply {
                setAspectRatio(1, 1)
                setCropShape(CropImageView.CropShape.RECTANGLE)
                setGuidelines(CropImageView.Guidelines.ON)
            }
            cropLauncher.launch(opts)
        }
    }

    Scaffold(
        bottomBar = {
            Button(
                onClick = { /* 회원가입 처리 */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
            ) {
                Text("회원가입", color = Color.White)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 프로필
            Box(modifier = Modifier.size(120.dp), contentAlignment = Alignment.Center) {
                Surface(
                    modifier = Modifier.size(120.dp),
                    shape = CircleShape,
                    color = Color(0xFFE0E0E0)
                ) {
                    if (croppedUri != null) {
                        AsyncImage(
                            model = croppedUri,
                            contentDescription = "프로필",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            painter = painterResource(R.drawable.ic_mypage),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().padding(30.dp),
                            tint = Color.Gray
                        )
                    }
                }
                Surface(
                    modifier = Modifier
                        .size(36.dp)
                        .align(Alignment.BottomEnd)
                        .offset(x = 8.dp, y = 8.dp)
                        .clickable { pickLauncher.launch("image/*") },
                    shape = CircleShape,
                    color = PrimaryColor,
                    shadowElevation = 4.dp
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_gallery),
                        contentDescription = null,
                        modifier = Modifier.padding(6.dp),
                        tint = Color.White
                    )
                }
            }

            // 아이디 + 중복확인
            Row(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = viewModel.userId.value,
                    onValueChange = { viewModel.onUserIdChange(it)},
                    label = { Text("아이디") },
                    modifier = Modifier.weight(2f),
                    colors = TextFieldDefaults.outlinedTextFieldColors(containerColor = BackgroundSoft)
                )
                Button(
                    modifier = Modifier.weight(1f).fillMaxHeight().padding(top = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    onClick = { /* 중복확인 */ },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                ) {
                    Text("중복확인", color = Color.White, style = AppTypography.labelMedium)
                }
            }

            // 비밀번호
            OutlinedTextField(
                value =  viewModel.password.value,
                onValueChange = { viewModel.onUserIdChange(it) },
                label = { Text("비밀번호") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(containerColor = BackgroundSoft)
            )
            OutlinedTextField(
                value = viewModel.password.value,
                onValueChange = { viewModel.onPasswordChange(it) },
                label = { Text("비밀번호 확인") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(containerColor = BackgroundSoft)
            )

            // 닉네임 + 중복확인
            Row(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value =  viewModel.nickname.value,
                    onValueChange = { viewModel.onNicknameChange(it)},
                    label = { Text("닉네임") },
                    modifier = Modifier.weight(2f),
                    colors = TextFieldDefaults.outlinedTextFieldColors(containerColor = BackgroundSoft)
                )
                Button(
                    modifier = Modifier.weight(1f).fillMaxHeight().padding(top = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    onClick = { /* 중복확인 */ },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                ) {
                    Text("중복확인", color = Color.White, style = AppTypography.labelMedium)
                }
            }

            // 현재 위치 인증 카드
            if (confirmedRegion == null) {
                LocationCard(
                    title = "현재 위치로 인증",
                    subTitle = "GPS를 통해 동네를 인증합니다",
                    buttonText = "현재 위치 인증하기",
                    onClick = {
                        if (locationPerm.allPermissionsGranted) {
                            getCurrentLocation(context) { lat, lng ->
                                currentLat = lat
                                currentLng = lng
                                viewModel.fetchRegionByCoord(lat, lng)
                                showMapDialog = true
                            }
                        } else {
                            Toast.makeText(context, "위치 권한이 필요합니다", Toast.LENGTH_SHORT).show()
                            locationPerm.launchMultiplePermissionRequest()
                        }
                    }
                )
            } else {
                LocationCard(
                    title = confirmedRegion!!,
                    subTitle = "",
                    buttonText = "현재 위치 인증 완료",
                    onClick = {}
                )
            }

            // 약관
            Column {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Checkbox(checked = agreeAll, onCheckedChange = {
                        agreeAll = it
                        agreeService = it
                        agreePrivacy = it
                        agreeMarketing = it
                    })
                    Text("전체 동의", modifier = Modifier.weight(1f))
                }
                Divider(color = Color.LightGray, thickness = 1.dp)
                AgreementRow(agreeService, { agreeService = it }, "[필수] 서비스 이용약관")
                AgreementRow(agreePrivacy, { agreePrivacy = it }, "[필수] 개인정보 처리방침")
                AgreementRow(agreeMarketing, { agreeMarketing = it }, "[선택] 마케팅 정보 수신")
            }
        }
    }

    // 지도 다이얼로그
    if (showMapDialog) {
        Dialog(onDismissRequest = { showMapDialog = false }) {
            Surface(
                modifier = Modifier.fillMaxWidth().fillMaxHeight(0.7f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(Modifier.fillMaxSize()) {
                    Text(
                        text = regionNameDisplay,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        style = AppTypography.titleMedium,
                        textAlign = TextAlign.Center
                    )
                    AndroidView(
                        factory = { ctx ->
                            MapView(ctx).apply {
                                start(
                                    object : MapLifeCycleCallback() {
                                        override fun onMapDestroy() {}
                                        override fun onMapError(e: Exception?) {}
                                        override fun onMapResumed() {}
                                    },
                                    object : KakaoMapReadyCallback() {
                                        override fun onMapReady(map: KakaoMap) {
                                            val pos = LatLng.from(currentLat, currentLng)
                                            map.labelManager?.layer?.addLabel(
                                                LabelOptions.from(pos).setStyles(R.drawable.location_on)
                                            )
                                            map.moveCamera(CameraUpdateFactory.newCenterPosition(pos, 15))
                                        }
                                    }
                                )
                            }
                        },
                        modifier = Modifier.weight(1f).fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
                    ) {
                        OutlinedButton(onClick = { showMapDialog = false }) { Text("취소") }
                        Button(
                            onClick = {
                                showMapDialog = false
                                rawRegionName?.let { confirmedRegion = it }
                            },
                            enabled = (rawRegionName != null)
                        ) { Text("확인") }
                    }
                }
            }
        }
    }
}

@SuppressLint("MissingPermission")
private fun getCurrentLocation(context: Context, onLocationReceived: (Double, Double) -> Unit) {
    LocationServices.getFusedLocationProviderClient(context)
        .lastLocation
        .addOnSuccessListener { it?.let { onLocationReceived(it.latitude, it.longitude) } }
}

@Composable
private fun AgreementRow(checked: Boolean, onCheckedChange: (Boolean) -> Unit, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Checkbox(checked, onCheckedChange)
        Text(title, modifier = Modifier.weight(1f))
        Text("보기", color = Color.Blue, modifier = Modifier.clickable { /* 상세보기 */ })
    }
}

@Composable
private fun LocationCard(title: String, subTitle: String, buttonText: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(painter = painterResource(R.drawable.ic_location_on), contentDescription = null, tint = PrimaryColor)
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(title, fontWeight = FontWeight.Medium)
                    if (subTitle.isNotEmpty()) {
                        Text(subTitle, fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Button(onClick = onClick) { Text(buttonText) }
        }
    }
}

//@Preview(showBackground = true, backgroundColor = 0xFFFEF9F0)
//@Composable
//private fun JoinPreview() {
//    JoinScreen(navController = rememberNavController(), viewModel = viewModel)
//}
