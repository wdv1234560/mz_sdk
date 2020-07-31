package com.mz.segiu.widget.webview;

import org.jetbrains.annotations.Nullable;

public interface OnJsCallListener {
    void onJsCall(@Nullable String var1);

    void pageStarted(@Nullable String var1);

    void pageFinished(@Nullable String var1);
}
