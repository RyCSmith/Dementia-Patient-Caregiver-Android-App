package dementiaapp.com.dementiaapp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class RemindersDbAdapter extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "reminders";

    // Scores table name
    private static final String TABLE_REMINDER_EVENTS = "reminderevents";

    // Scores Table Columns names
    private static final String KEY_NAME = "name";
    private static final String KEY_DATE = "date";

    private SQLiteDatabase mSqliteDb;

    public RemindersDbAdapter(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase mSqliteDb) {
        String CREATE_REMINDERS_TABLE = "CREATE TABLE " + TABLE_REMINDER_EVENTS + "("
                + KEY_NAME + " TEXT NOT NULL," + KEY_DATE + " DATETIME PRIMARY KEY NOT NULL)";
        mSqliteDb.execSQL(CREATE_REMINDERS_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase mSqliteDb, int oldVersion, int newVersion) {
        // Drop older table if existed
        mSqliteDb.execSQL("DROP TABLE IF EXISTS " + TABLE_REMINDER_EVENTS);

        // Create tables again
        onCreate(mSqliteDb);
    }

    public void open()
    {
        mSqliteDb = this.getWritableDatabase();
    }

    public void close()
    {
        mSqliteDb.close();
    }

    /**
     * All CRUD(Create, Read, Update, Delete) Operations
     */

    public void addEvent(String name, Date date)
    {
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, name);
        DateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        values.put(KEY_DATE, df.format(date));

        mSqliteDb.insert(TABLE_REMINDER_EVENTS, null, values);
    }

    public Date getEvent(String name)
    {
        Cursor cursor = mSqliteDb.query(TABLE_REMINDER_EVENTS, new String[] {KEY_NAME, KEY_DATE}, KEY_NAME + "= '" + name + "'", null, null, null, null);
        if (cursor != null)
        {
            if(cursor.moveToFirst())
            {
                String date = cursor.getString(cursor.getColumnIndex(KEY_DATE));
                cursor.close();
                DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                Date d = null;
                try {
                    d = formatter.parse(date);
                } catch (ParseException e) {
                }
                return d;
            }
        }
        return null;
    }

    public ArrayList<RemindersActivity.ReminderEvent> getAllEvents()
    {
        //mSqliteDb.delete(TABLE_REMINDER_EVENTS, null, null);
        //TODO: fix ordering
        ArrayList<RemindersActivity.ReminderEvent> allEvents = new ArrayList<RemindersActivity.ReminderEvent>();
        Cursor cursor = mSqliteDb.query(TABLE_REMINDER_EVENTS, new String[]{KEY_NAME, KEY_DATE}, null, null, null, null, null);
        if (!cursor.moveToFirst()) return allEvents;
        while(!cursor.isAfterLast())
        {
            String name = cursor.getString(cursor.getColumnIndex(KEY_NAME));
            String date = cursor.getString(cursor.getColumnIndex(KEY_DATE));
            DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            Date d = null;
            try {
                d = formatter.parse(date);
            } catch (ParseException e) {}
            allEvents.add(new RemindersActivity.ReminderEvent(name, d));
            cursor.moveToNext();
        }
        cursor.close();
        return allEvents;
    }

    public void updateDate(String name, Date date)
    {
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, name);
        values.put(KEY_DATE, date.toString());

        mSqliteDb.update(TABLE_REMINDER_EVENTS, values, KEY_NAME + "= '" + name + "'", null);
    }

}