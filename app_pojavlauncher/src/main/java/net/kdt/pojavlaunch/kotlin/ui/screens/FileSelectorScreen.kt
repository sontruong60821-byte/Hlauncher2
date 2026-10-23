package net.kdt.pojavlaunch.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import com.kdt.pickafile.FileListView
import java.io.File

@Composable
fun FileSelectorScreen(
    currentPath: String,
    rootPath: String,
    selectFolderVisible: Boolean,
    showFiles: Boolean,
    showFolders: Boolean,
    onCurrentPathChange: (String) -> Unit,
    onFileListViewReady: (FileListView) -> Unit,
    onSelectFolder: () -> Unit,
    onCreateFolder: (String) -> Unit,
    onFileSelected: (String) -> Unit
) {
    var createDialogOpen by remember { mutableStateOf(false) }
    var createFolderName by remember { mutableStateOf("") }

    Surface(color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = if (currentPath.isBlank()) rootPath else currentPath,
                style = MaterialTheme.typography.labelLarge
            )
            HorizontalDivider()

            AndroidView(
                factory = { ctx: Context ->
                    FileListView(ctx, null, arrayOfNulls<String>(0)).apply {
                        onFileListViewReady(this)
                        setShowFiles(showFiles)
                        setShowFolders(showFolders)
                        lockPathAt(File(rootPath))
                        setDialogTitleListener { title -> onCurrentPathChange(title ?: "") }
                        setFileSelectedListener(object : com.kdt.pickafile.FileSelectedListener() {
                            override fun onFileSelected(file: File?, path: String?) {
                                if (path != null) onFileSelected(path)
                            }
                        })
                    }
                },
                update = { view ->
                    view.setShowFiles(showFiles)
                    view.setShowFolders(showFolders)
                },
                modifier = Modifier.weight(1f).fillMaxWidth()
            )

            Row(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = onSelectFolder,
                    modifier = Modifier.weight(1f),
                    enabled = selectFolderVisible
                ) {
                    Text("Select Folder")
                }
                Spacer(modifier = Modifier.width(12.dp))
                Button(
                    onClick = { createDialogOpen = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Create Folder")
                }
            }
        }
    }

    if (createDialogOpen) {
        AlertDialog(
            onDismissRequest = {
                createDialogOpen = false
                createFolderName = ""
            },
            title = { Text("Create Folder") },
            text = {
                OutlinedTextField(
                    value = createFolderName,
                    onValueChange = { createFolderName = it },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (createFolderName.isNotBlank()) {
                            onCreateFolder(createFolderName.trim())
                        }
                        createFolderName = ""
                        createDialogOpen = false
                    }
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    createDialogOpen = false
                    createFolderName = ""
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}
