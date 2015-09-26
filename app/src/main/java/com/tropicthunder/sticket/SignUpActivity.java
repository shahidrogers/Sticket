package com.tropicthunder.sticket;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

public class SignUpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Sign Up");

        final SmoothProgressBar progBar = (SmoothProgressBar) findViewById(R.id.progressView);
        progBar.setSmoothProgressDrawableSpeed(0);

    }

    public void signupOnClick(View view){
        // Create the ParseUser
        ParseUser user = new ParseUser();

        //loading/progress bar start
        SmoothProgressBar progBar = (SmoothProgressBar) findViewById(R.id.progressView);
        progBar.setSmoothProgressDrawableSpeed(1);
        progBar.setVisibility(View.VISIBLE);

        // Set core properties
        EditText text1 = (EditText) findViewById(R.id.unTextField);
        EditText text2 = (EditText) findViewById(R.id.pwTextField);
        EditText text3 = (EditText) findViewById(R.id.emailTextField);

        String username = text1.getText().toString();
        String password = text2.getText().toString();
        String email = text3.getText().toString();

        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);

        // Set custom properties
        user.put("credit", 0);
        // Invoke signUpInBackground
        user.signUpInBackground(new SignUpCallback() {
            public void done(ParseException e) {
                if (e == null) {
                    startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                    SignUpActivity.this.finish();
                } else {
                    Toast.makeText(SignUpActivity.this, "Fault: " + e, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

}