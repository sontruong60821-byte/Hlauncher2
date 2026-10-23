package net.kdt.pojavlaunch.authenticator.accounts;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import androidx.annotation.Keep;
import androidx.annotation.Nullable;

import com.google.gson.JsonParseException;

import net.kdt.pojavlaunch.PojavApplication;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.authenticator.AuthType;
import net.kdt.pojavlaunch.lifecycle.ContextExecutor;
import net.kdt.pojavlaunch.skin.AndroidSkinAnalyzer;
import net.kdt.pojavlaunch.skin.SkinModelType;
import net.kdt.pojavlaunch.utils.FileUtils;
import net.kdt.pojavlaunch.utils.JSONUtils;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

@Keep
public class MinecraftAccount {
    public transient File mSaveLocation;
    public String accessToken = "0";
    public String profileId = "00000000-0000-0000-0000-000000000000";
    public String username = "Steve";
    public AuthType authType = AuthType.LOCAL;
    public boolean isMicrosoft = false;
    public String refreshToken = "0";
    public String xuid;
    public long expiresAt;
    public String skinPath;
    public String capePath;
    public SkinModelType skinModel = SkinModelType.STEVE;
    private transient Bitmap mFaceCache;

    protected MinecraftAccount() {}

    private Bitmap downloadSkin(String urlString) throws IOException {
        byte[] data = downloadSkinBytes(urlString);
        if (data == null) return null;
        return BitmapFactory.decodeByteArray(data, 0, data.length);
    }

    public void updateSkinFace(@Nullable AssetManager assetManager) {

        if (authType == AuthType.LOCAL && skinPath != null) {
            File localSkin = new File(skinPath);
            if (localSkin.exists()) {
                try (InputStream is = new FileInputStream(localSkin)) {
                    byte[] skinBytes = IOUtils.toByteArray(is);
                    Bitmap skinBitmap = BitmapFactory.decodeByteArray(skinBytes, 0, skinBytes.length);
                    if (skinBitmap != null) {
                        skinModel = AndroidSkinAnalyzer.INSTANCE.detectModel(skinBytes);
                        Bitmap skinFace = processSkinToHead(skinBitmap);
                        if (skinFace != null) {
                            saveSkinFace(skinFace);
                            try { save(); } catch (IOException ignored) {}
                        }
                        return;
                    }
                } catch (IOException e) {
                    Log.w("SkinLoader", "Failed to process local skin", e);
                }
            }
        }

        String skinUrlTemplate = authType.skinUrl;

        try {

            byte[] skinBytes = null;
            if (skinUrlTemplate != null) {

                String idToUse = username;
                if (authType == AuthType.MICROSOFT && profileId != null && !profileId.contains("00000000")) {
                    idToUse = profileId;
                }

                String url = String.format(skinUrlTemplate, URLEncoder.encode(idToUse, "UTF-8"));
                Log.i("SkinLoader", "Downloading skin for " + username + " from " + url);
                skinBytes = downloadSkinBytes(url);

                if (skinBytes == null && authType == AuthType.MICROSOFT && !idToUse.equals(username)) {
                    url = String.format(skinUrlTemplate, URLEncoder.encode(username, "UTF-8"));
                    skinBytes = downloadSkinBytes(url);
                }
            }

            if (skinBytes != null) {
                Bitmap fullSkin = BitmapFactory.decodeByteArray(skinBytes, 0, skinBytes.length);
                if (fullSkin != null) {

                    skinModel = AndroidSkinAnalyzer.INSTANCE.detectModel(skinBytes);

                    Bitmap head = processSkinToHead(fullSkin);
                    if (head != null) {
                        saveSkinFace(head);
                        try { save(); } catch (IOException ignored) {}
                        return;
                    }
                }
            }

            if (assetManager != null) {
                Log.i("SkinLoader", "Falling back to Steve head for " + username);
                try (InputStream is = assetManager.open("steve.png")) {
                    byte[] steveBytes = IOUtils.toByteArray(is);
                    Bitmap steveSkin = BitmapFactory.decodeByteArray(steveBytes, 0, steveBytes.length);
                    if (steveSkin != null) {
                        skinModel = SkinModelType.STEVE;
                        Bitmap skinFace = processSkinToHead(steveSkin);
                        if (skinFace != null) saveSkinFace(skinFace);
                        try { save(); } catch (IOException ignored) {}
                    }
                }
            }

        } catch (IOException e) {
            Log.w("SkinLoader", "Error updating skin face for " + username, e);
        }
    }

    private byte[] downloadSkinBytes(String urlString) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(urlString).openConnection();
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);
        conn.setInstanceFollowRedirects(true);

        try {
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                try (InputStream is = conn.getInputStream()) {
                    return IOUtils.toByteArray(is);
                }
            }
            return null;
        } finally {
            conn.disconnect();
        }
    }

    private Bitmap processSkinToHead(Bitmap skinBitmap) {
        int w = skinBitmap.getWidth();
        int h = skinBitmap.getHeight();

        boolean isStandardSkinSize = (w > 0 && w % 64 == 0) && (h == w || h == w / 2);

        if (w == h && (w == 120 || !isStandardSkinSize)) {
            return skinBitmap;
        } else {

            Bitmap head = new net.kdt.pojavlaunch.authenticator.accounts.SkinHeadRenderer().render(120, skinBitmap);
            if (head != null) {
                if (head != skinBitmap) skinBitmap.recycle();
                return head;
            }

            if (isStandardSkinSize) {
                int s = w / 64;
                try {

                    Bitmap face = Bitmap.createBitmap(skinBitmap, 8 * s, 8 * s, 8 * s, 8 * s);
                    Bitmap scaledFace = Bitmap.createScaledBitmap(face, 120, 120, false);
                    face.recycle();
                    if (skinBitmap != scaledFace) skinBitmap.recycle();
                    return scaledFace;
                } catch (Exception e) {
                    Log.e("SkinLoader", "Failed to crop 2D face fallback", e);
                }
            }

            if (w == h) return skinBitmap;
            return null;
        }
    }

    private synchronized void saveSkinFace(Bitmap skinFace) throws IOException {
        File skinFile = getSkinFaceFile();
        FileUtils.ensureParentDirectory(skinFile);

        File tempFile = new File(skinFile.getAbsolutePath() + ".tmp");
        try (FileOutputStream fileOutputStream = new FileOutputStream(tempFile)) {
            skinFace.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.getFD().sync();
        }

        if (!tempFile.renameTo(skinFile)) {
            if (!skinFile.delete() || !tempFile.renameTo(skinFile)) {
                throw new IOException("Failed to replace skin file " + skinFile.getAbsolutePath());
            }
        }

        mFaceCache = skinFace;
        Log.i("SkinLoader", "Saved skin face: " + skinFile.getAbsolutePath());
    }

    public void updateSkinFace() {
        Context context = ContextExecutor.getContext();
        if (context != null) {
            updateSkinFace(context.getAssets());
        } else {
            updateSkinFace(null);
        }
    }

    public boolean isLocal(){
        return accessToken.equals("0");
    }

    public void save() throws IOException {
        if (mSaveLocation == null) return;
        FileUtils.ensureParentDirectory(mSaveLocation);
        JSONUtils.writeToFile(mSaveLocation, this);
    }

    public MinecraftAccount reload() {
        try {
            MinecraftAccount minecraftAccount = JSONUtils.readFromFile(mSaveLocation, MinecraftAccount.class);
            if(minecraftAccount == null) return null;
            minecraftAccount.mSaveLocation = mSaveLocation;
            return minecraftAccount;
        }catch (IOException | JsonParseException e) {
            return null;
        }
     }

    public Bitmap getSkinFace(){
        File skinFaceFile = getSkinFaceFile();
        if(!skinFaceFile.exists()) {
             return null;
        }

        if(mFaceCache == null || mFaceCache.isRecycled()) {
            Bitmap loaded = null;
            try {
                loaded = BitmapFactory.decodeFile(skinFaceFile.getAbsolutePath());
            } catch (Exception e) {
                Log.e("SkinLoader", "Failed to decode skin face file", e);
            }

            if (loaded == null) return null;

            int w = loaded.getWidth();
            int h = loaded.getHeight();

            if (h == w / 2 && w >= 64) {
                Log.i("SkinLoader", "Fixing legacy 2:1 texture on the fly for " + username);
                Bitmap fixed = processSkinToHead(loaded);
                if (fixed != null) {
                    mFaceCache = fixed;
                    PojavApplication.sExecutorService.execute(() -> {
                        try { saveSkinFace(fixed); } catch (IOException e) { Log.w("SkinLoader", "Fix save failed", e); }
                    });
                }
            } else {
                mFaceCache = loaded;
            }
        }
        return mFaceCache;
    }

    private File getSkinFaceFile() {
        return new File(Tools.DIR_CACHE,  "skin-face-" + profileId +"-"+authType.name() + ".png");
    }
}
