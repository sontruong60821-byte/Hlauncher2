package net.kdt.pojavlaunch.fragments

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import com.kdt.mcgui.ProgressLayout
import net.ashmeet.hyperlauncher.R
import net.kdt.pojavlaunch.PojavApplication
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.JMinecraftVersionList
import net.kdt.pojavlaunch.extra.ExtraCore
import net.kdt.pojavlaunch.extra.ExtraConstants
import net.kdt.pojavlaunch.modloaders.modpacks.ModItemAdapter
import net.kdt.pojavlaunch.modloaders.modpacks.api.CommonApi
import net.kdt.pojavlaunch.modloaders.modpacks.api.ModpackApi
import net.kdt.pojavlaunch.modloaders.modpacks.models.ModDetail
import net.kdt.pojavlaunch.modloaders.modpacks.models.ModItem
import net.kdt.pojavlaunch.modloaders.modpacks.models.SearchFilters
import net.kdt.pojavlaunch.profiles.VersionSelectorDialog
import net.kdt.pojavlaunch.progresskeeper.ProgressKeeper
import net.kdt.pojavlaunch.progresskeeper.TaskCountListener
import net.kdt.pojavlaunch.ui.screens.ModSearchScreen
import net.kdt.pojavlaunch.ui.theme.PojavTheme
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class SearchModFragment : Fragment(), ModItemAdapter.SearchResultCallback {

    private lateinit var mModpackApi: ModpackApi
    private lateinit var mModItemAdapter: ModItemAdapter
    private val mSearchFilters = SearchFilters().apply { isModpack = true }

    private var mSearchQuery by mutableStateOf("")
    private var mIsLoading by mutableStateOf(false)
    private var mStatusVisible by mutableStateOf(false)
    private var mStatusText by mutableStateOf("")
    private var mStatusColor by mutableStateOf(Color.Unspecified)
    private val mItems = mutableStateListOf<ModItem>()
    private var mExpandedItemId by mutableStateOf<String?>(null)
    private var mExpandedDetail by mutableStateOf<ModDetail?>(null)
    private var mDetailLoading by mutableStateOf(false)
    private var mSelectedVersionIndex by mutableIntStateOf(0)
    private var mLastPage by mutableStateOf(false)
    private var mTasksRunning by mutableStateOf(false)

    private val mImportLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                val context = requireContext()
                val contentResolver = context.contentResolver
                PojavApplication.sExecutorService.execute {
                    performLocalInstall(it, context, contentResolver)
                }
            }
        }

    private val mTaskCountListener = TaskCountListener { taskCount ->
        mTasksRunning = taskCount > 0
        false
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mModpackApi = CommonApi(context.getString(R.string.curseforge_api_key))
        mModItemAdapter = ModItemAdapter(resources, mModpackApi, this)
        ProgressKeeper.addTaskCountListener(mModItemAdapter)
        ProgressKeeper.addTaskCountListener(mTaskCountListener)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                PojavTheme {
                    ModSearchScreen(
                        searchQuery = mSearchQuery,
                        isLoading = mIsLoading,
                        statusVisible = mStatusVisible,
                        statusText = mStatusText,
                        statusColor = mStatusColor,
                        items = mItems,
                        expandedItemId = mExpandedItemId,
                        expandedDetail = mExpandedDetail,
                        detailLoading = mDetailLoading,
                        selectedVersionIndex = mSelectedVersionIndex,
                        lastPage = mLastPage,
                        tasksRunning = mTasksRunning,
                        onSearchQueryChange = { mSearchQuery = it },
                        onSearchSubmit = { searchMods(mSearchQuery) },
                        onFilterClick = { displayFilterDialog() },
                        onImportClick = { mImportLauncher.launch("*/*") },
                        onItemClick = { item -> toggleExpand(item) },
                        onLoadMore = { mModItemAdapter.performNextPageQuery() },
                        onVersionSelected = { mSelectedVersionIndex = it },
                        onInstallClick = { item -> installMod(item) }
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val jmcList = ExtraCore.getValue(ExtraConstants.RELEASE_TABLE) as? JMinecraftVersionList
        mSearchFilters.mcVersion = jmcList?.latest?.get("release")
        searchMods(null)
    }

    private fun searchMods(name: String?) {
        mIsLoading = true
        mStatusVisible = false
        mSearchFilters.name = if (name.isNullOrBlank()) "sodium" else name
        mItems.clear()
        mModItemAdapter.performSearchQuery(mSearchFilters)
    }

    private fun toggleExpand(item: ModItem) {
        if (mExpandedItemId == item.id) {
            mExpandedItemId = null
            mExpandedDetail = null
        } else {
            mExpandedItemId = item.id
            mDetailLoading = true
            mModItemAdapter.getModDetail(item) { detail ->
                mExpandedDetail = detail
                mDetailLoading = false
                mSelectedVersionIndex = 0
            }
        }
    }

    private fun installMod(item: ModItem) {
        mModItemAdapter.getModDetail(item) { detail ->
            if (detail != null) {
                mModpackApi.handleModpackInstallation(requireContext(), detail, mSelectedVersionIndex)
            } else {
                Tools.showErrorRemote("Error", IOException("Failed to load mod details"))
            }
        }
    }

    override fun onSearchFinished() {
        mIsLoading = false
        mStatusVisible = false
        mItems.clear()
        mItems.addAll(mModItemAdapter.currentSearchResults.toList())
        mLastPage = mModItemAdapter.isLastPage
    }

    override fun onSearchError(error: Int) {
        mIsLoading = false
        mStatusVisible = true
        when (error) {
            ModItemAdapter.SearchResultCallback.ERROR_INTERNAL -> {
                mStatusColor = Color.Red
                mStatusText = getString(R.string.search_modpack_error)
            }
            ModItemAdapter.SearchResultCallback.ERROR_NO_RESULTS -> {
                mStatusColor = Color.Unspecified
                mStatusText = getString(R.string.search_modpack_no_result)
            }
        }
    }

    private fun displayFilterDialog() {
        VersionSelectorDialog.open(requireContext(), true) { id, _ ->
            mSearchFilters.mcVersion = id
            searchMods(mSearchQuery)
        }
    }

    private fun performLocalInstall(uri: Uri, context: Context, contentResolver: android.content.ContentResolver) {
        val fileName = Tools.getFileName(context, uri) ?: return
        val outFile = File(Tools.DIR_CACHE, "$fileName.cf")
        ProgressLayout.setProgress(ProgressLayout.INSTALL_MODPACK, 0, R.string.multirt_progress_caching)
        try {
            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(outFile).use { output ->
                    input.copyTo(output)
                }
            }
        } catch (e: IOException) {
            Tools.showErrorRemote("Error", e)
            ProgressLayout.clearProgress(ProgressLayout.INSTALL_MODPACK)
            return
        }
        try {
            mModpackApi.installLocalModpack(fileName, outFile, null)
        } catch (e: IOException) {
            Tools.showErrorRemote("Error", e)
        } finally {
            outFile.delete()
            ProgressLayout.clearProgress(ProgressLayout.INSTALL_MODPACK)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        ProgressKeeper.removeTaskCountListener(mModItemAdapter)
        ProgressKeeper.removeTaskCountListener(mTaskCountListener)
    }

    companion object {
        const val TAG = "SearchModFragment"
    }
}
