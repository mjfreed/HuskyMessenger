package tcss450.uw.edu.messengerapp.model;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import tcss450.uw.edu.messengerapp.R;

public class BootUpReceiver extends BroadcastReceiver {

    private static final String TAG = "BootUpReceiver";

    /*
     * Starts the receiver once a user has been detemined
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive");

        SharedPreferences sharedPreferences =
                context.getSharedPreferences(
                        context.getString(R.string. keys_shared_prefs),
                        Context.MODE_PRIVATE);
        if (sharedPreferences.getBoolean(context.getString(
                R.string. keys_sp_on), false)) {
            Log.d(TAG, "starting service");
        }
        else {
            Log.d(TAG, "Did NOT start the service");
        }

    }

    public BootUpReceiver() {
    }

}
