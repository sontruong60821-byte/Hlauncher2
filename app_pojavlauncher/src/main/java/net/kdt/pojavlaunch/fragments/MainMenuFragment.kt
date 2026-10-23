package net.kdt.pojavlaunch.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.Fragment
import net.ashmeet.hyperlauncher.R
import net.kdt.pojavlaunch.CustomControlsActivity
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.contracts.OpenDocumentWithExtension
import net.kdt.pojavlaunch.extra.ExtraConstants
import net.kdt.pojavlaunch.extra.ExtraCore
import net.kdt.pojavlaunch.instances.Instances
import net.kdt.pojavlaunch.prefs.LauncherPreferences
import net.kdt.pojavlaunch.progresskeeper.ProgressKeeper
import net.kdt.pojavlaunch.services.ProgressService
import net.kdt.pojavlaunch.ui.theme.PojavTheme
import net.kdt.pojavlaunch.utils.FileUtils
import com.kdt.mcgui.LauncherMenuButton
import com.kdt.mcgui.MineButton
import com.kdt.mcgui.mcVersionSpinner
import net.kdt.pojavlaunch.kotlin.ui.screens.MainMenuRevamp

class MainMenuFragment : Fragment() {

    private val mModInstallerLauncher: ActivityResultLauncher<Any?> =
        registerForActivityResult(OpenDocumentWithExtension("jar")) { data ->
            if (data != null) Tools.launchModInstaller(requireContext(), data)
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val themeEnabled by LauncherPreferences.PREF_THEME_TYPE_ENABLED_STATE
                val themeMode by LauncherPreferences.PREF_THEME_TYPE_MODE_STATE

                if (themeEnabled && themeMode == "crynoix") {
                    AndroidView(
                        factory = { ctx ->
                            inflater.inflate(R.layout.fragment_launcher, null).apply {
                                setupXmlViews(this)
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    PojavTheme {
                        MainMenuRevamp(
                            onEditProfileClick = {
                                Tools.swapFragment(requireActivity(), InstanceEditorFragment::class.java, InstanceEditorFragment.TAG, null)
                            },
                            onCustomControlsClick = {
                                startActivity(Intent(requireContext(), CustomControlsActivity::class.java))
                            },
                            onInstallJarClick = { runInstallerWithConfirmation() },
                            onShareLogsClick = { Tools.shareLog(requireContext()) },
                            onOpenFilesClick = { openGameDirectory(requireContext()) },
                            onWikiClick = { Tools.openURL(requireActivity(), Tools.URL_HOME) },
                            onSocialMediaClick = { Tools.openURL(requireActivity(), getString(R.string.social_media_invite)) },
                            onPlayClick = { ExtraCore.setValue(ExtraConstants.LAUNCH_GAME, true) },
                            onTerminateClick = {
                                ProgressService.stopService(requireContext())
                                Tools.fullyExit()
                            },
                            onInstanceSelect = {
                                Tools.swapFragment(requireActivity(), ProfileSelectionFragment::class.java, ProfileSelectionFragment.TAG, null)
                            }
                        )
                    }
                }
            }
        }
    }

    private fun setupXmlViews(view: View) {
        view.findViewById<LauncherMenuButton>(R.id.news_button)?.setOnClickListener {
            Tools.openURL(requireActivity(), Tools.URL_HOME)
        }
        view.findViewById<LauncherMenuButton>(R.id.social_media_button)?.setOnClickListener {
            Tools.openURL(requireActivity(), getString(R.string.social_media_invite))
        }
        view.findViewById<LauncherMenuButton>(R.id.custom_control_button)?.setOnClickListener {
            startActivity(Intent(requireContext(), CustomControlsActivity::class.java))
        }
        view.findViewById<LauncherMenuButton>(R.id.install_jar_button)?.setOnClickListener {
            runInstallerWithConfirmation()
        }
        view.findViewById<LauncherMenuButton>(R.id.share_logs_button)?.setOnClickListener {
            Tools.shareLog(requireContext())
        }
        view.findViewById<LauncherMenuButton>(R.id.open_files_button)?.setOnClickListener {
            openGameDirectory(requireContext())
        }
        view.findViewById<ImageButton>(R.id.edit_profile_button)?.setOnClickListener {
            Tools.swapFragment(requireActivity(), InstanceEditorFragment::class.java, InstanceEditorFragment.TAG, null)
        }
        view.findViewById<mcVersionSpinner>(R.id.mc_version_spinner)?.setOnClickListener {
            Tools.swapFragment(requireActivity(), ProfileSelectionFragment::class.java, ProfileSelectionFragment.TAG, null)
        }
        view.findViewById<MineButton>(R.id.play_button)?.setOnClickListener {
            ExtraCore.setValue(ExtraConstants.LAUNCH_GAME, true)
        }
    }

    private fun openGameDirectory(context: Context) {
        val instance = Instances.loadSelectedInstance()
        if (instance == null) {
            Toast.makeText(context, R.string.no_instance, Toast.LENGTH_LONG).show()
            return
        }
        val gameDirectory = instance.gameDirectory
        if (FileUtils.ensureDirectorySilently(gameDirectory)) {
            Tools.openPath(context, gameDirectory, false)
        } else {
            Toast.makeText(context, R.string.gamedir_open_failed, Toast.LENGTH_LONG).show()
        }
    }

    override fun onResume() {
        super.onResume()
        ExtraCore.setValue(ExtraConstants.REFRESH_ACCOUNT_SPINNER, true)
    }

    private fun runInstallerWithConfirmation() {
        if (ProgressKeeper.getTaskCount() == 0) {
            mModInstallerLauncher.launch(null)
        } else Toast.makeText(requireContext(), R.string.tasks_ongoing, Toast.LENGTH_LONG).show()
    }

    companion object {
        const val TAG = "MainMenuFragment"
    }
}
