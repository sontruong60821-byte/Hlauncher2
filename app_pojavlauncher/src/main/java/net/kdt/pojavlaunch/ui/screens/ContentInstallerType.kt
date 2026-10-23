package net.kdt.pojavlaunch.ui.screens

import net.ashmeet.hyperlauncher.R

enum class ContentInstallerType(val projectType: String, val labelRes: Int, val iconRes: Int) {
    MODS("mod", R.string.installer_mods, R.drawable.add_row_below_40px),
    SHADERS("shader", R.string.installer_shaders, R.drawable.lightbulb_2_40px),
    RESOURCES("resourcepack", R.string.installer_packs, R.drawable.box_edit_40px)
}
