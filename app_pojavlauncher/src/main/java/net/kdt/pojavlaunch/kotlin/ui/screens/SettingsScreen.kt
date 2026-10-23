package net.kdt.pojavlaunch.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color as AndroidColor
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import kotlinx.coroutines.launch
import net.ashmeet.hyperlauncher.R
import net.kdt.pojavlaunch.CustomControlsActivity
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.contracts.OpenDocumentWithExtension
import net.kdt.pojavlaunch.multirt.MultiRTConfigDialog
import net.kdt.pojavlaunch.plugins.LibraryPlugin
import net.kdt.pojavlaunch.prefs.LauncherPreferences
import net.kdt.pojavlaunch.ui.theme.PojavTheme
import net.kdt.pojavlaunch.utils.RendererCompatUtil
import net.kdt.pojavlaunch.ui.components.*
import net.kdt.pojavlaunch.utils.UpdateUtils
import java.io.File
import java.io.FileOutputStream
import kotlin.math.roundToInt

enum class SettingsPage(val titleRes: Int, val iconRes: Int) {
    APPEARANCE(R.string.preference_appearance_title, R.drawable.ic_px_theme),
    VIDEO(R.string.preference_video_title, R.drawable.ic_px_image),
    CONTROL(R.string.preference_control_title, R.drawable.ic_px_gamepad),
    JAVA(R.string.preference_java_title, R.drawable.ic_px_java),
    MISC(R.string.preference_misc_title, R.drawable.ic_px_alt_sliders),
    EXPERIMENTAL(R.string.preference_experimental_title, R.drawable.ic_px_experiment)
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit
) {
    var currentPage by rememberSaveable { mutableStateOf(SettingsPage.APPEARANCE) }
    var isMainPage by rememberSaveable { mutableStateOf(true) }
    val railScrollState = rememberScrollState()

    val animPreset = LauncherPreferences.PREF_TRANSITION_ANIMATION_STATE.value
    val animDuration = LauncherPreferences.PREF_TRANSITION_DURATION_STATE.value
    val animIntensity = LauncherPreferences.PREF_TRANSITION_INTENSITY_STATE.value

    val transitionSpec: AnimatedContentTransitionScope<Boolean>.() -> ContentTransform = {
        val duration = animDuration
        when (animPreset) {
            "fade" -> {
                fadeIn(animationSpec = tween(duration)) togetherWith fadeOut(animationSpec = tween(duration))
            }
            "bounce" -> {
                slideInVertically(
                    initialOffsetY = { -(it * animIntensity).toInt() },
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
                ) + fadeIn() togetherWith slideOutVertically(targetOffsetY = { it }) + fadeOut()
            }
            else -> {
                fadeIn(animationSpec = tween(duration)) togetherWith fadeOut(animationSpec = tween(duration))
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isWide = maxWidth > 600.dp

            if (isWide) {
                Row(modifier = Modifier.fillMaxSize()) {
                    NavigationRail(
                        modifier = Modifier.fillMaxHeight().width(80.dp),
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        windowInsets = WindowInsets(0, 0, 0, 0)
                    ) {
                        Spacer(modifier = Modifier.height(12.dp))

                        Column(
                            modifier = Modifier
                                .fillMaxHeight()
                                .verticalScroll(railScrollState),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            SettingsPage.entries.forEach { page ->
                                NavigationRailItem(
                                    selected = currentPage == page && !isMainPage,
                                    onClick = {
                                        currentPage = page
                                        isMainPage = false
                                    },
                                    icon = { Icon(painterResource(page.iconRes), contentDescription = null, modifier = Modifier.size(24.dp)) },
                                    label = { Text(stringResource(page.titleRes).substringBefore(" "), fontSize = 10.sp) },
                                    alwaysShowLabel = false,
                                    colors = NavigationRailItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                )
                            }
                        }
                    }

                    VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                    Scaffold(
                        containerColor = Color.Transparent,
                        contentWindowInsets = WindowInsets(0, 0, 0, 0),
                    ) { padding ->
                        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                            AnimatedContent(
                                targetState = currentPage,
                                transitionSpec = {
                                    fadeIn(animationSpec = tween(animDuration)) togetherWith fadeOut(animationSpec = tween(animDuration))
                                },
                                label = "settings_content_anim"
                            ) { page ->
                                SettingsContent(page)
                            }
                        }
                    }
                }
            } else {
                AnimatedContent(
                    targetState = isMainPage,
                    transitionSpec = transitionSpec,
                    label = "settings_nav_anim"
                ) { mainPage ->
                    if (mainPage) {
                        Scaffold(
                            containerColor = Color.Transparent,
                            contentWindowInsets = WindowInsets(0, 0, 0, 0),
                            topBar = {
                                TopAppBar(
                                    title = { Text("Settings", fontWeight = FontWeight.Bold) },
                                    navigationIcon = { },
                                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                                )
                            }
                        ) { padding ->
                            BackHandler { onBack() }
                            Box(modifier = Modifier.padding(padding)) {
                                MainSettings(onNavigate = {
                                    currentPage = it
                                    isMainPage = false
                                })
                            }
                        }
                    } else {
                        Scaffold(
                            containerColor = Color.Transparent,
                            contentWindowInsets = WindowInsets(0, 0, 0, 0),
                            topBar = {
                                TopAppBar(
                                    title = { Text(stringResource(currentPage.titleRes), fontWeight = FontWeight.Bold) },
                                    navigationIcon = {
                                        IconButton(onClick = {
                                            isMainPage = true
                                        }) {
                                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                                        }
                                    },
                                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                                )
                            }
                        ) { padding ->
                            BackHandler {
                                isMainPage = true
                            }
                            Box(modifier = Modifier.padding(padding)) {
                                SettingsContent(
                                    currentPage
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsContent(
    page: SettingsPage
) {
    when (page) {
        SettingsPage.APPEARANCE -> AppearanceSettings()
        SettingsPage.VIDEO -> VideoSettings()
        SettingsPage.CONTROL -> ControlSettings()
        SettingsPage.JAVA -> JavaSettings()
        SettingsPage.MISC -> MiscSettings()
        SettingsPage.EXPERIMENTAL -> ExperimentalSettings()
    }
}

@Composable
fun MainSettings(onNavigate: (SettingsPage) -> Unit) {
    val context = LocalContext.current

    val notificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.POST_NOTIFICATIONS
    } else {
        null
    }

    var hasNotificationPermission by remember {
        mutableStateOf(
            notificationPermission == null || ContextCompat.checkSelfPermission(context, notificationPermission) == PackageManager.PERMISSION_GRANTED
        )
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
        if (isGranted) {
            Toast.makeText(context, "Notification permission granted", Toast.LENGTH_SHORT).show()
        }
    }

    LazyColumn(contentPadding = PaddingValues(vertical = 12.dp)) {
        item {
            PreferenceGroup(title = stringResource(R.string.preference_category_main_categories)) {
                PreferenceItem(
                    title = stringResource(R.string.preference_appearance_title),
                    summary = stringResource(R.string.preference_appearance_description),
                    icon = painterResource(R.drawable.ic_px_theme),
                    onClick = { onNavigate(SettingsPage.APPEARANCE) }
                )
                PreferenceItem(
                    title = stringResource(R.string.preference_video_title),
                    summary = stringResource(R.string.preference_video_description),
                    icon = painterResource(R.drawable.ic_px_image),
                    onClick = { onNavigate(SettingsPage.VIDEO) }
                )
                PreferenceItem(
                    title = stringResource(R.string.preference_control_title),
                    summary = stringResource(R.string.preference_control_description),
                    icon = painterResource(R.drawable.ic_px_gamepad),
                    onClick = { onNavigate(SettingsPage.CONTROL) }
                )
                PreferenceItem(
                    title = stringResource(R.string.preference_java_title),
                    summary = stringResource(R.string.preference_java_description),
                    icon = painterResource(R.drawable.ic_px_java),
                    onClick = { onNavigate(SettingsPage.JAVA) }
                )
                PreferenceItem(
                    title = stringResource(R.string.preference_misc_title),
                    summary = stringResource(R.string.preference_misc_description),
                    icon = painterResource(R.drawable.ic_px_alt_sliders),
                    onClick = { onNavigate(SettingsPage.MISC) }
                )
                PreferenceItem(
                    title = stringResource(R.string.preference_experimental_title),
                    summary = stringResource(R.string.preference_experimental_description),
                    icon = painterResource(R.drawable.ic_px_experiment),
                    onClick = { onNavigate(SettingsPage.EXPERIMENTAL) }
                )
            }
        }

        item {
            PreferenceGroup {
                var forceEnglish by remember { mutableStateOf(LauncherPreferences.PREF_FORCE_ENGLISH) }
                PreferenceSwitch(
                    title = stringResource(R.string.preference_force_english_title),
                    summary = stringResource(R.string.preference_force_english_description),
                    icon = painterResource(R.drawable.ic_px_translate),
                    checked = forceEnglish,
                    onCheckedChange = {
                        forceEnglish = it
                        LauncherPreferences.PREF_FORCE_ENGLISH = it
                        LauncherPreferences.DEFAULT_PREF?.edit { putBoolean("force_english", it) }
                    }
                )

                if (notificationPermission != null && !hasNotificationPermission) {
                    PreferenceItem(
                        title = stringResource(R.string.preference_ask_for_notification_title),
                        summary = stringResource(R.string.preference_ask_for_notification_description),
                        icon = painterResource(R.drawable.ic_px_bell),
                        onClick = {
                            notificationLauncher.launch(notificationPermission)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AppearanceSettings() {
    val context = LocalContext.current
    val pickIconLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val file = File(context.filesDir, "drawer_button_icon.png")
                    FileOutputStream(file).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                    LauncherPreferences.PREF_DRAWER_BUTTON_ICON_PATH = file.absolutePath
                    LauncherPreferences.DEFAULT_PREF?.edit { putString("drawerButtonIconPath", file.absolutePath) }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to load icon", Toast.LENGTH_SHORT).show()
            }
        }
    }

    var showResetDialog by remember { mutableStateOf(false) }
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Logo") },
            text = { Text("Are you sure you want to reset the drawer button logo to default?") },
            confirmButton = {
                TextButton(onClick = {
                    LauncherPreferences.PREF_DRAWER_BUTTON_ICON_PATH = null
                    LauncherPreferences.DEFAULT_PREF?.edit { remove("drawerButtonIconPath") }
                    showResetDialog = false
                }) { Text("Reset") }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text("Cancel") }
            }
        )
    }

    var showThemeColorDialog by remember { mutableStateOf(false) }
    val currentSeedColor = LauncherPreferences.PREF_THEME_SEED_COLOR_STATE.intValue

    if (showThemeColorDialog) {
        var hue by remember { mutableFloatStateOf(0f) }
        LaunchedEffect(currentSeedColor) {
            val hsv = FloatArray(3)
            AndroidColor.colorToHSV(currentSeedColor, hsv)
            hue = hsv[0]
        }

        AlertDialog(
            onDismissRequest = { showThemeColorDialog = false },
            title = { Text("Custom Theme Color") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(Color(currentSeedColor), RoundedCornerShape(12.dp))
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Select Hue: ${hue.toInt()}")
                    Slider(
                        value = hue,
                        onValueChange = {
                            hue = it
                            val newColor = AndroidColor.HSVToColor(floatArrayOf(it, 0.82f, 0.96f))
                            LauncherPreferences.PREF_THEME_SEED_COLOR = newColor
                            LauncherPreferences.PREF_THEME_SEED_COLOR_STATE.intValue = newColor
                            LauncherPreferences.DEFAULT_PREF?.edit { putInt(LauncherPreferences.PREF_KEY_THEME_SEED_COLOR, newColor) }
                        },
                        valueRange = 0f..360f
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeColorDialog = false }) { Text("Done") }
            }
        )
    }

    var transparency by remember { mutableFloatStateOf(LauncherPreferences.PREF_BACKGROUND_TRANSPARENCY_STATE.value * 100f) }
    var blurEnabled by remember { mutableStateOf(LauncherPreferences.PREF_BACKGROUND_BLUR_ENABLED_STATE.value) }
    var showResetBackgroundDialog by remember { mutableStateOf(false) }

    if (showResetBackgroundDialog) {
        AlertDialog(
            onDismissRequest = { showResetBackgroundDialog = false },
            title = { Text("Reset Background") },
            text = { Text("Are you sure you want to reset the background and its settings?") },
            confirmButton = {
                TextButton(onClick = {
                    LauncherPreferences.PREF_BACKGROUND_PATH = null
                    LauncherPreferences.PREF_BACKGROUND_PATH_STATE.value = null

                    LauncherPreferences.PREF_BACKGROUND_BLUR_ENABLED = false
                    LauncherPreferences.PREF_BACKGROUND_BLUR_ENABLED_STATE.value = false
                    blurEnabled = false

                    LauncherPreferences.PREF_BACKGROUND_TRANSPARENCY = 0f
                    LauncherPreferences.PREF_BACKGROUND_TRANSPARENCY_STATE.value = 0f
                    transparency = 0f

                    LauncherPreferences.DEFAULT_PREF?.edit {
                        remove("backgroundPath")
                        putBoolean("backgroundBlurEnabled", false)
                        putFloat("backgroundTransparency", 0f)
                    }
                    showResetBackgroundDialog = false
                }) { Text("Reset") }
            },
            dismissButton = {
                @Suppress("DEPRECATION")
                TextButton(onClick = { showResetBackgroundDialog = false }) { Text("Cancel") }
            }
        )
    }

    LazyColumn(contentPadding = PaddingValues(vertical = 12.dp)) {
        item {
            PreferenceGroup(title = "Launcher Theme Type") {
                var themeTypeEnabled by remember { mutableStateOf(LauncherPreferences.PREF_THEME_TYPE_ENABLED_STATE.value) }
                PreferenceSwitch(
                    title = "Preset Theme Mode",
                    summary = "Override individual colors with high-curated presets",
                    checked = themeTypeEnabled,
                    onCheckedChange = {
                        themeTypeEnabled = it
                        LauncherPreferences.PREF_THEME_TYPE_ENABLED = it
                        LauncherPreferences.PREF_THEME_TYPE_ENABLED_STATE.value = it
                        LauncherPreferences.DEFAULT_PREF?.edit { putBoolean(LauncherPreferences.PREF_KEY_THEME_TYPE_ENABLED, it) }
                    }
                )

                if (themeTypeEnabled) {
                    var themeTypeMode by remember { mutableStateOf(LauncherPreferences.PREF_THEME_TYPE_MODE_STATE.value) }
                    PreferenceList(
                        title = "Theme Selection",
                        entries = arrayOf("Hyper Launcher (default)", "Crynoix Launcher(Collab)"),
                        entryValues = arrayOf("monochrome", "crynoix"),
                        selectedValue = themeTypeMode,
                        onValueChange = {
                            if (it == "crynoix" && themeTypeMode != "crynoix") {
                                LauncherPreferences.PREF_SHOW_CRYNOIX_LOADING.value = true
                            }
                            themeTypeMode = it
                            LauncherPreferences.PREF_THEME_TYPE_MODE = it
                            LauncherPreferences.PREF_THEME_TYPE_MODE_STATE.value = it
                            LauncherPreferences.DEFAULT_PREF?.edit { putString(LauncherPreferences.PREF_KEY_THEME_TYPE_MODE, it) }
                        }
                    )
                }
            }
        }

        item {
            val themeTypeEnabled = LauncherPreferences.PREF_THEME_TYPE_ENABLED_STATE.value
            PreferenceGroup(title = stringResource(R.string.preference_appearance_title)) {
                var appTheme by remember { mutableStateOf(LauncherPreferences.prefAppThemeState.value) }
                PreferenceList(
                    title = stringResource(R.string.preference_theme_title),
                    entries = stringArrayResource(R.array.theme_names),
                    entryValues = stringArrayResource(R.array.theme_values),
                    selectedValue = appTheme,
                    onValueChange = {
                        appTheme = it
                        LauncherPreferences.PREF_APP_THEME = it
                        LauncherPreferences.prefAppThemeState.value = it
                        LauncherPreferences.DEFAULT_PREF?.edit { putString("appTheme", it) }
                    },
                    icon = painterResource(R.drawable.ic_px_theme),
                    enabled = !themeTypeEnabled
                )

                var themeColorEnabled by remember { mutableStateOf(LauncherPreferences.PREF_THEME_COLOR_ENABLED_STATE.value) }
                PreferenceSwitch(
                    title = "Custom Theme Color",
                    summary = "Override launcher colors with a custom seed",
                    checked = themeColorEnabled,
                    onCheckedChange = {
                        themeColorEnabled = it
                        LauncherPreferences.PREF_THEME_COLOR_ENABLED = it
                        LauncherPreferences.PREF_THEME_COLOR_ENABLED_STATE.value = it
                        LauncherPreferences.DEFAULT_PREF?.edit { putBoolean(LauncherPreferences.PREF_KEY_THEME_COLOR_ENABLED, it) }
                    },
                    enabled = !themeTypeEnabled
                )

                if (themeColorEnabled && !themeTypeEnabled) {
                    PreferenceItem(
                        title = "Pick Seed Color",
                        summary = "Current: #${Integer.toHexString(currentSeedColor).uppercase()}",
                        onClick = { showThemeColorDialog = true }
                    )
                }
            }
        }

        item {
            PreferenceGroup(title = "Navigation Animation") {
                var animPreset by remember { mutableStateOf(LauncherPreferences.PREF_TRANSITION_ANIMATION_STATE.value) }
                PreferenceList(
                    title = "Transition Preset",
                    entries = arrayOf("Default", "Fade", "Bounce from Top"),
                    entryValues = arrayOf("default", "fade", "bounce"),
                    selectedValue = animPreset,
                    onValueChange = {
                        animPreset = it
                        LauncherPreferences.PREF_TRANSITION_ANIMATION = it
                        LauncherPreferences.PREF_TRANSITION_ANIMATION_STATE.value = it
                        LauncherPreferences.DEFAULT_PREF?.edit { putString("transitionAnimation", it) }
                    }
                )

                var duration by remember { mutableFloatStateOf(LauncherPreferences.PREF_TRANSITION_DURATION_STATE.value.toFloat()) }
                PreferenceSlider(
                    title = "Duration (ms)",
                    value = duration,
                    onValueChange = {
                        duration = it
                        val d = it.toInt()
                        LauncherPreferences.PREF_TRANSITION_DURATION = d
                        LauncherPreferences.PREF_TRANSITION_DURATION_STATE.value = d
                        LauncherPreferences.DEFAULT_PREF?.edit { putInt("transitionDuration", d) }
                    },
                    valueRange = 100f..1000f
                )

                var intensity by remember { mutableFloatStateOf(LauncherPreferences.PREF_TRANSITION_INTENSITY_STATE.value * 100f) }
                PreferenceSlider(
                    title = "Animation Intensity",
                    value = intensity,
                    onValueChange = {
                        intensity = it
                        val i = it / 100f
                        LauncherPreferences.PREF_TRANSITION_INTENSITY = i
                        LauncherPreferences.PREF_TRANSITION_INTENSITY_STATE.value = i
                        LauncherPreferences.DEFAULT_PREF?.edit { putFloat("transitionIntensity", i) }
                    },
                    valueRange = 50f..200f
                )
            }
        }

        item {
            PreferenceGroup(title = "Background Settings") {
                val hasBackground = LauncherPreferences.PREF_BACKGROUND_PATH_STATE.value != null
                val pickBackgroundLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                    if (uri != null) {
                        try {
                            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                                val file = File(context.filesDir, "launcher_background.png")
                                file.outputStream().use { outputStream ->
                                    inputStream.copyTo(outputStream)
                                }
                                val path = file.absolutePath
                                LauncherPreferences.PREF_BACKGROUND_PATH = path
                                LauncherPreferences.PREF_BACKGROUND_PATH_STATE.value = path
                                LauncherPreferences.DEFAULT_PREF?.edit { putString("backgroundPath", path) }
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Failed to load background", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                PreferenceItem(
                    title = "Change Background",
                    summary = "Select an image for the launcher background",
                    icon = painterResource(R.drawable.ic_px_image),
                    onClick = { pickBackgroundLauncher.launch("image/*") }
                )

                PreferenceItem(
                    title = "Reset Background",
                    summary = "Restore default background",
                    icon = painterResource(R.drawable.ic_px_trash),
                    onClick = {
                        showResetBackgroundDialog = true
                    }
                )

                PreferenceSlider(
                    title = "Background Transparency",
                    value = transparency,
                    enabled = hasBackground,
                    onValueChange = {
                        transparency = it
                        val alpha = it / 100f
                        LauncherPreferences.PREF_BACKGROUND_TRANSPARENCY = alpha
                        LauncherPreferences.PREF_BACKGROUND_TRANSPARENCY_STATE.value = alpha
                        LauncherPreferences.DEFAULT_PREF?.edit { putFloat("backgroundTransparency", alpha) }
                    },
                    valueRange = 0f..100f
                )

                PreferenceSwitch(
                    title = "Blur Effect",
                    summary = "Apply blur to the background image",
                    enabled = hasBackground,
                    checked = blurEnabled,
                    onCheckedChange = {
                        blurEnabled = it
                        LauncherPreferences.PREF_BACKGROUND_BLUR_ENABLED = it
                        LauncherPreferences.PREF_BACKGROUND_BLUR_ENABLED_STATE.value = it
                        LauncherPreferences.DEFAULT_PREF?.edit { putBoolean("backgroundBlurEnabled", it) }
                    }
                )

                if (blurEnabled) {
                    var blurIntensity by remember { mutableFloatStateOf(LauncherPreferences.PREF_BACKGROUND_BLUR_STATE.value * 100f) }
                    PreferenceSlider(
                        title = "Blur Intensity",
                        value = blurIntensity,
                        enabled = hasBackground,
                        onValueChange = {
                            blurIntensity = it
                            val intensity = it / 100f
                            LauncherPreferences.PREF_BACKGROUND_BLUR = intensity
                            LauncherPreferences.PREF_BACKGROUND_BLUR_STATE.value = intensity
                            LauncherPreferences.DEFAULT_PREF?.edit { putFloat("backgroundBlur", intensity) }
                        },
                        valueRange = 0f..100f
                    )
                }
            }
        }

        item {
            PreferenceGroup(title = "Drawer Pull Button") {
                PreferenceItem(
                    title = "Change Button Logo",
                    summary = "Pick a custom image for the pull button",
                    icon = painterResource(R.drawable.ic_px_image),
                    onClick = { pickIconLauncher.launch("image/*") }
                )

                PreferenceItem(
                    title = "Reset Button Logo",
                    summary = "Restore the default settings icon",
                    icon = painterResource(R.drawable.ic_px_trash),
                    onClick = { showResetDialog = true }
                )

                var preset by remember { mutableStateOf(LauncherPreferences.PREF_DRAWER_BUTTON_PRESET) }
                var posX by remember { mutableFloatStateOf(LauncherPreferences.PREF_DRAWER_BUTTON_X) }
                var posY by remember { mutableFloatStateOf(LauncherPreferences.PREF_DRAWER_BUTTON_Y) }

                PreferenceList(
                    title = "Position Preset",
                    entries = arrayOf("Top Center", "Bottom Left", "Bottom Right", "Custom"),
                    entryValues = arrayOf("top_center", "bottom_left", "bottom_right", "custom"),
                    selectedValue = preset,
                    onValueChange = { newValue ->
                        preset = newValue
                        LauncherPreferences.PREF_DRAWER_BUTTON_PRESET = newValue

                        when (newValue) {
                            "top_center" -> { posX = 50f; posY = 0f }
                            "bottom_left" -> { posX = 0f; posY = 100f }
                            "bottom_right" -> { posX = 100f; posY = 100f }
                        }

                        if (newValue != "custom") {
                            LauncherPreferences.PREF_DRAWER_BUTTON_X = posX
                            LauncherPreferences.PREF_DRAWER_BUTTON_Y = posY
                            LauncherPreferences.DEFAULT_PREF?.edit {
                                putInt("drawerButtonX", posX.toInt())
                                putInt("drawerButtonY", posY.toInt())
                                putString("drawerButtonPreset", newValue)
                            }
                        }
                    }
                )

                if (preset == "custom") {
                    PreferenceSlider(
                        title = "Horizontal Position (%)",
                        value = posX,
                        onValueChange = {
                            posX = it
                            LauncherPreferences.PREF_DRAWER_BUTTON_X = it
                            LauncherPreferences.DEFAULT_PREF?.edit { putInt("drawerButtonX", it.toInt()) }
                        },
                        valueRange = 0f..100f
                    )
                    PreferenceSlider(
                        title = "Vertical Position (%)",
                        value = posY,
                        onValueChange = {
                            posY = it
                            LauncherPreferences.PREF_DRAWER_BUTTON_Y = it
                            LauncherPreferences.DEFAULT_PREF?.edit { putInt("drawerButtonY", it.toInt()) }
                        },
                        valueRange = 0f..100f
                    )
                }

                var bgOpacity by remember { mutableFloatStateOf(LauncherPreferences.PREF_DRAWER_BUTTON_BG_OPACITY * 100f) }
                PreferenceSlider(
                    title = "Background Opacity",
                    value = bgOpacity,
                    onValueChange = {
                        bgOpacity = it
                        LauncherPreferences.PREF_DRAWER_BUTTON_BG_OPACITY = it / 100f
                        LauncherPreferences.DEFAULT_PREF?.edit { putInt("drawerButtonBgOpacity", it.toInt()) }
                    },
                    valueRange = 0f..100f
                )

                var iconOpacity by remember { mutableFloatStateOf(LauncherPreferences.PREF_DRAWER_BUTTON_ICON_OPACITY * 100f) }
                PreferenceSlider(
                    title = "Button Opacity",
                    value = iconOpacity,
                    onValueChange = {
                        iconOpacity = it
                        LauncherPreferences.PREF_DRAWER_BUTTON_ICON_OPACITY = it / 100f
                        LauncherPreferences.DEFAULT_PREF?.edit { putInt("drawerButtonIconOpacity", it.toInt()) }
                    },
                    valueRange = 0f..100f
                )

                var size by remember { mutableFloatStateOf(LauncherPreferences.PREF_DRAWER_BUTTON_SIZE.toFloat()) }
                PreferenceSlider(
                    title = "Button Size",
                    value = size,
                    onValueChange = {
                        size = it
                        LauncherPreferences.PREF_DRAWER_BUTTON_SIZE = it.toInt()
                        LauncherPreferences.DEFAULT_PREF?.edit { putInt("drawerButtonSize", it.toInt()) }
                    },
                    valueRange = 20f..100f
                )

                var cornerRadius by remember { mutableFloatStateOf(LauncherPreferences.PREF_DRAWER_BUTTON_CORNER_RADIUS.toFloat()) }
                PreferenceSlider(
                    title = "Background Corner Radius",
                    value = cornerRadius,
                    onValueChange = {
                        cornerRadius = it
                        LauncherPreferences.PREF_DRAWER_BUTTON_CORNER_RADIUS = it.toInt()
                        LauncherPreferences.DEFAULT_PREF?.edit { putInt("drawerButtonCornerRadius", it.toInt()) }
                    },
                    valueRange = 0f..50f
                )

                var movable by remember { mutableStateOf(LauncherPreferences.PREF_DRAWER_BUTTON_MOVABLE) }
                PreferenceSwitch(
                    title = "Hold to Move",
                    summary = "Allow moving the button by dragging it",
                    checked = movable,
                    onCheckedChange = {
                        movable = it
                        LauncherPreferences.PREF_DRAWER_BUTTON_MOVABLE = it
                        LauncherPreferences.DEFAULT_PREF?.edit { putBoolean("drawerButtonMovable", it) }
                    }
                )
            }
        }
    }
}

@Composable
fun VideoSettings() {
    val context = LocalContext.current
    val compatibleRenderers = remember { RendererCompatUtil.getCompatibleRenderers(context) }
    val rendererNames = remember { compatibleRenderers.rendererDisplayNames?.filterNotNull()?.toTypedArray() ?: emptyArray() }
    val rendererValues = remember { compatibleRenderers.rendererIds.filterNotNull().toTypedArray() }

    val supportsSustainedPerf = remember { Build.VERSION.SDK_INT >= Build.VERSION_CODES.N }

    val allPlugins = remember {
        listOfNotNull(
            LibraryPlugin.discoverPlugin(context, LibraryPlugin.ID_ANGLE_PLUGIN),
            LibraryPlugin.discoverPlugin(context, LibraryPlugin.ID_FFMPEG_PLUGIN)
        )
    }
    val hasAnglePlugin = remember { allPlugins.any { it.id == LibraryPlugin.ID_ANGLE_PLUGIN } }

    LazyColumn(contentPadding = PaddingValues(vertical = 12.dp)) {
        item {
            PreferenceGroup(title = stringResource(R.string.preference_category_video)) {
                var renderer by remember { mutableStateOf(LauncherPreferences.PREF_RENDERER) }
                PreferenceList(
                    title = stringResource(R.string.mcl_setting_category_renderer),
                    entries = rendererNames,
                    entryValues = rendererValues,
                    selectedValue = renderer,
                    onValueChange = {
                        renderer = it
                        LauncherPreferences.PREF_RENDERER = it
                        LauncherPreferences.DEFAULT_PREF?.edit { putString("renderer", it) }
                    },
                    icon = painterResource(R.drawable.ic_px_image_renderer)
                )

                var preferredBackend by remember { mutableStateOf(LauncherPreferences.PREF_PREFERRED_GRAPHICS_BACKEND) }
                PreferenceList(
                    title = stringResource(R.string.mcl_setting_title_preferred_graphics_backend),
                    summary = stringResource(R.string.mcl_setting_subtitle_preferred_graphics_backend),
                    entries = stringArrayResource(R.array.graphics_backend_names),
                    entryValues = stringArrayResource(R.array.graphics_backend_values),
                    selectedValue = preferredBackend,
                    onValueChange = {
                        preferredBackend = it
                        LauncherPreferences.PREF_PREFERRED_GRAPHICS_BACKEND = it
                        LauncherPreferences.DEFAULT_PREF?.edit { putString("preferredGraphicsBackend", it) }
                    },
                    icon = painterResource(R.drawable.ic_px_image_renderer)
                )

                var ignoreNotch by remember { mutableStateOf(LauncherPreferences.PREF_IGNORE_NOTCH) }
                PreferenceSwitch(
                    title = stringResource(R.string.mcl_setting_title_ignore_notch),
                    summary = stringResource(R.string.mcl_setting_subtitle_ignore_notch),
                    icon = painterResource(R.drawable.ic_px_viewport_expand),
                    checked = ignoreNotch,
                    onCheckedChange = {
                        ignoreNotch = it
                        LauncherPreferences.PREF_IGNORE_NOTCH = it
                        LauncherPreferences.DEFAULT_PREF?.edit { putBoolean("ignoreNotch", it) }
                    }
                )

                var resRatio by remember { mutableFloatStateOf(LauncherPreferences.PREF_SCALE_FACTOR * 100f) }
                PreferenceSlider(
                    title = stringResource(R.string.mcl_setting_title_resolution_scaler),
                    summary = stringResource(R.string.mcl_setting_subtitle_resolution_scaler),
                    value = resRatio,
                    onValueChange = {
                        resRatio = it
                        LauncherPreferences.PREF_SCALE_FACTOR = it / 100f
                        LauncherPreferences.DEFAULT_PREF?.edit { putInt("resolutionRatio", it.toInt()) }
                    },
                    valueRange = 25f..100f,
                    icon = painterResource(R.drawable.ic_px_resolution)
                )

                if (supportsSustainedPerf) {
                    var sustainedPerf by remember { mutableStateOf(LauncherPreferences.PREF_SUSTAINED_PERFORMANCE) }
                    PreferenceSwitch(
                        title = stringResource(R.string.preference_sustained_performance_title),
                        summary = stringResource(R.string.preference_sustained_performance_description),
                        checked = sustainedPerf,
                        onCheckedChange = {
                            sustainedPerf = it
                            LauncherPreferences.PREF_SUSTAINED_PERFORMANCE = it
                            LauncherPreferences.DEFAULT_PREF?.edit { putBoolean("sustainedPerformance", it) }
                        }
                    )
                }

                var altSurface by remember { mutableStateOf(LauncherPreferences.PREF_USE_ALTERNATE_SURFACE) }
                PreferenceSwitch(
                    title = stringResource(R.string.mcl_setting_title_use_surface_view),
                    summary = stringResource(R.string.mcl_setting_subtitle_use_surface_view),
                    checked = altSurface,
                    onCheckedChange = {
                        altSurface = it
                        LauncherPreferences.PREF_USE_ALTERNATE_SURFACE = it
                        LauncherPreferences.DEFAULT_PREF?.edit { putBoolean("alternate_surface", it) }
                    }
                )

                var forceVsync by remember { mutableStateOf(LauncherPreferences.PREF_FORCE_VSYNC) }
                PreferenceSwitch(
                    title = stringResource(R.string.preference_force_vsync_title),
                    summary = stringResource(R.string.preference_force_vsync_description),
                    checked = forceVsync,
                    onCheckedChange = {
                        forceVsync = it
                        LauncherPreferences.PREF_FORCE_VSYNC = it
                        LauncherPreferences.DEFAULT_PREF?.edit { putBoolean("force_vsync", it) }
                    }
                )

                if (hasAnglePlugin) {
                    var useAngle by remember { mutableStateOf(LauncherPreferences.PREF_USE_ANGLE) }
                    PreferenceSwitch(
                        title = stringResource(R.string.preference_use_angle_title),
                        summary = stringResource(R.string.preference_use_angle_description),
                        checked = useAngle,
                        onCheckedChange = {
                            useAngle = it
                            LauncherPreferences.PREF_USE_ANGLE = it
                            LauncherPreferences.DEFAULT_PREF?.edit { putBoolean("use_angle", it) }
                        }
                    )
                }

                var vsinkInZink by remember { mutableStateOf(LauncherPreferences.PREF_VSYNC_IN_ZINK) }
                PreferenceSwitch(
                    title = stringResource(R.string.preference_vsync_in_zink_title),
                    summary = stringResource(R.string.preference_vsync_in_zink_description),
                    checked = vsinkInZink,
                    onCheckedChange = {
                        vsinkInZink = it
                        LauncherPreferences.PREF_VSYNC_IN_ZINK = it
                        LauncherPreferences.DEFAULT_PREF?.edit { putBoolean("vsync_in_zink", it) }
                    }
                )
            }
        }
    }
}

@Composable
fun ControlSettings() {
    val context = LocalContext.current
    val supportsGyro = remember { Tools.deviceSupportsGyro(context) }

    val pickMouseIconLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val file = File(context.filesDir, "custom_mouse.png")
                    FileOutputStream(file).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                    LauncherPreferences.PREF_MOUSE_ICON_PATH = file.absolutePath
                    LauncherPreferences.PREF_MOUSE_ICON_PATH_STATE.value = file.absolutePath
                    LauncherPreferences.DEFAULT_PREF?.edit { putString("mouseIconPath", file.absolutePath) }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to load mouse icon", Toast.LENGTH_SHORT).show()
            }
        }
    }

    var showResetMouseDialog by remember { mutableStateOf(false) }
    if (showResetMouseDialog) {
        AlertDialog(
            onDismissRequest = { showResetMouseDialog = false },
            title = { Text("Reset Mouse Cursor") },
            text = { Text("Are you sure you want to reset the mouse cursor to default?") },
            confirmButton = {
                TextButton(onClick = {
                    LauncherPreferences.PREF_MOUSE_ICON_PATH = null
                    LauncherPreferences.PREF_MOUSE_ICON_PATH_STATE.value = null
                    LauncherPreferences.PREF_MOUSE_HOTSPOT_X = 0f
                    LauncherPreferences.PREF_MOUSE_HOTSPOT_Y = 0f
                    LauncherPreferences.DEFAULT_PREF?.edit {
                        remove("mouseIconPath")
                        remove("mouseHotspotX")
                        remove("mouseHotspotY")
                    }
                    showResetMouseDialog = false
                }) { Text("Reset") }
            },
            dismissButton = {
                TextButton(onClick = { showResetMouseDialog = false }) { Text("Cancel") }
            }
        )
    }

    var showHotspotDialog by remember { mutableStateOf(false) }
    if (showHotspotDialog) {
        MouseHotspotDialog(
            onDismiss = { showHotspotDialog = false }
        )
    }

    LazyColumn(contentPadding = PaddingValues(vertical = 12.dp)) {
        item {
            PreferenceGroup {
                PreferenceItem(
                    title = stringResource(R.string.preference_edit_controls_title),
                    summary = stringResource(R.string.preference_edit_controls_summary),
                    icon = painterResource(R.drawable.ic_px_gamepad),
                    onClick = {
                        context.startActivity(Intent(context, CustomControlsActivity::class.java))
                    }
                )
            }
        }

        item {
            PreferenceGroup(title = stringResource(R.string.preference_category_gestures)) {
                var disableGestures by remember { mutableStateOf(LauncherPreferences.PREF_DISABLE_GESTURES) }
                PreferenceSwitch(
                    title = stringResource(R.string.mcl_disable_gestures),
                    summary = stringResource(R.string.mcl_disable_gestures_subtitle),
                    icon = painterResource(R.drawable.ic_px_nogestures),
                    checked = disableGestures,
                    onCheckedChange = {
                        disableGestures = it
                        LauncherPreferences.PREF_DISABLE_GESTURES = it
                        LauncherPreferences.DEFAULT_PREF?.edit { putBoolean("disableGestures", it) }
                    }
                )

                var disableDoubleTap by remember { mutableStateOf(LauncherPreferences.PREF_DISABLE_SWAP_HAND) }
                PreferenceSwitch(
                    title = stringResource(R.string.mcl_disable_swap_hand),
                    summary = stringResource(R.string.mcl_disable_swap_hand_subtitle),
                    icon = painterResource(R.drawable.ic_px_switch_arrows_x),
                    checked = disableDoubleTap,
                    onCheckedChange = {
                        disableDoubleTap = it
                        LauncherPreferences.PREF_DISABLE_SWAP_HAND = it
                        LauncherPreferences.DEFAULT_PREF?.edit { putBoolean("disableDoubleTap", it) }
                    }
                )

                var longPressTrigger by remember { mutableFloatStateOf(LauncherPreferences.PREF_LONGPRESS_TRIGGER.toFloat()) }
                PreferenceSlider(
                    title = stringResource(R.string.mcl_setting_title_longpresstrigger),
                    summary = stringResource(R.string.mcl_setting_subtitle_longpresstrigger),
                    icon = painterResource(R.drawable.ic_px_gestures),
                    value = longPressTrigger,
                    onValueChange = {
                        longPressTrigger = it
                        LauncherPreferences.PREF_LONGPRESS_TRIGGER = it.toInt()
                        LauncherPreferences.DEFAULT_PREF?.edit { putInt("timeLongPressTrigger", it.toInt()) }
                    },
                    valueRange = 100f..1000f
                )
            }
        }

        item {
            PreferenceGroup(title = "Appearance") {
                var buttonScale by remember { mutableFloatStateOf(LauncherPreferences.PREF_BUTTONSIZE) }
                PreferenceSlider(
                    title = stringResource(R.string.mcl_setting_title_buttonscale),
                    summary = stringResource(R.string.mcl_setting_subtitle_buttonscale),
                    icon = painterResource(R.drawable.ic_px_control_size),
                    value = buttonScale,
                    onValueChange = {
                        buttonScale = it
                        LauncherPreferences.PREF_BUTTONSIZE = it
                        LauncherPreferences.DEFAULT_PREF?.edit { putInt("buttonscale", it.toInt()) }
                    },
                    valueRange = 50f..200f
                )

                var buttonAllCaps by remember { mutableStateOf(LauncherPreferences.PREF_BUTTON_ALL_CAPS) }
                PreferenceSwitch(
                    title = stringResource(R.string.mcl_setting_title_buttonallcaps),
                    summary = stringResource(R.string.mcl_setting_subtitle_buttonallcaps),
                    icon = painterResource(R.drawable.ic_px_letters),
                    checked = buttonAllCaps,
                    onCheckedChange = {
                        buttonAllCaps = it
                        LauncherPreferences.PREF_BUTTON_ALL_CAPS = it
                        LauncherPreferences.DEFAULT_PREF?.edit { putBoolean("buttonAllCaps", it) }
                    }
                )

                var mouseScale by remember { mutableFloatStateOf(LauncherPreferences.PREF_MOUSESCALE * 100f) }
                PreferenceSlider(
                    title = stringResource(R.string.mcl_setting_title_mousescale),
                    summary = stringResource(R.string.mcl_setting_subtitle_mousescale),
                    icon = painterResource(R.drawable.ic_px_pointer_size),
                    value = mouseScale,
                    onValueChange = {
                        mouseScale = it
                        LauncherPreferences.PREF_MOUSESCALE = it / 100f
                        LauncherPreferences.DEFAULT_PREF?.edit { putInt("mousescale", it.toInt()) }
                    },
                    valueRange = 25f..300f
                )
            }
        }

        item {
            PreferenceGroup(title = "Mouse Customization") {
                PreferenceItem(
                    title = "Change Mouse Cursor",
                    summary = "Select an image for the mouse cursor",
                    icon = painterResource(R.drawable.ic_px_mouse),
                    onClick = { pickMouseIconLauncher.launch("image/*") }
                )

                PreferenceItem(
                    title = "Set Mouse Hotspot",
                    summary = "Adjust the point where the mouse clicks",
                    icon = painterResource(R.drawable.ic_px_virtual_mouse),
                    onClick = { showHotspotDialog = true }
                )

                PreferenceItem(
                    title = "Reset Mouse Cursor",
                    summary = "Restore default mouse cursor",
                    icon = painterResource(R.drawable.ic_px_trash),
                    onClick = { showResetMouseDialog = true }
                )
            }
        }

        item {
            PreferenceGroup(title = stringResource(R.string.preference_category_virtual_mouse)) {
                var mouseSpeed by remember { mutableFloatStateOf(LauncherPreferences.PREF_MOUSESPEED * 100f) }
                PreferenceSlider(
                    title = stringResource(R.string.mcl_setting_title_mousespeed),
                    summary = stringResource(R.string.mcl_setting_subtitle_mousespeed),
                    icon = painterResource(R.drawable.ic_px_speed),
                    value = mouseSpeed,
                    onValueChange = {
                        mouseSpeed = it
                        LauncherPreferences.PREF_MOUSESPEED = it / 100f
                        LauncherPreferences.DEFAULT_PREF?.edit { putInt("mousespeed", it.toInt()) }
                    },
                    valueRange = 25f..300f
                )

                var mouseStart by remember { mutableStateOf(LauncherPreferences.PREF_VIRTUAL_MOUSE_START) }
                PreferenceSwitch(
                    title = stringResource(R.string.preference_mouse_start_title),
                    summary = stringResource(R.string.preference_mouse_start_description),
                    icon = painterResource(R.drawable.ic_px_virtual_mouse),
                    checked = mouseStart,
                    onCheckedChange = {
                        mouseStart = it
                        LauncherPreferences.PREF_VIRTUAL_MOUSE_START = it
                        LauncherPreferences.DEFAULT_PREF?.edit { putBoolean("mouse_start", it) }
                    }
                )

                var physicalMouse by remember { mutableStateOf(LauncherPreferences.PREF_ENABLE_PHYSICAL_MOUSE) }
                PreferenceSwitch(
                    title = stringResource(R.string.preference_physical_mouse_title),
                    summary = stringResource(R.string.preference_physical_mouse_description),
                    icon = painterResource(R.drawable.ic_px_mouse),
                    checked = physicalMouse,
                    onCheckedChange = {
                        physicalMouse = it
                        LauncherPreferences.PREF_ENABLE_PHYSICAL_MOUSE = it
                        LauncherPreferences.DEFAULT_PREF?.edit { putBoolean("enable_physical_mouse", it) }
                    }
                )
            }
        }

        if (supportsGyro) {
            item {
                PreferenceGroup(title = stringResource(R.string.preference_category_gyro_controls)) {
                    var enableGyro by remember { mutableStateOf(LauncherPreferences.PREF_ENABLE_GYRO) }
                    PreferenceSwitch(
                        title = stringResource(R.string.preference_enable_gyro_title),
                        summary = stringResource(R.string.preference_enable_gyro_description),
                        checked = enableGyro,
                        onCheckedChange = {
                            enableGyro = it
                            LauncherPreferences.PREF_ENABLE_GYRO = it
                            LauncherPreferences.DEFAULT_PREF?.edit { putBoolean("enableGyro", it) }
                        }
                    )

                    if (enableGyro) {
                        var gyroSens by remember { mutableFloatStateOf(LauncherPreferences.PREF_GYRO_SENSITIVITY * 100f) }
                        PreferenceSlider(
                            title = stringResource(R.string.preference_gyro_sensitivity_title),
                            summary = stringResource(R.string.preference_gyro_sensitivity_description),
                            value = gyroSens,
                            onValueChange = {
                                gyroSens = it
                                LauncherPreferences.PREF_GYRO_SENSITIVITY = it / 100f
                                LauncherPreferences.DEFAULT_PREF?.edit { putInt("gyroSensitivity", it.toInt()) }
                            },
                            valueRange = 25f..300f
                        )

                        var gyroRate by remember { mutableFloatStateOf(LauncherPreferences.PREF_GYRO_SAMPLE_RATE.toFloat()) }
                        PreferenceSlider(
                            title = stringResource(R.string.preference_gyro_sample_rate_title),
                            summary = stringResource(R.string.preference_gyro_sample_rate_description),
                            value = gyroRate,
                            onValueChange = {
                                gyroRate = it
                                LauncherPreferences.PREF_GYRO_SAMPLE_RATE = it.toInt()
                                LauncherPreferences.DEFAULT_PREF?.edit { putInt("gyroSampleRate", it.toInt()) }
                            },
                            valueRange = 5f..50f
                        )

                        var gyroSmoothing by remember { mutableStateOf(LauncherPreferences.PREF_GYRO_SMOOTHING) }
                        PreferenceSwitch(
                            title = stringResource(R.string.preference_gyro_smoothing_title),
                            summary = stringResource(R.string.preference_gyro_smoothing_description),
                            checked = gyroSmoothing,
                            onCheckedChange = {
                                gyroSmoothing = it
                                LauncherPreferences.PREF_GYRO_SMOOTHING = it
                                LauncherPreferences.DEFAULT_PREF?.edit { putBoolean("gyroSmoothing", it) }
                            }
                        )

                        var gyroInvertX by remember { mutableStateOf(LauncherPreferences.PREF_GYRO_INVERT_X) }
                        PreferenceSwitch(
                            title = stringResource(R.string.preference_gyro_invert_x_axis),
                            summary = stringResource(R.string.preference_gyro_invert_x_axis_description),
                            checked = gyroInvertX,
                            onCheckedChange = {
                                gyroInvertX = it
                                LauncherPreferences.PREF_GYRO_INVERT_X = it
                                LauncherPreferences.DEFAULT_PREF?.edit { putBoolean("gyroInvertX", it) }
                            }
                        )

                        var gyroInvertY by remember { mutableStateOf(LauncherPreferences.PREF_GYRO_INVERT_Y) }
                        PreferenceSwitch(
                            title = stringResource(R.string.preference_gyro_invert_y_axis),
                            summary = stringResource(R.string.preference_gyro_invert_y_axis_description),
                            checked = gyroInvertY,
                            onCheckedChange = {
                                gyroInvertY = it
                                LauncherPreferences.PREF_GYRO_INVERT_Y = it
                                LauncherPreferences.DEFAULT_PREF?.edit { putBoolean("gyroInvertY", it) }
                            }
                        )
                    }
                }
            }
        }

        item {
            PreferenceGroup(title = stringResource(R.string.preference_category_controller_settings)) {
                PreferenceItem(title = stringResource(R.string.preference_remap_controller_title), summary = stringResource(R.string.preference_remap_controller_description))
                PreferenceItem(title = stringResource(R.string.preference_wipe_controller_title), summary = stringResource(R.string.preference_wipe_controller_description))

                var deadzone by remember { mutableFloatStateOf(LauncherPreferences.PREF_DEADZONE_SCALE * 100f) }
                PreferenceSlider(
                    title = stringResource(R.string.preference_deadzone_scale_title),
                    summary = stringResource(R.string.preference_deadzone_scale_description),
                    value = deadzone,
                    onValueChange = {
                        deadzone = it
                        LauncherPreferences.PREF_DEADZONE_SCALE = it / 100f
                        LauncherPreferences.DEFAULT_PREF?.edit { putInt("gamepad_deadzone_scale", it.toInt()) }
                    },
                    valueRange = 50f..200f
                )
            }
        }
    }
}

@Composable
fun MouseHotspotDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val imagePath = LauncherPreferences.PREF_MOUSE_ICON_PATH
    val bitmap = remember(imagePath) {
        if (imagePath != null && File(imagePath).exists()) {
            BitmapFactory.decodeFile(imagePath)
        } else {
            BitmapFactory.decodeResource(context.resources, R.drawable.ic_mouse_pointer)
        }
    }

    var hotspotX by remember { mutableFloatStateOf(LauncherPreferences.PREF_MOUSE_HOTSPOT_X) }
    var hotspotY by remember { mutableFloatStateOf(LauncherPreferences.PREF_MOUSE_HOTSPOT_Y) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adjust Mouse Hotspot") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Drag the knob to set the click point", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .background(Color.Black.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        var containerWidth by remember { mutableIntStateOf(0) }
                        var containerHeight by remember { mutableIntStateOf(0) }

                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .onGloballyPositioned {
                                    containerWidth = it.size.width
                                    containerHeight = it.size.height
                                },
                            contentScale = ContentScale.Fit
                        )

                        val knobSize = 16.dp
                        val knobSizePx = with(LocalDensity.current) { knobSize.toPx() }

                        Box(
                            modifier = Modifier
                                .offset {
                                    IntOffset(
                                        (hotspotX / 100f * containerWidth - knobSizePx / 2).roundToInt(),
                                        (hotspotY / 100f * containerHeight - knobSizePx / 2).roundToInt()
                                    )
                                }
                                .size(knobSize)
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                                .pointerInput(Unit) {
                                    detectDragGestures { change, dragAmount ->
                                        change.consume()
                                        if (containerWidth > 0) hotspotX = (hotspotX + (dragAmount.x / containerWidth * 100f)).coerceIn(0f, 100f)
                                        if (containerHeight > 0) hotspotY = (hotspotY + (dragAmount.y / containerHeight * 100f)).coerceIn(0f, 100f)
                                    }
                                }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("X: ${hotspotX.roundToInt()}%", style = MaterialTheme.typography.labelMedium)
                    Text("Y: ${hotspotY.roundToInt()}%", style = MaterialTheme.typography.labelMedium)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                LauncherPreferences.PREF_MOUSE_HOTSPOT_X = hotspotX
                LauncherPreferences.PREF_MOUSE_HOTSPOT_Y = hotspotY
                LauncherPreferences.DEFAULT_PREF?.edit {
                    putFloat("mouseHotspotX", hotspotX)
                    putFloat("mouseHotspotY", hotspotY)
                }
                onDismiss()
            }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun JavaSettings() {
    val context = LocalContext.current
    var showJvmArgsDialog by remember { mutableStateOf(false) }

    val totalRam = remember { Tools.getTotalDeviceMemory(context).toFloat() }

    val mVmInstallLauncher = rememberLauncherForActivityResult(
        OpenDocumentWithExtension("xz")
    ) { uri ->
        if (uri != null) Tools.installRuntimeFromUri(context, uri)
    }

    val mDialogScreen = remember {
        MultiRTConfigDialog().apply {
            prepare(context, mVmInstallLauncher)
        }
    }

    if (showJvmArgsDialog) {
        var jvmArgs by remember { mutableStateOf(LauncherPreferences.PREF_CUSTOM_JAVA_ARGS ?: "") }
        AlertDialog(
            onDismissRequest = { showJvmArgsDialog = false },
            title = { Text(stringResource(R.string.mcl_setting_title_javaargs)) },
            text = {
                OutlinedTextField(
                    value = jvmArgs,
                    onValueChange = { jvmArgs = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.mcl_setting_subtitle_javaargs)) }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    LauncherPreferences.PREF_CUSTOM_JAVA_ARGS = jvmArgs
                    LauncherPreferences.DEFAULT_PREF?.edit { putString("javaArgs", jvmArgs) }
                    showJvmArgsDialog = false
                }) {
                    Text(stringResource(R.string.global_save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showJvmArgsDialog = false }) {
                    Text(stringResource(android.R.string.cancel))
                }
            }
        )
    }

    LazyColumn(contentPadding = PaddingValues(vertical = 12.dp)) {
        item {
            PreferenceGroup(title = stringResource(R.string.preference_category_java_tweaks)) {
                PreferenceItem(
                    title = stringResource(R.string.multirt_title),
                    summary = stringResource(R.string.multirt_subtitle),
                    icon = painterResource(R.drawable.ic_px_runtime_mgr),
                    onClick = {
                        mDialogTaskShow(mDialogScreen)
                    }
                )

                PreferenceItem(
                    title = stringResource(R.string.mcl_setting_title_javaargs),
                    summary = LauncherPreferences.PREF_CUSTOM_JAVA_ARGS?.ifEmpty { null } ?: stringResource(R.string.mcl_setting_subtitle_javaargs),
                    icon = painterResource(R.drawable.ic_px_console),
                    onClick = {
                        showJvmArgsDialog = true
                    }
                )

                var ram by remember {
                    mutableFloatStateOf(LauncherPreferences.PREF_RAM_ALLOCATION.toFloat().coerceIn(512f, totalRam))
                }
                PreferenceSlider(
                    title = stringResource(R.string.mcl_memory_allocation),
                    summary = stringResource(R.string.mcl_memory_allocation_subtitle),
                    icon = painterResource(R.drawable.ic_px_ram),
                    value = ram,
                    onValueChange = {
                        ram = it
                        LauncherPreferences.PREF_RAM_ALLOCATION = it.toInt()
                        LauncherPreferences.DEFAULT_PREF?.edit { putInt("allocation", it.toInt()) }
                    },
                    valueRange = 512f..totalRam
                )

                var javaSandbox by remember { mutableStateOf(LauncherPreferences.PREF_JAVA_SANDBOX) }
                PreferenceSwitch(
                    title = stringResource(R.string.mcl_setting_java_sandbox),
                    summary = stringResource(R.string.mcl_setting_java_sandbox_subtitle),
                    icon = painterResource(R.drawable.ic_px_java_run),
                    checked = javaSandbox,
                    onCheckedChange = {
                        javaSandbox = it
                        LauncherPreferences.PREF_JAVA_SANDBOX = it
                        LauncherPreferences.DEFAULT_PREF?.edit { putBoolean("java_sandbox", it) }
                    }
                )
            }
        }
    }
}

private fun mDialogTaskShow(dialog: MultiRTConfigDialog) {
    dialog.show()
}

@Composable
fun MiscSettings() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val notificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.POST_NOTIFICATIONS
    } else {
        null
    }

    var hasNotificationPermission by remember {
        mutableStateOf(
            notificationPermission == null || ContextCompat.checkSelfPermission(context, notificationPermission) == PackageManager.PERMISSION_GRANTED
        )
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
        if (isGranted) {
            Toast.makeText(context, "Notification permission granted", Toast.LENGTH_SHORT).show()
        }
    }

    var showUpdateDialog by remember { mutableStateOf(false) }
    var updateInfoState by remember { mutableStateOf<UpdateUtils.UpdateInfo?>(null) }

    if (showUpdateDialog && updateInfoState != null) {
        val info = updateInfoState!!
        AlertDialog(
            onDismissRequest = { showUpdateDialog = false },
            title = { Text("Update Available", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("A new version (${info.latestVersion}) is available!", style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(8.dp))
                    Text("Changelog:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                    Box(modifier = Modifier
                        .heightIn(max = 120.dp)
                        .verticalScroll(rememberScrollState())
                    ) {
                        Text(info.changelog, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(info.updateUrl))
                    context.startActivity(intent)
                    showUpdateDialog = false
                }) {
                    Text("UPDATE")
                }
            },
            dismissButton = {
                @Suppress("DEPRECATION")
                TextButton(onClick = { showUpdateDialog = false }) {
                    @Suppress("DEPRECATION")
                    Text(stringResource(id = android.R.string.cancel).uppercase())
                }
            }
        )
    }

    LazyColumn(contentPadding = PaddingValues(vertical = 12.dp)) {
        item {
            PreferenceGroup(title = "Launcher Updates") {
                PreferenceItem(
                    title = "Check for Updates",
                    summary = "Manually check if a newer version of Hyper Launcher is available",
                    icon = painterResource(R.drawable.ic_px_zap),
                    onClick = {
                        coroutineScope.launch {
                            Toast.makeText(context, "Checking for updates...", Toast.LENGTH_SHORT).show()
                            val info = UpdateUtils.checkForUpdates()
                            if (info != null) {
                                if (info.hasUpdate) {
                                    updateInfoState = info
                                    showUpdateDialog = true
                                } else {
                                    Toast.makeText(context, "You are on the latest version!", Toast.LENGTH_LONG).show()
                                }
                            } else {
                                Toast.makeText(context, "Failed to check for updates", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    trailingContent = {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    Toast.makeText(context, "Checking for updates...", Toast.LENGTH_SHORT).show()
                                    val info = UpdateUtils.checkForUpdates()
                                    if (info != null) {
                                        if (info.hasUpdate) {
                                            updateInfoState = info
                                            showUpdateDialog = true
                                        } else {
                                            Toast.makeText(context, "You are on the latest version!", Toast.LENGTH_LONG).show()
                                        }
                                    } else {
                                        Toast.makeText(context, "Failed to check for updates", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            shape = CircleShape,
                            contentPadding = PaddingValues(horizontal = 16.dp)
                        ) {
                            @Suppress("DEPRECATION")
                            Text("CHECK", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                )

                var skipUpdateCheck by remember { mutableStateOf(LauncherPreferences.PREF_SKIP_UPDATE_CHECK) }
                PreferenceSwitch(
                    title = "Automatic Update Check",
                    summary = "Check for updates when opening the launcher",
                    icon = painterResource(R.drawable.ic_px_bell),
                    checked = !skipUpdateCheck,
                    onCheckedChange = {
                        val newValue = !it
                        skipUpdateCheck = newValue
                        LauncherPreferences.PREF_SKIP_UPDATE_CHECK = newValue
                        LauncherPreferences.DEFAULT_PREF?.edit()?.putBoolean(LauncherPreferences.PREF_KEY_SKIP_UPDATE_CHECK, newValue)?.apply()
                    }
                )
            }
        }

        item {
            PreferenceGroup(title = stringResource(R.string.preference_category_miscellaneous)) {
                var checkFiles by remember { mutableStateOf(LauncherPreferences.PREF_VERIFY_FILES) }
                PreferenceSwitch(
                    title = stringResource(R.string.preference_verify_game_files_title),
                    summary = stringResource(R.string.preference_verify_game_files_description),
                    icon = painterResource(R.drawable.ic_px_hash),
                    checked = checkFiles,
                    onCheckedChange = {
                        checkFiles = it
                        LauncherPreferences.PREF_VERIFY_FILES = it
                        LauncherPreferences.DEFAULT_PREF?.edit { putBoolean("checkGameFiles", it) }
                    }
                )

                var fastStart by remember { mutableStateOf(LauncherPreferences.PREF_RAPID_START) }
                PreferenceSwitch(
                    title = stringResource(R.string.preference_go_vroom_title),
                    summary = stringResource(R.string.preference_go_vroom_description),
                    icon = painterResource(R.drawable.ic_px_zap),
                    checked = fastStart,
                    onCheckedChange = {
                        fastStart = it
                        LauncherPreferences.PREF_RAPID_START = it
                        LauncherPreferences.DEFAULT_PREF?.edit { putBoolean("fastStartupCheck", it) }
                    }
                )

                var dlSource by remember { mutableStateOf(LauncherPreferences.PREF_DOWNLOAD_SOURCE) }
                PreferenceList(
                    title = stringResource(R.string.preference_download_source_title),
                    entries = stringArrayResource(R.array.download_source_names),
                    entryValues = stringArrayResource(R.array.download_source_values),
                    selectedValue = dlSource,
                    onValueChange = {
                        dlSource = it
                        LauncherPreferences.PREF_DOWNLOAD_SOURCE = it
                        LauncherPreferences.DEFAULT_PREF?.edit { putString("downloadSource", it) }
                    },
                    icon = painterResource(R.drawable.ic_px_file_dl)
                )

                var verifyManifest by remember { mutableStateOf(LauncherPreferences.PREF_VERIFY_MANIFEST) }
                PreferenceSwitch(
                    title = stringResource(R.string.preference_verify_manifest_title),
                    summary = stringResource(R.string.preference_verify_manifest_description),
                    icon = painterResource(R.drawable.ic_px_verify_hash),
                    checked = verifyManifest,
                    onCheckedChange = {
                        verifyManifest = it
                        LauncherPreferences.PREF_VERIFY_MANIFEST = it
                        LauncherPreferences.DEFAULT_PREF?.edit { putBoolean("verifyManifest", it) }
                    }
                )

                var zinkPreferSystem by remember { mutableStateOf(LauncherPreferences.PREF_ZINK_PREFER_SYSTEM_DRIVER) }
                PreferenceSwitch(
                    title = stringResource(R.string.preference_vulkan_driver_system_title),
                    summary = stringResource(R.string.preference_vulkan_driver_system_description),
                    icon = painterResource(R.drawable.ic_px_volcano),
                    checked = zinkPreferSystem,
                    onCheckedChange = {
                        zinkPreferSystem = it
                        LauncherPreferences.PREF_ZINK_PREFER_SYSTEM_DRIVER = it
                        LauncherPreferences.DEFAULT_PREF?.edit { putBoolean("zinkPreferSystemDriver", it) }
                    }
                )

                if (notificationPermission != null && !hasNotificationPermission) {
                    PreferenceItem(
                        title = stringResource(R.string.preference_microphone_access_title),
                        summary = stringResource(R.string.preference_microphone_access_description),
                        icon = painterResource(R.drawable.ic_microphone),
                        onClick = {
                             notificationLauncher.launch(notificationPermission)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ExperimentalSettings() {
    LazyColumn(contentPadding = PaddingValues(vertical = 12.dp)) {
        item {
            PreferenceGroup(title = stringResource(R.string.preference_category_experimental_settings)) {
                var dumpShaders by remember { mutableStateOf(LauncherPreferences.PREF_DUMP_SHADERS) }
                PreferenceSwitch(
                    title = stringResource(R.string.preference_shader_dump_title),
                    summary = stringResource(R.string.preference_shader_dump_description),
                    checked = dumpShaders,
                    onCheckedChange = {
                        dumpShaders = it
                        LauncherPreferences.PREF_DUMP_SHADERS = it
                        LauncherPreferences.DEFAULT_PREF?.edit { putBoolean("dump_shaders", it) }
                    }
                )

                var bigCore by remember { mutableStateOf(LauncherPreferences.PREF_BIG_CORE_AFFINITY) }
                PreferenceSwitch(
                    title = stringResource(R.string.preference_force_big_core_title),
                    summary = stringResource(R.string.preference_force_big_core_desc),
                    checked = bigCore,
                    onCheckedChange = {
                        bigCore = it
                        LauncherPreferences.PREF_BIG_CORE_AFFINITY = it
                        LauncherPreferences.DEFAULT_PREF?.edit { putBoolean("bigCoreAffinity", it) }
                    }
                )

                var freedrenoSysmem by remember { mutableStateOf(LauncherPreferences.PREF_FREEDRENO_SYSMEM) }
                PreferenceSwitch(
                    title = stringResource(R.string.preference_sysmem_title),
                    summary = stringResource(R.string.preference_sysmem_summary),
                    checked = freedrenoSysmem,
                    onCheckedChange = {
                        freedrenoSysmem = it
                        LauncherPreferences.PREF_FREEDRENO_SYSMEM = it
                        LauncherPreferences.DEFAULT_PREF?.edit { putBoolean("freedrenoSysmem", it) }
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun SettingsScreenPreview() {
    PojavTheme(dynamicColor = true) {
        SettingsScreen(onBack = {})
    }
}
