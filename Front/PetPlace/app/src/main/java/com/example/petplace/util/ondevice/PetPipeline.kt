package com.example.petplace.util.ondevice

import android.content.Context
import android.graphics.*
import ai.onnxruntime.*
import java.nio.FloatBuffer
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min

data class Det(val cls:Int, val score:Float, val box:RectF) // 기존 탐지 결과와 동일 형식 가정
data class DogResult(val box: RectF, val detScore: Float, val breedLabel: String, val breedProb: Float)

class PetPipeline(private val context: Context) {
    private val env = OrtEnvironment.getEnvironment()

    private val breedSession: OrtSession by lazy {
        val bytes = context.assets.open("dogBreedModel.onnx").use { it.readBytes() }
        val so = OrtSession.SessionOptions().apply {
            setOptimizationLevel(OrtSession.SessionOptions.OptLevel.ALL_OPT)
            setIntraOpNumThreads(4)
        }
        env.createSession(bytes, so)
    }

    private val dogLabels: List<String> by lazy {
        context.assets.open("dogLabels.txt").bufferedReader().readLines()
    }

    fun classifyDogsFromDetections(
        bitmap: Bitmap,
        dets: List<Det>,
        minProb: Float = 0.30f, // 낮추면 덜 확신에도 표시됨
        inputSize: Int = 224    // dogBreedModel 입력 사이즈(맞게 변경)
    ): List<DogResult> {
        val dogs = dets.filter { it.cls == 1 } // 1=dog 가정
        val results = mutableListOf<DogResult>()
        for (d in dogs) {
            val crop = cropFromOriginal(bitmap, d.box, 0.1f)
            classifyBreed(crop, inputSize)?.let { (idx, prob) ->
                if (prob >= minProb) {
                    val label = dogLabels.getOrNull(idx) ?: "UNKNOWN"
                    results += DogResult(d.box, d.score, label, prob)
                }
            }
        }
        return results
    }

    private fun classifyBreed(crop: Bitmap, size:Int): Pair<Int, Float>? {
        val tensor = preprocessClsToNCHW(crop, size)
        val shape = longArrayOf(1, 3, size.toLong(), size.toLong())
        OnnxTensor.createTensor(env, tensor, shape).use { input ->
            breedSession.run(mapOf(breedSession.inputNames.first() to input)).use { out ->
                // [1, C] 가정
                val logits = (out[0].value as Array<FloatArray>)[0]
                return argmaxSoftmax(logits)
            }
        }
    }

    // ========== 전처리/후처리/유틸 ==========
    private fun preprocessClsToNCHW(src: Bitmap, size:Int): FloatBuffer {
        val bmp = Bitmap.createScaledBitmap(src, size, size, true)
        val buf = FloatBuffer.allocate(1 * 3 * size * size)
        val px = IntArray(size*size)
        bmp.getPixels(px,0,size,0,0,size,size)
        var idx=0
        for (c in 0 until 3) {
            for (y in 0 until size) for (x in 0 until size) {
                val p = px[y*size+x]
                val v = when(c){0->((p ushr 16) and 0xFF);1->((p ushr 8) and 0xFF);else->(p and 0xFF)}
                buf.put(idx++, v/255f) // 필요시 mean/std 정규화 추가
            }
        }
        buf.rewind()
        return buf
    }

    private fun argmaxSoftmax(logits: FloatArray): Pair<Int, Float> {
        var maxV = Float.NEGATIVE_INFINITY; var idx = -1
        for (i in logits.indices) if (logits[i] > maxV) { maxV = logits[i]; idx = i }
        val shift = logits.max()
        var sum = 0.0
        for (v in logits) sum += exp((v - shift).toDouble())
        val prob = exp((logits[idx] - shift).toDouble()) / sum
        return idx to prob.toFloat()
    }

    private fun cropFromOriginal(src: Bitmap, box: RectF, marginRatio: Float): Bitmap {
        val w = src.width.toFloat(); val h = src.height.toFloat()
        val bw = box.width(); val bh = box.height()
        val mx = bw * marginRatio; val my = bh * marginRatio
        val x1 = (box.left - mx).coerceIn(0f, w-1)
        val y1 = (box.top  - my).coerceIn(0f, h-1)
        val x2 = (box.right + mx).coerceIn(1f, w)
        val y2 = (box.bottom+ my).coerceIn(1f, h)
        val cw = (x2 - x1).toInt().coerceAtLeast(1)
        val ch = (y2 - y1).toInt().coerceAtLeast(1)
        return Bitmap.createBitmap(src, x1.toInt(), y1.toInt(), cw, ch)
    }
}
