package dementiaapp.com.dementiaapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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
import java.util.UUID;


public class StimulusUploadActivity extends Activity {

    private Button stimulusNameButton;
    private Button recordQuestionButton;
    private Button recordAnswerButton;
    private Button addPhotoButton;
    private Button saveButton;
    private Button discardButton;
    private String stimuliMainDir;
    private String newStimulusFolderPath;
    private static final int REQUEST_CODE_SPEECH_RECOGNITION = 100;
    private ImageView stimulusImage;

    final Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Get local directory on Android device that contains subfolders for all stimuli
        stimuliMainDir = getExternalFilesDir(Environment.getDataDirectory().getAbsolutePath()).getAbsolutePath() + "/MemAid/stimuli/";
        String randomID = UUID.randomUUID().toString();

        //Create new subfolder for this stimulus.
        newStimulusFolderPath = stimuliMainDir + randomID;
        File file = new File(newStimulusFolderPath);
        file.mkdirs();


        setContentView(R.layout.activity_stimulus_upload);

        //set up all Buttons
        stimulusNameButton = (Button) findViewById(R.id.stimulus_name_button);
        recordQuestionButton = (Button) findViewById(R.id.record_audio_stimulus_button);
        addPhotoButton = (Button) findViewById(R.id.upload_photo_button);
        recordAnswerButton = (Button) findViewById(R.id.record_audio_answer_button);
        saveButton = (Button) findViewById(R.id.save_button);
        discardButton = (Button) findViewById(R.id.discard_button);
        recordQuestionButton.setEnabled(false);
        recordAnswerButton.setEnabled(false);

        stimulusImage = (ImageView) findViewById(R.id.stimulus1_image);

        addPhotoButton.setEnabled(false);


        saveButton.setEnabled(false);
        //add listeners for each button

        //Record a question
        stimulusNameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String nameFilePath;
                if (newStimulusFolderPath.endsWith("/"))
                    nameFilePath = newStimulusFolderPath + "name" + ".txt";
                else
                    nameFilePath = newStimulusFolderPath + "/name" + ".txt";

                String metricsFilePath;
                if (newStimulusFolderPath.endsWith("/"))
                    metricsFilePath = newStimulusFolderPath + "metrics" + ".txt";
                else
                    metricsFilePath = newStimulusFolderPath + "/metrics" + ".txt";

                Intent i = new Intent(StimulusUploadActivity.this, NameUploadActivity.class);
                i.putExtra("nameFilePath", nameFilePath);
                i.putExtra("metricsFilePath", metricsFilePath);
                startActivity(i);
                stimulusNameButton.setEnabled(false);
                recordQuestionButton.setEnabled(true);
            }
        });

        //Record a question
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

        //Record the expected answer
        recordAnswerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

        //Optionally allow user to upload a photo for this stimulus
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

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        AlertDialog.Builder builder;
                        builder = new AlertDialog.Builder(context);

                        builder.setTitle("Confirm");
                        builder.setMessage("Do you wish to see the image?");

                        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int which) {
                                // Do nothing but close the dialog
                                dialog.dismiss();
                                displayPic();
                            }

                        });

                        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Do nothing
                                dialog.dismiss();
                            }
                        });

                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                }, 1000);
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

    private void displayPic() {
        Bitmap stimulusBitmap;

        if (newStimulusFolderPath.endsWith("/"))
            stimulusBitmap = BitmapFactory.decodeFile(newStimulusFolderPath + "photo.jpg");
        else
            stimulusBitmap = BitmapFactory.decodeFile(newStimulusFolderPath + "/photo.jpg");

        stimulusImage.setImageBitmap(stimulusBitmap);
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
                    if (newStimulusFolderPath.endsWith("/")) {
                        audioFile = new File(newStimulusFolderPath + "answer" + ".amr");
                    } else {
                        audioFile = new File(newStimulusFolderPath + "/answer" + ".amr");
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
                if (newStimulusFolderPath.endsWith("/"))
                    textFile = new File(newStimulusFolderPath + "possibleAnswers" + ".txt");
                else
                    textFile = new File(newStimulusFolderPath + "/possibleAnswers" + ".txt");
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
                } finally {
                    recordQuestionButton.setEnabled(false);
                    recordAnswerButton.setEnabled(false);
                    addPhotoButton.setEnabled(true);
                    saveButton.setEnabled(true);
                }
            }

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
