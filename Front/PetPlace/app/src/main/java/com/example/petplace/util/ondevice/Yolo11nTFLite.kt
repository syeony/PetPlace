package com.example.petplace.presentation.feature.missing_report

import android.content.Context
import android.graphics.*
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
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

/**
 * YOLO11n TFLite 러너 (dog/cat 2클래스 가정)
 * - 입력: Bitmap (임의 크기) → letterbox(모델 입력 크기) → 추론 → NMS → 원본 좌표계로 복원
 * - 출력: List<Detection> (label, score, xmin/ymin/xmax/ymax)
 */
class Yolo11nTFLite(private val context: Context) {

    private var inputW = 640
    private var inputH = 640
    private var inputC = 3

    companion object {
        private const val TAG = "YOLO11N"
        private const val MODEL_NAME = "yolo11n_float32.tflite"
        private const val NUM_THREADS = 4

        // 감지/후처리 파라미터
        private const val SCORE_THRESH = 0.55f      // 0.25 -> 0.55 (필요시 0.6~0.7로 조정)
        private const val NMS_IOU = 0.50f           // 0.45 -> 0.50
        private const val MIN_BOX_SIDE_PX = 12f     // 너무 작은 박스 제거(원본 기준)
        private const val MAX_DETS = 30             // 최종 최대 개수 제한

        // 모델 학습 라벨 순서에 맞춰 필요시 수정 (예: listOf("dog","cat"))
        private val LABELS = listOf("cat", "dog")
        private val TARGET_LABELS = setOf("cat", "dog")

        // 모델이 (cx, cy, w, h) 형식으로 출력하면 true, 이미 xyxy면 false
        private const val IS_XYWH = true
    }

    private val interpreter: Interpreter by lazy {
        val opts = Interpreter.Options().apply {
            setNumThreads(NUM_THREADS)
            // setUseNNAPI(true) // 필요 시
        }
        val it = Interpreter(loadModelFile(context, MODEL_NAME), opts)

        // 입력 텐서 모양을 모델에서 동적으로 읽어 입력 크기 자동 맞춤
        try {
            val inShape = it.getInputTensor(0).shape() // 예: [1, 640, 640, 3] 또는 [1, 3, 640, 640]
            if (inShape.size >= 4) {
                if (inShape[1] == 3) {
                    // NCHW
                    inputC = inShape[1]
                    inputH = inShape[2]
                    inputW = inShape[3]
                } else {
                    // NHWC
                    inputH = inShape[1]
                    inputW = inShape[2]
                    inputC = inShape[3]
                }
            }
        } catch (t: Throwable) {
            Log.w(TAG, "Failed to read input tensor shape, fallback to $inputW x $inputH", t)
        }
        Log.d(TAG, "Model input: ${inputW}x${inputH}x$inputC")
        it
    }

    private fun loadModelFile(ctx: Context, assetName: String): ByteBuffer {
        val afd = ctx.assets.openFd(assetName)
        FileInputStream(afd.fileDescriptor).use { fis ->
            val fc: FileChannel = fis.channel
            val mapped = fc.map(FileChannel.MapMode.READ_ONLY, afd.startOffset, afd.declaredLength)
                .order(ByteOrder.nativeOrder())
            // 리소스 경고 방지
            afd.close()
            return mapped
        }
    }

    @Suppress("DEPRECATION")
    fun decodeBitmapFromUri(uri: Uri): Bitmap? {
        return try {
            Log.d(TAG, "decodeBitmapFromUri() uri=$uri")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val src = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(src) { decoder, info, _ ->
                    // HEIC에서도 안전하게: 소프트웨어 디코더 + 타겟 사이즈 제한
                    decoder.isMutableRequired = false
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE

                    val w = info.size.width
                    val h = info.size.height
                    val maxSide = 1280
                    val scale = (max(w, h) / maxSide.toFloat()).coerceAtLeast(1f)
                    val tw = (w / scale).roundToInt().coerceAtLeast(1)
                    val th = (h / scale).roundToInt().coerceAtLeast(1)
                    decoder.setTargetSize(tw, th)
                }.also { bmp ->
                    Log.d(TAG, "decode OK ${bmp.width}x${bmp.height}")
                }
            } else {
                // 하위 버전: inSampleSize 계산 후 디코드
                val cr = context.contentResolver
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                cr.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }

                val maxSide = 1280
                var inSample = 1
                var w = bounds.outWidth
                var h = bounds.outHeight
                while (w / inSample > maxSide || h / inSample > maxSide) inSample *= 2

                val opts = BitmapFactory.Options().apply {
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                    inSampleSize = inSample
                }
                cr.openInputStream(uri)?.use { input ->
                    BitmapFactory.decodeStream(input, null, opts)?.also { bmp ->
                        Log.d(TAG, "decode OK ${bmp.width}x${bmp.height} (inSample=$inSample)")
                    }
                }
            }
        } catch (t: Throwable) {
            Log.e(TAG, "decode failed for $uri", t)
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

    // 1) letterbox가 대상 크기를 받도록 수정
    private fun letterbox(bitmap: Bitmap, dstW: Int, dstH: Int): LetterboxResult {
        val inW = bitmap.width
        val inH = bitmap.height
        val scale = min(dstW.toFloat() / inW, dstH.toFloat() / inH)
        val newW = (inW * scale).toInt()
        val newH = (inH * scale).toInt()
        val padX = (dstW - newW) / 2f
        val padY = (dstH - newH) / 2f

        val out = Bitmap.createBitmap(dstW, dstH, Bitmap.Config.ARGB_8888)
        Canvas(out).apply {
            drawColor(Color.BLACK)
            val src = Rect(0, 0, inW, inH)
            val dst = RectF(padX, padY, padX + newW, padY + newH)
            drawBitmap(bitmap, src, dst, Paint(Paint.ANTI_ALIAS_FLAG))
        }

        val buf = ByteBuffer.allocateDirect(1 * dstW * dstH * 3 * 4)
            .order(ByteOrder.nativeOrder())
        val pixels = IntArray(dstW * dstH)
        out.getPixels(pixels, 0, dstW, 0, 0, dstW, dstH)
        var i = 0
        for (y in 0 until dstH) {
            for (x in 0 until dstW) {
                val p = pixels[i++]
                buf.putFloat(((p shr 16) and 0xFF) / 255f)
                buf.putFloat(((p shr 8) and 0xFF) / 255f)
                buf.putFloat((p and 0xFF) / 255f)
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
    private fun toProb(x: Float): Float = if (x in 0f..1f) x else (1f / (1f + kotlin.math.exp(-x)))

    fun detect(bitmap: Bitmap): List<Detection> {
        val t0 = System.currentTimeMillis()

        // 1) 입력 텐서 크기 먼저 확보
        val it = interpreter
        runCatching {
            val s = it.getInputTensor(0).shape()
            if (s.size >= 4) {
                if (s[1] == 3) { inputC = s[1]; inputH = s[2]; inputW = s[3] }  // NCHW
                else            { inputH = s[1]; inputW = s[2]; inputC = s[3] }  // NHWC
            }
        }
        Log.d(TAG, "Model input: ${inputW}x${inputH}x${inputC}")

        // 2) 모델 입력 크기로 전처리
        val lb = letterbox(bitmap, inputW, inputH)  // ← 이전에 수정한 버전 사용
        val inputArray = arrayOf<Any>(lb.input)

        // 3) 출력 텐서 하나만 쓴다고 가정 (필요 시 확장 가능)
        val outShape = it.getOutputTensor(0).shape()
        val d1 = outShape.getOrNull(1) ?: 0
        val d2 = outShape.getOrNull(2) ?: 0

        // [1,N,C] 또는 [1,C,N]을 [N][C]로 통일
        val preds: Array<FloatArray> = when {
            d1 >= 6 && d2 >= 6 && d1 > d2 -> {
                val N = d1; val C = d2
                val out = Array(1) { Array(N) { FloatArray(C) } }
                it.runForMultipleInputsOutputs(inputArray, hashMapOf(0 to out) as Map<Int, Any>)
                out[0]
            }
            d1 >= 6 && d2 >= 6 -> {
                val C = d1; val N = d2
                val out = Array(1) { Array(C) { FloatArray(N) } }
                it.runForMultipleInputsOutputs(inputArray, hashMapOf(0 to out) as Map<Int, Any>)
                Array(N) { n -> FloatArray(C) { c -> out[0][c][n] } }
            }
            else -> {
                Log.e(TAG, "Unexpected output shape: ${outShape.contentToString()}")
                return emptyList()
            }
        }

        // preds 만든 바로 다음 줄에 넣기
        val perLen = preds[0].size
        val numClasses = perLen - 4  // v8/11: [cx,cy,w,h] + classes

        val raw = mutableListOf<Detection>()
        for (i in preds.indices) {
            val p = preds[i]

            // 1) v8/11 방식: objectness 없음 → 클래스 점수만
            var bestIdx = 0
            var conf = 0f
            for (c in 0 until numClasses) {
                val sc = sigmoid(p[4 + c])
                if (sc > conf) { conf = sc; bestIdx = c }
            }
            if (conf < SCORE_THRESH) continue

            // 2) cat/dog만 유지 (COCO일 때 cat=15, dog=16)
            val isCoco = numClasses >= 80
            if (isCoco && bestIdx != 15 && bestIdx != 16) continue
            val label = if (isCoco) {
                if (bestIdx == 15) "cat" else "dog"
            } else {
                // 2클래스 커스텀 모델일 때는 기존 LABELS 사용
                LABELS.getOrNull(bestIdx) ?: bestIdx.toString()
            }

            // 3) bbox: cx,cy,w,h → x1,y1,x2,y2
            val cx = p[0]; val cy = p[1]; val ww = p[2]; val hh = p[3]
            val x1i = cx - ww / 2f
            val y1i = cy - hh / 2f
            val x2i = cx + ww / 2f
            val y2i = cy + hh / 2f

            // 4) letterbox → 원본 좌표
            val x1 = ((x1i - lb.padX) / lb.scale).coerceIn(0f, (lb.outW - 1).toFloat())
            val y1 = ((y1i - lb.padY) / lb.scale).coerceIn(0f, (lb.outH - 1).toFloat())
            val x2 = ((x2i - lb.padX) / lb.scale).coerceIn(0f, (lb.outW - 1).toFloat())
            val y2 = ((y2i - lb.padY) / lb.scale).coerceIn(0f, (lb.outH - 1).toFloat())

            val bw = x2 - x1
            val bh = y2 - y1
            if (bw < MIN_BOX_SIDE_PX || bh < MIN_BOX_SIDE_PX) continue

            if (x2 > x1 && y2 > y1) {
                raw.add(Detection(label = label, score = conf, left = x1, top = y1, right = x2, bottom = y2))
            }
        }

        val kept = nms(raw.toMutableList(), NMS_IOU)
            .sortedByDescending { it.score }
            .take(MAX_DETS)


        val t1 = System.currentTimeMillis()
        Log.d(TAG, "inference done, detCount=${kept.size} (t=${t1 - t0}ms)")
        kept.take(10).forEachIndexed { i, d ->
            Log.d(TAG, "[$i] label=${d.label}, wFace=${"%.3f".format(d.score.toDouble())}, " +
                    "box=(${d.left.roundToInt()},${d.top.roundToInt()},${d.right.roundToInt()},${d.bottom.roundToInt()})")
        }
        return kept
    }

}
