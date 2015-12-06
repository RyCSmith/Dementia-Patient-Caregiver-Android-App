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
    private ArrayList<String> possibleCorrectResponses;
    private boolean customImage = false;
    int numCorrect;
    int numAsked;

    public newStimulus(String stimulusName, boolean customImage, int numAsked, int numCorrect) {
        if(!stimulusName.endsWith("/")) {
            stimulusName = stimulusName + "/";
        }
        this.numCorrect = numCorrect;
        this.numAsked = numAsked;
        this.stimulusName = stimulusName;

        audioPrompt = stimulusName + "question.mp3";
        correctAnswerAsAudio = stimulusName + "answer.amr";
        possibleCorrectResponses = readPossibleResponsesFromFile(stimulusName + "possibleAnswers.txt");
        if(customImage) {
            stimulusImage = stimulusName + "photo.jpg";
            this.customImage = true;
        }
        else {
            this.customImage = true;
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

    public void addCustomImage() {
        stimulusImage = stimulusName + "photo.jpg";
    }

    public String getStimulusName() {
        return stimulusName;
    }

    public int getNumCorrect() {
        return numCorrect;
    }

    public int getNumAsked() {
        return numAsked;
    }

    public String getAudioPrompt() {
        return audioPrompt;
    }

    public String getCorrectAnswerAsAudio() {
        return correctAnswerAsAudio;
    }
    public String getStimulusImage() {
        return stimulusImage;
    }

    public boolean hasCustomImage(){
        return customImage;
    }

    public ArrayList<String> getPossibleCorrectAnswers() {
        return possibleCorrectResponses;
    }

}
