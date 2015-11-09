package dementiaapp.com.dementiaapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

/**
 * Created by vishwa on 4/10/15.
 */
public class BootCompletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
       /*This had stuff to sound alarm if patient left safe zone.
       * I left the stub in case we want to use it for something.*/
    }
}
