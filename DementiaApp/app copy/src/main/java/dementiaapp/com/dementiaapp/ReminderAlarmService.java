package dementiaapp.com.dementiaapp;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.orhanobut.logger.Logger;

/**
 * Created by Jinesh Desai on 4/23/2015.
 */
public class ReminderAlarmService extends IntentService {

    protected int mNotificationId;

    public ReminderAlarmService() {

        super("ReminderAlarmService");

        mNotificationId = 1;

    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Logger.e("Entered onhandleintent");
        String title = intent.getStringExtra("title");
        Logger.e("Got title = "+title);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                                                    .setSmallIcon(R.drawable.ic_launcher)
                                                    .setContentTitle("MemAid Notification")
                                                    .setContentText(title);
        NotificationManager mMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mMgr.notify(mNotificationId, mBuilder.build());
        Logger.e("Issued notification");
        mNotificationId++;
    }
}
