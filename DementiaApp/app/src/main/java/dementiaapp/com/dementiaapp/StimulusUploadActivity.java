package dementiaapp.com.dementiaapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;


public class StimulusUploadActivity extends Activity {

    private Button recordQuestionButton;
    private Button recordAnswerButton;
    private Button addPhotoButton;
    private Button recordCorrectFeedbackButton;
    private Button recordIncorrectFeedbackButton;
    private Button saveButton;
    private Button discardButton;
    private String stimuliMainDir;
    private String newStimulusFolderPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        stimuliMainDir = getExternalFilesDir(Environment.getDataDirectory().getAbsolutePath()).getAbsolutePath() + "/MemAid/stimuli/";
        String randomID = UUID.randomUUID().toString();
        newStimulusFolderPath = stimuliMainDir + randomID;
        File file = new File(newStimulusFolderPath);
        file.mkdirs();


        setContentView(R.layout.activity_stimulus_upload);

        //set up all Buttons
        recordQuestionButton = (Button) findViewById(R.id.record_audio_stimulus_button);
        addPhotoButton = (Button) findViewById(R.id.upload_photo_button);
        recordAnswerButton = (Button) findViewById(R.id.record_audio_answer_button);
        recordCorrectFeedbackButton = (Button) findViewById(R.id.record_correct_audio_answer_button);
        recordIncorrectFeedbackButton = (Button) findViewById(R.id.record_incorrect_audio_answer_button);
        saveButton = (Button) findViewById(R.id.save_button);
        discardButton = (Button) findViewById(R.id.discard_button);
        recordAnswerButton.setEnabled(false);
        recordCorrectFeedbackButton.setEnabled(false);
        recordIncorrectFeedbackButton.setEnabled(false);
        addPhotoButton.setEnabled(false);
        saveButton.setEnabled(false);

        //add listeners for each button
        recordQuestionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newFilePath;
                if (newStimulusFolderPath.endsWith("/"))
                    newFilePath = newStimulusFolderPath + "question" + ".mp3";
                else
                    newFilePath = newStimulusFolderPath + "/question" + ".mp3";
                recordAudio(newFilePath, "question");
                recordQuestionButton.setEnabled(false);
                recordAnswerButton.setEnabled(true);
            }
        });

        recordAnswerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newFilePath;
                if (newStimulusFolderPath.endsWith("/"))
                    newFilePath = newStimulusFolderPath + "answer" + ".mp3";
                else
                    newFilePath = newStimulusFolderPath + "/answer" + ".mp3";
                recordAudio(newFilePath, "answer");
                recordQuestionButton.setEnabled(false);
                recordAnswerButton.setEnabled(false);
                recordCorrectFeedbackButton.setEnabled(true);
                recordIncorrectFeedbackButton.setEnabled(true);
                addPhotoButton.setEnabled(true);
                saveButton.setEnabled(true);
            }
        });

        addPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(StimulusUploadActivity.this, UploadPhotoWithNameActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("newStimulusFolder", newStimulusFolderPath);
                intent.putExtras(bundle);
                startActivity(intent);
                addPhotoButton.setEnabled(false);
            }
        });

        recordCorrectFeedbackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newFilePath;
                if (newStimulusFolderPath.endsWith("/"))
                    newFilePath = newStimulusFolderPath + "correctFeedback" + ".mp3";
                else
                    newFilePath = newStimulusFolderPath + "/correctFeedback" + ".mp3";
                recordAudio(newFilePath, "correctFeedback");
                recordCorrectFeedbackButton.setEnabled(false);
            }
        });

        recordIncorrectFeedbackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newFilePath;
                if (newStimulusFolderPath.endsWith("/"))
                    newFilePath = newStimulusFolderPath + "incorrectFeedback" + ".mp3";
                else
                    newFilePath = newStimulusFolderPath + "/incorrectFeedback" + ".mp3";
                recordAudio(newFilePath, "incorrectFeedback");
                recordIncorrectFeedbackButton.setEnabled(false);
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        discardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(File file: new File(newStimulusFolderPath).listFiles())
                    file.delete();
                new File(newStimulusFolderPath).delete();
                finish();
            }
        });
    }

    public void recordAudio(String filePath, String fileName) {
        final MediaRecorder recorder = new MediaRecorder();
        ContentValues values = new ContentValues(3);
        values.put(MediaStore.MediaColumns.TITLE, fileName);
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        recorder.setOutputFile(filePath);
        try {
            recorder.prepare();
        } catch (Exception e){
            e.printStackTrace();
        }

        final ProgressDialog mProgressDialog = new ProgressDialog(StimulusUploadActivity.this);
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_stimulus_upload, menu);
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
