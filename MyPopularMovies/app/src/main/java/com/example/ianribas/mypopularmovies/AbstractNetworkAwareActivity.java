package com.example.ianribas.mypopularmovies;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.example.ianribas.mypopularmovies.util.network.ConnectivityManagerDelegate;
import com.example.ianribas.mypopularmovies.util.network.NetworkStateListener;

import javax.inject.Inject;

/**
 */
public abstract class AbstractNetworkAwareActivity extends AppCompatActivity implements NetworkStateListener {
    protected View mOfflineView;
    private BroadcastReceiver mNetworkStateBroadcastReceiver;

    @Inject
    ConnectivityManagerDelegate mConnectivityManagerDelegate;


    @Override
    protected void onResume() {
        super.onResume();
        if (mOfflineView != null && mOfflineView.getVisibility() == View.VISIBLE) {
            setupNetworkStateBroadcastReceiver();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mNetworkStateBroadcastReceiver != null) {
            this.unregisterReceiver(mNetworkStateBroadcastReceiver);
            mNetworkStateBroadcastReceiver = null;
        }
    }

    @Override
    public void onNetworkUnavailable() {
        if (!mConnectivityManagerDelegate.isOnline()) {
            setupNetworkStateBroadcastReceiver();
        }
    }

    @Override
    public void onNetworkAvailable() {
        if (mNetworkStateBroadcastReceiver != null) {
            unregisterReceiver(mNetworkStateBroadcastReceiver);
            mNetworkStateBroadcastReceiver = null;
        }
    }

    private void setupNetworkStateBroadcastReceiver() {
        mNetworkStateBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent == null) {
                    return;
                }

                if (mConnectivityManagerDelegate.isOnline() && mOfflineView != null) {
                    mOfflineView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            onNetworkAvailable();
                        }
                    }, 500);
                }
            }
        };

        this.registerReceiver(mNetworkStateBroadcastReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }
}
