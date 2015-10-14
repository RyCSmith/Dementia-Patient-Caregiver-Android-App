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

    private static final int REQUEST_CODE_SPEECH_RECOGNITION = 100;

    private Button recordAudioResponseButton;
    private Button playAudioStimulusButton;

    private boolean hasRecordedAudioStimulus = false;
    private String audioStimulusId;
    private String audioFilePath;

    private ArrayList<String> stimulusPossibilities;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stimulus_upload);

        final Button recordAudioStimulusButton = (Button) findViewById(R.id.record_audio_stimulus_button);
        Button photoWithNameButton = (Button) findViewById(R.id.upload_photo_with_name_button);
        playAudioStimulusButton = (Button) findViewById(R.id.play_audio_stimulus_button);
        playAudioStimulusButton.setEnabled(false);
        recordAudioResponseButton = (Button) findViewById(R.id.record_audio_answer_button);
        recordAudioResponseButton.setEnabled(false);

        photoWithNameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(StimulusUploadActivity.this, UploadPhotoWithNameActivity.class);
                startActivity(intent);
            }
        });

        recordAudioStimulusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioStimulusId = UUID.randomUUID().toString();
                String filePath = getExternalFilesDir(Environment.getDataDirectory().getAbsolutePath()).getAbsolutePath() + "/MemAid/audio/";
                File file = new File(filePath);
                file.mkdirs();
                filePath = filePath + audioStimulusId + ".mp3";
                audioFilePath = filePath;
                recordAudio(filePath, audioStimulusId);
            }
        });

        recordAudioResponseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 20);
                try {
                    startActivityForResult(intent, REQUEST_CODE_SPEECH_RECOGNITION);
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "YOUR DEVICE DOES NOT SUPPORT SPEECH RECOGNITION", Toast.LENGTH_LONG).show();
                }
            }
        });

        playAudioStimulusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                MemAidUtils.playAudio(audioFilePath);
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
                hasRecordedAudioStimulus = true;
                recordAudioResponseButton.setEnabled(true);
                playAudioStimulusButton.setEnabled(true);
            }
        });

        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener(){
            public void onCancel(DialogInterface p1) {
                recorder.stop();
                recorder.release();
                hasRecordedAudioStimulus = true;
                recordAudioResponseButton.setEnabled(true);
                playAudioStimulusButton.setEnabled(true);
            }
        });
        recorder.start();
        mProgressDialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SPEECH_RECOGNITION) {
            if (resultCode == RESULT_OK && data != null) {
                stimulusPossibilities = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                for(String recoginzedName: stimulusPossibilities) {
                    Log.d("VISHWA", "Stimulus recorded:" + recoginzedName);
                }
                Log.d("VISHWA", "================================================================");

                try {
                    JSONObject possibleAnswers = new JSONObject();
                    JSONArray possibilities = new JSONArray(stimulusPossibilities);
                    Log.d("Vishwa", "stimulusPossibilities length = " + stimulusPossibilities.size());
                    Log.d("Vishwa", "JSON possibilities = " + possibilities.join(","));

                    possibleAnswers.put("possible_answers", possibilities);

                    Stimulus stimulus = new Stimulus();
                    stimulus.id = audioStimulusId;
                    stimulus.type = Stimulus.TYPE_AUDIO;
                    stimulus.questionFilepath = audioFilePath;
                    stimulus.possibleAnswers = possibleAnswers.toString();
                    stimulus.createdAt = new Date().getTime();
                    stimulus.save();

                    finish();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    int getLevenshteinDistance(String s, String t) {
        int[] v0 = new int[t.length() + 1];
        int[] v1 = new int[t.length() + 1];

        for (int i = 0; i < v0.length; i++) {
            v0[i] = i;
        }

        for (int i = 0; i < s.length(); i++) {
            v1[0] = i + 1;
            for (int j = 0; j < t.length(); j++) {
                if (i >= s.length() || j >= t.length()) {
                    continue;
                }
                int cost = (s.charAt(i) == t.charAt(j)) ? 0 : 1;
                v1[j + 1] = Math.min(Math.min(v1[j] + 1, v0[j + 1] + 1), v0[j] + cost);
            }

            for (int j = 0; j < v0.length; j++) {
                v0[j] = v1[j];
            }
        }

        return v1[t.length()];
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
