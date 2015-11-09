package dementiaapp.com.dementiaapp;

import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import se.emilsjolander.sprinkles.Migration;
import se.emilsjolander.sprinkles.Sprinkles;

/**
 * Created by vishwa on 3/2/15.
 */
public class MemAidApplication extends Application {

        @Override
        public void onCreate() {
            super.onCreate();

            Sprinkles sprinkles = Sprinkles.init(getApplicationContext());

            sprinkles.addMigration(new Migration() {
                @Override
                protected void onPreMigrate() {
                    // do nothing
                }

                @Override
                protected void doMigration(SQLiteDatabase db) {
                    db.execSQL(
                            "CREATE TABLE Stimulus (" +
                                    "id TEXT PRIMARY KEY," +
                                    "type INTEGER," +
                                    "question_filepath TEXT,"+
                                    "possible_answers TEXT," +
                                    "created_at INTEGER" +
                                    ")"
                    );

                    db.execSQL(
                            "CREATE TABLE Score (" +
                                    "id TEXT PRIMARY KEY," +
                                    "score INTEGER" +
                                    ")"
                    );

                    db.execSQL(
                            "CREATE TABLE Safezone (" +
                                    "id TEXT PRIMARY KEY," +
                                    "safezone_name TEXT," +
                                    "latitude INTEGER," +
                                    "longitude INTEGER," +
                                    "score INTEGER" +
                                    ")"
                    );
                }

                @Override
                protected void onPostMigrate() {
                    // do nothing
                }
            });
        }

        /*
         * Returns either 'patient' or 'caretaker' depending on the type of the user chosen at login
         */
        public String getUserType() {
            return getSharedPreferences("MEMAID", Context.MODE_PRIVATE).getString("USER_TYPE", Constants.USER_TYPE_PATIENT);
        }

        public String getPatientName() {
            return getSharedPreferences("MEMAID", Context.MODE_PRIVATE).getString("PATIENT_NAME", "");
        }

        public String getCaregiverPhoneNumber() {
            return getSharedPreferences("MEMAID", Context.MODE_PRIVATE).getString("CAREGIVER_PHONE_NUMBER", "");
        }
    }
