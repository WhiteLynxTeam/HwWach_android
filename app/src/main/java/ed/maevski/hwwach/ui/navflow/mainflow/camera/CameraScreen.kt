package ed.maevski.hwwach.ui.navflow.mainflow.camera

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.extensions.ExtensionMode
import androidx.camera.extensions.ExtensionsManager
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun CameraScreen(
    state: CameraState,
    onAction: (CameraAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    // ── Разрешения ──────────────────────────────────────────────────────────
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (!isGranted) onAction(CameraAction.OnBackPressed)
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    if (!hasCameraPermission) {
        Box(modifier = modifier.fillMaxSize().background(Color.Black))
        return
    }

    // ── ResolutionSelector: соотношение 4:3 (полный сенсор) + максимальное разрешение ──
    val resolutionSelector = remember {
        ResolutionSelector.Builder()
            .setAspectRatioStrategy(AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY)
            .setResolutionStrategy(ResolutionStrategy.HIGHEST_AVAILABLE_STRATEGY)
            .build()
    }

    // ── CameraX объекты ─────────────────────────────────────────────────────
    val imageCapture = remember {
        ImageCapture.Builder()
            // Максимальное качество вместо скорости
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            // Максимальное разрешение с соотношением 4:3
            .setResolutionSelector(resolutionSelector)
            // JPEG качество 100 — без потерь на сжатии
            .setJpegQuality(100)
            .build()
    }

    val baseCameraSelector = remember(state.lensFacing) {
        CameraSelector.Builder().requireLensFacing(state.lensFacing).build()
    }

    val previewView = remember { PreviewView(context) }
    var camera by remember { mutableStateOf<Camera?>(null) }

    LaunchedEffect(state.lensFacing) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            // ── ExtensionsManager: пробуем включить HDR или AUTO ────────────
            val extensionsManagerFuture = ExtensionsManager.getInstanceAsync(context, cameraProvider)
            extensionsManagerFuture.addListener({
                val extensionsManager = extensionsManagerFuture.get()

                // Выбираем лучший доступный режим: HDR → AUTO → без расширений
                val activeCameraSelector = when {
                    extensionsManager.isExtensionAvailable(baseCameraSelector, ExtensionMode.HDR) -> {
                        Log.d("CameraScreen", "HDR extension enabled")
                        extensionsManager.getExtensionEnabledCameraSelector(baseCameraSelector, ExtensionMode.HDR)
                    }
                    extensionsManager.isExtensionAvailable(baseCameraSelector, ExtensionMode.AUTO) -> {
                        Log.d("CameraScreen", "AUTO extension enabled")
                        extensionsManager.getExtensionEnabledCameraSelector(baseCameraSelector, ExtensionMode.AUTO)
                    }
                    else -> {
                        Log.d("CameraScreen", "No extensions available, using default")
                        baseCameraSelector
                    }
                }

                val preview = Preview.Builder()
                    .setResolutionSelector(resolutionSelector)
                    .build()
                    .also { it.surfaceProvider = previewView.surfaceProvider }

                try {
                    cameraProvider.unbindAll()
                    camera = cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        activeCameraSelector,
                        preview,
                        imageCapture
                    )
                } catch (e: Exception) {
                    // Расширение не поддерживается в сочетании с данной конфигурацией —
                    // откатываемся на базовый CameraSelector
                    Log.w("CameraScreen", "Extension binding failed, fallback to default", e)
                    try {
                        val fallbackPreview = Preview.Builder()
                            .setResolutionSelector(resolutionSelector)
                            .build()
                            .also { it.surfaceProvider = previewView.surfaceProvider }
                        cameraProvider.unbindAll()
                        camera = cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            baseCameraSelector,
                            fallbackPreview,
                            imageCapture
                        )
                    } catch (fallbackEx: Exception) {
                        Log.e("CameraScreen", "Camera binding failed", fallbackEx)
                    }
                }
            }, ContextCompat.getMainExecutor(context))
        }, ContextCompat.getMainExecutor(context))
    }

    // ── Состояния анимаций ───────────────────────────────────────────────────
    // Вспышка экрана при фото
    var flashAlpha by remember { mutableStateOf(0f) }
    val flashAlphaAnimated by animateFloatAsState(
        targetValue = flashAlpha,
        animationSpec = tween(durationMillis = 120, easing = LinearEasing),
        label = "shutterFlash"
    )

    // Масштаб кнопки затвора
    val shutterScale = remember { Animatable(1f) }

    // Индикатор фокуса
    var focusPoint by remember { mutableStateOf<Pair<Float, Float>?>(null) }
    val focusAlpha = remember { Animatable(0f) }
    val focusScale = remember { Animatable(1.4f) }

    // ── UI ──────────────────────────────────────────────────────────────────
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Превью камеры с обработкой тапа для фокуса
        AndroidViewWithTapFocus(
            previewView = previewView,
            camera = camera,
            onFocusTap = { x, y ->
                focusPoint = x to y
                scope.launch {
                    focusAlpha.snapTo(1f)
                    focusScale.snapTo(1.4f)
                    focusScale.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(300, easing = FastOutSlowInEasing)
                    )
                    delay(900)
                    focusAlpha.animateTo(0f, animationSpec = tween(300))
                    focusPoint = null
                }
            }
        )

        // Индикатор фокуса (квадратный прицел)
        focusPoint?.let { (x, y) ->
            Box(
                modifier = Modifier
                    .offset { IntOffset(x.toInt() - 48.dp.roundToPx(), y.toInt() - 48.dp.roundToPx()) }
                    .size(96.dp)
                    .alpha(focusAlpha.value)
                    .scale(focusScale.value)
                    .border(2.dp, Color(0xFFFFD600), RoundedCornerShape(8.dp))
            )
        }

        // Вспышка экрана (белый overlay при фото)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White.copy(alpha = flashAlphaAnimated))
        )

        // Кнопка «Назад»
        IconButton(
            onClick = { onAction(CameraAction.OnBackPressed) },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .size(48.dp)
                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Назад",
                tint = Color.White
            )
        }

        // Нижняя панель управления
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 48.dp, start = 32.dp, end = 32.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Заглушка для симметрии
            Box(modifier = Modifier.size(48.dp))

            // Кнопка затвора (premium shutter)
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .scale(shutterScale.value)
                    .border(3.dp, Color.White, CircleShape)
                    .padding(6.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                // Нажатие: уменьшить кнопку
                                scope.launch {
                                    shutterScale.animateTo(
                                        0.88f,
                                        animationSpec = tween(80, easing = FastOutSlowInEasing)
                                    )
                                }
                                tryAwaitRelease()
                                // Отпускание: вернуть размер
                                scope.launch {
                                    shutterScale.animateTo(
                                        1f,
                                        animationSpec = tween(150, easing = FastOutSlowInEasing)
                                    )
                                }
                            },
                            onTap = {
                                // Вспышка экрана
                                scope.launch {
                                    flashAlpha = 0.85f
                                    delay(80)
                                    flashAlpha = 0f
                                }

                                // Снимок
                                val photoFile = File(
                                    context.externalCacheDir,
                                    "HW_watch_${
                                        SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
                                            .format(System.currentTimeMillis())
                                    }.jpg"
                                )
                                photoFile.parentFile?.mkdirs()

                                val outputOptions =
                                    ImageCapture.OutputFileOptions.Builder(photoFile).build()

                                imageCapture.takePicture(
                                    outputOptions,
                                    ContextCompat.getMainExecutor(context),
                                    object : ImageCapture.OnImageSavedCallback {
                                        override fun onImageSaved(
                                            outputFileResults: ImageCapture.OutputFileResults
                                        ) {
                                            try {
                                                val contentUri = FileProvider.getUriForFile(
                                                    context,
                                                    "${context.packageName}.fileprovider",
                                                    photoFile
                                                )
                                                onAction(
                                                    CameraAction.OnPhotoCaptured(contentUri.toString())
                                                )
                                            } catch (e: Exception) {
                                                Log.e(
                                                    "CameraScreen",
                                                    "FileProvider failed",
                                                    e
                                                )
                                                val savedUri =
                                                    outputFileResults.savedUri
                                                        ?: Uri.fromFile(photoFile)
                                                onAction(
                                                    CameraAction.OnPhotoCaptured(savedUri.toString())
                                                )
                                            }
                                        }

                                        override fun onError(exception: ImageCaptureException) {
                                            Log.e(
                                                "CameraScreen",
                                                "Photo capture failed",
                                                exception
                                            )
                                        }
                                    }
                                )
                            }
                        )
                    }
            )

            // Переключение камеры
            IconButton(
                onClick = { onAction(CameraAction.ToggleLensFacing) },
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Cameraswitch,
                    contentDescription = "Переключить камеру",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

// ── Вспомогательный composable: превью + tap-to-focus ───────────────────────
@Composable
private fun AndroidViewWithTapFocus(
    previewView: PreviewView,
    camera: Camera?,
    onFocusTap: (Float, Float) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(camera) {
                detectTapGestures { offset ->
                    val cam = camera ?: return@detectTapGestures
                    val meteringPointFactory = previewView.meteringPointFactory
                    val point = meteringPointFactory.createPoint(offset.x, offset.y)
                    val action = FocusMeteringAction
                        .Builder(point, FocusMeteringAction.FLAG_AF or FocusMeteringAction.FLAG_AE)
                        .setAutoCancelDuration(3, TimeUnit.SECONDS)
                        .build()
                    cam.cameraControl.startFocusAndMetering(action)
                    onFocusTap(offset.x, offset.y)
                }
            }
    ) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )
    }
}
