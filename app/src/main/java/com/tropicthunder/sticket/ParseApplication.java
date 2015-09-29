package com.tropicthunder.sticket;

import android.app.Application;

import com.parse.Parse;

public class ParseApplication extends Application {

    public static final String YOUR_APPLICATION_ID = "3zUnInahebnSfVPGbrLIBLflh1yY5Vx8xHq4EmHe";
    public static final String YOUR_CLIENT_KEY = "BUIxAMInkYrB3Wql8VFFwSlTap6UXrGUdRid9VGq";

    @Override
    public void onCreate() {
        super.onCreate();

        //ParseCrashReporting.enable(this);
        // Add your initialization code here
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, YOUR_APPLICATION_ID, YOUR_CLIENT_KEY);
    }
}
