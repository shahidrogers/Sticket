package com.tropicthunder.sticket;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseUser;

import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

public class LoginActivity extends AppCompatActivity {

    private String username;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Login");

        //find views and allocate variables to them
        final Button loginBtn = (Button) findViewById(R.id.loginButton);
        final Button signupBtn = (Button) findViewById(R.id.signupButton);
        final EditText un = (EditText) findViewById(R.id.unTextField);
        final EditText pw = (EditText) findViewById(R.id.pwTextField);
        final SmoothProgressBar progBar = (SmoothProgressBar) findViewById(R.id.progressView);

        //when login button is pressed
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //get un and password text from EditText fields
                username = un.getText().toString();
                password = pw.getText().toString();

                //loading/progress bar??
                progBar.setSmoothProgressDrawableSpeed(1);
                progBar.setVisibility(View.VISIBLE);

                //login to parse
                ParseUser.logInInBackground(username, password, new LogInCallback() {
                    public void done(ParseUser user, ParseException e) {
                        if (user != null) {
                            //set username object
                            UserObj.getInstance().setUsername(username);

                            //dismiss loading bar


                            //change activity to main screen (MainActivity)
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            LoginActivity.this.finish();

                        } else {
                            //toast message
                            Toast toast = Toast.makeText(LoginActivity.this, "Error with your login!", Toast.LENGTH_LONG);
                            toast.show();
                        }
                    }
                });
            }
        });

        signupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //go to signup activity
                startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
            }
        });


    }
}
