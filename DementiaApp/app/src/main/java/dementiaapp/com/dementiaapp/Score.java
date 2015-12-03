package dementiaapp.com.dementiaapp;

import se.emilsjolander.sprinkles.Model;

/**
 * Created by vishwa on 4/21/15.
 */

public class Score extends Model {


    public String id;


    public int score;


    public int incorrect;

    public String getId() {
        return id;
    }
}
