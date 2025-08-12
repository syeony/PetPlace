package com.example.petplace.presentation.feature.missing_list

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class MissingReportUi(
    val id: Long,
    val reporterName: String,
    val reporterAvatarUrl: String?,
    val content: String,
    val photoUrl: String?,
    val seenAt: String,     // "2025년 5월 6일 오후 20:00"
    val location: String    // "경상북도 구미시 인의동 365-5"
)

@HiltViewModel
class MissingListViewModel @Inject constructor() : ViewModel() {
    private val _items = MutableStateFlow<List<MissingReportUi>>(emptyList())
    val items: StateFlow<List<MissingReportUi>> = _items

    init {
        // TODO(API 나오면 실제 호출로 교체)
        _items.value = listOf(
            MissingReportUi(
                id = 1L,
                reporterName = "이도형",
                reporterAvatarUrl = null,
                content = "인의동 슈퍼거리에서 잃어버렸습니다. 엄청 활발하고 골든 리트리버이고 코에 흰 점찍이 있는 아이입니다.",
                photoUrl = "https://images.unsplash.com/photo-1507149833265-60c372daea22?q=80&w=1200",
                seenAt = "2025년 5월 6일 오후 20:00",
                location = "경상북도 구미시 인의동 365-5"
            ),
            MissingReportUi(
                id = 2L,
                reporterName = "이도형",
                reporterAvatarUrl = null,
                content = "공원에서 산책하다가 놓쳤습니다. 갈색 털에 작은 체구의 푸들이고 목에 빨간 목걸이를 하고 있어요.",
                photoUrl = "https://images.unsplash.com/photo-1548199973-03cce0bbc87b?q=80&w=1200",
                seenAt = "2025년 5월 8일 오전 10:30",
                location = "경상북도 구미시 원평동 중앙공원"
            ),
            MissingReportUi(
                id = 3L,
                reporterName = "이도형",
                reporterAvatarUrl = null,
                content = "아파트 단지에서 실종되었습니다. 삼색 고양이이고 왼쪽 귀끝이 조금 접혀있어요. 매우 겁이 많은 아이입니다.",
                photoUrl = "https://images.unsplash.com/photo-1518791841217-8f162f1e1131?q=80&w=1200",
                seenAt = "2025년 5월 10일 오후 15:45",
                location = "경상북도 구미시 신평동 레미안아파트"
            ),
            MissingReportUi(
                id = 4L,
                reporterName = "이도형",
                reporterAvatarUrl = null,
                content = "마트 앞에서 잠깐 놓쳤습니다. 흰색 말티즈이고 분홍색 리본을 하고 있어요. 사람을 매우 좋아하는 성격입니다.",
                photoUrl = "https://images.unsplash.com/photo-1518020382113-a7e8fc38eac9?q=80&w=1200",
                seenAt = "2025년 5월 12일 오후 17:20",
                location = "경상북도 구미시 형곡동 이마트 앞"
            )
        )
    }
}
