package dementiaapp.com.dementiaapp;

import se.emilsjolander.sprinkles.Model;
import se.emilsjolander.sprinkles.annotations.Column;
import se.emilsjolander.sprinkles.annotations.Key;

/**
 * Created by vishwa on 5/6/15.
 */
public class Safezone extends Model {

    @Key
    @Column("id")
    public String id;

    @Column("safezone_name")
    public String safezoneName;

    @Column("latitude")
    public double latitude;

    @Column("longitude")
    public double longitude;

    @Column("radius")
    public double radius;
}
