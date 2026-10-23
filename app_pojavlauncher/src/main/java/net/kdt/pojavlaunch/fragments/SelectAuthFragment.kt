package net.kdt.pojavlaunch.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import net.ashmeet.hyperlauncher.R
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.extra.ExtraConstants
import net.kdt.pojavlaunch.extra.ExtraCore
import net.kdt.pojavlaunch.kotlin.ui.screens.SelectAuthScreen
import net.kdt.pojavlaunch.ui.theme.PojavTheme
import java.util.regex.Pattern

class SelectAuthFragment : Fragment() {

    private val mUsernameValidationPattern = Pattern.compile("^[a-zA-Z0-9_]*$")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                PojavTheme {
                    SelectAuthScreen(
                        onMicrosoftClick = { Tools.swapFragment(requireActivity(), MicrosoftLoginFragment::class.java, MicrosoftLoginFragment.TAG, null) },
                        onLocalClick = { username, skinPath, capePath ->
                            if (!checkUsername(username)) {
                                Tools.dialog(requireContext(), getString(R.string.local_login_bad_username_title), getString(R.string.local_login_bad_username_text))
                            } else {
                                ExtraCore.setValue(ExtraConstants.MOJANG_LOGIN_TODO, arrayOf(username, "", skinPath, capePath))
                                Tools.swapFragment(requireActivity(), MainMenuFragment::class.java, MainMenuFragment.TAG, null)
                            }
                        },
                        onElyByClick = { Tools.swapFragment(requireActivity(), ElyByLoginFragment::class.java, ElyByLoginFragment.TAG, null) }
                    )
                }
            }
        }
    }

    private fun checkUsername(username: String): Boolean {
        return username.isNotEmpty() &&
                username.length in 3..16 &&
                mUsernameValidationPattern.matcher(username).find()
    }

    companion object {
        const val TAG = "SelectAuthFragment"
    }
}
