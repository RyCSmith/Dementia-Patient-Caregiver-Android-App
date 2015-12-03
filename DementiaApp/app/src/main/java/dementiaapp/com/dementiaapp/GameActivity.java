package dementiaapp.com.dementiaapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by vishwa on 4/1/15.
 */
public class GameActivity extends Activity {

    private static final int REQUEST_CODE_SPEECH_RECOGNITION = 100;

    private ImageView stimulusImage;
    private TextView stimulusAnswer;
    private Button skipButton;
    private ImageView micButton;
    private TextView score;
    private Button helpButton;

    private List<newStimulus> stimulusList;
    private newStimulus currentStimulus;

    private int currentStimulusIndex;

    private String stimuliMainDirPath;

    private int currentScore = 0;
    String photoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_game);

        stimulusAnswer = (TextView) findViewById(R.id.stimulus_answer);
        skipButton = (Button) findViewById(R.id.skip_button);
        stimulusImage = (ImageView) findViewById(R.id.stimulus_image);
        micButton = (ImageView) findViewById(R.id.mic_button);
        score = (TextView) findViewById(R.id.score);
        helpButton = (Button) findViewById(R.id.help_button);

        //stimuliMainDir = getExternalFilesDir(Environment.getDataDirectory().getAbsolutePath()).getAbsolutePath() + "/MemAid/stimuli/";



        currentScore = 0;

        //folder containing all stimulus subfolders
        stimuliMainDirPath = getExternalFilesDir(Environment.getDataDirectory().getAbsolutePath()).getAbsolutePath() + "/MemAid/stimuli/";

        //We thought about including default audio recordings for correct and incorrect answers, as well as question-specific ones.
        //They were to reside in this folder.
        String defaultsFolderPath = getExternalFilesDir(Environment.getDataDirectory().getAbsolutePath()).getAbsolutePath() + "/defaults/";

        File stimuliMainFolder = new File(stimuliMainDirPath);
        File[] allStimuli = stimuliMainFolder.listFiles();

        stimulusList = new ArrayList<>();
        if (allStimuli.length != 0) {
            //create list of stimulus objects based on whether custom images or audio have been included
            for (File stimulusFolder : allStimuli) {
                if (stimulusFolder.isDirectory()) {
                    String fullPath = stimulusFolder.getAbsolutePath();
                    String stimulusName = fullPath.substring(fullPath.lastIndexOf("/stimuli/") + 8);
                    File[] stimulusParts = stimulusFolder.listFiles();
                    boolean containsIncorrectResponseAudio = false;
                    boolean containsCorrectResponseAudio = false;
                    boolean containsImage = false;

                    for (File part : stimulusParts) {
                        if (part.getAbsolutePath().contains("photo")) {
                            containsImage = true;
                            photoPath = part.getAbsolutePath();
                        }
                        if (part.getAbsolutePath().contains("incorrectFeedback")) {
                            containsIncorrectResponseAudio = true;
                        }
                        if (part.getAbsolutePath().contains("correctFeedback")) {
                            containsCorrectResponseAudio = true;
                        }
                    }

                    newStimulus stimulus = new newStimulus(fullPath, defaultsFolderPath, containsImage,
                            containsIncorrectResponseAudio, containsCorrectResponseAudio);
                    stimulusList.add(stimulus);

                    resetStimulus();

                    //immediately being playing game on launch
                    displayCurrentStimulus();
                }


            }
        }
}

    //Invoked either when user responds to a question or touches the skip button and either displays next stimulus
    //or text indicating there are no more stimuli.
    private void changeStimulus() {
        if (stimulusList == null ) {
            displayNoMoreStimuli();
        }
        if (currentStimulusIndex >= stimulusList.size() - 1)  {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setTitle("Confirm");
            builder.setMessage("Do you wish to play again?");

            builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    // Do nothing but close the dialog

                    dialog.dismiss();
                    currentStimulusIndex = 0;
                }

            });

            builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Do nothing
                    dialog.dismiss();
                    displayNoMoreStimuli();
                }
            });

            AlertDialog alert = builder.create();
            alert.show();


        } else {
            currentStimulusIndex++;
            displayCurrentStimulus();
        }
    }




    //Invoked after user has responded to a stimulus. Currently only supports responses via microphone
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SPEECH_RECOGNITION) {
            if (resultCode == RESULT_OK && data != null) {

                //List of strings that voice recognition matched to user's recorded answer.
                ArrayList<String> responses = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                //List of strings that voice recognition matched to admin's recorded correct answer.
                ArrayList<String> possibleAnswers = currentStimulus.getPossibleCorrectAnswers();

                boolean matchFound = false;
                for(String response: responses) {
                    if (possibleAnswers.contains(response)) {
                        matchFound = true;
                        currentScore++;
                        updateScore();

                        //Tell user the answer was correct, and move onto next stimulus.
                        if (currentStimulus.hasCustomCorrectResponseAudio()) {
                            MemAidUtils.playAudio(currentStimulus.getOnCorrectResponseAudio());
                        } else {
                            MemAidUtils.playAudio(stimuliMainDirPath+"correctFB.mp3");
                        }
                        //Toast.makeText(getApplicationContext(),"YOU GOT THE RIGHT ANSWER! CONGRATS!", Toast.LENGTH_LONG).show();
                        changeStimulus();
                    }
                }

                if (!matchFound) {
                    for(String response: responses) {
                        for(String possibility: possibleAnswers) {
                            //Exact match not found--check whether it was close and might be an issue of bad speech to text conversion
                            int minimumEditDistance = getLevenshteinDistance(response, possibility);
                            if (minimumEditDistance <= 3) {
                                currentScore++;
                                updateScore();
                                if (currentStimulus.hasCustomCorrectResponseAudio()) {
                                    MemAidUtils.playAudio(currentStimulus.getOnCorrectResponseAudio());
                                } else {
                                    MemAidUtils.playAudio(stimuliMainDirPath+"correctFB.mp3");
                                }

                                //Toast.makeText(getApplicationContext(),"YOU GOT THE RIGHT ANSWER! CONGRATS!", Toast.LENGTH_LONG).show();
                                Log.d("VISHWA", "MATCH FOUND THROUGH EDIT DISTANCE:" + response + " with " + possibility + " and edit distance = " + minimumEditDistance);
                                changeStimulus();
                            }
                        }
                    }
                }

                //didn't get the right answer--display message and play audio of correct answer
                if (currentStimulus.hasCustomCorrectResponseAudio()) {
                    MemAidUtils.playAudio(currentStimulus.getOnIncorrectResponseAudio());
                } else{
                    MemAidUtils.playAudio(stimuliMainDirPath+"incorrectFB.mp3");
                    //MemAidUtils.playAudio(currentStimulus.getCorrectAnswerAsAudio());
                }
                //Toast.makeText(getApplicationContext(),"TOO BAD, YOU DIDN'T GET THAT ONE RIGHT. BETTER LUCK NEXT TIME!", Toast.LENGTH_LONG).show();
            }
        }
    }


    private void updateScore() {
        score.setText(currentScore + "");
    }

    //Checks edit distance between two strings, i.e., how many letters would need to be changed to convert one string to another
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

    private void showBasicAlertDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(GameActivity.this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    //Play audio prompt and display stimulus image, if applicable
    private void displayCurrentStimulus() {
        currentStimulus = stimulusList.get(currentStimulusIndex);

        if(currentStimulus.hasCustomImage()) {
            //Bitmap stimulusBitmap = BitmapFactory.decodeFile("/storage/sdcard1/DCIM/Camera/IMG_20151113_173948.jpg");
            Bitmap stimulusBitmap = BitmapFactory.decodeFile(currentStimulus.getStimulusImage());

            if (stimulusBitmap == null){
                try {
                    //stimulusBitmap = BitmapFactory.decodeFile("file:///android_asset/questionMark.png");
                    InputStream bitmap=getAssets().open("questionMark.png");
                    stimulusBitmap=BitmapFactory.decodeStream(bitmap);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            stimulusImage.setImageBitmap(stimulusBitmap);
        }

        //play audio prompt automatically
        MemAidUtils.playAudio(currentStimulus.getAudioPrompt());

        //Allow user to skip to next stimulus
        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeStimulus();
            }
        });

        //help
        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //showBasicAlertDialog("How to play activity",
                //      "Click the microphone button and then name the object in the image shown.");
                MemAidUtils.playAudio(currentStimulus.getHelpAudio());
            }
        });

        //Allow user to record response to stimulus
        micButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 20);

                //Trigger speech recognition to validate user's response.
                try {
                    startActivityForResult(intent, REQUEST_CODE_SPEECH_RECOGNITION);
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "YOUR DEVICE DOES NOT SUPPORT SPEECH RECOGNITION", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    //Function to be invoked when user has gone through all stimuli. Currently only displays a text pop-up, but should probably do more.
    private void displayNoMoreStimuli() {
        Toast.makeText(getApplicationContext(), "SORRY, THERE ARE NO MORE MEMORY TESTS!", Toast.LENGTH_LONG).show();
        skipButton.setEnabled(false);
        micButton.setEnabled(false);
    }

    public void resetStimulus(){
        if (!stimulusList.isEmpty()) {
            currentStimulusIndex = 0;
        }
    }

}
