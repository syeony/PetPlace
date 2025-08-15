package com.example.petplace.presentation.feature.missing_report

import android.content.Context
import android.graphics.*
import android.net.Uri
import android.os.SystemClock
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petplace.PetPlaceApp
import com.example.petplace.data.local.onDevice.Detection
import com.example.petplace.data.model.missing_report.CreateSightingImageReq
import com.example.petplace.data.model.missing_report.CreateSightingReq
import com.example.petplace.data.model.missing_report.SightingRes
import com.example.petplace.data.repository.ImageRepository
import com.example.petplace.data.repository.MissingSightingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.FloatBuffer
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import ai.onnxruntime.NodeInfo
import ai.onnxruntime.TensorInfo
// ONNX Runtime (breed classification)
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import ai.onnxruntime.OnnxTensor
import android.content.ContentValues.TAG

// ===== UI STATE =====
data class ReportUiState(
    val description: String = "",
    val selectedDate: LocalDate = LocalDate.now(),
    val selectedTime: LocalTime = LocalTime.now().withSecond(0).withNano(0),
    val selectedAddress: String = "위치 정보를 가져오는 중...",
    val selectedLat: Double? = null,
    val selectedLng: Double? = null,
    val hasManualSelection: Boolean = false,

    val loading: Boolean = false,
    val error: String? = null,
    val submitted: Boolean = false,

    // Detection/Visualization state
    val detectionChecked: Boolean = false,
    val detectionMessage: String = "",
    val annotatedBitmap: Bitmap? = null,
    val dogResults: List<DogResult> = emptyList(),
    val catResults: List<CatResult> = emptyList(),          // ✅ 추가

    // PATCH: tunables
    val scoreThreshold: Float = 0.35f,
    val iouThreshold: Float = 0.45f,
    val minBoxPx: Float = 12f,
    val labelFilter: Set<String> = setOf("cat", "dog"),

    // PATCH: metrics
    val lastAnalyzeMs: Long = 0L
)

// 품종 분류 결과
data class DogResult(
    val box: RectF,
    val detScore: Float,
    val breedLabel: String,
    val breedProb: Float
)

// ✅ 고양이 품종 결과 추가
data class CatResult(
    val box: RectF,
    val detScore: Float,
    val breedLabel: String,
    val breedProb: Float
)

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val repo: MissingSightingRepository,
    private val imageRepo: ImageRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState: StateFlow<ReportUiState> = _uiState

    private val app = context as PetPlaceApp
    private val user = app.getUserInfo() ?: throw IllegalStateException("로그인 필요")

    // NOTE: 프로젝트 내 TFLite 감지기 래퍼 (이미 보유)
    private val detector by lazy { Yolo11nTFLite(context) }

    // ===== ONNX (품종 분류) =====
    private val ortEnv: OrtEnvironment by lazy { OrtEnvironment.getEnvironment() }

    // ✅ 개 품종 모델 (기존)
    private val dogBreedSession: OrtSession by lazy {
        val so = OrtSession.SessionOptions().apply {
            setOptimizationLevel(OrtSession.SessionOptions.OptLevel.ALL_OPT)
            setIntraOpNumThreads(4)
        }
        val bytes = context.assets.open("dogBreedModel.onnx").use { it.readBytes() }
        ortEnv.createSession(bytes, so)
    }
    private val dogLabels: List<String> by lazy {
        context.assets.open("dogLabels.txt").bufferedReader().readLines()
    }

    // ✅ 고양이 품종 모델 (새로 추가)
    private val catBreedSession: OrtSession by lazy {
        val so = OrtSession.SessionOptions().apply {
            setOptimizationLevel(OrtSession.SessionOptions.OptLevel.ALL_OPT)
            setIntraOpNumThreads(4)
        }
        // 파일명: catBreedModel.onnx
        val bytes = context.assets.open("catBreedModel.onnx").use { it.readBytes() }
        ortEnv.createSession(bytes, so)
    }
    private val catLabels: List<String> by lazy {
        // 레이블 파일명: catLabels.txt (class_names 순서와 동일)
        context.assets.open("catLabels.txt").bufferedReader().readLines()
    }

    // PATCH: throttle
    private var lastAnalyzeWallTime = 0L
    private val analyzeCooldownMs = 350L

    // ===== Public setters =====
    fun updateDescription(text: String) { _uiState.value = _uiState.value.copy(description = text) }
    fun setDate(date: LocalDate) { _uiState.value = _uiState.value.copy(selectedDate = date) }
    fun setTime(time: LocalTime) { _uiState.value = _uiState.value.copy(selectedTime = time) }

    fun setAutoAddress(address: String, lat: Double?, lng: Double?) {
        val cur = _uiState.value
        if (cur.hasManualSelection) return
        _uiState.value = cur.copy(selectedAddress = address, selectedLat = lat, selectedLng = lng)
    }

    fun setManualAddress(address: String, lat: Double?, lng: Double?) {
        _uiState.value = _uiState.value.copy(
            selectedAddress = address,
            selectedLat = lat,
            selectedLng = lng,
            hasManualSelection = true
        )
    }

    // PATCH: tunables
    fun setScoreThreshold(th: Float) { _uiState.value = _uiState.value.copy(scoreThreshold = th.coerceIn(0f, 1f)) }
    fun setIouThreshold(v: Float) { _uiState.value = _uiState.value.copy(iouThreshold = v.coerceIn(0f, 1f)) }
    fun setMinBox(px: Float) { _uiState.value = _uiState.value.copy(minBoxPx = max(0f, px)) }
    fun toggleLabelFilter(label: String) {
        val cur = _uiState.value.labelFilter.toMutableSet()
        if (cur.contains(label)) cur.remove(label) else cur.add(label)
        _uiState.value = _uiState.value.copy(labelFilter = cur)
    }

    /** 갤러리 Uri들을 순차 감지 → 첫 성공시 반환. NMS/필터/워터마크까지 적용 (PATCH). */
    fun analyzeImagesForPet(imageUris: List<Uri>) {
        viewModelScope.launch {
            val now = SystemClock.elapsedRealtime()
            if (now - lastAnalyzeWallTime < analyzeCooldownMs) return@launch
            lastAnalyzeWallTime = now

            Log.d("YOLO11N", "analyzeImagesForPet(): start, count=${imageUris.size}")
            if (imageUris.isEmpty()) {
                _uiState.value = _uiState.value.copy(
                    detectionChecked = true,
                    detectionMessage = "이미지가 없습니다.",
                    annotatedBitmap = null
                )
                return@launch
            }

            _uiState.value = _uiState.value.copy(
                detectionChecked = false,
                detectionMessage = "분석 중...",
                annotatedBitmap = null
            )

            val params = _uiState.value
            var detectedMsg = "강아지/고양이 없음"
            val tAll0 = SystemClock.elapsedRealtime()

            for ((idx, uri) in imageUris.withIndex()) {
                val tDec0 = SystemClock.elapsedRealtime()
                val bmp = withContext(Dispatchers.IO) { detector.decodeBitmapFromUri(uri) }
                val tDec = SystemClock.elapsedRealtime() - tDec0
                if (bmp == null) {
                    Log.w("YOLO11N", "image[$idx]: decode FAILED (t=${tDec}ms)")
                    continue
                } else {
                    Log.d("YOLO11N", "image[$idx]: decode OK ${bmp.width}x${bmp.height} (t=${tDec}ms)")
                }

                val tInf0 = SystemClock.elapsedRealtime()
                val raw = withContext(Dispatchers.Default) { detector.detect(bmp) }
                val tInf = SystemClock.elapsedRealtime() - tInf0
                Log.d("YOLO11N", "image[$idx]: inference done, raw=${raw.size} (t=${tInf}ms)")

                // PATCH: filter + NMS
                val filtered = raw.filter { d ->
                    val okScore = (d.score ?: 0f) >= params.scoreThreshold
                    val okLabel = params.labelFilter.isEmpty() || (d.label?.lowercase() in params.labelFilter)
                    val okSize = (d.right - d.left) >= params.minBoxPx && (d.bottom - d.top) >= params.minBoxPx
                    okScore && okLabel && okSize
                }
                val dets = nonMaxSuppression(filtered, params.iouThreshold)

                if (dets.isNotEmpty()) {
                    dets.forEachIndexed { i, d ->
                        val xmin = d.left.coerceIn(0f, (bmp.width - 1).toFloat()).roundToInt()
                        val ymin = d.top.coerceIn(0f, (bmp.height - 1).toFloat()).roundToInt()
                        val xmax = d.right.coerceIn(0f, (bmp.width - 1).toFloat()).roundToInt()
                        val ymax = d.bottom.coerceIn(0f, (bmp.height - 1).toFloat()).roundToInt()
                        Log.d("YOLO11N", "[$i] label=${d.label}, conf=${"%.3f".format(d.score)}, box=($xmin,$ymin,$xmax,$ymax)")
                    }

                    // ✅ 품종 분류 (dog & cat 각각)
                    val (dogResults, catResults) = withContext(Dispatchers.Default) {
                        val dogs = asyncClassifyDogs(bmp, dets, minProb = 0.30f, inputSize = 456)
                        val cats = asyncClassifyCats(bmp, dets, minProb = 0.30f, inputSize = 224)
                        Pair(dogs, cats)
                    }

                    // 라벨/스코어/품종 + 둥근배경 (PATCH)
                    val annotated = withContext(Dispatchers.Default) {
                        val base = drawDetectionsOnBitmapWithBreedRounded(bmp, dets, dogResults, catResults)
                        addWatermarkBottomRight(base, tInf + tDec)
                        base
                    }

                    detectedMsg = buildString {
                        append("감지됨: ")
                        append(dets.joinToString { "${it.label} ${"%.2f".format(it.score)}" })
                        if (dogResults.isNotEmpty()) {
                            append(" | 개 품종: ")
                            append(dogResults.joinToString { "${it.breedLabel} ${"%.2f".format(it.breedProb)}" })
                        }
                        if (catResults.isNotEmpty()) {
                            append(" | 고양이 품종: ")
                            append(catResults.joinToString { "${it.breedLabel} ${"%.2f".format(it.breedProb)}" })
                        }
                        append(" (idx=$idx)")
                    }

                    val elapsed = SystemClock.elapsedRealtime() - tAll0
                    _uiState.value = _uiState.value.copy(
                        detectionChecked = true,
                        detectionMessage = detectedMsg,
                        annotatedBitmap = annotated,
                        dogResults = dogResults,                // ✅ 저장
                        catResults = catResults,                // ✅ 저장
                        lastAnalyzeMs = elapsed
                    )
                    return@launch
                } else {
                    Log.d("YOLO11N", "image[$idx]: NO DETECTION")
                }
            }

            val tAll = SystemClock.elapsedRealtime() - tAll0
            Log.d("YOLO11N", "analyzeImagesForPet(): end, total=${tAll}ms, success=false, message=$detectedMsg")

            _uiState.value = _uiState.value.copy(
                detectionChecked = true,
                detectionMessage = detectedMsg,
                annotatedBitmap = null,
                lastAnalyzeMs = tAll
            )
        }
    }

    /** 제보 등록 (이미 업로드된 URL 사용) */
    fun submitSighting(
        imageUrls: List<String>,
        onSuccess: (SightingRes) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val s = _uiState.value
        val lat = s.selectedLat
        val lng = s.selectedLng
        if (lat == null || lng == null) {
            onFailure("좌표가 없습니다. 위치를 선택해주세요.")
            return
        }

        val address = s.selectedAddress.ifBlank { "주소 미확인" }
        val localDt = LocalDateTime.of(s.selectedDate, s.selectedTime)
        val instantUtc = localDt.atZone(ZoneId.systemDefault()).toInstant()
        val sightedAtIsoUtc = instantUtc.toString()

        val images = imageUrls.mapIndexed { idx, url ->
            CreateSightingImageReq(src = url, sort = idx + 1)
        }

        val req = CreateSightingReq(
            regionId = user.regionId,
            address = address,
            latitude = lat,
            longitude = lng,
            content = s.description,
            sightedAt = sightedAtIsoUtc,
            images = images
        )

        viewModelScope.launch {
            _uiState.value = s.copy(loading = true, error = null)
            repo.createSighting(req)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(loading = false, submitted = true)
                    onSuccess(it)
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(loading = false, error = e.message)
                    onFailure(e.message ?: "등록에 실패했습니다.")
                }
        }
    }

    /** Uri들을 서버에 업로드 → URL 리스트 획득 → 제보 등록 */
    fun submitSightingFromUris(
        imageUris: List<Uri>,
        onSuccess: (SightingRes) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val s = _uiState.value
        viewModelScope.launch {
            _uiState.value = s.copy(loading = true, error = null)
            val urls = runCatching { imageRepo.uploadImages(imageUris) }.getOrElse { e ->
                _uiState.value = s.copy(loading = false, error = e.message)
                onFailure(e.message ?: "이미지 업로드에 실패했습니다.")
                return@launch
            }
            submitSighting(
                imageUrls = urls,
                onSuccess = onSuccess,
                onFailure = onFailure
            )
        }
    }

    // ===== Overlay (PATCH: rounded bg, bold, anti alias) =====
    private fun drawDetectionsOnBitmapWithBreedRounded(
        src: Bitmap,
        dets: List<Detection>,
        dogResults: List<DogResult>,
        catResults: List<CatResult>              // ✅ 추가
    ): Bitmap {
        val out = src.copy(Bitmap.Config.ARGB_8888, true)
        val c = Canvas(out)

        val strokeW = max(3f, out.width * 0.004f)
        val textSize = max(22f, out.width * 0.035f)
        val padX = max(10f, out.width * 0.010f)
        val padY = max(6f, out.width * 0.006f)
        val corner = max(8f, out.width * 0.012f)

        val boxFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = Color.argb(90, 0, 153, 255)
        }
        val boxStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = strokeW
            color = Color.argb(230, 0, 153, 255)
        }
        val textBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = Color.argb(190, 0, 0, 0)
        }
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            this.textSize = textSize
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }

        val bounds = Rect()
        fun iou(a: RectF, b: RectF): Float {
            val ix = max(0f, min(a.right, b.right) - max(a.left, b.left))
            val iy = max(0f, min(a.bottom, b.bottom) - max(a.top, b.top))
            val inter = ix * iy
            val ua = a.width() * a.height() + b.width() * b.height() - inter
            return if (ua <= 0f) 0f else inter / ua
        }

        dets.forEach { d ->
            val r = RectF(d.left, d.top, d.right, d.bottom)
            c.drawRoundRect(r, corner, corner, boxFill)
            c.drawRoundRect(r, corner, corner, boxStroke)

            var label = "${d.label} ${(d.score * 100).roundToInt()}%"

            if (d.label.equals("dog", true) && dogResults.isNotEmpty()) {
                val best = dogResults.maxByOrNull { iou(r, it.box) }
                if (best != null && iou(r, best.box) > 0.3f) {
                    label += " · ${best.breedLabel} ${(best.breedProb * 100).roundToInt()}%"
                }
            }
            // ✅ 고양이일 때 품종 표시
            if (d.label.equals("cat", true) && catResults.isNotEmpty()) {
                val best = catResults.maxByOrNull { iou(r, it.box) }
                if (best != null && iou(r, best.box) > 0.3f) {
                    label += " · ${best.breedLabel} ${(best.breedProb * 100).roundToInt()}%"
                }
            }

            textPaint.getTextBounds(label, 0, label.length, bounds)
            val bg = RectF(
                r.left,
                max(0f, r.top - bounds.height() - padY * 2),
                min(out.width.toFloat(), r.left + bounds.width() + padX * 2),
                r.top
            )
            c.drawRoundRect(bg, corner, corner, textBg)
            c.drawText(label, bg.left + padX, bg.bottom - padY * 0.6f, textPaint)
        }
        return out
    }

    // PATCH: watermark bottom-right
    private fun addWatermarkBottomRight(bitmap: Bitmap, elapsedMs: Long) {
        val c = Canvas(bitmap)
        val text = "Detected in ${elapsedMs}ms • ${LocalDateTime.now(ZoneId.of("Asia/Seoul"))}"
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = max(20f, bitmap.width * 0.025f)
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(160, 0, 0, 0)
            style = Paint.Style.FILL
        }
        val pad = max(10f, bitmap.width * 0.012f)
        val bounds = Rect()
        paint.getTextBounds(text, 0, text.length, bounds)
        val rect = RectF(
            bitmap.width - bounds.width() - pad * 2,
            bitmap.height - bounds.height() - pad * 2,
            bitmap.width.toFloat(),
            bitmap.height.toFloat()
        )
        val corner = max(8f, bitmap.width * 0.01f)
        c.drawRoundRect(rect, corner, corner, bg)
        c.drawText(text, rect.left + pad, rect.bottom - pad * 0.6f, paint)
    }

    // ===== NMS =====
    private fun nonMaxSuppression(dets: List<Detection>, iouTh: Float): List<Detection> {
        if (dets.isEmpty()) return emptyList()
        val sorted = dets.sortedByDescending { it.score ?: 0f }.toMutableList()
        val picked = mutableListOf<Detection>()
        fun iou(a: Detection, b: Detection): Float {
            val aRect = RectF(a.left, a.top, a.right, a.bottom)
            val bRect = RectF(b.left, b.top, b.right, b.bottom)
            val x1 = max(aRect.left, bRect.left)
            val y1 = max(aRect.top, bRect.top)
            val x2 = min(aRect.right, bRect.right)
            val y2 = min(aRect.bottom, bRect.bottom)
            val inter = max(0f, x2 - x1) * max(0f, y2 - y1)
            val union = aRect.width() * aRect.height() + bRect.width() * bRect.height() - inter
            return if (union <= 0f) 0f else inter / union
        }
        while (sorted.isNotEmpty()) {
            val cur = sorted.removeAt(0)
            picked.add(cur)
            val it = sorted.iterator()
            while (it.hasNext()) {
                val other = it.next()
                if (iou(cur, other) >= iouTh) it.remove()
            }
        }
        return picked
    }

    // ===== 품종 분류: 개 =====
    private suspend fun asyncClassifyDogs(
        bitmap: Bitmap,
        dets: List<Detection>,
        minProb: Float = 0.30f,
        inputSize: Int = 456
    ): List<DogResult> = withContext(Dispatchers.Default) {
        val dogs = dets.filter { (it.label ?: "").equals("dog", ignoreCase = true) }
        val results = mutableListOf<DogResult>()
        for (d in dogs) {
            runCatching {
                val bodyCrop = cropFromOriginal(bitmap, RectF(d.left, d.top, d.right, d.bottom), 0.15f)
                val tensor = preprocessLikeServer(bodyCrop, inputSize)  // FloatBuffer (NCHW)
                val shape = longArrayOf(1, 3, inputSize.toLong(), inputSize.toLong())
                OnnxTensor.createTensor(ortEnv, tensor, shape).use { input ->
                    dogBreedSession.run(mapOf(dogBreedSession.inputNames.first() to input)).use { out ->
                        val logits = (out[0].value as Array<FloatArray>)[0]
                        val probs = softmaxStable(logits)
                        val topIdx = topKIndices(probs, k = 5)
                        val bestIdx = topIdx.first()
                        val bestProb = probs[bestIdx]
                        if (bestProb >= minProb) {
                            val bestLabel = dogLabels.getOrNull(bestIdx) ?: "UNKNOWN"
                            results += DogResult(
                                box = RectF(d.left, d.top, d.right, d.bottom),
                                detScore = d.score,
                                breedLabel = bestLabel,
                                breedProb = bestProb
                            )
                        }
                    }
                }
            }.onFailure { e -> Log.i("$TAG-DOG-BREED", "classify failed: ${e.message}") }
        }
        results
    }

    // ===== 품종 분류: 고양이 =====
    private suspend fun asyncClassifyCats(
        bitmap: Bitmap,
        dets: List<Detection>,
        minProb: Float = 0.30f,
        inputSize: Int = 224        // ✅ catBreedModel 기본 224
    ): List<CatResult> = withContext(Dispatchers.Default) {
        val cats = dets.filter { (it.label ?: "").equals("cat", ignoreCase = true) }
        val results = mutableListOf<CatResult>()
        for (d in cats) {
            runCatching {
                val bodyCrop = cropFromOriginal(bitmap, RectF(d.left, d.top, d.right, d.bottom), 0.15f)
                // 고양이 모델 전처리: ImageNet mean/std + NCHW. Resize(shorter=SIZE+32)→CenterCrop(SIZE)
                val tensor = preprocessLikeServer(bodyCrop, inputSize)
                val shape = longArrayOf(1, 3, inputSize.toLong(), inputSize.toLong())
                OnnxTensor.createTensor(ortEnv, tensor, shape).use { input ->
                    catBreedSession.run(mapOf(catBreedSession.inputNames.first() to input)).use { out ->
                        val logits = (out[0].value as Array<FloatArray>)[0]
                        val probs = softmaxStable(logits)
                        val topIdx = topKIndices(probs, k = 5)
                        val bestIdx = topIdx.first()
                        val bestProb = probs[bestIdx]
                        if (bestProb >= minProb) {
                            val bestLabel = catLabels.getOrNull(bestIdx) ?: "UNKNOWN"
                            results += CatResult(
                                box = RectF(d.left, d.top, d.right, d.bottom),
                                detScore = d.score,
                                breedLabel = bestLabel,
                                breedProb = bestProb
                            )
                        }
                    }
                }
            }.onFailure { e -> Log.i("$TAG-CAT-BREED", "classify failed: ${e.message}") }
        }
        results
    }

    private fun preprocessLikeServer(src: Bitmap, size: Int): java.nio.FloatBuffer {
        // torchvision.Resize(size+32): 짧은 변을 size+32로 리사이즈(종횡비 유지)
        val resized = resizeShorterSide(src, size + 32)
        // CenterCrop(size)
        val cropped = centerCrop(resized, size, size)

        // ToTensor (0~1), Normalize(mean,std) with ImageNet stats, NCHW
        val mean = floatArrayOf(0.485f, 0.456f, 0.406f)
        val std  = floatArrayOf(0.229f, 0.224f, 0.225f)

        val w = cropped.width
        val h = cropped.height
        val pixels = IntArray(w * h)
        cropped.getPixels(pixels, 0, w, 0, 0, w, h)

        val buf = java.nio.FloatBuffer.allocate(1 * 3 * h * w)
        // 채널별로 NCHW 쓰기 (C 먼저)
        // C=0(R),1(G),2(B)
        for (c in 0..2) {
            for (y in 0 until h) {
                for (x in 0 until w) {
                    val p = pixels[y * w + x]
                    val v = when (c) {
                        0 -> ((p ushr 16) and 0xFF) / 255f
                        1 -> ((p ushr 8) and 0xFF) / 255f
                        else -> (p and 0xFF) / 255f
                    }
                    val norm = (v - mean[c]) / std[c]
                    buf.put(norm)
                }
            }
        }
        buf.rewind()
        return buf
    }

    private fun preprocessClsToNCHW(src: Bitmap, size: Int): FloatBuffer {
        val bmp = Bitmap.createScaledBitmap(src, size, size, true)
        val buf = FloatBuffer.allocate(1 * 3 * size * size)
        val px = IntArray(size * size)
        bmp.getPixels(px, 0, size, 0, 0, size, size)
        var idx = 0
        for (c in 0 until 3) {
            for (y in 0 until size) for (x in 0 until size) {
                val p = px[y * size + x]
                val v = when (c) {
                    0 -> (p ushr 16) and 0xFF
                    1 -> (p ushr 8) and 0xFF
                    else -> p and 0xFF
                }
                buf.put(idx++, v / 255f)
            }
        }
        buf.rewind()
        return buf
    }

    private fun argmaxSoftmax(logits: FloatArray): Pair<Int, Float> {
        // stable softmax using max logit
        var idx = 0
        var maxV = logits[0]
        for (i in 1 until logits.size) if (logits[i] > maxV) { maxV = logits[i]; idx = i }
        var sum = 0.0
        for (v in logits) sum += kotlin.math.exp((v - maxV).toDouble())
        val prob = kotlin.math.exp((logits[idx] - maxV).toDouble()) / sum
        return idx to prob.toFloat()
    }

    private fun cropFromOriginal(src: Bitmap, box: RectF, marginRatio: Float = 0.1f): Bitmap {
        val w = src.width.toFloat(); val h = src.height.toFloat()
        val bw = box.width(); val bh = box.height()
        val mx = bw * marginRatio; val my = bh * marginRatio
        val x1 = (box.left - mx).coerceIn(0f, w - 1)
        val y1 = (box.top - my).coerceIn(0f, h - 1)
        val x2 = (box.right + mx).coerceIn(1f, w)
        val y2 = (box.bottom + my).coerceIn(1f, h)
        val cw = (x2 - x1).toInt().coerceAtLeast(1)
        val ch = (y2 - y1).toInt().coerceAtLeast(1)
        return Bitmap.createBitmap(src, x1.toInt(), y1.toInt(), cw, ch)
    }

    override fun onCleared() {
        try {
            dogBreedSession.close()
            catBreedSession.close()     // ✅ 추가
            ortEnv.close()
        } catch (_: Throwable) { }
        super.onCleared()
    }

    private fun resizeShorterSide(src: Bitmap, shortSize: Int): Bitmap {
        val w = src.width
        val h = src.height
        if (w == 0 || h == 0) return src

        val scale = if (w < h) shortSize / w.toFloat() else shortSize / h.toFloat()
        val newW = (w * scale).toInt().coerceAtLeast(1)
        val newH = (h * scale).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(src, newW, newH, true)
    }

    private fun centerCrop(src: Bitmap, cropW: Int, cropH: Int): Bitmap {
        val w = src.width
        val h = src.height
        val x = ((w - cropW) / 2).coerceAtLeast(0)
        val y = ((h - cropH) / 2).coerceAtLeast(0)
        val cw = min(cropW, w)
        val ch = min(cropH, h)
        return Bitmap.createBitmap(src, x, y, cw, ch)
    }

    private fun softmaxStable(logits: FloatArray): FloatArray {
        var maxV = logits[0]
        for (i in 1 until logits.size) if (logits[i] > maxV) maxV = logits[i]
        var sum = 0.0
        val out = FloatArray(logits.size)
        for (i in logits.indices) {
            val v = kotlin.math.exp((logits[i] - maxV).toDouble())
            sum += v
            out[i] = v.toFloat()
        }
        val inv = (1.0 / sum).toFloat()
        for (i in out.indices) out[i] *= inv
        return out
    }

    private fun topKIndices(arr: FloatArray, k: Int): IntArray {
        val idx = arr.indices.toList().sortedByDescending { arr[it] }
        val kk = minOf(k, idx.size)
        return idx.take(kk).toIntArray()
    }

    // ✅ ONNX 출력 차원 점검: 개/고양이 각각
    init {
        try {
            fun checkOutput(session: OrtSession, labelSize: Int, tag: String) {
                val first = session.outputInfo.values.firstOrNull()
                if (first == null) {
                    Log.w("BREEDCHK-$tag", "출력 정보가 비어있습니다.")
                } else {
                    val ti = first.info
                    if (ti is TensorInfo) {
                        val dims: LongArray = ti.shape
                        val lastDim = dims.lastOrNull() ?: -1L
                        val numClassesFromOnnx =
                            if (lastDim > 0) lastDim.toInt()
                            else dims.filter { it > 0 }.lastOrNull()?.toInt() ?: -1

                        Log.i("BREEDCHK-$tag", "ONNX numClasses=$numClassesFromOnnx, labelLen=$labelSize, dims=${dims.joinToString("x")}")

                        if (numClassesFromOnnx != -1 && numClassesFromOnnx != labelSize) {
                            Log.w("BREEDCHK-$tag", "⚠️ 라벨 개수와 ONNX 출력 차원 불일치! 매핑 오류 가능성 큼")
                        }
                    } else {
                        Log.w("BREEDCHK-$tag", "TensorInfo 아님: ${ti?.javaClass?.simpleName}")
                    }
                }
            }
            checkOutput(dogBreedSession, dogLabels.size, "DOG")
            checkOutput(catBreedSession, catLabels.size, "CAT")
        } catch (e: Throwable) {
            Log.w("BREEDCHK", "출력차원 파싱 실패: ${e.message}")
        }
    }
}
