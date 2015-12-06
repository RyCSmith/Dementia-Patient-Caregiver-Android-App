package dementiaapp.com.dementiaapp;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.io.*;
import java.util.ArrayList;
import java.io.File;

public class ViewMetricsActivity extends Activity {
    ArrayList<Double> scores;
    Button backButton;
    TextView timesPlayed;
    TextView avgScore;
    TextView maxScore;
    TextView minScore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_metrics);

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

        String filePath = getExternalFilesDir(Environment.getDataDirectory().getAbsolutePath()).getAbsolutePath() + "/MemAid/metrics.txt";
        File metricsFilePath = new File(filePath);
        if(metricsFilePath.exists()) {
            getMetrics(filePath);
        }
    }

    protected void getMetrics(String filePath) {
        try {
            scores = new ArrayList<Double>();
            FileReader fileReader = new FileReader(filePath);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line = bufferedReader.readLine();
            if(line != null) {
                line = bufferedReader.readLine();
            }
            while(line != null) {
                scores.add(Double.parseDouble(line));
                line = bufferedReader.readLine();
            }
            bufferedReader.close();
            timesPlayed.setText(scores.size() + "");
            avgScore.setText(getAvgScore() + "");
            maxScore.setText(getMaxScore() + "");
            minScore.setText(getMinScore() + "");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected double getAvgScore() {
        double avg = 0.0;
        for(double score : scores) {
            avg += score;
        }
        return avg / scores.size();
    }

    protected double getMaxScore() {
        double max = 0.0;
        for(double score : scores) {
            if(score > max) {
                max = score;
            }
        }
        return max;
    }

    protected double getMinScore() {
        double min = 100.0;
        for(double score : scores) {
            if(score < min) {
                min = score;
            }
        }
        return min;
    }
}