package dementiaapp.com.dementiaapp;
import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.widget.ScrollView;
import android.widget.LinearLayout;
import android.widget.DatePicker;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;

import com.orhanobut.logger.Logger;

import java.util.Calendar;
import java.util.ArrayList;
import java.util.Date;
/**
 * Created by vishwa on 3/2/15.
 */
public class RemindersActivity extends Activity {
    private RemindersDbAdapter mRemindersDb;

    private int year;
    private int month;
    private int date;
    private int hour;
    private int minutes;

    public static class ReminderEvent {
        public ReminderEvent(String name, Date date) {
            eventDate = date;
            eventName = name;
        }
        private Date eventDate;
        private String eventName;
        public String getName() {
            return eventName;
        }
        public Date getDate() {
            return eventDate;
        }
    }
    private ArrayList<ReminderEvent> upcomingEvents;
    private void addToDataSource(ReminderEvent e) {
        addReminderEventView(e);
        upcomingEvents.add(e);
        mRemindersDb.addEvent(e.getName(), e.getDate());
    }
    private void addReminderEventView(ReminderEvent e) {
        LinearLayout contents = (LinearLayout) findViewById(R.id.lcontents);
        View reminderEvent = getLayoutInflater().inflate(R.layout.reminderevent_template, null);
        TextView eventNameView = (TextView) reminderEvent.findViewById(R.id.event_name);
        eventNameView.setText(e.getName());
        TextView eventDateView = (TextView) reminderEvent.findViewById(R.id.event_date);
        eventDateView.setText(e.getDate().toString());
        contents.addView(reminderEvent);
    }
    @Override
    @TargetApi(19)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminders);
        mRemindersDb = new RemindersDbAdapter(this.getApplicationContext());
        mRemindersDb.open();
        upcomingEvents = mRemindersDb.getAllEvents();
        Button dateTimePicker = (Button) findViewById(R.id.show_date_time_picker_button);
        dateTimePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(RemindersActivity.this);
                dialog.setContentView(R.layout.datetime_picker_dialog);
                dialog.setTitle("Reminder Date & Time");
                dialog.show();

                final DatePicker dp = (DatePicker) dialog.findViewById(R.id.date_picker);
                final TimePicker tp = (TimePicker) dialog.findViewById(R.id.time_picker);
                Button okButton = (Button) dialog.findViewById(R.id.datetime_picked_button);
                okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        year = dp.getYear();
                        month = dp.getMonth();
                        date = dp.getDayOfMonth();
                        hour = tp.getCurrentHour();
                        minutes = tp.getCurrentMinute();
                        Toast.makeText(RemindersActivity.this, hour +" " +minutes, Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                    }
                });
            }
        });
        Button addEvent = (Button) findViewById(R.id.create_reminder_button);
        addEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView eventName = (TextView) findViewById(R.id.eventName);
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, date, hour, minutes, 0);

                addToDataSource(new ReminderEvent(eventName.getText().toString(), calendar.getTime()));

                Intent alarmIntent = new Intent(getApplicationContext(), ReminderAlarmService.class);
                alarmIntent.putExtra("title", eventName.getText().toString());
                PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 0, alarmIntent, 0);
                AlarmManager manager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
                manager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),pendingIntent);

            }
        });
        for (ReminderEvent e : upcomingEvents) {
            addReminderEventView(e);
        }
    }
}