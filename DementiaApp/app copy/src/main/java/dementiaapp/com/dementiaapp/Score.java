package dementiaapp.com.dementiaapp;

import se.emilsjolander.sprinkles.Model;
import se.emilsjolander.sprinkles.annotations.Column;
import se.emilsjolander.sprinkles.annotations.Key;
import se.emilsjolander.sprinkles.annotations.Table;

/**
 * Created by vishwa on 4/21/15.
 */
@Table("Score")
public class Score extends Model {

    @Key
    @Column("id")
    public String id;

    @Column("score")
    public int score;

    @Column("incorrect")
    public int incorrect;

    public String getId() {
        return id;
    }
}
