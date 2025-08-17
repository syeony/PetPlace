package com.example.petplace.presentation.feature.mypage

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petplace.data.repository.ImageRepository
import com.example.petplace.data.repository.PetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PetProfileUiState(
    val petName: String = "",
    val animal: String = "",
    val showAnimalMenu: Boolean = false,
    val breed: String = "",
    val showBreedMenu: Boolean = false,
    val gender: String? = null,
    val neutered: Boolean = false,
    val birthDate: String = "",
    val age: String = "",
    val profileImageUri: Uri? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val validationErrors: Map<String, String> = emptyMap(),
    val animalOptions: List<String> = listOf("강아지", "고양이", "토끼", "햄스터", "기니피그", "고슴도치", "앵무새", "새", "거북이", "물고기", "파충류", "양서류", "기타"),
    val breedOptions: List<String> = emptyList(), // 동적으로 변경됨
    val petId: Int? = null,  // 수정 모드일 때 사용
    val isEditMode: Boolean = false,
)

@HiltViewModel
class PetProfileViewModel @Inject constructor(
    private val petRepository: PetRepository,
    private val imageRepository: ImageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PetProfileUiState())
    val uiState: StateFlow<PetProfileUiState> = _uiState.asStateFlow()

    // 기존 펫 정보 로드 (수정 모드용)
    fun loadPetInfo(petId: Int) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                petRepository.getPetInfo(petId)
                    .onSuccess { pet ->
                        // 서버 이미지 URL 처리
                        val imageUrl = if (!pet.imgSrc.isNullOrEmpty()) {
                            if (pet.imgSrc.startsWith("http")) {
                                pet.imgSrc
                            } else {
                                "http://43.201.108.195:8081${pet.imgSrc}"
                            }
                        } else null

                        val animalDisplay = pet.animal
                        val breedDisplay = pet.breed

                        _uiState.value = _uiState.value.copy(
                            petId = petId,
                            isEditMode = true,
                            petName = pet.name,
                            animal = animalDisplay, // 동물 정보 추가
                            breed = breedDisplay,
                            breedOptions = getBreedOptionsByAnimal(animalDisplay), // 해당 동물의 품종 옵션 설정
                            gender = mapApiGenderToDisplay(pet.sex),
                            neutered = pet.tnr,
                            birthDate = formatApiDateToDisplay(pet.birthday),
                            age = calculateAge(pet.birthday).toString(),
                            profileImageUri = imageUrl?.let { Uri.parse(it) },
                            isLoading = false
                        )
                    }
                    .onFailure { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "펫 정보를 불러오는데 실패했습니다: ${exception.message}"
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "펫 정보를 불러오는데 실패했습니다."
                )
            }
        }
    }

    fun updatePetName(name: String) {
        _uiState.value = _uiState.value.copy(
            petName = name,
            validationErrors = _uiState.value.validationErrors - "petName"
        )
    }

    fun updateAnimal(animal: String) {
        _uiState.value = _uiState.value.copy(
            animal = animal,
            showAnimalMenu = false,
            breed = "", // 동물이 바뀌면 품종 초기화
            breedOptions = getBreedOptionsByAnimal(animal), // 새로운 품종 옵션 설정
            validationErrors = _uiState.value.validationErrors - "animal" - "breed"
        )
    }

    fun toggleAnimalMenu() {
        _uiState.value = _uiState.value.copy(
            showAnimalMenu = !_uiState.value.showAnimalMenu
        )
    }

    fun updateBreed(breed: String) {
        _uiState.value = _uiState.value.copy(
            breed = breed,
            showBreedMenu = false,
            validationErrors = _uiState.value.validationErrors - "breed"
        )
    }

    fun toggleBreedMenu() {
        _uiState.value = _uiState.value.copy(
            showBreedMenu = !_uiState.value.showBreedMenu
        )
    }

    fun updateGender(gender: String) {
        _uiState.value = _uiState.value.copy(
            gender = gender,
            validationErrors = _uiState.value.validationErrors - "gender"
        )
    }

    fun updateNeutered(neutered: Boolean) {
        _uiState.value = _uiState.value.copy(neutered = neutered)
    }

    fun updateBirthDate(date: String) {
        val calculatedAge = if (date.isNotBlank()) {
            calculateAgeFromBirthDate(date).toString()
        } else {
            ""
        }

        _uiState.value = _uiState.value.copy(
            birthDate = date,
            age = calculatedAge, // 나이 자동 설정
            validationErrors = _uiState.value.validationErrors - "birthDate" - "age" // 나이 에러도 함께 제거
        )
    }

    fun updateAge(age: String) {
        _uiState.value = _uiState.value.copy(
            age = age,
            validationErrors = _uiState.value.validationErrors - "age"
        )
    }

    fun updateProfileImage(uri: Uri?) {
        _uiState.value = _uiState.value.copy(profileImageUri = uri)
    }

    private fun validateForm(): Boolean {
        val state = _uiState.value
        val errors = mutableMapOf<String, String>()

        if (state.petName.isBlank()) {
            errors["petName"] = "반려동물 이름을 입력해주세요."
        }

        if (state.animal.isBlank()) {
            errors["animal"] = "동물을 선택해주세요."
        }

        if (state.breed.isBlank()) {
            errors["breed"] = "품종을 선택해주세요."
        }

        if (state.gender == null) {
            errors["gender"] = "성별을 선택해주세요."
        }

        if (state.age.isBlank()) {
            errors["age"] = "나이를 입력해주세요."
        } else {
            try {
                val ageInt = state.age.toInt()
                if (ageInt < 0 || ageInt > 30) {
                    errors["age"] = "올바른 나이를 입력해주세요. (0-30)"
                }
            } catch (e: NumberFormatException) {
                errors["age"] = "나이는 숫자로 입력해주세요."
            }
        }

        _uiState.value = state.copy(validationErrors = errors)
        return errors.isEmpty()
    }

    fun savePetProfile(onSuccess: (Int?) -> Unit) {
        if (!validateForm()) {
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isSaving = true, error = null)

                val state = _uiState.value

                // 1. 이미지 처리 로직 수정
                var finalImageUrl: String? = null

                if (state.profileImageUri != null) {
                    val imageUriString = state.profileImageUri.toString()

                    // 새로 선택한 이미지인지 확인 (기존 서버 URL이 아닌 경우)
                    if (!imageUriString.startsWith("http://43.201.108.195:8081")) {
                        // 새 이미지 업로드
                        try {
                            val uploadedUrls =
                                imageRepository.uploadImages(listOf(state.profileImageUri))
                            finalImageUrl = uploadedUrls.firstOrNull()
                        } catch (e: Exception) {
                            _uiState.value = _uiState.value.copy(
                                isSaving = false,
                                error = "이미지 업로드에 실패했습니다: ${e.message}"
                            )
                            return@launch
                        }
                    } else {
                        // 기존 이미지 URL에서 서버 베이스 URL 제거하여 상대 경로로 변환
                        finalImageUrl = imageUriString.replace("http://43.201.108.195:8081", "")
                    }
                }

                // 성별을 API 형식에 맞게 변환
                val apiGender = when (state.gender) {
                    "남아" -> "MALE"
                    "여아" -> "FEMALE"
                    else -> "MALE"
                }

                // 2. 펫 정보 저장 (기존 이미지 URL도 포함)
                val result = petRepository.savePetInfo(
                    petId = state.petId,
                    name = state.petName,
                    animal = mapAnimalToApiFormat(state.animal),
                    breed = mapBreedToApiFormat(state.breed),
                    sex = apiGender,
                    birthday = formatBirthDateForApi(state.birthDate),
                    imgSrc = finalImageUrl, // 기존 또는 새 이미지 URL
                    tnr = state.neutered
                )

                result.fold(
                    onSuccess = { responseData ->
                        _uiState.value = _uiState.value.copy(isSaving = false)
                        onSuccess(responseData.id)
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isSaving = false,
                            error = exception.message ?: "펫 프로필 저장에 실패했습니다."
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = e.message ?: "펫 프로필 저장 중 오류가 발생했습니다."
                )
            }
        }
    }

    // 헬퍼 메서드들
    // Animal 관련 변환 함수들
//    private fun mapApiAnimalToDisplay(apiAnimal: String): String {
//        return when (apiAnimal) {
//            "DOG" -> "강아지"
//            "CAT" -> "고양이"
//            "RABBIT" -> "토끼"
//            "HAMSTER" -> "햄스터"
//            "GUINEA_PIG" -> "기니피그"
//            "HEDGEHOG" -> "고슴도치"
//            "FERRET" -> "앵무새"
//            "BIRD" -> "새"
//            "TURTLE" -> "거북이"
//            "FISH" -> "물고기"
//            "REPTILE" -> "파충류"
//            "AMPHIBIAN" -> "양서류"
//            "OTHER" -> "기타"
//            else -> "기타"
//        }
//    }

    private fun mapAnimalToApiFormat(animal: String): String {
        return when (animal) {
            "강아지" -> "DOG"
            "고양이" -> "CAT"
            "토끼" -> "RABBIT"
            "햄스터" -> "HAMSTER"
            "기니피그" -> "GUINEA_PIG"
            "고슴도치" -> "HEDGEHOG"
            "앵무새" -> "FERRET"
            "새" -> "BIRD"
            "거북이" -> "TURTLE"
            "물고기" -> "FISH"
            "파충류" -> "REPTILE"
            "양서류" -> "AMPHIBIAN"
            "기타" -> "OTHER"
            else -> "OTHER"
        }
    }

    // 동물에 따른 품종 옵션 반환
    private fun getBreedOptionsByAnimal(animal: String): List<String> {
        return when (animal) {
            "강아지" -> listOf(
                "아펜핀셔", "아프간 하운드", "에어데일 테리어", "아키타", "아펜젤러", "오스트레일리언 테리어",
                "바센지", "바셋 하운드", "비글", "베들링턴 테리어", "버니즈 마운틴 독", "블랙 앤 탄 쿤하운드",
                "블레넘 스패니얼", "블러드하운드", "보더 콜리", "보더 테리어", "보르조이", "보스턴 테리어",
                "부비에 데 플랑드르", "복서", "브라반손 그리폰", "브리아드", "브리타니 스패니얼", "불마스티프",
                "케언 테리어", "카디건 웰시 코기", "체서피크 베이 리트리버", "치와와", "차우차우",
                "클럼버 스패니얼", "코커 스패니얼", "콜리", "컬리 코티드 리트리버", "댄디 딘몬트 테리어",
                "도레(아시아 야생개)", "딩고", "도베르만", "잉글리시 폭스하운드", "잉글리시 세터",
                "잉글리시 스프링거 스패니얼", "엔틀부허 마운틴 독", "에스키모 도그", "플랫 코티드 리트리버",
                "프렌치 불도그", "저먼 셰퍼드", "저먼 쇼트헤어드 포인터", "자이언트 슈나우저",
                "골든 리트리버", "고든 세터", "그레이트 데인", "그레이트 피레니즈", "그레이터 스위스 마운틴 독",
                "그루넨달", "이비자 하운드", "아이리시 세터", "아이리시 테리어", "아이리시 워터 스패니얼",
                "아이리시 울프하운드", "이탈리안 그레이하운드", "일본 스패니얼", "키스혼드", "켈피",
                "케리 블루 테리어", "코몬도르", "쿠바즈", "래브라도 리트리버", "레이클랜드 테리어",
                "레온베르거", "라사 압소", "알래스칸 말라뮤트", "말리노이즈", "말티즈", "멕시칸 헤어리스 도그",
                "미니어처 핀셔", "미니어처 푸들", "미니어처 슈나우저", "뉴펀들랜드", "노퍽 테리어",
                "노르웨이 엘크하운드", "노리치 테리어", "올드 잉글리시 쉽독", "오터하운드", "파피용", "페키니즈",
                "펨브록 웰시 코기", "포메라니안", "퍼그", "레드본 쿤하운드", "로디지안 리지백", "로트와일러",
                "세인트 버나드", "살루키", "사모예드", "스키퍼키", "스코티시 테리어", "스코티시 디어하운드",
                "실리햄 테리어", "셰틀랜드 쉽독", "시추", "시베리안 허스키", "실키 테리어",
                "소프트 코티드 휘튼 테리어", "스태퍼드셔 불테리어", "푸들", "스탠다드 슈나우저",
                "서식스 스패니얼", "티베탄 마스티프", "티베탄 테리어", "토이 푸들", "토이 테리어", "비즐라",
                "워커 하운드", "와이마라너", "웰시 스프링거 스패니얼", "웨스트 하이랜드 화이트 테리어",
                "휘핏", "와이어헤어드 폭스 테리어", "요크셔 테리어"
            )

            "고양이" -> listOf(
                "코리안 숏헤어", "러시안 블루", "페르시안", "샴", "먼치킨", "스코티시 폴드", "랙돌",
                "코리안 미디엄헤어", "아메리칸 숏헤어", "코리안 롱헤어", "토터셸", "칼리코", "토비",
                "다일루트 칼리코", "턱시도", "다일루트 토터셸", "태비", "메인 쿤", "벵갈", "노르웨이 숲", "기타"
            )
            "토끼" -> listOf(
                "네덜란드 드워프", "미니 렉스", "라이언헤드", "기타"
            )
            "햄스터" -> listOf(
                "골든 햄스터", "테디베어 햄스터", "캠벨 햄스터", "윈터 화이트 햄스터",
                "펄 윈터 화이트 햄스터", "로보로브스키 햄스터", "차이니즈 햄스터", "기타"
            )
            "새" -> listOf(
                "잉꼬", "왕관앵무", "모란앵무", "아프리칸 그레이 패럿", "금강앵무", "코카투",
                "코뉴어", "패럿렛", "아마존앵무", "링넥 파라킷", "카나리아", "주홍얼굴 핀치",
                "화이트 자바 핀치", "분홍얼굴 핀치", "구르디안 핀치", "기타"
            )
            "파충류" -> listOf(
                "레오파드 게코", "크레스티드 게코", "비어디 드래곤", "기타"
            )
            else -> listOf("기타")
        }
    }

    // Breed API 변환 함수 업데이트 (일부만 예시)
    private fun mapBreedToApiFormat(breed: String): String {
        return when (breed) {
            // DOG
            "아펜핀셔" -> "AFFENPINSCHER"
            "아프간 하운드" -> "AFGHAN_HOUND"
            "에어데일 테리어" -> "AIREDALE"
            "아키타" -> "AKITA"
            "아펜젤러" -> "APPENZELLER"
            "오스트레일리언 테리어" -> "AUSTRALIAN_TERRIER"
            "바센지" -> "BASENJI"
            "바셋 하운드" -> "BASSET"
            "비글" -> "BEAGLE"
            "베들링턴 테리어" -> "BEDLINGTON_TERRIER"
            "버니즈 마운틴 독" -> "BERNESE_MOUNTAIN_DOG"
            "블랙 앤 탄 쿤하운드" -> "BLACK_AND_TAN_COONHOUND"
            "블레넘 스패니얼" -> "BLENHEIM_SPANIEL"
            "블러드하운드" -> "BLOODHOUND"
            "보더 콜리" -> "BORDER_COLLIE"
            "보더 테리어" -> "BORDER_TERRIER"
            "보르조이" -> "BORZOI"
            "보스턴 테리어" -> "BOSTON_BULL"
            "부비에 데 플랑드르" -> "BOUVIER_DES_FLANDRES"
            "복서" -> "BOXER"
            "브라반손 그리폰" -> "BRABANCON_GRIFFON"
            "브리아드" -> "BRIARD"
            "브리타니 스패니얼" -> "BRITTANY_SPANIEL"
            "불마스티프" -> "BULL_MASTIFF"
            "케언 테리어" -> "CAIRN"
            "카디건 웰시 코기" -> "CARDIGAN"
            "체서피크 베이 리트리버" -> "CHESAPEAKE_BAY_RETRIEVER"
            "치와와" -> "CHIHUAHUA"
            "차우차우" -> "CHOW"
            "클럼버 스패니얼" -> "CLUMBER"
            "코커 스패니얼" -> "COCKER_SPANIEL"
            "콜리" -> "COLLIE"
            "컬리 코티드 리트리버" -> "CURLY_COATED_RETRIEVER"
            "댄디 딘몬트 테리어" -> "DANDIE_DINMONT"
            "도레(아시아 야생개)" -> "DHOLE"
            "딩고" -> "DINGO"
            "도베르만" -> "DOBERMAN"
            "잉글리시 폭스하운드" -> "ENGLISH_FOXHOUND"
            "잉글리시 세터" -> "ENGLISH_SETTER"
            "잉글리시 스프링거 스패니얼" -> "ENGLISH_SPRINGER"
            "엔틀부허 마운틴 독" -> "ENTLEBUCHER"
            "에스키모 도그" -> "ESKIMO_DOG"
            "플랫 코티드 리트리버" -> "FLAT_COATED_RETRIEVER"
            "프렌치 불도그" -> "FRENCH_BULLDOG"
            "저먼 셰퍼드" -> "GERMAN_SHEPHERD"
            "저먼 쇼트헤어드 포인터" -> "GERMAN_SHORT_HAIRED_POINTER"
            "자이언트 슈나우저" -> "GIANT_SCHNAUZER"
            "골든 리트리버" -> "GOLDEN_RETRIEVER"
            "고든 세터" -> "GORDON_SETTER"
            "그레이트 데인" -> "GREAT_DANE"
            "그레이트 피레니즈" -> "GREAT_PYRENEES"
            "그레이터 스위스 마운틴 독" -> "GREATER_SWISS_MOUNTAIN_DOG"
            "그루넨달" -> "GROENENDAEL"
            "이비자 하운드" -> "IBIZAN_HOUND"
            "아이리시 세터" -> "IRISH_SETTER"
            "아이리시 테리어" -> "IRISH_TERRIER"
            "아이리시 워터 스패니얼" -> "IRISH_WATER_SPANIEL"
            "아이리시 울프하운드" -> "IRISH_WOLFHOUND"
            "이탈리안 그레이하운드" -> "ITALIAN_GREYHOUND"
            "일본 스패니얼" -> "JAPANESE_SPANIEL"
            "키스혼드" -> "KEESHOND"
            "켈피" -> "KELPIE"
            "케리 블루 테리어" -> "KERRY_BLUE_TERRIER"
            "코몬도르" -> "KOMONDOR"
            "쿠바즈" -> "KUVASZ"
            "래브라도 리트리버" -> "LABRADOR_RETRIEVER"
            "레이클랜드 테리어" -> "LAKELAND_TERRIER"
            "레온베르거" -> "LEONBERG"
            "라사 압소" -> "LHASA"
            "알래스칸 말라뮤트" -> "MALAMUTE"
            "말리노이즈" -> "MALINOIS"
            "말티즈" -> "MALTESE_DOG"
            "멕시칸 헤어리스 도그" -> "MEXICAN_HAIRLESS"
            "미니어처 핀셔" -> "MINIATURE_PINSCHER"
            "미니어처 푸들" -> "MINIATURE_POODLE"
            "푸들" -> "STANDARD_POODLE"
            "미니어처 슈나우저" -> "MINIATURE_SCHNAUZER"
            "뉴펀들랜드" -> "NEWFOUNDLAND"
            "노퍽 테리어" -> "NORFOLK_TERRIER"
            "노르웨이 엘크하운드" -> "NORWEGIAN_ELKHOUND"
            "노리치 테리어" -> "NORWICH_TERRIER"
            "올드 잉글리시 쉽독" -> "OLD_ENGLISH_SHEEPDOG"
            "오터하운드" -> "OTTERHOUND"
            "파피용" -> "PAPILLON"
            "페키니즈" -> "PEKINESE"
            "펨브록 웰시 코기" -> "PEMBROKE"
            "포메라니안" -> "POMERANIAN"
            "퍼그" -> "PUG"
            "레드본 쿤하운드" -> "REDBONE"
            "로디지안 리지백" -> "RHODESIAN_RIDGEBACK"
            "로트와일러" -> "ROTTWEILER"
            "세인트 버나드" -> "SAINT_BERNARD"
            "살루키" -> "SALUKI"
            "사모예드" -> "SAMOYED"
            "스키퍼키" -> "SCHIPPERKE"
            "스코티시 테리어" -> "SCOTCH_TERRIER"
            "스코티시 디어하운드" -> "SCOTTISH_DEERHOUND"
            "실리햄 테리어" -> "SEALYHAM_TERRIER"
            "셰틀랜드 쉽독" -> "SHETLAND_SHEEPDOG"
            "시추" -> "SHIH_TZU"
            "시베리안 허스키" -> "SIBERIAN_HUSKY"
            "실키 테리어" -> "SILKY_TERRIER"
            "소프트 코티드 휘튼 테리어" -> "SOFT_COATED_WHEATEN_TERRIER"
            "스태퍼드셔 불테리어" -> "STAFFORDSHIRE_BULLTERRIER"
            "스탠다드 슈나우저" -> "STANDARD_SCHNAUZER"
            "서식스 스패니얼" -> "SUSSEX_SPANIEL"
            "티베탄 마스티프" -> "TIBETAN_MASTIFF"
            "티베탄 테리어" -> "TIBETAN_TERRIER"
            "토이 푸들" -> "TOY_POODLE"
            "토이 테리어" -> "TOY_TERRIER"
            "비즐라" -> "VIZSLA"
            "워커 하운드" -> "WALKER_HOUND"
            "와이마라너" -> "WEIMARANER"
            "웰시 스프링거 스패니얼" -> "WELSH_SPRINGER_SPANIEL"
            "웨스트 하이랜드 화이트 테리어" -> "WEST_HIGHLAND_WHITE_TERRIER"
            "휘핏" -> "WHIPPET"
            "와이어헤어드 폭스 테리어" -> "WIRE_HAIRED_FOX_TERRIER"
            "요크셔 테리어" -> "YORKSHIRE_TERRIER"

            // CAT
            "코리안 숏헤어" -> "KOREAN_SHORTHAIR"
            "러시안 블루" -> "RUSSIAN_BLUE"
            "페르시안" -> "PERSIAN"
            "샴" -> "SIAMESE"
            "먼치킨" -> "MUNCHKIN"
            "스코티시 폴드" -> "SCOTTISH_FOLD"
            "랙돌" -> "RAGDOLL"
            "코리안 미디엄헤어" -> "KOREAN_MEDIUMHAIR"
            "아메리칸 숏헤어" -> "AMERICAN_SHORTHAIR"
            "코리안 롱헤어" -> "KOREAN_LONGHAIR"
            "토터셸" -> "TORTOISESHELL"
            "칼리코" -> "CALICO"
            "토비" -> "TOBY"
            "다일루트 칼리코" -> "DILUTE_CALICO"
            "턱시도" -> "TUXEDO"
            "다일루트 토터셸" -> "DILUTE_TORTOISESHELL"
            "태비" -> "TABBY"
            "메인 쿤" -> "MAINE_COON"
            "벵갈" -> "BENGAL"
            "노르웨이 숲" -> "NORWEGIAN_FOREST"
            "기타" -> "OTHER_CAT"

            // RABBIT
            "네덜란드 드워프" -> "NETHERLAND_DWARF"
            "미니 렉스" -> "MINI_REX"
            "라이언헤드" -> "LIONHEAD"
            "기타" -> "OTHER_RABBIT"

            // HAMSTER
            "골든 햄스터" -> "GOLDEN_HAMSTER"
            "테디베어 햄스터" -> "TEDDY_BEAR_HAMSTER"
            "캠벨 햄스터" -> "CAMPBELL_DWARF"
            "윈터 화이트 햄스터" -> "WINTER_WHITE_DWARF"
            "펄 윈터 화이트 햄스터" -> "PEARL_WINTER_WHITE_DWARF"
            "로보로브스키 햄스터" -> "ROBOROVSKI_DWARF"
            "차이니즈 햄스터" -> "CHINESE_HAMSTER"
            "기타" -> "OTHER_HAMSTER"

            // BIRD
            "잉꼬" -> "BUDGERIGAR"
            "왕관앵무" -> "CROWN_PARROT"
            "모란앵무" -> "LOVEBIRD"
            "아프리칸 그레이 패럿" -> "AFRICAN_GREY_PARROT"
            "금강앵무" -> "CONURE"
            "코카투" -> "COCKATOO"
            "코뉴어" -> "CONURE_PARROT"
            "패럿렛" -> "PARROTFLET"
            "아마존앵무" -> "AMAZON_PARROT"
            "링넥 파라킷" -> "RINGNECK_PARAKEET"
            "카나리아" -> "CANARY"
            "주홍얼굴 핀치" -> "SCARLET_FACE_FINCH"
            "화이트 자바 핀치" -> "WHITE_JAVA_FINCH"
            "분홍얼굴 핀치" -> "PINK_FACE_FINCH"
            "구르디안 핀치" -> "GOURDIAN_FINCH"
            "기타" -> "OTHER_BIRD"

            // REPTILE
            "레오파드 게코" -> "LEOPARD_GECKO"
            "크레스티드 게코" -> "CRESTED_GECKO"
            "비어디 드래곤" -> "BEARDED_DRAGON"
            "기타" -> "OTHER_REPTILE"

            else -> "UNKNOWN"
        }
    }

//    private fun mapApiBreedToDisplay(apiBreed: String): String {
//        return when (apiBreed) {
//            // DOG
//            "POMERANIAN" -> "포메라니안"
//            "STANDARD_POODLE", "TOY_POODLE", "MINIATURE_POODLE" -> "푸들"
//            "MALTESE_DOG" -> "말티즈"
//            "GOLDEN_RETRIEVER" -> "골든 리트리버"
//            "CHIHUAHUA" -> "치와와"
//            "BEAGLE" -> "비글"
//            "YORKSHIRE_TERRIER" -> "요크셔 테리어"
//            "SHIH_TZU" -> "시추"
//            "PUG" -> "퍼그"
//
//            // CAT
//            "KOREAN_SHORTHAIR" -> "코리안 숏헤어"
//            "RUSSIAN_BLUE" -> "러시안 블루"
//            "PERSIAN" -> "페르시안"
//            "SIAMESE" -> "샴"
//            "MUNCHKIN" -> "먼치킨"
//            "SCOTTISH_FOLD" -> "스코티시 폴드"
//            "RAGDOLL" -> "랙돌"
//
//            // RABBIT
//            "NETHERLAND_DWARF" -> "네덜란드 드워프"
//            "MINI_REX" -> "미니 렉스"
//            "LIONHEAD" -> "라이언헤드"
//
//            // HAMSTER
//            "GOLDEN_HAMSTER" -> "골든 햄스터"
//            "TEDDY_BEAR_HAMSTER" -> "테디베어 햄스터"
//            "CAMPBELL_DWARF" -> "캠벨 햄스터"
//
//            // 기타 등등... 필요한 매핑 추가
//            else -> "기타"
//        }
//    }

    private fun mapApiGenderToDisplay(apiGender: String): String {
        return when (apiGender) {
            "MALE" -> "남아"
            "FEMALE" -> "여아"
            else -> "남아"
        }
    }

    private fun formatApiDateToDisplay(apiDate: String): String {
        // "2025-08-12" -> "08/12/2025" 형식으로 변환
        if (apiDate.isBlank()) return ""

        val parts = apiDate.split("-")
        if (parts.size == 3) {
            val year = parts[0]
            val month = parts[1]
            val day = parts[2]
            return "$month/$day/$year"
        }
        return apiDate
    }

    private fun calculateAgeFromBirthDate(birthDate: String): Int {
        return try {
            if (birthDate.isBlank()) return 0

            val parts = birthDate.split("/")
            if (parts.size == 3) {
                val month = parts[0].toInt()
                val day = parts[1].toInt()
                val year = parts[2].toInt()

                val calendar = java.util.Calendar.getInstance()
                val currentYear = calendar.get(java.util.Calendar.YEAR)
                val currentMonth = calendar.get(java.util.Calendar.MONTH) + 1 // Calendar.MONTH는 0부터 시작
                val currentDay = calendar.get(java.util.Calendar.DAY_OF_MONTH)

                var age = currentYear - year

                // 생일이 아직 안 지났으면 나이에서 1을 뺌
                if (currentMonth < month || (currentMonth == month && currentDay < day)) {
                    age--
                }

                return if (age < 0) 0 else age
            }
            0
        } catch (e: Exception) {
            0
        }
    }


    private fun calculateAge(birthday: String): Int {
        return try {
            val birthYear = birthday.substring(0, 4).toInt()
            val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
            currentYear - birthYear
        } catch (e: Exception) {
            0
        }
    }

    private fun formatBirthDateForApi(dateStr: String): String {
        // "mm/dd/yyyy" -> "yyyy-mm-dd" 형식으로 변환
        if (dateStr.isBlank()) return ""

        val parts = dateStr.split("/")
        if (parts.size == 3) {
            val month = parts[0].padStart(2, '0')
            val day = parts[1].padStart(2, '0')
            val year = parts[2]
            return "$year-$month-$day"
        }
        return dateStr
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}