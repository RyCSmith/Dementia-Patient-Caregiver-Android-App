package dementiaapp.com.dementiaapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.io.*;

public class ViewMetricsActivity extends Activity {
    Button backButton;
    TextView timesPlayed;
    TextView avgScore;
    TextView maxScore;
    TextView minScore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_admin);

        backButton = (Button) findViewById(R.id.back_button);
        timesPlayed = (TextView) findViewById(R.id.times_played_num);
        avgScore = (TextView) findViewById(R.id.avg_score_num);
        maxScore = (TextView) findViewById(R.id.max_score_num);
        minScore = (TextView) findViewById(R.id.min_score_num);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    protected void getMetrics() {
        String metricsFilePath = getExternalFilesDir(Environment.getDataDirectory().getAbsolutePath()).getAbsolutePath() + "/MemAid/metrics.txt";

        try {
            FileReader fileReader = new FileReader(metricsFilePath);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String numTimes = bufferedReader.readLine(); //line 1
            String avg = bufferedReader.readLine(); //line 2
            String max = bufferedReader.readLine(); //line 3
            String min = bufferedReader.readLine(); //line 4
            bufferedReader.close();

            timesPlayed.setText(numTimes);
            avgScore.setText(avg);
            maxScore.setText(max);
            minScore.setText(min);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}