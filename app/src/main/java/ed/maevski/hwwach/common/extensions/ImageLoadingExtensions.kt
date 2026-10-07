package ed.maevski.hwwach.common.extensions

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import coil3.request.fallback
import coil3.request.placeholder
import ed.maevski.hwwach.BuildConfig
import ed.maevski.hwwach.R
import ed.maevski.hwwach.domain.models.Photo

/**
 * Загрузка изображения с приоритетом localPath > remoteUrl (через прокси).
 *
 * PhotoRepository гарантирует: если файл был удалён, localPath = null.
 * Поэтому здесь достаточно тривиального выражения — это не бизнес-логика,
 * а чистая передача данных в Coil.
 */
@Composable
fun PriorityAsyncImage(
    photo: Photo,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    @DrawableRes placeholderResId: Int = R.drawable.ic_add_photo,
) {

    val context = LocalContext.current

    // Создаем модель только если изменились ключевые данные
    val model = remember(photo.localPath, photo.serverUuid, placeholderResId) {
        val imageSource = photo.localPath ?: photo.serverUuid?.let { uuid ->
            "${BuildConfig.BASE_MAIN_URL}photos/$uuid/download"
        }
        ImageRequest.Builder(context)
            .data(imageSource)
            .crossfade(300)
            .placeholder(placeholderResId)
            .error(placeholderResId)
            .fallback(placeholderResId) // Показываем, если и local, и remote == null
            .build()
    }

    AsyncImage(
        model = model,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
    )
}

@Composable
fun PriorityZoomableAsyncImage(
    photo: Photo,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    @DrawableRes placeholderResId: Int = R.drawable.ic_add_photo,
) {
    val context = LocalContext.current

    val model = remember(photo.localPath, photo.serverUuid, placeholderResId) {
        val imageSource = photo.localPath ?: photo.serverUuid?.let { uuid ->
            "${BuildConfig.BASE_MAIN_URL}photos/$uuid/download"
        }
        ImageRequest.Builder(context)
            .data(imageSource)
            .crossfade(300)
            .placeholder(placeholderResId)
            .error(placeholderResId)
            .fallback(placeholderResId)
            .build()
    }

    me.saket.telephoto.zoomable.coil3.ZoomableAsyncImage(
        model = model,
        contentDescription = contentDescription,
        modifier = modifier,
    )
}
