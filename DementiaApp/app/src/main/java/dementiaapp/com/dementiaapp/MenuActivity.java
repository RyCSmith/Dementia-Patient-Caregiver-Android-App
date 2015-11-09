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


public class MenuActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        Button playGameButton = (Button) findViewById(R.id.play_game_button);
        Button uploadStimulusButton = (Button) findViewById(R.id.upload_stimulus_button);
        Button createRemindersButton = (Button) findViewById(R.id.create_reminders_button);
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


        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                SharedPreferences.Editor sharedPreferences = getApplication().getSharedPreferences("MEMAID", Context.MODE_PRIVATE).edit();
                sharedPreferences.putBoolean("LOGGED_IN", false);
                sharedPreferences.putString("USER_TYPE", "");
                sharedPreferences.putString("PATIENT_NAME", "");
                sharedPreferences.commit();

                finish();
            }
        });

        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("MEMAID", Context.MODE_PRIVATE);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (Constants.IS_DEBUG_VERSION) {
            getMenuInflater().inflate(R.menu.menu_menu, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }
}
