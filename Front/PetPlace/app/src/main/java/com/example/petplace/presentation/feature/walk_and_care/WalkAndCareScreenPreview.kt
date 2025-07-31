/* WalkAndCarePreview.kt */
package com.example.petplace.presentation.feature.walk_and_care

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Preview(showBackground = true, backgroundColor = 0xFFF5F5F5)
@Composable
fun WalkAndCareScreenPreview() {
    MaterialTheme {
        /* 프리뷰용 ViewModel 생성 */
        val previewVm = WalkAndCareViewModel()
        WalkAndCareScreen(viewModel = previewVm)
    }
}
