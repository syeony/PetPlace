package com.example.petplace.presentation.feature.missing_report

import android.content.Context
import android.graphics.*
import android.net.Uri
import android.util.Log
import com.example.petplace.data.local.onDevice.Detection
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import android.os.Build
import android.graphics.ImageDecoder
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * YOLO11n TFLite 러너 (dog/cat 2클래스 가정)
 * - 입력: Bitmap (임의 크기) → letterbox(640) → 추론 → NMS → 원본 좌표계로 복원
 * - 출력: List<Detection> (label, score, xmin/ymin/xmax/ymax)
 */
class Yolo11nTFLite(private val context: Context) {

    companion object {
        private const val TAG = "YOLO11N"
        private const val MODEL_NAME = "yolo11n_float32.tflite"
        private const val INPUT_SIZE = 640
        private const val NUM_THREADS = 4
        private const val SCORE_THRESH = 0.25f
        private const val NMS_IOU = 0.45f

        // 모델 학습 라벨 순서에 맞춰 필요시 순서를 바꿔주세요 (예: dog, cat)
        private val LABELS = listOf("cat", "dog")

        // 모델이 (cx, cy, w, h) 형식으로 출력한다고 가정 (xyxy면 false로)
        private const val IS_XYWH = true
    }

    private val interpreter: Interpreter by lazy {
        val opts = Interpreter.Options().apply {
            setNumThreads(NUM_THREADS)
            // setUseNNAPI(true) // 필요 시
        }
        Interpreter(loadModelFile(context, MODEL_NAME), opts)
    }

    private fun loadModelFile(ctx: Context, assetName: String): ByteBuffer {
        val afd = ctx.assets.openFd(assetName)
        FileInputStream(afd.fileDescriptor).use { fis ->
            val fc: FileChannel = fis.channel
            return fc.map(FileChannel.MapMode.READ_ONLY, afd.startOffset, afd.declaredLength)
                .order(ByteOrder.nativeOrder())
        }
    }

    @Suppress("DEPRECATION")
    fun decodeBitmapFromUri(uri: Uri): Bitmap? {
        return try {
            Log.d("YOLO11N", "decodeBitmapFromUri() uri=$uri")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val src = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(src) { decoder, info, _ ->
                    // HEIC에서도 안전하게 동작하도록 소프트웨어 디코더 + 타겟 사이즈 제한
                    decoder.isMutableRequired = false
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE

                    val w = info.size.width
                    val h = info.size.height
                    val maxSide = 1280 // 추론 속도/메모리 밸런스
                    val scale = (max(w, h) / maxSide.toFloat()).coerceAtLeast(1f)
                    val tw = (w / scale).roundToInt().coerceAtLeast(1)
                    val th = (h / scale).roundToInt().coerceAtLeast(1)
                    decoder.setTargetSize(tw, th)
                }.also { bmp ->
                    Log.d("YOLO11N", "decode OK ${bmp.width}x${bmp.height}")
                }
            } else {
                // 하위 버전: 두 번 열어서 inSampleSize 계산 후 디코드
                val cr = context.contentResolver

                // 1) 크기만 확인
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                cr.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }

                val maxSide = 1280
                var inSample = 1
                var w = bounds.outWidth
                var h = bounds.outHeight
                while (w / inSample > maxSide || h / inSample > maxSide) inSample *= 2

                // 2) 실제 디코드
                val opts = BitmapFactory.Options().apply {
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                    inSampleSize = inSample
                }
                cr.openInputStream(uri)?.use { input ->
                    BitmapFactory.decodeStream(input, null, opts)?.also { bmp ->
                        Log.d("YOLO11N", "decode OK ${bmp.width}x${bmp.height} (inSample=$inSample)")
                    }
                }
            }
        } catch (t: Throwable) {
            Log.e("YOLO11N", "decode failed for $uri", t)
            null
        }
    }


    /** Letterbox 결과 + 좌표 복원에 필요한 정보 */
    private data class LetterboxResult(
        val input: ByteBuffer,
        val scale: Float,
        val padX: Float,
        val padY: Float,
        val outW: Int, // 원본(또는 축소본) 폭
        val outH: Int  // 원본(또는 축소본) 높이
    )

    /** 비율 유지 패딩(640x640) + Float32 [1,640,640,3] 전처리 */
    private fun letterbox(bitmap: Bitmap): LetterboxResult {
        val inW = bitmap.width
        val inH = bitmap.height
        val scale = min(INPUT_SIZE.toFloat() / inW, INPUT_SIZE.toFloat() / inH)
        val newW = (inW * scale).toInt()
        val newH = (inH * scale).toInt()
        val padX = (INPUT_SIZE - newW) / 2f
        val padY = (INPUT_SIZE - newH) / 2f

        val out = Bitmap.createBitmap(INPUT_SIZE, INPUT_SIZE, Bitmap.Config.ARGB_8888)
        Canvas(out).apply {
            drawColor(Color.BLACK)
            val src = Rect(0, 0, inW, inH)
            val dst = RectF(padX, padY, padX + newW, padY + newH)
            drawBitmap(bitmap, src, dst, Paint(Paint.ANTI_ALIAS_FLAG))
        }

        val buf = ByteBuffer.allocateDirect(1 * INPUT_SIZE * INPUT_SIZE * 3 * 4)
            .order(ByteOrder.nativeOrder())
        val pixels = IntArray(INPUT_SIZE * INPUT_SIZE)
        out.getPixels(pixels, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE)
        var i = 0
        for (y in 0 until INPUT_SIZE) {
            for (x in 0 until INPUT_SIZE) {
                val p = pixels[i++]
                buf.putFloat(((p shr 16) and 0xFF) / 255f) // R
                buf.putFloat(((p shr 8) and 0xFF) / 255f)  // G
                buf.putFloat((p and 0xFF) / 255f)          // B
            }
        }
        buf.rewind()
        return LetterboxResult(buf, scale, padX, padY, inW, inH)
    }

    private fun sigmoid(x: Float): Float =
        (1.0 / (1.0 + kotlin.math.exp((-x).toDouble()))).toFloat()

    private fun iou(a: Detection, b: Detection): Float {
        val x1 = max(a.left, b.left)
        val y1 = max(a.top, b.top)
        val x2 = min(a.right, b.right)
        val y2 = min(a.bottom, b.bottom)
        val inter = max(0f, x2 - x1) * max(0f, y2 - y1)
        val areaA = (a.right - a.left) * (a.bottom - a.top)
        val areaB = (b.right - b.left) * (b.bottom - b.top)
        val union = areaA + areaB - inter
        return if (union <= 0f) 0f else inter / union
    }

    private fun nms(boxes: MutableList<Detection>, iouThresh: Float): List<Detection> {
        boxes.sortByDescending { it.score }
        val kept = mutableListOf<Detection>()
        val removed = BooleanArray(boxes.size)
        for (i in boxes.indices) {
            if (removed[i]) continue
            val a = boxes[i]
            kept.add(a)
            for (j in i + 1 until boxes.size) {
                if (!removed[j] && iou(a, boxes[j]) > iouThresh) {
                    removed[j] = true
                }
            }
        }
        return kept
    }

    /**
     * 추론: 입력 비트맵 → 결과는 원본(혹은 decode 시 축소된) 좌표계로 반환
     * 출력 텐서가 [1,N,C] 또는 [1,C,N] 모두 처리
     */
    fun detect(bitmap: Bitmap): List<Detection> {
        val lb = letterbox(bitmap)
        val inputArray = arrayOf<Any>(lb.input)

        val outShape = interpreter.getOutputTensor(0).shape() // e.g. [1, 8400, 7] or [1, 7, 8400]
        val dim1 = outShape.getOrNull(1) ?: 0
        val dim2 = outShape.getOrNull(2) ?: 0

        val preds: Array<FloatArray> = when {
            dim1 >= 6 && dim2 >= 6 && dim1 > dim2 -> {
                // [1, N, C]
                val N = dim1
                val C = dim2
                val out = Array(1) { Array(N) { FloatArray(C) } }
                interpreter.runForMultipleInputsOutputs(inputArray,
                    hashMapOf(0 to out) as Map<Int, Any>
                )
                out[0]
            }
            dim1 >= 6 && dim2 >= 6 -> {
                // [1, C, N]
                val C = dim1
                val N = dim2
                val out = Array(1) { Array(C) { FloatArray(N) } }
                interpreter.runForMultipleInputsOutputs(inputArray,
                    hashMapOf(0 to out) as Map<Int, Any>
                )
                // transpose to [N][C]
                Array(N) { n -> FloatArray(C) { c -> out[0][c][n] } }
            }
            else -> {
                Log.e(TAG, "Unexpected output shape: ${outShape.contentToString()}")
                return emptyList()
            }
        }

        val numClasses = preds.firstOrNull()?.size?.minus(5) ?: 0
        if (numClasses <= 0) return emptyList()

        val raw = mutableListOf<Detection>()
        for (i in preds.indices) {
            val p = preds[i]
            val obj = sigmoid(p[4])
            if (obj < SCORE_THRESH) continue

            // best class
            var bestIdx = 0
            var bestCls = 0f
            for (c in 0 until numClasses) {
                val sc = sigmoid(p[5 + c])
                if (sc > bestCls) { bestCls = sc; bestIdx = c }
            }
            val conf = obj * bestCls
            if (conf < SCORE_THRESH) continue

            val label = LABELS.getOrNull(bestIdx) ?: bestIdx.toString()

            // bbox (xywh or xyxy in 640 space)
            val (x1i, y1i, x2i, y2i) =
                if (IS_XYWH) {
                    val cx = p[0]; val cy = p[1]; val ww = p[2]; val hh = p[3]
                    floatArrayOf(cx - ww / 2f, cy - hh / 2f, cx + ww / 2f, cy + hh / 2f)
                } else {
                    floatArrayOf(p[0], p[1], p[2], p[3])
                }

            // letterbox → 원본 좌표
            val x1 = ((x1i - lb.padX) / lb.scale).coerceIn(0f, (lb.outW - 1).toFloat())
            val y1 = ((y1i - lb.padY) / lb.scale).coerceIn(0f, (lb.outH - 1).toFloat())
            val x2 = ((x2i - lb.padX) / lb.scale).coerceIn(0f, (lb.outW - 1).toFloat())
            val y2 = ((y2i - lb.padY) / lb.scale).coerceIn(0f, (lb.outH - 1).toFloat())

            if (x2 > x1 && y2 > y1) {
                raw.add(
                    Detection(
                        label = label,
                        score = conf,
                        left = x1,
                        top = y1,
                        right = x2,
                        bottom = y2
                    )
                )
            }
        }

        val filtered = raw.filter { it.label == "cat" || it.label == "dog" }
        return nms(filtered.toMutableList(), NMS_IOU)
    }
}
