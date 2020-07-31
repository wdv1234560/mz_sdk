package com.mz.segiu.broadcasts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.jess.arms.integration.EventBusManager;

public class ScannerReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String scannerdata = intent.getStringExtra("scannerdata");
        EventBusManager.getInstance().post(scannerdata,"xmgScannerCode");
    }
}
