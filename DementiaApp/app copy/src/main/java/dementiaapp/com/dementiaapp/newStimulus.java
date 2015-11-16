package dementiaapp.com.dementiaapp;

import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by joshkessler on 11/9/15. Replaces preexisting Stimulus class that required database connection rather than local storage.
 */
public class newStimulus {
    private String stimulusName;
    private String audioPrompt;
    private String correctAnswerAsAudio;
    private String stimulusImage;
    private String onCorrectResponseAudio;
    private String onIncorrectResponseAudio;
    private ArrayList<String> possibleCorrectResponses;
    private boolean customImage = false;
    private boolean customIncorrectResponseAudio = false;
    private boolean customCorrectResponseAudio = false;


    public newStimulus(String stimulusName, String defaultDir, boolean customImage, boolean customIncorrectResponseAudio,
                       boolean customCorrectResponseAudio){
        if (!stimulusName.endsWith("/")) {
            stimulusName = stimulusName + "/";
        }

        this.stimulusName = stimulusName;

        audioPrompt = stimulusName + "question.mp3";
        correctAnswerAsAudio = stimulusName + "answer.mp3";
        possibleCorrectResponses = readPossibleResponsesFromFile(stimulusName + "possibleAnswers.txt");
        if (customImage) {
            stimulusImage = stimulusName + "photo.jpg";
            this.customImage = true;
        }

        if (customIncorrectResponseAudio) {
            onIncorrectResponseAudio = stimulusName + "onIncorrectResponseAudio.mp3";
            this.customIncorrectResponseAudio = true;
        }

        if (customCorrectResponseAudio) {
            onCorrectResponseAudio = stimulusName + "correctFeedback.mp3";
            this.customCorrectResponseAudio = true;
        }
    }

    private ArrayList<String> readPossibleResponsesFromFile(String filePath) {
        ArrayList<String> results = new ArrayList<>();
        File file = new File(filePath);

        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                results.add(line);
            }
        } catch(IOException e){
            e.printStackTrace();
        }
        return results;

    }

    public void addCustomIncorrectResponseAudio(){
        onIncorrectResponseAudio = stimulusName + "onIncorrectResponseAudio.mp3";
    }

    public void addCustomCorrectResponseAudio(){
        onCorrectResponseAudio = stimulusName + "defaultCorrectResponseAudio.mp3";
    }

    public void addCustomImage(){
        stimulusImage = stimulusName + "photo.jpg";
    }

    public String getStimulusName(){
        return stimulusName;
    }

    public String getAudioPrompt(){
        return audioPrompt;
    }

    public String getCorrectAnswerAsAudio(){
        return correctAnswerAsAudio;
    }
    public String getStimulusImage(){
        return stimulusImage;
    }

    public String getOnCorrectResponseAudio(){
        return onCorrectResponseAudio;
    }
    public String getOnIncorrectResponseAudio(){
        return onIncorrectResponseAudio;
    }

    public boolean hasCustomImage(){
        return  customImage;
    }

    public boolean hasCustomIncorrectResponseAudio(){
        return customIncorrectResponseAudio;
    }

    public boolean hasCustomCorrectResponseAudio(){
        return customCorrectResponseAudio;
    }

    public ArrayList<String> getPossibleCorrectAnswers(){
        return possibleCorrectResponses;
    }

}