package dementiaapp.com.dementiaapp;

import android.app.Activity;
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
import android.app.AlertDialog;

import com.google.android.gms.internal.cl;
import com.orhanobut.logger.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import se.emilsjolander.sprinkles.CursorList;
import se.emilsjolander.sprinkles.Query;

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
    private Score currentScoreObj;

    private int currentStimulusIndex;

    private int currentScore = 0;
    private int numberIncorrect = 0;
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
        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBasicAlertDialog("How to play activity",
                        "Click the microphone button and then name the object in the image shown.");
            }
        });

        if (Query.all(Score.class).get().size() != 0) {
            currentScoreObj = Query.all(Score.class).get().get(0);
            currentScore = currentScoreObj.score;
        } else {
            currentScore = 0;
            numberIncorrect = 0;
            currentScoreObj = new Score();
            currentScoreObj.id = UUID.randomUUID().toString();
            currentScoreObj.score = 0;
            currentScoreObj.incorrect = 0;
            currentScoreObj.save();
            String metricsFilePath = getExternalFilesDir(Environment.getDataDirectory().getAbsolutePath()).getAbsolutePath() + "/MemAid/metrics.txt";
            try {
                PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(metricsFilePath, true)));
                out.println("0.0");
                out.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        updateScore();

        //folder containing all stimulus subfolders
        String stimuliMainDirPath = getExternalFilesDir(Environment.getDataDirectory().getAbsolutePath()).getAbsolutePath() + "/MemAid/stimuli/";

        //We thought about including default audio recordings for correct and incorrect answers, as well as question-specific ones.
        //They were to reside in this folder.
        String defaultsFolderPath = getExternalFilesDir(Environment.getDataDirectory().getAbsolutePath()).getAbsolutePath() + "/defaults/";

        File stimuliMainFolder = new File(stimuliMainDirPath);
        File[] allStimuli = stimuliMainFolder.listFiles();

        stimulusList = new ArrayList<>();

        //create list of stimulus objects based on whether custom images or audio have been included
        for (File stimulusFolder : allStimuli){
            String fullPath = stimulusFolder.getAbsolutePath();
            String stimulusName = fullPath.substring(fullPath.lastIndexOf("/stimuli/") + 8);
            File[] stimulusParts = stimulusFolder.listFiles();
            boolean containsIncorrectResponseAudio = false;
            boolean containsCorrectResponseAudio = false;
            boolean containsImage = false;

            for (File part : stimulusParts){
                if (part.getAbsolutePath().contains("photo")){
                    containsImage = true;
                    photoPath = part.getAbsolutePath();
                }
                if (part.getAbsolutePath().contains("incorrectFeedback")){
                    containsIncorrectResponseAudio = true;
                }
                if (part.getAbsolutePath().contains("correctFeedback")){
                    containsCorrectResponseAudio = true;
                }
            }

            newStimulus stimulus = new newStimulus(fullPath, defaultsFolderPath, containsImage,
                    containsIncorrectResponseAudio, containsCorrectResponseAudio);
            stimulusList.add(stimulus);
        }

        resetStimulus();

        //immediately being playing game on launch
        displayCurrentStimulus();

}

    //Invoked either when user responds to a question or touches the skip button and either displays next stimulus
    //or text indicating there are no more stimuli.
    private void changeStimulus() {
        if (stimulusList == null || currentStimulusIndex >= stimulusList.size() - 1) {
            displayNoMoreStimuli();
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
                        }
                        Toast.makeText(getApplicationContext(),"YOU GOT THE RIGHT ANSWER! CONGRATS!", Toast.LENGTH_LONG).show();
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
                                }
                                Toast.makeText(getApplicationContext(),"YOU GOT THE RIGHT ANSWER! CONGRATS!", Toast.LENGTH_LONG).show();
                                Log.d("VISHWA", "MATCH FOUND THROUGH EDIT DISTANCE:" + response + " with " + possibility + " and edit distance = " + minimumEditDistance);
                                changeStimulus();
                            }
                            else {
                                numberIncorrect++;
                            }
                        }
                    }
                }

                //didn't get the right answer--display message and play audio of correct answer
                if (currentStimulus.hasCustomCorrectResponseAudio()) {
                    MemAidUtils.playAudio(currentStimulus.getOnIncorrectResponseAudio());
                } else{
                    MemAidUtils.playAudio(currentStimulus.getCorrectAnswerAsAudio());
                }
                Toast.makeText(getApplicationContext(),"TOO BAD, YOU DIDN'T GET THAT ONE RIGHT. BETTER LUCK NEXT TIME!", Toast.LENGTH_LONG).show();
            }
        }
    }


    private void updateScore() {
        double avgScore = 0.0;
        if(currentScore != 0 || numberIncorrect != 0) {
            avgScore = currentScore / (currentScore + numberIncorrect);
        }
        String metricsFilePath = getExternalFilesDir(Environment.getDataDirectory().getAbsolutePath()).getAbsolutePath() + "/MemAid/metrics.txt";
        try {
            FileReader fileReader = new FileReader(metricsFilePath);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            StringBuilder sb = new StringBuilder();
            //reads in all lines of file except last into string builder (that way it can write over last line
            // which is most recent line)
            String line = bufferedReader.readLine();
            while(line != null) {
                String line2 = bufferedReader.readLine();
                if(line2 != null) {
                    sb.append(line + "\n");
                }
                line = line2;
            }
            bufferedReader.close();
            sb.append(avgScore + "");
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(metricsFilePath, false)));
            out.println(sb.toString());
            out.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        currentScoreObj.score = currentScore;
        currentScoreObj.incorrect = numberIncorrect;
        currentScoreObj.save();
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
            Bitmap stimulusBitmap = BitmapFactory.decodeFile(currentStimulus.getStimulusImage());

            if (stimulusBitmap == null){
                int i = 0;
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
