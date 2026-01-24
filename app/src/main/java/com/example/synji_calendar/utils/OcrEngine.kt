package com.example.synji_calendar.utils

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.benjaminwan.ocrlibrary.OcrEngine
import com.benjaminwan.ocrlibrary.OcrResult

object OcrEngine {
    private const val TAG = "OcrEngine"
    private var ocrEngine: OcrEngine? = null
    @Volatile private var isInitialized = false

    fun init(context: Context): Boolean {
        if (isInitialized && ocrEngine != null) return true

        try {
            val engine = OcrEngine(context)
            // 精度优化 1：设置合理的内边距
            engine.padding = 10
            // 精度优化 2：降低得分阈值（从 0.5 -> 0.3），提高对弱特征文字的捕获力
            engine.boxScoreThresh = 0.3f
            engine.boxThresh = 0.2f
            // 精度优化 3：增大文字框膨胀系数（从 1.6 -> 2.0），确保文字边缘不被裁剪
            engine.unClipRatio = 2.0f
            engine.doAngle = true
            engine.mostAngle = true

            val ret = engine.init(context.assets, 4)

            if (ret) {
                ocrEngine = engine
                isInitialized = true
                Log.i(TAG, "OCR Engine initialized with High Accuracy mode")
                return true
            } else {
                Log.e(TAG, "OCR Engine internal init failed")
                return false
            }
        } catch (e: Exception) {
            Log.e(TAG, "OCR Engine init crashed", e)
            isInitialized = false
            return false
        }
    }

    /**
     * 高精度识别：不再强制在识别前缩小图片，或者将上限提至 2000px
     */
    fun recognize(bitmap: Bitmap): String {
        val engine = ocrEngine
        if (!isInitialized || engine == null) {
            Log.e(TAG, "OCR Engine not initialized!")
            return ""
        }

        return try {
            // 精度优化 4：提高送入检测器的长边上限（从 1080 -> 1600）
            val targetMaxSide = 1600
            val processedBitmap = scaleBitmapIfNeeded(bitmap, targetMaxSide)
            
            val config = processedBitmap.config ?: Bitmap.Config.ARGB_8888
            val outputBitmap = processedBitmap.copy(config, true)
            
            // 执行识别：将 maxSideLen 同步提升至 1600
            val result: OcrResult = engine.detect(processedBitmap, outputBitmap, targetMaxSide)
            
            val finalResult = result.strRes
            Log.d(TAG, "High Accuracy OCR Result: $finalResult")
            finalResult
        } catch (e: Exception) {
            Log.e(TAG, "OCR recognition failed", e)
            ""
        }
    }

    private fun scaleBitmapIfNeeded(bitmap: Bitmap, maxDim: Int): Bitmap {
        if (bitmap.width <= maxDim && bitmap.height <= maxDim) return bitmap
        
        val scale = maxDim.toFloat() / Math.max(bitmap.width, bitmap.height)
        val newWidth = (bitmap.width * scale).toInt()
        val newHeight = (bitmap.height * scale).toInt()
        
        Log.d(TAG, "Scaling for accuracy: from ${bitmap.width}x${bitmap.height} to ${newWidth}x${newHeight}")
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    fun destroy() {
        ocrEngine = null
        isInitialized = false
    }
}
