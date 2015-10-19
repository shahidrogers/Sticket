package com.tropicthunder.sticket;

import android.content.Intent;
import android.net.ParseException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.Date;

public class CheckUser extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_user);


        ParseUser currentUser = ParseUser.getCurrentUser();

        //checks if current user has logged in previously
        if(currentUser == null){
            Intent intent = new Intent(this, LoginActivity.class);
            UserObj.getInstance().setParkingStatus(false);

            startActivity(intent);
            this.finish();
        }else{

            //set username
            UserObj.getInstance().setUsername(currentUser.getUsername());

            ParseQuery<ParseObject> query = ParseQuery.getQuery("Parking");
            // Specify the object id
            query.whereEqualTo("username", UserObj.getInstance().getUsername());

            // Obtain the latest parking record of the current user
            query.getFirstInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject object, com.parse.ParseException e) {
                    if (object != null) {
                        // The query was successful.
                        // Get the ending date and time of parking record
                        Date endTime = object.getDate("parkEndDateAndTime");
                        Date currentTime = new Date();

                        //Checks to see if last ticket has expired
                        if (currentTime.after(endTime)) {
                            UserObj.getInstance().setParkingStatus(false);
                            Intent intent = new Intent(CheckUser.this, MainActivity.class);
                            startActivity(intent);
                            CheckUser.this.finish();
                        } else {
                            UserObj.getInstance().setParkingStatus(true);
                            Intent intent = new Intent(CheckUser.this, MainActivity.class);
                            startActivity(intent);
                            CheckUser.this.finish();
                        }

                    } else {
                        //The user has never parked before
                        UserObj.getInstance().setParkingStatus(false);
                        Intent intent = new Intent(CheckUser.this, MainActivity.class);
                        startActivity(intent);
                        CheckUser.this.finish();
                    }
                }

            });


        }


    }
}
