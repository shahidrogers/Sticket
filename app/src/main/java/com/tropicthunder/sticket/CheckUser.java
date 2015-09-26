package com.tropicthunder.sticket;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.parse.Parse;
import com.parse.ParseUser;

public class CheckUser extends AppCompatActivity {

    public static final String YOUR_APPLICATION_ID = "3zUnInahebnSfVPGbrLIBLflh1yY5Vx8xHq4EmHe";
    public static final String YOUR_CLIENT_KEY = "BUIxAMInkYrB3Wql8VFFwSlTap6UXrGUdRid9VGq";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_user);

        // Add your initialization code here
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, YOUR_APPLICATION_ID, YOUR_CLIENT_KEY);

        Intent intent;
        ParseUser currentUser = ParseUser.getCurrentUser();
        if(currentUser == null){
            intent = new Intent(this, LoginActivity.class);
        }else{
            intent = new Intent(this, MainActivity.class);
            //set username
            UserObj.getInstance().setUsername(currentUser.getUsername());
        }

        startActivity(intent);
        this.finish();
    }
}
