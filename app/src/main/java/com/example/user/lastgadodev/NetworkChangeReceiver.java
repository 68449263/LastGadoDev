package com.example.user.lastgadodev;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import static com.example.user.lastgadodev.MainActivity.NetworkStateIndicator;

public class NetworkChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {

        int status = NetworkUtil.getConnectivityStatusString(context);

        if ("android.net.conn.CONNECTIVITY_CHANGE".equals(intent.getAction())) {

            switch (status) {

                case NetworkUtil.NETWORK_STATUS_NOT_CONNECTED:
                    NetworkStateIndicator(false, "No internet connection");
                    break;
                case NetworkUtil.NETWORK_STATUS_WIFI:
                    NetworkStateIndicator(true, "wifi connection");
                    break;
                case NetworkUtil.NETWORK_STATUS_MOBILE:
                    NetworkStateIndicator(true, "Connected to Mobile data");
                    break;
                default:
                    break;
            }

        }
    }


}
