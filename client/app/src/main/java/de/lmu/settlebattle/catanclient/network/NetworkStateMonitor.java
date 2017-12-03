package de.lmu.settlebattle.catanclient.network;

import static de.lmu.settlebattle.catanclient.utils.Constants.ACTION_NETWORK_STATE_CHANGED;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;


public class NetworkStateMonitor extends BroadcastReceiver {


    private static final String TAG = NetworkStateMonitor.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Network connectivity change");

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        boolean networkIsOn = activeNetworkInfo != null && activeNetworkInfo.isConnected();

        Intent broadcastIntent = new Intent(ACTION_NETWORK_STATE_CHANGED);
        broadcastIntent.putExtra(ACTION_NETWORK_STATE_CHANGED, networkIsOn);
        LocalBroadcastManager.getInstance(context).sendBroadcast(broadcastIntent);
    }
}
