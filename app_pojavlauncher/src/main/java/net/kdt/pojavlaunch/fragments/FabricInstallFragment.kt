package net.kdt.pojavlaunch.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import net.ashmeet.hyperlauncher.R
import net.kdt.pojavlaunch.PojavApplication
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.extra.ExtraCore
import net.kdt.pojavlaunch.instances.Instances
import net.kdt.pojavlaunch.modloaders.*
import net.kdt.pojavlaunch.progresskeeper.ProgressKeeper
import net.kdt.pojavlaunch.ui.screens.FabricInstallScreen
import net.kdt.pojavlaunch.ui.theme.PojavTheme
import java.io.File
import java.io.IOException
import java.util.concurrent.Future

class FabricInstallFragment : Fragment(), ModloaderDownloadListener {

    private var mFabriclikeUtils = FabriclikeUtils.FABRIC_UTILS
    private var mExtraTag = TAG + "_proxy"

    private var mGameVersions by mutableStateOf(emptyList<FabricVersion>())
    private var mLoaderVersions by mutableStateOf(emptyList<FabricVersion>())
    private var mSelectedGameVersion by mutableStateOf<String?>(null)
    private var mSelectedLoaderVersion by mutableStateOf<String?>(null)
    private var mOnlyStable by mutableStateOf(true)
    private var mIsLoading by mutableStateOf(false)
    private var mShowRetry by mutableStateOf(false)

    private var mGameVersionFuture: Future<*>? = null
    private var mLoaderVersionFuture: Future<*>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val type = arguments?.getString(ARG_TYPE)
        if (type != null) {
            mFabriclikeUtils = FabriclikeUtils.getById(type)
            mExtraTag = type + "_proxy"
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val filteredGameVersions = remember(mGameVersions, mOnlyStable) {
                    mGameVersions.filter { !mOnlyStable || it.stable }
                }
                val filteredLoaderVersions = remember(mLoaderVersions, mOnlyStable) {
                    mLoaderVersions.filter { !mOnlyStable || it.stable }
                }

                PojavTheme {
                    FabricInstallScreen(
                        title = getString(R.string.fabric_dl_loader_title),
                        gameVersions = filteredGameVersions,
                        loaderVersions = filteredLoaderVersions,
                        selectedGameVersion = mSelectedGameVersion,
                        selectedLoaderVersion = mSelectedLoaderVersion,
                        onlyStable = mOnlyStable,
                        isLoading = mIsLoading,
                        showRetry = mShowRetry,
                        canInstall = mSelectedGameVersion != null && mSelectedLoaderVersion != null,
                        gameVersionLabel = getString(R.string.fabric_dl_game_version),
                        loaderVersionLabel = getString(R.string.fabric_dl_loader_version, mFabriclikeUtils.name),
                        onlyStableLabel = getString(R.string.fabric_dl_only_stable),
                        installLabel = getString(R.string.global_install),
                        retryLabel = getString(R.string.global_retry),
                        retryMessage = getString(R.string.fabric_dl_cant_read_meta, mFabriclikeUtils.name),
                        onGameVersionSelected = {
                            mSelectedGameVersion = it
                            mSelectedLoaderVersion = null
                            updateLoaderVersions()
                        },
                        onLoaderVersionSelected = { mSelectedLoaderVersion = it },
                        onOnlyStableChanged = {
                            mOnlyStable = it

                            val games = mGameVersions.filter { !mOnlyStable || it.stable }
                            if (mSelectedGameVersion != null && games.none { g -> g.version == mSelectedGameVersion }) {
                                mSelectedGameVersion = games.firstOrNull()?.version
                                mSelectedLoaderVersion = null
                                updateLoaderVersions()
                            }

                            val loaders = mLoaderVersions.filter { !mOnlyStable || it.stable }
                            if (mSelectedLoaderVersion != null && loaders.none { l -> l.version == mSelectedLoaderVersion }) {
                                mSelectedLoaderVersion = loaders.firstOrNull()?.version
                            }
                        },
                        onRetry = { onClickRetry() },
                        onInstall = { onClickStart() }
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val proxy = getListenerProxy()
        if (proxy != null) {
            mIsLoading = true
            proxy.attachListener(this)
        }
        updateGameVersions()
    }

    private fun updateGameVersions() {
        mIsLoading = true
        mShowRetry = false
        mGameVersionFuture?.cancel(true)
        mGameVersionFuture = PojavApplication.sExecutorService.submit {
            try {
                val versions = mFabriclikeUtils.downloadGameVersions()
                Tools.runOnUiThread {
                    if (versions != null) {
                        mGameVersions = versions.toList()
                        if (mSelectedGameVersion == null) {
                            val filtered = mGameVersions.filter { !mOnlyStable || it.stable }
                            mSelectedGameVersion = filtered.firstOrNull()?.version
                            if (mSelectedGameVersion != null) updateLoaderVersions()
                        }
                        mIsLoading = false
                    } else {
                        onException()
                    }
                }
            } catch (e: IOException) {
                onException()
            }
        }
    }

    private fun updateLoaderVersions() {
        val gameVer = mSelectedGameVersion ?: return
        mIsLoading = true
        mShowRetry = false
        mLoaderVersionFuture?.cancel(true)
        mLoaderVersionFuture = PojavApplication.sExecutorService.submit {
            try {
                val versions = mFabriclikeUtils.downloadLoaderVersions(gameVer)
                Tools.runOnUiThread {
                    if (versions != null) {
                        mLoaderVersions = versions.toList()
                        val filtered = mLoaderVersions.filter { !mOnlyStable || it.stable }
                        mSelectedLoaderVersion = filtered.firstOrNull()?.version
                        mIsLoading = false
                    } else {
                        onException()
                    }
                }
            } catch (e: IOException) {
                onException()
            }
        }
    }

    private fun onException() {
        Tools.runOnUiThread {
            mIsLoading = false
            mShowRetry = true
        }
    }

    private fun onClickRetry() {
        if (mGameVersions.isEmpty()) {
            updateGameVersions()
        } else {
            updateLoaderVersions()
        }
    }

    private fun onClickStart() {
        if (ProgressKeeper.hasOngoingTasks()) {
            Toast.makeText(context, R.string.tasks_ongoing, Toast.LENGTH_LONG).show()
            return
        }
        val proxy = ModloaderListenerProxy()
        proxy.attachListener(this)
        setListenerProxy(proxy)
        mIsLoading = true
        PojavApplication.sExecutorService.execute { performInstallation() }
    }

    private fun performInstallation() {
        try {
            val versionId = mFabriclikeUtils.install(mSelectedGameVersion, mSelectedLoaderVersion)
            if (versionId == null) {
                getListenerProxy()?.onDataNotAvailable()
                return
            }
            Instances.createInstance({ i ->
                i.name = mFabriclikeUtils.name
                i.icon = mFabriclikeUtils.iconName
                i.versionId = versionId
            }, versionId)
            getListenerProxy()?.onDownloadFinished(null)
        } catch (e: IOException) {
            Tools.showErrorRemote(e)
        }
    }

    override fun onDownloadFinished(downloadedFile: File?) {
        Tools.runOnUiThread {
            getListenerProxy()?.detachListener()
            setListenerProxy(null)
            mIsLoading = false
            parentFragmentManager.popBackStackImmediate()
        }
    }

    override fun onDataNotAvailable() {
        Tools.runOnUiThread {
            getListenerProxy()?.detachListener()
            setListenerProxy(null)
            mIsLoading = false
            Tools.dialog(requireContext(), getString(R.string.global_error), getString(R.string.fabric_dl_cant_read_meta, mFabriclikeUtils.name))
        }
    }

    override fun onDownloadError(e: Exception?) {
        Tools.runOnUiThread {
            getListenerProxy()?.detachListener()
            setListenerProxy(null)
            mIsLoading = false
            Tools.showError(requireContext(), e)
        }
    }

    override fun onStop() {
        mGameVersionFuture?.cancel(true)
        mLoaderVersionFuture?.cancel(true)
        getListenerProxy()?.detachListener()
        super.onStop()
    }

    private fun getListenerProxy(): ModloaderListenerProxy? = ExtraCore.getValue(mExtraTag) as? ModloaderListenerProxy
    private fun setListenerProxy(proxy: ModloaderListenerProxy?) = ExtraCore.setValue(mExtraTag, proxy)

    companion object {
        const val TAG = "FabricInstallFragment"
        const val ARG_TYPE = "type"
    }
}
