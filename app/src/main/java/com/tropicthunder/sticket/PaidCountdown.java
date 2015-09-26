package com.tropicthunder.sticket;

import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.widget.TextView;

public class PaidCountdown extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paid_countdown);

        TextView minTxt = (TextView) findViewById(R.id.minTxt);
        TextView hrTxt = (TextView) findViewById(R.id.hrTxt);

        new CountDownTimer(30000, 1000) {

            public void onTick(long millisUntilFinished) {
                //mTextField.setText(millisUntilFinished / DateUtils.MINUTE_IN_MILLIS);
            }

            public void onFinish() {
                //mTextField.setText("done!");
            }
        }.start();
    }
}
