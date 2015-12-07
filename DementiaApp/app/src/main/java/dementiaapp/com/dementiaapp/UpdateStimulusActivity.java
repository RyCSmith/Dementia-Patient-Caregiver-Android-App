package dementiaapp.com.dementiaapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;

public class UpdateStimulusActivity extends Activity {
    private File stimulusPathGlobal;
    private Button recordQuestionButton;
    private Button recordAnswerButton;
    private static final int REQUEST_CODE_SPEECH_RECOGNITION = 100;

    String question = null;
    String answerPath = null;
    String imagePath = null;
    String txtPath = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_stimulus);

        Intent myIntent = getIntent(); // gets the previously created intent
        final String path = myIntent.getStringExtra("path");
        final File stimulusPath = new File(path);
        stimulusPathGlobal = stimulusPath;

        File[] allFiles = stimulusPath.listFiles();
        String mp3 = "";
        String amr = "";
        String picture = "";
        String txt = "";

        final Button questionButton = (Button) findViewById(R.id.changeQuestionButton);

        Button answerButton = (Button) findViewById(R.id.changeAnswerButton);
        Button deleteButton = (Button) findViewById(R.id.deletePictureButton);

        final Button imageButton = (Button) findViewById(R.id.changePictureButton);



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
            else if (eachFile.toString().contains("possibleAnswers.txt")) {
                txt = eachFile.getAbsolutePath();
                txtPath = txt;

            }
        }
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (imagePath != null) {
                    File file = new File(imagePath);

                    try {
                        file.getCanonicalFile().delete();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                Intent intent = new Intent();
                intent.setClass(UpdateStimulusActivity.this, UploadPhotoWithNameActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("newStimulusFolder", path);
                intent.putExtras(bundle);
                startActivity(intent);

            }
        });
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (imagePath != null) {
                    File file = new File(imagePath);

                    try {
                        file.getCanonicalFile().delete();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                Toast.makeText(getApplicationContext(),
                        "DELETED PHOTO SUCCESSFULLY", Toast.LENGTH_LONG)
                        .show();

            }
        });
        questionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String newFilePath;
                if (question.endsWith("/"))
                    newFilePath = path + "question" + ".mp3";
                else
                    newFilePath = path + "/question" + ".mp3";
                recordAudio(newFilePath, "question");
            }
        });
        answerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (imagePath != null && txtPath != null) {
                    File file = new File(txtPath);
                    File file2 = new File(answerPath);

                    try {
                        file.getCanonicalFile().delete();
                        file.getCanonicalFile().delete();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 20);
                intent.putExtra("android.speech.extra.GET_AUDIO_FORMAT", "audio/AMR");
                intent.putExtra("android.speech.extra.GET_AUDIO", true);

                //Invoke speech recognition functionality
                try {
                    startActivityForResult(intent, REQUEST_CODE_SPEECH_RECOGNITION);
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "YOUR DEVICE DOES NOT SUPPORT SPEECH RECOGNITION", Toast.LENGTH_LONG).show();
                }

            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_update_stimulus, menu);
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
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Use speech recognition to generate list of possible acceptable response strings based on audio recording
        if (requestCode == REQUEST_CODE_SPEECH_RECOGNITION)
            if (resultCode == RESULT_OK && data != null) {

                //Get list of strings generated from audio by speech recognition.
                ArrayList<String> stimulusPossibilities = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);


                Bundle bundle = data.getExtras();

                // Speech recognition didn't seem to support recording and saving audio to a file, then
                // using speech recognitio on it. Intsead, we can retrieve the audio we sent to the speech recognition
                // from the data object.
                Uri audioUri = data.getData();
                ContentResolver contentResolver = getContentResolver();

                //Read data from submitted audio file into a file we can save locally.
                try {
                    InputStream filestream = contentResolver.openInputStream(audioUri);
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    int byteRead;
                    byte[] dataRead = new byte[1024];
                    while ((byteRead = filestream.read(dataRead)) != -1){
                        out.write(dataRead, 0, byteRead);
                    }
                    filestream.close();


                    File audioFile;
                    if (stimulusPathGlobal.toString().endsWith("/")) {
                        audioFile = new File(stimulusPathGlobal.toString() + "answer" + ".amr");
                    } else {
                        audioFile = new File(stimulusPathGlobal.toString() + "/answer" + ".amr");
                    }
                    audioFile.createNewFile();


                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(audioFile));
                    out.writeTo(bos);
                    bos.close();
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //Write strings from speech recognition to a file and save it locally.
                File textFile;
                if (stimulusPathGlobal.toString().endsWith("/"))
                    textFile = new File(stimulusPathGlobal.toString() + "possibleAnswers" + ".txt");
                else
                    textFile = new File(stimulusPathGlobal.toString() + "/possibleAnswers" + ".txt");
                try{
                    textFile.createNewFile();
                } catch(IOException e){
                    e.printStackTrace();
                }

                PrintStream out = null;

                try {
                    out = new PrintStream(new FileOutputStream(textFile));
                    for (String possibleResponse : stimulusPossibilities) {
                        out.println(possibleResponse);
                    }
                    if (out.checkError()) {
                    }

                    if (out != null) {
                        out.close();
                    }

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }

    }


    //Invoked to record audio files and save them locally when speech recognition not required.
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

        final ProgressDialog mProgressDialog = new ProgressDialog(UpdateStimulusActivity.this);
        mProgressDialog.setTitle("Recording Audio");
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setButton("Stop recording", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                mProgressDialog.dismiss();
                recorder.stop();
                recorder.release();
            }
        });

        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface p1) {
                recorder.stop();
                recorder.release();
            }
        });
        recorder.start();
        mProgressDialog.show();
    }
}
