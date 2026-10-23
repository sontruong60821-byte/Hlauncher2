package net.kdt.pojavlaunch.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.ashmeet.hyperlauncher.R
import net.kdt.pojavlaunch.Logger
import net.kdt.pojavlaunch.Tools

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoggerScreen(
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isLogOutputEnabled by remember { mutableStateOf(true) }
    var isAutoScrollEnabled by remember { mutableStateOf(true) }
    val logLines = remember { mutableStateListOf<String>() }
    val listState = rememberLazyListState()

    DisposableEffect(isLogOutputEnabled) {
        if (isLogOutputEnabled) {
            val listener = Logger.eventLogListener { text ->
                Tools.runOnUiThread {
                    logLines.add(text)
                }
            }
            Logger.setLogListener(listener)
            onDispose {
                Logger.setLogListener(null)
            }
        } else {
            logLines.clear()
            Logger.setLogListener(null)
            onDispose {}
        }
    }

    LaunchedEffect(logLines.size) {
        if (isAutoScrollEnabled && logLines.isNotEmpty()) {
            listState.animateScrollToItem(logLines.size - 1)
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color.Transparent
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.log_view_label_log_output),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                actions = {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black.copy(alpha = 0.6f))
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp)
                ) {
                    items(logLines) { line ->
                        Text(
                            text = line,
                            color = Color(0xFFBEBEBE),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            lineHeight = 12.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                if (logLines.isEmpty() && isLogOutputEnabled) {
                    Text(
                        text = "Waiting for output...",
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodySmall
                    )
                } else if (!isLogOutputEnabled) {
                    Text(
                        text = "Log output disabled",
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
