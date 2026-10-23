package net.kdt.pojavlaunch.modrinth

import android.graphics.Bitmap
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

data class ModrinthProject(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val author: String = "",
    val iconUrl: String? = null,
    val iconBitmapState: MutableState<Bitmap?> = mutableStateOf(null),
    val isIconLoadingState: MutableState<Boolean> = mutableStateOf(false)
) {

    constructor(id: String, title: String, description: String, iconUrl: String?) :
        this(id, title, description, "", iconUrl, mutableStateOf(null), mutableStateOf(false))

    val iconBitmap: Bitmap?
        get() = iconBitmapState.value

    val isIconLoading: Boolean
        get() = isIconLoadingState.value
}

data class ModrinthVersion(
    val name: String,
    val url: String,
    val filename: String?,
    val gameVersions: List<String>,
    val loaders: List<String>
)
