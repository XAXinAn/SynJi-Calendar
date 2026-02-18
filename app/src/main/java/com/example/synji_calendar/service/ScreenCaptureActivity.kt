package com.example.synji_calendar.service

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import com.example.synji_calendar.ui.home.HomeRepository
import com.example.synji_calendar.utils.OcrEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ScreenCaptureActivity : Activity() {

    private var projectionManager: MediaProjectionManager? = null
    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null
    private var token: String? = null

    companion object {
        private const val REQUEST_CODE_SCREEN_CAPTURE = 100
        private const val TAG = "ScreenCaptureActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        token = intent.getStringExtra(FloatingService.EXTRA_TOKEN)
        projectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        startActivityForResult(projectionManager?.createScreenCaptureIntent(), REQUEST_CODE_SCREEN_CAPTURE)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_SCREEN_CAPTURE) {
            if (resultCode == RESULT_OK && data != null) {
                mediaProjection = projectionManager?.getMediaProjection(resultCode, data)
                captureScreen()
            } else {
                Toast.makeText(this, "需要截屏权限以进行OCR识别", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun captureScreen() {
        val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val metrics = windowManager.currentWindowMetrics
        val bounds = metrics.bounds
        val screenWidth = bounds.width()
        val screenHeight = bounds.height()
        val screenDensity = resources.displayMetrics.densityDpi

        imageReader = ImageReader.newInstance(screenWidth, screenHeight, PixelFormat.RGBA_8888, 2)
        virtualDisplay = mediaProjection?.createVirtualDisplay(
            "ScreenCapture",
            screenWidth,
            screenHeight,
            screenDensity,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader?.surface,
            null,
            null
        )

        // 稍微延迟一点点确保内容已捕获
        Handler(Looper.getMainLooper()).postDelayed({
            val image = imageReader?.acquireLatestImage()
            if (image != null) {
                val bitmap = imageToBitmap(image)
                image.close()
                if (bitmap != null) {
                    processBitmap(bitmap)
                } else {
                    finish()
                }
            } else {
                Log.e(TAG, "Failed to acquire image")
                finish()
            }
        }, 300)
    }

    private fun imageToBitmap(image: Image): Bitmap? {
        val width = image.width
        val height = image.height
        val planes = image.planes
        val buffer = planes[0].buffer
        val pixelStride = planes[0].pixelStride
        val rowStride = planes[0].rowStride
        val rowPadding = rowStride - pixelStride * width
        
        val bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888)
        bitmap.copyPixelsFromBuffer(buffer)
        
        // 裁剪掉 padding 部分
        return Bitmap.createBitmap(bitmap, 0, 0, width, height)
    }

    private fun processBitmap(bitmap: Bitmap) {
        val currentToken = token
        if (currentToken.isNullOrEmpty()) {
            Toast.makeText(this, "登录已过期，请重新登录", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        Toast.makeText(this, "正在识别屏幕内容...", Toast.LENGTH_SHORT).show()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 1. OCR
                val text = OcrEngine.recognize(bitmap)
                if (text.isBlank()) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@ScreenCaptureActivity, "未发现可识别文字", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    return@launch
                }

                // 2. AI Parse & Add
                val repository = HomeRepository()
                val aiResponse = repository.parseScheduleWithAi(currentToken, text)
                
                withContext(Dispatchers.Main) {
                    if (aiResponse.code == 200 && aiResponse.data != null) {
                        val schedules = aiResponse.data
                        var successCount = 0
                        launch(Dispatchers.IO) {
                            schedules.forEach { extraction ->
                                // 对齐文档：设置为 AI 识别且未读
                                val scheduleToSave = extraction.copy(
                                    isAiGenerated = true,
                                    isViewed = false
                                )
                                val addResponse = repository.addSchedule(currentToken, scheduleToSave)
                                if (addResponse.code == 200) successCount++
                            }
                            withContext(Dispatchers.Main) {
                                Toast.makeText(this@ScreenCaptureActivity, "成功添加 $successCount 条日程", Toast.LENGTH_LONG).show()
                                finish()
                            }
                        }
                    } else {
                        Toast.makeText(this@ScreenCaptureActivity, "AI解析失败: ${aiResponse.message}", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing screen capture", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ScreenCaptureActivity, "处理失败: ${e.message}", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        virtualDisplay?.release()
        imageReader?.close()
        mediaProjection?.stop()
    }
}
