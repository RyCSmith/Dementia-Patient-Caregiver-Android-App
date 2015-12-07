package dementiaapp.com.dementiaapp;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class SecondBrowser extends Activity {

//
    String question = null;
    String answerPath = null;
    String imagePath = null;
    File stimulusPathGlobal = null;
//    File answer = null;
    private ImageView stimulusImage;
//    private String newStimulusFolderPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second_browser);

        // get the path
        Intent myIntent = getIntent(); // gets the previously created intent
        String path = myIntent.getStringExtra("path");
        final File stimulusPath = new File(path);
        stimulusPathGlobal = stimulusPath;

        Button listenQuestionButton = (Button) findViewById(R.id.listenButton);

        Button listenAnswerButton = (Button) findViewById(R.id.listenAnswerButton);
        Button viewImageButton = (Button) findViewById(R.id.browserImage);
//        Button editButton = (Button) findViewById(R.id.deleteButton1);
        Button deleteButton = (Button) findViewById(R.id.deleteButton1);
        Button updateButton = (Button) findViewById(R.id.updateButton);

        File[] allFiles = stimulusPath.listFiles();
        String mp3 = "";
        String amr = "";
        String picture = "";
//        stimulusImage = (ImageView) findViewById(R.id.viewImageBrowse);

        for (File eachFile : allFiles) {
            if (eachFile.toString().contains(".mp3")) {
                mp3 = eachFile.toString();
                question = mp3;
            }
            else if (eachFile.toString().contains(".amr")) {
                amr = eachFile.toString();
                answerPath = amr;
            }
            else if (eachFile.toString().contains(".jpg")) {
                picture = eachFile.toString();
                imagePath = picture;

            }
        }
//
        listenQuestionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MediaPlayer mPlayer = new MediaPlayer();
                if (question != null) {

                    try {
                        mPlayer.setDataSource(question);
                        mPlayer.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                playAudio(question);
            }
        });

        listenAnswerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playAudio(answerPath);
            }
        });

        //View image
        viewImageButton.setOnClickListener(new View.OnClickListener() {

            boolean showImage = true;

            @Override
            public void onClick(View v) {
                if (imagePath == null) {
                    Toast.makeText(getApplicationContext(),
                            "IMAGE DOES NOT EXIST FOR THIS STIMULUS", Toast.LENGTH_LONG).show();
                } else {

                    Intent intent = new Intent();
                    intent.putExtra("path", imagePath);
                    intent.setClass(SecondBrowser.this, ViewBrowserImage.class);
                    startActivity(intent);
                }
             }
        });

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent();
                if (stimulusPathGlobal != null) intent.putExtra("path", stimulusPathGlobal.toString());


                intent.setClass(SecondBrowser.this, UpdateStimulusActivity.class);
                startActivity(intent);
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (stimulusPathGlobal != null) {
                    DeleteRecursive(stimulusPathGlobal);
                }
                Intent intent = new Intent();
                intent.setClass(SecondBrowser.this, BrowserActivity.class);
                startActivity(intent);
            }
        });
    }
    private void playAudio(String filePath) {

        MediaPlayer mp = new MediaPlayer();

        try {
            mp.setDataSource(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mp.prepareAsync();
        mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
            }
        });

    }
    void DeleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                DeleteRecursive(child);

        fileOrDirectory.delete();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_second_browser, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
