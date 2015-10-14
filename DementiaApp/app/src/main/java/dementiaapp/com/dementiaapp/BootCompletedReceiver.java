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
        boolean hasUserApprovedLocationTracking = context.getSharedPreferences("MEMAID", Context.MODE_PRIVATE).getBoolean(Constants.LOCATION_TRACKING_APPROVED_KEY, false);
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED") && hasUserApprovedLocationTracking) {
            /* Setting the alarm here */
            Intent alarmIntent = new Intent(context, LocationBeaconService.class);
            PendingIntent pendingIntent = PendingIntent.getService(context, 0, alarmIntent, 0);
            AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), Constants.LOCATION_BEACON_INTERVAL, pendingIntent);

            SharedPreferences.Editor sharedPreferences = context.getSharedPreferences("MEMAID", Context.MODE_PRIVATE).edit();
            sharedPreferences.putBoolean(Constants.LOCATION_BEACON_ACTIVATED_KEY, true);
            sharedPreferences.commit();

            Toast.makeText(context, "Alarm Set for Dementia Location Tracking", Toast.LENGTH_SHORT).show();

        }
    }
}
