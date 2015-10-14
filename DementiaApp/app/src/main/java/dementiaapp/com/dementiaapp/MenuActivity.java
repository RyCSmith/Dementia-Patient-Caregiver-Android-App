package dementiaapp.com.dementiaapp;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.internal.cl;
import com.orhanobut.logger.Logger;
import com.parse.ParseUser;


public class MenuActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        Button playGameButton = (Button) findViewById(R.id.play_game_button);
        Button uploadStimulusButton = (Button) findViewById(R.id.upload_stimulus_button);
        Button createRemindersButton = (Button) findViewById(R.id.create_reminders_button);
        Button trackLocationButton = (Button) findViewById(R.id.track_location_button);
        Button addSafezoneButton = (Button) findViewById(R.id.add_safezone_button);
        Button logoutButton = (Button) findViewById(R.id.logout_button);

        playGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MenuActivity.this, GameActivity.class);
                startActivity(intent);
            }
        });

        uploadStimulusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MenuActivity.this, StimulusUploadActivity.class);
                startActivity(intent);
            }
        });

        createRemindersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MenuActivity.this, RemindersActivity.class);
                startActivity(intent);
            }
        });

        trackLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MenuActivity.this, LocationActivity.class);
                startActivity(intent);
            }
        });

        addSafezoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MenuActivity.this, SafezoneCreationActivity.class);
                startActivity(intent);
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseUser.getCurrentUser().logOut();

                SharedPreferences.Editor sharedPreferences = getApplication().getSharedPreferences("MEMAID", Context.MODE_PRIVATE).edit();
                sharedPreferences.putBoolean("LOGGED_IN", false);
                sharedPreferences.putString("USER_TYPE", "");
                sharedPreferences.putString("PATIENT_NAME", "");
                sharedPreferences.commit();

                finish();
            }
        });

        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("MEMAID", Context.MODE_PRIVATE);

        // THIS IS DIFFERENT FROM THE USER ALLOWING US TO USE LOCATION TRACKING!!!!!
        String userType = getSharedPreferences("MEMAID", Context.MODE_PRIVATE).getString("USER_TYPE", Constants.USER_TYPE_PATIENT);

        if (MemAidUtils.hasUserAllowedLocationTracking(getApplicationContext())
                && !MemAidUtils.hasLocationBeaconBeenActivated(getApplicationContext())
                && userType.equals(Constants.USER_TYPE_PATIENT)) {
            Intent alarmIntent = new Intent(getApplicationContext(), LocationBeaconService.class);
            PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 0, alarmIntent, 0);
            AlarmManager manager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
            manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), Constants.LOCATION_BEACON_INTERVAL, pendingIntent);

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(Constants.LOCATION_BEACON_ACTIVATED_KEY, true);
            editor.commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (Constants.IS_DEBUG_VERSION) {
            getMenuInflater().inflate(R.menu.menu_menu, menu);
            String locationTrackingSetting =
                    MemAidUtils.hasDebugLocationSettingTurnedOn(getApplicationContext()) ? "Turn Location Tracking Off" : "Turn Location Tracking On";
            menu.getItem(0).setTitle(locationTrackingSetting);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_location_tracking) {
            boolean newDebugLocationTrackingSetting = !MemAidUtils.hasDebugLocationSettingTurnedOn(getApplicationContext());
            getApplicationContext().getSharedPreferences("MEMAID", Context.MODE_PRIVATE).edit().putBoolean(Constants.LOCATION_TRACKING_SETTING_KEY, newDebugLocationTrackingSetting).commit();
            String locationTrackingSetting =
                    MemAidUtils.hasDebugLocationSettingTurnedOn(getApplicationContext()) ? "Turn Location Tracking Off" : "Turn Location Tracking On";
            item.setTitle(locationTrackingSetting);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
