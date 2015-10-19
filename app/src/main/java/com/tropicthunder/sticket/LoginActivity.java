package com.tropicthunder.sticket;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.Date;

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


                            ParseQuery<ParseObject> query = ParseQuery.getQuery("Parking");
                            // Specify the object id
                            query.whereEqualTo("username", username);

                            // Obtain the latest parking record of the current user
                            query.getFirstInBackground(new GetCallback<ParseObject>() {
                                @Override
                                public void done(ParseObject object, com.parse.ParseException e) {
                                    if (object != null) {
                                        // The query was successful.
                                        // Get the ending date and time of parking record
                                        Date endTime = object.getDate("parkEndDateAndTime");
                                        Date currentTime = new Date();
                                        Log.d("end time: ", endTime.toString());

                                        //Checks to see if last ticket has expired
                                        if (currentTime.after(endTime)) {
                                            UserObj.getInstance().setParkingStatus(false);
                                            //change activity to main screen (MainActivity)

                                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                            startActivity(intent);


                                            LoginActivity.this.finish();
                                        } else {
                                            UserObj.getInstance().setParkingStatus(true);
                                            //change activity to main screen (MainActivity)

                                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                            startActivity(intent);


                                            LoginActivity.this.finish();
                                        }

                                        Log.d("Status :", Boolean.toString(UserObj.getInstance().getParkingStatus()));

                                    } else {
                                        //The user has never parked before
                                        UserObj.getInstance().setParkingStatus(false);

                                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                        startActivity(intent);


                                        LoginActivity.this.finish();
                                    }
                                }

                            });




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
