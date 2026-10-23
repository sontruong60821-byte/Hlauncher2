package net.kdt.pojavlaunch.plugins;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LibraryPlugin {
    private static final String TAG = "LibraryPlugin";

    public static final String ID_ANGLE_PLUGIN = "git.mojo.angle";
    public static final String ID_FFMPEG_PLUGIN = "git.mojo.ffmpeg";
    public static final String ID_MOBILEGLUES_PLUGIN = "git.fcl.mobileglues";

    private static final String[] MOBILEGLUES_PACKAGES = {
            ID_MOBILEGLUES_PLUGIN,
            "net.kdt.mobileglues",
            "com.fcl.mobileglues",
            "com.fcl.plugin.mobileglues"
    };

    private String appId;
    private String libraryPath;
    private LibraryPlugin(String app, String libraryPath){
        this.appId = app;
        this.libraryPath = libraryPath;
    }

    public static LibraryPlugin discoverPlugin(Context ctx, String appId){

        LibraryPlugin directPlugin = discoverDirect(ctx, appId);
        if (directPlugin != null) return directPlugin;

        if (ID_MOBILEGLUES_PLUGIN.equals(appId)) {

            for (String pkg : MOBILEGLUES_PACKAGES) {
                if (pkg.equals(appId)) continue;
                LibraryPlugin p = discoverDirect(ctx, pkg);
                if (p != null) return p;
            }

            return discoverByMetaData(ctx, "renderer", "mobileglues");
        }

        return null;
    }

    private static LibraryPlugin discoverDirect(Context ctx, String appId) {
        try {
            PackageInfo pluginPackage = ctx.getPackageManager().getPackageInfo(appId, PackageManager.GET_SHARED_LIBRARY_FILES);
            return new LibraryPlugin(appId, pluginPackage.applicationInfo.nativeLibraryDir);
        } catch (Exception e){
            return null;
        }
    }

    private static LibraryPlugin discoverByMetaData(Context ctx, String metaKey, String metaValue) {
        PackageManager pm = ctx.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> infos = pm.queryIntentActivities(intent, PackageManager.GET_META_DATA);
        for (ResolveInfo info : infos) {
            ActivityInfo activityInfo = info.activityInfo;
            if (activityInfo == null) continue;

            Bundle metaData = activityInfo.metaData;

            if (metaData == null) {

                try {
                    ApplicationInfo appInfo = pm.getApplicationInfo(activityInfo.packageName, PackageManager.GET_META_DATA);
                    metaData = appInfo.metaData;
                } catch (PackageManager.NameNotFoundException ignored) {}
            }

            if (metaData != null && (metaData.getBoolean("fclPlugin", false) || metaData.containsKey(metaKey))) {
                Object value = metaData.get(metaKey);
                if (value != null && value.toString().equals(metaValue)) {
                    try {
                        ApplicationInfo appInfo = pm.getApplicationInfo(activityInfo.packageName, 0);
                        return new LibraryPlugin(activityInfo.packageName, appInfo.nativeLibraryDir);
                    } catch (PackageManager.NameNotFoundException e) {
                        return null;
                    }
                }
            }
        }
        return null;
    }

    public String getId(){
        return appId;
    }

    public String getLibraryPath(){
        return libraryPath;
    }
    public String resolveAbsolutePath(String library) {
        return new File(libraryPath, library).getAbsolutePath();
    }

    public boolean checkLibraries(String... libs){
        for(String lib : libs){
            if(!(new File(libraryPath, lib).exists())) return false;
        }
        return true;
    }
}
