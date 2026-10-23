package net.kdt.pojavlaunch.fragments

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.*
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import net.ashmeet.hyperlauncher.R
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.extra.ExtraConstants
import net.kdt.pojavlaunch.extra.ExtraCore
import net.kdt.pojavlaunch.instances.Instance
import net.kdt.pojavlaunch.instances.InstanceIconProvider
import net.kdt.pojavlaunch.instances.Instances
import net.kdt.pojavlaunch.multirt.MultiRTUtils
import net.kdt.pojavlaunch.multirt.Runtime
import net.kdt.pojavlaunch.profiles.VersionSelectorDialog
import net.kdt.pojavlaunch.ui.screens.InstanceEditorScreen
import net.kdt.pojavlaunch.ui.theme.PojavTheme
import net.kdt.pojavlaunch.utils.CropperUtils
import net.kdt.pojavlaunch.utils.RendererCompatUtil
import java.io.IOException

class InstanceEditorFragment : Fragment(), CropperUtils.CropperReceiver {

    private var mInstance: Instance? = null
    private var mRecommendedIconSize = 0
    private lateinit var mCropperLauncher: ActivityResultLauncher<*>

    private var mName by mutableStateOf("")
    private var mVersionId by mutableStateOf("")
    private var mControlLayout by mutableStateOf("")
    private var mJvmArgs by mutableStateOf("")
    private var mArgsMode by mutableIntStateOf(0)
    private var mSharedData by mutableStateOf(false)
    private var mCustomDirectory by mutableStateOf("")
    private var mSelectedRuntime by mutableStateOf<Runtime?>(null)
    private var mSelectedRendererIndex by mutableIntStateOf(0)
    private var mInstanceIconDrawable by mutableStateOf<android.graphics.drawable.Drawable?>(null)

    private var mRenderNames: List<String> = emptyList()
    private var mRenderDisplayNames: List<String> = emptyList()
    private var mAvailableRuntimes: List<Runtime> = emptyList()

    private var mWasSaved = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mCropperLauncher = CropperUtils.registerCropper(this, this)

        val renderersList = RendererCompatUtil.getCompatibleRenderers(requireContext())
        mRenderNames = renderersList.rendererIds
        mRenderDisplayNames = renderersList.rendererDisplayNames.toList() + getString(R.string.global_default)
        mAvailableRuntimes = MultiRTUtils.getRuntimes()

        mWasSaved = savedInstanceState?.getBoolean("was_saved", false) ?: false

        mInstance = Instances.loadSelectedInstance()
        if (mInstance == null) {
            Toast.makeText(requireContext(), R.string.no_instance, Toast.LENGTH_LONG).show()
            parentFragmentManager.popBackStack()
            return
        }

        loadValues()

        requireActivity().onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleBack()
            }
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("was_saved", mWasSaved)
    }

    private fun loadValues() {
        mInstance?.let { instance ->
            mName = instance.name ?: ""
            mVersionId = instance.versionId ?: ""
            mControlLayout = instance.controlLayout ?: ""
            mJvmArgs = instance.jvmArgs ?: ""
            mArgsMode = instance.argsMode
            mSharedData = instance.sharedData
            mCustomDirectory = if (instance.sharedData) "" else instance.instanceRoot.absolutePath

            mSelectedRuntime = mAvailableRuntimes.find { it.name == instance.selectedRuntime }
                ?: mAvailableRuntimes.lastOrNull()

            val rendererIndex = mRenderNames.indexOf(instance.getLaunchRenderer())
            mSelectedRendererIndex = if (rendererIndex == -1) mRenderDisplayNames.size - 1 else rendererIndex

            mInstanceIconDrawable = InstanceIconProvider.fetchIcon(resources, instance)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        (ExtraCore.consumeValue(ExtraConstants.FILE_SELECTOR) as? String)?.let {
            if (sFileSelectionMode == 1) {
                mControlLayout = it
            } else if (sFileSelectionMode == 2) {
                mCustomDirectory = it
            }
            sFileSelectionMode = 0
        }

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                PojavTheme {
                    InstanceEditorScreen(
                        onBack = { handleBack() },
                        onSave = { save() },
                        onDelete = {
                             mInstance?.let {
                                 try {
                                     Instances.removeInstance(it)
                                     mWasSaved = true
                                     Tools.backToMainMenu(requireActivity())
                                 } catch (e: IOException) {
                                     Tools.showErrorRemote(e)
                                 }
                             }
                        },
                        onIconClick = {
                            mRecommendedIconSize = 256
                            CropperUtils.startCropper(mCropperLauncher)
                        },
                        onVersionClick = {
                            VersionSelectorDialog.open(requireContext(), false) { id, _ -> mVersionId = id }
                        },
                        onControlClick = {
                            sFileSelectionMode = 1
                            val bundle = Bundle().apply {
                                putBoolean(FileSelectorFragment.BUNDLE_SELECT_FOLDER, false)
                                putString(FileSelectorFragment.BUNDLE_ROOT_PATH, Tools.CTRLMAP_PATH)
                            }
                            Tools.swapFragment(requireActivity(), FileSelectorFragment::class.java, FileSelectorFragment.TAG, bundle)
                        },
                        onCustomDirectoryClick = {
                            sFileSelectionMode = 2
                            val bundle = Bundle().apply {
                                putBoolean(FileSelectorFragment.BUNDLE_SELECT_FOLDER, true)
                                putString(FileSelectorFragment.BUNDLE_ROOT_PATH, Tools.DIR_GAME_HOME)
                            }
                            Tools.swapFragment(requireActivity(), FileSelectorFragment::class.java, FileSelectorFragment.TAG, bundle)
                        },

                        instanceIcon = mInstanceIconDrawable,
                        name = mName,
                        onNameChange = { mName = it },
                        versionId = mVersionId,
                        controlLayout = mControlLayout,
                        jvmArgs = mJvmArgs,
                        onJvmArgsChange = { mJvmArgs = it },
                        argsMode = mArgsMode,
                        onArgsModeChange = { mArgsMode = it },
                        sharedData = mSharedData,
                        onSharedDataChange = { mSharedData = it },
                        customDirectory = mCustomDirectory,

                        availableRuntimes = mAvailableRuntimes,
                        selectedRuntime = mSelectedRuntime,
                        onRuntimeSelected = { mSelectedRuntime = it },

                        rendererDisplayNames = mRenderDisplayNames,
                        selectedRendererIndex = mSelectedRendererIndex,
                        onRendererSelected = { mSelectedRendererIndex = it }
                    )
                }
            }
        }
    }

    private fun handleBack() {
        val isNewInstance = arguments?.getBoolean(ARG_IS_NEW_INSTANCE, false) ?: false
        if (isNewInstance && !mWasSaved) {
            mInstance?.let {
                try {
                    Instances.removeInstance(it)
                    Log.d("InstanceEditor", "Successfully cleaned up unsaved new instance at ${it.instanceRoot}")
                } catch (e: Exception) {
                    Log.e("InstanceEditor", "Failed to cleanup unsaved new instance", e)
                }
            }
        }
        parentFragmentManager.popBackStack()
    }

    private fun save() {
        mInstance?.let { instance ->
            instance.versionId = mVersionId
            instance.controlLayout = mControlLayout.ifEmpty { null }
            instance.name = mName
            instance.jvmArgs = mJvmArgs.ifEmpty { null }
            instance.argsMode = mArgsMode
            instance.sharedData = mSharedData

            instance.selectedRuntime = if (mSelectedRuntime?.name == "<Default>" || mSelectedRuntime?.versionString == null)
                null else mSelectedRuntime?.name

            instance.renderer = if (mSelectedRendererIndex == mRenderNames.size) null
            else mRenderNames[mSelectedRendererIndex]

            try {
                InstanceIconProvider.dropIcon(instance)
                instance.write()
                mWasSaved = true
                Tools.backToMainMenu(requireActivity())
            } catch (e: IOException) {
                Tools.showErrorRemote(e)
            }
        }
    }

    override fun getAspectRatio(): Float = 1f
    override fun getTargetMaxSide(): Int = mRecommendedIconSize

    override fun onCropped(contentBitmap: Bitmap) {
        mInstanceIconDrawable = android.graphics.drawable.BitmapDrawable(resources, contentBitmap)
        try {
            mInstance?.encodeNewIcon(contentBitmap)
        } catch (e: IOException) {
            Tools.showErrorRemote(e)
        }
    }

    override fun onFailed(exception: Exception) {
        Tools.showErrorRemote(exception)
    }

    companion object {
        const val TAG = "InstanceEditorFragment"
        const val ARG_IS_NEW_INSTANCE = "is_new_instance"
        private var sFileSelectionMode = 0
    }
}
