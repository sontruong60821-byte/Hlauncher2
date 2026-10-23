package net.kdt.pojavlaunch.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.kdt.pojavlaunch.modloaders.FabricVersion

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FabricInstallScreen(
    title: String,
    gameVersions: List<FabricVersion?>,
    loaderVersions: List<FabricVersion?>,
    selectedGameVersion: String?,
    selectedLoaderVersion: String?,
    onlyStable: Boolean,
    isLoading: Boolean,
    showRetry: Boolean,
    canInstall: Boolean,
    gameVersionLabel: String,
    loaderVersionLabel: String,
    onlyStableLabel: String,
    installLabel: String,
    retryLabel: String,
    retryMessage: String,
    errorColor: Color = MaterialTheme.colorScheme.error,
    onGameVersionSelected: (String?) -> Unit,
    onLoaderVersionSelected: (String?) -> Unit,
    onOnlyStableChanged: (Boolean) -> Unit,
    onRetry: () -> Unit,
    onInstall: () -> Unit
) {
    val scrollState = rememberScrollState()

    Surface(color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            HorizontalDivider()

            if (isLoading) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(28.dp))
                }
            }

            Text(
                text = gameVersionLabel,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            FabricVersionDropdown(
                options = gameVersions,
                selectedValue = selectedGameVersion,
                enabled = !isLoading,
                onValueSelected = {
                    onGameVersionSelected(it)
                }
            )

            Text(
                text = loaderVersionLabel,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            FabricVersionDropdown(
                options = loaderVersions,
                selectedValue = selectedLoaderVersion,
                enabled = !isLoading && selectedGameVersion != null,
                onValueSelected = onLoaderVersionSelected
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !isLoading) { onOnlyStableChanged(!onlyStable) }
                    .padding(vertical = 4.dp)
            ) {
                Checkbox(
                    checked = onlyStable,
                    onCheckedChange = onOnlyStableChanged,
                    enabled = !isLoading
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(onlyStableLabel, fontSize = 14.sp)
            }

            Button(
                onClick = onInstall,
                enabled = canInstall && !isLoading,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(installLabel, fontWeight = FontWeight.Bold)
            }

            AnimatedVisibility(
                visible = showRetry,
                enter = expandVertically() + fadeIn(tween(180)),
                exit = shrinkVertically() + fadeOut(tween(120))
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = retryMessage,
                            color = errorColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        TextButton(onClick = onRetry) {
                            Text(retryLabel)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FabricVersionDropdown(
    options: List<FabricVersion?>,
    selectedValue: String?,
    enabled: Boolean,
    onValueSelected: (String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val filteredOptions = remember(options) { options.filterNotNull() }
    val currentText = selectedValue.orEmpty()

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = currentText,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled) { expanded = true }
                .onFocusChanged {
                    if (it.isFocused && enabled) expanded = true
                },
            trailingIcon = {
                IconButton(onClick = { expanded = !expanded }, enabled = enabled) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            },
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
            ),
            placeholder = { Text("Select version") }
        )

        DropdownMenu(
            expanded = expanded && enabled && filteredOptions.isNotEmpty(),
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .fillMaxWidth(0.98f)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            filteredOptions.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option.version ?: "",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    onClick = {
                        onValueSelected(option.version)
                        expanded = false
                    }
                )
            }
        }
    }
}
