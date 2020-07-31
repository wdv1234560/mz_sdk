package com.mz.segiu.utils;

import android.app.Activity;
import android.os.Build;
import com.mz.segiu.app.GlideEngine;
import com.jess.arms.utils.ArmsUtils;
import com.jess.arms.utils.FileUtils;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureMimeType;

import org.jetbrains.annotations.NotNull;

public final class PictureSelectorUtils {
    public static final String OFFLINE_CACHE_CAMERA_PATH = "offlineCache";

    public static void toCamera(@NotNull Activity activity, int requestCode) {
        PictureSelector.create(activity)
                .openCamera(PictureMimeType.ofImage())
                .setOutputCameraPath(FileUtils.createRootPath(ArmsUtils.getContext()))
                .isUseCustomCamera(isUserCustomCamera())
                .loadImageEngine(GlideEngine.createGlideEngine())
                .forResult(requestCode);
    }

    public static void toCameraByOffline(@NotNull Activity activity, int requestCode) {
        PictureSelector.create(activity)
                .openCamera(PictureMimeType.ofImage())
                .setOutputCameraPath(FileUtils.createDir(FileUtils.createRootPath(activity) + "/offlineCache"))
                .isUseCustomCamera(isUserCustomCamera())
                .loadImageEngine(GlideEngine.createGlideEngine())
                .forResult(requestCode);
    }

    public static boolean isUserCustomCamera() {
        if ("Xiaomi".equals(Build.MANUFACTURER) || "OPPO R11".contains(Build.MODEL)) {
            return true;
        }
        return false;
    }

}