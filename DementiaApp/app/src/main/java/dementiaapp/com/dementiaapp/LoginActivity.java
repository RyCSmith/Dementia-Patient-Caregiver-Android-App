package dementiaapp.com.dementiaapp;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.IconTextView;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

/**
 * Created by vishwa on 3/2/15.
 */
public class LoginActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean isLoggedIn = getApplication().getSharedPreferences("MEMAID", Context.MODE_PRIVATE).getBoolean("LOGGED_IN", false);

        if(isLoggedIn) {
            openMenuActivity();

        } else {
            setContentView(R.layout.login_screen);

            final EditText emailField = (EditText) findViewById(R.id.email_field);
            final EditText passwordField = (EditText) findViewById(R.id.password_field);
            final EditText patientNameField = (EditText) findViewById(R.id.patient_name_field);
            final EditText caregiverPhoneNumber = (EditText) findViewById(R.id.caregiver_phone_number);
            final IconTextView questionTooltip = (IconTextView) findViewById(R.id.question_tooltip);
            questionTooltip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showBasicAlertDialog("Why we need your phone number",
                            "If you would like to monitor the patient for wandering then you can create areas where the patient is known to be safe," +
                                    "these are called 'safe zones'. By providing us with your phone number we can send you an SMS when the patient leaves their" +
                                    "safe zones and this way you can quickly find them if you suspect they might be wandering");
                }
            });
            Button loginButton = (Button) findViewById(R.id.login_button);

            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Switch locationTrackingSwitch = (Switch) findViewById(R.id.allow_location_tracking_switch);
                    if (emailField.getText().toString().isEmpty()
                            || passwordField.getText().toString().isEmpty()
                            || patientNameField.getText().toString().isEmpty()) {
                        showBasicAlertDialog("Error", "You left one of the three fields empty. Please enter your email, password, and the patient\'s name");
                    } else if ((locationTrackingSwitch.isChecked() && caregiverPhoneNumber.getText().toString().isEmpty())) {
                        showBasicAlertDialog("Error", "If you would like track the patient's location for wandering then you must include a phone number for notifications when the patient leaves their safe zone");
                    } else {
                        final ParseUser user = new ParseUser();
                        user.setEmail(emailField.getText().toString());
                        user.setPassword(passwordField.getText().toString());
                        user.setUsername(emailField.getText().toString());
                        user.put("patientName", patientNameField.getText().toString());

                        user.signUpInBackground(new SignUpCallback() {

                            @Override
                            public void done(ParseException e) {
                                if (e != null) {
                                    user.logInInBackground(emailField.getText().toString(), passwordField.getText().toString(), new LogInCallback() {
                                        @Override
                                        public void done(ParseUser parseUser, ParseException e) {
                                            if (e != null) {
                                                String errorMessage = e.getMessage();
                                                showBasicAlertDialog("Error ", errorMessage.substring(0, 1).toUpperCase() + errorMessage.substring(1));
                                            } else {
                                                handleSuccessfulLogin();
                                            }
                                        }
                                    });
                                } else {
                                    handleSuccessfulLogin();
                                }
                            }

                        });
                    }
                }
            });
        }
    }

    private void handleSuccessfulLogin() {
        SharedPreferences.Editor sharedPreferences = getApplication().getSharedPreferences("MEMAID", Context.MODE_PRIVATE).edit();
        sharedPreferences.putBoolean("LOGGED_IN", true);

        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.user_type_radio_group);
        String userType = ((RadioButton) findViewById(radioGroup.getCheckedRadioButtonId())).getText().toString().toLowerCase();
        sharedPreferences.putString("USER_TYPE", userType);
        sharedPreferences.putString("PATIENT_NAME", ((EditText) findViewById(R.id.patient_name_field)).getText().toString());
        sharedPreferences.putString("CAREGIVER_PHONE_NUMBER", ((EditText) findViewById(R.id.patient_name_field)).getText().toString());
        sharedPreferences.commit();

        Switch locationTrackingSwitch = (Switch) findViewById(R.id.allow_location_tracking_switch);
        sharedPreferences.putBoolean(Constants.LOCATION_TRACKING_APPROVED_KEY, locationTrackingSwitch.isChecked());
        sharedPreferences.commit();

        openMenuActivity();
    }

    private void openMenuActivity() {
        Intent intent = new Intent();
        intent.setClass(this, MenuActivity.class);
        startActivity(intent);

        finish();
    }

    private void showBasicAlertDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
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
