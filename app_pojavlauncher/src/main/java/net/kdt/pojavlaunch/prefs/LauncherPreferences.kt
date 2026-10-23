package net.kdt.pojavlaunch.prefs

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Rect
import android.os.Build
import android.util.DisplayMetrics
import android.util.Log
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import net.ashmeet.hyperlauncher.R
import net.kdt.pojavlaunch.Architecture
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.multirt.MultiRTUtils
import net.kdt.pojavlaunch.utils.JREUtils
import java.io.IOException

object LauncherPreferences {
    const val PREF_KEY_CURRENT_INSTANCE = "currentInstance"
    const val PREF_KEY_SKIP_NOTIFICATION_CHECK = "skipNotificationPermissionCheck"
    const val PREF_KEY_THEME_SEED_COLOR = "themeSeedColor"
    const val PREF_KEY_THEME_COLOR_ENABLED = "themeColorEnabled"
    const val PREF_KEY_THEME_TYPE_ENABLED = "themeTypeEnabled"
    const val PREF_KEY_THEME_TYPE_MODE = "themeTypeMode"
    const val PREF_KEY_SKIP_UPDATE_CHECK = "skipUpdateCheck"

    @JvmField
    var DEFAULT_PREF: SharedPreferences? = null

    @JvmField
    var PREF_RENDERER = "ltw"

    @JvmField
    var PREF_IGNORE_NOTCH = false

    @JvmField
    var PREF_BUTTONSIZE = 100f

    @JvmField
    var PREF_MOUSESCALE = 1f

    @JvmField
    var PREF_LONGPRESS_TRIGGER = 300

    @JvmField
    var PREF_DEFAULTCTRL_PATH = Tools.CTRLDEF_FILE

    @JvmField
    var PREF_CUSTOM_JAVA_ARGS: String? = null

    @JvmField
    var PREF_FORCE_ENGLISH = false

    const val PREF_VERSION_REPOS = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"

    @JvmField
    var PREF_DISABLE_GESTURES = false

    @JvmField
    var PREF_DISABLE_SWAP_HAND = false

    @JvmField
    var PREF_MOUSESPEED = 1f

    @JvmField
    var PREF_RAM_ALLOCATION = 0

    @JvmField
    var PREF_DEFAULT_RUNTIME: String? = null

    @JvmField
    var PREF_SUSTAINED_PERFORMANCE = false

    @JvmField
    var PREF_VIRTUAL_MOUSE_START = false

    @JvmField
    var PREF_ENABLE_PHYSICAL_MOUSE = false

    @JvmField
    var PREF_USE_ALTERNATE_SURFACE = true

    @JvmField
    var PREF_JAVA_SANDBOX = true

    @JvmField
    var PREF_SCALE_FACTOR = 1f

    @JvmField
    var PREF_ENABLE_GYRO = false

    @JvmField
    var PREF_GYRO_SENSITIVITY = 1f

    @JvmField
    var PREF_GYRO_SAMPLE_RATE = 16

    @JvmField
    var PREF_GYRO_SMOOTHING = true

    @JvmField
    var PREF_GYRO_INVERT_X = false

    @JvmField
    var PREF_GYRO_INVERT_Y = false

    @JvmField
    var PREF_FORCE_VSYNC = false

    @JvmField
    var PREF_USE_ANGLE = false

    @JvmField
    var PREF_BUTTON_ALL_CAPS = true

    @JvmField
    var PREF_DUMP_SHADERS = false

    @JvmField
    var PREF_DEADZONE_SCALE = 1f

    @JvmField
    var PREF_BIG_CORE_AFFINITY = false

    @JvmField
    var PREF_ZINK_PREFER_SYSTEM_DRIVER = false

    @JvmField
    var PREF_SKIP_UPDATE_CHECK = false

    @JvmField
    var PREF_VERIFY_MANIFEST = true

    @JvmField
    var PREF_DOWNLOAD_SOURCE = "default"

    @JvmField
    var PREF_SKIP_NOTIFICATION_PERMISSION_CHECK = false

    @JvmField
    var PREF_VSYNC_IN_ZINK = true

    @JvmField
    var PREF_RAPID_START = true

    @JvmField
    var PREF_VERIFY_FILES = true

    @JvmField
    var PREF_FREEDRENO_SYSMEM = false

    @JvmField
    var PREF_PREFERRED_GRAPHICS_BACKEND = "default"

    @JvmField
    var PREF_CUSTOM_ENV_VARS: String? = ""

    @JvmField
    var PREF_APP_THEME = "system"

    @JvmField
    var PREF_DRAWER_BUTTON_X = 50f

    @JvmField
    var PREF_DRAWER_BUTTON_Y = 0f

    @JvmField
    var PREF_DRAWER_BUTTON_PRESET = "top_center"

    @JvmField
    var PREF_DRAWER_BUTTON_SIZE = 40

    @JvmField
    var PREF_DRAWER_BUTTON_CORNER_RADIUS = 8

    @JvmField
    var PREF_DRAWER_BUTTON_BG_OPACITY = 0.33f

    @JvmField
    var PREF_DRAWER_BUTTON_ICON_OPACITY = 1f

    @JvmField
    var PREF_DRAWER_BUTTON_MOVABLE = false

    @JvmField
    var PREF_DRAWER_BUTTON_ICON_PATH: String? = null

    @JvmField
    var PREF_MOUSE_ICON_PATH: String? = null

    @JvmField
    var PREF_MOUSE_HOTSPOT_X = 0f

    @JvmField
    var PREF_MOUSE_HOTSPOT_Y = 0f

    @JvmField
    var PREF_THEME_SEED_COLOR = -0x98581

    @JvmField
    var PREF_THEME_COLOR_ENABLED = false

    @JvmField
    var PREF_THEME_TYPE_ENABLED = true

    @JvmField
    var PREF_THEME_TYPE_MODE = "monochrome"

    @JvmField
    var PREF_BACKGROUND_PATH: String? = null

    @JvmField
    var PREF_BACKGROUND_TRANSPARENCY = 0f

    @JvmField
    var PREF_BACKGROUND_BLUR = 0f

    @JvmField
    var PREF_BACKGROUND_BLUR_ENABLED = false

    @JvmField
    var PREF_TRANSITION_ANIMATION = "default"

    @JvmField
    var PREF_TRANSITION_DURATION = 300

    @JvmField
    var PREF_TRANSITION_INTENSITY = 1f

    val prefAppThemeState: MutableState<String> = mutableStateOf("system")
    val PREF_THEME_SEED_COLOR_STATE: MutableIntState = mutableIntStateOf(-0x98581)
    val PREF_THEME_COLOR_ENABLED_STATE: MutableState<Boolean> = mutableStateOf(false)
    val PREF_THEME_TYPE_ENABLED_STATE: MutableState<Boolean> = mutableStateOf(true)
    val PREF_THEME_TYPE_MODE_STATE: MutableState<String> = mutableStateOf("monochrome")

    val PREF_MOUSE_ICON_PATH_STATE: MutableState<String?> = mutableStateOf(null)

    val PREF_BACKGROUND_PATH_STATE: MutableState<String?> = mutableStateOf(null)
    val PREF_BACKGROUND_TRANSPARENCY_STATE: MutableState<Float> = mutableStateOf(0f)
    val PREF_BACKGROUND_BLUR_STATE: MutableState<Float> = mutableStateOf(0f)
    val PREF_BACKGROUND_BLUR_ENABLED_STATE: MutableState<Boolean> = mutableStateOf(false)

    val PREF_TRANSITION_ANIMATION_STATE: MutableState<String> = mutableStateOf("default")
    val PREF_TRANSITION_DURATION_STATE: MutableState<Int> = mutableStateOf(300)
    val PREF_TRANSITION_INTENSITY_STATE: MutableState<Float> = mutableStateOf(1f)

    val PREF_SHOW_CRYNOIX_LOADING: MutableState<Boolean> = mutableStateOf(false)

    @JvmStatic
    fun applyTheme() {

    }

    @JvmStatic
    fun loadPreferences(ctx: Context) {
        Tools.initStorageConstants(ctx)
        val prefs = DEFAULT_PREF ?: return
        val isDevicePowerful = isDevicePowerful(ctx)

        PREF_RENDERER = prefs.getString("renderer", "ltw") ?: "ltw"
        PREF_BUTTONSIZE = prefs.getInt("buttonscale", 100).toFloat()
        PREF_MOUSESCALE = prefs.getInt("mousescale", 100) / 100f
        PREF_MOUSESPEED = prefs.getInt("mousespeed", 100) / 100f
        PREF_IGNORE_NOTCH = prefs.getBoolean("ignoreNotch", false)
        PREF_LONGPRESS_TRIGGER = prefs.getInt("timeLongPressTrigger", 300)
        PREF_DEFAULTCTRL_PATH = prefs.getString("defaultCtrl", Tools.CTRLDEF_FILE) ?: Tools.CTRLDEF_FILE
        PREF_FORCE_ENGLISH = prefs.getBoolean("force_english", false)
        PREF_DISABLE_GESTURES = prefs.getBoolean("disableGestures", false)
        PREF_DISABLE_SWAP_HAND = prefs.getBoolean("disableDoubleTap", false)
        PREF_RAM_ALLOCATION = prefs.getInt("allocation", findBestRAMAllocation(ctx))
        PREF_CUSTOM_JAVA_ARGS = prefs.getString("javaArgs", "")
        PREF_SUSTAINED_PERFORMANCE = prefs.getBoolean("sustainedPerformance", isDevicePowerful)
        PREF_VIRTUAL_MOUSE_START = prefs.getBoolean("mouse_start", false)
        PREF_ENABLE_PHYSICAL_MOUSE = prefs.getBoolean("enable_physical_mouse", false)
        PREF_USE_ALTERNATE_SURFACE = prefs.getBoolean("alternate_surface", isDevicePowerful)
        PREF_JAVA_SANDBOX = prefs.getBoolean("java_sandbox", true)
        PREF_SCALE_FACTOR = prefs.getInt("resolutionRatio", findBestResolution(ctx, isDevicePowerful)) / 100f
        PREF_ENABLE_GYRO = prefs.getBoolean("enableGyro", false)
        PREF_GYRO_SENSITIVITY = prefs.getInt("gyroSensitivity", 100) / 100f
        PREF_GYRO_SAMPLE_RATE = prefs.getInt("gyroSampleRate", 16)
        PREF_GYRO_SMOOTHING = prefs.getBoolean("gyroSmoothing", true)
        PREF_GYRO_INVERT_X = prefs.getBoolean("gyroInvertX", false)
        PREF_GYRO_INVERT_Y = prefs.getBoolean("gyroInvertY", false)
        PREF_FORCE_VSYNC = prefs.getBoolean("force_vsync", isDevicePowerful)
        PREF_USE_ANGLE = prefs.getBoolean("use_angle", false)
        PREF_BUTTON_ALL_CAPS = prefs.getBoolean("buttonAllCaps", true)
        PREF_DUMP_SHADERS = prefs.getBoolean("dump_shaders", false)
        PREF_DEADZONE_SCALE = prefs.getInt("gamepad_deadzone_scale", 100) / 100f
        PREF_BIG_CORE_AFFINITY = prefs.getBoolean("bigCoreAffinity", false)
        PREF_ZINK_PREFER_SYSTEM_DRIVER = prefs.getBoolean("zinkPreferSystemDriver", false)
        PREF_DOWNLOAD_SOURCE = prefs.getString("downloadSource", "default") ?: "default"
        PREF_VERIFY_MANIFEST = prefs.getBoolean("verifyManifest", true)
        PREF_SKIP_NOTIFICATION_PERMISSION_CHECK = prefs.getBoolean(PREF_KEY_SKIP_NOTIFICATION_CHECK, false)
        PREF_VSYNC_IN_ZINK = prefs.getBoolean("vsync_in_zink", true)
        PREF_VERIFY_FILES = prefs.getBoolean("checkGameFiles", true)
        PREF_RAPID_START = prefs.getBoolean("fastStartupCheck", true)
        PREF_FREEDRENO_SYSMEM = prefs.getBoolean("freedrenoSysmem", false)
        PREF_PREFERRED_GRAPHICS_BACKEND = prefs.getString("preferredGraphicsBackend", "default") ?: "default"
        PREF_APP_THEME = prefs.getString("appTheme", "system") ?: "system"
        prefAppThemeState.value = PREF_APP_THEME

        PREF_DRAWER_BUTTON_X = prefs.getInt("drawerButtonX", 50).toFloat()
        PREF_DRAWER_BUTTON_Y = prefs.getInt("drawerButtonY", 0).toFloat()
        PREF_DRAWER_BUTTON_PRESET = prefs.getString("drawerButtonPreset", "top_center") ?: "top_center"
        PREF_DRAWER_BUTTON_SIZE = prefs.getInt("drawerButtonSize", 40)
        PREF_DRAWER_BUTTON_CORNER_RADIUS = prefs.getInt("drawerButtonCornerRadius", 8)
        PREF_DRAWER_BUTTON_BG_OPACITY = prefs.getInt("drawerButtonBgOpacity", 33) / 100f
        PREF_DRAWER_BUTTON_ICON_OPACITY = prefs.getInt("drawerButtonIconOpacity", 100) / 100f
        PREF_DRAWER_BUTTON_MOVABLE = prefs.getBoolean("drawerButtonMovable", false)
        PREF_DRAWER_BUTTON_ICON_PATH = prefs.getString("drawerButtonIconPath", null)

        PREF_MOUSE_ICON_PATH = prefs.getString("mouseIconPath", null)
        PREF_MOUSE_ICON_PATH_STATE.value = PREF_MOUSE_ICON_PATH
        PREF_MOUSE_HOTSPOT_X = prefs.getFloat("mouseHotspotX", 0f)
        PREF_MOUSE_HOTSPOT_Y = prefs.getFloat("mouseHotspotY", 0f)

        PREF_THEME_SEED_COLOR = prefs.getInt(PREF_KEY_THEME_SEED_COLOR, -0x98581)
        PREF_THEME_SEED_COLOR_STATE.intValue = PREF_THEME_SEED_COLOR
        PREF_THEME_COLOR_ENABLED = prefs.getBoolean(PREF_KEY_THEME_COLOR_ENABLED, false)
        PREF_THEME_COLOR_ENABLED_STATE.value = PREF_THEME_COLOR_ENABLED

        PREF_THEME_TYPE_ENABLED = prefs.getBoolean(PREF_KEY_THEME_TYPE_ENABLED, true)
        PREF_THEME_TYPE_ENABLED_STATE.value = PREF_THEME_TYPE_ENABLED
        PREF_THEME_TYPE_MODE = prefs.getString(PREF_KEY_THEME_TYPE_MODE, "monochrome") ?: "monochrome"
        PREF_THEME_TYPE_MODE_STATE.value = PREF_THEME_TYPE_MODE

        PREF_BACKGROUND_PATH = prefs.getString("backgroundPath", null)
        PREF_BACKGROUND_PATH_STATE.value = PREF_BACKGROUND_PATH
        PREF_BACKGROUND_TRANSPARENCY = prefs.getFloat("backgroundTransparency", 0f)
        PREF_BACKGROUND_TRANSPARENCY_STATE.value = PREF_BACKGROUND_TRANSPARENCY
        PREF_BACKGROUND_BLUR = prefs.getFloat("backgroundBlur", 0f)
        PREF_BACKGROUND_BLUR_STATE.value = PREF_BACKGROUND_BLUR
        PREF_BACKGROUND_BLUR_ENABLED = prefs.getBoolean("backgroundBlurEnabled", false)
        PREF_BACKGROUND_BLUR_ENABLED_STATE.value = PREF_BACKGROUND_BLUR_ENABLED

        PREF_TRANSITION_ANIMATION = prefs.getString("transitionAnimation", "default") ?: "default"
        PREF_TRANSITION_ANIMATION_STATE.value = PREF_TRANSITION_ANIMATION
        PREF_TRANSITION_DURATION = prefs.getInt("transitionDuration", 300)
        PREF_TRANSITION_DURATION_STATE.value = PREF_TRANSITION_DURATION
        PREF_TRANSITION_INTENSITY = prefs.getFloat("transitionIntensity", 1f)
        PREF_TRANSITION_INTENSITY_STATE.value = PREF_TRANSITION_INTENSITY

        PREF_SKIP_UPDATE_CHECK = prefs.getBoolean(PREF_KEY_SKIP_UPDATE_CHECK, false)

        PREF_CUSTOM_ENV_VARS = prefs.getString("customEnvVars", "")

        val argLwjglLibname = "-Dorg.lwjgl.opengl.libname="
        val customArgs = PREF_CUSTOM_JAVA_ARGS
        if (customArgs != null) {
            for (arg in JREUtils.parseJavaArguments(customArgs)) {
                if (arg.startsWith(argLwjglLibname)) {
                    prefs.edit().putString("javaArgs", customArgs.replace(arg, "")).apply()
                }
            }
        }

        if (prefs.contains("defaultRuntime")) {
            PREF_DEFAULT_RUNTIME = prefs.getString("defaultRuntime", "")
        } else {
            val runtimes = MultiRTUtils.getRuntimes()
            if (runtimes.isEmpty()) {
                PREF_DEFAULT_RUNTIME = ""
            } else {
                PREF_DEFAULT_RUNTIME = runtimes[0].name
                prefs.edit().putString("defaultRuntime", PREF_DEFAULT_RUNTIME).apply()
            }
        }
    }

    private fun findBestRAMAllocation(ctx: Context): Int {
        val deviceRam = Tools.getTotalDeviceMemory(ctx)
        if (deviceRam < 1024) return 296
        if (deviceRam < 1536) return 448
        if (deviceRam < 2048) return 656
        if (Architecture.is32BitsDevice()) return 696
        if (deviceRam < 3064) return 936
        if (deviceRam < 4096) return 1144
        if (deviceRam < 6144) return 1536
        return 2048
    }

    private fun findBestResolution(context: Context, isDevicePowerful: Boolean): Int {
        val metrics = context.resources.displayMetrics
        val minSide = Math.min(metrics.widthPixels, metrics.heightPixels)
        val targetSide = if (isDevicePowerful) 1080 else 720
        if (minSide <= targetSide) return 100
        val ratio = (100f * targetSide / minSide)
        val increment = context.resources.getInteger(R.integer.resolution_seekbar_increment)
        return (Math.ceil(ratio.toDouble() / increment) * increment).toInt()
    }

    private fun isDevicePowerful(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return false
        if (Tools.getTotalDeviceMemory(context) <= 4096) return false
        val metrics = context.resources.displayMetrics
        if (Math.min(metrics.widthPixels, metrics.heightPixels) < 1080) return false
        if (Runtime.getRuntime().availableProcessors() <= 4) return false
        if (hasAllCoreSameFreq()) return false
        return true
    }

    private fun hasAllCoreSameFreq(): Boolean {
        val coreCount = Runtime.getRuntime().availableProcessors()
        try {
            val freq0 = Tools.read("/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq")
            val freqX = Tools.read("/sys/devices/system/cpu/cpu" + (coreCount - 1) + "/cpufreq/cpuinfo_max_freq")
            if (freq0 == freqX) return true
        } catch (e: IOException) {
            Log.e("LauncherPreferences", "Failed to read CPU frequencies", e)
        }
        return false
    }

    @JvmStatic
    fun hasNotch(activity: Activity): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return false
        return try {
            val cutout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                activity.windowManager.currentWindowMetrics.windowInsets.displayCutout?.boundingRects?.get(0)
            } else {
                activity.window.decorView.rootWindowInsets?.displayCutout?.boundingRects?.get(0)
            }
            cutout != null && (cutout.width() != 0 || cutout.height() != 0)
        } catch (e: Exception) {
            Log.i("NOTCH DETECTION", "No notch detected, or the device if in split screen mode")
            false
        }
    }
}
