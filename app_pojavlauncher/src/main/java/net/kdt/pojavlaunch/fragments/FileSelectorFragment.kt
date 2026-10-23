package net.kdt.pojavlaunch.fragments

import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.*
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.extra.ExtraConstants
import net.kdt.pojavlaunch.extra.ExtraCore
import net.kdt.pojavlaunch.ui.screens.FileSelectorScreen
import net.kdt.pojavlaunch.ui.theme.PojavTheme
import java.io.File

class FileSelectorFragment : Fragment() {

    private var mSelectFolder = true
    private var mShowFiles = true
    private var mShowFolders = true
    private var mRootPath = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
        Tools.DIR_GAME_NEW
    else
        Environment.getExternalStorageDirectory().absolutePath

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        parseBundle()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                var currentPath by remember { mutableStateOf("") }
                var fileListView by remember { mutableStateOf<com.kdt.pickafile.FileListView?>(null) }

                PojavTheme {
                    FileSelectorScreen(
                        currentPath = currentPath,
                        rootPath = mRootPath,
                        selectFolderVisible = mSelectFolder,
                        showFiles = mShowFiles,
                        showFolders = mShowFolders,
                        onCurrentPathChange = { currentPath = removeLockPath(it) },
                        onFileListViewReady = { fileListView = it },
                        onSelectFolder = {
                            fileListView?.let {
                                ExtraCore.setValue(ExtraConstants.FILE_SELECTOR, removeLockPath(it.fullPath.absolutePath))
                                Tools.removeCurrentFragment(requireActivity())
                            }
                        },
                        onCreateFolder = { folderName ->
                            fileListView?.let {
                                val folder = File(it.fullPath, folderName)
                                if (folder.mkdir()) {
                                    it.listFileAt(folder)
                                } else {
                                    it.refreshPath()
                                }
                            }
                        },
                        onFileSelected = { path ->
                            ExtraCore.setValue(ExtraConstants.FILE_SELECTOR, removeLockPath(path))
                            Tools.removeCurrentFragment(requireActivity())
                        }
                    )
                }
            }
        }
    }

    private fun removeLockPath(path: String): String {
        return path.replace(mRootPath, ".")
    }

    private fun parseBundle() {
        arguments?.let { bundle ->
            mSelectFolder = bundle.getBoolean(BUNDLE_SELECT_FOLDER, mSelectFolder)
            mShowFiles = bundle.getBoolean(BUNDLE_SHOW_FILE, mShowFiles)
            mShowFolders = bundle.getBoolean(BUNDLE_SHOW_FOLDER, mShowFolders)
            mRootPath = bundle.getString(BUNDLE_ROOT_PATH, mRootPath) ?: mRootPath
        }
    }

    companion object {
        const val TAG = "FileSelectorFragment"
        const val BUNDLE_SELECT_FOLDER = "select_folder"
        const val BUNDLE_SELECT_FILE = "select_file"
        const val BUNDLE_SHOW_FILE = "show_file"
        const val BUNDLE_SHOW_FOLDER = "show_folder"
        const val BUNDLE_ROOT_PATH = "root_path"
    }
}
