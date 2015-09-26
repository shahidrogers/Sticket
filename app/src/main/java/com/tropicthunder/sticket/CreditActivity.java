package com.tropicthunder.sticket;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class CreditActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credit);
    }

    public void reloadOnClick(View view){
        //credit textbox variable
        EditText credit = (EditText) findViewById(R.id.reloadTextBox);

        //update credit value
        final double[] creditValue = new double[1];
        String credittext = credit.getText().toString();
        final double topupvalue = Double.parseDouble(credittext);
        // Specify which class to query
        ParseQuery<ParseObject> query = ParseQuery.getQuery("_User");
        // Specify the object id
        query.whereEqualTo("username", UserObj.getInstance().getUsername());
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            public void done(ParseObject object, ParseException e) {
                if (object != null) {
                    // The query was successful.
                    creditValue[0] = object.getDouble("credit");
                    object.put("credit", creditValue[0] + topupvalue);
                    object.saveInBackground();
                    //prompt success message
                    successPrompt();
                } else {
                    // Something went wrong.
                    Log.d("Error", e.toString());
                }
            }
        });

    }

    public void successPrompt(){
        //prompt success
        new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText("Good job!")
                .setContentText("You clicked the button!")
                .show();

        new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText("Transaction succesful")
                .setContentText("Thank you for reloading.")
                .setConfirmText("Continue")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.dismissWithAnimation();
                        startActivity(new Intent(CreditActivity.this, MainActivity.class));
                    }
                })
                .show();
    }
}
