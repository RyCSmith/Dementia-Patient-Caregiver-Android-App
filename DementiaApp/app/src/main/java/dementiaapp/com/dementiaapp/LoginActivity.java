package dementiaapp.com.dementiaapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * Created by vishwa on 3/2/15.
 */
public class LoginActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.login_screen);

        Button loginButton = (Button) findViewById(R.id.login_button);
        Button adminButton = (Button) findViewById(R.id.admin_button);
        Button quitButton = (Button) findViewById(R.id.quit_button);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //this is where all logic takes place when loginButton is clicked
                //doesnt do anything now, just called handleSuccessfulLogin where details are stored
                //this would be a good place to switch to a user mode button
                openMenuActivity();
            }
        });

        adminButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(3000); //add sleep to increase the click time (200 = 0.2 seconds)
                                //Your code after the long click goes here
                            } catch (Exception e) {

                            }
                            openMenuActivityAdmin();
                        }
                    };
                    runnable.run();
                    return false;
            }
        });

        quitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //this is where all logic takes place when loginButton is clicked
                //doesnt do anything now, just called handleSuccessfulLogin where details are stored
                //this would be a good place to switch to a user mode button
                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);

                builder.setTitle("Confirm");
                builder.setMessage("Do you want to exit?");

                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing but close the dialog
                        finish();
                        dialog.dismiss();
                    }

                });

                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing
                        dialog.dismiss();
                    }
                });

                AlertDialog alert = builder.create();
                alert.show();
            }
        });
    }

    private void openMenuActivity() {
        Intent intent = new Intent();
        intent.setClass(this, MenuActivity.class);
        startActivity(intent);
    }

    private void openMenuActivityAdmin() {
        Intent intent = new Intent();
        intent.setClass(this, MenuActivityAdmin.class);
        startActivity(intent);
    }

}