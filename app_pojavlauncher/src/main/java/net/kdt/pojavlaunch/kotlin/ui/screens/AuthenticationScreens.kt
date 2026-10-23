package net.kdt.pojavlaunch.kotlin.ui.screens

import android.annotation.SuppressLint
import android.net.Uri
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import net.ashmeet.hyperlauncher.R
import net.kdt.pojavlaunch.BaseActivity
import net.kdt.pojavlaunch.authenticator.accounts.Accounts
import net.kdt.pojavlaunch.authenticator.AuthType
import net.kdt.pojavlaunch.skin.AndroidSkinAnalyzer
import net.kdt.pojavlaunch.skin.SkinModelType
import net.kdt.pojavlaunch.skin.SkinUtils
import net.kdt.pojavlaunch.ui.theme.PojavTheme
import java.io.File
import java.io.FileOutputStream
import java.net.URLEncoder
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectAuthScreen(
    onMicrosoftClick: () -> Unit,
    onLocalClick: (String, String?, String?) -> Unit,
    onElyByClick: () -> Unit
) {
    var showLocalDialog by remember { mutableStateOf(false) }
    var localUsername by remember { mutableStateOf("") }
    var selectedSkinPath by remember { mutableStateOf<String?>(null) }
    var selectedCapePath by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current
    val currentAccount = remember { if (isPreview) null else try { Accounts.getCurrent() } catch(_: Exception) { null } }

    val skinLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val file = copyUriToInternal(context, it, "skin")
            if (file != null) {
                if (AndroidSkinAnalyzer.validate(file.readBytes())) {
                    selectedSkinPath = file.absolutePath
                    if (currentAccount?.authType == AuthType.LOCAL) {
                        currentAccount.skinPath = file.absolutePath
                        currentAccount.updateSkinFace()
                        Toast.makeText(context, "Skin updated!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Invalid skin dimensions!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val capeLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val file = copyUriToInternal(context, it, "cape")
            if (file != null) {
                selectedCapePath = file.absolutePath
                if (currentAccount?.authType == AuthType.LOCAL) {
                    currentAccount.capePath = file.absolutePath
                    try {
                        currentAccount.save()
                        Toast.makeText(context, "Cape updated!", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Failed to save cape", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    val backgroundBitmap = if (isPreview) {
        try { BaseActivity.getBackgroundBitmap() } catch (_: Exception) { null }
    } else null
    val hasBackground = backgroundBitmap != null

    Box(modifier = Modifier.fillMaxSize()) {

        if (isPreview) {
            if (backgroundBitmap != null) {
                Image(
                    bitmap = backgroundBitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
            }

            Box(modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = if (hasBackground) 0.4f else 0f))
            )
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {

            Surface(
                modifier = Modifier
                    .weight(0.9f)
                    .fillMaxHeight()
                    .padding(end = 8.dp),
                color = Color.Transparent
            ) {
                @Suppress("DEPRECATION")
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp)
                ) {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Select Login Method",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AuthActionButton(
                            text = stringResource(id = R.string.auth_select_microsoft),
                            icon = R.drawable.ic_auth_ms,
                            onClick = onMicrosoftClick,
                            tint = Color.Unspecified
                        )
                        AuthActionButton(
                            text = stringResource(id = R.string.auth_select_elyby),
                            icon = R.drawable.ic_auth_elyby,
                            onClick = onElyByClick,
                            tint = Color.Unspecified
                        )
                        AuthActionButton(
                            text = stringResource(id = R.string.auth_select_local),
                            icon = R.drawable.ic_px_home,
                            onClick = { showLocalDialog = true },
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }

            Surface(
                modifier = Modifier
                    .weight(1.1f)
                    .fillMaxHeight(),
                color = Color.Transparent
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                        .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isPreview) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_px_image_renderer),
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            @Suppress("DEPRECATION")
                            Text(
                                text = "3D Skin Preview\n(skinview3d)",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        val skinUrl = remember(currentAccount, selectedSkinPath) {
                            if (selectedSkinPath != null) {
                                "file://$selectedSkinPath"
                            } else {
                                SkinUtils.getSkinUrl(currentAccount)
                            }
                        }

                        val skinModel = remember(currentAccount, selectedSkinPath) {
                            if (selectedSkinPath != null) {
                                val bytes = File(selectedSkinPath!!).readBytes()
                                AndroidSkinAnalyzer.detectModel(bytes)
                            } else {
                                currentAccount?.skinModel ?: SkinModelType.STEVE
                            }
                        }

                        Skin3DViewer(
                            modifier = Modifier.fillMaxSize(),
                            skinUrl = skinUrl,
                            model = if (skinModel == SkinModelType.ALEX) "slim" else "default"
                        )

                        if (!isPreview && currentAccount?.authType == AuthType.LOCAL) {
                            var menuExpanded by rememberSaveable { mutableStateOf(false) }
                            BackHandler(menuExpanded) { menuExpanded = false }

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                contentAlignment = Alignment.BottomEnd
                            ) {
                                Column(horizontalAlignment = Alignment.End) {
                                    AnimatedVisibility(
                                        visible = menuExpanded,
                                        enter = fadeIn() + expandVertically(expandFrom = Alignment.Bottom),
                                        exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Bottom)
                                    ) {
                                        VerticalMenu(modifier = Modifier.padding(bottom = 8.dp)) {
                                            VerticalMenuItem(
                                                onClick = {
                                                    skinLauncher.launch("image/*")
                                                    menuExpanded = false
                                                },
                                                text = "Skin",
                                                icon = R.drawable.ic_px_image
                                            )
                                            VerticalMenuItem(
                                                onClick = {
                                                    capeLauncher.launch("image/*")
                                                    menuExpanded = false
                                                },
                                                text = "Cape",
                                                icon = R.drawable.ic_px_theme
                                            )
                                        }
                                    }

                                    SplitButton(
                                        onClick = { skinLauncher.launch("image/*") },
                                        onMenuClick = { menuExpanded = !menuExpanded },
                                        expanded = menuExpanded
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showLocalDialog) {
        AlertDialog(
            onDismissRequest = { showLocalDialog = false },
            title = { Text(stringResource(id = R.string.auth_select_local)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    @Suppress("DEPRECATION")
                    Text(
                        text = stringResource(id = R.string.login_online_username_hint),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = localUsername,
                        onValueChange = { localUsername = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Username") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                            focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                        )
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { skinLauncher.launch("image/*") },
                            modifier = Modifier.weight(1f),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(if (selectedSkinPath == null) "Pick Skin" else "Skin Selected", fontSize = 11.sp)
                        }
                        Button(
                            onClick = { capeLauncher.launch("image/*") },
                            modifier = Modifier.weight(1f),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(if (selectedCapePath == null) "Pick Cape" else "Cape Selected", fontSize = 11.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (localUsername.isNotBlank()) {
                            onLocalClick(localUsername, selectedSkinPath, selectedCapePath)
                            showLocalDialog = false
                        }
                    },
                    shape = CircleShape
                ) {
                    @Suppress("DEPRECATION")
                    Text(stringResource(id = R.string.login_online_login_label).uppercase(), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                @Suppress("DEPRECATION")
                TextButton(onClick = { showLocalDialog = false }) {
                    Text(stringResource(id = android.R.string.cancel))
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }
}

private fun copyUriToInternal(context: android.content.Context, uri: Uri, prefix: String): File? {
    return try {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            val fileName = "${prefix}_${UUID.randomUUID()}.png"
            val file = File(context.filesDir, fileName)
            FileOutputStream(file).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            file
        }
    } catch (e: Exception) {
        null
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun Skin3DViewer(
    modifier: Modifier = Modifier,
    skinUrl: String? = null,
    model: String = "default"
) {
    var isPageLoaded by remember { mutableStateOf(false) }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.allowFileAccess = true
                    @Suppress("DEPRECATION")
                    settings.allowFileAccessFromFileURLs = true
                    @Suppress("DEPRECATION")
                    settings.allowUniversalAccessFromFileURLs = true
                    settings.cacheMode = WebSettings.LOAD_DEFAULT
                    settings.domStorageEnabled = true
                    setBackgroundColor(0)

                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            isPageLoaded = true
                            val skin = if (!skinUrl.isNullOrEmpty()) skinUrl else "steve.png"
                            view?.evaluateJavascript("loadSkin('$skin', '$model'); startAnim('NewIdle');", null)
                        }
                    }

                    val encodedUrl = try { URLEncoder.encode(skinUrl ?: "", "UTF-8") } catch (_: Exception) { "" }
                    val finalUrl = "file:///android_asset/skinview.html" + (if (encodedUrl.isNotEmpty()) "?skin=$encodedUrl&model=$model" else "")
                    loadUrl(finalUrl)
                }
            },
            update = { webView ->
                if (isPageLoaded) {
                    val skin = if (!skinUrl.isNullOrEmpty()) skinUrl else "steve.png"
                    webView.evaluateJavascript("loadSkin('$skin', '$model');", null)
                }
            }
        )

        AnimatedVisibility(
            visible = !isPageLoaded,
            exit = fadeOut(tween(500))
        ) {
            Box(
                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(modifier = Modifier.size(32.dp), strokeWidth = 2.dp)
            }
        }
    }
}

@Composable
fun AuthActionButton(
    text: String,
    icon: Int,
    onClick: () -> Unit,
    tint: Color = Color.Unspecified
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 40.dp),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            @Suppress("DEPRECATION")
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = tint
            )
            Spacer(modifier = Modifier.width(12.dp))
            @Suppress("DEPRECATION")
            Text(
                text = text,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun LocalLoginScreen(
    onLoginClick: (String, String?, String?) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var selectedSkinPath by remember { mutableStateOf<String?>(null) }
    var selectedCapePath by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current

    val skinLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val file = copyUriToInternal(context, it, "skin")
            if (file != null) {
                if (AndroidSkinAnalyzer.validate(file.readBytes())) {
                    selectedSkinPath = file.absolutePath
                } else {
                    Toast.makeText(context, "Invalid skin dimensions!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val capeLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val file = copyUriToInternal(context, it, "cape")
            selectedCapePath = file?.absolutePath
        }
    }

    val backgroundBitmap = if (isPreview) {
        try { BaseActivity.getBackgroundBitmap() } catch (_: Exception) { null }
    } else null
    val hasBackground = backgroundBitmap != null

    Box(modifier = Modifier.fillMaxSize()) {

        if (isPreview) {
            if (backgroundBitmap != null) {
                Image(
                    bitmap = backgroundBitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
            }

            Box(modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = if (hasBackground) 0.4f else 0f))
            )
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            Surface(
                modifier = Modifier
                    .weight(0.9f)
                    .fillMaxHeight()
                    .padding(end = 8.dp),
                color = Color.Transparent
            ) {
                @Suppress("DEPRECATION")
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(id = R.string.auth_select_local),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )

                    Spacer(Modifier.height(32.dp))

                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Username") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        )
                    )

                    Spacer(Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                         Button(
                            onClick = { skinLauncher.launch("image/*") },
                            modifier = Modifier.weight(1f),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(if (selectedSkinPath == null) "Change Skin" else "Skin Picked", fontSize = 11.sp)
                        }
                        Button(
                            onClick = { capeLauncher.launch("image/*") },
                            modifier = Modifier.weight(1f),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(if (selectedCapePath == null) "Change Cape" else "Cape Picked", fontSize = 11.sp)
                        }
                    }

                    Spacer(Modifier.height(32.dp))

                    Button(
                        onClick = { onLoginClick(username, selectedSkinPath, selectedCapePath) },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = CircleShape
                    ) {
                        Text(
                            text = stringResource(id = R.string.login_online_login_label).uppercase(),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            Surface(
                modifier = Modifier
                    .weight(1.1f)
                    .fillMaxHeight(),
                color = Color.Transparent
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                        .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    val skinUrl = remember(username, selectedSkinPath) {
                         if (selectedSkinPath != null) "file://$selectedSkinPath"
                         else if (username.isNotBlank()) "https://minotar.net/skin/$username"
                         else "steve.png"
                    }
                    val skinModel = remember(selectedSkinPath) {
                        if (selectedSkinPath != null) {
                            val bytes = File(selectedSkinPath!!).readBytes()
                            AndroidSkinAnalyzer.detectModel(bytes)
                        } else SkinModelType.STEVE
                    }
                    Skin3DViewer(
                        modifier = Modifier.fillMaxSize(),
                        skinUrl = skinUrl,
                        model = if (skinModel == SkinModelType.ALEX) "slim" else "default"
                    )
                }
            }
        }
    }
}

@Composable
private fun VerticalMenu(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.width(140.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.background,
        border = BorderStroke(0.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 4.dp),
            content = content
        )
    }
}

@Composable
private fun VerticalMenuItem(
    onClick: () -> Unit,
    text: String,
    icon: Int,
    contentColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        contentColor = contentColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = contentColor.copy(alpha = 0.8f)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun SplitButton(
    onClick: () -> Unit,
    onMenuClick: () -> Unit,
    expanded: Boolean,
    modifier: Modifier = Modifier
) {
    val containerColor = MaterialTheme.colorScheme.primary
    val contentColor = MaterialTheme.colorScheme.onPrimary

    Surface(
        modifier = modifier.height(40.dp),
        shape = CircleShape,
        color = containerColor,
        contentColor = contentColor
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .clickable(onClick = onClick)
                    .padding(start = 16.dp, end = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_px_image),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = contentColor
                    )
                    Spacer(Modifier.width(8.dp))
                    @Suppress("DEPRECATION")
                    Text(
                        text = "Skin",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )
                }
            }

            VerticalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                thickness = 1.dp,
                color = contentColor.copy(alpha = 0.3f)
            )

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .clickable(onClick = onMenuClick)
                    .padding(start = 8.dp, end = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = contentColor
                )
            }
        }
    }
}

@Preview(showBackground = true, device = "spec:width=1280dp,height=800dp,orientation=landscape")
@Composable
fun SelectAuthScreenPreview() {
    PojavTheme(dynamicColor = true) {
        SelectAuthScreen({}, { _, _, _ -> }, {})
    }
}

@Preview(showBackground = true, device = "spec:width=1280dp,height=800dp,orientation=landscape")
@Composable
fun LocalLoginScreenPreview() {
    PojavTheme(dynamicColor = true) {
        LocalLoginScreen({ _, _, _ -> })
    }
}
