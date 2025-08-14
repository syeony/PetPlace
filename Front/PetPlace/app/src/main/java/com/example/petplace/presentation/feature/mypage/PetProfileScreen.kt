package com.example.petplace.presentation.feature.mypage

import android.app.DatePickerDialog
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.petplace.R
import com.example.petplace.presentation.common.theme.PrimaryColor
import java.util.Calendar
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material3.CircularProgressIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetProfileScreen(
    navController: NavController,
    petId: Int? = null,
    viewModel: PetProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    var showBreedMenu by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    val amber = Color(0xFFFFC981)

    // 갤러리에서 이미지 선택 런처
    val launcherGallery =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                viewModel.updateProfileImage(uri)
            }
        }

    LaunchedEffect(petId) {
        petId?.let { id ->
            viewModel.loadPetInfo(id)
        }
    }

    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // 에러 표시 로직
            viewModel.clearError()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 상단 바
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
            }
            Text(
                text = if (uiState.isEditMode) "펫 프로필 수정" else "펫 프로필 등록",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.width(48.dp)) // 오른쪽 여백 맞추기
        }

        Spacer(Modifier.height(16.dp))

        // 프로필 사진
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                if (uiState.profileImageUri != null) {
                    // 이미지 있을 때
                    Image(
                        painter = rememberAsyncImagePainter(uiState.profileImageUri),
                        contentDescription = "프로필 이미지",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // 이미지 없을 때 (작은 카메라 아이콘)
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .border(2.dp, amber, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CameraAlt,
                            contentDescription = "카메라",
                            tint = amber, // Primary 색상
                            modifier = Modifier.size(40.dp) // 작은 크기
                        )
                    }
                }
                Surface(
                    color = PrimaryColor,
                    shape = CircleShape,
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .offset(x = (-5).dp, y = (-5).dp)
                        .size(30.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            modifier = Modifier
                                .size(16.dp)
                                .clickable {
                                    launcherGallery.launch(
                                        PickVisualMediaRequest(
                                            ActivityResultContracts.PickVisualMedia.ImageOnly
                                        )
                                    )
                                },
                            imageVector = Icons.Filled.CameraAlt,
                            contentDescription = "사진 변경",
                            tint = Color.White
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Text("얼굴이 잘 보이는 무보정 단독 사진을 \n선택해주세요.", textAlign = TextAlign.Center)

        Spacer(Modifier.height(24.dp))

        // 반려동물 이름
        Text(
            text = "반려동물 이름",
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth(Alignment.Start)
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = uiState.petName,
            onValueChange = { viewModel.updatePetName(it) },
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color.White, shape = RoundedCornerShape(8.dp)),
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            placeholder = { Text(color = Color(0xFFADAEBC), text = "이름을 입력해주세요") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,   // 포커스 시 테두리 색
                unfocusedBorderColor = amber, // 포커스 없을 때 테두리 색
                cursorColor = MaterialTheme.colorScheme.primary           // 커서 색도 Primary
            )
        )

        Spacer(Modifier.height(16.dp))

        // 동물 선택
        Text(
            text = "동물 선택",
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth(Alignment.Start)
        )
        Spacer(Modifier.height(8.dp))
        ExposedDropdownMenuBox(
            expanded = uiState.showAnimalMenu,
            onExpandedChange = { viewModel.toggleAnimalMenu() }
        ) {
            OutlinedTextField(
                value = uiState.animal,
                onValueChange = {},
                readOnly = true,
                shape = RoundedCornerShape(8.dp),
                placeholder = { Text(color = Color(0xFFADAEBC), text = "동물을 선택해주세요") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = uiState.showAnimalMenu)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
                    .background(color = Color.White, shape = RoundedCornerShape(8.dp)),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = amber,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )
            ExposedDropdownMenu(
                expanded = uiState.showAnimalMenu,
                onDismissRequest = { viewModel.toggleAnimalMenu() },
                modifier = Modifier.heightIn(max = 200.dp)
            ) {
                uiState.animalOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            viewModel.updateAnimal(option)
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // 품종 선택
        Text(
            text = "품종 선택",
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth(Alignment.Start)
        )
        Spacer(Modifier.height(8.dp))
        ExposedDropdownMenuBox(
            expanded = uiState.showBreedMenu,
            onExpandedChange = { viewModel.toggleBreedMenu() }
        ) {
            OutlinedTextField(
                value = uiState.breed,
                onValueChange = {},
                readOnly = true,
                shape = RoundedCornerShape(8.dp),
                placeholder = { Text(color = Color(0xFFADAEBC), text = "품종을 선택해주세요") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = uiState.showBreedMenu)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
                    .background(color = Color.White, shape = RoundedCornerShape(8.dp)),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = amber,
                    cursorColor = MaterialTheme.colorScheme.primary
                ),
                enabled = uiState.animal.isNotBlank() // 동물이 선택되어야 활성화
            )
            ExposedDropdownMenu(
                expanded = uiState.showBreedMenu,
                onDismissRequest = { viewModel.toggleBreedMenu() },
                modifier = Modifier.heightIn(max = 200.dp)
            ) {
                uiState.breedOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            viewModel.updateBreed(option)
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = "성별 선택",
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth(Alignment.Start)
        )
        Spacer(Modifier.height(8.dp))
        // 성별 선택
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = { viewModel.updateGender("여아") },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (uiState.gender == "여아") Color(0xFFFFB745) else Color.White,
                    contentColor = if (uiState.gender == "여아") Color.White else Color.Black
                ),
                border = BorderStroke(1.dp, Color(0xFFFFB745)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1f) // 가로 공간 균등 분배
                    .height(58.dp)
            ) {
                Text("여아")
            }

            Button(
                onClick = { viewModel.updateGender("남아") },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (uiState.gender == "남아") Color(0xFFFFB745) else Color.White,
                    contentColor = if (uiState.gender == "남아") Color.White else Color.Black
                ),
                border = BorderStroke(1.dp, Color(0xFFFFB745)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(58.dp)
            ) {
                Text("남아")
            }
        }




        Spacer(Modifier.height(16.dp))

        // 중성화 여부
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("중성화했어요")
            Spacer(Modifier.weight(1f))
            Switch(
                checked = uiState.neutered,
                onCheckedChange = { viewModel.updateNeutered(it) }
            )
        }

        Spacer(Modifier.height(16.dp))

        // 생일
//        OutlinedTextField(
//            value = birthDate,
//            onValueChange = { birthDate = it },
//            placeholder = { Text("mm/dd/yyyy") },
//            label = { Text("생일") },
//            trailingIcon = {
//                Icon(Icons.Default.DateRange, contentDescription = "날짜 선택")
//            },
//            modifier = Modifier.fillMaxWidth()
//        )
        BirthDatePicker()

        Spacer(Modifier.height(16.dp))

        // 나이
        OutlinedTextField(
            value = uiState.age,
            onValueChange = { }, // 빈 람다로 변경 (수정 불가)
            shape = RoundedCornerShape(8.dp),
            placeholder = { Text("나이") },
            label = { Text("나이") }, // 레이블 수정
            readOnly = true, // 읽기 전용으로 설정
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color.White, shape = RoundedCornerShape(8.dp)),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = amber,
                cursorColor = MaterialTheme.colorScheme.primary,
                disabledTextColor = Color.Black, // 비활성화 상태에서도 텍스트 보이게
                disabledBorderColor = amber, // 비활성화 상태 테두리 색상
                disabledLabelColor = Color.Gray // 비활성화 상태 레이블 색상
            ),
            enabled = false // 비활성화하여 클릭 불가
        )

        Spacer(Modifier.height(24.dp))

        // 다음 버튼
        Button(
            onClick = {
                viewModel.savePetProfile { savedPetId ->
                    if (uiState.isEditMode) {
                        navController.popBackStack() // 수정 완료 후 이전 화면으로
                    } else {
                        navController.navigate("pet_complete/${savedPetId}") // 새 등록 후 완료 화면으로
                    }
                }
            },
            enabled = !uiState.isSaving,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
        ) {
            if (uiState.isSaving) {
                CircularProgressIndicator(
                    color = Color.White
                )
            } else {
                Text(
                    text = if (uiState.isEditMode) "수정 완료" else "다음",
                    color = Color.White
                )
            }
        }
        uiState.validationErrors.forEach { (field, error) ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun BirthDatePicker(
    viewModel: PetProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val amber = Color(0xFFFFC981)
    val context = LocalContext.current
    var birthDate by remember { mutableStateOf("") }

    // 오늘 날짜 기준으로 초기화
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    // 날짜 선택 다이얼로그
    val datePickerDialog = DatePickerDialog(
        context,
        { _, selectedYear, selectedMonth, selectedDay ->
            val formattedDate =
                "%02d/%02d/%04d".format(selectedMonth + 1, selectedDay, selectedYear)
            viewModel.updateBirthDate(formattedDate)
        },
        year, month, day
    )

    OutlinedTextField(
        value = uiState.birthDate,
        onValueChange = { viewModel.updateBirthDate(it) },
        placeholder = { Text("mm/dd/yyyy") },
        shape = RoundedCornerShape(8.dp),
        label = { Text("생일") },
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = "날짜 선택",
                modifier = Modifier
                    .clickable {
                        datePickerDialog.show()
                    },
                tint = PrimaryColor
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Color.White, shape = RoundedCornerShape(8.dp)),
        readOnly = true, // 직접 입력 못하게
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,   // 포커스 시 테두리 색
            unfocusedBorderColor = amber, // 포커스 없을 때 테두리 색
            cursorColor = MaterialTheme.colorScheme.primary           // 커서 색도 Primary
        )
    )
}
