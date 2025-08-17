package com.example.petplace.presentation.feature.missing_register

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.petplace.R
import com.example.petplace.presentation.common.navigation.BottomNavItem
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val BgColor      = Color(0xFFFEF9F0)
private val AccentOrange = Color(0xFFF79800)

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val detectionImageUri = MutableStateFlow<Uri?>(null)  // or Bitmap?
    val detail        by viewModel.detail.collectAsState()
    val images        by viewModel.imageList.collectAsState()
    val date          by viewModel.date.collectAsState()
    val time          by viewModel.time.collectAsState()
    val place         by viewModel.place.collectAsState()
    val petName       by viewModel.petName.collectAsState()
    val petBreed      by viewModel.petBreed.collectAsState()
    val petSex        by viewModel.petSex.collectAsState()
    val petBirthday   by viewModel.petBirthday.collectAsState()
    val petImgSrc     by viewModel.petImgSrc.collectAsState()
    val isSubmitting  by viewModel.isSubmitting.collectAsState()
    val detectionUri  by viewModel.detectionImageUri.collectAsState()

    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    val selectedAddress by remember(savedStateHandle) {
        savedStateHandle?.getStateFlow<String?>("selected_address", null)
    }?.collectAsState() ?: remember { mutableStateOf<String?>(null) }
    val selectedLat by remember(savedStateHandle) {
        savedStateHandle?.getStateFlow<Double?>("selected_lat", null)
    }?.collectAsState() ?: remember { mutableStateOf<Double?>(null) }
    val selectedLng by remember(savedStateHandle) {
        savedStateHandle?.getStateFlow<Double?>("selected_lng", null)
    }?.collectAsState() ?: remember { mutableStateOf<Double?>(null) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { /* granted -> if (granted) { ... } */ }

    val fusedClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    @SuppressLint("MissingPermission")
    fun fetchAndSetCurrentPlaceIfEmpty() {
        if (place != "주소 선택") return
        val hasFine = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (!hasFine) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            return
        }
        val cts = CancellationTokenSource()
        fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
            .addOnSuccessListener { loc ->
                if (loc != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        Geocoder(context, Locale.KOREAN)
                            .getFromLocation(loc.latitude, loc.longitude, 1) { list: MutableList<Address> ->
                                val addr = list.firstOrNull()?.let { a ->
                                    listOfNotNull(
                                        a.adminArea, a.locality, a.subLocality, a.thoroughfare, a.subThoroughfare
                                    ).joinToString(" ")
                                } ?: "현재 위치"
                                viewModel.setPlace(addr)
                                viewModel.setLatLng(loc.latitude, loc.longitude)
                            }
                    } else {
                        @Suppress("DEPRECATION")
                        val addr = Geocoder(context, Locale.KOREAN)
                            .getFromLocation(loc.latitude, loc.longitude, 1)
                            ?.firstOrNull()?.let { a ->
                                listOfNotNull(
                                    a.adminArea, a.locality, a.subLocality, a.thoroughfare, a.subThoroughfare
                                ).joinToString(" ")
                            } ?: "현재 위치"
                        viewModel.setPlace(addr)
                        viewModel.setLatLng(loc.latitude, loc.longitude)
                    }
                }
            }
    }

    LaunchedEffect(Unit) { fetchAndSetCurrentPlaceIfEmpty() }

    LaunchedEffect(selectedAddress) {
        selectedAddress?.let {
            viewModel.setPlace(it)
            savedStateHandle?.remove<String>("selected_address")
        }
    }
    LaunchedEffect(selectedLat, selectedLng) {
        if (selectedLat != null && selectedLng != null) {
            viewModel.setLatLng(selectedLat!!, selectedLng!!)
            savedStateHandle?.remove<Double>("selected_lat")
            savedStateHandle?.remove<Double>("selected_lng")
        }
    }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val launcherGallery =
        rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(5)) { uris ->
            if (!uris.isNullOrEmpty()) viewModel.addImages(uris)
        }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BgColor,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("실종 등록") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = BgColor),
                modifier = Modifier.height(48.dp),
                windowInsets = WindowInsets(0.dp)
            )
        },
        bottomBar = {
            Button(
                onClick = {
                    if (isSubmitting) return@Button
                    viewModel.submitRegister(
                        onSuccess = {
                            navController.navigate("${BottomNavItem.Neighborhood.route}?showDialog=true") {
                                popUpTo("Missing_register") { inclusive = true }
                            }
                        },
                        onFailure = { msg -> Toast.makeText(context, msg, Toast.LENGTH_SHORT).show() }
                    )
                },
                enabled = !isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(strokeWidth = 2.dp, color = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("등록 중...", color = Color.White)
                } else {
                    Text("작성완료", color = Color.White, fontSize = 16.sp)
                }
            }
        }
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.Top,
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp)
        ) {
            // 감지 결과
            item {
                AnimatedVisibility(visible = detectionUri != null) {
                    Column {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "감지된 사진",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = { viewModel.clearDetection() }) {
                                    Icon(Icons.Default.Close, contentDescription = "닫기")
                                }
                            }
                            AsyncImage(
                                model = detectionUri,
                                contentDescription = "Detection Result",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 120.dp, max = 300.dp)
                                    .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }

            // 펫 카드
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .clickable { navController.navigate("family/select") },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val fullUrl = petImgSrc?.let {
                            if (it.startsWith("http")) it else "http://i13d104.p.ssafy.io:8081$it"
                        }
                        if (fullUrl != null) {
                            AsyncImage(
                                model = fullUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Image(
                                painter = painterResource(R.drawable.pp_logo),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                            )
                        }

                        Spacer(Modifier.width(12.dp))

                        Column(Modifier.weight(1f)) {
                            val nameText  = petName ?: "내 펫 선택"
                            val breedText = petBreed ?: "펫을 선택해주세요"
                            val sexKo = when (petSex?.uppercase()) {
                                "MALE" -> "남아"
                                "FEMALE" -> "여아"
                                else -> "성별미상"
                            }
                            val ageText = petBirthday?.let { b ->
                                runCatching {
                                    val birth = java.time.LocalDate.parse(b)
                                    val years = java.time.Period.between(birth, java.time.LocalDate.now()).years
                                    "${years}살"
                                }.getOrElse { "" }
                            } ?: ""

                            Text(nameText, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(breedText, fontSize = 12.sp, color = Color.Gray)
                            if (petName != null) Text("$sexKo $ageText", fontSize = 12.sp, color = Color.Gray)
                        }

                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                }
            }

            // 사진 업로드
            item {
                Spacer(Modifier.height(24.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .border(BorderStroke(1.dp, Color(0xFFD7D7D7)), RoundedCornerShape(8.dp))
                            .clickable {
                                launcherGallery.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.outline_photo_camera_24),
                            contentDescription = null,
                            tint = Color(0xFF8C8C8C)
                        )
                    }

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(images) { uri ->
                            Image(
                                painter = rememberAsyncImagePainter(uri),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(BorderStroke(1.dp, Color(0xFFD7D7D7)), RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "한마리의 동물만 나오게 해주세요.\n얼굴이 잘 나온 사진을 등록해주세요.",
                    fontSize = 12.sp,
                    color = Color(0xFF8C8C8C),
                    lineHeight = 16.sp
                )
            }

            // 상세 내용
            item {
                Spacer(Modifier.height(24.dp))
                OutlinedTextField(
                    value = detail,
                    onValueChange = viewModel::setDetail,
                    placeholder = { Text("실종 장소, 상황, 특징 등을 작성해주세요.", color = Color(0xFFADAEBC)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color(0xFFE5E7EB),
                        unfocusedBorderColor = Color(0xFFE5E7EB)
                    )
                )
            }

            // 실종 일시
            item {
                Spacer(Modifier.height(24.dp))
                Text("실종 일시", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White)
                            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                            .clickable { showDatePicker = true },
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(date, modifier = Modifier.padding(start = 12.dp), fontSize = 14.sp, color = Color.Black)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White)
                            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                            .clickable { showTimePicker = true },
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(time, modifier = Modifier.padding(start = 12.dp), fontSize = 14.sp, color = Color.Black)
                    }
                }
            }

            // 실종 장소
            item {
                Spacer(Modifier.height(24.dp))
                Text("실종 장소", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                        .clickable { navController.navigate("missing_map") },
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(text = place, modifier = Modifier.padding(start = 12.dp), fontSize = 14.sp, color = Color.Black)
                }

                Spacer(Modifier.height(5.dp))
                Text("작성 완료 후에는 장소를 변경할 수 없어요.", fontSize = 12.sp, color = Color(0xFF8C8C8C))
            }

            item { Spacer(Modifier.height(8.dp)) }
        }
    }

    // 날짜 피커
    if (showDatePicker) {
        val pickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let {
                            val str = Instant.ofEpochMilli(it)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                                .format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"))
                            viewModel.setDate(str)
                        }
                        showDatePicker = false
                    }
                ) { Text("확인") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("취소") }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }

    // 시간 피커
    if (showTimePicker) {
        val timeState = rememberTimePickerState()
        AlertDialog(onDismissRequest = { showTimePicker = false }) {
            Surface(shape = RoundedCornerShape(16.dp)) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TimeInput(state = timeState)
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showTimePicker = false }) { Text("취소") }
                        Spacer(Modifier.width(8.dp))
                        TextButton(
                            onClick = {
                                val str = LocalTime.of(timeState.hour, timeState.minute)
                                    .format(DateTimeFormatter.ofPattern("a hh:mm", Locale.KOREAN))
                                viewModel.setTime(str)
                                showTimePicker = false
                            }
                        ) { Text("확인") }
                    }
                }
            }
        }
    }

    // savedStateHandle: 펫 선택 결과
    navController.currentBackStackEntry?.savedStateHandle?.let { handle ->
        val id       = handle.get<Long>("pet_id")
        val name     = handle.get<String>("pet_name")
        val breed    = handle.get<String>("pet_breed")
        val sex      = handle.get<String>("pet_sex")
        val birthday = handle.get<String>("pet_birthday")
        val img      = handle.get<String>("pet_img")
        if (id != null || name != null || breed != null || sex != null || birthday != null || img != null) {
            viewModel.setSelectedPet(name, breed, sex, birthday, img, id)
            handle.remove<Long>("pet_id")
            handle.remove<String>("pet_name")
            handle.remove<String>("pet_breed")
            handle.remove<String>("pet_sex")
            handle.remove<String>("pet_birthday")
            handle.remove<String>("pet_img")
        }
    }
}
