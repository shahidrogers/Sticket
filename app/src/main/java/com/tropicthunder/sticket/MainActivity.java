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
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
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

public class MainActivity extends AppCompatActivity implements com.google.android.gms.location.LocationListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    //GPS and MAP stuff
    //private LocationManager locationManager;
    private double latitude = 0, longitude = 0;
    private GoogleMap map;
    //make sure that marker is only added once
    private boolean markerAdded = false;

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    /**
     * Provides the entry point to Google Play services.
     */
    private GoogleApiClient mGoogleApiClient;

    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    private LocationRequest mLocationRequest;

    /**
     * Represents a geographical location.
     */
    private Location mCurrentLocation;

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

        //set addressTxt textview variable
        addressTxt = (TextView) findViewById(R.id.addressTxt);

        //set payBtn item variable
        payBtn = (CircularProgressButton) findViewById(R.id.payBtn);
        payBtn.setProgress(-1);

        //initialise map
        map = ((MapFragment) getFragmentManager()
                .findFragmentById(R.id.map)).getMap();
        map.getUiSettings().setScrollGesturesEnabled(false);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(3.1333,101.7000), 5.0f));

        //new gps code
        // Kick off the process of building a GoogleApiClient and requesting the LocationServices
        // API.
        buildGoogleApiClient();



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

        //check if user has a running parking ticket
        if (UserObj.getInstance().getParkingStatus() == true){
            changeLayoutToPaid();
        }

        Log.d("Oncreate Status :", Boolean.toString(UserObj.getInstance().getParkingStatus()));



    }

    /**
     * Builds a GoogleApiClient. Uses the {@code #addApi} method to request the
     * LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        //Log.i(TAG, "Building GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }

    /**
     * Sets up the location request. Android has two location request settings:
     * {@code ACCESS_COARSE_LOCATION} and {@code ACCESS_FINE_LOCATION}. These settings control
     * the accuracy of the current location. This sample uses ACCESS_FINE_LOCATION, as defined in
     * the AndroidManifest.xml.
     * <p/>
     * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
     * interval (5 seconds), the Fused Location Provider API returns location updates that are
     * accurate to within a few feet.
     * <p/>
     * These settings are appropriate for mapping applications that show real-time location
     * updates.
     */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onLocationChanged(Location location) {
        //set lat and long location variables
        latitude = location.getLatitude();
        longitude = location.getLongitude();

        //change status to current address
        LocationAddress locationAddress = new LocationAddress();
        getAddressFromLocation(latitude, longitude,
                getApplicationContext(), new GeocoderHandler());

        //add marker to map and zoom to location
        if (!markerAdded) { //ONLY IF MARKER NOT YET ADDED THE FIRST TIME
            onMapReady(map);
            mGoogleApiClient.disconnect(); //stop GPS?
            //locationManager.removeUpdates(this); //stop GPS
            markerAdded = true;

        }

        //indicate readiness to proceed
        //actionBar.setTitle("Ready to go, " + username); //change title
        payBtn.setProgress(0); //change button to READY
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

    protected void startLocationUpdates() {
        PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    /**
     * Removes location updates from the FusedLocationApi.
     */
    protected void stopLocationUpdates() {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.

        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();

        //check if user has a running parking ticket
        if (UserObj.getInstance().getParkingStatus() == true){
            changeLayoutToPaid();
        }

        Log.d("Onstart Status :", Boolean.toString(UserObj.getInstance().getParkingStatus()));
    }

    @Override
    public void onResume() {
        super.onResume();
        // Within {@code onPause()}, we pause location updates, but leave the
        // connection to GoogleApiClient intact.  Here, we resume receiving
        // location updates if the user has requested them.

        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
        }

        //check if user has a running parking ticket
        if (UserObj.getInstance().getParkingStatus() == true){
            changeLayoutToPaid();
        }

        Log.d("Onresume Status :", Boolean.toString(UserObj.getInstance().getParkingStatus()));
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop location updates to save battery, but don't disconnect the GoogleApiClient object.
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();

        super.onStop();
    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        // If the initial location was never previously requested, we use
        // FusedLocationApi.getLastLocation() to get it. If it was previously requested, we store
        // its value in the Bundle and check for it in onCreate(). We
        // do not request it again unless the user specifically requests location updates by pressing
        // the Start Updates button.
        //
        // Because we cache the value of the initial location in the Bundle, it means that if the
        // user launches the activity,
        // moves to a new location, and then changes the device orientation, the original location
        // is displayed as the activity is re-created.
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

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

        UserObj.getInstance().setParkingStatus(false);
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
        MainActivity.this.finish();
    }

    public void buyTicket(View view){
        //set variables for activity items
        //NumberPicker hourPick = (NumberPicker) findViewById(R.id.hourPick);
        //payBtn.setProgress(50); //pay button progress state...

        //int hours = hourPick.getValue();

        //do parse stuff here...
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
                    Log.d("Stuff went wrong", e.toString());
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

        // Obtain the latest parking record of the current user
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, com.parse.ParseException e) {

                //Check if a record exists, if it does, check if should update or create new record
                if (object != null) {
                    // The query was successful.
                    // Get the ending date and time of parking record
                    Date endTime = object.getDate("parkEndDateAndTime");
                    Date currentTime = new Date();

                    Log.d("endtime:", endTime.toString());


                    // Check if latest parking record is expired
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

                        //push data to cloud
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
                    // Update user records after deducting parking transaction
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

        // Get latest parking ticket to print ending date and time
        query2.getFirstInBackground(new GetCallback<ParseObject>() {
            public void done(ParseObject object, ParseException e) {
                if (object != null) {
                    // The query was successful.
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
