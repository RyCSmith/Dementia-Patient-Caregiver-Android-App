package dementiaapp.com.dementiaapp;

import android.app.Activity;
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

        adminButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //this is where all logic takes place when loginButton is clicked
                //doesnt do anything now, just called handleSuccessfulLogin where details are stored
                //this would be a good place to switch to a user mode button
                openMenuActivityAdmin();
            }
        });

        quitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //this is where all logic takes place when loginButton is clicked
                //doesnt do anything now, just called handleSuccessfulLogin where details are stored
                //this would be a good place to switch to a user mode button
                finish();
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