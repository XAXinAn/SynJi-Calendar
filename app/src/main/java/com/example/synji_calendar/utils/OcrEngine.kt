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
            // 1. 实例化引擎
            val engine = OcrEngine(context)
            
            // 2. 配置推理参数 (该版本 1.8.0 可能是通过属性设置)
            engine.padding = 10
            engine.boxScoreThresh = 0.5f
            engine.boxThresh = 0.3f
            engine.unClipRatio = 1.6f
            engine.doAngle = true
            engine.mostAngle = true

            // 3. 调用初始化
            // 根据报错，1.8.0 版本的 init 签名是 (AssetManager, numThread)
            val ret = engine.init(context.assets, 4)

            if (ret) {
                ocrEngine = engine
                isInitialized = true
                Log.i(TAG, "OCR Engine initialized successfully")
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

    fun recognize(bitmap: Bitmap): String {
        val engine = ocrEngine
        if (!isInitialized || engine == null) {
            Log.e(TAG, "OCR Engine not initialized!")
            return ""
        }

        return try {
            // 解决 Argument type mismatch: actual type is 'Bitmap.Config?', but 'Bitmap.Config' was expected
            val config = bitmap.config ?: Bitmap.Config.ARGB_8888
            val outputBitmap = bitmap.copy(config, true)
            
            // 执行识别: detect(input, output, maxSideLen)
            val result: OcrResult = engine.detect(bitmap, outputBitmap, 1080)
            
            val finalResult = result.strRes
            Log.d(TAG, "OCR Result: $finalResult")
            finalResult
        } catch (e: Exception) {
            Log.e(TAG, "OCR recognition failed", e)
            ""
        }
    }

    fun destroy() {
        ocrEngine = null
        isInitialized = false
    }
}