package net.kdt.pojavlaunch.skin

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.kdt.pojavlaunch.authenticator.AuthType
import net.kdt.pojavlaunch.authenticator.accounts.MinecraftAccount
import net.kdt.pojavlaunch.authenticator.accounts.SkinHeadRenderer
import java.io.File

object SkinUtils {

    private const val TAG = "SkinUtils"

    /**
     * Consolidates skin URL generation for different account types.
     */
    fun getSkinUrl(account: MinecraftAccount?): String {
        if (account == null) return "steve.png"

        return when (account.authType) {
            AuthType.LOCAL -> {
                if (account.skinPath != null) "file://${account.skinPath}"
                else "steve.png"
            }
            AuthType.ELY_BY -> {
                "https://skinsystem.ely.by/skins/${account.username}.png"
            }
            AuthType.MICROSOFT -> {
                if (account.profileId != null && !account.profileId.contains("00000000")) {
                    "https://minotar.net/skin/${account.profileId}"
                } else {
                    "https://minotar.net/skin/${account.username}"
                }
            }
            else -> "steve.png"
        }
    }

    /**
     * Determines the model type for the skin viewer.
     */
    fun getModelType(account: MinecraftAccount?): String {
        return when (account?.skinModel) {
            SkinModelType.ALEX -> "slim"
            else -> "default"
        }
    }

    /**
     * Renders a 3D isometric head from a skin bitmap or file.
     */
    suspend fun renderHead(context: Context, account: MinecraftAccount?): Bitmap? = withContext(Dispatchers.IO) {
        if (account == null) return@withContext loadSteveHead(context)

        try {
            val cachedFace = account.skinFace
            if (cachedFace != null && !cachedFace.isRecycled) {
                return@withContext cachedFace
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to get skinFace for ${account.username}", e)
        }

        if (account.authType == AuthType.LOCAL && account.skinPath != null) {
            val file = File(account.skinPath)
            if (file.exists()) {
                val bitmap = try {
                    BitmapFactory.decodeFile(file.absolutePath)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to decode skin file ${file.absolutePath}", e)
                    null
                }

                if (bitmap != null) {
                    val head = try {
                        SkinHeadRenderer().render(120, bitmap)
                    } catch (e: Exception) {
                        Log.e(TAG, "Renderer error for local skin", e)
                        null
                    }

                    if (head != null) {
                        if (head != bitmap) bitmap.recycle()
                        return@withContext head
                    }

                    if (bitmap.width == bitmap.height) return@withContext bitmap

                    bitmap.recycle()
                }
            }
        }

        return@withContext loadSteveHead(context)
    }

    private fun loadSteveHead(context: Context): Bitmap? {
        try {

            val steveBitmap = try {
                context.assets.open("steve.png").use { inputStream ->
                    BitmapFactory.decodeStream(inputStream)
                }
            } catch (e: Exception) {
                Log.w(TAG, "steve.png not found in assets, trying drawable fallback")
                null
            }

            if (steveBitmap != null) {
                val head = try {
                    SkinHeadRenderer().render(120, steveBitmap)
                } catch (e: Exception) {
                    Log.e(TAG, "Renderer failed to render steve.png", e)
                    null
                }

                if (head != null) {
                    if (head != steveBitmap) steveBitmap.recycle()
                    return head
                }

                if (steveBitmap.width == steveBitmap.height) {
                    return steveBitmap
                }
                steveBitmap.recycle()
            }

            val resId = context.resources.getIdentifier("head_steve", "drawable", context.packageName)
            if (resId != 0) {
                return BitmapFactory.decodeResource(context.resources, resId)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Fatal error loading steve head", e)
        }
        return null
    }

    /**
     * Composable helper to get a skin head state.
     */
    @Composable
    fun rememberSkinHead(account: MinecraftAccount?): State<Bitmap?> {
        val context = LocalContext.current
        return produceState<Bitmap?>(initialValue = null, account) {
            value = renderHead(context, account)
        }
    }
}
