package net.kdt.pojavlaunch.ui.screens

import android.view.View
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import net.ashmeet.hyperlauncher.R
import net.kdt.pojavlaunch.MinecraftGLSurface
import net.kdt.pojavlaunch.customcontrols.handleview.DrawerPullButton
import net.kdt.pojavlaunch.customcontrols.keyboard.TouchCharInput
import net.kdt.pojavlaunch.customcontrols.mouse.HotbarView
import net.kdt.pojavlaunch.prefs.LauncherPreferences
import net.kdt.pojavlaunch.ui.theme.PojavTheme

@Composable
fun BaseMainScreen(
    drawerState: DrawerState = rememberDrawerState(DrawerValue.Closed),
    onGlSurfaceBound: (MinecraftGLSurface) -> Unit = {},
    onTouchpadBound: (View) -> Unit = {},
    onCharInputBound: (TouchCharInput) -> Unit = {},
    onPullButtonBound: (DrawerPullButton) -> Unit = {},
    onHotbarBound: (HotbarView) -> Unit = {},
    drawerContent: @Composable (isExpanded: Boolean) -> Unit = {},
    loadingVisible: Boolean = true,
    onLoadingClick: () -> Unit = {},
    onDismissMenu: () -> Unit = {}
) {
    val isPreview = LocalInspectionMode.current
    val ignoreNotch = if (isPreview) true else LauncherPreferences.PREF_IGNORE_NOTCH

    val primaryContainerColor = MaterialTheme.colorScheme.primaryContainer
    val scrimColor = MaterialTheme.colorScheme.scrim
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant

    var isRailExpanded by remember { mutableStateOf(false) }
    val railWidth by animateDpAsState(
        targetValue = if (isRailExpanded) 200.dp else 80.dp,
        label = "railWidth"
    )

    val layoutModifier = if (ignoreNotch) {
        Modifier.fillMaxSize()
    } else {
        Modifier.fillMaxSize()
            .windowInsetsPadding(WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal))
    }

    val currentOnGlSurfaceBound by rememberUpdatedState(onGlSurfaceBound)
    val currentOnTouchpadBound by rememberUpdatedState(onTouchpadBound)
    val currentOnCharInputBound by rememberUpdatedState(onCharInputBound)
    val currentOnPullButtonBound by rememberUpdatedState(onPullButtonBound)
    val currentOnHotbarBound by rememberUpdatedState(onHotbarBound)

    val androidViewFactory = remember {
        { ctx: android.content.Context ->
            val contentFrame = android.widget.FrameLayout(ctx).apply { id = R.id.content_frame }

            val glSurface = MinecraftGLSurface(ctx).apply {
                id = R.id.main_game_render_view
                layoutParams = android.widget.FrameLayout.LayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT
                )
            }

            val touchpad = View(ctx).apply {
                id = R.id.main_touchpad
                layoutParams = android.widget.FrameLayout.LayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT
                )
                visibility = View.GONE
                translationZ = ctx.resources.displayMetrics.density * 1f
            }

            val charInput = TouchCharInput(ctx).apply {
                id = R.id.mainTouchCharInput
                layoutParams = android.widget.FrameLayout.LayoutParams(1, 1)
            }

            val pullButton = DrawerPullButton(ctx).apply {
                id = R.id.drawer_button
                val size = (2 * ctx.resources.displayMetrics.density).toInt()
                layoutParams = android.widget.FrameLayout.LayoutParams(size, size)
                visibility = View.VISIBLE
                elevation = ctx.resources.displayMetrics.density * 10f
            }

            val hotbarView = HotbarView(ctx).apply {
                id = R.id.hotbar_view
                layoutParams = android.widget.FrameLayout.LayoutParams(0, 0)
            }

            contentFrame.addView(glSurface)
            contentFrame.addView(touchpad)
            contentFrame.addView(charInput)
            contentFrame.addView(pullButton)
            contentFrame.addView(hotbarView)

            glSurface.start(false, touchpad)
            currentOnGlSurfaceBound(glSurface)
            currentOnTouchpadBound(touchpad)
            currentOnCharInputBound(charInput)
            currentOnPullButtonBound(pullButton)
            currentOnHotbarBound(hotbarView)

            contentFrame
        }
    }

    Box(modifier = layoutModifier) {

        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        MaterialTheme.colorScheme.background
                            .compositeOver(Color.Black.copy(alpha = 0.22f))
                            .compositeOver(MaterialTheme.colorScheme.primary.copy(alpha = 0.07f))
                    )
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(primaryContainerColor.copy(alpha = 0.04f))
            )
        }

        Box(modifier = Modifier.fillMaxSize()) {
            if (!isPreview) {
                key("game_surface_view") {
                    AndroidView(
                        factory = androidViewFactory,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.DarkGray.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Game View Placeholder")
                }
            }
        }

        if (drawerState.isOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(scrimColor.copy(alpha = 0.32f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            isRailExpanded = false
                            onDismissMenu()
                        }
                    )
            )
        }

        AnimatedVisibility(
            visible = drawerState.isOpen,
            enter = slideInHorizontally(initialOffsetX = { -it }) + fadeIn(),
            exit = slideOutHorizontally(targetOffsetX = { -it }) + fadeOut(),
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            NavigationRail(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(railWidth),
                containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.94f),
                header = {
                    IconButton(onClick = { isRailExpanded = !isRailExpanded }) {
                        Icon(
                            imageVector = if (isRailExpanded)
                                Icons.AutoMirrored.Filled.KeyboardArrowLeft
                            else
                                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Expand menu",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    drawerContent(isRailExpanded)
                }
            }
        }

        AnimatedVisibility(
            visible = loadingVisible,
            enter = fadeIn(),
            exit = fadeOut(animationSpec = tween(durationMillis = 800))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(scrimColor.copy(alpha = 0.5f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onLoadingClick
                    ),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.background.copy(alpha = 0.90f),
                    tonalElevation = 8.dp,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            color = primaryColor,
                            trackColor = primaryColor.copy(alpha = 0.2f),
                            strokeWidth = 4.dp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Launching game...",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Please wait",
                            style = MaterialTheme.typography.bodySmall,
                            color = onSurfaceVariantColor,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, device = "spec:width=1280dp,height=800dp,orientation=landscape")
@Composable
fun BaseMainScreenPreview() {
    PojavTheme(dynamicColor = false) {
        BaseMainScreen(
            loadingVisible = true
        )
    }
}

@Preview(showBackground = true, device = "spec:width=1280dp,height=800dp,orientation=landscape")
@Composable
fun BaseMainScreenRailPreview() {
    PojavTheme(dynamicColor = false) {
        BaseMainScreen(
            drawerState = rememberDrawerState(initialValue = DrawerValue.Open),
            loadingVisible = false,
            drawerContent = { expanded ->
                NavigationRailItem(
                    selected = false,
                    onClick = {},
                    icon = { Icon(Icons.Default.Settings, null) },
                    label = { Text("Settings") },
                    alwaysShowLabel = expanded
                )
            }
        )
    }
}
