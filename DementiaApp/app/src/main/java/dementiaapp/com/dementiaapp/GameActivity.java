package dementiaapp.com.dementiaapp;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
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

    private List<Stimulus> stimulusList;
    private Stimulus currentStimulus;
    private Score currentScoreObj;

    private int stimulusOffset = 0;

    private int currentScore = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_game);

        stimuhttp://bit.ly/mcit-ta-spring2016lusImage = (ImageView) findViewById(R.id.stimulus_image);
        stimulusAnswer = (TextView) findViewById(R.id.stimulus_answer);
        skipButton = (Button) findViewById(R.id.skip_button);
        micButton = (ImageView) findViewById(R.id.mic_button);
        score = (TextView) findViewById(R.id.score);
        helpButton = (Button) findViewById(R.id.help_button);
        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBasicAlertDialog("How to play activity",
                        "Click the mic to record your voice TBD");
            }
        });

        if (Query.all(Score.class).get().size() != 0) {
            currentScoreObj = Query.all(Score.class).get().get(0);
            currentScore = currentScoreObj.score;
        } else {
            currentScore = 0;
            currentScoreObj = new Score();
            currentScoreObj.id = UUID.randomUUID().toString();
            currentScoreObj.score = 0;
            currentScoreObj.save();
        }
        updateScore();

        stimulusList = Query.all(Stimulus.class).get().asList();

        if (!stimulusList.isEmpty()) {
            currentStimulus = stimulusList.get(0);

            switch (currentStimulus.type) {
                case Stimulus.TYPE_AUDIO:
                    // DO SOMETHING FOR AUDIO STIMULI HERE
                    stimulusImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_play_audio));
                    stimulusImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            MemAidUtils.playAudio(currentStimulus.questionFilepath);
                        }
                    });
                    break;

                case Stimulus.TYPE_IMAGE:
                    Bitmap stimulusBitmap = BitmapFactory.decodeFile(currentStimulus.questionFilepath);
                    stimulusImage.setImageBitmap(stimulusBitmap);
                    stimulusImage.setOnClickListener(null);
                    stimulusAnswer.setText(currentStimulus.possibleAnswers);
                    break;
            }

            skipButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    changeStimulus();
                }
            });

            micButton.setOnClickListener(new View.OnClickListener() {
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
        }
    }

    private void changeStimulus() {
        stimulusOffset++;
        if (stimulusList != null && stimulusOffset < stimulusList.size()) {
            currentStimulus = stimulusList.get(stimulusOffset);

            if (currentStimulus.type == Stimulus.TYPE_IMAGE) {
                Bitmap stimulusBitmap = BitmapFactory.decodeFile(currentStimulus.questionFilepath);
                stimulusImage.setImageBitmap(stimulusBitmap);
            } else {
                stimulusImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_play_audio));
                stimulusImage.setOnClickListener(null);
                stimulusImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MemAidUtils.playAudio(currentStimulus.questionFilepath);
                    }
                });
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SPEECH_RECOGNITION) {
            if (resultCode == RESULT_OK && data != null) {
                ArrayList<String> responses = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                Set<String> setOfPossibilities = new HashSet<String>();
                try {
                    JSONArray arr = (new JSONObject(currentStimulus.possibleAnswers)).optJSONArray(Constants.STIMULUS_RESPONSE_JSON_KEY);
                    for (int i = 0; i < arr.length(); i++) {
                        setOfPossibilities.add(arr.getString(i));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                boolean matchFound = false;
                for(String response: responses) {
                    if (setOfPossibilities.contains(response)) {
                        matchFound = true;
                        currentScore++;
                        updateScore();
                        changeStimulus();
                        Toast.makeText(getApplicationContext(),"YOU GOT THE RIGHT ANSWER! CONGRATS!", Toast.LENGTH_LONG).show();
                        break;
                    }
                }

                if (!matchFound) {
                    for(String response: responses) {
                        for(String possibility: setOfPossibilities) {
                            int minimumEditDistance = getLevenshteinDistance(response, possibility);
                            if (minimumEditDistance <= 3) {
                                currentScore++;
                                changeStimulus();
                                Log.d("VISHWA", "MATCH FOUND THROUGH EDIT DISTANCE:"+response+" with "+possibility+" and edit distance = "+minimumEditDistance);
                                Toast.makeText(getApplicationContext(),"YOU GOT THE RIGHT ANSWER! CONGRATS!", Toast.LENGTH_LONG).show();
                                updateScore();
                            }
                        }
                    }
                }
            }
        }
    }

    private void updateScore() {
        currentScoreObj.score = currentScore;
        currentScoreObj.save();
        score.setText(currentScore + "");
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
}
