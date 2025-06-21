package com.example.quicknotes;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import androidx.lifecycle.LiveData;

public class ConnectionLiveData extends LiveData<String> {

    private final Context context;
    private final ConnectivityManager connectivityManager;

    public ConnectionLiveData(Context context) {
        this.context = context.getApplicationContext();
        this.connectivityManager = (ConnectivityManager) this.context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    private final BroadcastReceiver networkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateConnectionStatus();
        }
    };

    @Override
    protected void onActive() {
        super.onActive();
        updateConnectionStatus();
        // Jab LiveData active ho to receiver register karo
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        context.registerReceiver(networkReceiver, filter);
    }

    @Override
    protected void onInactive() {
        super.onInactive();
        try {
            context.unregisterReceiver(networkReceiver);
        } catch (IllegalArgumentException e) {
            // Receiver already unregistered
        }
    }

    private void updateConnectionStatus() {
        String statusMessage;
        Network activeNetwork = connectivityManager.getActiveNetwork();
        if (activeNetwork == null) {
            statusMessage = "Offline: No Internet Connection";
        } else {
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(activeNetwork);
            if (capabilities == null) {
                statusMessage = "Offline: No Internet Connection";
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                statusMessage = "Online: Connected via WiFi";
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                statusMessage = "Online: Connected via Mobile Data";
            } else {
                statusMessage = "Online: Connected";
            }
        }
        // Sirf tab hi update post karein jab status change ho
        if (!statusMessage.equals(getValue())) {
            postValue(statusMessage);
        }
    }
}