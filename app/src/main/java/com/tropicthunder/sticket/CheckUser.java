package com.tropicthunder.sticket;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.parse.Parse;
import com.parse.ParseUser;

public class CheckUser extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_user);

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
