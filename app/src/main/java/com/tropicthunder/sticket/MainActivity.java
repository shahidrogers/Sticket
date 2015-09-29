package com.tropicthunder.sticket;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dd.CircularProgressButton;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.w3c.dom.Text;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

import static com.tropicthunder.sticket.LocationAddress.*;

public class MainActivity extends AppCompatActivity implements LocationListener {

    //GPS and MAP stuff
    private LocationManager locationManager;
    private double latitude = 0, longitude = 0;
    private GoogleMap map;
    //make sure that marker is only added once
    private boolean markerAdded = false;

    //textview variables
    private TextView addressTxt;
    private TextView noOfHrs;

    //actionbar variable
    private ActionBar actionBar;

    //Pay button variable
    private CircularProgressButton payBtn;

    //username variable
    private String username;

    //seekbar variable
    private SeekBar seek;

    //sliderBar for number of hours (value)
    private int progress = 0;

    //decimal format (2 decimal places for display of currency)
    private DecimalFormat fee = new DecimalFormat("0.00");

    //retrieve credit value variable
    private double creditValue;
    private double calc;

    //date object for end of parking ticket
    private Date end = new Date();

    private int noOfHours2;

    //loading refresher
    SweetAlertDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Sticket");
        }

        //loading bar
        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Getting location");
        pDialog.setCancelable(false);
        pDialog.show();

        updateCredit();

        //get current username
        username = UserObj.getInstance().getUsername();
        //TextView unTxt = (TextView) findViewById(R.id.unTxt);
        //set username to show carplate number
        //unTxt.setText(username);

        //set addressTxt textview variable
        addressTxt = (TextView) findViewById(R.id.addressTxt);

        //set payBtn item variable
        payBtn = (CircularProgressButton) findViewById(R.id.payBtn);
        payBtn.setProgress(-1);

        //initialise map + gps
        map = ((MapFragment) getFragmentManager()
                .findFragmentById(R.id.map)).getMap();
        map.getUiSettings().setScrollGesturesEnabled(false);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(3.1333,101.7000), 5.0f));

        //start locationManager
        MyLocation.LocationResult locationResult = new MyLocation.LocationResult(){
            @Override
            public void gotLocation(Location location){
                //Got the location!
                latitude = location.getLatitude();
                longitude = location.getLongitude();

                //change status to current address
                LocationAddress locationAddress = new LocationAddress();
                getAddressFromLocation(latitude, longitude,
                        getApplicationContext(), new GeocoderHandler());

                //add marker to map and zoom to location
                if(!markerAdded){ //ONLY IF MARKER NOT YET ADDED THE FIRST TIME
                    onMapReady(map);
                    //locationManager.removeUpdates(this); //stop GPS
                    markerAdded = true;
                }

                //indicate readyness to proceed
                //actionBar.setTitle("Ready to go, " + username); //change title
                payBtn.setProgress(0); //change button readyness
                //hide loading bar
                pDialog.hide();
                seek.setVisibility(View.VISIBLE);
            }
        };
        MyLocation myLocation = new MyLocation();
        myLocation.getLocation(this, locationResult);

        /* OLD LOCATION MANAGER CODE (slower)
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 1, this); //casted parameter
        */

        //no of hours + seekbar stuff~~~~~~~~
        seek = (SeekBar) findViewById(R.id.seekBar);
        noOfHrs = (TextView) findViewById(R.id.noOfHrsTxt);
        noOfHrs.setText(String.valueOf(seek.getProgress())); //update txtview with seekbar changes

        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                progress = i;
                noOfHrs.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                noOfHrs.setText(String.valueOf(progress));
                calc = 0.5 * (double) progress;
                payBtn.setText("Pay RM" + fee.format(calc));
            }
        });





    }

    @Override
    public void onLocationChanged(Location location) {

        //String msg = "New Latitude: " + location.getLatitude()
        //        + "New Longitude: " + location.getLongitude();
        //Toast.makeText(getBaseContext(), msg, Toast.LENGTH_LONG).show();

        latitude = location.getLatitude();
        longitude = location.getLongitude();

        //change status
        /*
        TextView statusTxt = (TextView) findViewById(R.id.textView);

        statusTxt.setText("Latitude: " + latitude
                + " Longitude: " + longitude);
        */

        //change status to current address

        LocationAddress locationAddress = new LocationAddress();
        getAddressFromLocation(latitude, longitude,
                getApplicationContext(), new GeocoderHandler());

        //add marker to map and zoom to location
        if(!markerAdded){ //ONLY IF MARKER NOT YET ADDED THE FIRST TIME
            onMapReady(map);
            locationManager.removeUpdates(this); //stop GPS
            markerAdded = true;
        }

        //indicate readyness to proceed
        //actionBar.setTitle("Ready to go, " + username); //change title
        payBtn.setProgress(0); //change button readyness
        //hide loading bar
        pDialog.hide();
        seek.setVisibility(View.VISIBLE);

    }

    public void onMapReady(GoogleMap map) {
        /*
            map.addMarker(new MarkerOptions()
                .position(new LatLng(0, 0))
                .title("Marker"));
        */

        LatLng userLocation = new LatLng(latitude, longitude);

        Marker carLocation = map.addMarker(new MarkerOptions().position(userLocation).title(username).snippet(""));
        carLocation.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.car));

        // zoom in the camera to car location
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 16));

        // animate the zoom process
        map.animateCamera(CameraUpdateFactory.zoomTo(16), 2000, null);
    }

    @Override
    public void onProviderDisabled(String provider) {

        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(intent);
        Toast.makeText(getBaseContext(), "GPS is off! ",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderEnabled(String provider) {

        Toast.makeText(getBaseContext(), "GPS is on! ",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub

    }

    private class GeocoderHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            String locationAddress;
            switch (message.what) {
                case 1:
                    Bundle bundle = message.getData();
                    locationAddress = bundle.getString("address");
                    break;
                default:
                    locationAddress = null;
            }
            addressTxt.setText(locationAddress);
        }
    }

    public void logOut(View view){
        ParseUser.logOut();
        ParseUser currentUser = ParseUser.getCurrentUser();
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
        MainActivity.this.finish();
    }

    public void buyTicket(View view){
        //set variables for activity items
        //NumberPicker hourPick = (NumberPicker) findViewById(R.id.hourPick);
        //payBtn.setProgress(50); //pay button progress state...

        //int hours = hourPick.getValue();

        //do parse shit here...
        if(progress==0){
            new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Oops!")
                    .setContentText("Please select the number of hours you wish to park for before making payment")
                    .setConfirmText("Ok")
                    .show();
        }else{
            //make sure there is sufficient credit before deduction
            if(!(calc > creditValue) && creditValue!=0){
                new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Are you sure?")
                        .setContentText("Transactions are non-refundable")
                        .setConfirmText("Yes, proceed")
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog) {
                                sDialog.dismissWithAnimation();
                                payBtnOnClick();
                                changeLayoutToPaid();
                                updateCredit();
                            }
                        })
                        .show();
            }else{
                new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Oops!")
                        .setContentText("Insufficient credit balance")
                        .setConfirmText("Ok")
                        .show();
            }
        }
    }

    public void reloadCredit(View view){
        startActivity(new Intent(MainActivity.this, CreditActivity.class));
        this.finish();
    }

    public void updateCredit(){
        final TextView credit = (TextView) findViewById(R.id.creditTxt);
        // Specify which class to query
        ParseQuery<ParseObject> query = ParseQuery.getQuery("_User");
        // Specify the object id
        query.whereEqualTo("username", UserObj.getInstance().getUsername());
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            public void done(ParseObject user, ParseException e) {
                if (user != null) {
                    // The query was successful.
                    creditValue = user.getDouble("credit");
                    credit.setText(String.valueOf(fee.format(creditValue)));
                } else {
                    // Something went wrong.
                    Log.d("Shit went wrong", e.toString());
                }
            }
        });
    }

    public void payBtnOnClick(){
        TextView creditTxt = (TextView) findViewById(R.id.creditTxt);
        final TextView locationTxt = (TextView) findViewById(R.id.addressTxt);
        TextView noOfHoursTxt = (TextView) findViewById(R.id.noOfHrsTxt);
        double credit = Double.parseDouble(creditTxt.getText().toString());
        final Integer noOfHours = Integer.parseInt(noOfHoursTxt.getText().toString());
        noOfHours2 = noOfHours;

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Parking");
        // Specify the object id
        query.whereEqualTo("username", UserObj.getInstance().getUsername());
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, com.parse.ParseException e) {
                if (object != null) {
                    // The query was successful.
                    Date endTime = object.getDate("parkEndDateAndTime");
                    Date currentTime = new Date();

                    Log.d("endtime:", endTime.toString());

                    if (currentTime.after(endTime)){
                        //purchase new ticket

                        //push parking row entry to db
                        ParseObject parking = new ParseObject("Parking");
                        parking.put("parkLocation", locationTxt.getText().toString());
                        Date current = new Date();

                        parking.put("parkDateAndTime", current);

                        parking.put("username", UserObj.getInstance().getUsername());

                        Date end2 = new Date();
                        end2.setTime(current.getTime() + (noOfHours * 3600000));
                        parking.put("parkEndDateAndTime", end2);
                        end.setTime(end2.getTime());
                        Log.d("Current Time:", end2.toString());

                        //push data to cloud
                        parking.saveInBackground();
                    } else {
                        //extend time of current ticket
                        Date current = object.getDate("parkEndDateAndTime");

                        Date end4 = new Date();
                        end4.setTime(current.getTime() + (noOfHours * 3600000));
                        object.put("parkEndDateAndTime", end4);
                        Log.d("Current Time:", end4.toString());
                        end.setTime(end4.getTime());
                        object.saveInBackground();

                    }



                } else {
                    // Ticket record does not exist yet
                    ParseObject parking = new ParseObject("Parking");
                    parking.put("parkLocation", locationTxt.getText().toString());
                    Date current = new Date();

                    parking.put("parkDateAndTime", current);

                    parking.put("username", UserObj.getInstance().getUsername());

                    Date end3 = new Date();
                    end3.setTime(current.getTime() + (noOfHours * 3600000));
                    parking.put("parkEndDateAndTime", end3);
                    Log.d("Current Time:", end3.toString());
                    end.setTime(end3.getTime());

                    //push data to cloud
                    parking.saveInBackground();
                }
            }

        });








        //update credit balance
        updateCredit();

        //deduct credit
        //update credit value
        final double[] creditValue = new double[1];

        // Specify which class to query
        ParseQuery<ParseObject> query2 = ParseQuery.getQuery("_User");
        // Specify the object id
        query2.whereEqualTo("username", UserObj.getInstance().getUsername());
        query2.getFirstInBackground(new GetCallback<ParseObject>() {
            public void done(ParseObject object, ParseException e) {
                if (object != null) {
                    // The query was successful.
                    creditValue[0] = object.getDouble("credit");
                    object.put("credit", creditValue[0] - calc);
                    object.saveInBackground();

                    //update credit balance??
                    updateCredit();

                } else {
                    // Something went wrong.
                    Log.d("Error", e.toString());
                }
            }
        });
    }

    public void changeLayoutToPaid(){
        //update credit bal
        updateCredit();

        RelativeLayout rl = (RelativeLayout) findViewById(R.id.payLayout);
        rl.setVisibility(View.GONE);
        RelativeLayout rlMain = (RelativeLayout) findViewById(R.id.paidLayout);
        rlMain.setVisibility(View.VISIBLE);

        //show ticket expiry
        final TextView expiryTxt = (TextView) findViewById(R.id.dateTxt);

        // Specify which class to query
        ParseQuery<ParseObject> query2 = ParseQuery.getQuery("Parking");
        // Specify the object id
        query2.whereEqualTo("username", UserObj.getInstance().getUsername());
        query2.orderByDescending("parkEndDateAndTime");
        Date[] endDateAndTime = {new Date()};

        query2.getFirstInBackground(new GetCallback<ParseObject>() {
            public void done(ParseObject object, ParseException e) {
                if (object != null) {
                    // The query was successful.
                    Log.d("uaqsgfiquf time", object.getDate("parkEndDateAndTime").toString());
                    //endDateAndTime[0].setTime(object.getDate("parkEndDateAndTime").getTime());
                    expiryTxt.setText(object.getDate("parkEndDateAndTime").toString());

                } else {
                    // Something went wrong
                    Date end = new Date();
                    end.setTime(end.getTime()+(noOfHours2*3600000));
                    expiryTxt.setText(end.toString());
                    Log.d("Error", e.toString());
                }
            }
        });

        //Log.d("End time: ", endDateAndTime[0].toString());
        //expiryTxt.setText(endDateAndTime[0].toString());
    }

    public void changeLayoutToMain(View view){
        RelativeLayout rlMain = (RelativeLayout) findViewById(R.id.payLayout);
        rlMain.setVisibility(View.VISIBLE);
        RelativeLayout rl = (RelativeLayout) findViewById(R.id.paidLayout);
        rl.setVisibility(View.GONE);

    }

}
