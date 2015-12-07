package dementiaapp.com.dementiaapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;

public class MenuActivityAdmin extends Activity {

    private String stimuliMainDir;

    private String correct;
    private String incorrect;
    private String help;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_admin);

        Button browserButton = (Button) findViewById(R.id.browserButton);
        Button uploadStimulusButton = (Button) findViewById(R.id.upload_stimulus_button);
        Button viewMetricsButton = (Button) findViewById(R.id.view_metrics_button);
        Button logoutButton = (Button) findViewById(R.id.logout_button);

        uploadStimulusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                stimuliMainDir = getExternalFilesDir(Environment.getDataDirectory().getAbsolutePath()).getAbsolutePath() + "/MemAid/stimuli/";

                correct = stimuliMainDir + "correctFB.mp3";
                incorrect = stimuliMainDir + "incorrectFB.mp3";
                help = stimuliMainDir + "help.mp3";
                File f = new File(correct);
                File f2 = new File(incorrect);
                File f3 = new File(help);

                //intent.setClass(MenuActivityAdmin.this, StimulusUploadActivity.class);

                if (f.exists() && f2.exists() && f3.exists()) {
                    intent.setClass(MenuActivityAdmin.this, StimulusUploadActivity.class);
                } else {
                    intent.setClass(MenuActivityAdmin.this, FeedbackUploadActivity.class);
                }
                startActivity(intent);
            }
        });

        browserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String stimDir = getExternalFilesDir(Environment.getDataDirectory().getAbsolutePath()).getAbsolutePath() + "/MemAid/stimuli/";
                File file = new File(stimDir);
                if(!file.exists()) {
                    Toast.makeText(getApplicationContext(),
                            "NO STIMULI EXIST TO VIEW YET", Toast.LENGTH_LONG).show();
                }
                else {
                    Intent intent = new Intent();
                    intent.setClass(MenuActivityAdmin.this, BrowserActivity.class);
                    startActivity(intent);
                }
            }
        });

        viewMetricsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MenuActivityAdmin.this, ViewMetricsActivity.class);
                startActivity(intent);
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Intent intent = new Intent();
                intent.setClass(MenuActivityAdmin.this, LoginActivity.class);
                startActivity(intent);*/
                finish();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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