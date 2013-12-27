package org.telegram.android.kernel;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Build;
import org.telegram.android.R;
import org.telegram.android.core.background.UpdateProcessor;
import org.telegram.android.log.Logger;
import org.telegram.api.TLAbsUpdates;
import org.telegram.api.engine.ApiCallback;
import org.telegram.api.engine.AppInfo;
import org.telegram.api.engine.TelegramApi;

/**
 * Created by ex3ndr on 16.11.13.
 */
public class ApiKernel {
    private static final String TAG = "ApiKernel";
    private ApplicationKernel kernel;

    private TelegramApi api;

    public ApiKernel(ApplicationKernel kernel) {
        this.kernel = kernel;
    }

    public ApplicationKernel getKernel() {
        return kernel;
    }

    public void runKernel() {
        api = new TelegramApi(kernel.getAuthKernel().getApiStorage(), new AppInfo(5, Build.MODEL, Build.VERSION.RELEASE, kernel.getTechKernel().getTechReflection().getAppVersion(),
                kernel.getApplication().getString(R.string.st_lang)), new ApiCallback() {
            @Override
            public void onApiDies(TelegramApi api) {
                if (api != ApiKernel.this.api) {
                    return;
                }
                kernel.logOut();
            }

            @Override
            public void onUpdatesInvalidated(TelegramApi api) {
                if (api != ApiKernel.this.api) {
                    return;
                }
                if (!kernel.getAuthKernel().isLoggedIn()) {
                    return;
                }
                kernel.getSyncKernel().getUpdateProcessor().invalidateUpdates();
            }

            @Override
            public void onUpdate(TLAbsUpdates updates) {
                if (api != ApiKernel.this.api) {
                    return;
                }
                if (!kernel.getAuthKernel().isLoggedIn()) {
                    return;
                }
                UpdateProcessor updateProcessor = kernel.getSyncKernel().getUpdateProcessor();
                if (updateProcessor != null) {
                    updateProcessor.onMessage(updates);
                }
            }
        });

        BroadcastReceiver networkStateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Logger.w(TAG, "Network Type Changed");
                api.resetNetworkBackoff();
            }
        };

        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        kernel.getApplication().registerReceiver(networkStateReceiver, filter);
    }

    public TelegramApi getApi() {
        return api;
    }
}
