package dementiaapp.com.dementiaapp;

import se.emilsjolander.sprinkles.Model;
import se.emilsjolander.sprinkles.annotations.Column;
import se.emilsjolander.sprinkles.annotations.Key;
import se.emilsjolander.sprinkles.annotations.Table;

/**
 * Created by vishwa on 4/1/15.
 */
@Table("Stimulus")
public class Stimulus extends Model {

    public static final int TYPE_AUDIO = 0;
    public static final int TYPE_IMAGE = 1;

    @Key
    @Column("id")
    public String id;

    @Column("type")
    public int type;

    @Column("question_filepath")
    public String questionFilepath;

    // This is actually a list of the possible results returned by the speech recognizer
    @Column("possible_answers")
    public String possibleAnswers;

    // RETRIEVE THIS USING getLong
    @Column("created_at")
    public long createdAt;

    public String getId() {
        return id;
    }

}
