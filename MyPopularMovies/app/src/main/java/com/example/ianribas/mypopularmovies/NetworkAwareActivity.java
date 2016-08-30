package com.example.ianribas.mypopularmovies;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.example.ianribas.mypopularmovies.model.ConnectivityManagerDelegate;
import com.example.ianribas.mypopularmovies.model.NetworkStateListener;

/**
 */
public abstract class NetworkAwareActivity extends AppCompatActivity implements NetworkStateListener {
    protected View mOfflineView;
    protected BroadcastReceiver mNetworkStateBroadcastReceiver;
    protected ConnectivityManagerDelegate mConnectivityManagerDelegate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mConnectivityManagerDelegate = new ConnectivityManagerDelegate((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE));
    }

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
        }
    }

    protected void setupNetworkStateBroadcastReceiver() {
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
