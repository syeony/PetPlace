package com.example.petplace.presentation.feature.join

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.example.petplace.R
import com.example.petplace.presentation.common.theme.BackgroundSoft
import com.example.petplace.presentation.common.theme.PrimaryColor
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.LocationServices
import com.kakao.vectormap.MapView
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import androidx.compose.ui.viewinterop.AndroidView

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun JoinScreen(navController: NavController) {
    val context = LocalContext.current

    // 위치 권한
    val locationPermissionState = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    LaunchedEffect(Unit) {
        locationPermissionState.launchMultiplePermissionRequest()
    }

    var userId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }
    var agreeAll by remember { mutableStateOf(false) }
    var agreeService by remember { mutableStateOf(false) }
    var agreePrivacy by remember { mutableStateOf(false) }
    var agreeMarketing by remember { mutableStateOf(false) }
    var croppedImageUri by remember { mutableStateOf<Uri?>(null) }

    // 지도 다이얼로그 상태
    var showMapDialog by remember { mutableStateOf(false) }
    var currentLat by remember { mutableStateOf(0.0) }
    var currentLng by remember { mutableStateOf(0.0) }

    // 크롭 런처
    val cropImageLauncher = rememberLauncherForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            croppedImageUri = result.uriContent
        } else {
            Log.e("Crop", "크롭 실패: ${result.error?.message}")
        }
    }

    // 갤러리 선택 런처
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val cropOptions = CropImageContractOptions(it, CropImageOptions()).apply {
                setAspectRatio(1, 1)
                setCropShape(CropImageView.CropShape.RECTANGLE)
                setGuidelines(CropImageView.Guidelines.ON)
            }
            cropImageLauncher.launch(cropOptions)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 프로필 이미지
        Box(modifier = Modifier.size(120.dp)) {
            Surface(
                modifier = Modifier.size(120.dp),
                shape = CircleShape,
                color = Color(0xFFE0E0E0)
            ) {
                if (croppedImageUri != null) {
                    AsyncImage(
                        model = croppedImageUri,
                        contentDescription = "선택한 이미지",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_mypage),
                        contentDescription = "기본 이미지",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(30.dp),
                        tint = Color.Gray
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .size(36.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 8.dp, y = 8.dp)
                    .clickable { pickImageLauncher.launch("image/*") },
                shape = CircleShape,
                color = Color(0xFFFF9800),
                shadowElevation = 4.dp
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_gallery),
                    contentDescription = "갤러리 열기",
                    tint = Color.White,
                    modifier = Modifier.padding(6.dp)
                )
            }
        }

        // 아이디 입력
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = userId,
                onValueChange = { userId = it },
                label = { Text("아이디") },
                modifier = Modifier.weight(1f),
                colors = TextFieldDefaults.outlinedTextFieldColors(containerColor = BackgroundSoft)
            )

            Button(
                onClick = { /* TODO: 중복 확인 */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                modifier = Modifier.align(Alignment.CenterVertically)
            ) {
                Text("중복확인", color = Color.White)
            }
        }

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("비밀번호") },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.outlinedTextFieldColors(containerColor = BackgroundSoft)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = nickname,
                onValueChange = { nickname = it },
                label = { Text("닉네임") },
                modifier = Modifier.weight(1f),
                colors = TextFieldDefaults.outlinedTextFieldColors(containerColor = BackgroundSoft)
            )

            Button(
                onClick = { /* TODO: 닉네임 확인 */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                modifier = Modifier.align(Alignment.CenterVertically)
            ) {
                Text("중복확인", color = Color.White)
            }
        }

        // 현재 위치 인증 카드
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_location_on),
                        contentDescription = "위치 아이콘",
                        modifier = Modifier.size(24.dp),
                        tint = PrimaryColor
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("현재 위치로 인증", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Text("GPS를 통해 동네를 인증합니다", fontSize = 12.sp, color = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = {
                    if (locationPermissionState.allPermissionsGranted) {
                        getCurrentLocation(context) { lat, lng ->
                            currentLat = lat
                            currentLng = lng
                            showMapDialog = true
                        }
                    } else {
                        Toast.makeText(context, "위치 권한이 필요합니다", Toast.LENGTH_SHORT).show()
                        locationPermissionState.launchMultiplePermissionRequest()
                    }
                }) {
                    Text("현재 위치 인증하기")
                }
            }
        }

        // 약관 체크박스
        Column {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Checkbox(
                    checked = agreeAll,
                    onCheckedChange = {
                        agreeAll = it
                        agreeService = it
                        agreePrivacy = it
                        agreeMarketing = it
                    }
                )
                Text("전체 동의", modifier = Modifier.weight(1f))
            }
            Divider(color = Color.LightGray, thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))

            AgreementRow(agreeService, { agreeService = it }, "[필수] 서비스 이용약관")
            AgreementRow(agreePrivacy, { agreePrivacy = it }, "[필수] 개인정보 처리방침")
            AgreementRow(agreeMarketing, { agreeMarketing = it }, "[선택] 마케팅 정보 수신")
        }

        Button(
            onClick = { /* TODO: 회원가입 처리 */ },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
        ) {
            Text("회원가입", color = Color.White)
        }
    }

    // 지도 다이얼로그
    if (showMapDialog) {
        Dialog(onDismissRequest = { showMapDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.7f),
                shape = RoundedCornerShape(16.dp)
            ) {
                KakaoMapScreen(currentLat, currentLng)
            }
        }
    }
}

@SuppressLint("MissingPermission")
fun getCurrentLocation(
    context: Context,
    onLocationReceived: (Double, Double) -> Unit
) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
        location?.let { onLocationReceived(it.latitude, it.longitude) }
    }
}

@Composable
fun AgreementRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    title: String
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text(title, modifier = Modifier.weight(1f))
        Text(
            text = "보기",
            color = Color.Blue,
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .clickable { /* TODO: 보기 클릭 */ }
        )
    }
}

@Composable
fun KakaoMapScreen(lat: Double, lng: Double) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            MapView(context).apply {
                start(
                    object : MapLifeCycleCallback() {
                        override fun onMapDestroy() { Log.d("KakaoMap", "Map destroyed") }
                        override fun onMapError(error: Exception?) { Log.e("KakaoMap", "Error: ${error?.message}") }
                    },
                    object : KakaoMapReadyCallback() {
                        override fun onMapReady(kakaoMap: KakaoMap) {
                            val center = LatLng.from(lat, lng)
                            kakaoMap.moveCamera(CameraUpdateFactory.newCenterPosition(center))

                            val markerLayer = kakaoMap.labelManager?.getLayer("markerLayer")
                            val markerStyle = LabelStyle.from(R.drawable.ic_location_on)
                            val marker = LabelOptions.from(center).setStyles(markerStyle)
                            markerLayer?.addLabel(marker)
                        }
                    }
                )
            }
        }
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFFEF9F0)
@Composable
fun JoinPreview() {
    JoinScreen(navController = rememberNavController())
}
