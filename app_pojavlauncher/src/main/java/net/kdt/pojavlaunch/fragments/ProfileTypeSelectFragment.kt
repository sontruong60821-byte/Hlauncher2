package net.kdt.pojavlaunch.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.instances.Instances
import net.kdt.pojavlaunch.ui.screens.ProfileTypeSelectScreen
import net.kdt.pojavlaunch.ui.theme.PojavTheme
import java.io.IOException

class ProfileTypeSelectFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                PojavTheme {
                    ProfileTypeSelectScreen(
                        onBack = { parentFragmentManager.popBackStack() },
                        onVanillaClick = {
                            try {
                                val instance = Instances.createDefaultInstance()
                                Instances.setSelectedInstance(instance)
                                val bundle = Bundle().apply {
                                    putBoolean(InstanceEditorFragment.ARG_IS_NEW_INSTANCE, true)
                                }
                                Tools.swapFragment(requireActivity(), InstanceEditorFragment::class.java, InstanceEditorFragment.TAG, bundle)
                            } catch (e: IOException) {
                                Tools.showError(context, e)
                            }
                        },
                        onOptifineClick = { Tools.swapFragment(requireActivity(), OptiFineInstallFragment::class.java, OptiFineInstallFragment.TAG, null) },
                        onFabricClick = {
                             val bundle = Bundle().apply { putString(FabricInstallFragment.ARG_TYPE, "fabric") }
                             Tools.swapFragment(requireActivity(), FabricInstallFragment::class.java, FabricInstallFragment.TAG, bundle)
                        },
                        onForgeClick = { Tools.swapFragment(requireActivity(), ForgeInstallFragment::class.java, ForgeInstallFragment.TAG, null) },
                        onQuiltClick = {
                             val bundle = Bundle().apply { putString(FabricInstallFragment.ARG_TYPE, "quilt") }
                             Tools.swapFragment(requireActivity(), FabricInstallFragment::class.java, FabricInstallFragment.TAG, bundle)
                        },
                        onNeoForgeClick = { Tools.swapFragment(requireActivity(), NeoforgeInstallFragment::class.java, NeoforgeInstallFragment.TAG, null) },
                        onLegacyFabricClick = {
                             val bundle = Bundle().apply { putString(FabricInstallFragment.ARG_TYPE, "legacy_fabric") }
                             Tools.swapFragment(requireActivity(), FabricInstallFragment::class.java, FabricInstallFragment.TAG, bundle)
                        },
                        onModpackClick = { Tools.swapFragment(requireActivity(), SearchModFragment::class.java, SearchModFragment.TAG, null) },
                        onBTAClick = { Tools.swapFragment(requireActivity(), BTAInstallFragment::class.java, BTAInstallFragment.TAG, null) }
                    )
                }
            }
        }
    }

    companion object {
        const val TAG = "ProfileTypeSelectFragment"
    }
}
