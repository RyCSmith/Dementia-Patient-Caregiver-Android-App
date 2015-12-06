package dementiaapp.com.dementiaapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.internal.cl;
import com.orhanobut.logger.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;


public class FeedbackUploadActivity extends Activity {


    private Button recordCorrectFeedbackButton;
    private Button recordIncorrectFeedbackButton;
    private Button recordHelpButton;
    private Button saveButton;

    private String stimuliMainDir;

    private String newStimulusFolderPath;
    private static final int REQUEST_CODE_SPEECH_RECOGNITION = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Get local directory on Android device that contains subfolders for all stimuli
        stimuliMainDir = getExternalFilesDir(Environment.getDataDirectory().getAbsolutePath()).getAbsolutePath() + "/MemAid/stimuli/";

        //Create new subfolder for this stimulus.
        newStimulusFolderPath = stimuliMainDir;


        setContentView(R.layout.activity_feedback_upload);

        //set up all Buttons
        recordCorrectFeedbackButton = (Button) findViewById(R.id.record_right_feedback_button);
        recordIncorrectFeedbackButton = (Button) findViewById(R.id.record_wrong_feedback_button);
        recordHelpButton = (Button) findViewById(R.id.record_audio_help_button);
        saveButton = (Button) findViewById(R.id.save1_button);
        recordCorrectFeedbackButton.setEnabled(true);
        recordIncorrectFeedbackButton.setEnabled(false);
        recordHelpButton.setEnabled(false);
        //addPhotoButton.setEnabled(false);



        saveButton.setEnabled(false);


        //add listeners for each button

        //Optionally allow user to record an audio message to play if game-player answers correctly.
        recordCorrectFeedbackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newFilePath;
                newFilePath = newStimulusFolderPath + "correctFB" + ".mp3";
                recordAudio(newFilePath, "correctFB");
                recordCorrectFeedbackButton.setEnabled(false);
                recordIncorrectFeedbackButton.setEnabled(true);
            }
        });

        //Optionally allow user to record an audio message to play if game-player answers incorrectly.
        recordIncorrectFeedbackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newFilePath;
                newFilePath = newStimulusFolderPath + "incorrectFB" + ".mp3";
                recordAudio(newFilePath, "incorrectFB");
                recordIncorrectFeedbackButton.setEnabled(false);
                recordHelpButton.setEnabled(true);
            }
        });

        recordHelpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newFilePath;
                newFilePath = newStimulusFolderPath + "help" + ".mp3";
                recordAudio(newFilePath, "help");
                recordHelpButton.setEnabled(false);
                saveButton.setEnabled(true);
            }
        });


        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Intent intent = new Intent();
                intent.setClass(FeedbackUploadActivity.this, StimulusUploadActivity.class);
                startActivity(intent);*/
                finish();
            }
        });

    }

    //Invoked to record audio files and save them locally when speech recognition not required.
    public void recordAudio(String filePath, String fileName) {
        final MediaRecorder recorder = new MediaRecorder();
        ContentValues values = new ContentValues(3);
        values.put(MediaStore.MediaColumns.TITLE, fileName);
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);

        File file = new File(newStimulusFolderPath);

        if (!((file).exists()) ) {

            file.mkdirs();

        }

        File file1 = new File(newStimulusFolderPath, fileName + ".mp3");
        try {
            file1.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        recorder.setOutputFile(filePath);
        try {

            recorder.prepare();
        } catch (Exception e){
            e.printStackTrace();
        }

        final ProgressDialog mProgressDialog = new ProgressDialog(FeedbackUploadActivity.this);
        mProgressDialog.setTitle("Recording Audio");
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setButton("Stop recording", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                mProgressDialog.dismiss();
                recorder.stop();
                recorder.release();
            }
        });

        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener(){
            public void onCancel(DialogInterface p1) {
                recorder.stop();
                recorder.release();
            }
        });
        recorder.start();
        mProgressDialog.show();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_feedback_upload, menu);
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
